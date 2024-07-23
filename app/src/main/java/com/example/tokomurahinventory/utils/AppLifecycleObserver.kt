package com.example.tokomurahinventory.utils


import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

import android.os.Handler
import android.os.Looper
import com.example.tokomurahinventory.MainActivity

class AppLifecycleObserver(private val mainActivity: MainActivity) : DefaultLifecycleObserver {
    private val handler = Handler(Looper.getMainLooper())
    private val logoutRunnable = Runnable {
        mainActivity.logout()
    }
    private val logoutDelayMillis: Long = 10000 // Delay in milliseconds (10 seconds)

    override fun onStop(owner: LifecycleOwner) {
        // Schedule the logout after the specified delay
        handler.postDelayed(logoutRunnable, logoutDelayMillis)
    }

    override fun onStart(owner: LifecycleOwner) {
        // Cancel the logout if the app comes back to the foreground
        handler.removeCallbacks(logoutRunnable)
    }
}
