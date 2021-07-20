package org.panda_lang.reposilite.web

import org.panda_lang.reposilite.Reposilite

interface WebServer {

    fun start(reposilite: Reposilite)

    fun stop()

    fun isAlive(): Boolean

}