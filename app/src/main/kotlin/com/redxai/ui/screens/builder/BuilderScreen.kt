package com.redxai.ui.screens.builder

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.redxai.data.local.entities.BuildEntity
import com.redxai.data.local.entities.BuildStatus
import com.redxai.data.remote.openrouter.RedxModels
import com.redxai.ui.theme.*

@Composable
fun BuilderScreen(
    onBack: () -> Unit,
    viewModel: BuilderViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    var showModelPicker by remember { mutableStateOf(false) }

    // Auto-poll running builds
    LaunchedEffect(state.builds) {
        state.builds.filter { it.status == BuildStatus.RUNNING || it.status == BuildStatus.FIXING }
            .forEach { build -> viewModel.pollBuild(build.id) }
    }

    Column(modifier = Modifier.fillMaxSize().background(RedxBackground)) {

        // Top bar
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = RedxTextPrimary)
            }
            Text("APK Builder", color = RedxTextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.weight(1f))
        }
        HorizontalDivider(color = RedxBorder)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Build form
            item {
                BuildForm(
                    state = state,
                    onAppName = viewModel::setAppName,
                    onDescription = viewModel::setDescription,
                    onModelPicker = { showModelPicker = true },
                    onSubmit = viewModel::startBuild,
                    onClearError = viewModel::clearError
                )
            }

            // Builds list
            if (state.builds.isNotEmpty()) {
                item {
                    Text("Build History", color = RedxTextSecondary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
                items(state.builds, key = { it.id }) { build ->
                    BuildCard(build = build, onDownload = {
                        build.apkUrl?.let { url ->
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                        }
                    })
                }
            }
        }
    }

    if (showModelPicker) {
        BuildModelPickerSheet(
            models = RedxModels.models,
            current = state.selectedModel,
            onSelect = { viewModel.setModel(it); showModelPicker = false },
            onDismiss = { showModelPicker = false }
        )
    }
}

