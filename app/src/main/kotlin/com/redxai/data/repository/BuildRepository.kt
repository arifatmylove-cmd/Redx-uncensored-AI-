package com.redxai.data.repository

import com.redxai.data.local.dao.BuildDao
import com.redxai.data.local.entities.BuildEntity
import com.redxai.data.local.entities.BuildStatus
import com.redxai.data.preferences.AppPreferences
import com.redxai.data.remote.github.AndroidProjectGenerator
import com.redxai.data.remote.github.GeneratedFile
import com.redxai.data.remote.github.GitHubService
import com.redxai.data.remote.openrouter.OpenRouterService
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
    private val prefs: AppPreferences
) {
    fun observeBuilds(): Flow<List<BuildEntity>> = buildDao.observeBuilds()
    fun observeBuild(id: Long): Flow<BuildEntity?> = buildDao.observeBuild(id)

    suspend fun startBuild(appName: String, description: String, model: String?): Long {
        val apiKey = prefs.openrouterKey.first()
        val token = prefs.githubToken.first()
        val username = prefs.githubUsername.first()
        val repo = prefs.githubRepo.first()
        val chosenModel = model ?: prefs.defaultModel.first()

        if (apiKey.isBlank()) throw IllegalStateException("OpenRouter API key not set. Go to Settings.")
        if (token.isBlank()) throw IllegalStateException("GitHub token not set. Go to Settings.")

        val packageName = "com.redxai.${appName.lowercase().replace(Regex("[^a-z0-9]"), "")}"

        val buildId = buildDao.insertBuild(
            BuildEntity(appName = appName, description = description, packageName = packageName, status = BuildStatus.PUSHING)
        )

        // Run the full build pipeline in background
        return buildId
    }

    suspend fun pushAndBuild(buildId: Long, model: String?) {
        val apiKey = prefs.openrouterKey.first()
        val token = prefs.githubToken.first()
        val username = prefs.githubUsername.first()
        val repo = prefs.githubRepo.first()
        val chosenModel = model ?: prefs.defaultModel.first()
        val build = buildDao.getBuildById(buildId) ?: return

        try {
            buildDao.updateStatus(buildId, BuildStatus.PUSHING)

            // Ensure repo exists
            gitHub.ensureRepoExists(token, username, repo).getOrThrow()

            // Generate files with AI
            val files = generator.generateProject(apiKey, chosenModel, build.appName, build.packageName, build.description)

            // Push all files to GitHub
            for (file in files) {
                gitHub.pushFile(token, username, repo, file.path, file.content, "Redx AI: add ${file.path}").getOrThrow()
                delay(300) // Rate limit protection
            }

            buildDao.updateStatus(buildId, BuildStatus.RUNNING)

            // Wait for workflow run to appear
            delay(5000)
            val runId = gitHub.getLatestRunId(token, username, repo)
            if (runId != null) {
                buildDao.updateRunId(buildId, runId, BuildStatus.RUNNING)
            }

        } catch (e: Exception) {
            buildDao.updateStatus(buildId, BuildStatus.FAILED)
            buildDao.updateLogs(buildId, "Push/generate failed: ${e.message}")
        }
    }

    suspend fun pollAndFix(buildId: Long): BuildEntity? {
        val apiKey = prefs.openrouterKey.first()
        val token = prefs.githubToken.first()
        val username = prefs.githubUsername.first()
        val repo = prefs.githubRepo.first()
        val chosenModel = prefs.defaultModel.first()
        val build = buildDao.getBuildById(buildId) ?: return null

        val runId = build.runId ?: run {
            // Try to find run ID
            val id = gitHub.getLatestRunId(token, username, repo)
            if (id != null) buildDao.updateRunId(buildId, id, BuildStatus.RUNNING)
            id
        } ?: return buildDao.getBuildById(buildId)

        val run = gitHub.pollWorkflowRun(token, username, repo, runId) ?: return buildDao.getBuildById(buildId)

        when {
            run.status == "completed" && run.conclusion == "success" -> {
                val apkUrl = gitHub.getLatestApkUrl(token, username, repo)
                    ?: "https://github.com/$username/$repo/releases/latest"
                buildDao.setApkUrl(buildId, apkUrl)
            }
            run.status == "completed" && run.conclusion != "success" -> {
                // Fetch error logs
                val logs = gitHub.getRunLogs(token, username, repo, runId)
                buildDao.updateLogs(buildId, logs)

                if (build.attempt < 5) {
                    buildDao.updateStatus(buildId, BuildStatus.FIXING)

                    // Get existing files to fix them
                    val files = generator.generateProject(apiKey, chosenModel, build.appName, build.packageName, build.description)
                    val (fixSummary, fixedFiles) = generator.fixBuildError(apiKey, chosenModel, logs, files)

                    // Push fixed files
                    for (file in fixedFiles) {
                        runCatching { gitHub.pushFile(token, username, repo, file.path, file.content, "Redx AI fix attempt ${build.attempt + 1}: ${file.path}") }
                        delay(300)
                    }

                    buildDao.recordFix(buildId, fixSummary)

                    // Wait for new run
                    delay(8000)
                    val newRunId = gitHub.getLatestRunId(token, username, repo)
                    if (newRunId != null && newRunId != runId) {
                        buildDao.updateRunId(buildId, newRunId, BuildStatus.RUNNING)
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
