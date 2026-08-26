package com.example.healt4u.Storage

import com.example.healt4u.model.FamilyAlert
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun getFamilyAlertsForDate(date: String): List<FamilyAlert> {
    return try {
        withContext(Dispatchers.IO) {
            supabase
                .from("family_alerts")
                .select {
                    filter {
                        eq(column = "date", value = date)
                    }
                }
                .decodeList<FamilyAlert>()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
    }
}

suspend fun getFamilyAlertsByPatient(patientId: String): List<FamilyAlert> {
    return try {
        withContext(Dispatchers.IO) {
            supabase
                .from("family_alerts")
                .select {
                    filter {
                        eq(column = "patient_id", value = patientId)
                    }
                }
                .decodeList<FamilyAlert>()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
    }
}

suspend fun upsertFamilyAlertCloud(alert: FamilyAlert): Boolean {
    return try {
        withContext(Dispatchers.IO) {
            supabase
                .from("family_alerts")
                .upsert(alert) {
                    onConflict = "id"
                }
        }
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

suspend fun upsertFamilyAlertsCloud(alerts: List<FamilyAlert>): Boolean {
    return try {
        withContext(Dispatchers.IO) {
            supabase
                .from("family_alerts")
                .upsert(alerts) {
                    onConflict = "id"
                }
        }
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

suspend fun deleteFamilyAlert(alertId: String): Boolean {
    return try {
        withContext(Dispatchers.IO) {
            supabase
                .from("family_alerts")
                .delete {
                    filter {
                        eq(column = "id", value = alertId)
                    }
                }
        }
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

/*
Run once in the Supabase SQL editor before using Family Mode:

create table if not exists family_alerts (
    id text primary key,
    medicine_name text not null,
    scheduled_time text not null,
    date text not null,
    patient_phone text not null,
    status text not null default 'PENDING',
    caregiver_name text not null,
    caregiver_phone text default '',
    created_at bigint not null,
    resolved_at bigint
);

-- Link family_alerts to patient_id for caregiver-side queries
alter table family_alerts add column if not exists patient_id text default 'p001';
*/
