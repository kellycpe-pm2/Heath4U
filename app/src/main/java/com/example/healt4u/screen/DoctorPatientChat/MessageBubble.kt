package com.example.healt4u.screen.DoctorPatientChat

import android.icu.text.SimpleDateFormat
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healt4u.R
import com.example.healt4u.model.Message
import com.example.healt4u.screen.componentUI.Theme.colorTheme
import java.util.Date
import java.util.Locale

@Composable
fun MessageBubble(
    message: Message,
    isFromCurrentUser: Boolean,
    senderRole: String
){
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 0.dp, vertical = 4.dp),
        horizontalArrangement = if (isFromCurrentUser) {
            Arrangement.End } else { Arrangement.Start
        },
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!isFromCurrentUser){
            senderAvatar(
                senderName = message.senderName,
                senderRole = senderRole,
                modifier = Modifier.padding(8.dp)
            )
        }

        Row(
            modifier = Modifier.widthIn(max = if (isFromCurrentUser){280.dp} else {250.dp})
                .clip(RoundedCornerShape(16.dp))
                .background(if (isFromCurrentUser){
                    MaterialTheme.colorScheme.secondary
                }else{
                    MaterialTheme.colorScheme.primary
                })
                .padding(12.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = message.content,
                color = if (isFromCurrentUser){
                    MaterialTheme.colorScheme.onSecondary
                } else {
                    MaterialTheme.colorScheme.onPrimary
                },
                fontSize = 15.sp,
                modifier = Modifier.weight(1f)
            )

            Text(
                text = formatTimestamp(message.timestamp),
                fontSize = 10.sp,
                color = if (isFromCurrentUser){
                    MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.7f)
                } else {
                    MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                },
                modifier = Modifier.padding(start = 8.dp),
            )

        }

    }
}

@Composable
fun senderAvatar(
    senderName: String,
    senderRole: String,
    modifier: Modifier = Modifier
){
    val avatarResource = R.drawable.ic_person_foreground

    Image(
        painter = painterResource(avatarResource),
        contentDescription = senderName,
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .border(
                width = 2.dp,
                color = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape
            )
    )
}

private fun formatTimestamp(timestamp: Long): String {
    val date = Date(timestamp)
    val format = SimpleDateFormat("HH:mm", Locale.getDefault())
    return format.format(date)
}

@Preview(showBackground = true)
@Composable
fun PreviewMessageBubble(){
    colorTheme {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            MessageBubble(
                message = Message("1","Hi.", "patient1","Lili", System.currentTimeMillis(), "text"),
                isFromCurrentUser = false,
                senderRole = "patient"
            )
        }
    }
}