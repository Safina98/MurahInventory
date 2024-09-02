package com.example.tokomurahinventory.utils

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.tokomurahinventory.database.DatabaseInventory
import java.util.Collections.emptyList

class TrimKodeWarnaWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        Log.i("WorkerProbs", "TrimKodeWarnaWorker starting")
        return try {
            performUpdate() // Directly call the suspend function
            Log.i("WorkerProbs", "Finnised")
            Result.success()

        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }

    private suspend fun performUpdate() {
        val database = DatabaseInventory.getInstance(applicationContext)
        val warnaDao = database.warnaDao
        val merkDao = database.merkDao

        // Fetch all WarnaTable entries
        val allWarna = warnaDao.getAllWarna() ?: emptyList()
        val allMerk = merkDao.selectAllMerkList()

        // Update namaMerk with trimmed values
        allMerk.forEach { merk ->
            val trimmedMerk = merk.namaMerk.trim()
            if (trimmedMerk != merk.namaMerk) {
                merk.namaMerk = trimmedMerk
                merkDao.update(merk) // Update the merk entry in the database
            }
        }
        Log.i("WorkerProbs", "finnished updating merk")
        // Update kodeWarna with trimmed values
        allWarna.forEach { warna ->
            val trimmedKodeWarna = warna.kodeWarna.trim() // Trim the kodeWarna string
            if (trimmedKodeWarna != warna.kodeWarna) {
                warna.kodeWarna = trimmedKodeWarna
                warnaDao.update(warna) // Update the warna entry in the database
            }
        }
        Log.i("WorkerProbs", "finnished updating warna")
    }
}
