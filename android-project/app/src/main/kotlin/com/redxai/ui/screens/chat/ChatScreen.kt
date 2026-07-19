package com.redxai.ui.screens.chat

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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
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

    Column(modifier = Modifier.fillMaxSize().background(RedxBackground).imePadding()) {

        // Top bar
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
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
                state.chat?.let { chat ->
                    Text(chat.model.substringAfterLast("/"), color = RedxTextMuted, fontSize = 11.sp, maxLines = 1)
                }
            }
            IconButton(onClick = { showModelPicker = true }) {
                Icon(Icons.Default.Tune, contentDescription = "Change model", tint = RedxTextSecondary)
            }
        }

        HorizontalDivider(color = RedxBorder)

        // Messages
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            state = listState,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (state.messages.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(top = 80.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("REDX AI", color = RedxRed, fontSize = 28.sp, fontWeight = FontWeight.Black, letterSpacing = 4.sp)
                            Spacer(Modifier.height(8.dp))
                            Text("Uncensored · Unrestricted", color = RedxTextMuted, fontSize = 12.sp)
                            Spacer(Modifier.height(4.dp))
                            Text("Ask anything. No filters.", color = RedxTextMuted, fontSize = 12.sp)
                        }
                    }
                }
            }
            items(state.messages, key = { it.id }) { msg ->
                MessageBubble(msg)
            }
            if (state.isTyping) {
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp, 16.dp, 16.dp, 16.dp))
                                .background(RedxSurface)
                                .border(1.dp, RedxBorder, RoundedCornerShape(4.dp, 16.dp, 16.dp, 16.dp))
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                repeat(3) { i ->
                                    Box(modifier = Modifier.size(6.dp).clip(RoundedCornerShape(50)).background(RedxRed.copy(alpha = 0.6f + i * 0.2f)))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Error
        state.error?.let { error ->
            Box(modifier = Modifier.fillMaxWidth().background(RedxRed.copy(alpha = 0.12f)).padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Error, contentDescription = null, tint = RedxRedBright, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(error, color = RedxRedBright, fontSize = 13.sp, modifier = Modifier.weight(1f))
                    IconButton(onClick = { viewModel.clearError() }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = RedxRedBright, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }

        // Input bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(RedxSurface)
                .border(width = 1.dp, color = RedxBorder.copy(alpha = 0.5f), shape = RoundedCornerShape(0.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            TextField(
                value = state.inputText,
                onValueChange = { viewModel.setInput(it) },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Message Redx AI...", color = RedxTextMuted, fontSize = 14.sp) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedTextColor = RedxTextPrimary,
                    unfocusedTextColor = RedxTextPrimary,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                maxLines = 6
            )
            IconButton(
                onClick = { viewModel.send(chatId) },
                enabled = state.inputText.isNotBlank() && !state.isTyping
            ) {
                Icon(
                    Icons.Default.Send,
                    contentDescription = "Send",
                    tint = if (state.inputText.isNotBlank() && !state.isTyping) RedxRed else RedxTextMuted
                )
            }
        }
    }

    // Model picker
    if (showModelPicker) {
        ModelPickerSheet(
            models = viewModel.availableModels,
            current = state.chat?.model ?: "",
            onSelect = { model ->
                viewModel.changeModel(chatId, model)
                showModelPicker = false
            },
            onDismiss = { showModelPicker = false }
        )
    }
}

@Composable
private fun MessageBubble(msg: MessageEntity) {
    val isUser = msg.role == "user"
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier.size(28.dp).clip(RoundedCornerShape(50)).background(RedxRed.copy(alpha = 0.15f)),
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
                // Detect code blocks
                val content = msg.content
                if (content.contains("```")) {
                    CodeAwareText(content)
                } else {
                    Text(
                        text = content,
                        color = RedxTextPrimary,
                        fontSize = 14.sp,
                        lineHeight = 21.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun CodeAwareText(content: String) {
    val parts = content.split("```")
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        parts.forEachIndexed { index, part ->
            if (index % 2 == 0) {
                if (part.isNotBlank()) {
                    Text(part.trim(), color = RedxTextPrimary, fontSize = 14.sp, lineHeight = 21.sp)
                }
            } else {
                // Code block
                val lines = part.trimStart('\n')
                val lang = lines.substringBefore('\n').trim()
                val code = if (lang.none { it.isWhitespace() } && lang.length < 20) lines.substringAfter('\n') else lines
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModelPickerSheet(
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
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 32.dp)) {
            Text("Select AI Model", color = RedxTextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(Modifier.height(4.dp))
            Text("All models are uncensored and unrestricted", color = RedxTextMuted, fontSize = 12.sp)
            Spacer(Modifier.height(16.dp))
            models.forEach { model ->
                val isSelected = model.id == current
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) RedxRed.copy(alpha = 0.1f) else Color.Transparent)
                        .border(1.dp, if (isSelected) RedxRed.copy(alpha = 0.4f) else Color.Transparent, RoundedCornerShape(10.dp))
                        .clickable { onSelect(model.id) }
                        .padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(model.name, color = if (isSelected) RedxRed else RedxTextPrimary, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                        Spacer(Modifier.height(2.dp))
                        Text(model.description, color = RedxTextMuted, fontSize = 11.sp, lineHeight = 15.sp)
                    }
                    if (isSelected) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = RedxRed, modifier = Modifier.size(18.dp).padding(top = 2.dp))
                    }
                }
                Spacer(Modifier.height(6.dp))
            }
        }
    }
}

