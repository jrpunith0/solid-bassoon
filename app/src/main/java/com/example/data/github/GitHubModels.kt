package com.example.data.github

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GitHubUser(
    val login: String = "",
    val id: Long = 0,
    @Json(name = "avatar_url") val avatarUrl: String = "",
    @Json(name = "html_url") val htmlUrl: String = "",
    val name: String? = null,
    val bio: String? = null,
    @Json(name = "public_repos") val publicRepos: Int = 0,
    val followers: Int = 0,
    val following: Int = 0,
    val email: String? = null,
    @Json(name = "created_at") val createdAt: String? = null
)

@JsonClass(generateAdapter = true)
data class GitHubLicense(
    val key: String? = null,
    val name: String? = null,
    @Json(name = "spdx_id") val spdxId: String? = null,
    val url: String? = null
)

@JsonClass(generateAdapter = true)
data class GitHubRepo(
    val id: Long = 0,
    val name: String = "",
    @Json(name = "full_name") val fullName: String = "",
    val description: String? = null,
    @Json(name = "html_url") val htmlUrl: String = "",
    val language: String? = "Kotlin",
    @Json(name = "stargazers_count") val stargazersCount: Int = 0,
    @Json(name = "forks_count") val forksCount: Int = 0,
    @Json(name = "open_issues_count") val openIssuesCount: Int = 0,
    @Json(name = "default_branch") val defaultBranch: String = "main",
    @Json(name = "updated_at") val updatedAt: String? = null,
    val private: Boolean = false,
    val license: GitHubLicense? = null,
    val size: Int = 0
)

enum class TokenWarningType {
    NONE,               // Token is valid and sufficient
    NEARING_EXPIRATION, // Token expiration in <= 14 days
    EXPIRED,            // Expiration timestamp is in the past
    LACKS_PERMISSIONS,  // Missing required repo or public_repo permissions
    INVALID_FORMAT,     // Token format is invalid or malformed
    UNAUTHORIZED        // 401 Unauthorized / Bad credentials
}

data class GitHubTokenValidationResult(
    val isValid: Boolean = true,
    val scopes: List<String> = emptyList(),
    val rawScopesHeader: String = "",
    val expirationDate: String? = null,
    val daysUntilExpiration: Int? = null,
    val warningType: TokenWarningType = TokenWarningType.NONE,
    val warningMessage: String = "",
    val hasRepoScope: Boolean = true,
    val hasPublicRepoScope: Boolean = true,
    val checkedAt: Long = System.currentTimeMillis()
)

@JsonClass(generateAdapter = true)
data class GitHubFileCommitRequest(
    val message: String,
    val content: String, // Base64 encoded content
    val branch: String = "main",
    val sha: String? = null // required if updating existing file
)

@JsonClass(generateAdapter = true)
data class GitHubCommitResponse(
    val content: GitHubCommitContent? = null,
    val commit: GitHubCommitInfo? = null
)

@JsonClass(generateAdapter = true)
data class GitHubCommitContent(
    val name: String = "",
    val path: String = "",
    val sha: String = "",
    @Json(name = "html_url") val htmlUrl: String = ""
)

@JsonClass(generateAdapter = true)
data class GitHubCommitInfo(
    val sha: String = "",
    val message: String = "",
    @Json(name = "html_url") val htmlUrl: String = ""
)

data class GitHubSyncResult(
    val success: Boolean,
    val message: String,
    val fileUrl: String? = null,
    val sha: String? = null,
    val timestamp: String = ""
)

@JsonClass(generateAdapter = true)
data class GitHubParticipationStats(
    val all: List<Int> = emptyList(),
    val owner: List<Int> = emptyList()
)

data class GitHubContributionDay(
    val dayOfWeek: Int, // 0 = Sunday .. 6 = Saturday
    val dateString: String,
    val count: Int,
    val level: Int // 0 (none), 1 (1-2), 2 (3-5), 3 (6-9), 4 (10+)
)

data class GitHubActivityWeek(
    val weekNumber: Int,
    val weekLabel: String,
    val totalCommits: Int,
    val days: List<GitHubContributionDay> = emptyList()
)

data class GitHubActivityHistory(
    val repoName: String,
    val totalYearCommits: Int,
    val activeStreakDays: Int,
    val maxDayCommits: Int,
    val weeklyFrequency: List<Int>, // weekly commit counts
    val weeks: List<GitHubActivityWeek> = emptyList()
)

@JsonClass(generateAdapter = true)
data class GitHubContentItem(
    val name: String = "",
    val path: String = "",
    val sha: String = "",
    val size: Long = 0L,
    val url: String = "",
    @Json(name = "html_url") val htmlUrl: String? = null,
    @Json(name = "download_url") val downloadUrl: String? = null,
    val type: String = "file" // "file", "dir", "symlink", "submodule"
) {
    val isDirectory: Boolean get() = type == "dir"
    val fileExtension: String get() = if (isDirectory) "" else name.substringAfterLast('.', "").lowercase()
}


