package com.example.healt4u.Storage

import androidx.lifecycle.viewModelScope
import com.example.healt4u.model.AdminUser
import com.example.healt4u.model.Medicine
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import io.ktor.client.engine.android.Android
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val SUPABASE_URL =
    "https://jotudzheiwopavprryxx.supabase.co"

private const val SUPABASE_KEY =
    "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImpvdHVkemhlaXdvcGF2cHJyeXh4Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODY1NDU1ODgsImV4cCI6MjEwMjEyMTU4OH0.Q4R0_c94lxfUKcMTVoIOdhilsDA6YfffQt7-dNoA1zM"


val supabase = createSupabaseClient(
    supabaseUrl = SUPABASE_URL,
    supabaseKey = SUPABASE_KEY
) {
    install(Postgrest)

    httpEngine = Android.create()
}

suspend fun getNextMedicineId(): Int {

    return try {

        val medicines = withContext(Dispatchers.IO) {

            supabase
                .from("medicine")
                .select {
                    order(
                        column = "id",
                        order = Order.DESCENDING
                    )

                    limit(1)
                }
                .decodeList<Medicine>()
        }

        if (medicines.isEmpty()) {

            1

        } else {

            val highestId =
                medicines.first().id

            val nextId =
                highestId + 1

            nextId
        }

    } catch (e: Exception) {


        e.printStackTrace()


        1
    }
}


suspend fun insertSingleMedicine(
    medicine: Medicine
): Boolean {

    return try {




        withContext(Dispatchers.IO) {

            supabase
                .from("medicine")
                .insert(medicine)
        }


        true

    } catch (e: Exception) {

        false
    }
}


suspend fun getAllMedicines(): List<Medicine> {

    return try {

        withContext(Dispatchers.IO) {

            val result = supabase
                .from("medicine")
                .select {
                    order(
                        column = "id",
                        order = Order.ASCENDING
                    )
                }

            val medicines =
                result.decodeList<Medicine>()

            medicines
        }

    } catch (e: Exception) {

        emptyList()
    }
}


suspend fun getMedicinesByPatientId(
    patientId: Int
): List<Medicine> {

    return try {

        withContext(Dispatchers.IO) {

            val result = supabase
                .from("medicine")
                .select {
                    filter {
                        eq(
                            column = "patient_id",
                            value = patientId
                        )
                    }
                    order(
                        column = "id",
                        order = Order.ASCENDING
                    )
                }

            result.decodeList<Medicine>()
        }

    } catch (e: Exception) {

        emptyList()
    }
}


suspend fun getMedicinesByIC(
    icValue: String = "1"
): List<Medicine> {

    return try {

        withContext(Dispatchers.IO) {

            val result = supabase
                .from("medicine")
                .select {
                    filter {
                        eq(
                            column = "ic",
                            value = icValue
                        )
                    }
                }

            result.decodeList<Medicine>()
        }

    } catch (e: Exception) {


        emptyList()
    }
}



suspend fun update_Medicine(
    medicine: Medicine
): Boolean {

    return try {

        withContext(Dispatchers.IO) {

            supabase
                .from("medicine")
                .update(medicine) {

                    filter {
                        eq(
                            column = "id",
                            value = medicine.id
                        )
                    }
                }
        }

        true

    } catch (e: Exception) {

        e.printStackTrace()

        false
    }
}
suspend fun delete_Medicine(
    id: Int
): Boolean {

    return try {

        withContext(Dispatchers.IO) {

            supabase
                .from("medicine")
                .delete {

                    filter {
                        eq(
                            column = "id",
                            value = id
                        )
                    }
                }
        }


        true

    } catch (e: Exception) {

        false
    }
}

suspend fun adminSignIn(emailOrPhone: String, password: String, loginMethod: String): Result<String> {
    return try {
        withContext(Dispatchers.IO) {
            android.util.Log.d("AdminAuth", "Signing in with $loginMethod: $emailOrPhone")
            val users = supabase
                .from("admin_users")
                .select()
                .decodeList<AdminUser>()

            android.util.Log.d("AdminAuth", "Total users in table: ${users.size}")

            val matched = users.find { user ->
                val credentialMatch = when (loginMethod) {
                    "email" -> user.email == emailOrPhone
                    "phone" -> user.phone == emailOrPhone
                    else -> false
                }
                credentialMatch && user.password == password
            }
            if (matched != null) {
                android.util.Log.d("AdminAuth", "Login successful for: ${matched.username}")
                Result.success(matched.username)
            } else {
                android.util.Log.d("AdminAuth", "No match found for: $emailOrPhone")
                Result.failure(Exception("Invalid credentials or password"))
            }
        }
    } catch (e: Exception) {
        android.util.Log.e("AdminAuth", "adminSignIn failed", e)
        Result.failure(Exception("Login failed: ${e.message}"))
    }
}

