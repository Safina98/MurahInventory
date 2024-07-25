package com.example.tokomurahinventory.viewmodels

import android.app.Application
import android.util.Log
import android.view.View
import android.widget.Toast
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
import com.example.tokomurahinventory.models.DetailWarnaTable
import com.example.tokomurahinventory.models.LogTable
import com.example.tokomurahinventory.utils.MASUKKELUAR
import com.example.tokomurahinventory.utils.SharedPreferencesHelper
import com.example.tokomurahinventory.utils.userNullString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

class LogViewModel (
    private val dataSourceMerk:MerkDao,
    val dataSourceWarna:WarnaDao,
    val dataSourceDetailWarna:DetailWarnaDao,
    private val dataSourceLog:LogDao,
    private val dataSourceBarangLog:BarangLogDao,
    val loggedInUser:String,
    application: Application): AndroidViewModel(application){

    //all log in database
    //var allLog= dataSourceLog.selectAllLog()
    private var _allLog = MutableLiveData<List<LogTable>>()
    val allLog :LiveData<List<LogTable>> get() = _allLog

    val allMerkFromDb = dataSourceMerk.selectAllNamaMerk()

    private val _codeWarnaByMerk = MutableLiveData<List<String>?>()
    val codeWarnaByMerk: LiveData<List<String>?> get() = _codeWarnaByMerk

    private val _isiByWarnaAndMerk = MutableLiveData<List<Double>?>()
    val isiByWarnaAndMerk: LiveData<List<Double>?> get() = _isiByWarnaAndMerk

    //Live Data List for Barang Log
    private val _countModelList = MutableLiveData<List<CountModel>?>()
    val countModelList :LiveData<List<CountModel>?> get() = _countModelList

   // val codeWarnaByMerk = SingleLiveEvent<List<String>>()
   // val isiByWarnaAndMerk = SingleLiveEvent<List<String>>()

    //Untuk Update Log


    //count model id
    private var currentId = -1

    //show or hide start date picker dialog
    private var _isStartDatePickerClicked = MutableLiveData<Boolean>()
    val isStartDatePickerClicked :LiveData<Boolean>get() = _isStartDatePickerClicked
    //Selected Date
    private val _selectedStartDate = MutableLiveData<Date?>()
    val selectedStartDate: LiveData<Date?> get() = _selectedStartDate
    private val _selectedEndDate = MutableLiveData<Date?>()
    val selectedEndDate: LiveData<Date?> get() = _selectedEndDate
    val _dateRangeString = MutableLiveData<String>()
    //two way binding edit text for input purpose
    val namaToko = MutableLiveData("")
    val user = MutableLiveData<String?>("")
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



    var mutableLog = MutableLiveData<LogTable?>()
    var mutableLogBarang = MutableLiveData<List<BarangLog>?>()


    init {
        getAllLogTable()
        user.value = loggedInUser
        updateDateRangeString(_selectedStartDate.value, _selectedEndDate.value)
    }



    ////////////////////////////Select/////////////////////////////////////////////////////
    //get list merk
    fun getAllLogTable(){
        viewModelScope.launch {
            val list = withContext(Dispatchers.IO){
                dataSourceLog.selectAllLogList(MASUKKELUAR.KELUAR)
            }
            _allLog.value = list
            _unFilteredLog.value = list
        }
    }
    fun setStartDateRange(startDate: Date?){
        viewModelScope.launch {
            _selectedStartDate.value = startDate
        }
    }
    fun setEndDateRange(endDate: Date?){
        viewModelScope.launch {
            _selectedEndDate.value=endDate
        }
    }
    private fun formatDate(date: Date?): String? {
        if (date != null) {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            return dateFormat.format(date)
        }
        return null
    }
    fun updateDateRangeString(startDate: Date?, endDate: Date?) {
        _dateRangeString.value = formatDateRange(startDate, endDate)
    }
    fun resetDate(){
        setStartDateRange(null)
        setEndDateRange(null)
        updateDateRangeString(null,null)
    }

    private fun formatDateRange(startDate: Date?, endDate: Date?): String {
        return if (startDate != null && endDate != null) {
            val dateFormat = SimpleDateFormat("EEEE, d MMMM yyyy", Locale("in", "ID"))
            val startDateString = dateFormat.format(startDate)
            val endDateString = dateFormat.format(endDate)
            "$startDateString - $endDateString"
        } else {
            "Pilih Tanggal"
        }
    }
    private fun constructYesterdayDate(): Date? {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -2)
        val date = calendar.time
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return date
    }
    fun updateRv4(){
        viewModelScope.launch {
            performDataFiltering(selectedStartDate.value, selectedEndDate.value)
        }
    }
    //filter data from database by date
    private fun performDataFiltering(startDate: Date?, endDate: Date?) {
        viewModelScope.launch {
            val filteredData = withContext(Dispatchers.IO) {
                dataSourceLog.getLogsByDateRange(startDate,endDate)
            }
            _allLog.value = filteredData
            _unFilteredLog.value = filteredData
        }
    }
    //get list merk for suggestion
    fun getWarnaByMerkOld(merk:String){
        viewModelScope.launch {
            val refMerk = withContext(Dispatchers.IO){dataSourceMerk.getMerkRefByName(merk)}
            val stringWarnaList=withContext(Dispatchers.IO){dataSourceWarna.selectStringWarnaByMerk(refMerk)}
            _codeWarnaByMerk.value = stringWarnaList
            //codeWarnaByMerk.setValue(stringWarnaList)
        }
    }
    suspend fun getWarnaByMerk(merk: String): List<String> {
        return withContext(Dispatchers.IO) {
            val refMerk = dataSourceMerk.getMerkRefByName(merk)
            dataSourceWarna.selectStringWarnaByMerk(refMerk)
        }
    }


    //get list isi for sugestion
    fun getIsiByWarnaAndMerk(merk:String,warna:String){
        viewModelScope.launch {
            val refMerk = withContext(Dispatchers.IO){dataSourceMerk.getMerkRefByName(merk)}
            val refWarna = withContext(Dispatchers.IO){dataSourceWarna.getWarnaRefByName(warna,refMerk)}
            val stringWarnaList=withContext(Dispatchers.IO){dataSourceDetailWarna.getIsiDetailWarnaByWarna(refWarna)}
            _isiByWarnaAndMerk.value = stringWarnaList
           // isiByWarnaAndMerk.setValue(stringWarnaList.map { it.toString() })
        }
    }
    /////////////////////////////////Insert and Update/////////////////////////////////////
    ///////////////////////////////////////Log/////////////////////////////////////////////
    fun updateLog(){
        viewModelScope.launch {
            val s = getStringS()
            val loggedInUsers = SharedPreferencesHelper.getLoggedInUser(getApplication())
            val updatedLog = LogTable(
                id = mutableLog.value!!.id,
                userName = loggedInUser,
                password = "",
                namaToko = namaToko.value ?: "Failed",
                logCreatedDate = mutableLog.value!!.logCreatedDate, // assuming you have a date field
                keterangan = subKeterangan.value ?: "Failed",
                merk = s,
                kodeWarna = "",
                logIsi = 0.0,
                pcs = countModelList.value!!.sumOf { it.psc },
                detailWarnaRef = "",
                refLog = mutableLog.value!!.refLog,
                lastEditedBy = loggedInUsers,
                logLastEditedDate = Date(),
                createdBy = mutableLog.value!!.createdBy,
                logTipe = mutableLog.value!!.logTipe
            )
            val cmList = countModelList.value!!
            updateLogToDao(updatedLog)
            updateLogBarang(updatedLog.refLog)
            //compare old countModel with the current one for delete purpose
            compare(updatedLog.refLog,cmList)
            getAllLogTable()
            onNavigateToLog()
        }
    }
    fun addLog(){
        viewModelScope.launch {
            val s = getStringS()
            val loggedInUsers = SharedPreferencesHelper.getLoggedInUser(getApplication())
            val allDataPresent = areAllCountModelValuesNotNull(countModelList)
           if (loggedInUsers!=null) {
            if (allDataPresent) {
                val newLog = LogTable(
                    id = 0,
                    userName = loggedInUsers,
                    password = "",
                    namaToko = namaToko.value ?: "Failed",
                    logCreatedDate = Date(), // assuming you have a date field
                    keterangan = subKeterangan.value ?: "Failed",
                    merk = s,
                    kodeWarna = "",
                    logIsi = 0.0,
                    pcs = countModelList.value!!.sumOf { it.psc },
                    detailWarnaRef = "",
                    refLog = UUID.randomUUID().toString(),
                    logLastEditedDate = Date(),
                    createdBy = loggedInUsers,
                    lastEditedBy = loggedInUsers,
                    logTipe = MASUKKELUAR.KELUAR
                )
                insertLogToDao(newLog)
                addLogBarang(newLog.refLog)

                getAllLogTable()
                onNavigateToLog()
            }else Toast.makeText(getApplication(),"Insert Failed, please check the data",Toast.LENGTH_SHORT).show()
            }
            else{
               Toast.makeText(getApplication(), userNullString, Toast.LENGTH_SHORT).show()
            }

        }
    }
    fun getBarangLog(namaMerk:String,kodeWarna:String,isi:Double,pcs:Int,refLog:String){
        viewModelScope.launch{
            val refMerk = getrefMerkByName(namaMerk.uppercase())
            val refWarna = getrefWanraByName(kodeWarna.uppercase(),refMerk)
            val refDetailWarna = getrefDetailWanraByWarnaRefndIsi(refWarna,isi)
            val loggedInUsers = SharedPreferencesHelper.getLoggedInUser(getApplication())
            val barangLog = BarangLog(
                refMerk = refMerk,
                warnaRef =  refWarna,
                detailWarnaRef = refDetailWarna,
                isi = isi,
                pcs = pcs,
                barangLogDate = Date(),
                refLog = refLog,
                barangLogRef = UUID.randomUUID().toString(),
                barangLogTipe = MASUKKELUAR.KELUAR

            )
            updateDetailWarnaTODao(refWarna,isi,pcs,loggedInUsers)
            insertBarangLogToDao(barangLog)
        }
    }
    fun addLogBarang(logRef:String){
        viewModelScope.launch {
            for (i in countModelList.value!!){
                getBarangLog(i.merkBarang!!,i.kodeBarang!!,i.isi!!,i.psc,logRef)
            }
        }
    }
    //////////////////////////////////Delete/////////////////////////////////////////////////
    /////////////////////////////////////Log////////////////////////////////////////////////
    //create mutable and live data selected merk ref,selected warna ref
    //observe selected merk
    //onchange update list warna
    // observe selected warna
    //on change update selected isi
    //di adapter panggil fungsi untuk set selected merk

    //delete log

    fun deleteLog(log: LogTable){
        viewModelScope.launch {
            //get barangLog
            val barangLogList = getBarangLogFromDao(log.refLog)
            updateDetailWarna(barangLogList)
            deleteLogToDao(log)
            getAllLogTable()
        }
    }
    fun updateDetailWarna(barangLogList:List<BarangLog>){
        viewModelScope.launch {
            val loggedInUsers = SharedPreferencesHelper.getLoggedInUser(getApplication())
            for(i in barangLogList){
                //update detail warna, add pcs detail warna  where ref = detail warna ref and isi = isi
                updateDetailWarnaTODao(i.warnaRef,i.isi,i.pcs*-1,loggedInUsers)
            }
        }
    }

    ///////////////////////////////Count Adapter////////////////////////////////////////////
    //delete from adalter
    fun deleteCountModel(id: Int) {
        val list = _countModelList.value?.toMutableList() ?: return
        val itemToRemove = list.find { it.id == id }
        if (itemToRemove != null) {
            list.remove(itemToRemove)
            _countModelList.value = list
        } else {
            Log.e("DeleteError", "Item with ID $id not found.")
        }
    }

    private suspend fun checkIfPcsReadyInStok(refDetailWarna:String,pcs_n:Int):Boolean{
        return withContext(Dispatchers.IO){
            dataSourceDetailWarna.isPcsReady(refDetailWarna,pcs_n)
        }
    }

    fun isDataEdited():Boolean{
        return true
    }


    // In LogViewModel
    fun updateCountModel(countModel: CountModel, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            val isMerkPresent = checkMerkExisted(countModel.merkBarang!!)
            val isWarnaPresent = isKodeWarnaInLiveData(codeWarnaByMerk, countModel.kodeBarang!!)
            val isIsiPresent = isIsiInLiveData(isiByWarnaAndMerk, countModel.isi!!)

            if (isMerkPresent && isWarnaPresent && isIsiPresent) {
                val refMerk = getrefMerkByName(countModel.merkBarang!!.uppercase())
                val refWarna = getrefWanraByName(countModel.kodeBarang!!.uppercase(), refMerk)
                val refDetailWarna = getrefDetailWanraByWarnaRefndIsi(refWarna, countModel.isi!!)

                val isPcsReadyInStok = checkIfPcsReadyInStok(refDetailWarna!!, countModel.psc)

                if (isPcsReadyInStok) {
                    val updatedList = _countModelList.value?.toMutableList()
                    val itemToUpdate = updatedList?.find { it.id == countModel.id }

                    if (itemToUpdate != null) {
                        itemToUpdate.merkBarang = countModel.merkBarang!!
                        itemToUpdate.kodeBarang = countModel.kodeBarang
                        itemToUpdate.isi = countModel.isi!!
                        itemToUpdate.psc = countModel.psc

                        merkMutable.value = countModel.merkBarang
                        _countModelList.value = updatedList // Notify observers of the change
                        callback(true) // Notify success
                    } else {
                        callback(false) // Notify failure
                    }
                } else {
                    callback(false) // Notify failure
                }
            } else {
                callback(false) // Notify failure
            }
        }
    }


    //try update count model
    fun updateCountModelOld(countModel: CountModel): Boolean {
        var success = false
        viewModelScope.launch {
            val isMerkPresent = checkMerkExisted(countModel.merkBarang!!)
            val isWarnaPresent = isKodeWarnaInLiveData(codeWarnaByMerk, countModel.kodeBarang!!)
            val isIsiPresent = isIsiInLiveData(isiByWarnaAndMerk, countModel.isi!!)

            Log.i("InsertLogTry", "updateCountModel isMerkPresent: $isMerkPresent")
            Log.i("InsertLogTry", "updateCountModel isWarnaPresent: $isWarnaPresent")
            Log.i("InsertLogTry", "updateCountModel isIsiPresent: $isIsiPresent")

            if (isMerkPresent && isWarnaPresent && isIsiPresent) {
                val refMerk = getrefMerkByName(countModel.merkBarang!!.uppercase())
                val refWarna = getrefWanraByName(countModel.kodeBarang!!.uppercase(), refMerk)
                val refDetailWarna = getrefDetailWanraByWarnaRefndIsi(refWarna, countModel.isi!!)

                Log.i("InsertLogTry", "updateCountModel refMerk: $refMerk")
                Log.i("InsertLogTry", "updateCountModel refWarna: $refWarna")
                Log.i("InsertLogTry", "updateCountModel refDetailWarna: $refDetailWarna")

                val isPcsReadyInStok = checkIfPcsReadyInStok(refDetailWarna!!, countModel.psc)

                Log.i("InsertLogTry", "updateCountModel isPCsReadyInStock: $isPcsReadyInStok")

                if (isPcsReadyInStok) {
                    val updatedList = _countModelList.value?.toMutableList()
                    val itemToUpdate = updatedList?.find { it.id == countModel.id }

                    Log.i("InsertLogTry", "updateCountModel itemToUpdate: $itemToUpdate")

                    if (itemToUpdate != null) {
                        itemToUpdate.merkBarang = countModel.merkBarang!!
                        itemToUpdate.kodeBarang = countModel.kodeBarang
                        itemToUpdate.isi = countModel.isi!!
                        itemToUpdate.psc = countModel.psc

                        Log.i("InsertLogTry", "updateCountModel itemToUpdate: $itemToUpdate")

                        merkMutable.value = countModel.merkBarang
                        _countModelList.value = updatedList // Notify observers of the change
                        success = true
                    } else {
                        Log.e("InsertLogTry", "Item with ID ${countModel.id} not found.")
                        Toast.makeText(getApplication(), "Barang tidak tersedia", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("InsertLogTry", "PCS not ready in stock for ID ${countModel.id}.")
                    Toast.makeText(getApplication(), "Item with ID ${countModel.id} not found.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(getApplication(), "Data tidak ada di database, coba lagi", Toast.LENGTH_SHORT).show()
            }
        }
        return success
    }


    // Function to update the merk value
    fun updateMerk(id: Int, merk: String) {
        viewModelScope.launch {
            if (checkMerkExisted(merk)) {
                val updatedList = _countModelList.value?.toMutableList()
                val itemToUpdate = updatedList?.find { it.id == id }
                if (itemToUpdate != null) {
                    itemToUpdate.merkBarang = merk
                    itemToUpdate.kodeBarang = null
                    itemToUpdate.isi = null
                    itemToUpdate.psc = 0
                    merkMutable.value = merk
                    _countModelList.value = updatedList // Notify observers of the change
                } else {
                    Log.e("InsertLogTry" +
                            "", "Item with ID $id not found.")
                }
            } else {
                Toast.makeText(getApplication(), "Data tidak ada di database, coba lagi", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Function to update the count value
    fun updateIsi(id: Int, count: Double) {
        viewModelScope.launch {
            val isPresent = isIsiInLiveData(isiByWarnaAndMerk, count)
            if (isPresent) {
                val updatedList = _countModelList.value?.toMutableList()
                val itemToUpdate = updatedList?.find { it.id == id }
                if (itemToUpdate != null) {
                    itemToUpdate.isi = count
                    itemToUpdate.psc = 0
                    _countModelList.value = updatedList // Notify observers of the change
                } else {
                    Log.e("UpdateError", "Item with ID $id not found.")
                }
            } else {
                Toast.makeText(getApplication(), "Data tidak ada di database, coba lagi", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun updatePcs(id: Int, net: Int) {
        viewModelScope.launch {
            val updatedList = _countModelList.value?.toMutableList()
            val itemToUpdate = updatedList?.find { it.id == id }
            if (itemToUpdate != null) {
                val refMerk = getrefMerkByName(itemToUpdate!!.merkBarang!!.uppercase())
                val refWarna = getrefWanraByName(itemToUpdate.kodeBarang!!.uppercase(), refMerk)
                val refDetailWarna = getrefDetailWanraByWarnaRefndIsi(refWarna, itemToUpdate.isi!!)

                val isPcsReadyInStok = checkIfPcsReadyInStok(refDetailWarna!!, itemToUpdate.psc)
                Log.e("UpdateError", "osPcsReadyIn tok $isPcsReadyInStok.")
                if (isPcsReadyInStok){
                    itemToUpdate.psc = net
                    _countModelList.value = updatedList // Notify observers of the change
                }else
                    Toast.makeText(getApplication(),"Stok barang tidak cukup",Toast.LENGTH_SHORT).show()

            } else {
                Log.e("UpdateError", "Item with ID $id not found.")
            }
        }
    }



    fun updateKode(id: Int, kode: String) {
        viewModelScope.launch {
            val isPresent = isKodeWarnaInLiveData(codeWarnaByMerk, kode)
            if (isPresent) {
                val updatedList = _countModelList.value?.toMutableList()
                val itemToUpdate = updatedList?.find { it.id == id }
                if (itemToUpdate != null) {
                    itemToUpdate.kodeBarang = kode
                    itemToUpdate.isi = null
                    itemToUpdate.psc = 0
                    _countModelList.value = updatedList // Notify observers of the change
                } else {
                    Log.e("UpdateError", "Item with ID $id not found.")
                }
            } else {
                Toast.makeText(getApplication(), "Data tidak ada di database, coba lagi", Toast.LENGTH_SHORT).show()
            }

            // _codeWarnaByMerk.value = null
        }
    }

    fun addNewCountItemBtn(){
        val a = _countModelList.value?.toMutableList() ?: mutableListOf()
        a.add(CountModel(getAutoIncrementId(),null,null,null,0,"",""))
        _countModelList.value=a

    }
    //Auto Increment Count Model Id
    private fun getAutoIncrementId(): Int {
        currentId++
        return currentId
    }

    // Function to update the net value

//////////////////////////////////////Check and filter///////////////////////////////////////
    //Untuk cek kode ada di list
    fun isKodeWarnaInLiveData(liveData: LiveData<List<String>?>, value: String): Boolean {
        return liveData.value?.contains(value) == true
    }
    //Untuk Check isi ada di list
    fun isIsiInLiveData(liveData: LiveData<List<Double>?>, value: Double): Boolean {
        return liveData.value?.contains(value) == true
    }

    fun filterLog(query: String?) {
        val list = mutableListOf<LogTable>()
        if (!query.isNullOrEmpty()) {
            list.addAll(_unFilteredLog.value!!.filter {
                it.namaToko.lowercase(Locale.getDefault()).contains(query.lowercase(Locale.getDefault())) ||
                        it.userName?.lowercase(Locale.getDefault())?.contains(query.lowercase(Locale.getDefault())) ?: false ||
                        it.merk.lowercase(Locale.getDefault()).contains(query.lowercase(Locale.getDefault()))
            })
        } else {
            list.addAll(_unFilteredLog.value!!)
        }
        _allLog.value = list
    }

    // Function to update the code value


    //add new item to _countModelList when button clicked

////////////////////////////////////////////Log Crud/////////////////////////////////////////
    //Save log
    fun insertOrUpdate(){
        if (mutableLog.value!=null){
            updateLog()
        }
    else{
        addLog()
        }
    }
/////////////////////////////////////Converter//////////////////////////////////////////////
fun updateBarangLogToCountModel(barangLogList: List<BarangLog>){
    viewModelScope.launch{
        val list = barangLogList.map { barangLog ->
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

////////////////////////////////populate and upopulate live data//////////////////////////////
    fun populateMutableLiveData(log:LogTable){
        namaToko.value = log.namaToko
        user.value = log.userName
        subKeterangan.value = log.keterangan
        mutableLog.value = log
        populateListOfLogBarang(log.refLog)
    }
    fun populateListOfLogBarang(logRef:String){
        viewModelScope.launch {
            val list = getBarangLogFromDao(logRef)
            mutableLogBarang.value = list
            updateBarangLogToCountModel(list)
        }
    }

    fun updateLogBarang(logRef: String){
        viewModelScope.launch {
            for (i in countModelList.value!!){
                Log.i("InsertLogTry","merk: ${i.merkBarang}, kode = ${i.kodeBarang}, detail: ${i.isi}")
                if(doesBarangLogExist(i.barangLogRef)) {
                    Log.i("InsertLogTry","Exist in database merk: ${i.merkBarang}, kode = ${i.kodeBarang}, detail: ${i.isi}")
                    getBarangLogUpdate(i.merkBarang!!, i.kodeBarang!!, i.isi!!, i.psc, logRef, i.barangLogRef)
                }else{
                    Log.i("InsertLogTry","Not in database merk: ${i.merkBarang}, kode = ${i.kodeBarang}, detail: ${i.isi}")
                    getBarangLog(i.merkBarang!!,i.kodeBarang!!,i.isi!!,i.psc,logRef)
                }
            }
        }
    }
    fun compare(logRef:String,cmList:List<CountModel>){
        viewModelScope.launch {
            val loggedInUsers = SharedPreferencesHelper.getLoggedInUser(getApplication())
            val a = withContext(Dispatchers.IO){dataSourceBarangLog.selectBarangLogByLogRef(logRef)}
            val itemsNotInCmList = a.filter { dbItem ->
                cmList.none { cmItem -> cmItem.barangLogRef == dbItem.barangLogRef }
            }
            itemsNotInCmList.forEach { item ->
                Log.i("InsertLogTry", "Item ${item.isi} - ${item.isi} not found in cmList")
                updateDetailWarnaTODao(item.warnaRef,item.isi,-item.pcs,loggedInUsers)
                deleteBarangLogToDao(item.id)
            }
        }
    }
    fun getBarangLogUpdate(namaMerk:String,kodeWarna:String,isi:Double,pcs:Int,refLog:String,barangLogRef:String){
        viewModelScope.launch{
            val refMerk = getrefMerkByName(namaMerk.uppercase())
            val refWarna = getrefWanraByName(kodeWarna.uppercase(),refMerk)
            val refDetailWarna = getrefDetailWanraByWarnaRefndIsi(refWarna,isi)
            val barangLog = BarangLog(
                refMerk = refMerk,
                warnaRef =  refWarna,
                detailWarnaRef = refDetailWarna,
                isi = isi,
                pcs = pcs,
                barangLogDate = Date(),
                refLog = refLog,
                barangLogRef = barangLogRef,
                barangLogTipe = MASUKKELUAR.KELUAR
            )
            updateDetailWarna(barangLog)
            updateBarangLogToDao(barangLog)
        }
    }


        fun updateDetailWarna(newBarangLog: BarangLog) {
            viewModelScope.launch {
                val loggedInUsers = SharedPreferencesHelper.getLoggedInUser(getApplication())
                try {
                    val oldBarangLog = withContext(Dispatchers.IO) {
                        dataSourceBarangLog.selectBarangLogByRef(newBarangLog.barangLogRef)
                    }
                    val selisihPcs: Int

                    if (oldBarangLog != null) {
                        Log.i("InsertLogTry", "old log barang isi ${oldBarangLog.isi} new log barang isi=${newBarangLog.isi}")

                        if (oldBarangLog.warnaRef == newBarangLog.warnaRef) {
                            if (oldBarangLog.isi == newBarangLog.isi) {
                                selisihPcs = newBarangLog.pcs - oldBarangLog.pcs
                                updateDetailWarnaTODao(newBarangLog.warnaRef, newBarangLog.isi, selisihPcs,loggedInUsers)
                            } else {
                                val oldDetailWarna = getDetailWarna(oldBarangLog.warnaRef, oldBarangLog.isi)
                                val newDetailWarnaTable = getDetailWarna(newBarangLog.warnaRef, newBarangLog.isi)
                                selisihPcs = -oldBarangLog.pcs
                                Log.i("InsertLogTry", "old detail warna pcs - selisih pcs=${oldDetailWarna.detailWarnaIsi} -> ${oldDetailWarna.detailWarnaPcs - selisihPcs}")
                                Log.i("InsertLogTry", "new detail warna - selisih pcs:${newDetailWarnaTable.detailWarnaIsi} -> ${newDetailWarnaTable.detailWarnaPcs - newBarangLog.pcs}")
                                updateDetailWarnaTODao(oldBarangLog.warnaRef, oldBarangLog.isi, selisihPcs,loggedInUsers)
                                updateDetailWarnaTODao(newBarangLog.warnaRef, newBarangLog.isi, newBarangLog.pcs,loggedInUsers)
                            }
                        } else {
                            Log.i("InsertLogTry", "old ref != new ref")
                            selisihPcs = -oldBarangLog.pcs
                            updateDetailWarnaTODao(oldBarangLog.warnaRef, oldBarangLog.isi, selisihPcs,loggedInUsers)
                            updateDetailWarnaTODao(newBarangLog.warnaRef, newBarangLog.isi, newBarangLog.pcs,loggedInUsers)
                        }
                    } else {
                        Log.e("InsertLogTry", "oldBarangLog is null")
                        updateDetailWarnaTODao(newBarangLog.warnaRef, newBarangLog.isi, newBarangLog.pcs,loggedInUsers)
                    }
                } catch (e: Exception) {
                    Log.e("InsertLogTry", "Error updating detail warna: ${e.message}", e)
                }
            }
        }


    fun areAllCountModelValuesNotNull(countModelList: LiveData<List<CountModel>?>): Boolean {
        val countModelItems = countModelList.value ?: return false
        for (countModel in countModelItems) {
            if (countModel.kodeBarang == null || countModel.merkBarang == null || countModel.isi == null ||countModel.psc==0) {
                return false
            }
        }
        return true
    }

    fun getStringS():String{
        var s =""
        for (i in countModelList.value!!){
            s = s+"${i.merkBarang} ${i.kodeBarang}; ${i.isi} meter; ${i.psc} pcs\n"
        }
        return s
    }




    fun setLiveDataToNull(){
        _codeWarnaByMerk.value = null
        _isiByWarnaAndMerk.value=null
    }

    fun resetTwoWayBindingSub(){
        viewModelScope.launch {
            namaToko.value  = null
            user.value=loggedInUser
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
    private suspend fun getrefDetailWanraByWarnaRefndIsi(name:String,isi:Double):String?{
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
    private suspend fun getDetailWarna(waraRef:String, isi:Double):DetailWarnaTable{
        return withContext(Dispatchers.IO){
            dataSourceDetailWarna. getDetailWarnaByIsii(waraRef,isi)
        }
    }

    private suspend fun updateDetailWarnaTODao(refWarna: String, isi: Double, pcs: Int,loggedInUsers: String?) {
        withContext(Dispatchers.IO) {
            try {
                dataSourceDetailWarna.updateDetailWarna(refWarna, isi, pcs,loggedInUsers)
                dataSourceDetailWarna.selecttTry(refWarna)
                //Log.i("InsertLogTry", "Updated $resultt rows for refDetailWarna=$refWarna, isi=$isi, pcs=$pcs")
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
    private suspend fun updateBarangLogToDao(log: BarangLog) {
        withContext(Dispatchers.IO) {
            if (doesBarangLogExist(log.barangLogRef)) {
                // Update existing record if barangLogRef exists
                dataSourceBarangLog.updateByBarangLogRef(log.refMerk, log.warnaRef, log.detailWarnaRef ?: "", log.isi, log.pcs, log.barangLogDate, log.refLog, log.barangLogRef)
            }
        }
    }
    private suspend fun doesBarangLogExist(barangLogRef: String): Boolean {
        return withContext(Dispatchers.IO) {
            dataSourceBarangLog.findByBarangLogRef(barangLogRef) != null
        }
    }

    private suspend fun deleteLogToDao(log: LogTable){
        withContext(Dispatchers.IO){
            dataSourceLog.delete(log.id)
        }
    }
    private suspend fun deleteBarangLogToDao(barangLogId:Int){
        withContext(Dispatchers.IO){
            dataSourceBarangLog.delete(barangLogId)
        }
    }
    //function to check wether merk in db or not
    private suspend fun checkMerkExisted(namaMerk: String):Boolean{
        return withContext(Dispatchers.IO){
            dataSourceMerk.isDataExists(namaMerk)
        }
    }
    private suspend fun getBarangLogFromDao(logRef:String):List<BarangLog>{
        return withContext(Dispatchers.IO){
            dataSourceBarangLog.selectBarangLogByLogRef(logRef)
        }
    }



    //Navigation
    fun onAddLogFabClick(){ _addLogFab.value = true }
    fun onAddLogFabClicked(){ _addLogFab.value = false }
    fun onStartDatePickerClick(){ _isStartDatePickerClicked.value = true }
    fun onStartDatePickerClicked(){ _isStartDatePickerClicked.value = false }
    fun onNavigateToLog(){ _navigateToLog.value = true }
    fun onNavigatedToLog(){ _navigateToLog.value = false }
    fun onLongClick(v: View): Boolean { return true }




}