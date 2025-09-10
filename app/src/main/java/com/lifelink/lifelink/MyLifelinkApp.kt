package com.lifelink.lifelink

import android.app.Application
import com.lifelink.lifelink.api.SupabaseClient

class MyLifelinkApp : Application() {

    override fun onCreate() {
        super.onCreate()
        SupabaseClient.initialize(this)
    }
}