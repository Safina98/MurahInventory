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
import com.example.tokomurahinventory.utils.UpdateStatus
import com.example.tokomurahinventory.utils.dataNotFoundMsg
import com.example.tokomurahinventory.utils.incorrectInputMsg
import com.example.tokomurahinventory.utils.stokTidakCukup
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
    //live data for sugestion
    val allMerkFromDb = dataSourceMerk.selectAllNamaMerk()
    private val _codeWarnaByMerk = MutableLiveData<List<String>?>()
    val codeWarnaByMerk: LiveData<List<String>?> get() = _codeWarnaByMerk
    private val _isiByWarnaAndMerk = MutableLiveData<List<Double>?>()
    val isiByWarnaAndMerk: LiveData<List<Double>?> get() = _isiByWarnaAndMerk

    //Live Data List for Barang Log
    val _countModelList = MutableLiveData<List<CountModel>?>()
    val countModelList :LiveData<List<CountModel>?> get() = _countModelList
    // Live data to handle visibility while loading and or crashed
    private val _isLogLoading = MutableLiveData<Boolean>(false)
    val isLogLoading: LiveData<Boolean> get() = _isLogLoading
    private val _isLoadCrashed = MutableLiveData<Boolean>(false)
    val isLoadCrashed: LiveData<Boolean> get() = _isLoadCrashed

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

    var mutableLog = MutableLiveData<LogTable?>()
    var mutableLogBarang = MutableLiveData<List<BarangLog>?>()


    init {
        //getAllLogTable()
        user.value = loggedInUser
        updateDateRangeString(_selectedStartDate.value, _selectedEndDate.value)
        setInitialStartDateAndEndDate()
    }



    ////////////////////////////Select/////////////////////////////////////////////////////

   fun setInitialStartDateAndEndDate() {
       val startDate = Calendar.getInstance().apply {
           set(Calendar.HOUR_OF_DAY, 0)
           set(Calendar.MINUTE, 0)
           set(Calendar.SECOND, 0)
           set(Calendar.MILLISECOND, 0)
       }.time
       val endDate = Calendar.getInstance().apply {
           set(Calendar.HOUR_OF_DAY, 23)
           set(Calendar.MINUTE, 59)
           set(Calendar.SECOND, 59)
           set(Calendar.MILLISECOND, 999)
       }.time
       _selectedStartDate.value = startDate
       _selectedEndDate.value=endDate
       updateDateRangeString(null,null)
   }
    //populate warna for sugestion in new pop up
    fun getWarnaByMerkNew(merk:String){
        viewModelScope.launch {
            val refMerk = withContext(Dispatchers.IO){dataSourceMerk.getMerkRefByName(merk)}
            if (refMerk != null) {
                val stringWarnaList = withContext(Dispatchers.IO) {
                    dataSourceWarna.selectStringWarnaByMerk(refMerk)
                }
                _codeWarnaByMerk.value = stringWarnaList
            }
        }
    }

    //get list merk

    fun setStartAndEndDateRange(startDate: Date?,endDate: Date?){
        viewModelScope.launch {
            _selectedStartDate.value = startDate
            _selectedEndDate.value = endDate
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
        setInitialStartDateAndEndDate()
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
            _isLogLoading.value = true
            _isLoadCrashed.value = false
            try {
                val filteredData = withContext(Dispatchers.IO) {
                    dataSourceLog.getLogsByDateRange(startDate, endDate, MASUKKELUAR.KELUAR)
                }
                // Update LiveData on the main thread
                withContext(Dispatchers.Main) {
                    _isLogLoading.value = false
                    _allLog.value = filteredData
                    _unFilteredLog.value = filteredData
                }
            } catch (e: Exception) {
                Log.e("DataFilteringError", "Error during data filtering", e)
                withContext(Dispatchers.Main) {
                    _isLogLoading.value = false
                    _isLoadCrashed.value = true
                }
            }
        }
    }
    //get list merk for suggestion
    fun getWarnaByMerkOld(merk:String){
        viewModelScope.launch {
            val refMerk = withContext(Dispatchers.IO){dataSourceMerk.getMerkRefByName(merk)!!}
            val stringWarnaList=withContext(Dispatchers.IO){dataSourceWarna.selectStringWarnaByMerk(refMerk)}
            _codeWarnaByMerk.value = stringWarnaList
        }
    }
    suspend fun getWarnaByMerk(merk: String): List<String> {
        return withContext(Dispatchers.IO) {
            val refMerk = dataSourceMerk.getMerkRefByName(merk)!!
            dataSourceWarna.selectStringWarnaByMerk(refMerk)
        }
    }

    //get list isi for sugestion
    fun getIsiByWarnaAndMerk(merk:String,warna:String){
        viewModelScope.launch {
            val refMerk = withContext(Dispatchers.IO){dataSourceMerk.getMerkRefByName(merk)}
            if (refMerk!=null){
                val refWarna = withContext(Dispatchers.IO){dataSourceWarna.getWarnaRefByName(warna,refMerk)}
                if (refWarna!=null){
                    val stringWarnaList=withContext(Dispatchers.IO){dataSourceDetailWarna.getIsiDetailWarnaByWarna(refWarna)}
                    _isiByWarnaAndMerk.value = stringWarnaList
                }
            }
           // isiByWarnaAndMerk.setValue(stringWarnaList.map { it.toString() })
        }
    }
    /////////////////////////////////Insert and Update/////////////////////////////////////
    ///////////////////////////////////////Log/////////////////////////////////////////////
    fun updateLog(){
        viewModelScope.launch {
            _isLogLoading.value = true

            val s = getStringS()
            val loggedInUsers = SharedPreferencesHelper.getLoggedInUser(getApplication())
            //check if all data exist  in database
            val allDataPresent = areAllCountModelValuesNotNull(countModelList)
            //check if data have any identical item
            val hasNotIdenticalItem = hasNotIdenticalItems(countModelList.value)
            if (allDataPresent&&hasNotIdenticalItem) {
                val updatedLog = LogTable(
                    id = mutableLog.value!!.id,
                    userName = loggedInUser,
                    password = "",
                    namaToko = namaToko.value ?: "Tidak diisi",
                    logCreatedDate = mutableLog.value!!.logCreatedDate, // assuming you have a date field
                    keterangan = subKeterangan.value ?: "Tidak diisi",
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
                Log.i("InsertLogTry","logbarang created date =${updatedLog.logCreatedDate}")
                Log.i("InsertLogTry","logbarang created date =${updatedLog.logLastEditedDate}")
                updateLogBarang(updatedLog.refLog,updatedLog.namaToko)
                //compare old countModel with the current one for delete purpose
                compare(updatedLog.refLog, cmList,updatedLog.namaToko) //check
                updateLogToDao(updatedLog)
                //getAllLogTable()
                onNavigateToLog()
            }else Toast.makeText(getApplication(), incorrectInputMsg,Toast.LENGTH_SHORT).show()
        }
    }
    fun addLog() {
        viewModelScope.launch {
            _isLogLoading.value = true
            Log.i("InsertLogTry", "addLog() called")
            val s = getStringS()
            val loggedInUsers = SharedPreferencesHelper.getLoggedInUser(getApplication())
            val allDataPresent = areAllCountModelValuesNotNull(countModelList)
            val hasNotIdenticalItem = hasNotIdenticalItems(countModelList.value)
            if (loggedInUsers != null) {
                if (allDataPresent && hasNotIdenticalItem) {
                    Log.i("InsertLogTry", "all data present")
                    val newLog = LogTable(
                        id = 0,
                        userName = loggedInUsers,
                        password = "",
                        namaToko = namaToko.value ?: "Tidak diisi",
                        logCreatedDate = Date(),
                        keterangan = subKeterangan.value ?: "Tidak diisi",
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

                    // Prepare list of BarangLog
                    val barangLogs = countModelList.value!!.map { item ->
                        val refMerk = getrefMerkByName(item.merkBarang!!.uppercase())
                        val refWarna = getrefWanraByName(item.kodeBarang!!, refMerk)!!
                        val refDetailWarna = getrefDetailWanraByWarnaRefndIsi(refWarna, item.isi!!)
                        BarangLog(
                            refMerk = refMerk,
                            warnaRef = refWarna,
                            detailWarnaRef = refDetailWarna,
                            isi = item.isi!!,
                            pcs = item.psc,
                            barangLogDate = Date(),
                            refLog = newLog.refLog,
                            barangLogRef = UUID.randomUUID().toString(),
                            barangLogTipe = MASUKKELUAR.KELUAR
                        )
                    }

                    try {
                        Log.i("InsertLogTry", "try inserting data")
                        insertLogAndUpdateDetailWarna(newLog,barangLogs,loggedInUsers)

                        //yourDao.insertLogAndUpdateDetailWarna(newLog, barangLogs, loggedInUsers)
                        //getAllLogTable()
                        onNavigateToLog()
                        Log.i("InsertLogTry", "Berhasil")
                    } catch (e: Exception) {
                        Log.e("InsertLogTry", "Error performing transaction: ${e.message}", e)
                        Toast.makeText(getApplication(), incorrectInputMsg, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(getApplication(), incorrectInputMsg, Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(getApplication(), userNullString, Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun getBarangLog(namaMerk:String,kodeWarna:String,isi:Double,pcs:Int,refLog:String,toko:String){
        viewModelScope.launch{
            Log.i("InsertLogTry","getBarangLog() called")
            val refMerk = getrefMerkByName(namaMerk.uppercase())
            val refWarna = getrefWanraByName(kodeWarna,refMerk)
            Log.i("InsertLogTry","addLogCalled")
            if (refWarna!=null){
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
                val ket = "Barang keluar sebanyak ${barangLog.pcs} pcs untuk $toko"
                updateDetailWarnaAndInsertBarangLogToDao(barangLog,refWarna,isi,pcs,loggedInUsers,ket)
            }
        }
    }

    //////////////////////////////////Delete/////////////////////////////////////////////////
    /////////////////////////////////////Log////////////////////////////////////////////////

    fun deleteLog(log: LogTable){
        viewModelScope.launch {
            //get barangLog
            _isLogLoading.value = true
            val barangLogList = getBarangLogFromDao(log.refLog)
            val loggedInUsers = SharedPreferencesHelper.getLoggedInUser(getApplication())
            updateDetailWarnaAndDeleteBarangLog(log,barangLogList,loggedInUsers)
            updateRv4()
           // getAllLogTable()
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
        val a =dataSourceDetailWarna.countMatchingRows(refDetailWarna,pcs_n)
            a>0
        //dataSourceDetailWarna.isPcsReady(refDetailWarna,pcs_n)
        }
    }




    fun updateCountModel(countModel: CountModel, oldCountModel: CountModel, callback: (UpdateStatus) -> Unit) {
        viewModelScope.launch {
            // Log initial state

            val updatedList = _countModelList.value?.toMutableList() ?: mutableListOf()
            val itemToUpdate = updatedList.find { it.id == countModel.id }

            if (itemToUpdate == null) {
                callback(UpdateStatus.ITEM_NOT_FOUND)
                return@launch
            }

            // Perform validation checks
            val isMerkPresent = checkMerkExisted(countModel.merkBarang!!)
            val isWarnaPresent = isKodeWarnaInLiveData(codeWarnaByMerk, countModel.kodeBarang!!)
            val isIsiPresent = isIsiInLiveData(isiByWarnaAndMerk, countModel.isi!!)

            // Fetch data from database
            val barangLogfromdb = withContext(Dispatchers.IO) { dataSourceBarangLog.findByBarangLogRef(countModel.barangLogRef) } ?: BarangLog()
            val selisihpcs = countModel.psc - barangLogfromdb.pcs

            val isPcsReadyInStok = if (isIsiPresent) {
                val refMerk = getrefMerkByName(countModel.merkBarang!!.uppercase())
                val refWarna = getrefWanraByName(countModel.kodeBarang!!, refMerk)
                val refDetailWarna = refWarna?.let { getrefDetailWanraByWarnaRefndIsi(it, countModel.isi!!) }
                checkIfPcsReadyInStok(refDetailWarna!!, selisihpcs)
            } else false

            if (isMerkPresent && isWarnaPresent && isIsiPresent && isPcsReadyInStok) {
                // Update item in list
                itemToUpdate.apply {
                    val refMerk = getrefMerkByName(countModel.merkBarang!!.uppercase())
                    val refWarna = getrefWanraByName(countModel.kodeBarang!!, refMerk)
                    merkBarang = countModel.merkBarang
                    kodeBarang = countModel.kodeBarang
                    isi = countModel.isi
                    psc = countModel.psc
                    satuan = withContext(Dispatchers.IO){dataSourceWarna.getSatuanWarnaByRef(refWarna!!)}
                }
                merkMutable.value = countModel.merkBarang
                _countModelList.value = updatedList
                callback(UpdateStatus.SUCCESS)
            } else {
                // Revert changes and notify of failure
                updatedList.apply {
                    remove(itemToUpdate)
                    add(oldCountModel)
                }
                _countModelList.value = updatedList

                callback(
                    when {
                        !isMerkPresent -> UpdateStatus.MERK_NOT_PRESENT
                        !isWarnaPresent -> UpdateStatus.WARNA_NOT_PRESENT
                        !isIsiPresent -> UpdateStatus.ISI_NOT_PRESENT
                        !isPcsReadyInStok -> UpdateStatus.PCS_NOT_READY_IN_STOCK
                        else -> UpdateStatus.ITEM_NOT_FOUND
                    }
                )
            }
        }
    }
    //try update count model

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
                Toast.makeText(getApplication(), dataNotFoundMsg, Toast.LENGTH_SHORT).show()
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
                Toast.makeText(getApplication(), dataNotFoundMsg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun updatePcs(id: Int, net: Int) {
        viewModelScope.launch {
            val updatedList = _countModelList.value?.toMutableList()
            val itemToUpdate = updatedList?.find { it.id == id }
            if (itemToUpdate != null) {
                val refMerk = getrefMerkByName(itemToUpdate.merkBarang!!.uppercase())
                val refWarna = getrefWanraByName(itemToUpdate.kodeBarang!!, refMerk)

                val refDetailWarna = getrefDetailWanraByWarnaRefndIsi(refWarna!!, itemToUpdate.isi!!)

                val isPcsReadyInStok = checkIfPcsReadyInStok(refDetailWarna!!, net)


                if (isPcsReadyInStok){
                    itemToUpdate.psc = net
                    _countModelList.value = updatedList // Notify observers of the change
                }else
                    Toast.makeText(getApplication(), stokTidakCukup,Toast.LENGTH_SHORT).show()

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
                Toast.makeText(getApplication(), dataNotFoundMsg, Toast.LENGTH_SHORT).show()
            }

            // _codeWarnaByMerk.value = null
        }
    }
    fun removeCountItem(itemId: Int) {
        val updatedList = _countModelList.value?.toMutableList() ?: mutableListOf()

        val itemToRemove = updatedList.find { it.id == itemId }

        if (itemToRemove != null) {
            if (itemToRemove.merkBarang==null||itemToRemove.kodeBarang==null||itemToRemove.isi==null||itemToRemove.psc==0)
            {
                updatedList.remove(itemToRemove)
                _countModelList.value = updatedList
            }
        }
    }

    fun addNewCountItemBtn():CountModel{
        val a = _countModelList.value?.toMutableList() ?: mutableListOf()
        val b = CountModel(getAutoIncrementId(),null,null,null,0,"","","")
        a.add(b)
        _countModelList.value=a
        return b
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
        if (_unFilteredLog.value!=null){
            if (!query.isNullOrEmpty()) {
                list.addAll(_unFilteredLog.value!!.filter {
                    it.namaToko.lowercase(Locale.getDefault()).contains(query.lowercase(Locale.getDefault())) ||
                            it.userName?.lowercase(Locale.getDefault())?.contains(query.lowercase(Locale.getDefault())) ?: false ||
                            it.merk.lowercase(Locale.getDefault()).contains(query.lowercase(Locale.getDefault()))
                })
            } else {
                list.addAll(_unFilteredLog.value!!)
            }
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
fun updateBarangLogToCountModel(barangLogList: List<BarangLog>,satuan:String){
    viewModelScope.launch{
        val list = barangLogList.map { barangLog ->
            CountModel(
                id = barangLog.id,
                kodeBarang = withContext(Dispatchers.IO){dataSourceWarna.getKodeWarnaByRef(barangLog.warnaRef)}, // Assuming `kodeBarang` is equivalent to `detailWarnaRef`
                merkBarang = withContext(Dispatchers.IO){dataSourceMerk.getMerkNameByRef(barangLog.refMerk)}, // Assuming `merkBarang` is equivalent to `refMerk`
                isi = barangLog.isi,
                psc = barangLog.pcs,
                logRef = barangLog.refLog, // Assuming `logRef` is equivalent to `refLog`
                barangLogRef=barangLog.barangLogRef,
                satuan = satuan
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
            var satuan=""
            if (list.isNotEmpty()){
                satuan = withContext(Dispatchers.IO){dataSourceWarna.getSatuanWarnaByRef(list[0].warnaRef)}
            }
            mutableLogBarang.value = list
            updateBarangLogToCountModel(list,satuan)
        }
    }

    fun updateLogBarang(logRef: String,toko:String){
        viewModelScope.launch {
            for (i in countModelList.value!!){
                if(doesBarangLogExist(i.barangLogRef)) {
                    getBarangLogUpdate(i.merkBarang!!, i.kodeBarang!!, i.isi!!, i.psc, logRef, i.barangLogRef,toko)
                }else{
                    Log.i("InsertLogTry","Not in database merk: ${i.merkBarang}, kode = ${i.kodeBarang}, detail: ${i.isi}")
                    getBarangLog(i.merkBarang!!,i.kodeBarang!!,i.isi!!,i.psc,logRef,toko)
                }
            }
        }
    }
    //when update log and delete a log
    fun compare(logRef:String,cmList:List<CountModel>,toko:String){
        viewModelScope.launch {
            val loggedInUsers = SharedPreferencesHelper.getLoggedInUser(getApplication())
            val a = withContext(Dispatchers.IO){dataSourceBarangLog.selectBarangLogByLogRef(logRef)}
            val itemsNotInCmList = a.filter { dbItem ->
                cmList.none { cmItem -> cmItem.barangLogRef == dbItem.barangLogRef }
            }
            itemsNotInCmList.forEach { item ->
                Log.i("InsertLogTry", "Item ${item.isi} - ${item.isi} not found in cmList")
                val detailWarnaKet="Barang keluar untuk $toko dibatalkan sebanyak ${item.pcs} pcs"
                //updateDetailWarnaTODao(item.warnaRef,item.isi,-item.pcs,loggedInUsers)
                //deleteBarangLogToDao(item.id)
                updateDetailAndDeleteBarangLogToDao(item.warnaRef,item.isi,-item.pcs,loggedInUsers,item.id,detailWarnaKet)
            }
        }
    }
    fun getBarangLogUpdate(namaMerk:String,kodeWarna:String,isi:Double,pcs:Int,refLog:String,barangLogRef:String,toko:String){
        viewModelScope.launch{
            val refMerk = getrefMerkByName(namaMerk.uppercase())
            val refWarna = getrefWanraByName(kodeWarna,refMerk)
            val refDetailWarna = getrefDetailWanraByWarnaRefndIsi(refWarna!!,isi)
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
            updateDetailWarna(barangLog,toko)
            //updateBarangLogToDao(barangLog)
        }
    }




        fun updateDetailWarna(newBarangLog: BarangLog,toko: String) {
            viewModelScope.launch {
                val loggedInUsers = SharedPreferencesHelper.getLoggedInUser(getApplication())
                try {
                    val oldBarangLog = withContext(Dispatchers.IO) {
                        dataSourceBarangLog.findByBarangLogRef(newBarangLog.barangLogRef)
                    }

                    val detailWarnaUpdates = mutableListOf<DetailWarnaTable>()

                    if (oldBarangLog != null) {
                        if (oldBarangLog.warnaRef == newBarangLog.warnaRef) {
                            if (oldBarangLog.isi == newBarangLog.isi) {
                                val selisihPcs = newBarangLog.pcs - oldBarangLog.pcs
                                if (selisihPcs!=0){
                                    detailWarnaUpdates.add(
                                        DetailWarnaTable(
                                            warnaRef = oldBarangLog.warnaRef,
                                            detailWarnaIsi = oldBarangLog.isi,
                                            detailWarnaPcs = selisihPcs,
                                            detailWarnaKet = "Stok barang keluar untuk ${toko} diubah dari ${oldBarangLog.pcs} pcs menjadi ${newBarangLog.pcs} pcs"
                                        )
                                    )
                                }

                            } else {
                                //val oldDetailWarna = getDetailWarna(oldBarangLog.warnaRef, oldBarangLog.isi)
                                //val newDetailWarnaTable = getDetailWarna(newBarangLog.warnaRef, newBarangLog.isi)
                                val oldSelisihPcs = -oldBarangLog.pcs
                                val newSelisihPcs = newBarangLog.pcs
                                detailWarnaUpdates.add(
                                    DetailWarnaTable(
                                        warnaRef = oldBarangLog.warnaRef,
                                        detailWarnaIsi = oldBarangLog.isi,
                                        detailWarnaPcs = oldSelisihPcs,
                                        detailWarnaKet = "Stok barang bertambah ${oldBarangLog.pcs} pcs. Barang keluar untuk $toko menjadi  ${newBarangLog.pcs} pcs isi ${newBarangLog.isi}"
                                    )
                                )
                                detailWarnaUpdates.add(
                                    DetailWarnaTable(
                                        warnaRef = newBarangLog.warnaRef,
                                        detailWarnaIsi = newBarangLog.isi,
                                        detailWarnaPcs = newSelisihPcs,
                                        detailWarnaKet = "Barang keluar ${newBarangLog.pcs} pcs untuk $toko",
                                    )
                                )
                            }
                        } else {
                            val oldSelisihPcs = -oldBarangLog.pcs
                            val warna = withContext(Dispatchers.IO){dataSourceWarna.getKodeWarnaByRef(newBarangLog.warnaRef)}
                            val merk = withContext(Dispatchers.IO){dataSourceMerk.getMerkNameByRef(newBarangLog.refMerk)}
                            detailWarnaUpdates.add(
                                DetailWarnaTable(
                                    warnaRef = oldBarangLog.warnaRef,
                                    detailWarnaIsi = oldBarangLog.isi,
                                    detailWarnaPcs = oldSelisihPcs,
                                    detailWarnaKet = "Barang bertambah ${oldBarangLog.pcs} pcs. Stok barang keluar diubah menjadi ${newBarangLog.pcs} pcs $merk $warna",
                                )
                            )
                            detailWarnaUpdates.add(
                                DetailWarnaTable(
                                    warnaRef = newBarangLog.warnaRef,
                                    detailWarnaIsi = newBarangLog.isi,
                                    detailWarnaPcs = newBarangLog.pcs,
                                    detailWarnaKet = "Barang keluar sebanyak ${newBarangLog.pcs} pcs untuk ke $toko"
                                )
                            )
                        }
                    } else {
                        detailWarnaUpdates.add(
                            DetailWarnaTable(
                                warnaRef = newBarangLog.warnaRef,
                                detailWarnaIsi = newBarangLog.isi,
                                detailWarnaPcs = newBarangLog.pcs
                            )
                        )
                    }

                    // Call the DAO transaction method
                    updateBarangLogAndDetailWarna(
                        newBarangLog.refMerk,
                        newBarangLog.warnaRef,
                        newBarangLog.detailWarnaRef!!,
                        newBarangLog.isi,
                        newBarangLog.pcs,
                        newBarangLog.barangLogDate,
                        newBarangLog.refLog,
                        newBarangLog.barangLogRef,
                        detailWarnaUpdates,
                        loggedInUsers
                    )

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
    fun hasNotIdenticalItems(countList: List<CountModel>?): Boolean {
        val countModelItems = countList?: return false
        val seen = mutableSetOf<Triple<String?, String?, Double?>>()
        for (item in countModelItems) {
            val key = Triple(item.kodeBarang, item.merkBarang, item.isi)
            if (key in seen) {
                Log.i("InsertLogTry","$key")
                return false // Identical item found
            }
            seen.add(key)
        }
        return true // No identical items found
    }

    fun getStringS():String{
        var s =""
        if (countModelList.value!=null){
            for (i in countModelList.value!!){
                s = s+"${i.merkBarang} kode ${i.kodeBarang};  isi ${i.isi} ${i.satuan}; ${i.psc} pcs\n"
            }
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

    private suspend fun updateDetailWarnaAndInsertBarangLogToDao(
        barangLog: BarangLog,
        refWarna: String,
        detailWarnaIsi: Double,
        detailWarnaPcs: Int,
        loggedInUsers: String?,
        ket:String
    ){
        withContext(Dispatchers.IO){
            //dataSource5.insert(logTable)
            dataSourceBarangLog.insertBarangLogAndUpdateDetailWarna(barangLog,refWarna,detailWarnaIsi,detailWarnaPcs,loggedInUsers,ket)
        }
    }

    private suspend fun getrefMerkByName(name:String):String{
        return withContext(Dispatchers.IO){
            dataSourceMerk.getMerkRefByName(name)!!
        }
    }
    private suspend fun getrefWanraByName(name:String,refMerk:String):String?{
        return withContext(Dispatchers.IO){
            dataSourceWarna.getWarnaRefByName(name,refMerk)
        }
    }
    private suspend fun getrefDetailWanraByWarnaRefndIsi(name:String,isi:Double):String?{
        return withContext(Dispatchers.IO){
            dataSourceDetailWarna.getDetailWarnaRefByIsiAndWarnaRef(name,isi)
        }
    }



    private suspend fun updateBarangLogAndDetailWarna( refMerk: String,
                                                       warnaRef: String,
                                                       detailWarnaRef: String,
                                                       isi: Double,
                                                       pcs: Int,
                                                       barangLogDate: Date,
                                                       refLog: String,
                                                       barangLogRef: String,
                                                       detailWarnaUpdates: List<DetailWarnaTable>,
                                                       loggedInUsers: String?){
        return withContext(Dispatchers.IO){
            dataSourceBarangLog.updateBarangLogAndDetails(
                refMerk,
                warnaRef,
                detailWarnaRef,
                isi,
                pcs,
                barangLogDate,
                refLog,
                barangLogRef,
                detailWarnaUpdates,
                loggedInUsers
            )

        }
    }


    private suspend fun updateDetailWarnaAndDeleteBarangLog( log: LogTable, barangLogList: List<BarangLog>, loggedInUsers: String?){
        withContext(Dispatchers.IO){
            dataSourceLog.deleteLogAndUpdateDetailWarna(  log, barangLogList, loggedInUsers) }
    }

    private suspend fun updateLogToDao(log: LogTable){
        withContext(Dispatchers.IO){
            dataSourceLog.update(log)
        }
    }

    private suspend fun doesBarangLogExist(barangLogRef: String): Boolean {
        return withContext(Dispatchers.IO) {
            dataSourceBarangLog.findByBarangLogRef(barangLogRef) != null
        }
    }

    private suspend fun updateDetailAndDeleteBarangLogToDao(refWarna:String, detailWarnaIsi:Double, detailWarnaPcs:Int, loggedInUsers:String?, id:Int,detailWarnaKet:String){
        withContext(Dispatchers.IO){
            try {
                dataSourceBarangLog.updateDetailAndDeleteBarangLog(refWarna, detailWarnaIsi, detailWarnaPcs, loggedInUsers, id,detailWarnaKet)
            } catch (e: Exception) {
                Log.i("INSERTLOGTRY","updateDetailDeleteLog failed")
                Log.i("INSERTLOGTRY","$e")
            }
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
    private suspend fun insertLogAndUpdateDetailWarna(log:LogTable,barangLogList:List<BarangLog>,loggedInUsers: String?){
        return withContext(Dispatchers.IO){
            Log.i("dataSize", "dao called")
            dataSourceBarangLog.insertLogAndUpdateDetailWarna(log,barangLogList,loggedInUsers)
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