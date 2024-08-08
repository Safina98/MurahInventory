package com.example.tokomurahinventory.utils

import android.app.Application

import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import androidx.work.PeriodicWorkRequestBuilder


class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize WorkManager


        // Schedule the periodic cleanup
       // schedulePeriodicCleanup()
        //scheduleOneTimeCleanup()
        schedulePeriodicCleanup()
    }
    private fun scheduleOneTimeCleanup() {
        val workRequest = OneTimeWorkRequestBuilder<CleanupWorker>()
            .setInitialDelay(1, TimeUnit.MINUTES) // Delay to start after 5 minutes
            .build()
        WorkManager.getInstance(this)
            .enqueue(workRequest)
    }
    private fun schedulePeriodicCleanup() {
        val workRequest = PeriodicWorkRequestBuilder<CleanupWorker>(1, TimeUnit.DAYS)
            .build()
        WorkManager.getInstance(this)
            .enqueue(workRequest)
    }
}