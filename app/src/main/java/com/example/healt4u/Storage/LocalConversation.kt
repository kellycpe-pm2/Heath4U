package com.example.healt4u.Storage

import android.content.Context
import android.util.Log
import com.example.healt4u.model.Conversation
import com.example.healt4u.model.Message
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

private const val CONVERSATION_FILE = "conversations.json"
private const val MESSAGE_FILE = "messages.json"
private const val TAG = "ConversationStorage"

// Create Json instance with proper configuration
private val json = Json {
    prettyPrint = true
    ignoreUnknownKeys = true
    encodeDefaults = true
}

fun saveConversations(context: Context, conversations: List<Conversation>) {
    try {
        val jsonString = json.encodeToString(conversations)
        context.openFileOutput(CONVERSATION_FILE, Context.MODE_PRIVATE).use {
            it.write(jsonString.toByteArray())
        }
        Log.d(TAG, "Saved ${conversations.size} conversations")
    } catch (e: Exception) {
        Log.e(TAG, "Error saving conversations", e)
    }
}

fun loadConversations(context: Context): List<Conversation> {
    val file = File(context.filesDir, CONVERSATION_FILE)
    if (!file.exists()) {
        Log.d(TAG, "Conversation file does not exist")
        return emptyList()
    }
    return try {
        val result: List<Conversation> = json.decodeFromString(file.readText())
        Log.d(TAG, "Loaded ${result.size} conversations")
        result
    } catch (e: Exception) {
        Log.e(TAG, "Error loading conversations", e)
        emptyList()
    }
}

fun upsertConversation(context: Context, conversation: Conversation): Boolean {
    return try {
        val currentList = loadConversations(context).toMutableList()
        val index = currentList.indexOfFirst { it.id == conversation.id }
        if (index != -1) {
            currentList[index] = conversation
            Log.d(TAG, "Updated conversation: ${conversation.id}")
        } else {
            currentList.add(conversation)
            Log.d(TAG, "Added new conversation: ${conversation.id}")
        }
        saveConversations(context, currentList)
        true
    } catch (e: Exception) {
        Log.e(TAG, "Error upserting conversation", e)
        false
    }
}

fun getConversationById(context: Context, conversationId: String): Conversation? {
    return try {
        val conversations = loadConversations(context)
        conversations.find { it.id == conversationId }
    } catch (e: Exception) {
        Log.e(TAG, "Error getting conversation by ID", e)
        null
    }
}

fun getActiveConversations(context: Context): List<Conversation> {
    return try {
        val conversations = loadConversations(context)
        conversations.filter { it.isActive }
            .sortedByDescending { it.lastMessageTime }
    } catch (e: Exception) {
        Log.e(TAG, "Error getting active conversations", e)
        emptyList()
    }
}

fun getConversationsByPatient(context: Context, patientId: String): List<Conversation> {
    return try {
        val conversations = loadConversations(context)
        conversations.filter {
            it.isActive && it.patientId == patientId
        }.sortedByDescending { it.lastMessageTime }
    } catch (e: Exception) {
        Log.e(TAG, "Error getting conversations by patient", e)
        emptyList()
    }
}

fun getConversationsByDoctor(context: Context, doctorId: Int): List<Conversation> {
    return try {
        val conversations = loadConversations(context)
        conversations.filter {
            it.isActive && it.doctorId == doctorId
        }.sortedByDescending { it.lastMessageTime }
    } catch (e: Exception) {
        Log.e(TAG, "Error getting conversations by doctor", e)
        emptyList()
    }
}

fun updateConversationLastMessage(
    context: Context,
    conversationId: String,
    lastMessage: String,
    lastMessageTime: Long = System.currentTimeMillis(),
    incrementUnread: Int = 1
): Boolean {
    return try {
        val currentList = loadConversations(context).toMutableList()
        val index = currentList.indexOfFirst { it.id == conversationId }
        if (index != -1) {
            val conv = currentList[index]
            currentList[index] = conv.copy(
                lastMessage = lastMessage,
                lastMessageTime = lastMessageTime,
                unreadCount = conv.unreadCount + incrementUnread
            )
            saveConversations(context, currentList)
            Log.d(TAG, "Updated last message for: $conversationId")
            true
        } else {
            Log.d(TAG, "Conversation not found: $conversationId")
            false
        }
    } catch (e: Exception) {
        Log.e(TAG, "Error updating last message", e)
        false
    }
}

