package com.example.tokomurahinventory

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.tokomurahinventory.utils.AppLifecycleObserver
import com.example.tokomurahinventory.utils.LogoutManager
import com.example.tokomurahinventory.utils.LogoutReceiver

class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()

         LogoutManager.onLogout = {
            val intent = Intent(this, LogoutReceiver::class.java)
            sendBroadcast(intent)
        }

        val appLifecycleObserver = AppLifecycleObserver {
            LogoutManager.performLogout()
        }
        ProcessLifecycleOwner.get().lifecycle.addObserver(appLifecycleObserver)
    }
}
