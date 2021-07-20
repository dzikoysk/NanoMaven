package org.panda_lang.reposilite.web.infrastructure

import com.dzikoysk.openapi.ktor.OpenApiConfiguration
import com.dzikoysk.openapi.ktor.OpenApiPlugin
import com.dzikoysk.openapi.ktor.swagger.SwaggerConfiguration
import com.dzikoysk.openapi.ktor.swagger.SwaggerPlugin
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.databind.ObjectMapper
import io.javalin.Javalin
import io.javalin.core.JavalinConfig
import io.javalin.plugin.json.JavalinJackson
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.ServerConnector
import org.eclipse.jetty.util.ssl.SslContextFactory
import org.panda_lang.reposilite.Reposilite
import org.panda_lang.reposilite.ReposiliteWebConfiguration
import org.panda_lang.reposilite.VERSION
import org.panda_lang.reposilite.config.Configuration
import org.panda_lang.reposilite.web.api.RouteMethod.AFTER
import org.panda_lang.reposilite.web.api.RouteMethod.BEFORE
import org.panda_lang.reposilite.web.api.RouteMethod.DELETE
import org.panda_lang.reposilite.web.api.RouteMethod.GET
import org.panda_lang.reposilite.web.api.RouteMethod.HEAD
import org.panda_lang.reposilite.web.api.RouteMethod.POST
import org.panda_lang.reposilite.web.api.RouteMethod.PUT

internal object JavalinWebServerConfiguration {

    internal fun configure(reposilite: Reposilite, configuration: Configuration, config: JavalinConfig) {
        val server = Server()

        configureJavalin(config)
        configureJsonSerialization(config)
        configureSSL(reposilite, configuration, config, server)
        configureCors(config)
        configureOpenApi(configuration, config)
        configureDebug(reposilite, configuration, config)

        config.server { server }
    }

    private fun configureJavalin(config: JavalinConfig) {
        config.showJavalinBanner = false
    }

    private fun configureJsonSerialization(config: JavalinConfig) {
        val objectMapper = ObjectMapper()
        objectMapper.setSerializationInclusion(Include.NON_NULL)
        JavalinJackson.configure(objectMapper)
    }

    private fun configureSSL(reposilite: Reposilite, configuration: Configuration, config: JavalinConfig, server: Server) {
        if (configuration.sslEnabled) {
            reposilite.logger.info("Enabling SSL connector at ::" + configuration.sslPort)

            val sslContextFactory: SslContextFactory = SslContextFactory.Server()
            sslContextFactory.keyStorePath = configuration.keyStorePath.replace("\${WORKING_DIRECTORY}", reposilite.workingDirectory.toAbsolutePath().toString())
            sslContextFactory.setKeyStorePassword(configuration.keyStorePassword)

            val sslConnector = ServerConnector(server, sslContextFactory)
            sslConnector.port = configuration.sslPort
            server.addConnector(sslConnector)

            if (!configuration.enforceSsl) {
                val standardConnector = ServerConnector(server)
                standardConnector.port = configuration.port
                server.addConnector(standardConnector)
            }
        }

        config.enforceSsl = configuration.enforceSsl
    }

    private fun configureCors(config: JavalinConfig) {
        config.enableCorsForAllOrigins()
    }

    private fun configureOpenApi(configuration: Configuration, config: JavalinConfig) {
        if (configuration.swagger) {
            val openApiConfiguration = OpenApiConfiguration()
            openApiConfiguration.title = configuration.title
            openApiConfiguration.description = configuration.description
            openApiConfiguration.version = VERSION
            config.registerPlugin(OpenApiPlugin(openApiConfiguration))

            val swaggerConfiguration = SwaggerConfiguration()
            swaggerConfiguration.title = openApiConfiguration.title
            config.registerPlugin(SwaggerPlugin(swaggerConfiguration))
        }
    }

    private fun configureDebug(reposilite: Reposilite, configuration: Configuration, config: JavalinConfig) {
        if (configuration.debugEnabled) {
            // config.requestCacheSize = FilesUtils.displaySizeToBytesCount(System.getProperty("reposilite.requestCacheSize", "8MB"));
            // Reposilite.getLogger().debug("requestCacheSize set to " + config.requestCacheSize + " bytes");
            reposilite.logger.info("Debug enabled")
            config.enableDevLogging()
        }
    }

    internal fun routing(reposilite: Reposilite, javalin: Javalin) {
        ReposiliteWebConfiguration.routing(reposilite)
            .flatMap { it.routes }
            .sorted()
            .map { Pair(it, it.createHandler(reposilite.contextFactory)) }
            .also { reposilite.logger.debug("--- Routes") }
            .forEach { (route, handler) ->
                route.methods.forEach { method ->
                    when (method) {
                        HEAD -> javalin.head(route.path, handler)
                        GET -> javalin.get(route.path, handler)
                        PUT -> javalin.put(route.path, handler)
                        POST -> javalin.post(route.path, handler)
                        DELETE -> javalin.delete(route.path, handler)
                        AFTER -> javalin.after(route.path, handler)
                        BEFORE -> javalin.before(route.path, handler)
                    }

                    reposilite.logger.debug("- $method ${route.path}")
                }
            }
    }

}