fun markConversationAsRead(context: Context, conversationId: String): Boolean {
    return try {
        val currentList = loadConversations(context).toMutableList()
        val index = currentList.indexOfFirst { it.id == conversationId }
        if (index != -1) {
            currentList[index] = currentList[index].copy(unreadCount = 0)
            saveConversations(context, currentList)
            Log.d(TAG, "Marked as read: $conversationId")
            true
        } else {
            false
        }
    } catch (e: Exception) {
        Log.e(TAG, "Error marking as read", e)
        false
    }
}

fun deleteConversation(context: Context, conversationId: String): Boolean {
    return try {
        val currentList = loadConversations(context).toMutableList()
        val removed = currentList.removeAll { it.id == conversationId }
        if (removed) {
            saveConversations(context, currentList)
            deleteMessagesByConversation(context, conversationId)
            Log.d(TAG, "Deleted conversation: $conversationId")
            true
        } else {
            false
        }
    } catch (e: Exception) {
        Log.e(TAG, "Error deleting conversation", e)
        false
    }
}

fun archiveConversation(context: Context, conversationId: String): Boolean {
    return try {
        val currentList = loadConversations(context).toMutableList()
        val index = currentList.indexOfFirst { it.id == conversationId }
        if (index != -1) {
            currentList[index] = currentList[index].copy(isActive = false)
            saveConversations(context, currentList)
            Log.d(TAG, "Archived conversation: $conversationId")
            true
        } else {
            false
        }
    } catch (e: Exception) {
        Log.e(TAG, "Error archiving conversation", e)
        false
    }
}

fun getConversationCount(context: Context): Int {
    return try {
        val conversations = loadConversations(context)
        conversations.count { it.isActive }
    } catch (e: Exception) {
        Log.e(TAG, "Error getting conversation count", e)
        0
    }
}

fun searchConversations(context: Context, query: String): List<Conversation> {
    return try {
        val conversations = loadConversations(context)
        conversations.filter {
            it.isActive && (
                    it.doctorName.contains(query, ignoreCase = true) ||
                            it.hospitalName.contains(query, ignoreCase = true) ||
                            it.lastMessage.contains(query, ignoreCase = true) ||
                            it.patientName.contains(query, ignoreCase = true)
                    )
        }.sortedByDescending { it.lastMessageTime }
    } catch (e: Exception) {
        Log.e(TAG, "Error searching conversations", e)
        emptyList()
    }
}

fun saveMessages(context: Context, messages: List<Message>) {
    try {
        val jsonString = json.encodeToString(messages)
        context.openFileOutput(MESSAGE_FILE, Context.MODE_PRIVATE).use {
            it.write(jsonString.toByteArray())
        }
        Log.d(TAG, "Saved ${messages.size} messages")
    } catch (e: Exception) {
        Log.e(TAG, "Error saving messages", e)
    }
}

fun loadMessages(context: Context): List<Message> {
    val file = File(context.filesDir, MESSAGE_FILE)
    if (!file.exists()) {
        Log.d(TAG, "Message file does not exist")
        return emptyList()
    }
    return try {
        val result: List<Message> = json.decodeFromString(file.readText())
        Log.d(TAG, "Loaded ${result.size} messages")
        result
    } catch (e: Exception) {
        Log.e(TAG, "Error loading messages", e)
        emptyList()
    }
}

