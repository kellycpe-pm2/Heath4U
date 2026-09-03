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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.healt4u.Storage.getDoctorById
import com.example.healt4u.Storage.getPatientById
import com.example.healt4u.ViewModel.ConversationViewModel
import com.example.healt4u.model.Conversation
import com.example.healt4u.screen.componentUI.Theme.colorTheme
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.OffsetDateTime
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    userId: Int,
    userRole: String,
    onConversationClick: (Conversation, String) -> Unit,
    onNewChatClick: () -> Unit,
    onBack: () -> Unit,
    viewModel: ConversationViewModel = viewModel()
) {
    LaunchedEffect(userId, userRole) {
        if (userRole == "doctor") {
            viewModel.loadConversationsForDoctor(userId)
        } else {
            viewModel.loadConversations(userId)
        }
    }

    val conversations by viewModel.conversations.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()

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
            if (userRole=="patient"){
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
                            if (userRole == "doctor") {
                                viewModel.loadConversationsForDoctor(userId)
                            } else {
                                viewModel.loadConversations(userId)
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
                            key = { it.id ?: 0 }
                        ) { conversation ->
                            ConversationItem(
                                userId = userId,
                                userRole = userRole,
                                conversation = conversation,
                                onClick = { chatName ->
                                    onConversationClick(conversation, chatName)  // ✅ Pass loaded name directly
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
    onClick: (String) -> Unit
) {
    val doctorId = conversation.doctorId
    val patientId = conversation.patientId

    var displayName by remember { mutableStateOf("Loading...") }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(doctorId, patientId, userId, userRole) {
        coroutineScope.launch {
            try {
                // ✅ userRole FIRST — no ID overlap confusion!
                displayName = if (userRole == "patient") {
                    getDoctorById(doctorId)?.name ?: "Doctor"
                } else {
                    getPatientById(patientId)?.name ?: "Patient"
                }
                Log.d("ChatName", "userRole=$userRole → name=$displayName")
            } catch (e: Exception) {
                Log.e("ChatName", "Failed to load name", e)
                displayName = if (userRole == "patient") "Doctor" else "Patient"
            }
        }
    }

    val timestampMillis = remember(conversation.lastMessageTime) {
        parseTimestampSafe(conversation.lastMessageTime)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(displayName) },  // ✅ Pass loaded name to chat
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

                if (userRole == "patient" && conversation.hospitalName.isNotEmpty()) {
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
                Spacer(modifier = Modifier.width(8.dp))
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
private fun parseTimestampSafe(timestampStr: String): Long {
    val cleaned = timestampStr.replace(" ", "T")
    return try {
        OffsetDateTime.parse(cleaned).toInstant().toEpochMilli()
    } catch (e: Exception) {
        try {
            Instant.parse(cleaned).toEpochMilli()
        } catch (e2: Exception) {
            Log.w("DateParse", "Failed to parse time: $timestampStr", e2)
            System.currentTimeMillis()
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
            onConversationClick = { _, _ -> },
            onNewChatClick = {},
            onBack = {}
        )
    }
}