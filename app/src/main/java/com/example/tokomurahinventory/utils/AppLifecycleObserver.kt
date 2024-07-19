package com.example.tokomurahinventory.utils

import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.example.tokomurahinventory.MainActivity

class AppLifecycleObserver(private val mainActivity: MainActivity) : DefaultLifecycleObserver {
    private val handler = Handler(Looper.getMainLooper())
    private val logoutRunnable = Runnable {
        mainActivity.logout()
    }
    private val logoutDelayMillis: Long = 5000 // Delay in milliseconds (5 seconds)

    override fun onStop(owner: LifecycleOwner) {
        // Schedule the logout after the specified delay
        handler.postDelayed(logoutRunnable, logoutDelayMillis)
    }

    override fun onStart(owner: LifecycleOwner) {
        // Cancel the logout if the app comes back to the foreground
        handler.removeCallbacks(logoutRunnable)
    }
}