fun sendMessage(context: Context, message: Message): Boolean {
    return try {
        val currentMessages = loadMessages(context).toMutableList()
        val msgWithId = if (message.id.isEmpty()) {
            message.copy(id = System.currentTimeMillis().toString())
        } else {
            message
        }
        currentMessages.add(msgWithId)
        saveMessages(context, currentMessages)
        Log.d(TAG, "Message sent: ${msgWithId.id}")

        updateConversationLastMessage(
            context,
            message.conversationId,
            message.content,
            message.timestamp,
            1
        )
        true
    } catch (e: Exception) {
        Log.e(TAG, "Error sending message", e)
        false
    }
}

fun getMessagesByConversation(context: Context, conversationId: String): List<Message> {
    return try {
        val messages = loadMessages(context)
        messages.filter { it.conversationId == conversationId }
            .sortedBy { it.timestamp }
    } catch (e: Exception) {
        Log.e(TAG, "Error getting messages by conversation", e)
        emptyList()
    }
}

fun getLastMessage(context: Context, conversationId: String): Message? {
    return try {
        val messages = loadMessages(context)
        messages.filter { it.conversationId == conversationId }
            .maxByOrNull { it.timestamp }
    } catch (e: Exception) {
        Log.e(TAG, "Error getting last message", e)
        null
    }
}

fun deleteMessagesByConversation(context: Context, conversationId: String): Boolean {
    return try {
        val currentMessages = loadMessages(context).toMutableList()
        val removed = currentMessages.removeAll { it.conversationId == conversationId }
        if (removed) {
            saveMessages(context, currentMessages)
            Log.d(TAG, "Deleted messages for: $conversationId")
        }
        true
    } catch (e: Exception) {
        Log.e(TAG, "Error deleting messages by conversation", e)
        false
    }
}

fun deleteMessage(context: Context, messageId: String): Boolean {
    return try {
        val currentMessages = loadMessages(context).toMutableList()
        val removed = currentMessages.removeAll { it.id == messageId }
        if (removed) {
            saveMessages(context, currentMessages)
            Log.d(TAG, "Deleted message: $messageId")
            true
        } else {
            false
        }
    } catch (e: Exception) {
        Log.e(TAG, "Error deleting message", e)
        false
    }
}

fun getMessageCount(context: Context, conversationId: String): Int {
    return try {
        val messages = loadMessages(context)
        messages.count { it.conversationId == conversationId }
    } catch (e: Exception) {
        Log.e(TAG, "Error getting message count", e)
        0
    }
}

fun searchMessages(context: Context, query: String): List<Message> {
    return try {
        val messages = loadMessages(context)
        messages.filter {
            it.content.contains(query, ignoreCase = true) ||
                    it.senderName.contains(query, ignoreCase = true)
        }.sortedByDescending { it.timestamp }
    } catch (e: Exception) {
        Log.e(TAG, "Error searching messages", e)
        emptyList()
    }
}

fun clearAllConversationData(context: Context): Boolean {
    return try {
        File(context.filesDir, CONVERSATION_FILE).delete()
        File(context.filesDir, MESSAGE_FILE).delete()
        Log.d(TAG, "All data cleared")
        true
    } catch (e: Exception) {
        Log.e(TAG, "Error clearing data", e)
        false
    }
}

