package com.example.tokomurahinventory.utils

import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean

class SingleLiveEvent<T> : MutableLiveData<T>() {
    private val pending = AtomicBoolean(false)

    override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
        super.observe(owner, Observer<T> { t ->
            if (pending.compareAndSet(true, false)) {
                observer.onChanged(t)
            }
        })
    }

    override fun setValue(t: T?) {
        pending.set(true)
        super.setValue(t)
    }

    fun call() {
        value = null
    }
}
/*
Log.i("InsertLogTry", "Update Detail Warna called")
val loggedInUsers = SharedPreferencesHelper.getLoggedInUser(getApplication())
Log.i("InsertLogTry", "loggedInUsers: $loggedInUsers")
try {
    val oldBarangLog = withContext(Dispatchers.IO) {
        dataSourceBarangLog.selectBarangLogByRef(newBarangLog.barangLogRef)
    }
    val selisihPcs: Int
    if (oldBarangLog != null) {
        Log.i("InsertLogTry", "old barang log != null")
        Log.i("InsertLogTry", "old log barang ref ${oldBarangLog.warnaRef} new log barang ref=${newBarangLog.warnaRef}")
        if (oldBarangLog.warnaRef == newBarangLog.warnaRef) {
            Log.i("InsertLogTry", "old log barang ref == new barang ref")
            if (oldBarangLog.isi == newBarangLog.isi) {
                Log.i("InsertLogTry", "old log barang isi ${oldBarangLog.isi} new log barang isi=${newBarangLog.isi}")
                selisihPcs = newBarangLog.pcs - oldBarangLog.pcs
                Log.i("InsertLogTry", "old barang pcs - new barang pcs  ${oldBarangLog.pcs} - ${newBarangLog.pcs} = ${selisihPcs}")
                updateDetailWarnaTODao(newBarangLog.warnaRef, newBarangLog.isi, -selisihPcs,loggedInUsers)
            } else {
                Log.i("InsertLogTry", "old log barang isi != new barang isi ${oldBarangLog.isi} new log barang isi=${newBarangLog.isi}")
                val oldDetailWarna = getDetailWarna(oldBarangLog.warnaRef, oldBarangLog.isi)
                val newDetailWarnaTable = getDetailWarna(newBarangLog.warnaRef, newBarangLog.isi)
                selisihPcs = -oldBarangLog.pcs
                Log.i("InsertLogTry", "old data ${newBarangLog.isi} ->stok awal${oldDetailWarna.detailWarnaPcs} -> setelah diedit ${oldDetailWarna.detailWarnaPcs -(-selisihPcs)}")
                Log.i("InsertLogTry", "new data ${newDetailWarnaTable.detailWarnaIsi} ->stok awal${newDetailWarnaTable.detailWarnaPcs} -> setelah diedit ${newDetailWarnaTable.detailWarnaPcs -(newBarangLog.pcs)}")
                Log.i("InsertLogTry", "new detail warna - selisih pcs:${newDetailWarnaTable.detailWarnaPcs} -> ${newDetailWarnaTable.detailWarnaPcs - (-newBarangLog.pcs)}")
                updateDetailWarnaTODao(oldBarangLog.warnaRef, oldBarangLog.isi, -selisihPcs,loggedInUsers)
                updateDetailWarnaTODao(newBarangLog.warnaRef, newBarangLog.isi, -newBarangLog.pcs,loggedInUsers)
            }
        } else {
            Log.i("InsertLogTry", "old ref != new ref")
            selisihPcs = -oldBarangLog.pcs
            updateDetailWarnaTODao(oldBarangLog.warnaRef, oldBarangLog.isi, -selisihPcs,loggedInUsers)
            updateDetailWarnaTODao(newBarangLog.warnaRef, newBarangLog.isi, -newBarangLog.pcs,loggedInUsers)
        }
    } else {
        Log.e("InsertLogTry", "oldBarangLog is null")
        updateDetailWarnaTODao(newBarangLog.warnaRef, newBarangLog.isi, -newBarangLog.pcs,loggedInUsers)
    }
} catch (e: Exception) {
    Log.e("InsertLogTry", "Error updating detail warna: ${e.message}", e)
*/