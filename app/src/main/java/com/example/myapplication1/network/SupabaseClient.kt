package com.example.myapplication1.network

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime

object SupabaseClient {
    private const val SUPABASE_URL = "https://zqmsvpaodvpymynbirgw.supabase.co"
    private const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InpxbXN2cGFvZHZweW15bmJpcmd3Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzY5NzU4MjcsImV4cCI6MjA5MjU1MTgyN30.gS8LnU9nB9nyFT53NqAz0hRgg9Uuklp0Ou6yqHDkZBQ"

    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_ANON_KEY
    ) {
        install(Auth)
        install(Postgrest)
        install(Realtime)
    }
}