fun insertSampleConversations(context: Context) {
    val now = System.currentTimeMillis()

    val sampleConversations = listOf(
        // ===== Penang General Hospital (Hospital ID: 1) =====
        Conversation(
            id = "1_p001",  // doctorId = 1, patientId = p001
            doctorId = 1,
            doctorName = "Dr. Ahmad Ismail",
            doctorSpecialty = "Cardiologist",
            hospitalId = 1,
            hospitalName = "Penang General Hospital",
            patientId = "p001",
            patientName = "John Doe",
            lastMessage = "Take your medication twice a day.",
            lastMessageTime = now - 3600000,
            unreadCount = 2,
            isActive = true
        ),
        Conversation(
            id = "2_p001",
            doctorId = 2,
            doctorName = "Dr. Sarah Tan",
            doctorSpecialty = "Neurologist",
            hospitalId = 1,
            hospitalName = "Penang General Hospital",
            patientId = "p001",
            patientName = "John Doe",
            lastMessage = "How are you feeling today?",
            lastMessageTime = now - 86400000,
            unreadCount = 0,
            isActive = true
        ),
        Conversation(
            id = "3_p001",
            doctorId = 3,
            doctorName = "Dr. Ravi Kumar",
            doctorSpecialty = "Orthopedic",
            hospitalId = 1,
            hospitalName = "Penang General Hospital",
            patientId = "p001",
            patientName = "John Doe",
            lastMessage = "Your X-ray results are normal.",
            lastMessageTime = now - 172800000,
            unreadCount = 1,
            isActive = true
        ),

        // ===== Hospital Bukit Mertajam (Hospital ID: 2) =====
        Conversation(
            id = "5_p001",
            doctorId = 5,
            doctorName = "Dr. Norashikin",
            doctorSpecialty = "Dermatologist",
            hospitalId = 2,
            hospitalName = "Hospital Bukit Mertajam",
            patientId = "p001",
            patientName = "John Doe",
            lastMessage = "Your skin rash is healing well.",
            lastMessageTime = now - 259200000,
            unreadCount = 0,
            isActive = true
        ),
        Conversation(
            id = "6_p001",
            doctorId = 6,
            doctorName = "Dr. Nik Mohd",
            doctorSpecialty = "Cardiologist",
            hospitalId = 2,
            hospitalName = "Hospital Bukit Mertajam",
            patientId = "p001",
            patientName = "John Doe",
            lastMessage = "Please schedule a follow-up appointment.",
            lastMessageTime = now - 345600000,
            unreadCount = 3,
            isActive = true
        ),

        // ===== Hospital Sultan Abdul Halim (Hospital ID: 3) =====
        Conversation(
            id = "10_p001",
            doctorId = 10,
            doctorName = "Dr. Khairul",
            doctorSpecialty = "Surgeon",
            hospitalId = 3,
            hospitalName = "Hospital Sultan Abdul Halim",
            patientId = "p001",
            patientName = "John Doe",
            lastMessage = "Surgery scheduled for next week.",
            lastMessageTime = now - 432000000,
            unreadCount = 1,
            isActive = true
        ),

        // ===== Hospital Taiping (Hospital ID: 4) =====
        Conversation(
            id = "13_p001",
            doctorId = 13,
            doctorName = "Dr. Farid",
            doctorSpecialty = "Cardiologist",
            hospitalId = 4,
            hospitalName = "Hospital Taiping",
            patientId = "p001",
            patientName = "John Doe",
            lastMessage = "Your heart rate is stable.",
            lastMessageTime = now - 518400000,
            unreadCount = 0,
            isActive = true
        ),

        // ===== Hospital Raja Permaisuri Bainun (Hospital ID: 5) =====
        Conversation(
            id = "16_p001",
            doctorId = 16,
            doctorName = "Dr. Rahim",
            doctorSpecialty = "Infectious Disease",
            hospitalId = 5,
            hospitalName = "Hospital Raja Permaisuri Bainun",
            patientId = "p001",
            patientName = "John Doe",
            lastMessage = "Your infection is clearing up.",
            lastMessageTime = now - 604800000,
            unreadCount = 0,
            isActive = true
        ),
        Conversation(
            id = "19_p001",
            doctorId = 19,
            doctorName = "Dr. Lim",
            doctorSpecialty = "Cardiologist",
            hospitalId = 5,
            hospitalName = "Hospital Raja Permaisuri Bainun",
            patientId = "p001",
            patientName = "John Doe",
            lastMessage = "Please take your medication regularly.",
            lastMessageTime = now - 691200000,
            unreadCount = 2,
            isActive = true
        )
    )

    sampleConversations.forEach { conv ->
        upsertConversation(context, conv)
    }

    Log.d(TAG, "Inserted ${sampleConversations.size} sample conversations")
}

