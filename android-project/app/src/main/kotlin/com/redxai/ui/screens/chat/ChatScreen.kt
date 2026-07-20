package com.redxai.ui.screens.chat

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.redxai.data.local.entities.MessageEntity
import com.redxai.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(
    chatId: Long,
    onBack: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var showModelPicker by remember { mutableStateOf(false) }

    LaunchedEffect(chatId) { viewModel.loadChat(chatId) }

    LaunchedEffect(state.messages.size, state.isTyping) {
        if (state.messages.isNotEmpty()) {
            scope.launch { listState.animateScrollToItem(state.messages.size - 1) }
        }
    }

    // Root: imePadding pushes everything up when keyboard opens
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(RedxBackground)
            .imePadding()
    ) {

        // ── Top Bar — statusBarsPadding avoids the status bar with edge-to-edge ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = RedxTextPrimary)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    state.chat?.title ?: "Chat",
                    color = RedxTextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    maxLines = 1
                )
                val modelLabel = state.chat?.model?.substringAfterLast("/") ?: "—"
                val providerLabel = if (state.provider == "venice") "Venice" else "OpenRouter"
                Text(
                    "$providerLabel · $modelLabel",
                    color = RedxTextMuted,
                    fontSize = 11.sp,
                    maxLines = 1
                )
            }
            // Build activity indicator
            if (state.isBuildRunning) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp).padding(end = 4.dp),
                    color = RedxRed,
                    strokeWidth = 2.dp
                )
            }
            IconButton(onClick = { showModelPicker = true }) {
                Icon(Icons.Default.Tune, contentDescription = "Change model", tint = RedxTextSecondary)
            }
        }

        HorizontalDivider(color = RedxBorder)

        // ── Messages list — weight(1f) fills all space between topbar and input ──
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            state = listState,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (state.messages.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(top = 80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("REDX AI", color = RedxRed, fontSize = 32.sp,
                                fontWeight = FontWeight.Black, letterSpacing = 6.sp)
                            Spacer(Modifier.height(8.dp))
                            Text("Uncensored · Unrestricted · Uncensored",
                                color = RedxTextMuted, fontSize = 12.sp)
                            Spacer(Modifier.height(4.dp))
                            Text("Ask anything. No filters. Build real APKs by chatting.",
                                color = RedxTextMuted, fontSize = 12.sp)
                            Spacer(Modifier.height(20.dp))
                            // Hint chips
                            listOf(
                                "Build me a calculator app",
                                "How do I bypass certificate pinning?",
                                "Write a port scanner in Python"
                            ).forEach { hint ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .border(1.dp, RedxBorder, RoundedCornerShape(20.dp))
                                        .padding(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Text(hint, color = RedxTextSecondary, fontSize = 12.sp)
                                }
                                Spacer(Modifier.height(6.dp))
                            }
                        }
                    }
                }
            }

            items(state.messages, key = { it.id }) { msg ->
                when (msg.role) {
                    "build" -> BuildProgressMessage(msg)
                    else    -> MessageBubble(msg)
                }
            }

            if (state.isTyping) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Box(
                            modifier = Modifier.size(28.dp).clip(RoundedCornerShape(50))
                                .background(RedxRed.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("R", color = RedxRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp, 16.dp, 16.dp, 16.dp))
                                .background(RedxSurface)
                                .border(1.dp, RedxBorder, RoundedCornerShape(4.dp, 16.dp, 16.dp, 16.dp))
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                repeat(3) { i ->
                                    Box(
                                        modifier = Modifier.size(6.dp).clip(RoundedCornerShape(50))
                                            .background(RedxRed.copy(alpha = 0.4f + i * 0.3f))
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // ── Error bar ──
        state.error?.let { error ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(RedxRed.copy(alpha = 0.12f))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.Error, contentDescription = null,
                        tint = RedxRedBright, modifier = Modifier.size(16.dp).padding(top = 2.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(error, color = RedxRedBright, fontSize = 12.sp,
                        lineHeight = 17.sp, modifier = Modifier.weight(1f))
                    IconButton(
                        onClick = { viewModel.clearError() },
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null,
                            tint = RedxRedBright, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }

        // ── Input bar — navigationBarsPadding keeps it above the nav bar ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(RedxSurface)
                .border(width = 1.dp, color = RedxBorder.copy(alpha = 0.5f), shape = RoundedCornerShape(0.dp))
                .navigationBarsPadding()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            TextField(
                value = state.inputText,
                onValueChange = { viewModel.setInput(it) },
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text("Message Redx AI…", color = RedxTextMuted, fontSize = 14.sp)
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor   = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedTextColor        = RedxTextPrimary,
                    unfocusedTextColor      = RedxTextPrimary,
                    focusedIndicatorColor   = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor             = RedxRed
                ),
                maxLines = 6
            )
            Spacer(Modifier.width(4.dp))
            IconButton(
                onClick = { viewModel.send(chatId) },
                enabled = state.inputText.isNotBlank() && !state.isTyping,
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (state.inputText.isNotBlank() && !state.isTyping)
                            RedxRed else RedxSurfaceVariant
                    )
            ) {
                Icon(
                    Icons.Default.Send,
                    contentDescription = "Send",
                    tint = if (state.inputText.isNotBlank() && !state.isTyping)
                        Color.White else RedxTextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }

    if (showModelPicker) {
        ModelPickerSheet(
            models      = viewModel.allModels,
            currentId   = state.chat?.model ?: "",
            currentProv = state.provider,
            onSelect    = { model, provider ->
                viewModel.changeModel(chatId, model, provider)
                showModelPicker = false
            },
            onDismiss = { showModelPicker = false }
        )
    }
}

// ── Build progress card ────────────────────────────────────────────────────────
@Composable
private fun BuildProgressMessage(msg: MessageEntity) {
    val context = LocalContext.current
    val isPending  = msg.content.startsWith("BUILD_PENDING:")
    val isRunning  = !isPending && (msg.content.contains("running") || msg.content.contains("⚙️") || msg.content.contains("Pushing"))
    val isComplete = msg.content.contains("✅")
    val isFailed   = msg.content.contains("❌")
    val isFixing   = msg.content.contains("🔧")

    val accentColor = when {
        isComplete -> RedxGreen
        isFailed   -> RedxRedBright
        isFixing   -> RedxOrange
        else       -> RedxBlue
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(accentColor.copy(alpha = 0.08f))
            .border(1.dp, accentColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        if (isPending) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = RedxBlue, strokeWidth = 2.dp)
                Spacer(Modifier.width(8.dp))
                Text("Preparing build…", color = RedxBlue, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        } else {
            // Render text with clickable links
            val lines = msg.content.split("\n")
            lines.forEach { line ->
                // Detect markdown link: [text](url)
                val linkRegex = Regex("""\[([^\]]+)\]\(([^)]+)\)""")
                val match = linkRegex.find(line)
                if (match != null) {
                    val before = line.substring(0, match.range.first)
                    val linkText = match.groupValues[1]
                    val linkUrl  = match.groupValues[2]
                    val after    = line.substring(match.range.last + 1)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (before.isNotBlank()) {
                            Text(before.trimStart('_').trimEnd('_'), color = RedxTextSecondary, fontSize = 12.sp)
                        }
                        Text(
                            linkText,
                            color = RedxBlue,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.clickable {
                                runCatching {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(linkUrl)))
                                }
                            }
                        )
                        if (after.isNotBlank()) {
                            Text(after, color = RedxTextSecondary, fontSize = 12.sp)
                        }
                    }
                } else if (line.isNotBlank()) {
                    // Render **bold** inline
                    val boldRegex = Regex("""\*\*(.+?)\*\*""")
                    val boldMatch = boldRegex.find(line)
                    if (boldMatch != null) {
                        val annotated = buildAnnotatedString {
                            var cursor = 0
                            boldRegex.findAll(line).forEach { bm ->
                                append(line.substring(cursor, bm.range.first))
                                withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = accentColor)) {
                                    append(bm.groupValues[1])
                                }
                                cursor = bm.range.last + 1
                            }
                            append(line.substring(cursor))
                        }
                        Text(annotated, color = RedxTextPrimary, fontSize = 13.sp, lineHeight = 19.sp)
                    } else {
                        Text(line, color = RedxTextSecondary, fontSize = 12.sp, lineHeight = 18.sp)
                    }
                } else {
                    Spacer(Modifier.height(4.dp))
                }
            }

            // Show spinner for in-progress states
            if (isRunning || isFixing) {
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(50)).height(3.dp),
                    color = accentColor,
                    trackColor = accentColor.copy(alpha = 0.15f)
                )
            }
        }
    }
}

