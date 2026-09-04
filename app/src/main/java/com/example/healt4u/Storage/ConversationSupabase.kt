package com.example.healt4u.Storage

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.healt4u.model.Conversation
import com.example.healt4u.model.Message
import com.example.healt4u.model.Payment
import com.example.healt4u.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonPrimitive
import java.time.Instant
import java.time.OffsetDateTime

private const val TAG = "SupabaseStorage"

@RequiresApi(Build.VERSION_CODES.O)
suspend fun getConversationsByPatient(patientId: Int): List<Conversation> {
    return try {
        Log.d(TAG, "Loading conversations for patientId: $patientId")

        val rawList = SupabaseClient.supabase
            .from("conversation")
            .select {
                filter { eq("patient_id", patientId) }
            }
            .decodeList<Map<String, JsonElement>>()

        Log.d(TAG, "Raw items count: ${rawList.size}")
        if (rawList.isNotEmpty()) {
            Log.d(TAG, "First item id: ${rawList.first()["id"]}")
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
                isActive = map["is_active"]?.jsonPrimitive?.boolean ?: true,
                createdTime = map["created_time"]?.jsonPrimitive?.content ?: Instant.now().toString()
            )
        }

        Log.d(TAG, "Returning ${conversations.size} conversations")
        return conversations

    } catch (e: Exception) {
        Log.e(TAG, "Error loading patient conversations: ${e.message}", e)
        emptyList()
    }
}

@RequiresApi(Build.VERSION_CODES.O)
suspend fun getConversationsByDoctor(doctorId: Int): List<Conversation> {
    return try {
        Log.d(TAG, "Loading conversations for doctorId: $doctorId")

        val rawList = SupabaseClient.supabase
            .from("conversation")
            .select {
                filter { eq("doctor_id", doctorId) }
            }
            .decodeList<Map<String, JsonElement>>()

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
                isActive = map["is_active"]?.jsonPrimitive?.boolean ?: true,
                createdTime = map["created_time"]?.jsonPrimitive?.content ?: Instant.now().toString()
            )
        }

        Log.d(TAG, "Loaded ${conversations.size} conversations")
        return conversations

    } catch (e: Exception) {
        Log.e(TAG, "Error loading doctor conversations: ${e.message}", e)
        emptyList()
    }
}

@RequiresApi(Build.VERSION_CODES.O)
suspend fun getConversationById(conversationId: Int): Conversation? {
    return try {
        val rawList = SupabaseClient.supabase
            .from("conversation")
            .select {
                filter { eq("id", conversationId) }
            }
            .decodeList<Map<String, JsonElement>>()

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
            lastMessageTime = map["last_message_time"]?.jsonPrimitive?.content
                ?: Instant.now().toString(),
            unreadCount = map["unread_count"]?.jsonPrimitive?.int ?: 0,
            isActive = map["is_active"]?.jsonPrimitive?.boolean ?: true,
            createdTime = map["created_time"]?.jsonPrimitive?.content ?: Instant.now().toString()
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

        Log.d(TAG, "UPSERT RETURNED ID=$realId")

        if (realId == null || realId == 0) {
            Log.w(TAG, "ID still null/0 → re-querying...")
            val found = getConversationByDoctorPatient(conversation.doctorId, conversation.patientId)
            if (found?.id != null && found.id != 0) {
                Log.d(TAG, "FOUND REAL ID: ${found.id}")
                return found
            }
        }

        return conversation.copy(
            id = realId,
            doctorName = row["doctor_name"]?.jsonPrimitive?.content ?: conversation.doctorName,
            patientName = row["patient_name"]?.jsonPrimitive?.content ?: conversation.patientName,
            hospitalName = row["hospital_name"]?.jsonPrimitive?.content ?: conversation.hospitalName,
            createdTime = row["created_time"]?.jsonPrimitive?.content ?: conversation.createdTime
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
        Log.d(TAG, "Updated last_message: id=$convId")
    } catch (e: Exception) {
        Log.w(TAG, "Update skipped: ${e.message}")
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
        // Check for existing
        val existing = getConversationByDoctorPatient(doctorId, patientId)
        if (existing != null) {
            Log.d(TAG, "EXISTS: ID=${existing.id}, Dr=${existing.doctorName}, Patient=${existing.patientName}")
            return existing
        }

        // Resolve names if not provided
        val realDoctorName = doctorName ?: run {
            try {
                SupabaseClient.supabase
                    .from("doctor")
                    .select { filter { eq("id", doctorId) } }
                    .decodeList<Map<String, JsonElement>>()
                    .firstOrNull()?.get("name")?.jsonPrimitive?.content ?: "Dr. Unknown"
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get doctor name: ${e.message}")
                "Dr. Unknown"
            }
        }

        val realPatientName = patientName ?: run {
            try {
                SupabaseClient.supabase
                    .from("patient_user")
                    .select { filter { eq("id", patientId) } }
                    .decodeList<Map<String, JsonElement>>()
                    .firstOrNull()?.get("name")?.jsonPrimitive?.content ?: "Patient"
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get patient name: ${e.message}")
                "Patient"
            }
        }

        val safeHospitalName = hospitalName.takeIf { it.isNotEmpty() } ?: "General Hospital"
        val nowStr = Instant.now().toString()

        Log.d(TAG, "NEW: Dr=$realDoctorName, Patient=$realPatientName, Hosp=$safeHospitalName")

        val newConv = Conversation(
            id = null,
            doctorId = doctorId,
            doctorName = realDoctorName,
            patientId = patientId,
            patientName = realPatientName,
            hospitalId = hospitalId,
            hospitalName = safeHospitalName,
            lastMessage = "Start your conversation!",
            lastMessageTime = nowStr,
            unreadCount = 0,
            isActive = true,
            createdTime = nowStr
        )

        val saved = upsertConversation(newConv)
        if (saved?.id == null || saved.id == 0) {
            val found = getConversationByDoctorPatient(doctorId, patientId)
            if (found != null) return found
        }

        Log.d(TAG, "SAVED: ID=${saved?.id}")
        return saved

    } catch (e: Exception) {
        Log.e(TAG, "Error creating conversation: ${e.message}", e)
        null
    }
}

@RequiresApi(Build.VERSION_CODES.O)
suspend fun getConversationByDoctorPatient(doctorId: Int, patientId: Int): Conversation? {
    return try {
        val map = SupabaseClient.supabase
            .from("conversation")
            .select {
                filter {
                    eq("doctor_id", doctorId)
                    eq("patient_id", patientId)
                }
            }
            .decodeList<Map<String, JsonElement>>()
            .firstOrNull() ?: return null

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
            isActive = map["is_active"]?.jsonPrimitive?.boolean ?: true,
            createdTime = map["created_time"]?.jsonPrimitive?.content ?: Instant.now().toString()
        )
    } catch (e: Exception) {
        Log.e(TAG, "Error finding conversation: ${e.message}", e)
        null
    }
}

