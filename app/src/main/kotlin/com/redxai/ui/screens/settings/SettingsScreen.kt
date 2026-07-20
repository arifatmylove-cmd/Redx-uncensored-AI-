package com.redxai.ui.screens.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
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
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(RedxBackground)
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 4.dp, end = 16.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = RedxTextPrimary)
            }
            Text("Settings", color = RedxTextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }
        HorizontalDivider(color = RedxBorder)

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── Venice.ai (Primary AI) ────────────────────────────────────────
            item {
                SettingsSection(
                    title    = "Venice.ai — Uncensored AI",
                    icon     = Icons.Default.Lock,
                    iconTint = RedxRed
                ) {
                    // Provider toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(RedxSurfaceVariant)
                            .padding(4.dp)
                    ) {
                        listOf("venice" to "Venice (Uncensored)", "openrouter" to "OpenRouter").forEach { (key, label) ->
                            val selected = state.aiProvider == key
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selected) RedxRed else Color.Transparent)
                                    .clickable { viewModel.setAiProvider(key) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    label,
                                    color = if (selected) Color.White else RedxTextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    if (state.aiProvider == "venice") {
                        SecretField(
                            label       = "Venice API Key",
                            value       = state.veniceKey,
                            onValueChange = viewModel::setVeniceKey,
                            placeholder = "your-venice-api-key",
                            helper      = "Get a free key at venice.ai/settings/api · Fully uncensored, no filters"
                        )
                        Spacer(Modifier.height(8.dp))
                        // Link to Venice.ai
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, RedxRed.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .clickable {
                                    context.startActivity(
                                        Intent(Intent.ACTION_VIEW,
                                            Uri.parse("https://venice.ai/settings/api"))
                                    )
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.OpenInBrowser, contentDescription = null,
                                tint = RedxRed, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Get Venice API Key (Free) →", color = RedxRed,
                                fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                    } else {
                        SecretField(
                            label       = "OpenRouter API Key",
                            value       = state.openrouterKey,
                            onValueChange = viewModel::setOpenrouterKey,
                            placeholder = "sk-or-xxxxxxxxxxxxxxxxxxxx",
                            helper      = "Get a free key at openrouter.ai · Some models are FREE, others need credits"
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, Color(0xFF6E40C9).copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .clickable {
                                    context.startActivity(
                                        Intent(Intent.ACTION_VIEW,
                                            Uri.parse("https://openrouter.ai/keys"))
                                    )
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.OpenInBrowser, contentDescription = null,
                                tint = Color(0xFF6E40C9), modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Get OpenRouter Key →", color = Color(0xFF6E40C9),
                                fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                    }

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
                        Text(msg,
                            color = if (msg.startsWith("✓")) RedxGreen else RedxRedBright,
                            fontSize = 12.sp)
                    }
                }
            }

            // ── GitHub Integration ───────────────────────────────────────────
            item {
                SettingsSection(
                    title    = "GitHub Integration",
                    icon     = Icons.Default.Code,
                    iconTint = Color(0xFF6E40C9)
                ) {
                    Text(
                        "GitHub is used to push generated code and trigger APK builds via Actions.",
                        color = RedxTextSecondary, fontSize = 12.sp, lineHeight = 17.sp
                    )
                    Spacer(Modifier.height(12.dp))

                    // ── Quick Connect button ──────────────────────────────────
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF238636).copy(alpha = 0.15f))
                            .border(1.dp, Color(0xFF238636).copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                            .clickable {
                                // Opens GitHub PAT creation page with all required scopes pre-filled
                                // User just clicks "Generate token" — no manual navigation needed
                                context.startActivity(
                                    Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse(
                                            "https://github.com/settings/tokens/new" +
                                            "?scopes=repo,workflow,read:user" +
                                            "&description=Redx+AI+Builder" +
                                            "&default_expires_at=no-expiration"
                                        )
                                    )
                                )
                            }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.OpenInBrowser, contentDescription = null,
                            tint = Color(0xFF3FB950), modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Quick Connect — Open GitHub",
                                color = Color(0xFF3FB950), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text("Opens GitHub with all permissions pre-selected.\nPaste the generated token below.",
                                color = Color(0xFF3FB950).copy(alpha = 0.7f), fontSize = 11.sp, lineHeight = 15.sp)
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    SettingsTextField(
                        label       = "GitHub Username",
                        value       = state.githubUsername,
                        onValueChange = viewModel::setGithubUsername,
                        placeholder = "your-github-username"
                    )
                    Spacer(Modifier.height(10.dp))
                    SettingsTextField(
                        label       = "Repository Name",
                        value       = state.githubRepo,
                        onValueChange = viewModel::setGithubRepo,
                        placeholder = "Redx-uncensored-AI-"
                    )
                    Spacer(Modifier.height(10.dp))
                    SecretField(
                        label       = "Personal Access Token",
                        value       = state.githubToken,
                        onValueChange = viewModel::setGithubToken,
                        placeholder = "ghp_xxxxxxxxxxxxxxxxxxxx",
                        helper      = "Generate via 'Quick Connect' above — needs: repo, workflow, read:user scopes"
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        SettingsButton(
                            text      = "Save",
                            icon      = Icons.Default.Save,
                            onClick   = viewModel::saveGitHubSettings,
                            isLoading = false,
                            color     = Color(0xFF6E40C9),
                            modifier  = Modifier.weight(1f)
                        )
                        SettingsButton(
                            text      = "Verify",
                            icon      = Icons.Default.CheckCircle,
                            onClick   = viewModel::verifyGitHub,
                            isLoading = state.isVerifying,
                            color     = RedxGreen,
                            modifier  = Modifier.weight(1f)
                        )
                    }
                    state.githubMessage?.let { msg ->
                        Spacer(Modifier.height(6.dp))
                        Text(msg,
                            color = if (msg.startsWith("✓")) RedxGreen else RedxRedBright,
                            fontSize = 12.sp)
                    }
                }
            }

            // ── Firebase (Optional) ──────────────────────────────────────────
            item {
                SettingsSection(
                    title    = "Firebase (Optional)",
                    icon     = Icons.Default.LocalFireDepartment,
                    iconTint = RedxOrange
                ) {
                    Text(
                        "Link Firebase to generated apps that need auth, Firestore, or storage. Paste your google-services.json content.",
                        color = RedxTextSecondary, fontSize = 12.sp, lineHeight = 17.sp
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value         = state.firebaseConfig,
                        onValueChange = viewModel::setFirebaseConfig,
                        label         = { Text("google-services.json content", color = RedxTextSecondary) },
                        placeholder   = { Text("Paste JSON here…", color = RedxTextMuted, fontSize = 12.sp) },
                        modifier      = Modifier.fillMaxWidth().heightIn(min = 80.dp),
                        colors        = redxFieldColors(),
                        maxLines      = 6,
                        shape         = RoundedCornerShape(8.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    SettingsButton(
                        text      = "Save Firebase Config",
                        icon      = Icons.Default.Save,
                        onClick   = viewModel::saveFirebaseConfig,
                        isLoading = false,
                        color     = RedxOrange
                    )
                    state.firebaseMessage?.let { msg ->
                        Spacer(Modifier.height(6.dp))
                        Text(msg,
                            color = if (msg.startsWith("✓")) RedxGreen else RedxRedBright,
                            fontSize = 12.sp)
                    }
                }
            }

            // ── About ────────────────────────────────────────────────────────
            item {
                SettingsSection(
                    title    = "About Redx AI",
                    icon     = Icons.Default.Info,
                    iconTint = RedxTextSecondary
                ) {
                    InfoRow("Version",       "2.0.0")
                    InfoRow("Primary AI",    "Venice.ai (Dolphin 72B — uncensored)")
                    InfoRow("Backup AI",     "OpenRouter (Gemini 2.0 Flash Free)")
                    InfoRow("Build Engine",  "GitHub Actions + Gradle 8.9")
                    InfoRow("Chat-to-Build", "✓ Just ask me to build an app")
                    InfoRow("Wake Lock",     "✓ CPU stays awake during builds")
                    InfoRow("Restrictions",  "None — fully unrestricted")
                }
            }

            item { Spacer(Modifier.height(40.dp)) }
        }
    }
}

