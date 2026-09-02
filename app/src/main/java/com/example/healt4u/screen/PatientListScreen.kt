package com.example.healt4u.screen

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healt4u.Storage.getConversationsByDoctor
import com.example.healt4u.Storage.getPatientById
import com.example.healt4u.model.Conversation
import com.example.healt4u.screen.componentUI.Theme.colorTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientListScreen(
    doctorId: Int,
    onPatientClick: (patientId: Int, conversation: Conversation) -> Unit,
    onBack: () -> Unit
) {
    var patients by remember { mutableStateOf<List<Conversation>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var reloadKey by remember { mutableIntStateOf(0) }

    LaunchedEffect(doctorId, reloadKey) {
        try {
            isLoading = true
            errorMessage = null
            patients = getConversationsByDoctor(doctorId)
        } catch (e: Exception) {
            errorMessage = e.message ?: "Failed to load patients"
        } finally {
            isLoading = false
        }
    }

    val filteredPatients = remember(searchQuery, patients) {
        if (searchQuery.isBlank()) patients
        else patients.filter {
            it.patientName.contains(searchQuery, ignoreCase = true) || it.patientId.toString().contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Patient List", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search patients...") },
                leadingIcon = { Icon(Icons.Filled.Search, "Search", tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Filled.Clear, "Clear", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.secondary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            when {
                isLoading -> {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.secondary)
                            Spacer(Modifier.height(16.dp))
                            Text("Loading patients...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                errorMessage != null -> {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(errorMessage ?: "Something went wrong", color = MaterialTheme.colorScheme.error)
                            Spacer(Modifier.height(16.dp))
                            Button(onClick = { reloadKey++ }) { Text("Retry") }
                        }
                    }
                }
                patients.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("No patients yet", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                filteredPatients.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("No patients found", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredPatients, key = { it.id?:0 }) { conversation ->
                            PatientItem(
                                conversation = conversation,
                                onClick = {
                                    onPatientClick(conversation.patientId, conversation)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PatientItem(
    conversation: Conversation,
    onClick: () -> Unit
) {
    val patientId = conversation.patientId

    var realPatientName by remember { mutableStateOf(conversation.patientName ?: "") }

    LaunchedEffect(patientId) {
        getPatientById(patientId)?.name?.let {
            realPatientName = it
        }
    }

    val timestampMillis = try {
        val str = conversation.lastMessageTime ?: ""
        when {
            str.all { it.isDigit() } -> str.toLong()
            else -> {
                java.time.OffsetDateTime.parse(str)
                    .toInstant()
                    .toEpochMilli()
            }
        }
    } catch (e: Exception) {
        Log.w("DateParse", "Failed to parse time", e)
        System.currentTimeMillis()
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = realPatientName.take(2).uppercase(),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                    Text(realPatientName, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(formatTimeString(timestampMillis), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text("ID: $patientId", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                    Text(
                        text = conversation.lastMessage ?: "",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (conversation.unreadCount > 0) {
                        Box(
                            Modifier.size(20.dp).clip(CircleShape).background(Color.Red),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("${conversation.unreadCount}", fontSize = 10.sp, color = Color.White)
                        }
                    }
                }
            }

            Icon(Icons.Filled.ChevronRight, "Open", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun formatTimeString(timestamp: Long): String {
    return try {
        val date = Date(timestamp)
        val now = Calendar.getInstance()
        val cal = Calendar.getInstance().apply { time = date }
        when {
            isSameDay(cal, now) -> SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
            isYesterday(cal, now) -> "Yesterday"
            else -> SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date)
        }
    } catch (e: Exception) {
        "Unknown"
    }
}

private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

private fun isYesterday(cal1: Calendar, cal2: Calendar): Boolean {
    val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
    return isSameDay(cal1, yesterday)
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun PreviewPatientListScreen() {
    colorTheme {
        PatientListScreen(
            doctorId = 1,
            onPatientClick = { _, _ -> },
            onBack = {}
        )
    }
}