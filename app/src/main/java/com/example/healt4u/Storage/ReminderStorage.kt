package com.example.healt4u.Storage

import com.example.healt4u.model.ReminderLog
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Objects.isNull

suspend fun getReminderLogsForDate(date: String): List<ReminderLog> {
    return try {
        withContext(Dispatchers.IO) {
            supabase
                .from("reminder_logs")
                .select {
                    filter {
                        eq("date", date)
                    }
                }
                .decodeList<ReminderLog>()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
    }
}

suspend fun upsertReminderLog(log: ReminderLog): Boolean {
    return try {
        withContext(Dispatchers.IO) {
            supabase
                .from("reminder_logs")
                .upsert(log) {
                    onConflict = "id"
                }
        }
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

suspend fun upsertReminderLogs(logs: List<ReminderLog>): Boolean {
    return try {
        withContext(Dispatchers.IO) {
            supabase
                .from("reminder_logs")
                .upsert(logs) {
                    onConflict = "id"
                }
        }
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

/*
Run once in the Supabase SQL editor for this project before the Schedule
screen can sync to the cloud (local device storage works either way):

create table if not exists reminder_logs (
    id text primary key,
    medicine_id int not null,
    medicine_name text not null,
    date text not null,
    time text not null,
    status text not null default 'PENDING'
);

alter table medicine add column if not exists reminder_time text default '08:00';
alter table medicine add column if not exists times_per_day int default 1;
*/
