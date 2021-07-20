package org.panda_lang.reposilite.maven.infrastructure

import com.dzikoysk.openapi.annotations.HttpMethod
import com.dzikoysk.openapi.annotations.OpenApi
import com.dzikoysk.openapi.annotations.OpenApiContent
import com.dzikoysk.openapi.annotations.OpenApiParam
import com.dzikoysk.openapi.annotations.OpenApiResponse
import io.javalin.http.HttpCode.NO_CONTENT
import org.panda_lang.reposilite.failure.api.ErrorResponse
import org.panda_lang.reposilite.failure.api.errorResponse
import org.panda_lang.reposilite.maven.MavenFacade
import org.panda_lang.reposilite.maven.api.DeployRequest
import org.panda_lang.reposilite.maven.api.DirectoryInfo
import org.panda_lang.reposilite.maven.api.DocumentInfo
import org.panda_lang.reposilite.maven.api.FileDetails
import org.panda_lang.reposilite.maven.api.LookupRequest
import org.panda_lang.reposilite.web.api.MimeTypes.MULTIPART_FORM_DATA
import org.panda_lang.reposilite.web.api.Route
import org.panda_lang.reposilite.web.api.RouteMethod.DELETE
import org.panda_lang.reposilite.web.api.RouteMethod.GET
import org.panda_lang.reposilite.web.api.RouteMethod.HEAD
import org.panda_lang.reposilite.web.api.RouteMethod.POST
import org.panda_lang.reposilite.web.api.RouteMethod.PUT
import org.panda_lang.reposilite.web.api.Routes
import org.panda_lang.reposilite.web.resultAttachment
import panda.std.Result

internal class MavenFileEndpoint(private val mavenFacade: MavenFacade) : Routes {

    @OpenApi(
        path = "/:repository-name/*",
        methods = [HttpMethod.GET],
        tags = ["Maven"],
        summary = "Browse the contents of repositories",
        description = "The route may return various responses to properly handle Maven specification and frontend application using the same path.",
        pathParams = [OpenApiParam(name = "*", description = "Artifact path qualifier", required = true, allowEmptyValue = true)],
        responses = [
            OpenApiResponse(status = "200", description = "Input stream of requested file", content = [OpenApiContent(type = MULTIPART_FORM_DATA)]),
            OpenApiResponse(status = "404", description = "Returns 404 (for Maven) with frontend (for user) as a response if requested resource is not located in the current repository")
        ]
    )
    val findFile = Route("/:repository-name/*", GET) {
        accessed {
            LookupRequest(parameter("repository-name"), wildcard(), this?.accessToken)
                .let { mavenFacade.findFile(it) }
                .peek {
                    when (it) {
                        is DocumentInfo -> ctx.resultAttachment(it)
                        is DirectoryInfo -> response = errorResponse(NO_CONTENT, "Requested file is a directory")
                    }
                }
                .onError { response = Result.error(it) }
        }
    }

    @OpenApi(
        path = "/:repository-name/*",
        methods = [HttpMethod.POST, HttpMethod.PUT],
        summary = "Deploy artifact to the repository",
        description = "Deploy supports both, POST and PUT, methods and allows to deploy artifact builds",
        tags = [ "Maven" ],
        pathParams = [ OpenApiParam(name = "*", description = "Artifact path qualifier", required = true) ],
        responses = [
            OpenApiResponse(status = "200", description = "Input stream of requested file", content = [OpenApiContent(type = MULTIPART_FORM_DATA)]),
            OpenApiResponse(status = "401", description = "Returns 401 for invalid credentials"),
            OpenApiResponse(status = "405", description = "Returns 405 if deployment is disabled in configuration"),
            OpenApiResponse(status = "507", description = "Returns 507 if Reposilite does not have enough disk space to store the uploaded file")
        ]
    )
    private val deployFile = Route("/:repository-name/*", POST, PUT) {
        authorized {
            response = DeployRequest(parameter("repository-name"), wildcard(), getSessionIdentifier(), context.input())
                .let { mavenFacade.deployFile(it) }
                .onError { context.logger.debug("Cannot deploy artifact due to: ${it.message}") }
        }
    }

    @OpenApi(
        path = "/:repository-name/*",
        summary = "Delete the given file from repository",
        methods = [HttpMethod.DELETE]
    )
    private val deleteFile = Route("/:repository-name/*", DELETE) {
        authorized {
            response = mavenFacade.deleteFile(parameter("repository-name"), wildcard())
        }
    }

