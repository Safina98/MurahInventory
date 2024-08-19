package com.example.tokomurahinventory.utils

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.tokomurahinventory.database.DatabaseInventory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.util.Locale

class UpdateLogMerkStringWorker(
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

    private suspend fun performUpdate() = withContext(Dispatchers.IO) {
        val dataSourceLog = DatabaseInventory.getInstance(applicationContext).logDao
        val dataSourceBarangLog = DatabaseInventory.getInstance(applicationContext).barangLogDao
        val dataSourceDetailWarna = DatabaseInventory.getInstance(applicationContext).detailWarnaDao
        val dataSourceWarna = DatabaseInventory.getInstance(applicationContext).warnaDao
        val dataSourceMerk = DatabaseInventory.getInstance(applicationContext).merkDao

        val allLogMasuk = dataSourceLog.selectAllLogListWithFilters(MASUKKELUAR.MASUK, null, null)
        Log.i("AllTransProbs", "Log size: ${allLogMasuk.size}")

        for (log in allLogMasuk) {
            Log.i("AllTransProbs", "loop start")
            val barangLog = dataSourceBarangLog.selectBarangLogByLogRef(log.refLog)
            if (barangLog.isNullOrEmpty()) {
                Log.w("AllTransProbs", "No barangLog found for refLog: ${log.refLog}")
                continue
            }

            val detailWarna = dataSourceDetailWarna.getDetailWarnaByDetailWarnaRef(barangLog[0].detailWarnaRef ?: "")
            if (detailWarna == null) {
                Log.w("AllTransProbs", "No detailWarna found for ref: ${barangLog[0].detailWarnaRef}")
                continue
            }

            val warnaTable = dataSourceWarna.getWarnaTableByRef(detailWarna.warnaRef)
            if (warnaTable == null) {
                Log.w("AllTransProbs", "No warnaTable found for ref: ${detailWarna.detailWarnaRef}")
                continue
            }

            val merk = dataSourceMerk.getMerkNameByRef(warnaTable.refMerk)
            val newMerkString = getStringS(merk, warnaTable.kodeWarna, detailWarna.detailWarnaIsi, warnaTable.satuan, detailWarna.detailWarnaPcs)

            log.merk = newMerkString
            Log.i("AllTransProbs", "Updating log: ${log.id}, new merk: $newMerkString")

            dataSourceLog.update(log)
        }

        Log.i("WorkerProbs", "Finished updating logs.")
    }

    fun getStringS(merk:String?,kodeWarna:String?,isi: Double?,satuan: String?,pcs: Int?):String{
        var s ="${merk} kode ${kodeWarna};  isi ${String.format(Locale.US,"%.2f",isi)} ${satuan}; ${pcs} pcs\n"
        return s
    }


}