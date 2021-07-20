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

package org.panda_lang.reposilite

import io.javalin.Javalin
import net.dzikoysk.dynamiclogger.Journalist
import org.panda_lang.reposilite.auth.application.AuthenticationWebConfiguration
import org.panda_lang.reposilite.config.Configuration
import org.panda_lang.reposilite.console.application.ConsoleWebConfiguration
import org.panda_lang.reposilite.failure.application.FailureWebConfiguration
import org.panda_lang.reposilite.frontend.application.FrontendWebConfiguration
import org.panda_lang.reposilite.maven.application.MavenWebConfiguration
import org.panda_lang.reposilite.statistics.application.StatisticsWebConfiguration
import org.panda_lang.reposilite.token.application.AccessTokenWebConfiguration
import org.panda_lang.reposilite.web.ReposiliteContextFactory
import org.panda_lang.reposilite.web.api.Routes
import org.panda_lang.reposilite.web.application.WebConfiguration
import java.nio.file.Path

object ReposiliteWebConfiguration {

    fun createReposilite(journalist: Journalist, configuration: Configuration, workingDirectory: Path, testEnv: Boolean): Reposilite {
        val logger = journalist.logger

        val webServer = WebConfiguration.createWebServer()
        val failureFacade = FailureWebConfiguration.createFacade(logger)
        val consoleFacade = ConsoleWebConfiguration.createFacade(logger, failureFacade)
        val mavenFacade = MavenWebConfiguration.createFacade(logger, failureFacade, configuration.repositories)
        val frontendFacade = FrontendWebConfiguration.createFacade(configuration)
        val statisticFacade = StatisticsWebConfiguration.createFacade(logger)
        val accessTokenFacade = AccessTokenWebConfiguration.createFacade(logger)
        val authenticationFacade = AuthenticationWebConfiguration.createFacade(logger, accessTokenFacade, mavenFacade)
        val contextFactory = ReposiliteContextFactory(logger, configuration.forwardedIp, authenticationFacade)

        return Reposilite(
            logger = logger,
            configuration = configuration,
            workingDirectory = workingDirectory,
            testEnv = testEnv,
            webServer = webServer,
            failureFacade = failureFacade,
            contextFactory = contextFactory,
            authenticationFacade = authenticationFacade,
            mavenFacade = mavenFacade,
            consoleFacade = consoleFacade,
            accessTokenFacade = accessTokenFacade,
            frontendFacade = frontendFacade,
            statisticsFacade = statisticFacade
        )
    }

    fun initialize(reposilite: Reposilite) {
        // AuthenticationWebConfiguration.initialize()
        FailureWebConfiguration.initialize(reposilite.consoleFacade, reposilite.failureFacade)
        ConsoleWebConfiguration.initialize(reposilite.consoleFacade, reposilite)
        // MavenWebConfiguration.initialize()
        // FrontendWebConfiguration.initialize()
        // MavenWebConfiguration.initialize()
        StatisticsWebConfiguration.initialize(reposilite.statisticsFacade, reposilite.consoleFacade)
        AccessTokenWebConfiguration.initialize(reposilite.accessTokenFacade, reposilite.consoleFacade)
    }

    fun routing(reposilite: Reposilite): Collection<Routes> =
        setOf(
            AuthenticationWebConfiguration.routing(reposilite.authenticationFacade),
            ConsoleWebConfiguration.routing(reposilite),
            FailureWebConfiguration.routing(),
            FrontendWebConfiguration.routing(reposilite.frontendFacade),
            MavenWebConfiguration.routing(reposilite.mavenFacade),
            StatisticsWebConfiguration.routing(reposilite.statisticsFacade),
            // AccessTokenWebConfiguration.routing(),
        )
        .flatten()

    // TOFIX: Remove dependency on infrastructure details
    fun javalin(reposilite: Reposilite, javalin: Javalin) {
        ConsoleWebConfiguration.javalin(javalin, reposilite)
        FailureWebConfiguration.javalin(javalin, reposilite.failureFacade)
    }

    fun dispose(reposilite: Reposilite) {
        ConsoleWebConfiguration.dispose(reposilite.consoleFacade)
    }

}