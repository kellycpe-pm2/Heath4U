package com.example.healt4u

import android.app.Application
import android.util.Log
import com.example.healt4u.Service.BarcodeLookUpService
import com.example.healt4u.Service.NPRADataService
import com.example.healt4u.Service.OpenFDAService
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltAndroidApp
class Health4UApplication : Application() {

    @Inject
    lateinit var dataService: NPRADataService

    @Inject
    lateinit var openFdaService: OpenFDAService

    @Inject
    lateinit var barcodeLookupService: BarcodeLookUpService

    override fun onCreate() {
        super.onCreate()

        CoroutineScope(Dispatchers.IO).launch {
            // 1. NPRA 数据
            try {
                val count = dataService.fetchAllMedicines().size
            } catch (e: Exception) {
            }

        }
    }
}
