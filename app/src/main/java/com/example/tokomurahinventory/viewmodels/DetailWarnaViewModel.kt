package com.example.tokomurahinventory.viewmodels

import android.app.Application
import android.util.Log
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.tokomurahinventory.database.BarangLogDao
import com.example.tokomurahinventory.database.DetailWarnaDao
import com.example.tokomurahinventory.database.InputLogDao
import com.example.tokomurahinventory.database.LogDao
import com.example.tokomurahinventory.database.WarnaDao
import com.example.tokomurahinventory.models.BarangLog
import com.example.tokomurahinventory.models.DetailWarnaTable
import com.example.tokomurahinventory.models.InputLogTable
import com.example.tokomurahinventory.models.LogTable
import com.example.tokomurahinventory.models.model.DetailWarnaModel
import com.example.tokomurahinventory.utils.MASUKKELUAR
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

class DetailWarnaViewModel(val dataSourceWarna : WarnaDao,
                           val dataSourceDetailWarna:DetailWarnaDao,
                           val dataSourceInputLog:InputLogDao,
                           val dataSourceLog: LogDao,
                           val dataSourceBarangLog: BarangLogDao,
                           val refWarna:String,
                           val loggedInUser:String,
                           application: Application
): AndroidViewModel(application) {
    //Add detail warna fab
    private val _addDetailWarnaFab = MutableLiveData<Boolean>()
    val addDetailWarnaFab: LiveData<Boolean> get() = _addDetailWarnaFab
    var dummyDetail = mutableListOf<DetailWarnaTable>()
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
        viewModelScope.launch {
            var detailWarnaTable = DetailWarnaTable()
            detailWarnaTable.warnaRef = refWarna
            detailWarnaTable.lastEditedBy=loggedInUser
            detailWarnaTable.detailWarnaLastEditedDate=Date()
            detailWarnaTable.detailWarnaIsi = isi
            detailWarnaTable.detailWarnaPcs = pcs
            var detailWarnaTable1 = checkIfIsiExisted(isi,refWarna)
            if (detailWarnaTable1!=null){
                detailWarnaTable1.lastEditedBy=loggedInUser
                detailWarnaTable1.detailWarnaIsi = isi
                detailWarnaTable1.detailWarnaPcs = pcs
                detailWarnaTable.detailWarnaLastEditedDate = Date()
                updateDetailWarnaToDao(detailWarnaTable1,isi)
                insertInputLog(detailWarnaTable1)
            }else{
                detailWarnaTable.detailWarnaRef = UUID.randomUUID().toString()
                detailWarnaTable.createdBy = loggedInUser
                detailWarnaTable.detailWarnaDate = Date()
                insertDetailWarnaToDao(detailWarnaTable)
                insertInputLog(detailWarnaTable)
            }

        }
    }
    fun insertInputLog(detailWarnaTable: DetailWarnaTable){
        viewModelScope.launch {
            var log=LogTable()
            log.refLog = UUID.randomUUID().toString()
            log.logTipe = MASUKKELUAR.MASUK
            log.createdBy = loggedInUser
            log.lastEditedBy = loggedInUser
            log.logCreatedDate = Date()
            log.logLastEditedDate=Date()
            var barangLog = BarangLog()
            barangLog.refLog = log.refLog
            barangLog.detailWarnaRef = detailWarnaTable.detailWarnaRef
            barangLog.refMerk = getMerkRef()
            barangLog.warnaRef = detailWarnaTable.warnaRef
            barangLog.isi = detailWarnaTable.detailWarnaIsi
            barangLog.pcs = detailWarnaTable.detailWarnaPcs
            barangLog.barangLogTipe = MASUKKELUAR.MASUK
            insertLogToDao(log)
            insertBarangLogToDao(barangLog)
            selectAllInputLog()
        }

    }
    fun insertInputLogOld(detailWarnaTable: DetailWarnaTable){
        viewModelScope.launch {
            var inputLogTable =InputLogTable()
            inputLogTable.refMerk = getMerkRef()
            inputLogTable.warnaRef = detailWarnaTable.warnaRef
            inputLogTable.detailWarnaRef = detailWarnaTable.detailWarnaRef
            inputLogTable.isi = detailWarnaTable.detailWarnaIsi
            inputLogTable.pcs = detailWarnaTable.detailWarnaPcs
            inputLogTable.createdBy = loggedInUser
            inputLogTable.lastEditedBy = loggedInUser
            inputLogTable.barangLogInsertedDate = Date()
            inputLogTable.barangLogLastEditedDate=Date()
            inputLogTable.inputBarangLogRef = UUID.randomUUID().toString()
            insertInputLogToDao(inputLogTable)
            selectAllInputLog()
        }
    }

    fun selectAllInputLog(){
        viewModelScope.launch {
            var list = withContext(Dispatchers.IO){
                dataSourceInputLog.selectAllTable()
            }
            Log.i("INPUTLOGTRY","$list")
        }
    }

    private suspend fun insertInputLogToDao(inputLogTable: InputLogTable){
        withContext(Dispatchers.IO){
            dataSourceInputLog.insert(inputLogTable)
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



    fun updateDetailWarna(oldDetailWarnaModel:DetailWarnaModel,pcs:Int,isi:Double){
        viewModelScope.launch {
            //for i in pcs, update isi from detail warna where isi = old isi and ref = warna ref
        var list = withContext(Dispatchers.IO){ dataSourceDetailWarna.getFirstDetailWarna(isi,oldDetailWarnaModel.warnaRef,pcs) }
            for (i in list){
                //i.detailWarnaIsi = isi
                updateDetailWarnaToDao(i,isi)
            }
        //updateDetailWarnaToDao(detailWarnaModel.toDetailWarnaTable())
        }
    }
    fun deleteDetailWarna(detailWarnaModel: DetailWarnaModel){
        viewModelScope.launch{
            deleteDetailWarnaToDao(detailWarnaModel.detailWarnaIsi,detailWarnaModel.warnaRef)
        }
    }
    private suspend fun updateDetailWarnaToDao(detailWarnaTable:DetailWarnaTable,newIsi:Double){
        withContext(Dispatchers.IO){
            dataSourceDetailWarna.updateDetailWarnaA(detailWarnaTable.warnaRef,newIsi,detailWarnaTable.detailWarnaPcs,detailWarnaTable.lastEditedBy,detailWarnaTable.detailWarnaDate)
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

    fun onAddWarnaFabClick() { _addDetailWarnaFab.value = true }
    fun onAddWarnaFabClicked() { _addDetailWarnaFab.value = false }
    fun onLongClick(v: View): Boolean { return false }
}