// ── Reusable composables ───────────────────────────────────────────────────────

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
            value                  = value,
            onValueChange          = onValueChange,
            label                  = { Text(label, color = RedxTextSecondary) },
            placeholder            = { Text(placeholder, color = RedxTextMuted, fontSize = 12.sp) },
            modifier               = Modifier.fillMaxWidth(),
            colors                 = redxFieldColors(),
            singleLine             = true,
            shape                  = RoundedCornerShape(8.dp),
            visualTransformation   = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions        = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon           = {
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
        value         = value,
        onValueChange = onValueChange,
        label         = { Text(label, color = RedxTextSecondary) },
        placeholder   = { Text(placeholder, color = RedxTextMuted, fontSize = 12.sp) },
        modifier      = Modifier.fillMaxWidth(),
        colors        = redxFieldColors(),
        singleLine    = true,
        shape         = RoundedCornerShape(8.dp)
    )
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
        onClick   = onClick,
        enabled   = !isLoading,
        modifier  = modifier.height(44.dp),
        shape     = RoundedCornerShape(8.dp),
        colors    = ButtonDefaults.buttonColors(
            containerColor         = color.copy(alpha = 0.15f),
            disabledContainerColor = color.copy(alpha = 0.08f)
        ),
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
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
        Text(label, color = RedxTextSecondary, fontSize = 12.sp, modifier = Modifier.width(130.dp))
        Text(value, color = RedxTextPrimary, fontSize = 12.sp, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun redxFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor   = RedxRed,
    unfocusedBorderColor = RedxBorder,
    focusedTextColor     = RedxTextPrimary,
    unfocusedTextColor   = RedxTextPrimary,
    cursorColor          = RedxRed,
    focusedLabelColor    = RedxRed,
    unfocusedLabelColor  = RedxTextSecondary,
    focusedContainerColor   = Color.Transparent,
    unfocusedContainerColor = Color.Transparent
)
