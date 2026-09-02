package com.example.healt4u

import android.app.Application
import android.util.Log
import com.example.healt4u.Service.NPRADataService
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Main Application class for Health4U.
 *
 * This class is annotated with @HiltAndroidApp to trigger Hilt's code generation,
 * which is required for dependency injection to work throughout the app.
 *
 * It also performs a background fetch of NPRA medicine data on startup to
 * ensure the cache is warmed up for the user.
 */
@HiltAndroidApp
class Health4UApplication : Application() {

    @Inject
    lateinit var dataService: NPRADataService

    override fun onCreate() {
        super.onCreate()

        // Pre-warm the NPRA medicine cache in the background
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val count = dataService.fetchAllMedicines().size
                Log.d("Health4UApplication", "Loaded $count medicines from NPRA cache/source.")
            } catch (e: Exception) {
                Log.e("Health4UApplication", "Initial NPRA data load failed", e)
            }
        }
    }
}
