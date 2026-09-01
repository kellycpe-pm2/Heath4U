package com.example.healt4u.screen.DoctorPatientChat

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healt4u.Storage.getPatientById
import com.example.healt4u.data.HospitalData.getDoctorById
import com.example.healt4u.model.Message
import com.example.healt4u.screen.componentUI.DateHeader
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeParseException

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
    onBack: () -> Unit,
    onSendMessage: (Message) -> Unit,
    onDeleteMessage: (Message) -> Unit,
    onAvatarClick: (Int) -> Unit,
    onClearAllMessages: () -> Unit,
    isMuted: Boolean = false,
    onMuteChanged: (Boolean) -> Unit = {}
) {
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

    val myName = remember(effectiveRole, doctorName, patientName) {
        if (effectiveRole == "doctor") doctorName else patientName
    }

    var messages by remember { mutableStateOf(initialMessages) }
    var textInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    var showMenu by remember { mutableStateOf(false) }
    var showClearAllDialog by remember { mutableStateOf(false) }
    var messageToDelete by remember { mutableStateOf<Message?>(null) }
    var mutedState by remember { mutableStateOf(isMuted) }

    val displayName = remember(effectiveRole, doctorName, patientName) {
        if (effectiveRole == "doctor") patientName else doctorName
    }

    val groupedMessages = remember(messages) {
        groupMessagesByDate(messages)
    }

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
                            text = chatName,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondary
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(Color(0xFF4CAF50), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Online",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.8f)
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
                        placeholder = { Text("Type here...") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.secondary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
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
                                userRole = effectiveRole,
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