package com.redxai.data.repository

import com.redxai.data.local.dao.BuildDao
import com.redxai.data.local.entities.BuildEntity
import com.redxai.data.local.entities.BuildStatus
import com.redxai.data.preferences.AppPreferences
import com.redxai.data.remote.github.AndroidProjectGenerator
import com.redxai.data.remote.github.GitHubService
import com.redxai.util.WakeLockManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BuildRepository @Inject constructor(
    private val buildDao: BuildDao,
    private val gitHub: GitHubService,
    private val generator: AndroidProjectGenerator,
    private val prefs: AppPreferences,
    private val wakeLock: WakeLockManager
) {
    fun observeBuilds(): Flow<List<BuildEntity>> = buildDao.observeBuilds()
    fun observeBuild(id: Long): Flow<BuildEntity?> = buildDao.observeBuild(id)

    suspend fun getGithubUsername(): String = prefs.githubUsername.first()
    suspend fun getGithubRepo(): String = prefs.githubRepo.first()

    private suspend fun getBestApiKey(): String {
        val openrouterKey = prefs.openrouterKey.first()
        val veniceKey     = prefs.veniceKey.first()
        return when {
            openrouterKey.isNotBlank() -> openrouterKey
            veniceKey.isNotBlank()     -> veniceKey
            else -> ""
        }
    }

    private suspend fun getModel(): String = prefs.defaultModel.first()

    suspend fun startBuild(appName: String, description: String, model: String?): Long {
        val token    = prefs.githubToken.first()
        val repo     = prefs.githubRepo.first()

        if (token.isBlank()) throw IllegalStateException(
            "GitHub token not set.\nGo to Settings → GitHub Integration → Quick Connect."
        )
        if (repo.isBlank()) throw IllegalStateException(
            "GitHub repo name not set.\nGo to Settings → GitHub Integration."
        )

        val packageName = "com.redxai.${appName.lowercase().replace(Regex("[^a-z0-9]"), "")}"

        return buildDao.insertBuild(
            BuildEntity(
                appName     = appName,
                description = description,
                packageName = packageName,
                status      = BuildStatus.PUSHING
            )
        )
    }

    suspend fun pushAndBuild(buildId: Long, model: String?) {
        val apiKey   = getBestApiKey()
        val token    = prefs.githubToken.first()
        val username = prefs.githubUsername.first()
        val repo     = prefs.githubRepo.first()
        val chosenModel = model ?: getModel()
        val build    = buildDao.getBuildById(buildId) ?: return

        // Keep CPU awake for the full push + build cycle
        wakeLock.acquire("redxai:push:${build.appName}")
        try {
            buildDao.updateStatus(buildId, BuildStatus.PUSHING)

            // Ensure repo exists (create if not)
            gitHub.ensureRepoExists(token, username, repo).getOrThrow()

            // Generate Kotlin source files with AI
            val files = generator.generateProject(apiKey, chosenModel, build.appName, build.packageName, build.description)

            // Push all files to GitHub
            for (file in files) {
                gitHub.pushFile(
                    token, username, repo, file.path, file.content,
                    "Redx AI: add ${file.path}"
                ).getOrThrow()
                delay(300)   // gentle rate-limit protection
            }

            buildDao.updateStatus(buildId, BuildStatus.RUNNING)

            // Wait for GitHub Actions to start the workflow
            delay(6000)
            val runId = gitHub.getLatestRunId(token, username, repo)
            if (runId != null) buildDao.updateRunId(buildId, runId, BuildStatus.RUNNING)

        } catch (e: Exception) {
            buildDao.updateStatus(buildId, BuildStatus.FAILED)
            buildDao.updateLogs(buildId, "Push/generate failed: ${e.message}")
        } finally {
            wakeLock.release()
        }
    }

    suspend fun pollAndFix(buildId: Long): BuildEntity? {
        val apiKey   = getBestApiKey()
        val token    = prefs.githubToken.first()
        val username = prefs.githubUsername.first()
        val repo     = prefs.githubRepo.first()
        val chosenModel = getModel()
        val build    = buildDao.getBuildById(buildId) ?: return null

        val runId = build.runId ?: run {
            val id = gitHub.getLatestRunId(token, username, repo)
            if (id != null) buildDao.updateRunId(buildId, id, BuildStatus.RUNNING)
            id
        } ?: return buildDao.getBuildById(buildId)

        val run = gitHub.pollWorkflowRun(token, username, repo, runId)
            ?: return buildDao.getBuildById(buildId)

        when {
            run.status == "completed" && run.conclusion == "success" -> {
                val apkUrl = gitHub.getLatestApkUrl(token, username, repo)
                    ?: "https://github.com/$username/$repo/actions"
                buildDao.setApkUrl(buildId, apkUrl)
            }
            run.status == "completed" && run.conclusion != "success" -> {
                val logs = gitHub.getRunLogs(token, username, repo, runId)
                buildDao.updateLogs(buildId, logs)

                if (build.attempt < 5) {
                    // Wake lock during fix attempts too
                    wakeLock.acquire("redxai:fix:${build.appName}:${build.attempt + 1}")
                    try {
                        buildDao.updateStatus(buildId, BuildStatus.FIXING)
                        val files = generator.generateProject(apiKey, chosenModel, build.appName, build.packageName, build.description)
                        val (fixSummary, fixedFiles) = generator.fixBuildError(apiKey, chosenModel, logs, files)

                        for (file in fixedFiles) {
                            runCatching {
                                gitHub.pushFile(token, username, repo, file.path, file.content,
                                    "Redx AI fix #${build.attempt + 1}: ${file.path}")
                            }
                            delay(300)
                        }
                        buildDao.recordFix(buildId, fixSummary)

                        delay(8000)
                        val newRunId = gitHub.getLatestRunId(token, username, repo)
                        if (newRunId != null && newRunId != runId) {
                            buildDao.updateRunId(buildId, newRunId, BuildStatus.RUNNING)
                        }
                    } finally {
                        wakeLock.release()
                    }
                } else {
                    buildDao.updateStatus(buildId, BuildStatus.FAILED)
                }
            }
            run.status == "in_progress" || run.status == "queued" -> {
                buildDao.updateStatus(buildId, BuildStatus.RUNNING)
            }
        }

        return buildDao.getBuildById(buildId)
    }
}
