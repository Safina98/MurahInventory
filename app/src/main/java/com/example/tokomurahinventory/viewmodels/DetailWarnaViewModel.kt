package com.example.tokomurahinventory.viewmodels

import android.app.Application
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.tokomurahinventory.database.BarangLogDao
import com.example.tokomurahinventory.database.DetailWarnaDao
import com.example.tokomurahinventory.database.LogDao
import com.example.tokomurahinventory.database.WarnaDao
import com.example.tokomurahinventory.models.BarangLog
import com.example.tokomurahinventory.models.DetailWarnaTable
import com.example.tokomurahinventory.models.LogTable
import com.example.tokomurahinventory.models.model.DetailWarnaModel
import com.example.tokomurahinventory.utils.MASUKKELUAR
import com.example.tokomurahinventory.utils.SharedPreferencesHelper
import com.example.tokomurahinventory.utils.userNullString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

class DetailWarnaViewModel(val dataSourceWarna : WarnaDao,
                           val dataSourceDetailWarna:DetailWarnaDao,
                           val dataSourceLog: LogDao,
                           val dataSourceBarangLog: BarangLogDao,
                           val refWarna:String,
                           val loggedInUser:String,
                           application: Application
): AndroidViewModel(application) {

    private var viewModelJob = Job()
    //ui scope for coroutines
    private val uiScope = CoroutineScope(Dispatchers.Main +  viewModelJob)
    //Add detail warna fab
    private val _addDetailWarnaFab = MutableLiveData<Boolean>()
    val addDetailWarnaFab: LiveData<Boolean> get() = _addDetailWarnaFab

    //delete?
    val warna = dataSourceWarna.selectWarnaByWarnaRef(refWarna)
    //detail warna
    //val detailWarnaList = dataSourceDetailWarna.selectDetailWarnaByWarnaIdGroupByIsi(refWarna)
    val detailWarnaList = dataSourceDetailWarna.getDetailWarnaSummary(refWarna)

    private fun constructYesterdayDate(month: Int): Date? {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -2)
        val date = calendar.time
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return date
    }

    fun insertDetailWarna(pcs: Int, isi: Double) {
        uiScope.launch {
            val detailWarnaTable = DetailWarnaTable()
            val loggedInUsers = SharedPreferencesHelper.getLoggedInUser(getApplication())
            if (loggedInUsers != null) {
                detailWarnaTable.warnaRef = refWarna
                detailWarnaTable.lastEditedBy = loggedInUsers
                detailWarnaTable.detailWarnaLastEditedDate = Date()
                detailWarnaTable.detailWarnaIsi = isi
                detailWarnaTable.detailWarnaPcs = pcs

                val detailWarnaTable1 = checkIfIsiExisted(isi, refWarna)
                if (detailWarnaTable1 != null) {
                    detailWarnaTable1.lastEditedBy = loggedInUsers
                    detailWarnaTable1.detailWarnaIsi = isi
                    detailWarnaTable1.detailWarnaPcs = pcs
                    detailWarnaTable.detailWarnaLastEditedDate = Date()
                    updateDetailWarnaToDao(detailWarnaTable1, isi)
                    insertInputLog(detailWarnaTable1)
                } else {
                    detailWarnaTable.detailWarnaRef = UUID.randomUUID().toString()
                    detailWarnaTable.createdBy = loggedInUsers
                    detailWarnaTable.detailWarnaDate = Date()
                    insertDetailWarnaToDao(detailWarnaTable)
                    detailWarnaTable.user = loggedInUsers
                    insertInputLog(detailWarnaTable)
                }
            }else{
                Toast.makeText(getApplication(), userNullString, Toast.LENGTH_SHORT).show()
            }

        }
    }
    fun insertInputLog(detailWarnaTable: DetailWarnaTable) {
        uiScope.launch {
            try {
                Log.i("InsertLogTry","insert InputLog detailWarna: $detailWarnaTable")

                val log = LogTable().apply {
                    refLog = UUID.randomUUID().toString()
                    logTipe = MASUKKELUAR.MASUK
                    createdBy = loggedInUser
                    lastEditedBy = loggedInUser
                    userName = loggedInUser
                    logCreatedDate = Date()
                    logLastEditedDate = Date()
                }

                val barangLog = BarangLog().apply {
                    refLog = log.refLog
                    detailWarnaRef = detailWarnaTable.detailWarnaRef
                    refMerk = getMerkRef() // Ensure getMerkRef() returns a valid value
                    warnaRef = detailWarnaTable.warnaRef
                    isi = detailWarnaTable.detailWarnaIsi
                    pcs = detailWarnaTable.detailWarnaPcs
                    barangLogRef = UUID.randomUUID().toString()
                    barangLogTipe = MASUKKELUAR.MASUK
                }

                Log.i("InsertLogTry","insert InputLog log: $barangLog")

                insertLogToDao(log)
                insertBarangLogToDao(barangLog)

                Log.i("InsertLogTry","Insertion completed successfully")
            } catch (e: Exception) {
                Log.e("InsertLogError", "Error inserting log and barang log", e)
            }
        }
    }





    private suspend fun insertBarangLogToDao(barangLog: BarangLog){
        withContext(Dispatchers.IO){
            dataSourceBarangLog.insert(barangLog)
        }
    }
    private suspend fun insertLogToDao(log:LogTable){
        withContext(Dispatchers.IO){
            dataSourceLog.insert(log)
        }
    }
    private suspend fun getMerkRef():String{
       return withContext(Dispatchers.IO){
            dataSourceWarna.getMerkRefByWarnaRef(refWarna)
        }
    }
    private suspend fun checkIfIsiExisted(isi:Double,refWarna: String):DetailWarnaTable?{
        return withContext(Dispatchers.IO){
            dataSourceDetailWarna.checkIfIsiExisted(isi,refWarna)
        }
    }

    fun DetailWarnaModel.toDetailWarnaTable(): DetailWarnaTable {
        return DetailWarnaTable(
            detailWarnaIsi = this.detailWarnaIsi,
            detailWarnaPcs = this.detailWarnaPcs,
            detailWarnaRef = this.warnaRef
        )
    }


