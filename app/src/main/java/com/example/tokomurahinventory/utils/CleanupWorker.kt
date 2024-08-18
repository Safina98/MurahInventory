package com.example.tokomurahinventory.utils

import android.content.Context
import android.util.Log
import androidx.lifecycle.viewModelScope
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.tokomurahinventory.database.DatabaseInventory
import com.example.tokomurahinventory.database.LogDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Date

class CleanupWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        Log.i("WorkerProbs","staring")
        return try {
            // Run the suspend function inside a coroutine scope
            runBlocking {
                Log.i("WorkerProbs","run blocking")
                performCleanup()
            }
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }

    private suspend fun performCleanup() = withContext(Dispatchers.IO) {
        Log.i("WorkerProbs","peformCleanup")
        val dao = getDao()
        val cutoffDate = getOneYearAgoDate()
        dao.deleteRecordsOlderThan(cutoffDate)
    }

    private fun getOneYearAgoDate(): Date {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.YEAR, -1)
        Log.i("WorkerProbs","oneYear ago date: ${calendar.time}")
        return calendar.time
    }

    private fun getDao(): LogDao {
        // Obtain your DAO instance from the database
        return DatabaseInventory.getInstance(applicationContext).logDao
    }


}
