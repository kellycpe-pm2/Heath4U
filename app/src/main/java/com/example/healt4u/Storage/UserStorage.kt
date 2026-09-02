package com.example.healt4u.Storage

import com.example.healt4u.model.PatientUser
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

suspend fun patientSignUp(
    name: String,
    password: String,
    email: String?,
    phone: String?
): Result<PatientUser> {
    return try {
        withContext(Dispatchers.IO) {
            android.util.Log.d("PatientAuth", "Signing up with name: $name")

            if (!email.isNullOrBlank()) {
                val existingEmail = supabase
                    .from("Patient")
                    .select {
                        filter { eq("email", email) }
                    }
                    .decodeList<PatientUser>()

                if (existingEmail.isNotEmpty()) {
                    return@withContext Result.failure(Exception("Email already registered"))
                }
            }

            if (!phone.isNullOrBlank()) {
                val existingPhone = supabase
                    .from("Patient")
                    .select {
                        filter { eq("phone", phone) }
                    }
                    .decodeList<PatientUser>()

                if (existingPhone.isNotEmpty()) {
                    return@withContext Result.failure(Exception("Phone number already registered"))
                }
            }

            val icNumber = "IC_${System.currentTimeMillis()}"

            val insertData = buildMap {
                put("ic", JsonPrimitive(icNumber))
                put("patient_name", JsonPrimitive(name))
                put("password", JsonPrimitive(password))
                if (!email.isNullOrBlank()) put("email", JsonPrimitive(email))
                if (!phone.isNullOrBlank()) put("phone", JsonPrimitive(phone))
            }

            supabase
                .from("Patient")
                .insert(JsonObject(insertData))

            val inserted = supabase
                .from("Patient")
                .select {
                    filter {
                        eq("ic", icNumber)
                    }
                }
                .decodeList<PatientUser>()
                .firstOrNull()

            android.util.Log.d("PatientAuth", "Account created successfully")
            if (inserted != null) {
                Result.success(inserted)
            } else {
                Result.failure(Exception("Account created but could not retrieve user"))
            }
        }
    } catch (e: Exception) {
        android.util.Log.e("PatientAuth", "patientSignUp failed", e)
        Result.failure(Exception("Registration failed: ${e.message}"))
    }
}

suspend fun patientSignIn(
    emailOrPhone: String,
    password: String,
    loginMethod: String
): Result<PatientUser> {
    return try {
        withContext(Dispatchers.IO) {
            android.util.Log.d("PatientAuth", "Signing in with $loginMethod: $emailOrPhone")
            val users = supabase
                .from("Patient")
                .select()
                .decodeList<PatientUser>()

            android.util.Log.d("PatientAuth", "Total users in table: ${users.size}")

            val matched = users.find { user ->
                val credentialMatch = when (loginMethod) {
                    "email" -> user.email == emailOrPhone
                    "phone" -> user.phone == emailOrPhone
                    else -> false
                }
                credentialMatch && user.password == password
            }
            if (matched != null) {
                android.util.Log.d("PatientAuth", "Login successful for: ${matched.name}")
                Result.success(matched)
            } else {
                android.util.Log.d("PatientAuth", "No match found for: $emailOrPhone")
                Result.failure(Exception("Invalid credentials or password"))
            }
        }
    } catch (e: Exception) {
        android.util.Log.e("PatientAuth", "patientSignIn failed", e)
        Result.failure(Exception("Login failed: ${e.message}"))
    }
}

suspend fun getPatientByPhone(phone: String): PatientUser? {
    return try {
        withContext(Dispatchers.IO) {
            val users = supabase
                .from("Patient")
                .select {
                    filter { eq("phone", phone) }
                }
                .decodeList<PatientUser>()
            users.firstOrNull()
        }
    } catch (e: Exception) {
        android.util.Log.e("PatientAuth", "getPatientByPhone failed", e)
        null
    }
}

