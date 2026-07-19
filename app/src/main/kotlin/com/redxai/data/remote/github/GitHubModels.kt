package com.redxai.data.remote.github

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GitHubRepo(
    @Json(name = "id") val id: Long,
    @Json(name = "name") val name: String,
    @Json(name = "full_name") val fullName: String,
    @Json(name = "html_url") val htmlUrl: String,
    @Json(name = "private") val isPrivate: Boolean = false
)

@JsonClass(generateAdapter = true)
data class CreateRepoRequest(
    @Json(name = "name") val name: String,
    @Json(name = "description") val description: String = "",
    @Json(name = "private") val private: Boolean = false,
    @Json(name = "auto_init") val autoInit: Boolean = false
)

@JsonClass(generateAdapter = true)
data class FileContent(
    @Json(name = "sha") val sha: String? = null,
    @Json(name = "content") val content: String? = null,
    @Json(name = "encoding") val encoding: String? = null
)

@JsonClass(generateAdapter = true)
data class PutFileRequest(
    @Json(name = "message") val message: String,
    @Json(name = "content") val content: String, // base64
    @Json(name = "sha") val sha: String? = null,
    @Json(name = "branch") val branch: String = "main"
)

@JsonClass(generateAdapter = true)
data class WorkflowRun(
    @Json(name = "id") val id: Long,
    @Json(name = "status") val status: String, // queued|in_progress|completed
    @Json(name = "conclusion") val conclusion: String?, // success|failure|cancelled|null
    @Json(name = "html_url") val htmlUrl: String,
    @Json(name = "run_number") val runNumber: Int
)

@JsonClass(generateAdapter = true)
data class WorkflowRunsResponse(
    @Json(name = "workflow_runs") val workflowRuns: List<WorkflowRun>
)

@JsonClass(generateAdapter = true)
data class WorkflowJob(
    @Json(name = "id") val id: Long,
    @Json(name = "name") val name: String,
    @Json(name = "status") val status: String,
    @Json(name = "conclusion") val conclusion: String?
)

@JsonClass(generateAdapter = true)
data class WorkflowJobsResponse(
    @Json(name = "jobs") val jobs: List<WorkflowJob>
)

@JsonClass(generateAdapter = true)
data class GitHubArtifact(
    @Json(name = "id") val id: Long,
    @Json(name = "name") val name: String,
    @Json(name = "archive_download_url") val downloadUrl: String
)

@JsonClass(generateAdapter = true)
data class ArtifactsResponse(
    @Json(name = "artifacts") val artifacts: List<GitHubArtifact>
)

@JsonClass(generateAdapter = true)
data class GitHubRelease(
    @Json(name = "id") val id: Long,
    @Json(name = "tag_name") val tagName: String,
    @Json(name = "html_url") val htmlUrl: String,
    @Json(name = "assets") val assets: List<ReleaseAsset>
)

@JsonClass(generateAdapter = true)
data class ReleaseAsset(
    @Json(name = "id") val id: Long,
    @Json(name = "name") val name: String,
    @Json(name = "browser_download_url") val downloadUrl: String
)
