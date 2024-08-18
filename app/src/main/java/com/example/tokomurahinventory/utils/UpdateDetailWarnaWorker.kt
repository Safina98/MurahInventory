package com.example.tokomurahinventory.utils

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.tokomurahinventory.database.DatabaseInventory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class UpdateDetailWarnaWorker(
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
    // select all data from detail warna table
    // select all from barang log table
    // for i in detail warnatabe
    //for j in baranglog table
    //if i.detail warna ref == j. detail warna ref
    //update detail warna dikeluarkan oleh

    private suspend fun performUpdateNew()= withContext(Dispatchers.IO){
        val dataSourceLog = DatabaseInventory.getInstance(applicationContext).logDao
        val dataSourceBarangLog = DatabaseInventory.getInstance(applicationContext).barangLogDao
        val dataSourceDetailWarna = DatabaseInventory.getInstance(applicationContext).detailWarnaDao
        val allDetailWarna = dataSourceDetailWarna.getAllDetailWarnas()
        for(dw in allDetailWarna){
            dw.lastEditedBy=null
            dataSourceDetailWarna.update(dw)
        }
        val allLogKeluar = dataSourceLog.getLogsByDateRange(null, null, MASUKKELUAR.KELUAR)
        for (log in allLogKeluar) {
            val barangLogList = dataSourceBarangLog.selectBarangLogByLogRef(log.refLog)
            for (bl in barangLogList) {
                dataSourceDetailWarna.updateLastEditedByDetailWarna(bl.detailWarnaRef, log.lastEditedBy)
            }
            Log.i("DetailDateProbs", "barang log size ${barangLogList.size}")
        }
    }

    private suspend fun performUpdate() = withContext(Dispatchers.IO) {
        Log.i("WorkerProbs", "performUpdate in UpdateDetailWarnaWorker")
        val dataSourceLog = DatabaseInventory.getInstance(applicationContext).logDao
        val dataSourceBarangLog = DatabaseInventory.getInstance(applicationContext).barangLogDao
        val dataSourceDetailWarna = DatabaseInventory.getInstance(applicationContext).detailWarnaDao
        val allLogKeluar = dataSourceLog.getLogsByDateRange(null, null, MASUKKELUAR.KELUAR)
        Log.i("DetailDateProbs", "log size ${allLogKeluar.size}")
        for (log in allLogKeluar) {
            val barangLogList = dataSourceBarangLog.selectBarangLogByLogRef(log.refLog)
            for (bl in barangLogList) {
                dataSourceDetailWarna.updateOutDateDetailWarna(bl.detailWarnaRef, log.logLastEditedDate)
            }
            Log.i("DetailDateProbs", "barang log size ${barangLogList.size}")
        }
    }
}
