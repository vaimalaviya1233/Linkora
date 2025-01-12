package com.sakethh.linkora.data.remote.repository

import com.sakethh.linkora.common.utils.wrappedResultFlow
import com.sakethh.linkora.domain.Result
import com.sakethh.linkora.domain.dto.github.GitHubReleaseDTOItem
import com.sakethh.linkora.domain.repository.remote.GitHubReleasesRepo
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.flow.Flow

class GitHubReleasesRepoImpl(
    private val ktorClient: HttpClient
) : GitHubReleasesRepo {
    override suspend fun getLatestVersionData(): Flow<Result<GitHubReleaseDTOItem>> {
        return wrappedResultFlow {
            ktorClient.get("https://api.github.com/repos/sakethpathike/Linkora/releases")
                .body<List<GitHubReleaseDTOItem>>().first().apply {
                    if (releaseName.startsWith("v").not()) {
                        releaseName = "v$releaseName"
                    }
                }
        }
    }
}