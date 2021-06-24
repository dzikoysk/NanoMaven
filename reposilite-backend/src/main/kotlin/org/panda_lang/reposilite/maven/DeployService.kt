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

import io.javalin.http.HttpCode
import net.dzikoysk.dynamiclogger.Journalist
import net.dzikoysk.dynamiclogger.Logger
import org.panda_lang.reposilite.failure.api.ErrorResponse
import org.panda_lang.reposilite.failure.api.errorResponse
import org.panda_lang.reposilite.maven.api.DeployRequest
import org.panda_lang.reposilite.maven.api.FileDetailsResponse
import org.panda_lang.utilities.commons.function.Result

internal class DeployService(
    private val journalist: Journalist,
    private val repositoryService: RepositoryService,
    private val metadataService: MetadataService
) : Journalist {

    fun deployArtifact(deployRequest: DeployRequest): Result<FileDetailsResponse, ErrorResponse> {
        val repository = repositoryService.getRepository(deployRequest.repository) ?: return errorResponse(HttpCode.NOT_FOUND, "Repository not found")

        if (!repository.isDeployEnabled) {
            return errorResponse(HttpCode.METHOD_NOT_ALLOWED, "Artifact deployment is disabled")
        }

        if (repository.isFull()) {
            return errorResponse(HttpCode.INSUFFICIENT_STORAGE, "Not enough storage space available")
        }

        val path = repository.relativize(deployRequest.gav) ?: return errorResponse(HttpCode.BAD_REQUEST, "Invalid GAV")
        val metadataFile = path.resolveSibling(METADATA_FILE)
        metadataService.clearMetadata(metadataFile)

        return try {
            val result: Result<FileDetailsResponse, ErrorResponse> =
                if (path.fileName.toString().contains(METADATA_FILE_NAME)) {
                    metadataService.getMetadata(repository, metadataFile).map { it.key }
                }
                else {
                    repository.putFile(path, deployRequest.content)
                }

            result.peek { logger.info("DEPLOY Artifact successfully deployed $path by ${deployRequest.by}") }
        }
        catch (exception: Exception) {
            errorResponse(HttpCode.INTERNAL_SERVER_ERROR, "Failed to upload artifact")
        }
    }

    override fun getLogger(): Logger =
        journalist.logger

}