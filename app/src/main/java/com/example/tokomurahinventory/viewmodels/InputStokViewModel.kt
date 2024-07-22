package com.example.tokomurahinventory.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.tokomurahinventory.database.BarangLogDao
import com.example.tokomurahinventory.database.DetailWarnaDao
import com.example.tokomurahinventory.models.BarangLog
import com.example.tokomurahinventory.models.model.InputStokLogModel
import com.example.tokomurahinventory.utils.MASUKKELUAR
import com.example.tokomurahinventory.utils.SharedPreferencesHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class InputStokViewModel (
    val dataSourceBarangLog: BarangLogDao,
    val dataSourceDetailWarna:DetailWarnaDao,
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
    init {
        getAllInputLogModel()
        updateDateRangeString(_selectedStartDate.value, _selectedEndDate.value)
    }

    fun getAllInputLogModel(){
        uiScope.launch {
            var list = withContext(Dispatchers.IO){
                dataSourceBarangLog.getAllLogMasuk(MASUKKELUAR.MASUK)
            }
            _inputLogModel.value=list
            _unFilteredLog.value = list
        }
    }
    fun filterLog(query: String?) {
        val list = mutableListOf<InputStokLogModel>()
        val queryParts = query?.split("\\s+".toRegex())?.map { it.lowercase(Locale.getDefault()) } ?: emptyList()
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
    fun updateRv4(){
        uiScope.launch {
            performDataFiltering(selectedStartDate.value, selectedEndDate.value)
        }
    }
    //filter data from database by date
    private fun performDataFiltering(startDate: Date?, endDate: Date?) {
        uiScope.launch {
            val filteredData = withContext(Dispatchers.IO) {
               dataSourceBarangLog.getLogMasukByDateRange(startDate,endDate)
            }
            _inputLogModel.value = filteredData
            _unFilteredLog.value = filteredData
        }
    }
    fun setStartDateRange(startDate: Date?){
        uiScope.launch {
            _selectedStartDate.value = startDate
        }
    }
    fun setEndDateRange(endDate: Date?){
        uiScope.launch {
            _selectedEndDate.value=endDate
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
        setStartDateRange(null)
        setEndDateRange(null)
        updateDateRangeString(null,null)
    }

    fun deleteInputStok(inputStokLogModel: InputStokLogModel){
        uiScope.launch {
            val loggedInUsers = SharedPreferencesHelper.getLoggedInUser(getApplication())
            val item = getBarangLogFromDB(inputStokLogModel.inputBarangLogRef)
            if (item!=null){
            updateDetailWarnaTODao(item.warnaRef,item.isi,item.pcs,loggedInUsers)
            deleteBarangLogToDao(item.id)
                getAllInputLogModel()
        }
        }
    }
    private suspend fun getBarangLogFromDB(barangLogRef:String):BarangLog?{
       return withContext(Dispatchers.IO){
            dataSourceBarangLog.findByBarangLogRef(barangLogRef)
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
    private suspend fun deleteBarangLogToDao(barangLogId:Int){
        withContext(Dispatchers.IO){
            dataSourceBarangLog.delete(barangLogId)
        }
    }


    //Navigation
    fun onStartDatePickerClick(){ _isStartDatePickerClicked.value = true }
    fun onStartDatePickerClicked(){ _isStartDatePickerClicked.value = false }
    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}