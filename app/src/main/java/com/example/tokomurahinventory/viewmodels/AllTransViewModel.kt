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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

    fun updateRv(merk:String,kode:String,isi:Double?){
        viewModelScope.launch {
            _isLogLoading.value = true
            _isLoadCrashed.value = false
            performFiltering(merk,kode,isi)
        }
    }

    private suspend fun performFiltering(merk:String,kode:String,isi:Double?){
        withContext(Dispatchers.IO) {
            try {
                val logList= mutableListOf<LogTable>()
                val refMerk = dataSourceMerk.getMerkRefByName(merk)
                if (refMerk!=null){
                    val refWarna = dataSourceWarna.getWarnaRefByName(kode,refMerk)
                    if(refWarna!=null) {
                        val detailWarnaList = dataSourceDetailWarna.getDetailWarnaListByWarnaRefAndIsi(refWarna,isi)
                        for (detailWarna in detailWarnaList){
                            val barangLogList = dataSourceBarangLog.selectBarangLogByLogDetailWarnaRef(detailWarna!!.detailWarnaRef)
                            for ( i in barangLogList){
                                val log = dataSourceLog.getLogById(i.refLog)
                                logList.add(log)
                            }
                            withContext(Dispatchers.Main) {
                                _isLogLoading.value = false
                                _filteredLog.value = logList
                                _unFilteredLog.value = logList
                            }
                        }

                    }else{}
                }else{}

            }catch (e:Exception){
                withContext(Dispatchers.Main) {
                    _isLogLoading.value = false
                    _isLoadCrashed.value = true
                    Log.i("AllTransProbs","${e}")
                }
            }

        }
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