fun insertSampleMessages(context: Context) {
    val now = System.currentTimeMillis()

    // ===== 对话 1: Dr. Ahmad Ismail (ID: 1_p001) =====
    val conversationId1 = "1_p001"
    val sampleMessages1 = listOf(
        Message(
            id = (now).toString(),
            conversationId = conversationId1,
            content = "Hello doctor, I have a question about my medication.",
            senderId = "p001",
            senderName = "John Doe",
            timestamp = now - 7200000,
            type = "text"
        ),
        Message(
            id = (now + 1).toString(),
            conversationId = conversationId1,
            content = "Of course, how can I help you?",
            senderId = "1",
            senderName = "Dr. Ahmad Ismail",
            timestamp = now - 5400000,
            type = "text"
        ),
        Message(
            id = (now + 2).toString(),
            conversationId = conversationId1,
            content = "I've been having some chest pain.",
            senderId = "p001",
            senderName = "John Doe",
            timestamp = now - 1800000,
            type = "text"
        ),
        Message(
            id = (now + 3).toString(),
            conversationId = conversationId1,
            content = "Take your medication twice a day and rest.",
            senderId = "1",
            senderName = "Dr. Ahmad Ismail",
            timestamp = now - 600000,
            type = "text"
        )
    )

    // ✅ 发送对话 1 的消息
    sampleMessages1.forEach { msg ->
        sendMessage(context, msg)
    }

    // ===== 对话 2: Dr. Sarah Tan (ID: 2_p001) =====
    val conversationId2 = "2_p001"
    val sampleMessages2 = listOf(
        Message(
            id = (now + 10).toString(),
            conversationId = conversationId2,
            content = "How are you feeling today?",
            senderId = "2",
            senderName = "Dr. Sarah Tan",
            timestamp = now - 86400000,
            type = "text"
        ),
        Message(
            id = (now + 11).toString(),
            conversationId = conversationId2,
            content = "I'm feeling much better, thank you!",
            senderId = "p001",
            senderName = "John Doe",
            timestamp = now - 82800000,
            type = "text"
        )
    )

    // ✅ 发送对话 2 的消息
    sampleMessages2.forEach { msg ->
        sendMessage(context, msg)
    }

    // ===== 对话 3: Dr. Ravi Kumar (ID: 3_p001) =====
    val conversationId3 = "3_p001"
    val sampleMessages3 = listOf(
        Message(
            id = (now + 20).toString(),
            conversationId = conversationId3,
            content = "Your X-ray results are normal.",
            senderId = "3",
            senderName = "Dr. Ravi Kumar",
            timestamp = now - 172800000,
            type = "text"
        ),
        Message(
            id = (now + 21).toString(),
            conversationId = conversationId3,
            content = "That's great news! Thank you doctor.",
            senderId = "p001",
            senderName = "John Doe",
            timestamp = now - 169200000,
            type = "text"
        )
    )

    // ✅ 发送对话 3 的消息
    sampleMessages3.forEach { msg ->
        sendMessage(context, msg)
    }

    // ===== 对话 6: Dr. Nik Mohd (ID: 6_p001) =====
    val conversationId6 = "6_p001"
    val sampleMessages6 = listOf(
        Message(
            id = (now + 30).toString(),
            conversationId = conversationId6,
            content = "Please schedule a follow-up appointment.",
            senderId = "6",
            senderName = "Dr. Nik Mohd",
            timestamp = now - 345600000,
            type = "text"
        ),
        Message(
            id = (now + 31).toString(),
            conversationId = conversationId6,
            content = "I'll call the clinic tomorrow.",
            senderId = "p001",
            senderName = "John Doe",
            timestamp = now - 342000000,
            type = "text"
        )
    )

    // ✅ 发送对话 6 的消息
    sampleMessages6.forEach { msg ->
        sendMessage(context, msg)
    }

    // ✅ 计算总数并打印日志（在循环外面！）
    val totalMessages = sampleMessages1.size + sampleMessages2.size +
            sampleMessages3.size + sampleMessages6.size
    Log.d(TAG, "Inserted $totalMessages sample messages")
}