    @OpenApi(
        path = "/api/maven/details/:repository-name/*",
        methods = [HttpMethod.HEAD, HttpMethod.GET],
        summary = "Browse the contents of repositories using API",
        description = "Get details about the requested file as JSON response",
        tags = ["Maven"],
        pathParams = [OpenApiParam(name = "*", description = "Artifact path qualifier", required = true, allowEmptyValue = true)],
        responses = [
            OpenApiResponse(
                status = "200",
                description = "Returns document (different for directory and file) that describes requested resource",
                content = [OpenApiContent(from = FileDetails::class)]
            ),
            OpenApiResponse(
                status = "401",
                description = "Returns 401 in case of unauthorized attempt of access to private repository",
                content = [OpenApiContent(from = ErrorResponse::class)]
            ),
            OpenApiResponse(
                status = "404",
                description = "Returns 404 (for Maven) and frontend (for user) as a response if requested artifact is not in the repository"
            )
        ]
    )
    val findFileDetails = Route("/api/maven/details/:repository-name/*", HEAD, GET) {
        accessed {
            response = LookupRequest(parameter("repository-name"), wildcard(), this?.accessToken)
                .let { mavenFacade.findFile(it) }
        }
    }

    override val routes = setOf(findFile, deployFile, deleteFile, findFileDetails)

}

/** Lookup Controller


OpenApi(
route = "xD"
summary = "Browse the contents of repositories",
description = "The route may return various responses to properly handle Maven specification and frontend application using the same path.",
tags = ["Repository"],
pathParams = [OpenApiParam(
name = "*",
description = "Artifact path qualifier",
required = true,
allowEmptyValue = true
), OpenApiParam(
name = "*\/latest",
description = "[Optional] Artifact path qualifier with /latest at the end returns latest version of artifact as text/plain"
)],
responses = [OpenApiResponse(
status = "200",
description = "Input stream of requested file",
content = [OpenApiContent(type = FORM_DATA_MULTIPART)]
), OpenApiResponse(
status = "404",
description = "Returns 404 (for Maven) with frontend (for user) as a response if requested resource is not located in the current repository"
)]
)
override fun handle(ctx: Context) = context(contextFactory, ctx) {
    context.getLogger().debug("LOOKUP " + context.uri().toString() + " from " + context.address())

    val response: Result<LookupResponse, ErrorResponse>
    if (lookupService.exists(context)) {
        response = try {
            lookupService.find(context)
        } catch (e: IOException) {
            e.printStackTrace()
            return
        }
    } else if (hasProxied) {
        response = proxyService.findProxied(context)
    } else {
        response = Result.error<LookupResponse, ErrorResponse>(ErrorResponse(HttpStatus.SC_NOT_FOUND, "File not found"))
    }
    handleResult(ctx, context, response)
}

private fun handleResult(ctx: Context, context: ReposiliteContext, result: Result<LookupResponse, ErrorResponse>) {
    result
        .peek(Consumer<LookupResponse> { response: LookupResponse? -> handleResult(ctx, context, response) })
        .onError(Consumer<ErrorResponse> { error: ErrorResponse -> handleError(ctx, error) })
}

private fun handleResult(ctx: Context, context: ReposiliteContext, response: LookupResponse) {
    response.getFileDetails().peek { details ->
        if (details.getContentLength() > 0) {
            ctx.res.setContentLengthLong(details.getContentLength())
        }
        if (response.isAttachment()) {
            ctx.res.setHeader("Content-Disposition", "attachment; filename=\"" + details.getName().toString() + "\"")
        }
    }
    response.getContentType().peek { type: String? -> ctx.res.contentType = type }
    response.getValue().peek(ctx::result)
    context.result().peek { result ->
        try {
            if (OutputUtils.isProbablyOpen(ctx.res.outputStream)) {
                result.accept(ctx.res.outputStream)
            }
        } catch (exception: IOException) {
            failureService.throwException(context.uri(), exception)
        }
    }
}

private fun handleError(ctx: Context, error: ErrorResponse) {
    ctx.result(frontend.forMessage(error.message))
        .status(error.status)
        .contentType("text/html").res.setCharacterEncoding("UTF-8")
}

**/