suspend fun adminSignUp(
    username: String,
    password: String,
    email: String?,
    phone: String?
): Result<String> {
    return try {
        withContext(Dispatchers.IO) {
            android.util.Log.d("AdminAuth", "Signing up with username: $username")

            val existingUsername = supabase
                .from("admin_users")
                .select {
                    filter { eq("username", username) }
                }
                .decodeList<AdminUser>()

            if (existingUsername.isNotEmpty()) {
                return@withContext Result.failure(Exception("Username already taken"))
            }

            if (!email.isNullOrBlank()) {
                val existingEmail = supabase
                    .from("admin_users")
                    .select {
                        filter { eq("email", email) }
                    }
                    .decodeList<AdminUser>()

                if (existingEmail.isNotEmpty()) {
                    return@withContext Result.failure(Exception("Email already registered"))
                }
            }

            if (!phone.isNullOrBlank()) {
                val existingPhone = supabase
                    .from("admin_users")
                    .select {
                        filter { eq("phone", phone) }
                    }
                    .decodeList<AdminUser>()

                if (existingPhone.isNotEmpty()) {
                    return@withContext Result.failure(Exception("Phone number already registered"))
                }
            }

            supabase
                .from("admin_users")
                .insert(
                    AdminUser(
                        username = username,
                        password = password,
                        email = email,
                        phone = phone
                    )
                )

            android.util.Log.d("AdminAuth", "Account created successfully")
            Result.success("Account created successfully")
        }
    } catch (e: Exception) {
        android.util.Log.e("AdminAuth", "adminSignUp failed", e)
        Result.failure(Exception("Registration failed: ${e.message}"))
    }
}

suspend fun adminFindAccount(emailOrPhone: String, method: String): Result<String> {
    return try {
        withContext(Dispatchers.IO) {
            android.util.Log.d("AdminAuth", "Finding account with $method: $emailOrPhone")
            val users = supabase
                .from("admin_users")
                .select()
                .decodeList<AdminUser>()

            val matched = users.find { user ->
                when (method) {
                    "email" -> user.email == emailOrPhone
                    "phone" -> user.phone == emailOrPhone
                    else -> false
                }
            }

            if (matched != null) {
                android.util.Log.d("AdminAuth", "Account found for: $emailOrPhone")
                Result.success(matched.username)
            } else {
                android.util.Log.d("AdminAuth", "No account found for: $emailOrPhone")
                Result.failure(Exception("No account found with this $method"))
            }
        }
    } catch (e: Exception) {
        android.util.Log.e("AdminAuth", "adminFindAccount failed", e)
        Result.failure(Exception("Account lookup failed: ${e.message}"))
    }
}

suspend fun adminResetPassword(emailOrPhone: String, method: String, newPassword: String): Result<String> {
    return try {
        withContext(Dispatchers.IO) {
            android.util.Log.d("AdminAuth", "Resetting password for $method: $emailOrPhone")
            val users = supabase
                .from("admin_users")
                .select()
                .decodeList<AdminUser>()

            val matched = users.find { user ->
                when (method) {
                    "email" -> user.email == emailOrPhone
                    "phone" -> user.phone == emailOrPhone
                    else -> false
                }
            }

            if (matched == null) {
                return@withContext Result.failure(Exception("No account found with this $method"))
            }

            supabase
                .from("admin_users")
                .update(
                    mapOf("password" to newPassword)
                ) {
                    filter { eq("id", matched.id) }
                }

            android.util.Log.d("AdminAuth", "Password reset successful for: $emailOrPhone")
            Result.success("Password reset successful")
        }
    } catch (e: Exception) {
        android.util.Log.e("AdminAuth", "adminResetPassword failed", e)
        Result.failure(Exception("Password reset failed: ${e.message}"))
    }
}

suspend fun adminChangePassword(
    username: String,
    currentPassword: String,
    newPassword: String
): Result<String> {
    return try {
        withContext(Dispatchers.IO) {
            android.util.Log.d("AdminAuth", "Changing password for: $username")
            val users = supabase
                .from("admin_users")
                .select()
                .decodeList<AdminUser>()

            val matched = users.find {
                it.username == username && it.password == currentPassword
            }

            if (matched == null) {
                return@withContext Result.failure(Exception("Current password is incorrect"))
            }

            supabase
                .from("admin_users")
                .update(
                    mapOf("password" to newPassword)
                ) {
                    filter { eq("id", matched.id) }
                }

            android.util.Log.d("AdminAuth", "Password changed successfully for: $username")
            Result.success("Password changed successfully")
        }
    } catch (e: Exception) {
        android.util.Log.e("AdminAuth", "adminChangePassword failed", e)
        Result.failure(Exception("Password change failed: ${e.message}"))
    }
}

