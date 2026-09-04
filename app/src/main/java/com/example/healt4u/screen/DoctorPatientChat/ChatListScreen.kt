package com.example.healt4u.screen.DoctorPatientChat

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healt4u.Storage.getConversationsByDoctor
import com.example.healt4u.Storage.getConversationsByPatient
import com.example.healt4u.Storage.getDoctorById
import com.example.healt4u.Storage.getMessagesByConversation
import com.example.healt4u.Storage.getPatientById
import com.example.healt4u.model.Conversation
import com.example.healt4u.screen.componentUI.Theme.colorTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.OffsetDateTime
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.milliseconds

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    userId: Int,
    userRole: String,
    onConversationClick: (Conversation, Long) -> Unit,
    onNewChatClick: () -> Unit,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()

    var conversations by remember { mutableStateOf(emptyList<Conversation>()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(userId, userRole) {
        isLoading = true
        errorMessage = null
        scope.launch {
            try {
                conversations = if (userRole == "doctor") {
                    getConversationsByDoctor(userId)
                } else {
                    getConversationsByPatient(userId)
                }
            } catch (e: Exception) {
                errorMessage = e.message ?: "Failed to load"
            }
            isLoading = false
        }
    }

    // ⏱️ TIMER — updates EVERY 30 SECONDS
    var tickTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            tickTime = System.currentTimeMillis()
            delay(30_000.milliseconds)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "My Chats",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentWidth(Alignment.CenterHorizontally)
                            .padding(end = 48.dp)
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
            if (userRole == "patient") {
                FloatingActionButton(
                    onClick = onNewChatClick,
                    containerColor = MaterialTheme.colorScheme.secondary
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "New Chat")
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when {
                isLoading -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Loading chats...",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                errorMessage != null -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = errorMessage ?: "Something went wrong",
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = {
                            isLoading = true
                            errorMessage = null
                            scope.launch {
                                try {
                                    conversations = if (userRole == "doctor") {
                                        getConversationsByDoctor(userId)
                                    } else {
                                        getConversationsByPatient(userId)
                                    }
                                } catch (e: Exception) {
                                    errorMessage = e.message ?: "Failed to load"
                                }
                                isLoading = false
                            }
                        }) {
                            Text("Retry")
                        }
                    }
                }

                conversations.isEmpty() -> {
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
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            items = conversations,
                            key = { conversation ->
                                (conversation.id ?: 0) to tickTime
                            }
                        ) { conversation ->
                            ConversationItem(
                                userId = userId,
                                userRole = userRole,
                                conversation = conversation,
                                doctorId = conversation.doctorId,
                                patientId = conversation.patientId,
                                currentTimeMs = tickTime,
                                onConversationClick = { expiryTime ->
                                    onConversationClick(conversation, expiryTime)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ConversationItem(
    userId: Int,
    userRole: String,
    conversation: Conversation,
    doctorId: Int,
    patientId: Int,
    currentTimeMs: Long,
    onConversationClick: (Long) -> Unit
) {
    val conversationId = conversation.id ?: 0
    val ONE_DAY_MS = 24 * 60 * 60 * 1000L

    var displayName by remember { mutableStateOf("Loading...") }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(doctorId, patientId, userId, userRole) {
        coroutineScope.launch {
            try {
                displayName = if (userRole == "patient") {
                    getDoctorById(doctorId)?.name ?: "Doctor"
                } else {
                    getPatientById(patientId)?.name ?: "Patient"
                }
            } catch (e: Exception) {
                displayName = if (userRole == "patient") "Doctor" else "Patient"
            }
        }
    }

    val timestampMillis = remember(conversation.lastMessageTime) {
        parseTimestampSafe(conversation.lastMessageTime)
    }

    val createdTimeMs = remember(conversation.createdTime) {
        parseTimestampSafe(conversation.createdTime)
    }

    val expiryTimeMs = remember(createdTimeMs) { createdTimeMs + ONE_DAY_MS }

    var hasDoctorReplied by remember { mutableStateOf(false) }

    LaunchedEffect(conversationId, doctorId) {
        coroutineScope.launch {
            hasDoctorReplied = checkIfDoctorReplied(conversationId, doctorId)
        }
    }

    val remainingTime = expiryTimeMs - currentTimeMs
    val isExpired = remainingTime <= 0L

    val countdownText = when {
        isExpired -> if (!hasDoctorReplied) "Refund Available" else "Expired"
        else -> {
            val hours = TimeUnit.MILLISECONDS.toHours(remainingTime)
            val mins = TimeUnit.MILLISECONDS.toMinutes(remainingTime) % 60
            val secs = TimeUnit.MILLISECONDS.toSeconds(remainingTime) % 60
            when {
                hours > 0 -> "⏳ ${hours}h ${mins}m remaining"
                mins > 0 -> "⏳ ${mins}m ${secs}s remaining"
                else -> "⏳ ${secs}s remaining"
            }
        }
    }

    val statusColor = when {
        isExpired && !hasDoctorReplied -> Color(0xFF4CAF50)
        isExpired -> Color(0xFFFF5722)
        remainingTime < 60 * 60 * 1000 -> Color(0xFFFF9800)
        else -> Color(0xFF2196F3)
    }

    val canOpen = !isExpired || hasDoctorReplied

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = canOpen) { onConversationClick(expiryTimeMs) },
        colors = CardDefaults.cardColors(
            containerColor = if (isExpired && !hasDoctorReplied)
                Color(0xFFE8F5E9) else MaterialTheme.colorScheme.onPrimary
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
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
                    text = displayName.split(" ")
                        .mapNotNull { it.firstOrNull()?.toString() }
                        .take(2)
                        .joinToString("")
                        .uppercase(),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = displayName, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = formatTime(timestampMillis),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = conversation.lastMessage.takeIf { it.isNotEmpty() } ?: "No messages yet",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = countdownText,
                    fontSize = 12.sp,
                    color = statusColor,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp)
                )
                if (userRole == "patient" && conversation.hospitalName.isNotEmpty()) {
                    Text(
                        text = conversation.hospitalName,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
            if (conversation.unreadCount > 0) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(Color.Red),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "${conversation.unreadCount}", fontSize = 10.sp, color = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
            }
            Icon(
                Icons.Filled.ChevronRight,
                "Open chat",
                tint = if (canOpen) MaterialTheme.colorScheme.onSurfaceVariant else Color(0xFFBDBDBD)
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
suspend fun checkIfDoctorReplied(conversationId: Int, doctorId: Int): Boolean {
    return try {
        getMessagesByConversation(conversationId).any { it.senderId == doctorId }
    } catch (e: Exception) {
        Log.w("DoctorReply", "Check failed", e)
        false
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun parseTimestampSafe(timestampStr: String): Long {
    val cleaned = timestampStr.replace(" ", "T")
    return try {
        OffsetDateTime.parse(cleaned).toInstant().toEpochMilli()
    } catch (e: Exception) {
        try {
            Instant.parse(cleaned).toEpochMilli()
        } catch (e2: Exception) {
            System.currentTimeMillis()
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun formatTime(timestamp: Long): String {
    val date = Date(timestamp)
    val cal = Calendar.getInstance().apply { time = date }
    val now = Calendar.getInstance()
    return when {
        isSameDay(cal, now) -> android.icu.text.SimpleDateFormat("HH:mm").format(date)
        isYesterday(cal, now) -> "Yesterday"
        else -> android.icu.text.SimpleDateFormat("dd/MM/yyyy").format(date)
    }
}

private fun isSameDay(c1: Calendar, c2: Calendar) =
    c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
            c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR)

private fun isYesterday(c1: Calendar, c2: Calendar) =
    isSameDay(c1, Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) })

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun PreviewChatListScreen() {
    colorTheme {
        ChatListScreen(1, "patient", { _, _ -> }, {}, {})
    }
}