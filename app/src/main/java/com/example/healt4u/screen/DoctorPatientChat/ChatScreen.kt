package com.example.healt4u.screen.DoctorPatientChat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healt4u.model.Message
import com.example.healt4u.screen.componentUI.DateHeader
import java.lang.System.currentTimeMillis
import java.util.Calendar
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    chatName: String,
    userId: String,
    initialMessages: List<Message>,
    onBack: () -> Unit,
    onSendMessage: (Message) -> Unit
){
    var messages by remember { mutableStateOf(initialMessages) }
    var textInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    val groupedMessages = remember(messages){
        groupMessagesByDate(messages)
    }

    //auto scroll to the bottom when received new message
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()){
            listState.animateScrollToItem(messages.size - 1)
        }
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
                            if (textInput.isNotBlank()){
                                val newMessage = Message(
                                    id = currentTimeMillis().toString(),
                                    content = textInput,
                                    senderId = userId,
                                    senderName = if (userId.contains("doctor")) "Doctor" else "Patient",
                                    timestamp = currentTimeMillis(),
                                    type = "text"
                                )
                                messages = messages + newMessage
                                onSendMessage(newMessage)
                                textInput = ""
                            }
                        },
                        containerColor = if (textInput.isNotEmpty()){
                            MaterialTheme.colorScheme.secondary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        contentColor = if (textInput.isNotEmpty()){
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                            DateHeader(date = group.date)
                        }
                        items(
                            items = group.messages,
                            key = { it.id }
                        ) { message ->
                            val isFromCurrentUser = message.senderId == userId

                            MessageBubble(
                                message = message,
                                isFromCurrentUser = isFromCurrentUser
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun groupMessagesByDate(messages: List<Message>): List<MessageGroup> {
    val groups = mutableListOf<MessageGroup>()

    if (messages.isEmpty()) return groups

    val sorted = messages.sortedBy { it.timestamp }
    var currentDate = getDateKey(sorted.first().timestamp)
    var currentMessages = mutableListOf<Message>()

    for (message in sorted) {
        val messageDate = getDateKey(message.timestamp)
        if (messageDate != currentDate) {
            groups.add(MessageGroup(date = currentDate, messages = currentMessages))
            currentDate = messageDate
            currentMessages = mutableListOf()
        }
        currentMessages.add(message)
    }

    if (currentMessages.isNotEmpty()) {
        groups.add(MessageGroup(date = currentDate, messages = currentMessages))
    }

    return groups
}

private fun getDateKey(timestamp: Long): Long {
    val cal = Calendar.getInstance().apply { time = Date(timestamp) }
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal.timeInMillis
}

private data class MessageGroup(
    val date: Long,
    val messages: List<Message>
)

@Preview(showBackground = true)
@Composable
fun PreviewChatScreen() {
    com.example.healt4u.screen.componentUI.Theme.colorTheme {
        ChatScreen(
            chatName = "Dr. Smith",
            userId = "patient_001",
            initialMessages = listOf(
                Message(
                    id = "1",
                    content = "Hello! How are you?",
                    senderId = "patient_001",
                    senderName = "Patient",
                    timestamp = currentTimeMillis() - 120000,
                    type = "text"
                ),
                Message(
                    id = "2",
                    content = "I'm fine, thank you! How can I help?",
                    senderId = "doctor_001",
                    senderName = "Dr. Smith",
                    timestamp = currentTimeMillis() - 60000,
                    type = "text"
                ),
                Message(
                    id = "3",
                    content = "I've been having some headaches lately.",
                    senderId = "patient_001",
                    senderName = "Patient",
                    timestamp = currentTimeMillis() - 30000,
                    type = "text"
                ),
                Message(
                    id = "4",
                    content = "I see. Where exactly is the pain located?",
                    senderId = "doctor_001",
                    senderName = "Dr. Smith",
                    timestamp = currentTimeMillis() - 10000,
                    type = "text"
                )
            ),
            onBack = { /* Handle back navigation */ },
            onSendMessage = { message ->
                println("Sending: $message")
            }
        )
    }
}