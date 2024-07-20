package com.example.tokomurahinventory.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.tokomurahinventory.database.BarangLogDao
import com.example.tokomurahinventory.models.model.InputStokLogModel
import com.example.tokomurahinventory.utils.MASUKKELUAR
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class InputStokViewModel (
    val dataSourceBarangLog: BarangLogDao,
    val loggedInUser:String,
    application: Application):AndroidViewModel(application) {

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
        viewModelScope.launch {
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
                            log.createdBy.lowercase(Locale.getDefault()).contains(part)

                }
            })
        } else {
            list.addAll(_unFilteredLog.value!!)
        }
        _inputLogModel.value = list
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
               dataSourceBarangLog.getLogMasukByDateRange(startDate,endDate)
            }
            _inputLogModel.value = filteredData
            _unFilteredLog.value = filteredData
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

    //Navigation
    fun onStartDatePickerClick(){ _isStartDatePickerClicked.value = true }
    fun onStartDatePickerClicked(){ _isStartDatePickerClicked.value = false }

}