/*
    fun updateDetailWarna(oldDetailWarnaModel:DetailWarnaModel,pcs:Int,isi:Double){
        uiScope.launch {
            //for i in pcs, update isi from detail warna where isi = old isi and ref = warna ref
            val detailWarnaTable = withContext(Dispatchers.IO){ dataSourceDetailWarna.getFirstDetailWarna(isi,oldDetailWarnaModel.warnaRef,pcs) }
            val loggedInUsers = SharedPreferencesHelper.getLoggedInUser(getApplication())
            detailWarnaTable.lastEditedBy =loggedInUsers
            updateDetailWarnaToDao(detailWarnaTable,isi)

        //updateDetailWarnaToDao(detailWarnaModel.toDetailWarnaTable())
        }
    }

 */
    fun deleteDetailWarna(detailWarnaModel: DetailWarnaModel){
        uiScope.launch{
            deleteDetailWarnaToDao(detailWarnaModel.detailWarnaIsi,detailWarnaModel.warnaRef)
        }
    }
    private suspend fun updateDetailWarnaToDao(detailWarnaTable:DetailWarnaTable,newIsi:Double){
        withContext(Dispatchers.IO){
            dataSourceDetailWarna.updateDetailWarnaA(detailWarnaTable.warnaRef,newIsi,detailWarnaTable.detailWarnaPcs,detailWarnaTable.lastEditedBy?:"",detailWarnaTable.detailWarnaDate)
        }
    }
    private suspend fun deleteDetailWarnaToDao(isi:Double,warnaRef:String){
        withContext(Dispatchers.IO){
            val records = dataSourceDetailWarna.getDetailWarnaByIsiAndRef(isi, warnaRef)
            Log.i("DETAILWARNAPROB","records $records")
            dataSourceDetailWarna.deteteDetailWarnaByIsi(warnaRef,isi)
        }
    }
    private suspend fun insertDetailWarnaToDao(detailWarnaTable: DetailWarnaTable) {
        withContext(Dispatchers.IO) {
            //dummyDetail.add(detailWarnaTable)
            dataSourceDetailWarna.insert(detailWarnaTable)
        }
    }

    fun onAddDetailWarnaFabClick() { _addDetailWarnaFab.value = true }
    fun onAddDetailWarnaFabClicked() { _addDetailWarnaFab.value = false }
    fun onLongClick(v: View): Boolean { return false }
    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}