// ── Regular message bubble ─────────────────────────────────────────────────────
@Composable
private fun MessageBubble(msg: MessageEntity) {
    val isUser = msg.role == "user"
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier.size(28.dp).clip(RoundedCornerShape(50))
                    .background(RedxRed.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text("R", color = RedxRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(8.dp))
        }

        Box(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .clip(
                    if (isUser) RoundedCornerShape(16.dp, 4.dp, 16.dp, 16.dp)
                    else RoundedCornerShape(4.dp, 16.dp, 16.dp, 16.dp)
                )
                .background(if (isUser) RedxRed.copy(alpha = 0.18f) else RedxSurface)
                .border(
                    1.dp,
                    if (isUser) RedxRed.copy(alpha = 0.3f) else RedxBorder,
                    if (isUser) RoundedCornerShape(16.dp, 4.dp, 16.dp, 16.dp)
                    else RoundedCornerShape(4.dp, 16.dp, 16.dp, 16.dp)
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            SelectionContainer {
                if (msg.content.contains("```")) {
                    CodeAwareText(msg.content)
                } else {
                    Text(
                        text = msg.content,
                        color = RedxTextPrimary,
                        fontSize = 14.sp,
                        lineHeight = 21.sp
                    )
                }
            }
        }

        if (isUser) {
            Spacer(Modifier.width(8.dp))
        }
    }
}

