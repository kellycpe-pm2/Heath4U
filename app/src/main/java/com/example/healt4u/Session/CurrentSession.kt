package com.example.healt4u.Session

// Tracks which patient is currently logged in on this device, so local JSON
// caches (medicines, reminder logs) and cloud queries can be scoped to just
// that account instead of one shared file for every patient who's ever used
// this device. Set on login/switch-account, reset on logout.
object CurrentSession {
    @Volatile
    var patientId: Int = 0
}