suspend fun adminGetProfile(username: String): Result<AdminUser> {
    return try {
        withContext(Dispatchers.IO) {
            android.util.Log.d("AdminAuth", "Fetching profile for: $username")
            val users = supabase
                .from("admin_users")
                .select {
                    filter { eq("username", username) }
                }
                .decodeList<AdminUser>()

            val user = users.firstOrNull()
            if (user != null) {
                android.util.Log.d("AdminAuth", "Profile found: ${user.username}, email=${user.email}, phone=${user.phone}")
                Result.success(user)
            } else {
                Result.failure(Exception("Profile not found"))
            }
        }
    } catch (e: Exception) {
        android.util.Log.e("AdminAuth", "adminGetProfile failed", e)
        Result.failure(Exception("Failed to load profile: ${e.message}"))
    }
}

suspend fun adminUpdateUsername(currentUsername: String, newUsername: String, password: String): Result<String> {
    return try {
        withContext(Dispatchers.IO) {
            android.util.Log.d("AdminAuth", "Updating username from $currentUsername to $newUsername")

            if (currentUsername == newUsername) {
                return@withContext Result.success("Username unchanged")
            }

            val existing = supabase
                .from("admin_users")
                .select {
                    filter { eq("username", newUsername) }
                }
                .decodeList<AdminUser>()

            if (existing.isNotEmpty()) {
                return@withContext Result.failure(Exception("Username already taken"))
            }

            val users = supabase
                .from("admin_users")
                .select()
                .decodeList<AdminUser>()

            val matched = users.find {
                it.username == currentUsername && it.password == password
            }

            if (matched == null) {
                return@withContext Result.failure(Exception("Current password is incorrect"))
            }

            supabase
                .from("admin_users")
                .update(mapOf("username" to newUsername)) {
                    filter { eq("id", matched.id) }
                }

            android.util.Log.d("AdminAuth", "Username updated successfully")
            Result.success("Username updated successfully")
        }
    } catch (e: Exception) {
        android.util.Log.e("AdminAuth", "adminUpdateUsername failed", e)
        Result.failure(Exception("Username update failed: ${e.message}"))
    }
}

suspend fun adminUpdateEmail(username: String, newEmail: String): Result<String> {
    return try {
        withContext(Dispatchers.IO) {
            android.util.Log.d("AdminAuth", "Updating email for: $username to $newEmail")

            val users = supabase
                .from("admin_users")
                .select()
                .decodeList<AdminUser>()

            val matched = users.find { it.username == username }
            if (matched == null) {
                return@withContext Result.failure(Exception("User not found"))
            }

            if (!newEmail.isNullOrBlank()) {
                val existingEmail = users.find {
                    it.email == newEmail && it.username != username
                }
                if (existingEmail != null) {
                    return@withContext Result.failure(Exception("Email already registered to another account"))
                }
            }

            supabase
                .from("admin_users")
                .update(mapOf("email" to newEmail.ifBlank { null })) {
                    filter { eq("id", matched.id) }
                }

            android.util.Log.d("AdminAuth", "Email updated successfully")
            Result.success("Email updated successfully")
        }
    } catch (e: Exception) {
        android.util.Log.e("AdminAuth", "adminUpdateEmail failed", e)
        Result.failure(Exception("Email update failed: ${e.message}"))
    }
}

suspend fun adminUpdatePhone(username: String, newPhone: String): Result<String> {
    return try {
        withContext(Dispatchers.IO) {
            android.util.Log.d("AdminAuth", "Updating phone for: $username to $newPhone")

            val users = supabase
                .from("admin_users")
                .select()
                .decodeList<AdminUser>()

            val matched = users.find { it.username == username }
            if (matched == null) {
                return@withContext Result.failure(Exception("User not found"))
            }

            if (!newPhone.isNullOrBlank()) {
                val existingPhone = users.find {
                    it.phone == newPhone && it.username != username
                }
                if (existingPhone != null) {
                    return@withContext Result.failure(Exception("Phone number already registered to another account"))
                }
            }

            supabase
                .from("admin_users")
                .update(mapOf("phone" to newPhone.ifBlank { null })) {
                    filter { eq("id", matched.id) }
                }

            android.util.Log.d("AdminAuth", "Phone updated successfully")
            Result.success("Phone updated successfully")
        }
    } catch (e: Exception) {
        android.util.Log.e("AdminAuth", "adminUpdatePhone failed", e)
        Result.failure(Exception("Phone update failed: ${e.message}"))
    }
}
