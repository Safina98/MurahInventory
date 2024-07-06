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
import com.example.tokomurahinventory.database.LogDao
import com.example.tokomurahinventory.database.MerkDao
import com.example.tokomurahinventory.database.WarnaDao
import com.example.tokomurahinventory.models.BarangLog
import com.example.tokomurahinventory.models.CountModel
import com.example.tokomurahinventory.models.LogTable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.UUID
import kotlin.math.log

class LogViewModel (
    val dataSourceMerk:MerkDao,
    val dataSourceWarna:WarnaDao,
    val dataSourceDetailWarna:DetailWarnaDao,
    val dataSourceLog:LogDao,
    val dataSourceBarangLog:BarangLogDao,
    application: Application): AndroidViewModel(application){
    //dummy list
    var logDummy = mutableListOf<LogTable>()
    var allLog= dataSourceLog.selectAllLog()

    //
    private var logRef =MutableLiveData<String>()

    //Live Data List for Barang Log
    private val _countModelList = MutableLiveData<List<CountModel>>()
    val countModelList :LiveData<List<CountModel>> get() = _countModelList
    //Untuk Update Log
    private var mutableLogTable = MutableLiveData(LogTable())
    private val mutableDspTableNew = MutableLiveData(BarangLog())
    private val mutableDspList = MutableLiveData<List<BarangLog>?>()

    //count model id
    private var currentId = -1

    //two way binding edit text for input purpose
    val namaToko = MutableLiveData("")
    val user = MutableLiveData("")
    val subKeterangan = MutableLiveData("")
    val barangString = MutableLiveData("")
    val subDate = MutableLiveData("")

    //Navigation
    //add log fab from log
    private val _addLogFab = MutableLiveData<Boolean>()
    val addLogFab: LiveData<Boolean> get() = _addLogFab
    //buntton save from input log
    private val _navigateToLog = MutableLiveData<Boolean>()
    val navigateToLog: LiveData<Boolean> get() = _navigateToLog

    init {
        //dummy list
       // logDummy.add(LogTable(0,"JohnDoe","asdf", "Toko ABC",Date(),"Jane Doe","","",0.0,0,"",""))
       // logDummy.add(LogTable(2,"DoeJohn","asdf", "Toko DEF",Date(),"Doe Jane","","",0.0,0,"",""))
    }

    fun deleteCountModel(countModel: CountModel,position: Int){
        var list = countModelList.value?.toMutableList()
        list?.removeAt(position)
        if (countModel.logRef!=""){
            deleteDSPNew(countModel.logRef)
        }
        _countModelList.value  = list?.toList()
    }
    fun deleteDSPNew(id:String){ viewModelScope.launch {
        deleteDSPNewS(id)
    } }
    private suspend fun deleteDSPNewS(id: String){
        withContext(Dispatchers.IO){
            //dataSource4.deleteDSPById(id)
        }
    }
    // Function to update the count value
    fun updateIsi(position: Int, count: Double) {
        val updatedList = countModelList.value?.toMutableList()
        updatedList?.get(position)?.isi = count
        _countModelList.value = updatedList!!

    }
    // Function to update the merk value
    fun updateMerk(position: Int, merk: String) {
        val updatedList = countModelList.value?.toMutableList()
        updatedList?.get(position)?.merkBarang = merk
        _countModelList.value = updatedList!!
    }

    // Function to update the net value
    fun updatePcs(position: Int, net: Int) {
        val updatedList = countModelList.value?.toMutableList()
        updatedList?.get(position)?.psc = net
        _countModelList.value = updatedList!!
    }
    // Function to update the code value
    fun updateKode(position: Int, kode: String) {
        val updatedList = countModelList.value?.toMutableList()
        updatedList?.get(position)?.kodeBarang = kode
        _countModelList.value = updatedList!!
    }

    //add new item to _countModelList when button clicked
    fun addNewCountItemBtn(){
        val a = _countModelList.value?.toMutableList() ?: mutableListOf()
        a.add(CountModel(getAutoIncrementId(),"9001","CAMARO",35.0,1,""))
        _countModelList.value=a

    }
    //Auto Increment Count Model Id
    private fun getAutoIncrementId(): Int {
        currentId++
        return currentId
    }

    //Save log
    fun addLog(){
        viewModelScope.launch {
            var s = getStringS()
            //TODO update detailWarna
            val newLog = LogTable(
                id = 0,
                userName = user.value ?: "Failed",
                password = "",
                namaToko = namaToko.value ?: "Failed",
                logDate = Date(), // assuming you have a date field
                keterangan = subKeterangan.value ?: "Failed",
                merk = s,
                kodeWarna = "",
                logIsi = 0.0,
                pcs = countModelList.value!!.sumOf { it.psc },
                detailWarnaRef = "",
                refLog = UUID.randomUUID().toString(),
            )
            Log.i("InsertLogTry", "add log mutable${mutableLogTable.value}")
            if ( mutableLogTable.value?.refLog =="") {
                insertLogToDao(newLog)
                addLogBarang(newLog.refLog)
            }
            else {
                //updateSubNewTable(mutableLogTable.value!!)
                //addDsp()
            }
            onNavigateToLog()
        }
    }
    fun getStringS():String{
        var s =""
        for (i in countModelList.value!!){
            mutableDspTableNew.value!!.refMerk = i.kodeBarang.toString()
            mutableDspTableNew.value!!.isi = i.isi
            mutableDspTableNew.value!!.pcs = i.psc
            mutableDspTableNew.value!!.id = i.id
            s = s+"${i.kodeBarang}; ${i.isi} meter; ${i.psc} pcs\n"
        }
        return s
    }

    fun getBarangLog(namaMerk:String,kodeWarna:String,isi:Double,pcs:Int,refLog:String){
        viewModelScope.launch{
            val refMerk = getrefMerkByName(namaMerk.toUpperCase())
            val refWarna = getrefWanraByName(kodeWarna.toUpperCase(),refMerk)
            var refDetailWarna = getrefDetailWanraByWarnaRefndIsi(refWarna,isi)
            var barangLog = BarangLog(
                refMerk = refMerk,
                warnaRef =  refWarna,
                detailWarnaRef = refDetailWarna,
                isi = isi,
                pcs = pcs,
                barangLogDate = Date(),
                refLog = refLog,
                barangLogRef = UUID.randomUUID().toString()
            )
            updateDetailWarna(refDetailWarna,isi,pcs)
            insertBarangLogToDao(barangLog)
        }
    }
    private suspend fun updateDetailWarna(refDetailWarna:String,isi:Double,pcs:Int){
        withContext(Dispatchers.IO){
            dataSourceDetailWarna.updateDetailWarna(refDetailWarna,isi,pcs)
        }

    }

    fun addLogBarang(logRef:String){
        viewModelScope.launch {
            for (i in countModelList.value!!){
                getBarangLog(i.merkBarang,i.kodeBarang,i.isi,i.psc,logRef)
            }
        }
    }


    //Suspend
    private suspend fun insertLogToDao(logTable:LogTable){
        withContext(Dispatchers.IO){
            //dataSource5.insert(logTable)
            dataSourceLog.insert(logTable)
        }
    }
    private suspend fun insertBarangLogToDao(barangLog: BarangLog){
        withContext(Dispatchers.IO){
            //dataSource5.insert(logTable)
            dataSourceBarangLog.insert(barangLog)
        }
    }
    private suspend fun getrefMerkByName(name:String):String{
        return withContext(Dispatchers.IO){
            dataSourceMerk.getMerkRefByName(name)
        }
    }
    private suspend fun getrefWanraByName(name:String,refMerk:String):String{
        return withContext(Dispatchers.IO){
            dataSourceWarna.getWarnaRefByName(name,refMerk)
        }
    }
    private suspend fun getrefDetailWanraByWarnaRefndIsi(name:String,isi:Double):String{
        return withContext(Dispatchers.IO){
            dataSourceDetailWarna.getDetailWarnaByIsi(name,isi)
        }
    }



    //Navigation
    fun onAddLogFabClick(){ _addLogFab.value = true }
    fun onAddLogFabClicked(){ _addLogFab.value = false }
    fun onNavigateToLog(){ _navigateToLog.value = true }
    fun onNavigatedToLog(){ _navigateToLog.value = false }
    fun onLongClick(v: View): Boolean { return true }


}