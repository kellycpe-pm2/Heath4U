package com.example.healt4u.screen.DoctorPatientChat

import android.icu.text.SimpleDateFormat
import android.os.Build
import androidx.annotation.RequiresApi
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healt4u.R
import com.example.healt4u.model.Message
import java.util.Date
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MessageBubble(
    message: Message,
    isFromCurrentUser: Boolean
){
    Row(
        modifier = Modifier.fillMaxWidth().background(color = MaterialTheme.colorScheme.primary).padding(horizontal = 0.dp, vertical = 8.dp),
        horizontalArrangement = if (isFromCurrentUser) {
            Arrangement.End } else { Arrangement.Start }
    ) {
        if (!isFromCurrentUser){
            SenderAvatar(
                senderName = message.senderName,
                modifier = Modifier.padding(8.dp)
            )
        }

        Column(
            modifier = Modifier
                .wrapContentWidth()
                .widthIn(min = 50.dp, max = if (isFromCurrentUser){280.dp} else {250.dp})
                .clip(RoundedCornerShape(16.dp))
                .background(if (isFromCurrentUser){
                    MaterialTheme.colorScheme.secondary
                }else{
                    MaterialTheme.colorScheme.onSecondary
                })
                .padding(12.dp)
        ) {
            Text(
                text = message.content,
                color = if (isFromCurrentUser){
                    MaterialTheme.colorScheme.onSecondary
                } else {
                    MaterialTheme.colorScheme.secondary
                },
                fontSize = 15.sp
            )

            Row(
                modifier = Modifier.padding(top = 4.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = formatTimestamp(message.timestamp),
                    fontSize = 10.sp,
                    color = if (isFromCurrentUser) {
                        MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.7f)
                    } else {
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f)
                    }
                )
            }

        }

    }
}

@Composable
fun SenderAvatar(
    senderName: String,
    modifier: Modifier = Modifier
){
    val avatarResource = R.drawable.person

    Image(
        painter = painterResource(avatarResource),
        contentDescription = senderName,
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(color = Color.White)
            .border(
                width = 1.dp,
                color = Color.Black,
                shape = CircleShape
            )
    )
}

@RequiresApi(Build.VERSION_CODES.O)
private fun formatTimestamp(timestamp: String): String {
    return try {
        val instant = java.time.Instant.parse(timestamp)
        val date = Date(instant.toEpochMilli())
        val format = SimpleDateFormat("HH:mm", Locale.getDefault())
        format.format(date)
    } catch (e: Exception) {
        "??:??"  // Fallback if parsing fails
    }
}
