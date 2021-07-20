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
package org.panda_lang.reposilite.web.infrastructure

import org.panda_lang.reposilite.ReposiliteLauncher
import javax.servlet.annotation.MultipartConfig
import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@MultipartConfig
@WebServlet(urlPatterns = ["/*"], name = "ReposiliteServlet", asyncSupported = true)
internal class JavalinServlet : HttpServlet() {

    private val reposilite = ReposiliteLauncher.create("", "", /* servlet = true, */ false)

    override fun init() {
        reposilite.logger.info("Starting Reposilite servlet...")

        try {
            reposilite.load()
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }

    override fun service(req: HttpServletRequest, res: HttpServletResponse) {
        // reposilite.httpServer.javalin?.servlet()?.service(req, res)
    }

    override fun destroy() {
        try {
            reposilite.shutdown()
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }

}