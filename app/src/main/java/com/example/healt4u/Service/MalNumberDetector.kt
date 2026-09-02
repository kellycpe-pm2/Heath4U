package com.example.healt4u.Service

object MalNumberDetector {

    private val malRegex = Regex(
        """\bMAL\s*\d{6,14}\s*[A-Z]{0,3}\b""",
        RegexOption.IGNORE_CASE
    )

    fun extractMalNumber(text: String): String? {

        val match = malRegex.find(text)

        return match
            ?.value
            ?.replace(Regex("\\s+"), "")
            ?.uppercase()
    }
}