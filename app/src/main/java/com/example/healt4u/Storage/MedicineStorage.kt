package com.example.healt4u.Storage

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.example.healt4u.model.Medicine
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale.filter
import kotlin.collections.mapOf

@Composable
fun MedicineStorage() {

    val supabaseURL = "https://jotudzheiwopavprryxx.supabase.co/rest/v1/"

    val supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImpvdHVkemhlaXdvcGF2cHJyeXh4Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODY1NDU1ODgsImV4cCI6MjEwMjEyMTU4OH0.Q4R0_c94lxfUKcMTVoIOdhilsDA6YfffQt7-dNoA1zM"
    val supabase = createSupabaseClient(supabaseURL,supabaseKey){

    }
}
val supabaseURL = "https://jotudzheiwopavprryxx.supabase.co/rest/v1/"

val supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImpvdHVkemhlaXdvcGF2cHJyeXh4Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODY1NDU1ODgsImV4cCI6MjEwMjEyMTU4OH0.Q4R0_c94lxfUKcMTVoIOdhilsDA6YfffQt7-dNoA1zM"
val supabase = createSupabaseClient(supabaseURL,supabaseKey){
    install(Postgrest)

}
@Composable
fun Med_Retrieve(){
    val meds = remember { mutableListOf<Medicine>() }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO){
            val result = supabase.from("Medicine").select().decodeList<Medicine>()
            meds.addAll(result)
        }
    }
}
@SuppressLint("CoroutineCreationDuringComposition")
suspend fun insertSingleMedicine(medicine: Medicine): Boolean {
    return try {
        supabase.from("Medicine")
            .insert(medicine)
            .decodeSingle<Medicine>()
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}


@SuppressLint("CoroutineCreationDuringComposition")
suspend fun deleteMedicine(medicineId: Int): Boolean {

    return try {
        supabase.from("Medicine")
            .delete() {
                filter {
                    eq("id", medicineId)
                }
            }.decodeList<Medicine>()
        true
    } catch (e: Exception) {
        false
    }
}