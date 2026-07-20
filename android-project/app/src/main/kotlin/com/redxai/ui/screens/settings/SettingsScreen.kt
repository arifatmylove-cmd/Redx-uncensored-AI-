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
    val state   by viewModel.state.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(RedxBackground)
    ) {
        // ── Top bar ──────────────────────────────────────────────────────────
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
            modifier            = Modifier.fillMaxSize().navigationBarsPadding(),
            contentPadding      = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── Venice.ai (Primary AI) ───────────────────────────────────────
            item {
                SettingsSection(title = "AI Configuration", icon = Icons.Default.Psychology, iconTint = RedxRed) {

                    // Provider toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(RedxSurfaceVariant)
                            .padding(4.dp)
                    ) {
                        listOf("venice" to "🔓 Venice (Uncensored)", "openrouter" to "🌐 OpenRouter").forEach { (key, label) ->
                            val selected = state.aiProvider == key
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selected) RedxRed else Color.Transparent)
                                    .clickable { viewModel.setAiProvider(key) }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    label,
                                    color      = if (selected) Color.White else RedxTextSecondary,
                                    fontSize   = 12.sp,
                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    if (state.aiProvider == "venice") {
                        // Step 1 instruction
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(RedxRed.copy(alpha = 0.07f))
                                .border(1.dp, RedxRed.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Text("How to get your free Venice.ai key:", color = RedxTextPrimary,
                                    fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Spacer(Modifier.height(6.dp))
                                listOf(
                                    "1. Tap the button below to open venice.ai",
                                    "2. Create a free account (or sign in)",
                                    "3. Go to Settings → API Keys → Generate",
                                    "4. Copy the key and paste it below",
                                    "5. Tap Save, then Test Connection"
                                ).forEach { step ->
                                    Text(step, color = RedxTextSecondary, fontSize = 12.sp, lineHeight = 19.sp)
                                }
                            }
                        }

                        Spacer(Modifier.height(10.dp))

                        // Open venice.ai button
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, RedxRed.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .clickable {
                                    context.startActivity(
                                        Intent(Intent.ACTION_VIEW, Uri.parse("https://venice.ai/settings/api"))
                                    )
                                }
                                .padding(12.dp),
                            verticalAlignment  = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.OpenInBrowser, contentDescription = null,
                                tint = RedxRed, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Open venice.ai → Get Free API Key", color = RedxRed,
                                fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }

                        Spacer(Modifier.height(10.dp))

                        SecretField(
                            label         = "Venice API Key",
                            value         = state.veniceKey,
                            onValueChange = viewModel::setVeniceKey,
                            placeholder   = "Paste your Venice API key here…",
                            helper        = "Free key from venice.ai · Fully uncensored, zero filters"
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF6E40C9).copy(alpha = 0.07f))
                                .border(1.dp, Color(0xFF6E40C9).copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Text("Free OpenRouter models (no credits needed):", color = RedxTextPrimary,
                                    fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Spacer(Modifier.height(4.dp))
                                Text("Gemini 2.0 Flash · DeepSeek R1 · Llama 3.3 70B · Qwen3 235B",
                                    color = RedxTextSecondary, fontSize = 12.sp)
                                Spacer(Modifier.height(8.dp))
                                Text("1. Open openrouter.ai and create a free account\n2. Generate an API key\n3. Paste it below",
                                    color = RedxTextSecondary, fontSize = 12.sp, lineHeight = 18.sp)
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, Color(0xFF6E40C9).copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .clickable {
                                    context.startActivity(
                                        Intent(Intent.ACTION_VIEW, Uri.parse("https://openrouter.ai/keys"))
                                    )
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.OpenInBrowser, contentDescription = null,
                                tint = Color(0xFF6E40C9), modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Open openrouter.ai → Get Free Key", color = Color(0xFF6E40C9),
                                fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(Modifier.height(10.dp))
                        SecretField(
                            label         = "OpenRouter API Key",
                            value         = state.openrouterKey,
                            onValueChange = viewModel::setOpenrouterKey,
                            placeholder   = "sk-or-xxxxxxxxxxxxxxxxxxxx",
                            helper        = "Free key from openrouter.ai — many free models available"
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    // Save + Test row
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        SettingsButton(
                            text      = "Save Key",
                            icon      = Icons.Default.Save,
                            onClick   = viewModel::saveAiSettings,
                            isLoading = state.isSaving,
                            color     = RedxRed,
                            modifier  = Modifier.weight(1f)
                        )
                        if (state.aiProvider == "venice") {
                            SettingsButton(
                                text      = "Test Connection",
                                icon      = Icons.Default.Wifi,
                                onClick   = viewModel::testVeniceConnection,
                                isLoading = state.isTesting,
                                color     = RedxGreen,
                                modifier  = Modifier.weight(1f)
                            )
                        }
                    }

                    // Save message
                    state.aiSaveMessage?.let { msg ->
                        Spacer(Modifier.height(6.dp))
                        Text(msg,
                            color    = if (msg.startsWith("✓")) RedxGreen else RedxRedBright,
                            fontSize = 12.sp, lineHeight = 17.sp)
                    }

                    // Test result
                    state.aiTestMessage?.let { msg ->
                        Spacer(Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (msg.startsWith("✓")) RedxGreen.copy(alpha = 0.1f)
                                    else RedxRedBright.copy(alpha = 0.1f)
                                )
                                .border(
                                    1.dp,
                                    if (msg.startsWith("✓")) RedxGreen.copy(alpha = 0.3f)
                                    else RedxRedBright.copy(alpha = 0.3f),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(10.dp)
                        ) {
                            Text(
                                msg,
                                color    = if (msg.startsWith("✓")) RedxGreen else RedxRedBright,
                                fontSize = 12.sp, lineHeight = 17.sp
                            )
                        }
                    }
                }
            }

            // ── GitHub Integration ──────────────────────────────────────────
            item {
                SettingsSection(title = "GitHub Integration (APK Builder)", icon = Icons.Default.Code, iconTint = Color(0xFF6E40C9)) {
                    Text(
                        "Required for the APK Builder. Your GitHub account is used to store generated code and run builds via GitHub Actions.",
                        color = RedxTextSecondary, fontSize = 12.sp, lineHeight = 17.sp
                    )
                    Spacer(Modifier.height(12.dp))

                    // Step-by-step GitHub setup guide
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF238636).copy(alpha = 0.07f))
                            .border(1.dp, Color(0xFF238636).copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Text("How to connect GitHub:", color = RedxTextPrimary,
                                fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Spacer(Modifier.height(6.dp))
                            listOf(
                                "1. Tap 'Generate GitHub Token' below",
                                "2. Browser opens — all permissions are pre-selected",
                                "3. Scroll down → click 'Generate token'",
                                "4. Copy the token (starts with ghp_)",
                                "5. Come back here, paste it in the Token field",
                                "6. Fill in your GitHub username and repo name",
                                "7. Tap Save then Verify"
                            ).forEach { step ->
                                Text(step, color = RedxTextSecondary, fontSize = 12.sp, lineHeight = 19.sp)
                            }
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    // Quick Connect button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF238636).copy(alpha = 0.12f))
                            .border(1.dp, Color(0xFF238636).copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                            .clickable {
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
                            Text("Generate GitHub Token", color = Color(0xFF3FB950),
                                fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Opens GitHub with all permissions pre-selected → just click Generate",
                                color = Color(0xFF3FB950).copy(alpha = 0.7f), fontSize = 11.sp, lineHeight = 15.sp)
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    SettingsTextField(
                        label         = "GitHub Username",
                        value         = state.githubUsername,
                        onValueChange = viewModel::setGithubUsername,
                        placeholder   = "your-github-username"
                    )
                    Spacer(Modifier.height(10.dp))
                    SettingsTextField(
                        label         = "Repository Name",
                        value         = state.githubRepo,
                        onValueChange = viewModel::setGithubRepo,
                        placeholder   = "Redx-uncensored-AI-"
                    )
                    Spacer(Modifier.height(10.dp))
                    SecretField(
                        label         = "Personal Access Token",
                        value         = state.githubToken,
                        onValueChange = viewModel::setGithubToken,
                        placeholder   = "ghp_xxxxxxxxxxxxxxxxxxxx",
                        helper        = "Generated via 'Generate GitHub Token' above — starts with ghp_"
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
                            color    = if (msg.startsWith("✓")) RedxGreen else RedxRedBright,
                            fontSize = 12.sp)
                    }
                }
            }

            // ── Firebase (Optional) ─────────────────────────────────────────
            item {
                SettingsSection(title = "Firebase (Optional)", icon = Icons.Default.LocalFireDepartment, iconTint = RedxOrange) {
                    Text(
                        "Link Firebase to generated apps that need authentication, Firestore, or cloud storage. Paste your google-services.json content.",
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
                            color    = if (msg.startsWith("✓")) RedxGreen else RedxRedBright,
                            fontSize = 12.sp)
                    }
                }
            }

            // ── About ───────────────────────────────────────────────────────
            item {
                SettingsSection(title = "About Redx AI", icon = Icons.Default.Info, iconTint = RedxTextSecondary) {
                    InfoRow("Version",       "2.0.0")
                    InfoRow("Primary AI",    "Venice.ai — fully uncensored (free)")
                    InfoRow("Backup AI",     "OpenRouter — free & paid models")
                    InfoRow("Build Engine",  "GitHub Actions + Gradle 8.9")
                    InfoRow("Chat-to-Build", "✓ Describe an app in chat to build it")
                    InfoRow("Wake Lock",     "✓ CPU stays awake during builds")
                    InfoRow("Restrictions",  "None — fully unrestricted")
                }
            }

            item { Spacer(Modifier.height(40.dp)) }
        }
    }
}

// ── Reusable composables ──────────────────────────────────────────────────────

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
            value                = value,
            onValueChange        = onValueChange,
            label                = { Text(label, color = RedxTextSecondary) },
            placeholder          = { Text(placeholder, color = RedxTextMuted, fontSize = 12.sp) },
            modifier             = Modifier.fillMaxWidth(),
            colors               = redxFieldColors(),
            singleLine           = true,
            shape                = RoundedCornerShape(8.dp),
            visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions      = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon         = {
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
        onClick        = onClick,
        enabled        = !isLoading,
        modifier       = modifier.height(46.dp),
        shape          = RoundedCornerShape(8.dp),
        colors         = ButtonDefaults.buttonColors(
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
    focusedBorderColor      = RedxRed,
    unfocusedBorderColor    = RedxBorder,
    focusedTextColor        = RedxTextPrimary,
    unfocusedTextColor      = RedxTextPrimary,
    cursorColor             = RedxRed,
    focusedLabelColor       = RedxRed,
    unfocusedLabelColor     = RedxTextSecondary,
    focusedContainerColor   = Color.Transparent,
    unfocusedContainerColor = Color.Transparent
)
