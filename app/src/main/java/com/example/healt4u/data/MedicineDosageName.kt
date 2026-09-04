package com.example.healt4u.data
data class MedicineNameDosage(
    val name: String,
    val dosage: Int
)

object MedicineNameParser {

    private val dosageRegex =
        Regex(
            """\b\d+(?:\.\d+)?\s*(mg|mcg|g|ml)\b""",
            RegexOption.IGNORE_CASE
        )

    private val dosageFormRegex =
        Regex(
            """\b(
                tablet|
                tablets|
                tab|
                tabs|
                capsule|
                capsules|
                cap|
                caps|
                syrup|
                solution|
                suspension|
                cream|
                ointment|
                gel|
                drops|
                spray|
                injection
            )\b""",
            setOf(
                RegexOption.IGNORE_CASE,
                RegexOption.COMMENTS
            )
        )

    fun parse(
        productName: String,
        npraDosage: Int? = null
    ): MedicineNameDosage {

        val original =
            productName
                .trim()
                .replace(
                    Regex("\\s+"),
                    " "
                )

        val dosageMatch =
            dosageRegex.find(original)

        val dosage =
            if (dosageMatch != null) {

                val number =
                    dosageMatch
                        .value
                        .filter {
                            it.isDigit() ||
                                    it == '.'
                        }
                        .toDoubleOrNull()

                val unit =
                    dosageMatch
                        .groupValues[1]
                        .lowercase()

                when (unit) {

                    "g" ->
                        number
                            ?.times(1000)
                            ?.toInt()

                    "mcg" ->
                        number
                            ?.div(1000)
                            ?.toInt()

                    else ->
                        number?.toInt()
                }

            } else {

                npraDosage
            }

        var name =
            original

        /*
         * Remove dosage.
         */

        name =
            name.replace(
                dosageRegex,
                ""
            )

        /*
         * Remove common dosage forms.
         */

        name =
            name.replace(
                dosageFormRegex,
                ""
            )

        /*
         * Clean spacing.
         */

        name =
            name
                .replace(
                    Regex("\\s+"),
                    " "
                )
                .trim(
                    ' ',
                    '-',
                    '/',
                    ','
                )

        return MedicineNameDosage(

            name =
                name.ifBlank {
                    original
                },

            dosage =
                dosage ?: 0
        )
    }
}