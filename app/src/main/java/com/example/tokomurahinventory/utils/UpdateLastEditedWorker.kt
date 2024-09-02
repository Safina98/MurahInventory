package com.example.tokomurahinventory.utils

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.tokomurahinventory.database.DatabaseInventory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class UpdateLastEditedWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        Log.i("WorkerProbs", "UpdateDetailWarnaWorker starting")
        return try {
            // Run the suspend function inside a coroutine scope
            runBlocking {
                performUpdate()
            }
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }

    private suspend fun performUpdate()= withContext(Dispatchers.IO){
        val dataSourceLog = DatabaseInventory.getInstance(applicationContext).logDao
        val dataSourceBarangLog = DatabaseInventory.getInstance(applicationContext).barangLogDao
        val dataSourceDetailWarna = DatabaseInventory.getInstance(applicationContext).detailWarnaDao
        Log.i("WorkerProbs", "function perform update called")
        dataSourceDetailWarna.updateDateOutNotInKeluar(null)


    }




}
