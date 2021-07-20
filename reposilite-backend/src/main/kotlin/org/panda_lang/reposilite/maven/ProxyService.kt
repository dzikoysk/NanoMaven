/*
 * Copyright (c) 2021 dzikoysk
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.panda_lang.reposilite.maven

import com.google.api.client.http.GenericUrl
import com.google.api.client.http.HttpRequestFactory
import com.google.api.client.http.HttpResponse
import com.google.api.client.http.javanet.NetHttpTransport
import io.javalin.http.HttpCode
import org.panda_lang.reposilite.ReposiliteException
import org.panda_lang.reposilite.failure.FailureFacade
import org.panda_lang.reposilite.failure.api.ErrorResponse
import org.panda_lang.reposilite.failure.api.errorResponse
import org.panda_lang.reposilite.maven.api.DocumentInfo
import org.panda_lang.reposilite.maven.api.FileDetails
import org.panda_lang.reposilite.maven.api.Repository
import org.panda_lang.reposilite.maven.api.RepositoryVisibility.PRIVATE
import org.panda_lang.reposilite.storage.StorageProvider
import org.panda_lang.reposilite.web.ReposiliteContext
import org.panda_lang.reposilite.web.asResult
import panda.std.Option
import panda.std.Result
import panda.utilities.StringUtils
import java.io.IOException
import java.net.SocketTimeoutException
import java.nio.file.Paths
import java.util.Collections
import java.util.concurrent.CompletableFuture

internal class ProxyService(
    private val storeProxied: Boolean,
    private val proxyPrivate: Boolean,
    private val proxyConnectTimeout: Int,
    private val proxyReadTimeout: Int,
    private val proxied: List<String>,
    private val repositoryService: RepositoryService,
    private val failureFacade: FailureFacade,
    private val storageProvider: StorageProvider
) {

    private val httpRequestFactory: HttpRequestFactory = NetHttpTransport().createRequestFactory()

    fun findProxied(context: ReposiliteContext): Result<FileDetails, ErrorResponse> {
        var uri = context.uri
        var repository: Repository? = null

        for (localRepository in repositoryService.getRepositories()) {
            if (uri.startsWith("/" + localRepository.name)) {
                repository = localRepository
                uri = uri.substring(1 + localRepository.name.length)
                break
            }
        }

        if (repository == null) {
            return errorResponse(HttpCode.NOT_FOUND, "Unknown repository")
        }

        if (!proxyPrivate && repository.visibility == PRIVATE) {
            return errorResponse(HttpCode.NOT_FOUND, "Proxying is disabled in private repositories")
        }

        // /groupId/artifactId/<content>
        if (StringUtils.countOccurrences(uri, "/") < 3) {
            return errorResponse(HttpCode.NON_AUTHORITATIVE_INFORMATION, "Invalid proxied request")
        }

        val remoteUri = uri
        val list: MutableList<CompletableFuture<Void>> = ArrayList()
        val responses = Collections.synchronizedList(ArrayList<HttpResponse>())

        for (proxied in proxied) {
            list.add(CompletableFuture.runAsync {
                try {
                    val remoteRequest = httpRequestFactory.buildGetRequest(GenericUrl(proxied + remoteUri))
                    remoteRequest.throwExceptionOnExecuteError = false
                    remoteRequest.connectTimeout = proxyConnectTimeout * 1000
                    remoteRequest.readTimeout = proxyReadTimeout * 1000
                    val remoteResponse = remoteRequest.execute()
                    val headers = remoteResponse.headers

                    if ("text/html" == headers.contentType) {
                        return@runAsync
                    }

                    if (remoteResponse.isSuccessStatusCode) {
                        responses.add(remoteResponse)
                    }
                }
                catch (exception: Exception) {
                    val message = "Proxied repository " + proxied + " is unavailable due to: " + exception.message
                    context.logger.error(message)

                    if (exception !is SocketTimeoutException) {
                        failureFacade.throwException(remoteUri, ReposiliteException(message, exception))
                    }
                }
            })
        }

        CompletableFuture.allOf(*list.toTypedArray<CompletableFuture<*>>()).join()

        return if (responses.isNotEmpty()) {
            val remoteResponse = responses[0]

            if (context.method != "HEAD") {
                if (storeProxied) {
                    return store(remoteUri, remoteResponse, context)
                }
            }

            val contentLength = Option.of(remoteResponse.headers.contentLength).orElseGet(0L)
            val path = remoteUri.split("/").toTypedArray()

            DocumentInfo(path.last(), remoteResponse.contentType, contentLength) {
                remoteResponse.content
            }.asResult()
        }
        else errorResponse(HttpCode.NOT_FOUND, "Artifact $uri not found")
    }

    private fun store(uri: String, remoteResponse: HttpResponse, context: ReposiliteContext): Result<FileDetails, ErrorResponse> {
        var uri = uri

        if (storageProvider.isFull()) {
            val error = "Not enough storage space available for $uri"
            context.logger.warn(error)
            return errorResponse(HttpCode.INSUFFICIENT_STORAGE, error)
        }

        val repositoryName = StringUtils.split(uri.substring(1), "/")[0] // skip first path separator

        val repository = repositoryService.getRepository(repositoryName)
            ?: return errorResponse(HttpCode.BAD_REQUEST, "Missing valid repository name")

        val proxiedFile = Paths.get(uri)

        return try {
            storageProvider.putFile(proxiedFile, remoteResponse.content)
                .peek {
                    context.logger.info("Stored proxied $proxiedFile from ${remoteResponse.request.url}")
                }
        }
        catch (ioException: IOException) {
            errorResponse(HttpCode.UNPROCESSABLE_ENTITY, "Cannot process artifact")
        }
    }

}