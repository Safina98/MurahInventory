package com.example.tokomurahinventory.utils

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.tokomurahinventory.database.DatabaseInventory
import java.util.Calendar
import java.util.Date

class CleanupWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {
        // Get the database instance
        val database = DatabaseInventory.getInstance(applicationContext)
        val logDao = database.logDao

        // Get the current time
        val now = Date()

        // Set the cutoff date to 5 seconds ago
        val calendar = Calendar.getInstance()
        calendar.time = now
        calendar.add(Calendar.SECOND, -5)
        val cutoffDate = calendar.time

        // Perform the cleanup operation
        logDao.deleteLogsBefore(cutoffDate)

        return Result.success()
    }
}
