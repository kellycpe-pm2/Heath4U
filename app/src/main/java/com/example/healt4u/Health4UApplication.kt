package com.example.healt4u

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

// Required for Hilt's dependency injection to work at all (MainActivity is
// @AndroidEntryPoint, and Service/AppModule.kt provides dependencies through
// it). AndroidManifest.xml references this class by name — without it here,
// the app fails to build/launch even though lint was suppressed with
// tools:ignore="MissingClass".
@HiltAndroidApp
class Health4UApplication : Application()
