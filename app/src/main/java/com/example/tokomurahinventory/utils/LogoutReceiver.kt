package com.example.tokomurahinventory.utils

import android.app.Activity
import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.tokomurahinventory.MainActivity

class LogoutReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (context is Application) {
            Log.d("AppDebug", "Context is Application.")
            LogoutManager.performLogout()  // Use central logout logic
        } else {
            Log.d("AppDebug", "Context is not Application or MainActivity, handling logout in else.")
            // Handle other cases or use a different method to trigger logout
            // This block might be redundant if context cannot be MainActivity
        }
    }
}