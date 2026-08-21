package com.example.healt4u.Storage

import com.example.healt4u.model.Medicine
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import io.ktor.client.engine.android.Android
import kotlinx.coroutines.Dispatchers
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
    install(Auth)

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



suspend fun updateMedicineQuantity(
    id: Int,
    newQuantity: Int
): Boolean {

    return try {

        if (newQuantity < 0) {

            return false
        }


        val updateData =
            mapOf(
                "quantity_left" to newQuantity
            )


        withContext(Dispatchers.IO) {

            supabase
                .from("medicine")
                .update(updateData) {

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

        e.printStackTrace()

        false
    }
}


suspend fun deleteMedicine(
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

        e.printStackTrace()

        false
    }
}