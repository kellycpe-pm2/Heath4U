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
import androidx.compose.material.icons.filled.Chat
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
import com.example.healt4u.Storage.getPatientById
import com.example.healt4u.data.HospitalData.getDoctorById
import com.example.healt4u.model.Conversation
import com.example.healt4u.screen.componentUI.Theme.colorTheme
import com.example.healt4u.screen.formatTimeString
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    userId: Int,
    userRole: String,
    onConversationClick: (Conversation) -> Unit,
    onNewChatClick: () -> Unit,
    onBack: () -> Unit
) {
    var conversations by remember { mutableStateOf<List<Conversation>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()
    var reloadKey by remember { mutableStateOf(0) }

    LaunchedEffect(userId, userRole,reloadKey) {
        try {
            isLoading = true
            errorMessage = null

            val result = if (userRole == "doctor") {
                getConversationsByDoctor(userId.toString())
            } else {
                getConversationsByPatient(userId.toString())
            }

            conversations = result
        } catch (e: Exception) {
            errorMessage = e.message ?: "Failed to load conversations"
        } finally {
            isLoading = false
        }
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
                Icon(
                    Icons.Filled.Add,
                    contentDescription = "New Chat"
                )
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
                        Button(
                            onClick = {
                                reloadKey++
                            }
                        ) {
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
                            key = { it.id }
                        ) { conversation ->
                            ConversationItem(
                                userId = userId,
                                userRole = userRole,
                                conversation = conversation,
                                onClick = { onConversationClick(conversation) }
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
    onClick: () -> Unit
) {
    val ids = conversation.id.split("_")
    val doctorId = ids.getOrNull(0)?.toIntOrNull() ?: 0
    val patientId = ids.getOrNull(1)?.toIntOrNull() ?: 0

    var doctorName by remember { mutableStateOf("Doctor") }
    var patientName by remember { mutableStateOf("Patient") }

    LaunchedEffect(doctorId, patientId) {
        doctorName = getDoctorById(doctorId)?.name ?: "Doctor"
        patientName = getPatientById(patientId)?.name ?: "Patient"
    }

    val effectiveRole = when {
        userRole == "doctor" || userId == doctorId -> "doctor"
        userRole == "patient" || userId == patientId -> "patient"
        else -> "patient"
    }

    val displayName = remember(effectiveRole, doctorName, patientName) {
        if (effectiveRole == "doctor") patientName else doctorName
    }

    val timestampMillis = try {
        val str = conversation.lastMessageTime
        when {
            str.all { it.isDigit() } -> str.toLong()
            else -> {
                java.time.OffsetDateTime.parse(str)
                    .toInstant()
                    .toEpochMilli()
            }
        }
    } catch (e: Exception) {
        Log.w("DateParse", "Failed to parse time", e)
        System.currentTimeMillis()
    }

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
                    text = displayName
                        .split(" ")
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
                    Text(
                        text = displayName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
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

                if (userRole=="patient") {
                    Text(
                        text = conversation.hospitalName,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
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

@RequiresApi(Build.VERSION_CODES.O)
private fun formatTime(timestamp: Long): String {
    return try {
        val date = Date(timestamp)
        val now = Calendar.getInstance()
        val cal = Calendar.getInstance().apply { time = date }

        when {
            isSameDay(cal, now) -> {
                val format = android.icu.text.SimpleDateFormat("HH:mm", Locale.getDefault())
                format.format(date)
            }
            isYesterday(cal, now) -> "Yesterday"
            else -> {
                val format = android.icu.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                format.format(date)
            }
        }
    } catch (e: Exception) {
        "??:??"
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

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun PreviewChatListScreen() {
    colorTheme {
        ChatListScreen(
            userId = 1,
            userRole = "patient",
            onConversationClick = {},
            onNewChatClick = {},
            onBack = {}
        )
    }
}