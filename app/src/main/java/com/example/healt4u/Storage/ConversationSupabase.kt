package com.example.healt4u.Storage

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.healt4u.model.Conversation
import com.example.healt4u.model.Message
import com.example.healt4u.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonPrimitive

private const val TAG = "SupabaseStorage"

@RequiresApi(Build.VERSION_CODES.O)
suspend fun getConversationsByPatient(patientId: String): List<Conversation> {
    return try {
        Log.d("SupabaseStorage", "patientId: $patientId")

        val id = patientId.toIntOrNull()
        if (id == null) {
            Log.e("SupabaseStorage", "patientId is not a valid Int: $patientId")
            return emptyList()
        }

        Log.d("SupabaseStorage", "id as Int: $id")

        val response = SupabaseClient.supabase
            .from("conversations")
            .select {
                filter { eq("patient_id", id) }
                order("last_message_time", Order.DESCENDING)
            }

        val rawList = Json.decodeFromString<List<Map<String, JsonElement>>>(response.data)

        Log.d("SupabaseStorage", "Raw items count: ${rawList.size}")
        if (rawList.isNotEmpty()) {
            Log.d("SupabaseStorage", "First item doctor_id: ${rawList.first()["doctor_id"]}")
        }

        val conversations = rawList.map { map ->
            Conversation(
                id = map["id"]?.jsonPrimitive?.content ?: "",
                doctorId = map["doctor_id"]?.jsonPrimitive?.int ?: 0,
                doctorName = map["doctor_name"]?.jsonPrimitive?.content ?: "",
                patientId = map["patient_id"]?.jsonPrimitive?.int ?: 0,
                patientName = map["patient_name"]?.jsonPrimitive?.content ?: "",
                hospitalId = map["hospital_id"]?.jsonPrimitive?.content ?: "",
                hospitalName = map["hospital_name"]?.jsonPrimitive?.content ?: "",
                lastMessage = map["last_message"]?.jsonPrimitive?.content ?: "",
                lastMessageTime = map["last_message_time"]?.jsonPrimitive?.content
                    ?: java.time.Instant.now().toString(),
                unreadCount = map["unread_count"]?.jsonPrimitive?.int ?: 0,
                isActive = map["is_active"]?.jsonPrimitive?.boolean ?: true
            )
        }

        Log.d("SupabaseStorage", "Returning ${conversations.size} conversations")

        val syncedConversations = conversations.map { conv ->
            val latestMsg = getLastMessage(conv.id)
            if (latestMsg != null) {
                conv.copy(lastMessage = latestMsg.content)
            } else {
                conv
            }
        }

        Log.d("SupabaseStorage", "Returning ${syncedConversations.size} conversations")
        return syncedConversations

    } catch (e: Exception) {
        Log.e("SupabaseStorage", "Error: ${e.message}", e)
        emptyList()
    }
}

@RequiresApi(Build.VERSION_CODES.O)
suspend fun getConversationsByDoctor(doctorId: String): List<Conversation> {
    return try {
        val id = doctorId.toIntOrNull() ?: return emptyList()

        val response = SupabaseClient.supabase
            .from("conversations")
            .select {
                filter { eq("doctor_id", id) }
                order("last_message_time", Order.DESCENDING)
            }

        val rawList = Json.decodeFromString<List<Map<String, JsonElement>>>(response.data)
        val conversations = rawList.map { map ->
            Conversation(
                id = map["id"]?.jsonPrimitive?.content ?: "",
                doctorId = map["doctor_id"]?.jsonPrimitive?.int ?: 0,
                doctorName = map["doctor_name"]?.jsonPrimitive?.content ?: "",
                patientId = map["patient_id"]?.jsonPrimitive?.int ?: 0,
                patientName = map["patient_name"]?.jsonPrimitive?.content ?: "",
                hospitalId = map["hospital_id"]?.jsonPrimitive?.content ?: "",
                hospitalName = map["hospital_name"]?.jsonPrimitive?.content ?: "",
                lastMessage = map["last_message"]?.jsonPrimitive?.content ?: "",
                lastMessageTime = map["last_message_time"]?.jsonPrimitive?.content
                    ?: java.time.Instant.now().toString(),
                unreadCount = map["unread_count"]?.jsonPrimitive?.int ?: 0,
                isActive = map["is_active"]?.jsonPrimitive?.boolean ?: true
            )
        }

        Log.d(TAG, "Loaded ${conversations.size} conversations")
        val syncedConversations = conversations.map { conv ->
            val latestMsg = getLastMessage(conv.id)
            if (latestMsg != null) {
                conv.copy(lastMessage = latestMsg.content)
            } else {
                conv
            }
        }

        Log.d(TAG, "Returning ${syncedConversations.size} conversations")
        return syncedConversations

    } catch (e: Exception) {
        Log.e(TAG, "Error loading conversations: ${e.message}", e)
        emptyList()
    }
}

