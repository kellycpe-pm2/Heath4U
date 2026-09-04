package com.example.healt4u.ViewModel

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.core.content.edit
import com.example.healt4u.Storage.addCaregiverLink
import com.example.healt4u.Storage.getCaregiversForPatient
import com.example.healt4u.Storage.getPatientsForCaregiver
import com.example.healt4u.Storage.removeCaregiverLink
import com.example.healt4u.Storage.getPatientByPhone
import com.example.healt4u.Storage.upsertFamilyAlertCloud
import com.example.healt4u.Storage.upsertFamilyAlertsCloud
import com.example.healt4u.Storage.getFamilyAlertsForCaregiver
import com.example.healt4u.Storage.getReminderLogsForDate
import com.example.healt4u.data.local.loadFamilyAlerts
import com.example.healt4u.data.local.loadReminderLogsForDate
import com.example.healt4u.data.local.saveFamilyAlerts
import com.example.healt4u.data.local.upsertReminderLogLocal
import com.example.healt4u.model.CaregiverLink
import com.example.healt4u.model.FamilyAlert
import com.example.healt4u.Storage.upsertReminderLog
import com.example.healt4u.notification.ReminderEngine
import com.example.healt4u.notification.CaregiverAlertScheduler
import com.example.healt4u.supabase.SupabaseClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Suppress("unused")
class FamilyModeViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val _caregivers = MutableStateFlow<List<CaregiverLink>>(emptyList())
    val caregivers: StateFlow<List<CaregiverLink>> = _caregivers

    private val _myPatients = MutableStateFlow<List<CaregiverLink>>(emptyList())
    val myPatients: StateFlow<List<CaregiverLink>> = _myPatients

    private val _alerts = MutableStateFlow<List<FamilyAlert>>(emptyList())
    val alerts: StateFlow<List<FamilyAlert>> = _alerts

    private val _caregiverAlerts = MutableStateFlow<List<FamilyAlert>>(emptyList())
    val caregiverAlerts: StateFlow<List<FamilyAlert>> = _caregiverAlerts

    private val _patientPhone = MutableStateFlow("")
    val patientPhone: StateFlow<String> = _patientPhone

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private var caregiverRealtimeJob: Job? = null

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun todayDate(): String = dateFormat.format(Calendar.getInstance().time)

    fun loadPatientPhone(context: Context) {
        val prefs = context.getSharedPreferences("family_mode_prefs", Context.MODE_PRIVATE)
        _patientPhone.value = prefs.getString("patient_phone", "") ?: ""
    }

    fun savePatientPhone(context: Context, phone: String) {
        val prefs = context.getSharedPreferences("family_mode_prefs", Context.MODE_PRIVATE)
        prefs.edit { putString("patient_phone", phone) }
        _patientPhone.value = phone
    }

    fun refreshCaregivers(patientUserId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val links = getCaregiversForPatient(patientUserId)
                _caregivers.value = links
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun refreshMyPatients(caregiverUserId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val links = getPatientsForCaregiver(caregiverUserId)
                _myPatients.value = links
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private val _addCaregiverResult = MutableStateFlow<String?>(null)
    val addCaregiverResult: StateFlow<String?> = _addCaregiverResult

    fun clearAddCaregiverResult() {
        _addCaregiverResult.value = null
    }

    fun addCaregiver(
        patientUserId: Int,
        phone: String,
        relationship: String,
        patientName: String,
        patientPhone: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val caregiverUser = getPatientByPhone(phone)
                if (caregiverUser == null) {
                    _addCaregiverResult.value = "No user found with this phone number"
                    return@launch
                }

                val existingLink = com.example.healt4u.Storage.getCaregiverLinkByPatientAndCaregiver(
                    patientUserId = patientUserId,
                    caregiverUserId = caregiverUser.id
                )
                if (existingLink != null) {
                    _addCaregiverResult.value = "This user is already your caregiver"
                    return@launch
                }

                val link = CaregiverLink(
                    id = 0,
                    patientUserId = patientUserId,
                    caregiverUserId = caregiverUser.id,
                    relationship = relationship,
                    status = "ACCEPTED",
                    caregiverName = caregiverUser.name,
                    caregiverPhone = caregiverUser.phone ?: "",
                    patientName = patientName,
                    patientPhone = patientPhone
                )

                val success = addCaregiverLink(link)
                if (success) {
                    refreshCaregivers(patientUserId)
                    _addCaregiverResult.value = "SUCCESS"
                } else {
                    _addCaregiverResult.value = "Failed to add caregiver"
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _addCaregiverResult.value = "Error: ${e.message}"
            }
        }
    }

    fun removeCaregiver(linkId: Int, patientUserId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                removeCaregiverLink(linkId)
                refreshCaregivers(patientUserId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun loadAlerts(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val file = java.io.File(context.filesDir, "family_alerts.json")
            if (!file.exists()) return@launch
            try {
                val list = kotlinx.serialization.json.Json.decodeFromString<List<FamilyAlert>>(file.readText())
                _alerts.value = list
            } catch (_: Exception) { }
        }
    }

    fun loadCaregiverAlerts(caregiverUserId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val alertsList = getFamilyAlertsForCaregiver(caregiverUserId)
                _caregiverAlerts.value = alertsList
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun startCaregiverAlertRealtime(context: Context, caregiverUserId: Int) {
        if (caregiverUserId == 0) return
        caregiverRealtimeJob?.cancel()
        caregiverRealtimeJob = viewModelScope.launch {
            val channel = SupabaseClient.supabase.realtime.channel("family-alerts-$caregiverUserId") {}
            val changes = channel.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
                table = "family_alerts"
            }
            channel.subscribe()
            try {
                changes.collect {
                    CaregiverAlertScheduler.runNow(context, caregiverUserId)
                    loadCaregiverAlerts(caregiverUserId)
                }
            } finally {
                channel.unsubscribe()
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun checkOverdueAndCreateAlerts(context: Context, patientUserId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                val date = todayDate()
                val now = Calendar.getInstance()
                val nowMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)

                // Refresh first so overdue PENDING logs are converted to the
                // same 30-minute MISSED state used by Today's Schedule.  The
                // engine also persists that state for subsequent family-mode
                // loads.
                val logs = ReminderEngine.refresh(context).schedule
                val allStoredAlerts = loadFamilyAlerts(context)
                val existingAlerts = allStoredAlerts.filter { it.date == date }
                val existingAlertKeys = existingAlerts.map { "${it.medicineName}_${it.scheduledTime}" }.toSet()

                var caregivers = _caregivers.value
                if (caregivers.isEmpty()) {
                    try {
                        caregivers = getCaregiversForPatient(patientUserId)
                        _caregivers.value = caregivers
                    } catch (_: Exception) {}
                }
                if (caregivers.isEmpty()) {
                    _isLoading.value = false
                    return@launch
                }

                val newAlerts = mutableListOf<FamilyAlert>()
                for (log in logs) {
                    if (log.medicineId == -1 || (log.status != "PENDING" && log.status != "MISSED")) continue
                    val parts = log.time.split(":")
                    val slotMinutes = (parts.getOrNull(0)?.toIntOrNull() ?: 8) * 60 + (parts.getOrNull(1)?.toIntOrNull() ?: 0)
                    if (log.status == "PENDING" && nowMinutes - slotMinutes <= 30) continue

                    val key = "${log.medicineName}_${log.time}"
                    if (key in existingAlertKeys) continue

                    for (cg in caregivers) {
                        // Keep the reminder-log id in the alert id. The caregiver
                        // may be on another device, so resolving an alert must
                        // still be able to update the patient's exact schedule row.
                        val alertId = "fa_${log.id}_${cg.id}"
                        newAlerts.add(
                            FamilyAlert(
                                id = alertId,
                                medicineName = log.medicineName,
                                scheduledTime = log.time,
                                date = date,
                                patientPhone = _patientPhone.value,
                                status = "PENDING",
                                caregiverName = cg.caregiverName,
                                caregiverPhone = cg.caregiverPhone,
                                caregiverUserId = cg.caregiverUserId,
                                patientUserId = patientUserId
                            )
                        )
                    }
                }

                if (newAlerts.isNotEmpty()) {
                    val allAlerts = (allStoredAlerts + newAlerts).distinctBy { it.id }
                    _alerts.value = allAlerts
                    saveFamilyAlerts(context, allAlerts)
                    upsertFamilyAlertsCloud(newAlerts)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun markAlertCalled(context: Context, alert: FamilyAlert) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = alert.copy(status = "CALLED")
            val all = _alerts.value.map { if (it.id == updated.id) updated else it }
            _alerts.value = all
            saveFamilyAlerts(context, all)
            upsertFamilyAlertCloud(updated)
        }
    }

    fun resolveAlert(context: Context, alert: FamilyAlert) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = alert.copy(
                status = "RESOLVED",
                resolvedAt = System.currentTimeMillis()
            )
            val allAlerts = _alerts.value.map { if (it.id == updated.id) updated else it }
            _alerts.value = allAlerts
            saveFamilyAlerts(context, allAlerts)

            val allCaregiverAlerts = _caregiverAlerts.value.map { if (it.id == updated.id) updated else it }
            _caregiverAlerts.value = allCaregiverAlerts

            upsertFamilyAlertCloud(updated)

            // Resolve the patient's cloud log, not only this device's local log.
            // Caregiver and patient accounts commonly run on separate devices.
            val cloudLogs = getReminderLogsForDate(alert.date, alert.patientUserId)
            val localLogs = loadReminderLogsForDate(context, alert.date)
            val matchingLog = (cloudLogs + localLogs).distinctBy { it.id }.find {
                it.medicineName == alert.medicineName && it.time == alert.scheduledTime
            }
            val reminderLog = matchingLog ?: alertReminderLog(alert)
            if (reminderLog.status != "TAKEN") {
                // Only persist locally when this is the patient's own local log.
                // Writing a patient's log into the caregiver's local file would
                // pollute the caregiver's schedule after an account switch.
                if (matchingLog != null && matchingLog.patientId == com.example.healt4u.Session.CurrentSession.patientId) {
                    upsertReminderLogLocal(context, reminderLog.copy(status = "TAKEN"))
                }
                upsertReminderLog(reminderLog.copy(status = "TAKEN"))
            }
        }
    }

    /** Reconstructs a missing cloud row for alerts created from a local-only log. */
    private fun alertReminderLog(alert: FamilyAlert): com.example.healt4u.model.ReminderLog {
        val encodedLogId = alert.id.removePrefix("fa_").substringBeforeLast('_')
        val idParts = encodedLogId.split('_')
        val medicineId = when {
            idParts.getOrNull(2)?.matches(Regex("\\d{4}-\\d{2}-\\d{2}")) == true -> idParts.getOrNull(1)?.toIntOrNull()
            else -> idParts.firstOrNull()?.toIntOrNull()
        } ?: -1
        return com.example.healt4u.model.ReminderLog(
            // New alerts retain the exact generated id. Older alerts get a
            // deterministic id and are matched by medicine/time on the patient.
            id = if (idParts.getOrNull(2)?.matches(Regex("\\d{4}-\\d{2}-\\d{2}")) == true)
                encodedLogId
            else
                "family_${alert.patientUserId}_${medicineId}_${alert.date}_${alert.scheduledTime}",
            patientId = alert.patientUserId,
            medicineId = medicineId,
            medicineName = alert.medicineName,
            date = alert.date,
            time = alert.scheduledTime,
            status = "MISSED",
            type = "MEDICINE"
        )
    }
}