@Composable
private fun BuildForm(
    state: BuilderState,
    onAppName: (String) -> Unit,
    onDescription: (String) -> Unit,
    onModelPicker: () -> Unit,
    onSubmit: () -> Unit,
    onClearError: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(RedxSurface)
            .border(1.dp, RedxBorder, RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Build a New APK", color = RedxTextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text(
            "Describe the app you want. Redx AI generates the full Kotlin source, pushes to GitHub, monitors the build, and auto-fixes any errors.",
            color = RedxTextSecondary, fontSize = 12.sp, lineHeight = 17.sp
        )

        OutlinedTextField(
            value = state.appName,
            onValueChange = onAppName,
            label = { Text("App Name", color = RedxTextSecondary) },
            placeholder = { Text("e.g. Network Scanner, Port Checker...", color = RedxTextMuted, fontSize = 13.sp) },
            modifier = Modifier.fillMaxWidth(),
            colors = redxTextFieldColors(),
            singleLine = true,
            shape = RoundedCornerShape(8.dp)
        )

        OutlinedTextField(
            value = state.description,
            onValueChange = onDescription,
            label = { Text("App Description", color = RedxTextSecondary) },
            placeholder = { Text("Describe ALL features in detail. The more specific, the better the result.", color = RedxTextMuted, fontSize = 13.sp) },
            modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
            colors = redxTextFieldColors(),
            maxLines = 10,
            shape = RoundedCornerShape(8.dp)
        )

        // Model selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, RedxBorder, RoundedCornerShape(8.dp))
                .clickable(onClick = onModelPicker)
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.SmartToy, contentDescription = null, tint = RedxTextSecondary, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("AI Model", color = RedxTextSecondary, fontSize = 11.sp)
                Text(state.selectedModel.substringAfterLast("/"), color = RedxTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
            Icon(Icons.Default.ExpandMore, contentDescription = null, tint = RedxTextMuted)
        }

        state.error?.let { error ->
            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(RedxRed.copy(alpha = 0.1f)).padding(10.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(Icons.Default.Error, contentDescription = null, tint = RedxRedBright, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text(error, color = RedxRedBright, fontSize = 12.sp, modifier = Modifier.weight(1f))
                IconButton(onClick = onClearError, modifier = Modifier.size(20.dp)) {
                    Icon(Icons.Default.Close, contentDescription = null, tint = RedxRedBright, modifier = Modifier.size(14.dp))
                }
            }
        }

        Button(
            onClick = onSubmit,
            enabled = !state.isSubmitting,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = RedxRed, disabledContainerColor = RedxRedDim)
        ) {
            if (state.isSubmitting) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                Spacer(Modifier.width(10.dp))
                Text("Generating & Pushing...")
            } else {
                Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Build APK", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun BuildCard(build: BuildEntity, onDownload: () -> Unit) {
    val statusColor = when (build.status) {
        BuildStatus.SUCCESS -> RedxGreen
        BuildStatus.FAILED -> RedxRedBright
        BuildStatus.RUNNING -> RedxBlue
        BuildStatus.FIXING -> RedxOrange
        BuildStatus.PUSHING -> RedxPurple
        else -> RedxTextMuted
    }
    val statusIcon = when (build.status) {
        BuildStatus.SUCCESS -> Icons.Default.CheckCircle
        BuildStatus.FAILED -> Icons.Default.Cancel
        BuildStatus.RUNNING -> Icons.Default.Sync
        BuildStatus.FIXING -> Icons.Default.Build
        BuildStatus.PUSHING -> Icons.Default.Upload
        else -> Icons.Default.Schedule
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(RedxSurface)
            .border(1.dp, statusColor.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(statusIcon, contentDescription = null, tint = statusColor, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(build.appName, color = RedxTextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text(build.status.uppercase() + if (build.attempt > 1) " · Attempt ${build.attempt}" else "", color = statusColor, fontSize = 11.sp, fontWeight = FontWeight.Medium)
            }
        }

        if (build.description.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(build.description, color = RedxTextSecondary, fontSize = 12.sp, lineHeight = 16.sp, maxLines = 2)
        }

        if (build.fixSummary != null) {
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(6.dp)).background(RedxOrange.copy(alpha = 0.08f)).padding(8.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(Icons.Default.AutoFixHigh, contentDescription = null, tint = RedxOrange, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(6.dp))
                Text("Fix applied: ${build.fixSummary}", color = RedxOrange, fontSize = 11.sp, lineHeight = 15.sp)
            }
        }

        if (build.status == BuildStatus.SUCCESS && build.apkUrl != null) {
            Spacer(Modifier.height(10.dp))
            Button(
                onClick = onDownload,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RedxGreen.copy(alpha = 0.15f)),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Icon(Icons.Default.Download, contentDescription = null, tint = RedxGreen, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text("Download APK", color = RedxGreen, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            }
        }

        if (build.status == BuildStatus.RUNNING || build.status == BuildStatus.FIXING || build.status == BuildStatus.PUSHING) {
            Spacer(Modifier.height(10.dp))
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(50)),
                color = statusColor,
                trackColor = statusColor.copy(alpha = 0.15f)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                when (build.status) {
                    BuildStatus.PUSHING -> "Generating code & pushing to GitHub..."
                    BuildStatus.RUNNING -> "GitHub Actions compiling APK..."
                    BuildStatus.FIXING -> "Analyzing error logs and applying fix..."
                    else -> ""
                },
                color = statusColor, fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun redxTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = RedxRed,
    unfocusedBorderColor = RedxBorder,
    focusedTextColor = RedxTextPrimary,
    unfocusedTextColor = RedxTextPrimary,
    cursorColor = RedxRed,
    focusedLabelColor = RedxRed,
    unfocusedLabelColor = RedxTextSecondary
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BuildModelPickerSheet(
    models: List<com.redxai.data.remote.openrouter.ModelEntry>,
    current: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = RedxSurface,
        dragHandle = { BottomSheetDefaults.DragHandle(color = RedxBorder) }
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp).padding(bottom = 32.dp)) {
            Text("Select Build Model", color = RedxTextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(Modifier.height(4.dp))
            Text("Choose the AI that will generate your Android app", color = RedxTextMuted, fontSize = 12.sp)
            Spacer(Modifier.height(16.dp))
            models.forEach { model ->
                val isSelected = model.id == current
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) RedxRed.copy(alpha = 0.1f) else Color.Transparent)
                        .border(1.dp, if (isSelected) RedxRed.copy(alpha = 0.4f) else Color.Transparent, RoundedCornerShape(8.dp))
                        .clickable { onSelect(model.id) }
                        .padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(model.name, color = if (isSelected) RedxRed else RedxTextPrimary, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                        Text(model.description, color = RedxTextMuted, fontSize = 11.sp, lineHeight = 15.sp)
                    }
                    if (isSelected) Icon(Icons.Default.Check, contentDescription = null, tint = RedxRed, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.height(4.dp))
            }
        }
    }
}