@RequiresApi(Build.VERSION_CODES.O)
suspend fun getConversationById(conversationId: String): Conversation? {
    return try {
        val response = SupabaseClient.supabase
            .from("conversations")
            .select {
                filter { eq("id", conversationId) }
            }

        val rawList = Json.decodeFromString<List<Map<String, JsonElement>>>(response.data)
        if (rawList.isEmpty()) return null

        val map = rawList.first()
        Conversation(
            id = map["id"]?.jsonPrimitive?.content ?: "",
            doctorId = map["doctor_id"]?.jsonPrimitive?.int ?: 0,
            doctorName = map["doctor_name"]?.jsonPrimitive?.content ?: "",
            patientId = map["patient_id"]?.jsonPrimitive?.int ?: 0,
            patientName = map["patient_name"]?.jsonPrimitive?.content ?: "",
            hospitalId = map["hospital_id"]?.jsonPrimitive?.content ?: "",
            hospitalName = map["hospital_name"]?.jsonPrimitive?.content ?: "",
            lastMessage = map["last_message"]?.jsonPrimitive?.content ?: "",
            lastMessageTime = map["last_message_time"]?.jsonPrimitive?.content
                ?: java.time.Instant.now().toString(),
            unreadCount = map["unread_count"]?.jsonPrimitive?.int ?: 0,
            isActive = map["is_active"]?.jsonPrimitive?.boolean ?: true
        )
    } catch (e: Exception) {
        Log.e(TAG, "Error getting conversation: ${e.message}", e)
        null
    }
}

suspend fun upsertConversation(conversation: Conversation): Boolean {
    return try {
        SupabaseClient.supabase
            .from("conversations")
            .upsert(conversation)
        Log.d(TAG, "Conversation upserted: ${conversation.id}")
        true
    } catch (e: Exception) {
        Log.e(TAG, "Error upserting conversation: ${e.message}", e)
        false
    }
}

@RequiresApi(Build.VERSION_CODES.O)
suspend fun updateConversationLastMessage(
    conversationId: String,
    lastMessage: String,
    lastMessageTime: String,
    incrementUnread: Int = 1
): Boolean {
    return try {
        val conversation = getConversationById(conversationId)
        if (conversation == null) {
            Log.e(TAG, "Conversation not found: $conversationId")
            return false
        }

        SupabaseClient.supabase
            .from("conversations")
            .update(
                mapOf(
                    "last_message" to lastMessage,
                    "last_message_time" to lastMessageTime,
                    "unread_count" to (conversation.unreadCount + incrementUnread)
                )
            ) {
                filter { eq("id", conversationId) }
            }
        Log.d(TAG, "Updated last message for: $conversationId")
        true
    } catch (e: Exception) {
        Log.e(TAG, "Error updating last message: ${e.message}", e)
        false
    }
}

suspend fun markConversationAsRead(conversationId: String): Boolean {
    return try {
        SupabaseClient.supabase
            .from("conversations")
            .update(
                mapOf("unread_count" to 0)
            ) {
                filter { eq("id", conversationId) }
            }
        Log.d(TAG, "Marked as read: $conversationId")
        true
    } catch (e: Exception) {
        Log.e(TAG, "Error marking as read: ${e.message}", e)
        false
    }
}

suspend fun deleteConversation(conversationId: String): Boolean {
    return try {
        SupabaseClient.supabase
            .from("conversations")
            .update(
                mapOf("is_active" to false)
            ) {
                filter { eq("id", conversationId) }
            }
        Log.d(TAG, "Deleted conversation: $conversationId")
        true
    } catch (e: Exception) {
        Log.e(TAG, "Error deleting conversation: ${e.message}", e)
        false
    }
}

@RequiresApi(Build.VERSION_CODES.O)
suspend fun createConversation(
    doctorId: Int,
    patientId: Int,
    doctorName: String,
    patientName: String,
    hospitalId: Int,
    hospitalName: String
): String {
    val conversationId = "${doctorId}_${patientId}"
    val existing = getConversationById(conversationId)

    if (existing == null) {
        val conversation = Conversation(
            id = conversationId,
            doctorId = doctorId,
            doctorName = doctorName,
            patientId = patientId,
            patientName = patientName,
            hospitalId = hospitalId.toString(),
            hospitalName = hospitalName,
            lastMessage = "Start your conversation!",
            lastMessageTime = System.currentTimeMillis().let {
                java.time.Instant.ofEpochMilli(it).toString()
            },
            unreadCount = 0,
            isActive = true
        )
        upsertConversation(conversation)
        Log.d(TAG, "Created conversation: $conversationId")
    }

    return conversationId
}

