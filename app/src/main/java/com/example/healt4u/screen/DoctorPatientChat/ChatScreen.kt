package com.example.healt4u.screen.DoctorPatientChat

import android.annotation.SuppressLint
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.healt4u.Storage.getConversationById
import com.example.healt4u.Storage.getDoctorById
import com.example.healt4u.Storage.getPatientById
import com.example.healt4u.Storage.refundPaymentIfEligible
import com.example.healt4u.ViewModel.ConversationViewModel
import com.example.healt4u.model.Message
import com.example.healt4u.screen.componentUI.DateHeader
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.util.Locale
import kotlin.time.Duration.Companion.milliseconds

@SuppressLint("DefaultLocale")
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
    onMuteChanged: (Boolean) -> Unit = {},
    getDoctorStatus: (Int) -> String,
    viewModel: ConversationViewModel = viewModel()
) {
    val displayName = chatName
    var myName by remember { mutableStateOf("Me") }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // Doctor status
    var doctorStatus by remember { mutableStateOf("offline") }

    LaunchedEffect(userId, userRole) {
        coroutineScope.launch {
            myName = if (userRole == "doctor") {
                getDoctorById(userId)?.name ?: "Doctor"
            } else {
                getPatientById(userId)?.name ?: "Patient"
            }
        }
    }

    LaunchedEffect(doctorId) {
        while (true) {
            doctorStatus = getDoctorStatus(doctorId)
            Log.d("ChatStatus", "Doctor $doctorId status → $doctorStatus")
            delay(3000.milliseconds)
        }
    }

    // Status colors
    val statusColor = when (doctorStatus) {
        "available" -> Color(0xFF4CAF50)
        "busy" -> Color(0xFFFF5722)
        else -> Color(0xFF9E9E9E)
    }
    val statusText = doctorStatus.uppercase()

    var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var lockedExpiryTime by remember { mutableLongStateOf(0L) }
    val ONE_DAY_MS = 24 * 60 * 60 * 1000L

    LaunchedEffect(conversationId) {
        coroutineScope.launch {
            val conv = getConversationById(conversationId)
            val createdMs = parseTimestampSafe(conv?.createdTime ?: "").toEpochMilli()
            lockedExpiryTime = createdMs + ONE_DAY_MS
        }
        while (true) {
            currentTime = System.currentTimeMillis()
            delay(1000.milliseconds)
        }
    }

    val remainingMs = lockedExpiryTime - currentTime
    val totalSeconds = (remainingMs / 1000).coerceAtLeast(0)
    val hours = (totalSeconds / 3600).toInt()
    val minutes = ((totalSeconds % 3600) / 60).toInt()
    val seconds = (totalSeconds % 60).toInt()
    val timeText = String.format("%02d:%02d:%02d", hours, minutes, seconds)
    val isChatExpired = lockedExpiryTime > 0 && totalSeconds <= 0L

    // Check if doctor has ever replied
    val hasDoctorReplied = remember(initialMessages) {
        derivedStateOf { initialMessages.any { it.senderId == doctorId } }
    }

    // Trigger refund if expired AND no reply
    LaunchedEffect(isChatExpired, hasDoctorReplied.value) {
        if (isChatExpired && !hasDoctorReplied.value) {
            Log.d("Refund", "Chat expired with no reply — processing refund")
            coroutineScope.launch {
                refundPaymentIfEligible(patientId, doctorId)
            }
        }
    }

    // Can send messages? Only if NOT expired OR doctor already replied
    val canSend = !isChatExpired || hasDoctorReplied.value

    // Messages state
    var messages by remember { mutableStateOf(initialMessages) }
    var textInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Auto-scroll to bottom
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Menu & dialog state
    var showMenu by remember { mutableStateOf(false) }
    var showClearAllDialog by remember { mutableStateOf(false) }
    var messageToDelete by remember { mutableStateOf<Message?>(null) }
    var mutedState by remember { mutableStateOf(isMuted) }

    LaunchedEffect(isMuted) {
        mutedState = isMuted
    }

    // --- Dialogs ---
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

    // --- UI ---
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

                        // ✅ SHOW COUNTDOWN TIMER
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentWidth(Alignment.CenterHorizontally),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (!isChatExpired) {
                                Text(
                                    text = "⏳ Time: $timeText",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (hours < 1) Color(0xFFFFCC00) else Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            } else if (!hasDoctorReplied.value) {
                                Text(
                                    text = "✅ Refund Processed",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF4CAF50),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            } else {
                                Text(
                                    text = "❌ Access Expired",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFFFCC00),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            // Doctor status (only for patient)
                            if (userRole == "patient") {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(statusColor, CircleShape)
                                )
                                Text(
                                    text = statusText,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = Color.White,
                                    fontSize = 11.sp
                                )
                            }
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
            if (isChatExpired && !canSend) {
                // CHAT EXPIRED — Show BLOCKED bar
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFFFFE0E0),
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (!hasDoctorReplied.value) {
                                "⏰ Chat expired — No reply received. Payment refunded."
                            } else {
                                "⏰ Chat access expired (24h limit)"
                            },
                            color = Color(0xFFB71C1C),
                            style = MaterialTheme.typography.bodyMedium,
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                // NORMAL INPUT — Still within 24h OR doctor replied
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
                    if (!isChatExpired && userRole == "patient") {
                        Text(
                            text = "Waiting for doctor's reply...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFFFCC00),
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
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

// --- Helper Functions ---

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
    val groups = messages.groupBy { getMessageDayKey(it.timestamp) }
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
    return parseTimestampSafe(timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
}

private data class MessageGroup(val dateMillis: Long, val messages: List<Message>)

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
            chatExpiryTime = System.currentTimeMillis() + 24 * 60 * 60 * 1000,
            initialMessages = listOf(
                Message(1, 1, "Hello! How are you feeling today?", 1, "Dr. Sarah Tan", "2026-09-02 10:00:00Z", "text"),
                Message(2, 1, "I am feeling much better, thank you doctor.", 2, "Yuki Chung", "2026-09-02 11:00:00Z", "text")
            ),
            onBack = {},
            onSendMessage = {},
            onDeleteMessage = {},
            onAvatarClick = {},
            onClearAllMessages = {},
            getDoctorStatus = {"available"}
        )
    }
}