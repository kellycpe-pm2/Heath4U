package com.example.healt4u.Storage

import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.random.Random

// A generic 6-digit OTP, keyed by whatever identifier the caller uses
// (an email address or phone number) — not tied to a specific role, so both
// the Patient and Admin forgot-password flows can share it.
@Serializable
private data class OtpCode(
    val identifier: String,
    val code: String,
    @SerialName("expires_at")
    val expiresAt: Long,
    val verified: Boolean = false
)

private const val OTP_VALID_MINUTES = 5

// Generates a 6-digit code, stores it (5 min expiry), and "sends" it.
// NOTE: there's no real SMS/email gateway wired into this project (that would
// need a paid provider like Twilio/SendGrid and its own API keys), so for this
// assignment the code is returned directly in the Result instead of actually
// being delivered — the UI shows it in a "demo mode" banner so the full
// request -> verify -> reset flow can still be demonstrated end-to-end.
suspend fun requestOtp(identifier: String): Result<String> {
    return try {
        withContext(Dispatchers.IO) {
            val code = Random.nextInt(100000, 999999).toString()
            val expiresAt = System.currentTimeMillis() + OTP_VALID_MINUTES * 60 * 1000

            supabase
                .from("otp_codes")
                .upsert(
                    OtpCode(identifier = identifier, code = code, expiresAt = expiresAt)
                ) {
                    onConflict = "identifier"
                }

            Result.success(code)
        }
    } catch (e: Exception) {
        Result.failure(Exception("Failed to send OTP: ${e.message}"))
    }
}

suspend fun verifyOtp(identifier: String, enteredCode: String): Result<Unit> {
    return try {
        withContext(Dispatchers.IO) {
            val matches = supabase
                .from("otp_codes")
                .select {
                    filter { eq("identifier", identifier) }
                }
                .decodeList<OtpCode>()

            val record = matches.firstOrNull()
                ?: return@withContext Result.failure(Exception("No OTP was requested for this account"))

            if (System.currentTimeMillis() > record.expiresAt) {
                return@withContext Result.failure(Exception("This OTP has expired — please request a new one"))
            }

            if (record.code != enteredCode.trim()) {
                return@withContext Result.failure(Exception("Incorrect OTP code"))
            }

            supabase
                .from("otp_codes")
                .update(mapOf("verified" to true)) {
                    filter { eq("identifier", identifier) }
                }

            Result.success(Unit)
        }
    } catch (e: Exception) {
        Result.failure(Exception("OTP verification failed: ${e.message}"))
    }
}

/*
Run once in the Supabase SQL editor for this project:

create table if not exists otp_codes (
    identifier text primary key,
    code text not null,
    expires_at bigint not null,
    verified boolean not null default false
);
*/