@RequiresApi(Build.VERSION_CODES.O)
suspend fun sendMessage(message: Message): Boolean {
    return try {
        SupabaseClient.supabase
            .from("messages")
            .insert(message)

        updateConversationLastMessage(
            message.conversationId,
            message.content,
            message.timestamp,
            1
        )
        true
    } catch (e: Exception) {
        Log.e("TAG", "Error: ${e.message}", e)
        false
    }
}

@RequiresApi(Build.VERSION_CODES.O)
suspend fun getMessagesByConversation(conversationId: String): List<Message> {
    return try {
        Log.d(TAG, "Searching messages with conversationId: '$conversationId'")

        val response = SupabaseClient.supabase
            .from("messages")
            .select {
                filter { eq("conversation_id", conversationId) }
                order("timestamp", Order.ASCENDING)
            }

        val rawList = Json.decodeFromString<List<Map<String, JsonElement>>>(response.data)
        Log.d(TAG, "Raw messages found: ${rawList.size}")

        val messages = rawList.map { map ->
            val timestampStr = map["timestamp"]?.jsonPrimitive?.content ?: ""

            Message(
                id = map["id"]?.jsonPrimitive?.content?.toLongOrNull() ?: 0L,
                conversationId = map["conversation_id"]?.jsonPrimitive?.content ?: "",
                content = map["content"]?.jsonPrimitive?.content ?: "",
                senderId = map["sender_id"]?.jsonPrimitive?.content?.toIntOrNull() ?: 0,
                senderName = map["sender_name"]?.jsonPrimitive?.content ?: "",
                timestamp = timestampStr,
                type = map["type"]?.jsonPrimitive?.content ?: "text"
            )
        }

        Log.d(TAG, "Loaded ${messages.size} messages!")
        return messages

    } catch (e: Exception) {
        Log.e(TAG, "Error: ${e.message}", e)
        emptyList()
    }
}

@RequiresApi(Build.VERSION_CODES.O)
suspend fun getLastMessage(conversationId: String): Message? {
    return try {
        Log.d(TAG, "Getting last message for: '$conversationId'")

        val response = SupabaseClient.supabase
            .from("messages")
            .select {
                filter { eq("conversation_id", conversationId) }
            }

        val rawList = Json.decodeFromString<List<Map<String, JsonElement>>>(response.data)
        Log.d(TAG, "Total messages found: ${rawList.size}")

        if (rawList.isEmpty()) return null

        var newestMap: Map<String, JsonElement>? = null
        var newestTime = -1L

        for (map in rawList) {
            val timestampStr = map["timestamp"]?.jsonPrimitive?.content
            val time = try {
                timestampStr?.let { java.time.Instant.parse(it).toEpochMilli() } ?: 0L
            } catch (e: Exception) {
                0L
            }

            val content = map["content"]?.jsonPrimitive?.content ?: ""
            Log.d(TAG, "Message -> time=$time | $content")

            if (time > newestTime) {
                newestTime = time
                newestMap = map
            }
        }

        if (newestMap == null) return null

        val timestampStr = newestMap["timestamp"]?.jsonPrimitive?.content
            ?: java.time.Instant.now().toString()

        val message = Message(
            id = newestMap["id"]?.jsonPrimitive?.content?.toLongOrNull() ?: 0L,
            conversationId = newestMap["conversation_id"]?.jsonPrimitive?.content ?: "",
            content = newestMap["content"]?.jsonPrimitive?.content ?: "",
            senderId = newestMap["sender_id"]?.jsonPrimitive?.content?.toIntOrNull() ?: 0,
            senderName = newestMap["sender_name"]?.jsonPrimitive?.content ?: "",
            timestamp = timestampStr,
            type = newestMap["type"]?.jsonPrimitive?.content ?: "text"
        )

        Log.d(TAG, "Newest last message: ${message.content}")
        return message

    } catch (e: Exception) {
        Log.e(TAG, "Error getting last message: ${e.message}", e)
        null
    }
}

suspend fun deleteMessage(messageId: String): Boolean {
    return try {
        SupabaseClient.supabase
            .from("messages")
            .delete {
                filter { eq("id", messageId) }
            }
        Log.d(TAG, "Deleted message: $messageId")
        true
    } catch (e: Exception) {
        Log.e(TAG, "Error deleting message: ${e.message}", e)
        false
    }
}