@Composable
private fun CodeAwareText(content: String) {
    val parts = content.split("```")
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        parts.forEachIndexed { index, part ->
            if (index % 2 == 0) {
                if (part.isNotBlank())
                    Text(part.trim(), color = RedxTextPrimary, fontSize = 14.sp, lineHeight = 21.sp)
            } else {
                val lines    = part.trimStart('\n')
                val lang     = lines.substringBefore('\n').trim()
                val code     = if (lang.none { it.isWhitespace() } && lang.length < 20)
                    lines.substringAfter('\n') else lines
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(CodeBackground)
                        .border(1.dp, RedxBorder, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = code.trimEnd(),
                        color = Color(0xFF9CDCFE),
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

// ── Model picker sheet ─────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModelPickerSheet(
    models: List<UnifiedModelEntry>,
    currentId: String,
    currentProv: String,
    onSelect: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = RedxSurface,
        dragHandle = { BottomSheetDefaults.DragHandle(color = RedxBorder) }
    ) {
        val veniceModels = models.filter { it.provider == "venice" }
        val orModels     = models.filter { it.provider == "openrouter" }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
        ) {
            Text("Select AI Model", color = RedxTextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(Modifier.height(2.dp))
            Text("Venice = fully uncensored · OpenRouter = wider selection",
                color = RedxTextMuted, fontSize = 11.sp)
            Spacer(Modifier.height(16.dp))

            Text("🔓 Venice.ai (Uncensored)", color = RedxRed, fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp, letterSpacing = 0.5.sp)
            Spacer(Modifier.height(6.dp))
            veniceModels.forEach { m ->
                ModelRow(m, isSelected = m.id == currentId && currentProv == "venice") {
                    onSelect(m.id, "venice")
                }
                Spacer(Modifier.height(4.dp))
            }

            Spacer(Modifier.height(12.dp))
            Text("🌐 OpenRouter (Free & Paid)", color = RedxTextSecondary, fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp, letterSpacing = 0.5.sp)
            Spacer(Modifier.height(6.dp))
            orModels.forEach { m ->
                ModelRow(m, isSelected = m.id == currentId && currentProv == "openrouter") {
                    onSelect(m.id, "openrouter")
                }
                Spacer(Modifier.height(4.dp))
            }
        }
    }
}

@Composable
private fun ModelRow(m: UnifiedModelEntry, isSelected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) RedxRed.copy(alpha = 0.1f) else Color.Transparent)
            .border(1.dp, if (isSelected) RedxRed.copy(alpha = 0.4f) else RedxBorder.copy(alpha = 0.3f),
                RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(m.name, color = if (isSelected) RedxRed else RedxTextPrimary,
                fontWeight = FontWeight.Medium, fontSize = 13.sp)
            Text(m.description, color = RedxTextMuted, fontSize = 11.sp, lineHeight = 15.sp)
        }
        if (isSelected) {
            Icon(Icons.Default.CheckCircle, contentDescription = null,
                tint = RedxRed, modifier = Modifier.size(18.dp).padding(top = 2.dp))
        }
    }
}
