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

package org.panda_lang.reposilite.auth.application

import net.dzikoysk.dynamiclogger.Journalist
import org.panda_lang.reposilite.auth.AuthenticationFacade
import org.panda_lang.reposilite.auth.Authenticator
import org.panda_lang.reposilite.auth.SessionService
import org.panda_lang.reposilite.auth.infrastructure.AuthenticationEndpoint
import org.panda_lang.reposilite.auth.infrastructure.PostAuthHandler
import org.panda_lang.reposilite.maven.MavenFacade
import org.panda_lang.reposilite.token.AccessTokenFacade
import org.panda_lang.reposilite.web.api.Routes

internal object AuthenticationWebConfiguration {

    fun createFacade(journalist: Journalist, accessTokenFacade: AccessTokenFacade, mavenFacade: MavenFacade): AuthenticationFacade {
        val authenticator = Authenticator(accessTokenFacade)
        val sessionService = SessionService(mavenFacade)

        return AuthenticationFacade(journalist, authenticator, sessionService)
    }

    fun routing(authenticationFacade: AuthenticationFacade): Set<Routes> =
        setOf(
            AuthenticationEndpoint(authenticationFacade),
            PostAuthHandler()
        )

}