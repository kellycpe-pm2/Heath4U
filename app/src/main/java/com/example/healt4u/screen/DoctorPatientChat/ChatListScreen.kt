package com.example.healt4u.screen.DoctorPatientChat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healt4u.model.Conversation
import com.example.healt4u.screen.componentUI.Theme.colorTheme
import com.example.healt4u.Storage.getConversationsByPatient
import com.example.healt4u.Storage.loadConversations
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    patientId: String = "p001",
    onConversationClick: (Conversation) -> Unit,
    onNewChatClick: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    val conversations = remember {
        getConversationsByPatient(context, patientId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "My Chats",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNewChatClick,
                containerColor = MaterialTheme.colorScheme.secondary
            ) {
                Icon(Icons.Filled.Chat, contentDescription = "New Chat")
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (conversations.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "No chats yet",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Tap the + button to start a new chat",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = conversations,
                        key = { it.id }
                    ) { conversation ->
                        ConversationItem(
                            conversation = conversation,
                            onClick = { onConversationClick(conversation) }
                        )
                    }
                }
            }
        }
    }
}
@Composable
fun ConversationItem(
    conversation: Conversation,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = conversation.doctorName.take(2).uppercase(),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = conversation.doctorName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = formatTime(conversation.lastMessageTime),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = conversation.lastMessage,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = conversation.hospitalName,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }

            if (conversation.unreadCount > 0) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(Color.Red),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${conversation.unreadCount}",
                        fontSize = 10.sp,
                        color = Color.White
                    )
                }
            }

            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = "Open chat",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatTime(timestamp: Long): String {
    val now = Calendar.getInstance()
    val date = Calendar.getInstance().apply { time = Date(timestamp) }

    return when {
        isSameDay(date, now) -> {
            val format = SimpleDateFormat("HH:mm", Locale.getDefault())
            format.format(Date(timestamp))
        }
        isYesterday(date, now) -> "Yesterday"
        else -> {
            val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            format.format(Date(timestamp))
        }
    }
}

private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

private fun isYesterday(cal1: Calendar, cal2: Calendar): Boolean {
    val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
    return isSameDay(cal1, yesterday)
}

@Preview(showBackground = true)
@Composable
fun PreviewChatListScreen() {
    colorTheme {
        ChatListScreen(
            patientId = "p001",
            onConversationClick = {},
            onNewChatClick = {},
            onBack = {}
        )
    }
}