package com.example.tokomurahinventory.viewmodels

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.tokomurahinventory.database.BarangLogDao
import com.example.tokomurahinventory.database.DetailWarnaDao
import com.example.tokomurahinventory.database.MerkDao
import com.example.tokomurahinventory.database.WarnaDao
import com.example.tokomurahinventory.models.BarangLog
import com.example.tokomurahinventory.models.DetailWarnaTable
import com.example.tokomurahinventory.models.model.InputStokLogModel
import com.example.tokomurahinventory.utils.MASUKKELUAR
import com.example.tokomurahinventory.utils.SharedPreferencesHelper
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

class InputStokViewModel (
    val dataSourceBarangLog: BarangLogDao,
    val dataSourceDetailWarna:DetailWarnaDao,
    private val dataSourceMerk: MerkDao,
    val dataSourceWarna: WarnaDao,
    val loggedInUser:String,
    application: Application):AndroidViewModel(application) {

    private var viewModelJob = Job()
    //ui scope for coroutines
    private val uiScope = CoroutineScope(Dispatchers.Main +  viewModelJob)
    //show or hide start date picker dialog
    private var _isStartDatePickerClicked = MutableLiveData<Boolean>()
    val isStartDatePickerClicked :LiveData<Boolean>get() = _isStartDatePickerClicked
    //Selected Date
    private val _selectedStartDate = MutableLiveData<Date?>()
    val selectedStartDate: LiveData<Date?> get() = _selectedStartDate
    private val _selectedEndDate = MutableLiveData<Date?>()
    val selectedEndDate: LiveData<Date?> get() = _selectedEndDate
    val _dateRangeString = MutableLiveData<String>()

    private var _inputLogModel = MutableLiveData<List<InputStokLogModel>>()
    val inputLogModel : LiveData<List<InputStokLogModel>> get() = _inputLogModel
    private val _unFilteredLog = MutableLiveData<List<InputStokLogModel>>()

    val allMerkFromDb = dataSourceMerk.selectAllNamaMerk()

    private val _codeWarnaByMerk = MutableLiveData<List<String>?>()
    val codeWarnaByMerk: LiveData<List<String>?> get() = _codeWarnaByMerk

    private val _isiByWarnaAndMerk = MutableLiveData<List<Double>?>()
    val isiByWarnaAndMerk: LiveData<List<Double>?> get() = _isiByWarnaAndMerk

    private val _isInputLogLoading = MutableLiveData<Boolean>(false)
    val isInputLogLoading: LiveData<Boolean> get() = _isInputLogLoading

    private val _isLoadCrashed = MutableLiveData<Boolean>(false)
    val isLoadCrashed: LiveData<Boolean> get() = _isLoadCrashed


    init {
        //getAllInputLogModel()
        updateDateRangeString(_selectedStartDate.value, _selectedEndDate.value)
    }
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
        Log.i("InputStokLogProbs", "_selectedStartDate ${_selectedStartDate.value}")
        Log.i("InputStokLogProbs", "_selectedEndDate ${_selectedEndDate.value}")
        updateDateRangeString(null,null)
    }

    //get wanrna for sugestion
    fun getWarnaByMerk(merk:String){
        uiScope.launch {
            val refMerk = withContext(Dispatchers.IO){dataSourceMerk.getMerkRefByName(merk)}
            if (refMerk != null) {
                val stringWarnaList = withContext(Dispatchers.IO) {
                    dataSourceWarna.selectStringWarnaByMerk(refMerk)
                }
                _codeWarnaByMerk.value = stringWarnaList
            }
        }
    }
    //get list isi for sugestion
    fun getIsiByWarnaAndMerk(merk:String,warna:String){
        uiScope.launch {
            val refMerk = withContext(Dispatchers.IO){dataSourceMerk.getMerkRefByName(merk)}
            if (refMerk != null) {
                val refWarna = withContext(Dispatchers.IO){ dataSourceWarna.getWarnaRefByName(warna,refMerk) }
                if (refWarna!=null){
                    val stringWarnaList=withContext(Dispatchers.IO){dataSourceDetailWarna.getIsiDetailWarnaByWarna(refWarna)}
                    _isiByWarnaAndMerk.value = stringWarnaList
                }

            }else Toast.makeText(getApplication(),"Merk atau warna tidak ada di database.",Toast.LENGTH_SHORT).show()
            // isiByWarnaAndMerk.setValue(stringWarnaList.map { it.toString() })
        }
    }
    //filter rv by text
    fun filterLog(query: String?) {
        val list = mutableListOf<InputStokLogModel>()
        val queryParts = query?.split("\\s+".toRegex())?.map { it.lowercase(Locale.getDefault()) } ?: emptyList()
        if (_unFilteredLog.value!=null){
            if (queryParts.isNotEmpty()) {
                list.addAll(_unFilteredLog.value!!.filter { log ->
                    // Check if any part of the query matches either namaMerk or kodeWarna
                    queryParts.all { part ->
                        log.namaMerk.lowercase(Locale.getDefault()).contains(part) ||
                                log.kodeWarna.lowercase(Locale.getDefault()).contains(part) ||
                                log.createdBy?.lowercase(Locale.getDefault())?.contains(part) ?: false
                    }
                })
            } else {
                list.addAll(_unFilteredLog.value!!)
            }
            _inputLogModel.value = list
        }
    }
    //update rv by date
    fun updateRv4(){
        uiScope.launch {
            Log.i("InputStokLogProbs", "updateRv4 called")
            Log.i("InputStokLogProbs", "startDate: ${selectedStartDate.value}")
            Log.i("InputStokLogProbs", "endDate: ${selectedEndDate.value}")
            performDataFiltering(selectedStartDate.value, selectedEndDate.value)
        }
    }
    //filter data from database by date
    private fun performDataFiltering(startDate: Date?, endDate: Date?) {
        uiScope.launch {
            _isInputLogLoading.value = true
            _isLoadCrashed.value=false
            try {
                Log.i("InputStokLogProbs", "perform filtering called")
                Log.i("InputStokLogProbs", "startDate: $startDate")
                Log.i("InputStokLogProbs", "endDate: $endDate")
                val filteredData = withContext(Dispatchers.IO) {
                    dataSourceBarangLog.getLogMasukByDateRange(
                        startDate,
                        endDate,
                        MASUKKELUAR.MASUK
                    )
                }
                withContext(Dispatchers.Main) {
                    _inputLogModel.value = filteredData
                    _unFilteredLog.value = filteredData
                    _isInputLogLoading.value = false
                }
            }catch (e:Exception){
                withContext(Dispatchers.Main) {
                    _isInputLogLoading.value = false
                    _isLoadCrashed.value = true
                }
            }
        }
    }
    fun setStartDateRange(startDate: Date?,endDate: Date?){
        uiScope.launch {
            _selectedStartDate.value = startDate
            _selectedEndDate.value = endDate

        }
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
    fun updateDateRangeString(startDate: Date?, endDate: Date?) {
        _dateRangeString.value = formatDateRange(startDate, endDate)
    }
    fun resetDate(){
        setInitialStartDateAndEndDate()
        updateDateRangeString(null,null)
    }

    fun deleteInputStok(inputStokLogModel: InputStokLogModel){
        uiScope.launch {
            _isInputLogLoading.value = true
            try {
                val loggedInUsers = SharedPreferencesHelper.getLoggedInUser(getApplication())
                val item = getBarangLogFromDB(inputStokLogModel.inputBarangLogRef)
                if (item!=null){
                    val detailWarnaKet = "Stok Masuk Dihapus sebanyak ${inputStokLogModel.pcs} pcs."
                    updataDetailWarnaAndDeleteBarangLogToDao(item.warnaRef, item.isi, item.pcs, loggedInUsers, item.id,detailWarnaKet)
                    updateRv4()
                }
            }catch (e:Exception){
                Toast.makeText(getApplication(),"$e",Toast.LENGTH_SHORT).show()
                Log.e("InsertLogTry", "Error updating detail warna: ${e.message}", e)
            }
            _isInputLogLoading.value = false
        }
    }
    fun convertToBarangLog(input: InputStokLogModel,refMerk:String,refWarna:String,refDetailWarna:String,loggedInUsers:String?,logRef:String): BarangLog {

        return BarangLog(
            // Assuming id is auto-generated in BarangLog, so you can set it to 0 or handle accordingly.
            id = 0, // Change this if id needs to be handled differently
            refMerk = refMerk,
            warnaRef = refWarna,
            detailWarnaRef = refDetailWarna, // Set this if needed, or fetch from a different source
            isi = input.isi,
            pcs = input.pcs,
            barangLogDate = Date(),
            refLog = logRef, // Handle null or default value
            barangLogRef = input.inputBarangLogRef,
            barangLogExtraBool = false, // Set default value or handle if required
            barangLogExtraDouble = 0.0, // Set default value or handle if required
            barangLogExtraString = "", // Set default value or handle if required
            barangLogTipe = MASUKKELUAR.MASUK // Set default value or handle if required
        )
    }
    fun updateInputStok(inputStokLogModel: InputStokLogModel){
        uiScope.launch {
            _isInputLogLoading.value = true
            //get logged in user for last edited
            val loggedInUsers = SharedPreferencesHelper.getLoggedInUser(getApplication())
            val refMerk = getrefMerkByName(inputStokLogModel.namaMerk.uppercase())
            //check if merk and warna in database
            if (refMerk!=null){
                val refWarna = getrefWanraByName(inputStokLogModel.kodeWarna,refMerk)
                if (refWarna!=null){
                    var refDetailWarna = getrefDetailWanraByWarnaRefndIsi(refWarna,inputStokLogModel.isi)
                  //if detail warna in database, then insert
                    if (refDetailWarna==null) {
                        refDetailWarna = UUID.randomUUID().toString()
                        insertDetailWarnaToDao(refWarna,inputStokLogModel.isi,0,loggedInUsers,refDetailWarna)
                    }
                    //get old barang log id from db
                    val item = getBarangLogFromDB(inputStokLogModel.inputBarangLogRef)
                    if (item!=null){
                        //convert input model to newBarangLog
                        val barangNewLog = convertToBarangLog(inputStokLogModel,refMerk,refWarna,refDetailWarna,loggedInUsers,item.refLog)
                        updateDetailWarna(barangNewLog,item)
                        //updateBarangLogToDao(barangNewLog)
                        //getAllInputLogModel()
                }

                }else {
                    Toast.makeText(getApplication(),"Warna tidak ada di database. Input warna terlebih dulu",Toast.LENGTH_SHORT).show()
                    _isInputLogLoading.value=false
                }
            }else {
                _isInputLogLoading.value=false
                Toast.makeText(getApplication(),"Merk tidak ada di database. Input merk terlebih dulu",Toast.LENGTH_SHORT).show()
            }

        }

    }
    fun updateDetailWarna(newBarangLog: BarangLog,oldBarangLog:BarangLog?) {
        uiScope.launch {
            val loggedInUsers = SharedPreferencesHelper.getLoggedInUser(getApplication())
            var selisihPcs = 0
            val detailWarnaUpdates = mutableListOf<DetailWarnaTable>()
            try {
                // Fetch the old record from the database
                if (oldBarangLog != null) {
                    if (oldBarangLog.warnaRef == newBarangLog.warnaRef) {
                        if (oldBarangLog.isi == newBarangLog.isi) {
                            selisihPcs = newBarangLog.pcs - oldBarangLog.pcs
                            Log.i("InsertLogTry", "Difference in pcs: $selisihPcs")
                            detailWarnaUpdates.add(
                                DetailWarnaTable(
                                    warnaRef = oldBarangLog.warnaRef,
                                    detailWarnaIsi = oldBarangLog.isi,
                                    detailWarnaPcs = -selisihPcs,
                                    detailWarnaKet = "Stok barang masuk diubah dari ${oldBarangLog.pcs} pcs menjadi ${newBarangLog.pcs} pcs"
                                )
                            )
                        } else {
                            // Update old log
                            detailWarnaUpdates.add(
                                DetailWarnaTable(
                                    warnaRef = oldBarangLog.warnaRef,
                                    detailWarnaIsi = oldBarangLog.isi,
                                    detailWarnaPcs =  oldBarangLog.pcs,
                                    detailWarnaKet = "Stok barang masuk berkurang ${oldBarangLog.pcs} pcs. Barang masuk diubah menjadi  ${newBarangLog.pcs} pcs isi ${newBarangLog.isi}"
                                )
                            )
                            detailWarnaUpdates.add(
                                DetailWarnaTable(
                                    warnaRef = newBarangLog.warnaRef,
                                    detailWarnaIsi = newBarangLog.isi,
                                    detailWarnaPcs = -newBarangLog.pcs,
                                    detailWarnaKet = "Barang masuk bertambah ${newBarangLog.pcs} pcs",
                                )
                            )
                        }
                    } else {
                        val warna = withContext(Dispatchers.IO){dataSourceWarna.getKodeWarnaByRef(newBarangLog.warnaRef)}
                        val merk = withContext(Dispatchers.IO){dataSourceMerk.getMerkNameByRef(newBarangLog.refMerk)}
                        detailWarnaUpdates.add(
                            DetailWarnaTable(
                                warnaRef = oldBarangLog.warnaRef,
                                detailWarnaIsi = oldBarangLog.isi,
                                detailWarnaPcs = oldBarangLog.pcs,
                                detailWarnaKet = "Barang masuk berkurang ${oldBarangLog.pcs}. Stok barang masuk diubah menjadi ${newBarangLog.pcs} pcs $merk $warna",
                            )
                        )
                        detailWarnaUpdates.add(
                            DetailWarnaTable(
                                warnaRef = newBarangLog.warnaRef,
                                detailWarnaIsi = newBarangLog.isi,
                                detailWarnaPcs = -newBarangLog.pcs,
                                detailWarnaKet = "Barang masuk bertambah ${newBarangLog.pcs}"
                            )
                        )
                    }
                } else {
                    detailWarnaUpdates.add(
                        DetailWarnaTable(
                            warnaRef = newBarangLog.warnaRef,
                            detailWarnaIsi = newBarangLog.isi,
                            detailWarnaPcs =-newBarangLog.pcs,
                            detailWarnaKet = "Barang masuk bertambah ${newBarangLog.pcs}"
                        )
                    )
                }
                Log.e("InsertLogTry", "2 BarangLogDate ${newBarangLog.barangLogDate}")
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
                    loggedInUsers,
                )
                updateRv4()

                //getAllInputLogModel()
            } catch (e: Exception) {
                Toast.makeText(getApplication(),"$e",Toast.LENGTH_SHORT).show()
                Log.e("InsertLogTry", "Error updating detail warna: ${e.message}", e)
                _isInputLogLoading.value = false
            }

        }
    }

    private suspend fun getBarangLogFromDB(barangLogRef:String):BarangLog?{
       return withContext(Dispatchers.IO){
            dataSourceBarangLog.findByBarangLogRef(barangLogRef)
        }
    }

    private suspend fun insertDetailWarnaToDao(refWarna: String, isi: Double, pcs: Int, loggedInUsers: String?,refDetailWarna: String) {
        withContext(Dispatchers.IO){
            val newDetailWarna = DetailWarnaTable(
                warnaRef = refWarna,
                detailWarnaIsi = isi,
                detailWarnaPcs = pcs,
                detailWarnaRef = refDetailWarna,
                user = loggedInUsers,
                detailWarnaDate=Date(),
                detailWarnaLastEditedDate=Date(),
                createdBy=loggedInUsers,
                lastEditedBy = loggedInUsers
            )
            dataSourceDetailWarna.insert(newDetailWarna)
        }
    }

    private suspend fun updataDetailWarnaAndDeleteBarangLogToDao(refWarna: String,
                                                                 detailWarnaIsi: Double,
                                                                 detailWarnaPcs: Int,
                                                                 loggedInUsers: String?,
                                                                 id: Int,
                                                                 detailWarnaKet:String){
        withContext(Dispatchers.IO){
            //dataSourceBarangLog.delete(barangLogId)
            dataSourceBarangLog.updateDetailAndDeleteBarangLog(refWarna,detailWarnaIsi,detailWarnaPcs,loggedInUsers,id,detailWarnaKet)
        }
    }

    private suspend fun getrefMerkByName(name:String):String?{
        return withContext(Dispatchers.IO){
            dataSourceMerk.getMerkRefByName(name)
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
    private suspend fun doesBarangLogExist(barangLogRef: String): Boolean {
        return withContext(Dispatchers.IO) {
            dataSourceBarangLog.findByBarangLogRef(barangLogRef) != null
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
            Log.e("InsertLogTry", "3 BarangLogDate ${barangLogDate}")
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
    //Navigation
    fun onStartDatePickerClick(){ _isStartDatePickerClicked.value = true }
    fun onStartDatePickerClicked(){ _isStartDatePickerClicked.value = false }
    override fun onCleared() {
        super.onCleared()
        clearUiScope()
    }

    fun clearUiScope(){
        viewModelJob.cancel()
    }
}