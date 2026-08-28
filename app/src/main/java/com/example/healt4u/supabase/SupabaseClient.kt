package com.example.healt4u.supabase

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.auth.Auth

object SupabaseClient {

    private const val SUPABASE_URL = "https://jotudzheiwopavprryxx.supabase.co"

    private const val SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImpvdHVkemhlaXdvcGF2cHJyeXh4Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODY1NDU1ODgsImV4cCI6MjEwMjEyMTU4OH0.Q4R0_c94lxfUKcMTVoIOdhilsDA6YfffQt7-dNoA1zM"

    val supabase = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_KEY
    ) {
        install(Postgrest)
        install(Realtime)
        install(Auth)
    }
}