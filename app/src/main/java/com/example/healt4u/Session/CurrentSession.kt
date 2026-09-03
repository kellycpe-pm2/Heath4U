package com.example.healt4u.Session

import android.content.Context
import android.content.SharedPreferences

/**
 * Tracks which user is currently logged in on this device.
 * Persists login state across app restarts using SharedPreferences.
 */
object CurrentSession {
    private const val PREF_NAME = "Health4USession"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USER_ROLE = "user_role"
    private const val KEY_USER_NAME = "user_name"
    private const val KEY_USER_PHONE = "user_phone"
    private const val KEY_IS_LOGGED_IN = "is_logged_in"

    @Volatile
    var patientId: Int = 0
        set(value) {
            field = value
        }

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveSession(
        context: Context,
        userId: Int,
        role: String,
        name: String = "",
        phone: String = ""
    ) {
        val editor = getPrefs(context).edit()
        editor.putInt(KEY_USER_ID, userId)
        editor.putString(KEY_USER_ROLE, role)
        editor.putString(KEY_USER_NAME, name)
        editor.putString(KEY_USER_PHONE, phone)
        editor.putBoolean(KEY_IS_LOGGED_IN, true)
        editor.apply()
        
        patientId = userId
    }

    fun clearSession(context: Context) {
        getPrefs(context).edit().clear().apply()
        patientId = 0
    }

    fun isLoggedIn(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun getUserId(context: Context): Int {
        return getPrefs(context).getInt(KEY_USER_ID, 0)
    }

    fun getUserRole(context: Context): String {
        return getPrefs(context).getString(KEY_USER_ROLE, "patient") ?: "patient"
    }

    fun getUserName(context: Context): String {
        return getPrefs(context).getString(KEY_USER_NAME, "") ?: ""
    }

    fun getUserPhone(context: Context): String {
        return getPrefs(context).getString(KEY_USER_PHONE, "") ?: ""
    }
}
