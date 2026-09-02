package com.example.healt4u.Service

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FarmaTagVerificationService @Inject constructor(
    @ApplicationContext private val context: Context
) {

    sealed class VerificationResult {

        /**
         * The scanned value is a valid KKM MyUBAT/FarmaTag URL.
         *
         * This does NOT mean that we have independently verified
         * the label as genuine. The official KKM service must perform
         * the authenticity check.
         */
        data class ValidFarmaTag(
            val url: String,
            val code: String
        ) : VerificationResult()

        /**
         * The QR looks like a MyUBAT QR but the required code
         * could not be extracted.
         */
        data class InvalidFarmaTag(
            val rawValue: String
        ) : VerificationResult()

        /**
         * The scanned value is not a FarmaTag QR.
         */
        data class NotFarmaTag(
            val rawValue: String
        ) : VerificationResult()

        /**
         * We could not open the official KKM URL.
         */
        data class OpenFailed(
            val url: String
        ) : VerificationResult()
    }

    /**
     * Official KKM MyUBAT domain used by the FarmaTag QR shown
     * in the user's example.
     */
    private val myUbatHost = "myubat.pharmacy.gov.my"

    /**
     * Recognizes a KKM FarmaTag/MyUBAT QR.
     *
     * Example:
     *
     * https://myubat.pharmacy.gov.my/?c=100000000314924948
     */
    fun isFarmaTagQr(value: String): Boolean {
        val uri = parseHttpsUri(value) ?: return false

        if (!uri.host.equals(myUbatHost, ignoreCase = true)) {
            return false
        }

        val code = uri.getQueryParameter("c")

        return !code.isNullOrBlank()
    }

    /**
     * Extract the FarmaTag/MyUBAT code.
     *
     * Example:
     *
     * URL:
     * https://myubat.pharmacy.gov.my/?c=100000000314924948
     *
     * Returns:
     * 100000000314924948
     */
    fun extractFarmaTagCode(value: String): String? {

        val uri = parseHttpsUri(value) ?: return null

        if (!uri.host.equals(myUbatHost, ignoreCase = true)) {
            return null
        }

        val code = uri.getQueryParameter("c")
            ?.trim()

        if (code.isNullOrBlank()) {
            return null
        }

        return code
    }

    /**
     * Normalize and validate a scanned FarmaTag QR.
     */
    fun verifyQr(rawValue: String): VerificationResult {

        val value = rawValue.trim()

        if (value.isBlank()) {
            return VerificationResult.NotFarmaTag(rawValue)
        }

        val uri = parseHttpsUri(value)

        if (uri == null) {
            return VerificationResult.NotFarmaTag(value)
        }

        if (!uri.host.equals(myUbatHost, ignoreCase = true)) {
            return VerificationResult.NotFarmaTag(value)
        }

        val code = uri.getQueryParameter("c")
            ?.trim()

        if (code.isNullOrBlank()) {
            return VerificationResult.InvalidFarmaTag(value)
        }

        /*
         * FarmaTag QR codes currently use the MyUBAT "c" parameter.
         *
         * We deliberately do not convert this code into an MAL number.
         */
        return VerificationResult.ValidFarmaTag(
            url = buildOfficialUrl(code),
            code = code
        )
    }

    /**
     * Build the official KKM URL from the FarmaTag code.
     */
    fun buildOfficialUrl(code: String): String {

        require(code.isNotBlank()) {
            "FarmaTag code cannot be blank"
        }

        return Uri.Builder()
            .scheme("https")
            .authority(myUbatHost)
            .path("/")
            .appendQueryParameter("c", code)
            .build()
            .toString()
    }

    /**
     * Open the official KKM MyUBAT verification page.
     *
     * This is intentionally done through the official KKM
     * service rather than pretending that HTTP 200 means
     * that a FarmaTag is genuine.
     */
    fun openOfficialVerification(
        url: String
    ): VerificationResult {

        return try {

            val uri = parseHttpsUri(url)
                ?: return VerificationResult.OpenFailed(url)

            if (
                !uri.host.equals(
                    myUbatHost,
                    ignoreCase = true
                )
            ) {
                return VerificationResult.OpenFailed(url)
            }

            val intent = Intent(
                Intent.ACTION_VIEW,
                uri
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(intent)

            VerificationResult.ValidFarmaTag(
                url = url,
                code = uri.getQueryParameter("c").orEmpty()
            )

        } catch (e: Exception) {

            Log.e(
                TAG,
                "Unable to open FarmaTag verification URL",
                e
            )

            VerificationResult.OpenFailed(url)
        }
    }

    /**
     * Convenience method:
     *
     * Scan QR → validate → open official KKM page.
     */
    fun verifyAndOpen(
        rawValue: String
    ): VerificationResult {

        val result = verifyQr(rawValue)

        return when (result) {

            is VerificationResult.ValidFarmaTag -> {
                openOfficialVerification(result.url)
            }

            else -> result
        }
    }

    /**
     * Network connectivity is intentionally NOT used as proof
     * of authenticity.
     *
     * A QR resolving successfully only means that the URL exists.
     * Authenticity must come from the KKM verification system.
     */
    suspend fun checkOfficialUrlReachable(
        url: String
    ): Boolean {

        return withContext(Dispatchers.IO) {

            try {

                val uri = parseHttpsUri(url)
                    ?: return@withContext false

                if (
                    !uri.host.equals(
                        myUbatHost,
                        ignoreCase = true
                    )
                ) {
                    return@withContext false
                }

                /*
                 * We deliberately don't perform an unauthenticated
                 * HTTP request here.
                 *
                 * KKM's verification result should come from
                 * the official service/UI.
                 */
                true

            } catch (e: Exception) {

                Log.e(
                    TAG,
                    "FarmaTag URL validation failed",
                    e
                )

                false
            }
        }
    }

    private fun parseHttpsUri(
        value: String
    ): Uri? {

        return try {

            val uri = Uri.parse(value)

            if (
                !uri.scheme.equals(
                    "https",
                    ignoreCase = true
                )
            ) {
                return null
            }

            uri

        } catch (_: Exception) {
            null
        }
    }

    companion object {

        private const val TAG =
            "FarmaTagVerification"
    }
}
