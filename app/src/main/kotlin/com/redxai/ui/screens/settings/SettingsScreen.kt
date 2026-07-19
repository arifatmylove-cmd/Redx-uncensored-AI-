package com.redxai.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.redxai.ui.theme.*

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Column(modifier = Modifier.fillMaxSize().background(RedxBackground)) {
        // Top bar
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = RedxTextPrimary)
            }
            Text("Settings", color = RedxTextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }
        HorizontalDivider(color = RedxBorder)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // AI Settings section
            item {
                SettingsSection(title = "AI Configuration", icon = Icons.Default.SmartToy, iconTint = RedxRed) {
                    SecretField(
                        label = "OpenRouter API Key",
                        value = state.openrouterKey,
                        onValueChange = viewModel::setOpenrouterKey,
                        placeholder = "sk-or-xxxxxxxxxxxxxxxxxxxx",
                        helper = "Get a free key at openrouter.ai · Powers all AI chat and APK generation"
                    )
                    Spacer(Modifier.height(12.dp))
                    ModelDropdown(
                        label = "Default AI Model",
                        value = state.defaultModel,
                        onSelect = viewModel::setDefaultModel
                    )
                    Spacer(Modifier.height(12.dp))
                    SettingsButton(
                        text = "Save AI Settings",
                        icon = Icons.Default.Save,
                        onClick = viewModel::saveAiSettings,
                        isLoading = state.isSaving,
                        color = RedxRed
                    )
                    state.aiSaveMessage?.let { msg ->
                        Spacer(Modifier.height(6.dp))
                        Text(msg, color = if (msg.startsWith("✓")) RedxGreen else RedxRedBright, fontSize = 12.sp)
                    }
                }
            }

            // GitHub section
            item {
                SettingsSection(title = "GitHub Integration", icon = Icons.Default.Code, iconTint = Color(0xFF6E40C9)) {
                    SettingsTextField(
                        label = "GitHub Username",
                        value = state.githubUsername,
                        onValueChange = viewModel::setGithubUsername,
                        placeholder = "your-github-username"
                    )
                    Spacer(Modifier.height(10.dp))
                    SettingsTextField(
                        label = "Repository Name",
                        value = state.githubRepo,
                        onValueChange = viewModel::setGithubRepo,
                        placeholder = "my-apk-repo"
                    )
                    Spacer(Modifier.height(10.dp))
                    SecretField(
                        label = "Personal Access Token",
                        value = state.githubToken,
                        onValueChange = viewModel::setGithubToken,
                        placeholder = "ghp_xxxxxxxxxxxxxxxxxxxx",
                        helper = "Needs: repo, workflow scopes · github.com → Settings → Developer settings → PAT"
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        SettingsButton(
                            text = "Save",
                            icon = Icons.Default.Save,
                            onClick = viewModel::saveGitHubSettings,
                            isLoading = state.isVerifying,
                            color = Color(0xFF6E40C9),
                            modifier = Modifier.weight(1f)
                        )
                        SettingsButton(
                            text = "Verify",
                            icon = Icons.Default.CheckCircle,
                            onClick = viewModel::verifyGitHub,
                            isLoading = state.isVerifying,
                            color = RedxGreen,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    state.githubMessage?.let { msg ->
                        Spacer(Modifier.height(6.dp))
                        Text(msg, color = if (msg.startsWith("✓")) RedxGreen else RedxRedBright, fontSize = 12.sp)
                    }
                }
            }

            // Firebase section
            item {
                SettingsSection(title = "Firebase (Optional)", icon = Icons.Default.LocalFireDepartment, iconTint = RedxOrange) {
                    Text(
                        "Link your Firebase project for APKs that require auth, Firestore, or cloud storage. Paste your google-services.json content below.",
                        color = RedxTextSecondary, fontSize = 12.sp, lineHeight = 17.sp
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = state.firebaseConfig,
                        onValueChange = viewModel::setFirebaseConfig,
                        label = { Text("google-services.json content", color = RedxTextSecondary) },
                        placeholder = { Text("Paste JSON content here...", color = RedxTextMuted, fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 80.dp),
                        colors = redxFieldColors(),
                        maxLines = 6,
                        shape = RoundedCornerShape(8.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    SettingsButton(
                        text = "Save Firebase Config",
                        icon = Icons.Default.Save,
                        onClick = viewModel::saveFirebaseConfig,
                        isLoading = false,
                        color = RedxOrange
                    )
                    state.firebaseMessage?.let { msg ->
                        Spacer(Modifier.height(6.dp))
                        Text(msg, color = if (msg.startsWith("✓")) RedxGreen else RedxRedBright, fontSize = 12.sp)
                    }
                }
            }

            // About section
            item {
                SettingsSection(title = "About Redx AI", icon = Icons.Default.Info, iconTint = RedxTextSecondary) {
                    InfoRow("Version", "1.0.0")
                    InfoRow("Model", "cognitivecomputations/dolphin-llama-3-70b + 9 more")
                    InfoRow("Build Engine", "GitHub Actions + Gradle 8.9")
                    InfoRow("Restrictions", "None — fully unrestricted")
                }
            }

            item { Spacer(Modifier.height(40.dp)) }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    icon: ImageVector,
    iconTint: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(RedxSurface)
            .border(1.dp, RedxBorder, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text(title, color = RedxTextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
        }
        Spacer(Modifier.height(16.dp))
        content()
    }
}

@Composable
private fun SecretField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    helper: String
) {
    var visible by remember { mutableStateOf(false) }
    Column {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label, color = RedxTextSecondary) },
            placeholder = { Text(placeholder, color = RedxTextMuted, fontSize = 12.sp) },
            modifier = Modifier.fillMaxWidth(),
            colors = redxFieldColors(),
            singleLine = true,
            shape = RoundedCornerShape(8.dp),
            visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                IconButton(onClick = { visible = !visible }) {
                    Icon(
                        if (visible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = null, tint = RedxTextMuted, modifier = Modifier.size(18.dp)
                    )
                }
            }
        )
        Spacer(Modifier.height(3.dp))
        Text(helper, color = RedxTextMuted, fontSize = 10.sp, lineHeight = 14.sp)
    }
}