@RequiresApi(Build.VERSION_CODES.O)
suspend fun sendMessage(message: Message): Boolean {
    return try {
        Log.d(TAG, "sendMessage: conv=${message.conversationId}, sender=${message.senderId}")

        SupabaseClient.supabase
            .from("message")
            .insert(message)

        updateConversationLastMessage(
            message.conversationId,
            message.content,
            Instant.now().toString()
        )

        Log.d(TAG, "Message sent & conversation updated")
        true
    } catch (e: Exception) {
        Log.e(TAG, "Error sending message: ${e.message}", e)
        false
    }
}

@RequiresApi(Build.VERSION_CODES.O)
suspend fun getMessagesByConversation(conversationId: Int): List<Message> {
    return try {
        Log.d(TAG, "Loading messages for convId: $conversationId")

        val rawList = SupabaseClient.supabase
            .from("message")
            .select {
                filter { eq("conversation_id", conversationId) }
                order("timestamp", Order.ASCENDING)
            }
            .decodeList<Map<String, JsonElement>>()

        Log.d(TAG, "Raw messages found: ${rawList.size}")

        val messages = rawList.map { map ->
            Message(
                id = map["id"]?.jsonPrimitive?.int ?: 0,
                conversationId = map["conversation_id"]?.jsonPrimitive?.int ?: 0,
                content = map["content"]?.jsonPrimitive?.content ?: "",
                senderId = map["sender_id"]?.jsonPrimitive?.int ?: 0,
                senderName = map["sender_name"]?.jsonPrimitive?.content ?: "",
                timestamp = map["timestamp"]?.jsonPrimitive?.content ?: Instant.now().toString(),
                type = map["type"]?.jsonPrimitive?.content ?: "text"
            )
        }

        Log.d(TAG, "Returning ${messages.size} messages")
        return messages

    } catch (e: Exception) {
        Log.e(TAG, "Error loading messages: ${e.message}", e)
        emptyList()
    }
}

suspend fun deleteMessage(messageId: Int): Boolean {
    return try {
        SupabaseClient.supabase
            .from("message")
            .delete { filter { eq("id", messageId) } }
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
            .delete { filter { eq("conversation_id", conversationId) } }

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

        Log.d(TAG, "Cleared messages for convId: $conversationId")
        true
    } catch (e: Exception) {
        Log.e(TAG, "Error clearing messages: ${e.message}", e)
        false
    }
}

@RequiresApi(Build.VERSION_CODES.O)
suspend fun refundPaymentIfEligible(
    patientId: Int,
    doctorId: Int
): Boolean {
    return try {
        Log.d("Refund", "Checking refund: patient=$patientId, doctor=$doctorId")

        val payment = SupabaseClient.supabase
            .from("payments")
            .select {
                filter {
                    eq("patient_id", patientId)
                    eq("doctor_id", doctorId)
                }
            }
            .decodeList<Payment>()
            .firstOrNull()

        if (payment == null) {
            Log.w("Refund", "No payment found — cannot refund")
            return false
        }

        if (payment.status.equals("refunded", ignoreCase = true)) {
            Log.d("Refund", "Already refunded")
            return false
        }

        // update to supabase
        SupabaseClient.supabase
            .from("payments")
            .update(
                mapOf(
                    "status" to "refunded",
                    "refunded_at" to getCurrentTimestamp()
                )
            ) {
                filter { eq("id", payment.id) }
            }

        return true

    } catch (e: Exception) {
        false
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun getCurrentTimestamp(): String {
    return OffsetDateTime.now().toString()
}