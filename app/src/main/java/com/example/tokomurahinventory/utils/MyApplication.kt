package com.example.tokomurahinventory.utils

import android.app.Application
import android.preference.PreferenceManager
import androidx.work.ExistingWorkPolicy

import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import androidx.work.PeriodicWorkRequestBuilder


class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        val wmbPreference = PreferenceManager.getDefaultSharedPreferences(this)

        val isFirstRun = wmbPreference.getBoolean("FIRSTRUN", true)

        if (isFirstRun) {
            scheduleOneTimeUpdateMerkWarna()
            markFirstRunCompleted() // Set the flag after scheduling
        }
        // Initialize WorkManager

        schedulePeriodicCleanup()
    }
    private fun markFirstRunCompleted() {
        val wmbPreference = PreferenceManager.getDefaultSharedPreferences(this)
        wmbPreference.edit().putBoolean("FIRSTRUN", false).apply()
    }

    private fun scheduleOneTimeUpdateMerkWarna() {
        val workManager = WorkManager.getInstance(this)

        // Create a OneTimeWorkRequest for the worker
        val workRequest = OneTimeWorkRequestBuilder<TrimKodeWarnaWorker>()
            .setInitialDelay(1, TimeUnit.SECONDS) // Optional: Adjust delay if needed
            .build()

        // Enqueue unique work, ensuring it runs only once
        workManager.enqueueUniqueWork(
            "TrimKodeWarnaWorker",
            ExistingWorkPolicy.KEEP, // This ensures the worker runs only once
            workRequest
        )
    }


    private fun scheduleOneTimeUpdateDetailWarna() {
        val workRequest = OneTimeWorkRequestBuilder<UpdateDetailWarnaWorker>()
            .setInitialDelay(1, TimeUnit.MINUTES) // Adjust the delay as needed
            .build()
        WorkManager.getInstance(this)
            .enqueue(workRequest)
    }

    private fun scheduleOneTimeUpdateLog() {
        val workRequest = OneTimeWorkRequestBuilder<UpdateLogMerkStringWorker>()
            .setInitialDelay(3, TimeUnit.SECONDS) // Adjust the delay as needed
            .build()
        WorkManager.getInstance(this)
            .enqueue(workRequest)
    }
    fun scheduleWorkerAgain() {
        val workRequest = OneTimeWorkRequestBuilder<UpdateLogMerkStringWorker>()
            .setInitialDelay(3, TimeUnit.MINUTES) // No delay for immediate execution
            .build()

        WorkManager.getInstance(this)
            .enqueue(workRequest)
    }
    private fun scheduleOneTimeUpdateLastEditedDetailWarna() {
        val workRequest = OneTimeWorkRequestBuilder<UpdateLastEditedWorker>()
            .setInitialDelay(15, TimeUnit.SECONDS) // Adjust the delay as needed
            .build()
        WorkManager.getInstance(this)
            .enqueue(workRequest)
    }

    private fun scheduleOneTimeUpdateLastEditedDateDetailWarna() {
        val workRequest = OneTimeWorkRequestBuilder<UpdateLastEditedWorker>()
            .setInitialDelay(15, TimeUnit.SECONDS) // Adjust the delay as needed
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