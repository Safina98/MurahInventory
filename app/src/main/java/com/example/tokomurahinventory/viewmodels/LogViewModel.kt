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
import java.util.Locale
import java.util.UUID

class LogViewModel (
    val dataSourceMerk:MerkDao,
    val dataSourceWarna:WarnaDao,
    val dataSourceDetailWarna:DetailWarnaDao,
    val dataSourceLog:LogDao,
    val dataSourceBarangLog:BarangLogDao,
    application: Application): AndroidViewModel(application){

    //all log in database
    //var allLog= dataSourceLog.selectAllLog()
    private var _allLog = MutableLiveData<List<LogTable>>()
    val allLog :LiveData<List<LogTable>> get() = _allLog

    val allMerkFromDb = dataSourceMerk.selectAllNamaMerk()
    private val _pendingDialog = MutableLiveData<Triple<CountModel, Int, String>?>()
    val pendingDialog: LiveData<Triple<CountModel, Int, String>?> get() = _pendingDialog

    private val _codeWarnaByMerk = MutableLiveData<List<String>>()
    val codeWarnaByMerk: LiveData<List<String>> get() = _codeWarnaByMerk

    private val _isiByWarnaAndMerk = MutableLiveData<List<Double>>()
    val isiByWarnaAndMerk: LiveData<List<Double>> get() = _isiByWarnaAndMerk

    //Live Data List for Barang Log
    private val _countModelList = MutableLiveData<List<CountModel>?>()
    val countModelList :LiveData<List<CountModel>?> get() = _countModelList

    //Untuk Update Log


    //count model id
    private var currentId = -1

    //two way binding edit text for input purpose
    val namaToko = MutableLiveData("")
    val user = MutableLiveData("")
    val subKeterangan = MutableLiveData("")
    val merkMutable=MutableLiveData<String?>(null)

    //Navigation
    //add log fab from log
    private val _addLogFab = MutableLiveData<Boolean>()
    val addLogFab: LiveData<Boolean> get() = _addLogFab
    //for search query
    private val _unFilteredLog = MutableLiveData<List<LogTable>>()

    //buntton save from input log
    private val _navigateToLog = MutableLiveData<Boolean>()
    val navigateToLog: LiveData<Boolean> get() = _navigateToLog

    //state
    private val _isWarnaClick = MutableLiveData<Boolean>()
    val isWarnaClick: LiveData<Boolean> get() = isWarnaClick


    var mutableLog = MutableLiveData<LogTable?>()
    var mutableLogBarang = MutableLiveData<List<BarangLog>?>()


    init {
        getAllMerkTable()
    }

    fun setPendingDialog(countModel: CountModel, position: Int, type: String) {
        _pendingDialog.value = Triple(countModel, position, type)
    }
    fun resetPendingDialog() {
        _pendingDialog.value = null
    }


    fun getAllMerkTable(){
        viewModelScope.launch {
            var list = withContext(Dispatchers.IO){
                dataSourceLog.selectAllLogList()
            }
            _allLog.value = list
            _unFilteredLog.value = list
        }
    }
    fun filterLog(query: String?) {
        val list = mutableListOf<LogTable>()
        if (!query.isNullOrEmpty()) {
            list.addAll(_unFilteredLog.value!!.filter {
                it.namaToko.lowercase(Locale.getDefault()).contains(query.lowercase(Locale.getDefault())) ||
                        it.userName.lowercase(Locale.getDefault()).contains(query.lowercase(Locale.getDefault()))||
                        it.merk.lowercase(Locale.getDefault()).contains(query.lowercase(Locale.getDefault()))
            })
        } else {
            list.addAll(_unFilteredLog.value!!)
        }
        _allLog.value = list
    }


    //delete from adalter
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


    // Function to update the count value
    fun updateIsi(position: Int, count: Double) {
        val updatedList = countModelList.value?.toMutableList()
        updatedList?.get(position)?.isi = count
        _countModelList.value = updatedList!!
    }

    // Function to update the merk value
    fun updateMerk(position: Int, merk: String) {
        val updatedList = _countModelList.value?.toMutableList()
        if (updatedList != null && position in updatedList.indices) {
            updatedList[position].merkBarang = merk
            merkMutable.value = merk
            _countModelList.value = updatedList // Notify observers of the change
        }
    }

    fun getWarnaByMerk(merk:String){
        viewModelScope.launch {
            val refMerk = withContext(Dispatchers.IO){dataSourceMerk.getMerkRefByName(merk)}
            val stringWarnaList=withContext(Dispatchers.IO){dataSourceWarna.selectStringWarnaByMerk(refMerk)}
            _codeWarnaByMerk.value = stringWarnaList
        }
    }
    fun getIsiByWarnaAndMerk(merk:String,warna:String){
        viewModelScope.launch {
            val refMerk = withContext(Dispatchers.IO){dataSourceMerk.getMerkRefByName(merk)}
            val refWarna = withContext(Dispatchers.IO){dataSourceWarna.getWarnaRefByName(warna,refMerk)}
            val stringWarnaList=withContext(Dispatchers.IO){dataSourceDetailWarna.getIsiDetailWarnaByWarna(refWarna)}
            Log.i("AutoCompleteTextProb","Merk: $merk ref: $refMerk")
            Log.i("AutoCompleteTextProb","Warna: $warna ref: $refMerk")
            Log.i("AutoCompleteTextProb","listDetail: $stringWarnaList")
            _isiByWarnaAndMerk.value = stringWarnaList
        }
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
       // _codeWarnaByMerk.value =null
    }

    //add new item to _countModelList when button clicked
    fun addNewCountItemBtn(){
        val a = _countModelList.value?.toMutableList() ?: mutableListOf()
        a.add(CountModel(getAutoIncrementId(),"9001","CAMARO",35.0,1,"",""))
        _countModelList.value=a

    }
    //Auto Increment Count Model Id
    private fun getAutoIncrementId(): Int {
        currentId++
        return currentId
    }
////////////////////////////////////////////Log Crud/////////////////////////////////////////
    //Save log
    fun insertOrUpdate(){
        if (mutableLog.value!=null){
            updateLog()
            Log.i("InsertLogTry", "update log")
        }
    else{
        addLog()
            Log.i("InsertLogTry", "add log")
        }
    }
    fun populateMutableLiveData(log:LogTable){
        namaToko.value = log.namaToko
        user.value = log.userName
        subKeterangan.value = log.keterangan
        mutableLog.value = log
        populateListOfLogBarang(log.refLog)
    }
    fun populateListOfLogBarang(logRef:String){
        viewModelScope.launch {
            var list = withContext(Dispatchers.IO){
                dataSourceBarangLog.selectBarangLogByLogRef(logRef)
            }
            mutableLogBarang.value = list
            updateBarangLogToCountModel(list)
        }
    }
    fun updateBarangLogToCountModel(barangLogList: List<BarangLog>){
        viewModelScope.launch{
            var list = barangLogList.map { barangLog ->
                CountModel(
                    id = barangLog.id,
                    kodeBarang = withContext(Dispatchers.IO){dataSourceWarna.getKodeWarnaByRef(barangLog.warnaRef)}, // Assuming `kodeBarang` is equivalent to `detailWarnaRef`
                    merkBarang = withContext(Dispatchers.IO){dataSourceMerk.getMerkNameByRef(barangLog.refMerk)}, // Assuming `merkBarang` is equivalent to `refMerk`
                    isi = barangLog.isi,
                    psc = barangLog.pcs,
                    logRef = barangLog.refLog, // Assuming `logRef` is equivalent to `refLog`
                    barangLogRef=barangLog.barangLogRef
                )
            }
            _countModelList.value = list
        }

    }


    fun updateLog(){
        viewModelScope.launch {
            val s = getStringS()
            val updatedLog = LogTable(
                id = mutableLog.value!!.id,
                userName = user.value ?: "Failed",
                password = "",
                namaToko = namaToko.value ?: "Failed",
                logDate = mutableLog.value!!.logDate, // assuming you have a date field
                keterangan = subKeterangan.value ?: "Failed",
                merk = s,
                kodeWarna = "",
                logIsi = 0.0,
                pcs = countModelList.value!!.sumOf { it.psc },
                detailWarnaRef = "",
                refLog = mutableLog.value!!.refLog,
            )
            updateLogToDao(updatedLog)
            updateLogBarang(updatedLog.refLog)
            getAllMerkTable()
            onNavigateToLog()
        }
    }
    fun updateLogBarang(logRef: String){
        viewModelScope.launch {
            for (i in countModelList.value!!){
                getBarangLogUpdate(i.merkBarang,i.kodeBarang,i.isi,i.psc,logRef,i.barangLogRef)
            }
        }
    }
    fun getBarangLogUpdate(namaMerk:String,kodeWarna:String,isi:Double,pcs:Int,refLog:String,barangLogRef:String){
        viewModelScope.launch{
            val refMerk = getrefMerkByName(namaMerk.toUpperCase())
            val refWarna = getrefWanraByName(kodeWarna.toUpperCase(),refMerk)
            var refDetailWarna = getrefDetailWanraByWarnaRefndIsi(refWarna,isi)
            val barangLog = BarangLog(
                refMerk = refMerk,
                warnaRef =  refWarna,
                detailWarnaRef = refDetailWarna,
                isi = isi,
                pcs = pcs,
                barangLogDate = Date(),
                refLog = refLog,
                barangLogRef = barangLogRef
            )
            //updateDetailWarna(refDetailWarna,isi,pcs)
            //get the old number of pcs, use barang log ref?
            updateDetailWarna(barangLog)
            updateBarangLogToDao(barangLog)
        }
    }
    /*
    fun updateDetailWarna(newBarangLog: BarangLog){
        viewModelScope.launch{
            var oldBarangLog = withContext(Dispatchers.IO){ dataSourceBarangLog.selectBarangLogByRef(newBarangLog.barangLogRef)}
            var selisihPcs=0
            if (oldBarangLog.warnaRef ==newBarangLog.warnaRef){
                if (oldBarangLog.isi == newBarangLog.isi){
                    selisihPcs = newBarangLog.pcs-oldBarangLog.pcs
                    updateDetailWarna(newBarangLog.detailWarnaRef,newBarangLog.isi,selisihPcs)
                }else{
                    selisihPcs = oldBarangLog.pcs*-1
                    updateDetailWarna(oldBarangLog.detailWarnaRef,oldBarangLog.isi,selisihPcs)
                    updateDetailWarna(newBarangLog.detailWarnaRef,newBarangLog.isi,newBarangLog.pcs)
                }
            }else{
                Log.i("InsertLogTry", "old ref!= new ref")
            }

        }
    }

     */
    fun updateDetailWarna(newBarangLog: BarangLog) {
        viewModelScope.launch {
            try {
                val oldBarangLog = withContext(Dispatchers.IO) {
                    dataSourceBarangLog.selectBarangLogByRef(newBarangLog.barangLogRef)
                }
                var selisihPcs = 0
                if (oldBarangLog != null) {
                    if (oldBarangLog.warnaRef == newBarangLog.warnaRef) {
                        if (oldBarangLog.isi == newBarangLog.isi) {
                            selisihPcs = newBarangLog.pcs - oldBarangLog.pcs
                            updateDetailWarna(newBarangLog.detailWarnaRef, newBarangLog.isi, selisihPcs)
                        } else {
                            selisihPcs = oldBarangLog.pcs * -1
                            updateDetailWarna(oldBarangLog.detailWarnaRef, oldBarangLog.isi, selisihPcs)
                            updateDetailWarna(newBarangLog.detailWarnaRef, newBarangLog.isi, newBarangLog.pcs)
                        }
                    } else {
                        Log.i("InsertLogTry", "old ref!= new ref")
                    }
                } else {
                    Log.e("InsertLogTry", "oldBarangLog is null")
                }
            } catch (e: Exception) {
                Log.e("InsertLogTry", "Error updating detail warna: ${e.message}", e)
            }
        }
    }
    fun addLog(){
        viewModelScope.launch {
            val s = getStringS()
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
            insertLogToDao(newLog)
            addLogBarang(newLog.refLog)

            getAllMerkTable()
            onNavigateToLog()
        }
    }
    fun getStringS():String{
        var s =""
        for (i in countModelList.value!!){
            s = s+"${i.kodeBarang}; ${i.isi} meter; ${i.psc} pcs\n"
        }
        return s
    }

    fun getBarangLog(namaMerk:String,kodeWarna:String,isi:Double,pcs:Int,refLog:String){
        viewModelScope.launch{
            val refMerk = getrefMerkByName(namaMerk.toUpperCase())
            val refWarna = getrefWanraByName(kodeWarna.toUpperCase(),refMerk)
            var refDetailWarna = getrefDetailWanraByWarnaRefndIsi(refWarna,isi)
            val barangLog = BarangLog(
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

    fun addLogBarang(logRef:String){
        viewModelScope.launch {
            for (i in countModelList.value!!){
                getBarangLog(i.merkBarang,i.kodeBarang,i.isi,i.psc,logRef)
            }
        }
    }

    fun resetTwoWayBindingSub(){
        viewModelScope.launch {
            namaToko.value  = null
            user.value=null
            subKeterangan.value =null
            mutableLog.value=null
            mutableLogBarang.value=null
            _countModelList.value=null
            currentId = -1
            Log.i("InsertLogTry", "add log mutable${mutableLog.value}")
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
    /*
        private suspend fun updateDetailWarna(refDetailWarna:String,isi:Double,pcs:Int){
            withContext(Dispatchers.IO){
                dataSourceDetailWarna.updateDetailWarna(refDetailWarna,isi,pcs)
            }
        }

     */
    private suspend fun updateOldDetailWarna(refDetailWarna:String,isi:Double,pcs:Int){
        withContext(Dispatchers.IO){
            dataSourceDetailWarna.updateOldDetailWarna(refDetailWarna,isi,pcs)
        }
    }
    private suspend fun updateDetailWarna(refDetailWarna: String, isi: Double, pcs: Int) {
        withContext(Dispatchers.IO) {
            try {
                val result = dataSourceDetailWarna.updateDetailWarna(refDetailWarna, isi, pcs)
                Log.i("InsertLogTry", "Updated $result rows for refDetailWarna=$refDetailWarna, isi=$isi, pcs=$pcs")
            } catch (e: Exception) {
                Log.e("InsertLogTry", "Error updating detail warna: ${e.message}", e)
            }
        }
    }

    private suspend fun updateLogToDao(log: LogTable){
        withContext(Dispatchers.IO){
            dataSourceLog.update(log)
        }
    }
    private suspend fun updateBarangLogToDao(log: BarangLog){
        withContext(Dispatchers.IO){
            dataSourceBarangLog.updateByBarangLogRef(log.refMerk,log.warnaRef,log.detailWarnaRef,log.isi,log.pcs,log.barangLogDate,log.refLog,log.barangLogRef)
        }
    }
    private suspend fun deleteDSPNewS(id: String){
        withContext(Dispatchers.IO){
            //dataSource4.deleteDSPById(id)
        }
    }



    //Navigation
    fun onAddLogFabClick(){ _addLogFab.value = true }
    fun onAddLogFabClicked(){ _addLogFab.value = false }
    fun onNavigateToLog(){ _navigateToLog.value = true }
    fun onNavigatedToLog(){ _navigateToLog.value = false }
    fun onLongClick(v: View): Boolean { return true }

    fun onWarnaClick(){ _isWarnaClick.value = true }
    fun onWarnaClickked(){ _isWarnaClick.value = false }


}