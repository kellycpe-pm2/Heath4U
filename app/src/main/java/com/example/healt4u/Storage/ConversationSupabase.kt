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
import java.time.Instant

private const val TAG = "SupabaseStorage"

@RequiresApi(Build.VERSION_CODES.O)
private fun parseTimestampToEpochMs(timestampStr: String): Long {
    return try {
        java.time.OffsetDateTime.parse(timestampStr).toInstant().toEpochMilli()
    } catch (e: Exception) {
        try {
            Instant.parse(timestampStr).toEpochMilli()
        } catch (e2: Exception) {
            System.currentTimeMillis()
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
suspend fun getConversationsByPatient(patientId: Int): List<Conversation> {
    return try {
        Log.d("SupabaseStorage", "patientId: $patientId")

        val response = SupabaseClient.supabase
            .from("conversation")
            .select {
                filter { eq("patient_id", patientId) }
            }

        val rawList = Json.decodeFromString<List<Map<String, JsonElement>>>(response.data)

        Log.d("SupabaseStorage", "Raw items count: ${rawList.size}")
        if (rawList.isNotEmpty()) {
            Log.d("SupabaseStorage", "First item id: ${rawList.first()["id"]}")
        }

        val conversations = rawList.map { map ->
            Conversation(
                id = map["id"]?.jsonPrimitive?.int,
                doctorId = map["doctor_id"]?.jsonPrimitive?.int ?: 0,
                doctorName = map["doctor_name"]?.jsonPrimitive?.content ?: "",
                patientId = map["patient_id"]?.jsonPrimitive?.int ?: 0,
                patientName = map["patient_name"]?.jsonPrimitive?.content ?: "",
                hospitalId = map["hospital_id"]?.jsonPrimitive?.int ?: 0,
                hospitalName = map["hospital_name"]?.jsonPrimitive?.content ?: "General Hospital",
                lastMessage = map["last_message"]?.jsonPrimitive?.content ?: "",
                lastMessageTime = map["last_message_time"]?.jsonPrimitive?.content
                    ?: Instant.now().toString(),
                unreadCount = map["unread_count"]?.jsonPrimitive?.int ?: 0,
                isActive = map["is_active"]?.jsonPrimitive?.boolean ?: true
            )
        }

        Log.d("SupabaseStorage", "Returning ${conversations.size} conversations")

        return conversations

    } catch (e: Exception) {
        Log.e("SupabaseStorage", "Error: ${e.message}", e)
        emptyList()
    }
}

@RequiresApi(Build.VERSION_CODES.O)
suspend fun getConversationsByDoctor(doctorId: Int): List<Conversation> {
    return try {
        val response = SupabaseClient.supabase
            .from("conversation")
            .select {
                filter { eq("doctor_id", doctorId) }
            }

        val rawList = Json.decodeFromString<List<Map<String, JsonElement>>>(response.data)

        val conversations = rawList.map { map ->
            Conversation(
                id = map["id"]?.jsonPrimitive?.int,
                doctorId = map["doctor_id"]?.jsonPrimitive?.int ?: 0,
                doctorName = map["doctor_name"]?.jsonPrimitive?.content ?: "",
                patientId = map["patient_id"]?.jsonPrimitive?.int ?: 0,
                patientName = map["patient_name"]?.jsonPrimitive?.content ?: "",
                hospitalId = map["hospital_id"]?.jsonPrimitive?.int ?: 0,
                hospitalName = map["hospital_name"]?.jsonPrimitive?.content ?: "General Hospital",
                lastMessage = map["last_message"]?.jsonPrimitive?.content ?: "",
                lastMessageTime = map["last_message_time"]?.jsonPrimitive?.content
                    ?: Instant.now().toString(),
                unreadCount = map["unread_count"]?.jsonPrimitive?.int ?: 0,
                isActive = map["is_active"]?.jsonPrimitive?.boolean ?: true
            )
        }

        Log.d(TAG, "Loaded ${conversations.size} conversations")

        return conversations

    } catch (e: Exception) {
        Log.e(TAG, "Error loading conversations: ${e.message}", e)
        emptyList()
    }
}

@RequiresApi(Build.VERSION_CODES.O)
suspend fun getConversationById(conversationId: Int): Conversation? {
    return try {
        val response = SupabaseClient.supabase
            .from("conversation")
            .select {
                filter { eq("id", conversationId) }
            }

        val rawList = Json.decodeFromString<List<Map<String, JsonElement>>>(response.data)
        if (rawList.isEmpty()) return null

        val map = rawList.first()
        Conversation(
            id = map["id"]?.jsonPrimitive?.int,
            doctorId = map["doctor_id"]?.jsonPrimitive?.int ?: 0,
            doctorName = map["doctor_name"]?.jsonPrimitive?.content ?: "",
            patientId = map["patient_id"]?.jsonPrimitive?.int ?: 0,
            patientName = map["patient_name"]?.jsonPrimitive?.content ?: "",
            hospitalId = map["hospital_id"]?.jsonPrimitive?.int ?: 0,
            hospitalName = map["hospital_name"]?.jsonPrimitive?.content ?: "General Hospital",
            lastMessage = map["last_message"]?.jsonPrimitive?.content ?: "",
            lastMessageTime = map["last_message_time"]?.jsonPrimitive?.content
                ?: Instant.now().toString(),
            unreadCount = map["unread_count"]?.jsonPrimitive?.int ?: 0,
            isActive = map["is_active"]?.jsonPrimitive?.boolean ?: true
        )
    } catch (e: Exception) {
        Log.e(TAG, "Error getting conversation: ${e.message}", e)
        null
    }
}

@RequiresApi(Build.VERSION_CODES.O)
suspend fun upsertConversation(conversation: Conversation): Conversation? {
    return try {
        val savedList = SupabaseClient.supabase
            .from("conversation")
            .upsert(conversation) {
                select()
            }
            .decodeList<Map<String, JsonElement>>()

        if (savedList.isEmpty()) {
            Log.e(TAG, "No row returned from upsert!")
            return null
        }

        val row = savedList.first()
        val realId = row["id"]?.jsonPrimitive?.int

        Log.d(TAG, "✅ UPSERT RETURNED ID=$realId")

        if (realId == null || realId == 0) {
            Log.w(TAG, "ID still null/0 → re-querying by doctor+patient...")
            val found = getConversationByDoctorPatient(conversation.doctorId, conversation.patientId)
            if (found != null && found.id != null && found.id != 0) {
                Log.d(TAG, "✅ FOUND REAL ID: ${found.id}")
                return found
            }
        }

        return conversation.copy(
            id = realId,
            doctorName = row["doctor_name"]?.jsonPrimitive?.content ?: conversation.doctorName,
            patientName = row["patient_name"]?.jsonPrimitive?.content ?: conversation.patientName,
            hospitalName = row["hospital_name"]?.jsonPrimitive?.content ?: conversation.hospitalName
        )
    } catch (e: Exception) {
        Log.e(TAG, "Error upserting conversation: ${e.message}", e)
        null
    }
}

@RequiresApi(Build.VERSION_CODES.O)
suspend fun updateConversationLastMessage(convId: Int, lastMsg: String, msgTime: String) {
    try {
        SupabaseClient.supabase
            .from("conversation")
            .update({
                set("last_message", lastMsg)
                set("last_message_time", msgTime)
            }) {
                filter { eq("id", convId) }
            }
        Log.d("SupabaseStorage", "✅ Conversation updated: id=$convId")
    } catch (e: Exception) {
        Log.w("SupabaseStorage", "⚠️ Updated last_message skipped: ${e.message}")
    }
}

suspend fun markConversationAsRead(conversationId: Int): Boolean {
    return try {
        SupabaseClient.supabase
            .from("conversation")
            .update(
                mapOf("unread_count" to 0)
            ) {
                filter { eq("id", conversationId) }
            }
        Log.d(TAG, "Marked as read: id=$conversationId")
        true
    } catch (e: Exception) {
        Log.e(TAG, "Error marking as read: ${e.message}", e)
        false
    }
}

suspend fun deleteConversation(conversationId: Int): Boolean {
    return try {
        SupabaseClient.supabase
            .from("conversation")
            .update(
                mapOf("is_active" to false)
            ) {
                filter { eq("id", conversationId) }
            }
        Log.d(TAG, "Deleted conversation: id=$conversationId")
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
    doctorName: String?,
    patientName: String?,
    hospitalId: Int,
    hospitalName: String
): Conversation? {
    return try {
        val convResponse = SupabaseClient.supabase
            .from("conversation")
            .select {
                filter {
                    eq("doctor_id", doctorId)
                    eq("patient_id", patientId)
                }
            }

        val rawList = Json.decodeFromString<List<Map<String, JsonElement>>>(convResponse.data)
        val existing = rawList.firstOrNull()

        if (existing != null) {
            val existingId = existing["id"]?.jsonPrimitive?.int
            val existingDocName = existing["doctor_name"]?.jsonPrimitive?.content ?: "Dr. Unknown"
            val existingPatName = existing["patient_name"]?.jsonPrimitive?.content ?: "Patient"
            val existingHospName = existing["hospital_name"]?.jsonPrimitive?.content ?: "General Hospital"

            Log.d(TAG, "✅ EXISTS: ID=$existingId, Dr=$existingDocName, Patient=$existingPatName, Hosp=$existingHospName")

            return Conversation(
                id = existingId,
                doctorId = doctorId,
                doctorName = existingDocName,
                patientId = patientId,
                patientName = existingPatName,
                hospitalId = hospitalId,
                hospitalName = existingHospName,
                lastMessage = existing["last_message"]?.jsonPrimitive?.content ?: "",
                lastMessageTime = existing["last_message_time"]?.jsonPrimitive?.content ?: Instant.now().toString(),
                unreadCount = existing["unread_count"]?.jsonPrimitive?.int ?: 0,
                isActive = existing["is_active"]?.jsonPrimitive?.boolean ?: true
            )
        }

        val realDoctorName = doctorName ?: run {
            try {
                val docList = SupabaseClient.supabase
                    .from("doctor")
                    .select { filter { eq("id", doctorId) } }
                    .decodeList<Map<String, JsonElement>>()
                docList.firstOrNull()?.get("name")?.jsonPrimitive?.content ?: "Dr. Unknown"
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get doctor name: ${e.message}")
                "Dr. Unknown"
            }
        }

        val realPatientName = patientName ?: run {
            try {
                val patList = SupabaseClient.supabase
                    .from("patient_user")
                    .select { filter { eq("id", patientId) } }
                    .decodeList<Map<String, JsonElement>>()
                patList.firstOrNull()?.get("name")?.jsonPrimitive?.content ?: "Patient"
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get patient name: ${e.message}")
                "Patient"
            }
        }

        val safeHospitalName = hospitalName.takeIf { it.isNotEmpty() } ?: "General Hospital"

        Log.d(TAG, "✅ NEW: Dr=$realDoctorName, Patient=$realPatientName, Hosp=$safeHospitalName")

        val newConversation = Conversation(
            id = null,
            doctorId = doctorId,
            doctorName = realDoctorName,
            patientId = patientId,
            patientName = realPatientName,
            hospitalId = hospitalId,
            hospitalName = safeHospitalName,
            lastMessage = "Start your conversation!",
            lastMessageTime = Instant.now().toString(),
            unreadCount = 0,
            isActive = true
        )

        val savedConversation = upsertConversation(newConversation)

        if (savedConversation?.id == null || savedConversation.id == 0) {
            Log.w(TAG, "Upsert returned ID=null/0 → re-querying for real ID...")
            val found = getConversationByDoctorPatient(doctorId, patientId)
            if (found != null) {
                Log.d(TAG, "✅ FOUND REAL ID: ${found.id}")
                return found
            }
        }

        Log.d(TAG, "✅ SAVED: ID=${savedConversation?.id}")
        return savedConversation

    } catch (e: Exception) {
        Log.e(TAG, "Error creating conversation: ${e.message}", e)
        null
    }
}

@RequiresApi(Build.VERSION_CODES.O)
suspend fun getConversationByDoctorPatient(doctorId: Int, patientId: Int): Conversation? {
    return try {
        val response = SupabaseClient.supabase
            .from("conversation")
            .select {
                filter {
                    eq("doctor_id", doctorId)
                    eq("patient_id", patientId)
                }
            }

        val rawList = Json.decodeFromString<List<Map<String, JsonElement>>>(response.data)
        val map = rawList.firstOrNull() ?: return null

        Conversation(
            id = map["id"]?.jsonPrimitive?.int,
            doctorId = map["doctor_id"]?.jsonPrimitive?.int ?: 0,
            doctorName = map["doctor_name"]?.jsonPrimitive?.content ?: "",
            patientId = map["patient_id"]?.jsonPrimitive?.int ?: 0,
            patientName = map["patient_name"]?.jsonPrimitive?.content ?: "",
            hospitalId = map["hospital_id"]?.jsonPrimitive?.int ?: 0,
            hospitalName = map["hospital_name"]?.jsonPrimitive?.content ?: "General Hospital",
            lastMessage = map["last_message"]?.jsonPrimitive?.content ?: "",
            lastMessageTime = map["last_message_time"]?.jsonPrimitive?.content ?: Instant.now().toString(),
            unreadCount = map["unread_count"]?.jsonPrimitive?.int ?: 0,
            isActive = map["is_active"]?.jsonPrimitive?.boolean ?: true
        )
    } catch (e: Exception) {
        Log.e(TAG, "Error finding conversation: ${e.message}", e)
        null
    }
}

@RequiresApi(Build.VERSION_CODES.O)
suspend fun sendMessage(message: Message): Boolean {
    return try {
        Log.d(TAG, "sendMessage called: ${message.content}")
        Log.d(TAG, "Message data: id=${message.id}, conversationId=${message.conversationId}, senderId=${message.senderId}")

        SupabaseClient.supabase
            .from("message")
            .insert(message)
        Log.d(TAG, "Message inserted to Supabase: ${message.id}")

        updateConversationLastMessage(
            message.conversationId,
            message.content,
            Instant.now().toString()
        )
        Log.d(TAG, "Conversation updated: id=${message.conversationId}")
        true
    } catch (e: Exception) {
        Log.e(TAG, "Error sending message: ${e.message}", e)
        false
    }
}

@RequiresApi(Build.VERSION_CODES.O)
suspend fun getMessagesByConversation(conversationId: Int): List<Message> {
    return try {
        Log.d(TAG, "Searching messages with conversationId: $conversationId")

        val response = SupabaseClient.supabase
            .from("message")
            .select {
                filter { eq("conversation_id", conversationId) }
                order("timestamp", Order.ASCENDING)
            }

        val rawList = Json.decodeFromString<List<Map<String, JsonElement>>>(response.data)
        Log.d(TAG, "Raw messages found: ${rawList.size}")

        val messages = rawList.map { map ->
            val timestampStr = map["timestamp"]?.jsonPrimitive?.content ?: ""

            Message(
                id = map["id"]?.jsonPrimitive?.int ?: 0,
                conversationId = map["conversation_id"]?.jsonPrimitive?.int ?: 0,
                content = map["content"]?.jsonPrimitive?.content ?: "",
                senderId = map["sender_id"]?.jsonPrimitive?.int ?: 0,
                senderName = map["sender_name"]?.jsonPrimitive?.content ?: "",
                timestamp = timestampStr,
                type = map["type"]?.jsonPrimitive?.content ?: "text"
            )
        }

        Log.d(TAG, "Loaded ${messages.size} messages!")
        return messages

    } catch (e: Exception) {
        Log.e(TAG, "Error loading messages: ${e.message}", e)
        emptyList()
    }
}

@RequiresApi(Build.VERSION_CODES.O)
suspend fun getLastMessage(conversationId: Int): Message? {
    return try {
        Log.d(TAG, "Getting last message for conversationId: $conversationId")

        val response = SupabaseClient.supabase
            .from("message")
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
            val time = parseTimestampToEpochMs(timestampStr ?: "")

            if (time > newestTime) {
                newestTime = time
                newestMap = map
            }
        }

        if (newestMap == null) return null

        val timestampStr = newestMap["timestamp"]?.jsonPrimitive?.content
            ?: Instant.now().toString()

        val message = Message(
            id = newestMap["id"]?.jsonPrimitive?.int ?: 0,
            conversationId = newestMap["conversation_id"]?.jsonPrimitive?.int ?: 0,
            content = newestMap["content"]?.jsonPrimitive?.content ?: "",
            senderId = newestMap["sender_id"]?.jsonPrimitive?.int ?: 0,
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

suspend fun deleteMessage(messageId: Int): Boolean {
    return try {
        SupabaseClient.supabase
            .from("message")
            .delete {
                filter { eq("id", messageId) }
            }
        Log.d(TAG, "Deleted message: id=$messageId")
        true
    } catch (e: Exception) {
        Log.e(TAG, "Error deleting message: ${e.message}", e)
        false
    }
}

@RequiresApi(Build.VERSION_CODES.O)
suspend fun clearMessagesByConversation(conversationId: Int): Boolean {
    return try {
        SupabaseClient.supabase
            .from("message")
            .delete {
                filter { eq("conversation_id", conversationId) }
            }

        SupabaseClient.supabase
            .from("conversation")
            .update(
                mapOf(
                    "last_message" to "No messages yet",
                    "last_message_time" to Instant.now().toString(),
                    "unread_count" to 0
                )
            ) {
                filter { eq("id", conversationId) }
            }

        Log.d(TAG, "Cleared ALL messages for conversationId: $conversationId")
        true
    } catch (e: Exception) {
        Log.e(TAG, "Error clearing messages: ${e.message}", e)
        false
    }
}