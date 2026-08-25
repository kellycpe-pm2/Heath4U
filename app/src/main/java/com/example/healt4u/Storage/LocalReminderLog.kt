// com/example/healt4u/data/local/LocalReminderLog.kt
package com.example.healt4u.data.local

import android.content.Context
import com.example.healt4u.model.ReminderLog
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

private const val FILE_NAME = "reminder_logs.json"

private val json = Json {
    prettyPrint = true
    ignoreUnknownKeys = true
    encodeDefaults = true
}

fun saveReminderLogs(context: Context, logs: List<ReminderLog>) {
    try {
        val jsonString = json.encodeToString(logs)
        context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE).use {
            it.write(jsonString.toByteArray())
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun loadReminderLogs(context: Context): List<ReminderLog> {
    val file = File(context.filesDir, FILE_NAME)
    if (!file.exists()) return emptyList()
    return try {
        Json.decodeFromString(file.readText())
    } catch (e: Exception) {
        emptyList()
    }
}

fun loadReminderLogsForDate(context: Context, date: String): List<ReminderLog> {
    return loadReminderLogs(context).filter { it.date == date }
}

// Insert-or-replace by id, keeping every other stored log untouched
fun upsertReminderLogLocal(context: Context, log: ReminderLog): Boolean {
    return try {
        val current = loadReminderLogs(context).toMutableList()
        val index = current.indexOfFirst { it.id == log.id }
        if (index != -1) current[index] = log else current.add(log)
        saveReminderLogs(context, current)
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

fun upsertReminderLogsLocal(context: Context, logs: List<ReminderLog>): Boolean {
    return try {
        val current = loadReminderLogs(context).toMutableList()
        for (log in logs) {
            val index = current.indexOfFirst { it.id == log.id }
            if (index != -1) current[index] = log else current.add(log)
        }
        saveReminderLogs(context, current)
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}
