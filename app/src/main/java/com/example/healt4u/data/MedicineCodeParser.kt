package com.example.healt4u.data

import android.net.Uri

object MedicineCodeParser {

    private val malRegex =
        Regex(
            pattern = """\bMAL\s*\d{6,12}[A-Z0-9]{0,6}\b""",
            option = RegexOption.IGNORE_CASE
        )

    private val questHosts = setOf(
        "quest3plus.bpfk.gov.my",
        "quest3plus.npra.gov.my"
    )

    fun extractMal(value: String): String? {

        val match = malRegex.find(value)
            ?: return null

        return normalizeMal(match.value)
    }

    fun normalizeMal(value: String): String {

        return value
            .trim()
            .uppercase()
            .replace("\\s+".toRegex(), "")
    }

    fun isQuest3PlusUrl(value: String): Boolean {

        return try {

            val uri = Uri.parse(value)

            val scheme = uri.scheme?.lowercase()
            val host = uri.host?.lowercase()

            scheme == "https" &&
                    host in questHosts

        } catch (_: Exception) {

            false
        }
    }

    fun extractMalFromQuest3PlusUrl(
        value: String
    ): String? {

        // First search the complete URL.
        extractMal(value)?.let {
            return it
        }

        return try {

            val uri = Uri.parse(value)

            val parameterNames = listOf(
                "id",
                "mal",
                "regno",
                "registration",
                "registration_no",
                "registrationNumber"
            )

            for (name in parameterNames) {

                val parameter =
                    uri.getQueryParameter(name)

                if (!parameter.isNullOrBlank()) {

                    extractMal(parameter)?.let {
                        return it
                    }

                    if (
                        parameter
                            .uppercase()
                            .startsWith("MAL")
                    ) {
                        return normalizeMal(parameter)
                    }
                }
            }

            null

        } catch (_: Exception) {

            null
        }
    }

    fun isBarcode(value: String): Boolean {

        val clean = value.trim()

        return clean.all { it.isDigit() } &&
                clean.length in 8..14
    }

    fun normalizeBarcode(value: String): String {

        return value
            .trim()
            .filter { it.isDigit() }
    }
}