@Composable
private fun SettingsTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = RedxTextSecondary) },
        placeholder = { Text(placeholder, color = RedxTextMuted, fontSize = 12.sp) },
        modifier = Modifier.fillMaxWidth(),
        colors = redxFieldColors(),
        singleLine = true,
        shape = RoundedCornerShape(8.dp)
    )
}

@Composable
private fun ModelDropdown(label: String, value: String, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val models = com.redxai.data.remote.openrouter.RedxModels.models
    Box {
        OutlinedTextField(
            value = value.substringAfterLast("/"),
            onValueChange = {},
            readOnly = true,
            label = { Text(label, color = RedxTextSecondary) },
            modifier = Modifier.fillMaxWidth(),
            colors = redxFieldColors(),
            singleLine = true,
            shape = RoundedCornerShape(8.dp),
            trailingIcon = { IconButton(onClick = { expanded = true }) { Icon(Icons.Default.ExpandMore, null, tint = RedxTextMuted) } }
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, containerColor = RedxSurfaceVariant) {
            models.forEach { model ->
                DropdownMenuItem(
                    text = { Column {
                        Text(model.name, color = RedxTextPrimary, fontSize = 13.sp)
                        Text(model.description, color = RedxTextMuted, fontSize = 10.sp)
                    }},
                    onClick = { onSelect(model.id); expanded = false }
                )
            }
        }
    }
}

@Composable
private fun SettingsButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    isLoading: Boolean,
    color: Color,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = !isLoading,
        modifier = modifier.height(44.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color.copy(alpha = 0.15f), disabledContainerColor = color.copy(alpha = 0.08f)),
        contentPadding = PaddingValues(horizontal = 12.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(color = color, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
        } else {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text(text, color = color, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(label, color = RedxTextSecondary, fontSize = 13.sp, modifier = Modifier.width(120.dp))
        Text(value, color = RedxTextPrimary, fontSize = 13.sp)
    }
}

@Composable
private fun redxFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = RedxRed,
    unfocusedBorderColor = RedxBorder,
    focusedTextColor = RedxTextPrimary,
    unfocusedTextColor = RedxTextPrimary,
    cursorColor = RedxRed,
    focusedLabelColor = RedxRed,
    unfocusedLabelColor = RedxTextSecondary,
    focusedContainerColor = Color.Transparent,
    unfocusedContainerColor = Color.Transparent
)
