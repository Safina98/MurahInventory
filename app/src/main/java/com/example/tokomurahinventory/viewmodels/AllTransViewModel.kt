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
    val mutableTipe=MutableLiveData<String>("Semua")



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

    fun updateRv(merk: String, kode: String, isi: Double?, selectedSpinner: String?) {
        viewModelScope.launch {
            _isLogLoading.value = true
            _isLoadCrashed.value = false
            val tipe = getTipeFromSpinner(selectedSpinner)
            val startDate=_selectedStartDate.value
            val endDate=_selectedEndDate.value
            Log.i("AllTransProbs","update rv start date:${startDate}")
            Log.i("AllTransProbs","update rv end date:${endDate}")
            Log.i("AllTransProbs", "tipe $tipe")

            try {
                val logList = withContext(Dispatchers.IO){dataSourceLog.getLogs(merk, kode, isi, tipe,startDate,endDate)}
                setMutableValues(merk,kode,isi,selectedSpinner)
                updateDateRangeString(startDate,endDate)
                _filteredLog.value = logList
                _unFilteredLog.value = logList
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
        mutableIsi.value=if (isi!=null) String.format(Locale.US,"%.2f", isi) else{""}
        mutableTipe.value=tipe
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

}