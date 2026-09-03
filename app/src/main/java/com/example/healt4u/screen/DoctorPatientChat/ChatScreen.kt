package com.example.healt4u.screen.DoctorPatientChat

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.healt4u.Storage.getDoctorById
import com.example.healt4u.Storage.getPatientById
import com.example.healt4u.model.Message
import com.example.healt4u.screen.componentUI.DateHeader
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    chatName: String,
    userId: Int,
    userRole: String,
    conversationId: Int,
    doctorId: Int,
    patientId: Int,
    initialMessages: List<Message>,
    chatExpiryTime: Long = 0L,
    onBack: () -> Unit,
    onSendMessage: (Message) -> Unit,
    onDeleteMessage: (Message) -> Unit,
    onAvatarClick: (Int) -> Unit,
    onClearAllMessages: () -> Unit,
    isMuted: Boolean = false,
    onMuteChanged: (Boolean) -> Unit = {}
) {
    val displayName = chatName

    var myName by remember { mutableStateOf("Me") }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(userId, userRole) {
        coroutineScope.launch {
            myName = if (userRole == "doctor") {
                getDoctorById(userId)?.name ?: "Doctor"
            } else {
                getPatientById(userId)?.name ?: "Patient"
            }
        }
    }

    var messages by remember { mutableStateOf(initialMessages) }
    var textInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    var showMenu by remember { mutableStateOf(false) }
    var showClearAllDialog by remember { mutableStateOf(false) }
    var messageToDelete by remember { mutableStateOf<Message?>(null) }
    var mutedState by remember { mutableStateOf(isMuted) }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    LaunchedEffect(isMuted) {
        mutedState = isMuted
    }

    if (messageToDelete != null) {
        AlertDialog(
            title = { Text("Delete Message") },
            text = { Text("Are you sure you want to delete this message?") },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteMessage(messageToDelete!!)
                    messages = messages.filter { it.id != messageToDelete!!.id }
                    messageToDelete = null
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { messageToDelete = null }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.secondary)
                }
            },
            onDismissRequest = { messageToDelete = null }
        )
    }

    if (showClearAllDialog) {
        AlertDialog(
            title = { Text("Clear All Messages") },
            text = { Text("Are you sure you want to clear ALL messages? This cannot be undone!") },
            confirmButton = {
                TextButton(onClick = {
                    onClearAllMessages()
                    messages = emptyList()
                    showClearAllDialog = false
                }) {
                    Text("Clear All", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllDialog = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.secondary)
                }
            },
            onDismissRequest = { showClearAllDialog = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = displayName,
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentWidth(Alignment.CenterHorizontally)
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentWidth(Alignment.CenterHorizontally)
                                .padding(end=12.dp)
                            ,
                            verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(Color(0xFF4CAF50), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Online",
                                style = MaterialTheme.typography.titleSmall,
                                color = Color.White,
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More options")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    if (mutedState) "Unmute Notifications" else "Mute Notifications",
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            },
                            onClick = {
                                mutedState = !mutedState
                                onMuteChanged(mutedState)
                                showMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Clear All Messages", color = MaterialTheme.colorScheme.secondary) },
                            onClick = {
                                showMenu = false
                                showClearAllDialog = true
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        placeholder = { Text("Type here...", color = MaterialTheme.colorScheme.onBackground) },
                        modifier = Modifier
                            .weight(1f)
                            .background(Color.White, shape = RoundedCornerShape(28.dp)),
                        shape = RoundedCornerShape(28.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.secondary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onBackground,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )
                    FloatingActionButton(
                        onClick = {
                            if (textInput.isNotBlank()) {
                                val nowIso = Instant.now().toString()
                                val newMessage = Message(
                                    id = Instant.now().toEpochMilli().toInt(),
                                    conversationId = conversationId,
                                    content = textInput,
                                    senderId = userId,
                                    senderName = myName,
                                    timestamp = nowIso,
                                    type = "text"
                                )
                                messages = messages + newMessage
                                onSendMessage(newMessage)
                                textInput = ""
                            }
                        },
                        containerColor = if (textInput.isNotEmpty()) {
                            MaterialTheme.colorScheme.secondary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        contentColor = if (textInput.isNotEmpty()) {
                            MaterialTheme.colorScheme.onSecondary
                        } else {
                            MaterialTheme.colorScheme.surface
                        },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Send,
                            contentDescription = "Send",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(color = MaterialTheme.colorScheme.primary)
        ) {
            if (messages.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "No messages yet",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = listState,
                    contentPadding = PaddingValues(vertical = 8.dp),
                    reverseLayout = false
                ) {
                    val groupedMessages = groupMessagesByDate(messages)
                    groupedMessages.forEach { group ->
                        item {
                            DateHeader(date = group.dateMillis)
                        }
                        items(
                            items = group.messages,
                            key = { it.id }
                        ) { message ->
                            val isFromCurrentUser = message.senderId == userId
                            MessageBubble(
                                message = message,
                                isFromCurrentUser = isFromCurrentUser,
                                userRole = userRole,
                                otherPersonName = displayName,
                                onAvatarClick = onAvatarClick,
                                onDeleteClick = { messageToDelete = message }
                            )
                        }
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun parseTimestampSafe(timestampStr: String): Instant {
    return try {
        OffsetDateTime.parse(timestampStr).toInstant()
    } catch (e: Exception) {
        try {
            Instant.parse(timestampStr)
        } catch (e2: Exception) {
            Log.w("DateParse", "Failed to parse time: $timestampStr", e2)
            Instant.now()
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun groupMessagesByDate(messages: List<Message>): List<MessageGroup> {
    if (messages.isEmpty()) return emptyList()

    val groups = messages.groupBy { message ->
        getMessageDayKey(message.timestamp)
    }

    return groups.entries
        .sortedBy { it.key }
        .map { entry ->
            val dayKey = entry.key
            val dateMillis = dayKey.atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
            MessageGroup(dateMillis = dateMillis, messages = entry.value)
        }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun getMessageDayKey(timestamp: String): LocalDate {
    val instant = parseTimestampSafe(timestamp)
    return instant.atZone(ZoneId.systemDefault()).toLocalDate()
}

private data class MessageGroup(
    val dateMillis: Long,
    val messages: List<Message>
)

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewChatScreen() {
    com.example.healt4u.screen.componentUI.Theme.colorTheme {
        ChatScreen(
            chatName = "Dr. Sarah Tan",
            userId = 2,
            userRole = "patient",
            conversationId = 1,
            doctorId = 1,
            patientId = 2,
            initialMessages = listOf(
                com.example.healt4u.model.Message(
                    id = 1,
                    conversationId = 1,
                    content = "Hello! How are you feeling today?",
                    senderId = 1,
                    senderName = "Dr. Sarah Tan",
                    timestamp = "2026-09-02 10:00:00Z",
                    type = "text"
                ),
                com.example.healt4u.model.Message(
                    id = 2,
                    conversationId = 1,
                    content = "I am feeling much better, thank you doctor.",
                    senderId = 2,
                    senderName = "Yuki Chung",
                    timestamp = "2026-09-02 11:00:00Z",
                    type = "text"
                )
            ),
            onBack = {},
            onSendMessage = {},
            onDeleteMessage = {},
            onAvatarClick = {},
            onClearAllMessages = {}
        )
    }
}