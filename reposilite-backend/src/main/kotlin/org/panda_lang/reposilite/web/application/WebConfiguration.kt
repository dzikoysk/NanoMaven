package org.panda_lang.reposilite.web.application

import org.panda_lang.reposilite.web.WebServer
import org.panda_lang.reposilite.web.infrastructure.JavalinWebServer

object WebConfiguration {

    fun createWebServer(): WebServer =
        JavalinWebServer()

}