package com.example.tokomurahinventory.utils

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

class AppLifecycleObserver(private val logout: () -> Unit) : DefaultLifecycleObserver {

    private val handler = Handler(Looper.getMainLooper())
    private val logoutRunnable = Runnable {
        Log.d("AppDebug", "App has been in background for $LOGOUT_DELAY ms, logging out.")
        logout()
    }

    override fun onStop(owner: LifecycleOwner) {
        Log.d("AppDebug", "App is going to background.")
        handler.postDelayed(logoutRunnable, LOGOUT_DELAY)
    }

    override fun onStart(owner: LifecycleOwner) {
        Log.d("AppDebug", "App is coming to foreground.")
        handler.removeCallbacks(logoutRunnable)
    }

    companion object {
        const val LOGOUT_DELAY = 1 * 3 * 1000L // 5 minutes delay in milliseconds
    }
}
