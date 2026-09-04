package com.example.healt4u.notification


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ChatMessageReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val sender = intent.getStringExtra("senderName") ?: "New Message"
        val text = intent.getStringExtra("message") ?: ""

        NotificationHelper.showChatMessage(context, sender, text)
    }
}