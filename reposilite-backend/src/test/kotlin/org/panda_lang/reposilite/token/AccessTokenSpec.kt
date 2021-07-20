package org.panda_lang.reposilite.token

import net.dzikoysk.dynamiclogger.backend.InMemoryLogger
import org.panda_lang.reposilite.token.infrastructure.InMemoryAccessTokenRepository

internal open class AccessTokenSpec {

    protected val logger = InMemoryLogger()
    protected val accessTokenFacade = AccessTokenFacade(logger, InMemoryAccessTokenRepository())

}