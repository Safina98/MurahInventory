package com.example.tokomurahinventory.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.tokomurahinventory.database.BarangLogDao
import com.example.tokomurahinventory.database.DetailWarnaDao
import com.example.tokomurahinventory.database.LogDao
import com.example.tokomurahinventory.database.MerkDao
import com.example.tokomurahinventory.database.UsersDao
import com.example.tokomurahinventory.database.WarnaDao
import com.example.tokomurahinventory.models.LogTable
import com.example.tokomurahinventory.utils.MASUKKELUAR
import com.example.tokomurahinventory.utils.MASUKKELUARSPINNER
import com.example.tokomurahinventory.utils.UpdateStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AllTransViewModel(val dataSourceMerk: MerkDao,
                        val dataSourceWarna: WarnaDao,
                        val dataSourceDetailWarna: DetailWarnaDao,
                        val dataSourceLog: LogDao,
                        val dataSourceBarangLog: BarangLogDao,
                        val dataSourceUsers: UsersDao,
                        val loggedInUser:String,
                        application: Application
)  : AndroidViewModel(application){
    val allMerkFromDb = dataSourceMerk.selectAllNamaMerk()
    private val _codeWarnaByMerk = MutableLiveData<List<String>?>()
    val codeWarnaByMerk: LiveData<List<String>?> get() = _codeWarnaByMerk
    private val _isiByWarnaAndMerk = MutableLiveData<List<Double>?>()
    val isiByWarnaAndMerk: LiveData<List<Double>?> get() = _isiByWarnaAndMerk

    private val _filteredLog = MutableLiveData<List<LogTable>?>()
    val filteredLog: LiveData<List<LogTable>?> get() = _filteredLog

    private val _isLogLoading = MutableLiveData<Boolean>(false)
    val isLogLoading: LiveData<Boolean> get() = _isLogLoading
    private val _isLoadCrashed = MutableLiveData<Boolean>(false)
    val isLoadCrashed: LiveData<Boolean> get() = _isLoadCrashed
    private val _unFilteredLog = MutableLiveData<List<LogTable>>()

    private val _selectedStartDate = MutableLiveData<Date?>()
    val selectedStartDate: LiveData<Date?> get() = _selectedStartDate

    private val _selectedEndDate = MutableLiveData<Date?>()
    val selectedEndDate: LiveData<Date?> get() = _selectedEndDate

    val _dateRangeString = MutableLiveData<String>("Pilih Tanggal")
    val mutableMerk=MutableLiveData<String?>("")
    val mutableKode=MutableLiveData<String?>("")
    val mutableIsi=MutableLiveData<String?>("")
    val mutableTipe=MutableLiveData<String>("")
    val mutableDate = MutableLiveData<String>("")

    private var offset = 0
    private val limit = 50
    private var hasMoreData = true

    //val logs = MutableLiveData<List<LogTable>>()

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
    private fun formatDateRangeM(startDate: Date?, endDate: Date?): String {
        return if (startDate != null && endDate != null) {
            val dateFormat = SimpleDateFormat("EEEE, d MMMM yyyy", Locale("in", "ID"))
            val startDateString = dateFormat.format(startDate)
            val endDateString = dateFormat.format(endDate)
            "$startDateString - $endDateString"
        } else {
            "Semua"
        }
    }
    fun updateDateRangeString(startDate: Date?, endDate: Date?) {
        _dateRangeString.value = formatDateRange(startDate, endDate)
        mutableDate.value=formatDateRangeM(startDate,endDate)
    }
    fun setStartAndEndDateRange(startDate: Date?,endDate: Date?){
        viewModelScope.launch {
            _selectedStartDate.value = startDate
            _selectedEndDate.value = endDate
            Log.i("AllTransProbs","start date:${_selectedStartDate.value}")
            Log.i("AllTransProbs","end date:${_selectedEndDate.value}")
        }
    }

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

    fun checkIfDataExist(merk:String,warna: String?,isi:Double?, callback: (UpdateStatus) -> Unit) {
        viewModelScope.launch {
            // Log initial stat
            val isMerkPresent = checkMerkExisted(merk)
            val isWarnaPresent = if(!warna.isNullOrEmpty())isKodeWarnaInLiveData(codeWarnaByMerk, warna!!) else true
            val isIsiPresent = if (isi!=null)isIsiInLiveData(isiByWarnaAndMerk, isi) else true
            Log.i("AllTransProbs","Warna:$warna")
            // Fetch data from database
            if (isMerkPresent && isWarnaPresent && isIsiPresent) {
                // Update item in list
                callback(UpdateStatus.SUCCESS)
            } else {
                // Revert changes and notify of failure
                callback(
                    when {
                        !isMerkPresent -> UpdateStatus.MERK_NOT_PRESENT
                        !isWarnaPresent -> UpdateStatus.WARNA_NOT_PRESENT
                        !isIsiPresent -> UpdateStatus.ISI_NOT_PRESENT
                        else -> UpdateStatus.ITEM_NOT_FOUND
                    }
                )
            }
        }
    }
    fun loadMoreData(){
        if (!hasMoreData) return // Stop if no more data to load
        val kode=if (mutableKode.value=="Semua") null else mutableKode.value
       performFiltering(mutableMerk.value?:"",kode,mutableIsi.value?.toDoubleOrNull(),mutableTipe.value)
    }
    fun reloadData(){
        val kode=if (mutableKode.value=="Semua") null else mutableKode.value
        updateRv(mutableMerk.value?:"",kode,mutableIsi.value?.toDoubleOrNull(),mutableTipe.value)
    }
    fun resetLogs() {
        offset = 0
        _filteredLog.value = emptyList()
        _unFilteredLog.value= emptyList()
        hasMoreData=true
    }

    fun updateRv(merk: String, kode: String?, isi: Double?, selectedSpinner: String?) {
        viewModelScope.launch {
            _isLogLoading.value = true
            performFiltering(merk,kode,isi,selectedSpinner)
        }
    }
    private fun performFiltering(merk: String, kode: String?, isi: Double?, selectedSpinner: String?){
        viewModelScope.launch {
            _isLoadCrashed.value = false
            val tipe = getTipeFromSpinner(selectedSpinner)
            val startDate=_selectedStartDate.value
            val endDate=_selectedEndDate.value
            setMutableValues(merk,kode?:"Semua",isi,selectedSpinner)
            if (startDate==null){
                updateDateRangeString(null,null)
            }
            try {
                val newLogs = withContext(Dispatchers.IO){dataSourceLog.getLogs(merk, kode, isi, tipe,startDate,endDate,limit,offset)}
                val currentLogs = _unFilteredLog.value.orEmpty()
                Log.e("AllTransProbs", "data size:${newLogs.size} ")
                if (newLogs.size < limit) {
                    hasMoreData = false
                }
                _filteredLog.value = currentLogs + newLogs
                _unFilteredLog.value=currentLogs + newLogs
                offset += limit
                //_filteredLog.value = logList
                //_unFilteredLog.value = logList
            } catch (e: Exception) {
                _isLoadCrashed.value = true
                Log.e("AllTransProbs", "$e")
            } finally {
                _isLogLoading.value = false
            }
        }
    }
    
    private fun getTipeFromSpinner(selectedSpinner: String?): String? {
        return when (selectedSpinner) {
            MASUKKELUARSPINNER.MASUK -> MASUKKELUAR.MASUK
            MASUKKELUARSPINNER.KELUAR -> MASUKKELUAR.KELUAR
            else -> null
        }
    }

    fun setMutableValues(merk:String,kode: String,isi: Double?,tipe:String?){
        mutableMerk.value = merk
        mutableKode.value=kode
        mutableIsi.value=if (isi!=null) String.format(Locale.US,"%.2f", isi) else{"Semua"}
        mutableTipe.value=tipe

    }
    fun setDateStringIfDateNull(){

    }



    fun filterLogQuery(query: String?) {
        val list = mutableListOf<LogTable>()
        if (_unFilteredLog.value!=null){
            if (!query.isNullOrEmpty()) {
                list.addAll(_unFilteredLog.value!!.filter {
                    it.namaToko.lowercase(Locale.getDefault()).contains(query.lowercase(Locale.getDefault())) ||
                            it.userName?.lowercase(Locale.getDefault())?.contains(query.lowercase(
                                Locale.getDefault())) ?: false ||
                            it.merk.lowercase(Locale.getDefault()).contains(query.lowercase(Locale.getDefault()))
                })
            } else {
                list.addAll(_unFilteredLog.value!!)
            }
        }
        _filteredLog.value = list
    }
    private suspend fun checkMerkExisted(namaMerk: String):Boolean{
        return withContext(Dispatchers.IO){
            dataSourceMerk.isDataExists(namaMerk)
        }
    }
    //Untuk cek kode ada di list
    fun isKodeWarnaInLiveData(liveData: LiveData<List<String>?>, value: String): Boolean {
        return liveData.value?.contains(value) == true
    }
    //Untuk Check isi ada di list
    fun isIsiInLiveData(liveData: LiveData<List<Double>?>, value: Double): Boolean {
        return liveData.value?.contains(value) == true
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

}