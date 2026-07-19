package com.redxai.data.remote.github

import android.util.Base64
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.delay
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

interface GitHubApi {
    @GET("user")
    suspend fun getUser(): Response<Map<String, Any>>

    @GET("repos/{owner}/{repo}")
    suspend fun getRepo(@Path("owner") owner: String, @Path("repo") repo: String): Response<GitHubRepo>

    @POST("user/repos")
    suspend fun createRepo(@Body request: CreateRepoRequest): Response<GitHubRepo>

    @GET("repos/{owner}/{repo}/contents/{path}")
    suspend fun getFile(@Path("owner") owner: String, @Path("repo") repo: String, @Path("path") path: String): Response<FileContent>

    @PUT("repos/{owner}/{repo}/contents/{path}")
    suspend fun putFile(@Path("owner") owner: String, @Path("repo") repo: String, @Path("path") path: String, @Body request: PutFileRequest): Response<Any>

    @GET("repos/{owner}/{repo}/actions/runs")
    suspend fun getWorkflowRuns(@Path("owner") owner: String, @Path("repo") repo: String, @Query("per_page") perPage: Int = 5): Response<WorkflowRunsResponse>

    @GET("repos/{owner}/{repo}/actions/runs/{runId}")
    suspend fun getWorkflowRun(@Path("owner") owner: String, @Path("repo") repo: String, @Path("runId") runId: Long): Response<WorkflowRun>

    @GET("repos/{owner}/{repo}/actions/runs/{runId}/jobs")
    suspend fun getRunJobs(@Path("owner") owner: String, @Path("repo") repo: String, @Path("runId") runId: Long): Response<WorkflowJobsResponse>

    @GET("repos/{owner}/{repo}/actions/runs/{runId}/logs")
    suspend fun getRunLogs(@Path("owner") owner: String, @Path("repo") repo: String, @Path("runId") runId: Long): Response<okhttp3.ResponseBody>

    @GET("repos/{owner}/{repo}/actions/runs/{runId}/artifacts")
    suspend fun getRunArtifacts(@Path("owner") owner: String, @Path("repo") repo: String, @Path("runId") runId: Long): Response<ArtifactsResponse>

    @GET("repos/{owner}/{repo}/releases/latest")
    suspend fun getLatestRelease(@Path("owner") owner: String, @Path("repo") repo: String): Response<GitHubRelease>
}

@Singleton
class GitHubService @Inject constructor() {

    private fun buildApi(token: String): GitHubApi {
        val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val req = chain.request().newBuilder()
                    .addHeader("Authorization", "token $token")
                    .addHeader("Accept", "application/vnd.github.v3+json")
                    .addHeader("User-Agent", "RedxAI/1.0")
                    .build()
                chain.proceed(req)
            }
            .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GitHubApi::class.java)
    }

    suspend fun verifyToken(token: String, username: String, repo: String): Result<String> = runCatching {
        val api = buildApi(token)
        val userResp = api.getUser()
        if (!userResp.isSuccessful) throw Exception("Invalid token: ${userResp.code()}")
        "Connected as $username"
    }

    suspend fun ensureRepoExists(token: String, username: String, repo: String): Result<GitHubRepo> = runCatching {
        val api = buildApi(token)
        val existing = api.getRepo(username, repo)
        if (existing.isSuccessful) return@runCatching existing.body()!!

        val created = api.createRepo(CreateRepoRequest(name = repo, description = "Built with Redx AI"))
        if (!created.isSuccessful) throw Exception("Failed to create repo: ${created.code()} ${created.errorBody()?.string()}")
        created.body()!!
    }

    suspend fun pushFile(token: String, username: String, repo: String, path: String, content: String, message: String = "Redx AI: update $path"): Result<Unit> = runCatching {
        val api = buildApi(token)
        val encoded = Base64.encodeToString(content.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)

        // Get existing SHA if file exists
        val existing = api.getFile(username, repo, path)
        val sha = if (existing.isSuccessful) existing.body()?.sha else null

        val resp = api.putFile(username, repo, path, PutFileRequest(message = message, content = encoded, sha = sha))
        if (!resp.isSuccessful) throw Exception("Push failed for $path: ${resp.code()} ${resp.errorBody()?.string()}")
    }

    suspend fun getLatestRunId(token: String, username: String, repo: String): Long? {
        return runCatching {
            val api = buildApi(token)
            val resp = api.getWorkflowRuns(username, repo, perPage = 1)
            resp.body()?.workflowRuns?.firstOrNull()?.id
        }.getOrNull()
    }

    suspend fun pollWorkflowRun(token: String, username: String, repo: String, runId: Long): WorkflowRun? {
        return runCatching {
            val api = buildApi(token)
            api.getWorkflowRun(username, repo, runId).body()
        }.getOrNull()
    }

    suspend fun getRunLogs(token: String, username: String, repo: String, runId: Long): String {
        return runCatching {
            val api = buildApi(token)
            val resp = api.getRunLogs(username, repo, runId)
            if (resp.isSuccessful) resp.body()?.string() ?: "No logs available"
            else "Could not fetch logs: ${resp.code()}"
        }.getOrElse { "Error fetching logs: ${it.message}" }
    }

    suspend fun getLatestApkUrl(token: String, username: String, repo: String): String? {
        return runCatching {
            val api = buildApi(token)
            val release = api.getLatestRelease(username, repo)
            release.body()?.assets?.find { it.name.endsWith(".apk") }?.downloadUrl
        }.getOrNull()
    }
}
