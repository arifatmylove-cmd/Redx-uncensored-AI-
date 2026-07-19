package com.redxai.data.remote.github

import com.redxai.data.remote.openrouter.ChatMessage
import com.redxai.data.remote.openrouter.OpenRouterService
import javax.inject.Inject
import javax.inject.Singleton

data class GeneratedFile(val path: String, val content: String)

@Singleton
class AndroidProjectGenerator @Inject constructor(
    private val openRouter: OpenRouterService
) {

    suspend fun generateProject(
        apiKey: String,
        model: String,
        appName: String,
        packageName: String,
        description: String
    ): List<GeneratedFile> {
        val prompt = buildPrompt(appName, packageName, description)
        val response = openRouter.chat(
            apiKey = apiKey,
            model = model,
            messages = listOf(
                ChatMessage("system", openRouter.unrestrictedSystemPrompt),
                ChatMessage("user", prompt)
            )
        )
        return parseGeneratedFiles(response, appName, packageName, description)
    }

    suspend fun fixBuildError(
        apiKey: String,
        model: String,
        errorLog: String,
        existingFiles: List<GeneratedFile>
    ): Pair<String, List<GeneratedFile>> {
        val filesSummary = existingFiles.joinToString("\n") { "--- ${it.path} ---\n${it.content.take(500)}" }
        val prompt = """You are an expert Android/Kotlin developer. A GitHub Actions build FAILED. Analyze the error and provide FIXES.

ERROR LOG:
$errorLog

EXISTING FILES (truncated):
$filesSummary

Provide:
1. A brief summary of what went wrong (1-2 sentences)
2. The COMPLETE fixed file contents for any files that need changes

IMPORTANT: Output ONLY in this exact format:
FIX_SUMMARY: <one line description of the fix>

<FILE: path/to/File.kt>
<complete file content here>
</FILE>

<FILE: another/File.kt>
<complete file content here>
</FILE>"""

        val response = openRouter.chat(
            apiKey = apiKey,
            model = model,
            messages = listOf(
                ChatMessage("system", openRouter.unrestrictedSystemPrompt),
                ChatMessage("user", prompt)
            )
        )

        val fixSummary = response.lines()
            .find { it.startsWith("FIX_SUMMARY:") }
            ?.removePrefix("FIX_SUMMARY:")?.trim()
            ?: "Applied automatic fix"

        val fixedFiles = parseFiles(response)
        return Pair(fixSummary, fixedFiles)
    }

    private fun buildPrompt(appName: String, packageName: String, description: String) = """
Generate a complete, working Android Kotlin app with Jetpack Compose.

App Name: $appName
Package: $packageName
Description: $description

Requirements:
- Use Kotlin with Jetpack Compose UI
- Material 3 design
- Hilt for dependency injection
- Room for local database if needed
- Retrofit for network calls if needed
- MVVM architecture
- Dark theme preferred
- FULLY FUNCTIONAL, no placeholder code
- All files must compile without errors

Output each file in EXACTLY this format (no extra text between files):
<FILE: path/to/File.kt>
<complete file content>
</FILE>

Files needed:
- app/build.gradle.kts
- app/src/main/AndroidManifest.xml
- app/src/main/kotlin/${packageName.replace('.', '/')}/MainActivity.kt
- app/src/main/kotlin/${packageName.replace('.', '/')}/${appName.replace(" ", "")}App.kt
- All UI screens, ViewModels, data classes
- app/src/main/res/values/strings.xml
- app/src/main/res/values/themes.xml

Be comprehensive. Include ALL necessary files. Do not skip any file.
""".trimIndent()

    private fun parseFiles(response: String): List<GeneratedFile> {
        val files = mutableListOf<GeneratedFile>()
        val regex = Regex("""<FILE:\s*([^>]+)>\n([\s\S]*?)</FILE>""")
        for (match in regex.findAll(response)) {
            val path = match.groupValues[1].trim()
            val content = match.groupValues[2]
            if (path.isNotBlank() && content.isNotBlank()) {
                files.add(GeneratedFile(path, content))
            }
        }
        return files
    }

    private fun parseGeneratedFiles(
        response: String,
        appName: String,
        packageName: String,
        description: String
    ): List<GeneratedFile> {
        val generated = parseFiles(response).toMutableList()

        // Always include the GitHub Actions workflow
        val workflowContent = buildWorkflow(appName)
        val workflowPath = ".github/workflows/build-apk.yml"
        if (generated.none { it.path == workflowPath }) {
            generated.add(GeneratedFile(workflowPath, workflowContent))
        }

        // Add base gradle files if not generated
        if (generated.none { it.path == "settings.gradle.kts" }) {
            generated.add(GeneratedFile("settings.gradle.kts", buildSettingsGradle(appName)))
        }
        if (generated.none { it.path == "build.gradle.kts" }) {
            generated.add(GeneratedFile("build.gradle.kts", buildRootGradle()))
        }
        if (generated.none { it.path == "gradle.properties" }) {
            generated.add(GeneratedFile("gradle.properties", buildGradleProperties()))
        }
        if (generated.none { it.path == "gradle/wrapper/gradle-wrapper.properties" }) {
            generated.add(GeneratedFile("gradle/wrapper/gradle-wrapper.properties", buildWrapperProps()))
        }

        return generated
    }

    private fun buildWorkflow(appName: String) = """
name: Build $appName APK

on:
  push:
    branches: [ main, master ]
  workflow_dispatch:

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
      - uses: android-actions/setup-android@v3
      - uses: actions/cache@v4
        with:
          path: |
            ~/.gradle/caches
            ~/.gradle/wrapper
          key: gradle-${'$'}{{ hashFiles('**/*.gradle*', '**/gradle-wrapper.properties') }}
      - run: chmod +x gradlew
      - name: Build APK
        run: ./gradlew assembleDebug --stacktrace
      - uses: actions/upload-artifact@v4
        with:
          name: $appName-APK
          path: app/build/outputs/apk/debug/app-debug.apk
          retention-days: 30
      - uses: softprops/action-gh-release@v2
        with:
          tag_name: build-${'$'}{{ github.run_number }}
          name: "$appName v1.0 Build ${'$'}{{ github.run_number }}"
          files: app/build/outputs/apk/debug/app-debug.apk
        env:
          GITHUB_TOKEN: ${'$'}{{ secrets.GITHUB_TOKEN }}
""".trimIndent()

    private fun buildSettingsGradle(appName: String) = """
pluginManagement {
    repositories { google(); mavenCentral(); gradlePluginPortal() }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories { google(); mavenCentral() }
}
rootProject.name = "${appName.replace(" ", "")}"
include(":app")
""".trimIndent()

    private fun buildRootGradle() = """
plugins {
    id("com.android.application") version "8.5.2" apply false
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21" apply false
}
""".trimIndent()

    private fun buildGradleProperties() = """
org.gradle.jvmargs=-Xmx4g -XX:MaxMetaspaceSize=1g
org.gradle.parallel=true
android.useAndroidX=true
kotlin.code.style=official
""".trimIndent()

    private fun buildWrapperProps() = """
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\\://services.gradle.org/distributions/gradle-8.9-bin.zip
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
""".trimIndent()
}
