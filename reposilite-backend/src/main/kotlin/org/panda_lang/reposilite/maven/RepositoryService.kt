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
package org.panda_lang.reposilite.maven

import net.dzikoysk.dynamiclogger.Journalist
import net.dzikoysk.dynamiclogger.Logger
import org.panda_lang.reposilite.config.Configuration.RepositoryConfiguration
import org.panda_lang.reposilite.maven.api.Repository

internal class RepositoryService(
    private val journalist: Journalist,
    private val repositories: Map<String, Repository>,
) : Journalist {

    fun getRepository(name: String): Repository? =
        repositories[name]

    fun getRepositories(): Collection<Repository> =
        repositories.values

    override fun getLogger(): Logger =
        journalist.logger

}

internal object RepositoryServiceFactory {

    fun createRepositoryService(journalist: Journalist, repositoriesConfigurations: Map<String, RepositoryConfiguration>): RepositoryService {
        val repositories: MutableMap<String, Repository> = LinkedHashMap(repositoriesConfigurations.size)

        for ((repositoryName, repositoryConfiguration) in repositoriesConfigurations) {
            repositories[repositoryName] = RepositoryFactory.createRepository(journalist, repositoryName, repositoryConfiguration)
        }

        return RepositoryService(journalist, repositories)
    }

}