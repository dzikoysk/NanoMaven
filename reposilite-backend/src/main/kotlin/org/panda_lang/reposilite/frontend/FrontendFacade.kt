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
package org.panda_lang.reposilite.frontend

import org.panda_lang.reposilite.config.Configuration
import org.panda_lang.reposilite.shared.FilesUtils.getResource
import panda.std.Lazy
import panda.utilities.StringUtils
import panda.utilities.text.Formatter
import java.util.function.Supplier

class FrontendFacade internal constructor(index: Supplier<String>, app: Supplier<String>) {

    private val index: Lazy<String> = Lazy(index)
    private val app: Lazy<String> = Lazy(app)

    companion object {

        fun load(configuration: Configuration): FrontendFacade {
            val formatter = Formatter()
                .register("{{REPOSILITE.BASE_PATH}}", configuration.basePath)
                .register("{{REPOSILITE.VUE_BASE_PATH}}", configuration.basePath.substring(0, configuration.basePath.length - 1))
                .register("{{REPOSILITE.TITLE}}", configuration.title.replace("'", "\\'"))
                .register("{{REPOSILITE.DESCRIPTION}}", configuration.description.replace("'", "\\'"))
                .register("{{REPOSILITE.ACCENT_COLOR}}", configuration.accentColor)

            return FrontendFacade(
                { formatter.format(getResource("/static/index.html")) },
                { formatter.format(getResource("/static/js/app.js")) }
            )
        }

    }

    fun forMessage(message: String): String {
        return StringUtils.replace(index.get(), "{{REPOSILITE.MESSAGE}}", message)
    }

}