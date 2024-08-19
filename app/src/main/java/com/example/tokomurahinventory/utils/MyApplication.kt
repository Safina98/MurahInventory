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
        //WorkManager.initialize(this, WorkManagerInitializer.getConfiguration(this))
        //scheduleOneTimeUpdateLastEditedDetailWarna()
        // Schedule the one-time worker to update detail warna
       //scheduleOneTimeUpdateDetailWarna()
        //scheduleOneTimeUpdateLog()
        //scheduleWorkerAgain()
        schedulePeriodicCleanup()
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
            .setInitialDelay(15, TimeUnit.SECONDS) // Adjust the delay as needed
            .build()
        WorkManager.getInstance(this)
            .enqueue(workRequest)
    }
    fun scheduleWorkerAgain() {
        val workRequest = OneTimeWorkRequestBuilder<UpdateLogMerkStringWorker>()
            .setInitialDelay(0, TimeUnit.SECONDS) // No delay for immediate execution
            .build()

        WorkManager.getInstance(this)
            .enqueue(workRequest)
    }
    private fun scheduleOneTimeUpdateLastEditedDetailWarna() {
        val workRequest = OneTimeWorkRequestBuilder<UpdateLastEditedWorker>()
            .setInitialDelay(5, TimeUnit.SECONDS) // Adjust the delay as needed
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