package com.example.healt4u.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ChatMessageReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val senderName = intent.getStringExtra("senderName") ?: "New Message"
        val messageText = intent.getStringExtra("message") ?: ""
        val conversationId = intent.getIntExtra("conversationId", 0)

        NotificationHelper.showChatMessage(
            context = context,
            notificationId = conversationId.hashCode(),
            senderName = senderName,
            messageText = messageText
        )
    }
}