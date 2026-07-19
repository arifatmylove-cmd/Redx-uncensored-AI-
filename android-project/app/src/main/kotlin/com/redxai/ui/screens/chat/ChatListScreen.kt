package com.redxai.ui.screens.chat

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.redxai.data.local.entities.ChatEntity
import com.redxai.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ChatListScreen(
    onChatSelected: (Long) -> Unit,
    onNewChat: (Long) -> Unit,
    onBack: () -> Unit,
    viewModel: ChatListViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().background(RedxBackground)
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = RedxTextPrimary)
            }
            Text("Chats", color = RedxTextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.weight(1f))
            IconButton(onClick = { viewModel.newChat(onNewChat) }) {
                Icon(Icons.Default.Add, contentDescription = "New Chat", tint = RedxRed)
            }
        }

        HorizontalDivider(color = RedxBorder)

        if (state.chats.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Chat, contentDescription = null, tint = RedxTextMuted, modifier = Modifier.size(56.dp))
                    Spacer(Modifier.height(16.dp))
                    Text("No chats yet", color = RedxTextSecondary, fontSize = 16.sp)
                    Spacer(Modifier.height(8.dp))
                    Text("Tap + to start a new conversation", color = RedxTextMuted, fontSize = 13.sp)
                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = { viewModel.newChat(onNewChat) },
                        colors = ButtonDefaults.buttonColors(containerColor = RedxRed)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("New Chat")
                    }
                }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(state.chats, key = { it.id }) { chat ->
                    ChatListItem(
                        chat = chat,
                        onClick = { onChatSelected(chat.id) },
                        onDelete = { viewModel.deleteChat(chat.id) }
                    )
                    HorizontalDivider(color = RedxBorder.copy(alpha = 0.5f), modifier = Modifier.padding(horizontal = 16.dp))
                }
            }
        }
    }
}

@Composable
private fun ChatListItem(
    chat: ChatEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val dateStr = remember(chat.updatedAt) {
        SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(Date(chat.updatedAt))
    }

    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(RedxRed.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Chat, contentDescription = null, tint = RedxRed, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(chat.title, color = RedxTextPrimary, fontWeight = FontWeight.Medium, fontSize = 15.sp, maxLines = 1)
            Spacer(Modifier.height(2.dp))
            Text(
                chat.model.substringAfterLast("/"),
                color = RedxTextMuted, fontSize = 11.sp
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(dateStr, color = RedxTextMuted, fontSize = 11.sp)
            Spacer(Modifier.height(4.dp))
            Box {
                IconButton(onClick = { showMenu = true }, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.MoreVert, contentDescription = null, tint = RedxTextMuted, modifier = Modifier.size(16.dp))
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    containerColor = RedxSurfaceVariant
                ) {
                    DropdownMenuItem(
                        text = { Text("Delete", color = RedxRedBright) },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = RedxRedBright) },
                        onClick = { showMenu = false; onDelete() }
                    )
                }
            }
        }
    }
}