suspend fun getPatientById(id: Int): PatientUser? {
    return try {
        withContext(Dispatchers.IO) {
            val users = supabase
                .from("Patient")
                .select {
                    filter { eq("id", id) }
                }
                .decodeList<PatientUser>()
            users.firstOrNull()
        }
    } catch (e: Exception) {
        android.util.Log.e("PatientAuth", "getPatientById failed", e)
        null
    }
}

suspend fun getAllPatients(): List<PatientUser> {
    return try {
        withContext(Dispatchers.IO) {
            supabase
                .from("Patient")
                .select()
                .decodeList<PatientUser>()
        }
    } catch (e: Exception) {
        android.util.Log.e("PatientAuth", "getAllPatients failed", e)
        emptyList()
    }
}

suspend fun patientUpdateName(patientId: Int, newName: String): Result<String> {
    return try {
        withContext(Dispatchers.IO) {
            if (newName.isBlank()) {
                return@withContext Result.failure(Exception("Name cannot be empty"))
            }

            supabase
                .from("Patient")
                .update(mapOf("patient_name" to newName)) {
                    filter { eq("id", patientId) }
                }

            Result.success("Name updated successfully")
        }
    } catch (e: Exception) {
        android.util.Log.e("PatientAuth", "patientUpdateName failed", e)
        Result.failure(Exception("Name update failed: ${e.message}"))
    }
}

suspend fun patientUpdateEmail(patientId: Int, newEmail: String): Result<String> {
    return try {
        withContext(Dispatchers.IO) {
            if (newEmail.isNotBlank()) {
                val existing = supabase
                    .from("Patient")
                    .select {
                        filter { eq("email", newEmail) }
                    }
                    .decodeList<PatientUser>()

                if (existing.any { it.id != patientId }) {
                    return@withContext Result.failure(Exception("Email already registered to another account"))
                }
            }

            supabase
                .from("Patient")
                .update(mapOf("email" to newEmail.ifBlank { null })) {
                    filter { eq("id", patientId) }
                }

            Result.success("Email updated successfully")
        }
    } catch (e: Exception) {
        android.util.Log.e("PatientAuth", "patientUpdateEmail failed", e)
        Result.failure(Exception("Email update failed: ${e.message}"))
    }
}

suspend fun patientUpdatePhone(patientId: Int, newPhone: String): Result<String> {
    return try {
        withContext(Dispatchers.IO) {
            if (newPhone.isNotBlank()) {
                val existing = supabase
                    .from("Patient")
                    .select {
                        filter { eq("phone", newPhone) }
                    }
                    .decodeList<PatientUser>()

                if (existing.any { it.id != patientId }) {
                    return@withContext Result.failure(Exception("Phone number already registered to another account"))
                }
            }

            supabase
                .from("Patient")
                .update(mapOf("phone" to newPhone.ifBlank { null })) {
                    filter { eq("id", patientId) }
                }

            Result.success("Phone updated successfully")
        }
    } catch (e: Exception) {
        android.util.Log.e("PatientAuth", "patientUpdatePhone failed", e)
        Result.failure(Exception("Phone update failed: ${e.message}"))
    }
}

suspend fun patientChangePassword(patientId: Int, currentPassword: String, newPassword: String): Result<String> {
    return try {
        withContext(Dispatchers.IO) {
            val patients = supabase
                .from("Patient")
                .select {
                    filter { eq("id", patientId) }
                }
                .decodeList<PatientUser>()

            val matched = patients.firstOrNull()
                ?: return@withContext Result.failure(Exception("Account not found"))

            if (matched.password != currentPassword) {
                return@withContext Result.failure(Exception("Current password is incorrect"))
            }

            supabase
                .from("Patient")
                .update(mapOf("password" to newPassword)) {
                    filter { eq("id", patientId) }
                }

            Result.success("Password changed successfully")
        }
    } catch (e: Exception) {
        android.util.Log.e("PatientAuth", "patientChangePassword failed", e)
        Result.failure(Exception("Password change failed: ${e.message}"))
    }
}
