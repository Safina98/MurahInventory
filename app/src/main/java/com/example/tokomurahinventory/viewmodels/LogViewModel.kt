package com.example.tokomurahinventory.viewmodels

import android.app.Application
import android.util.Log
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.tokomurahinventory.models.BarangLog
import com.example.tokomurahinventory.models.CountModel
import com.example.tokomurahinventory.models.LogTable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date

class LogViewModel (application: Application): AndroidViewModel(application){
    //dummy list
    var logDummy = mutableListOf<LogTable>()

    //fab
    //Add merk fab
    private val _addLogFab = MutableLiveData<Boolean>()
    val addLogFab: LiveData<Boolean> get() = _addLogFab

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
        a.add(CountModel(getAutoIncrementId(),"9001",1.0,1,""))
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
            mutableLogTable.value?.namaToko = namaToko.value ?: "Failed"
            mutableLogTable.value?.pcs = countModelList.value!!.sumOf{ it.psc}
            mutableLogTable.value?.logRef = logRef.value ?: ""
            mutableLogTable.value?.namaToko = namaToko.value ?: "Failed"
            mutableLogTable.value?.keterangan = subKeterangan.value ?: "Failed"
            mutableLogTable.value?.user = user.value ?: "Failed"

            if ( mutableLogTable.value?.logRef =="") {
                insertLogToDao(mutableLogTable.value!!)
                addLogBarang()
            }
            else {
                //updateSubNewTable(mutableLogTable.value!!)
                //addDsp()
            }
        }
    }



    fun addLogBarang(){
        viewModelScope.launch {
            mutableDspTableNew.value!!.logRef = logRef.value ?: ""
            mutableLogTable.value!!.date = Date()
            for (i in countModelList.value!!){
                mutableDspTableNew.value!!.kodeBarang = i.kodeBarang.toString()
                mutableDspTableNew.value!!.isi = i.isi
                mutableDspTableNew.value!!.pcs = i.psc
                mutableDspTableNew.value!!.id = i.id
               Log.i("InsertLogTry", "${i.kodeBarang} ${i.isi}meter ${i.psc}pcs")

            }
        }
    }


    //Suspend
    private suspend fun insertLogToDao(logTable:LogTable){
        withContext(Dispatchers.IO){
            //dataSource5.insert(logTable)
            logDummy.add(logTable)
            for (i in logDummy){
                Log.i("InsertLogTry", "${i.namaToko} ${i.user} ${i.keterangan}")
            }

        }
    }



    //Navigation
    fun onAddLogFabClick(){ _addLogFab.value = true }
    fun onAddLogFabClicked(){ _addLogFab.value = false }
    fun onLongClick(v: View): Boolean { return true }


}