package org.panda_lang.reposilite.maven

import org.panda_lang.reposilite.maven.api.Repository
import org.panda_lang.reposilite.maven.api.RepositoryVisibility.HIDDEN
import org.panda_lang.reposilite.maven.api.RepositoryVisibility.PRIVATE
import org.panda_lang.reposilite.maven.api.RepositoryVisibility.PUBLIC
import org.panda_lang.reposilite.token.api.AccessToken
import org.panda_lang.reposilite.token.api.Permission.READ
import java.nio.file.Path

internal class RepositorySecurityProvider {

    fun canAccessResource(accessToken: AccessToken?, repository: Repository, gav: Path): Boolean =
        when (repository.visibility) {
            PUBLIC -> true
            HIDDEN -> true
            PRIVATE -> hasPermissionTo(accessToken, gav)
        }

    fun canBrowseResource(accessToken: AccessToken?, repository: Repository, gav: Path): Boolean =
        when (repository.visibility) {
            PUBLIC -> true
            HIDDEN -> hasPermissionTo(accessToken, gav)
            PRIVATE -> hasPermissionTo(accessToken, gav)
        }

    private fun hasPermissionTo(accessToken: AccessToken?, gav: Path): Boolean =
        accessToken?.hasPermissionTo(gav.toString(), READ) ?: false

}