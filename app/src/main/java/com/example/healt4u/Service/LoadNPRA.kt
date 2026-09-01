package com.example.healt4u

import android.app.Application
import android.util.Log
import com.example.healt4u.Service.NPRADataService
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class Health4UApplication : Application() {

    @Inject
    lateinit var dataService: NPRADataService

    override fun onCreate() {
        super.onCreate()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val count = dataService.fetchAllMedicines().size
            } catch (e: Exception) {
            }
        }
    }
}