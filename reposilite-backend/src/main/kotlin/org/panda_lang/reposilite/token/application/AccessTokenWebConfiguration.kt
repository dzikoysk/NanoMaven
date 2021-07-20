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

package org.panda_lang.reposilite.token.application

import net.dzikoysk.dynamiclogger.Journalist
import org.panda_lang.reposilite.console.ConsoleFacade
import org.panda_lang.reposilite.token.AccessTokenFacade
import org.panda_lang.reposilite.token.ChAliasCommand
import org.panda_lang.reposilite.token.ChModCommand
import org.panda_lang.reposilite.token.KeygenCommand
import org.panda_lang.reposilite.token.RevokeCommand
import org.panda_lang.reposilite.token.TokensCommand
import org.panda_lang.reposilite.token.infrastructure.SqlAccessTokenRepository

internal object AccessTokenWebConfiguration {

    fun createFacade(journalist: Journalist): AccessTokenFacade =
        AccessTokenFacade(journalist, SqlAccessTokenRepository())

    fun initialize(accessTokenFacade: AccessTokenFacade, consoleFacade: ConsoleFacade) {
        consoleFacade.registerCommand(TokensCommand(accessTokenFacade))
        consoleFacade.registerCommand(KeygenCommand(accessTokenFacade))
        consoleFacade.registerCommand(ChAliasCommand(accessTokenFacade))
        consoleFacade.registerCommand(ChModCommand(accessTokenFacade))
        consoleFacade.registerCommand(RevokeCommand(accessTokenFacade))
    }

}