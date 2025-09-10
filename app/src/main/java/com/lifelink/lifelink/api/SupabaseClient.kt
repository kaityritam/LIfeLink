package com.lifelink.lifelink.api

import android.content.Context
import com.lifelink.lifelink.BuildConfig
import com.lifelink.lifelink.api.SupabaseClient.client
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest

object SupabaseClient {

    lateinit var client: SupabaseClient
        private set

    fun initialize(context: Context) {
        if (::client.isInitialized) return

        client = createSupabaseClient(
                supabaseUrl = BuildConfig.PROJECT_URL,
                supabaseKey = BuildConfig.PROJECT_ANON_KEY
            ) {
                install(Postgrest)
                install(Auth) {
                    autoLoadFromStorage = true
                }
            }
        }

}

