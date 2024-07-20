package com.example.tokomurahinventory.viewmodels

import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.tokomurahinventory.database.WarnaDao
import com.example.tokomurahinventory.models.MerkTable
import com.example.tokomurahinventory.models.WarnaTable
import com.example.tokomurahinventory.models.model.WarnaModel
import com.example.tokomurahinventory.utils.SharedPreferencesHelper
import com.example.tokomurahinventory.utils.userNullString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.Locale
import java.util.UUID

class WarnaViewModel(
    val dataSourceWarna : WarnaDao,
    val refMerk:String,
    val loggedInUser:String,
    application: Application
): AndroidViewModel(application) {

    //all warna by merk
    //val allWarnaByMerk = dataSourceWarna.selectWarnaByMerk(refMerk)
   // val allWarnaByMerk  = dataSourceWarna.getWarnaWithTotalPcs(refMerk)
    private var viewModelJob = Job()
    //ui scope for coroutines
    private val uiScope = CoroutineScope(Dispatchers.Main +  viewModelJob)


    private var _allWarnaByMerk = MutableLiveData<List<WarnaModel>>()
    val allWarnaByMerk :LiveData<List<WarnaModel>> get() = _allWarnaByMerk
    //Add warna fab
    private val addWarnaFabM = MutableLiveData<Boolean>()
    val addWanraFab: LiveData<Boolean> get() = addWarnaFabM

    //Navigate to detail warna
    private val navigateToDetailWarnaM = MutableLiveData<String>()
    val navigateToDetailWarna: LiveData<String> get() = navigateToDetailWarnaM

    private val _unFilteredWarna = MutableLiveData<List<WarnaModel>>()

init {
    getWarnaByMerk()
}

    fun getWarnaByMerk(){
        uiScope.launch {
            Log.i("FRAGMENT LIFECYCLE","ref merk $refMerk")
            var list = withContext(Dispatchers.IO){
                dataSourceWarna.getWarnaWithTotalPcsList(refMerk)
            }
            Log.i("FRAGMENT LIFECYCLE","list $list")
            _allWarnaByMerk.value = list
            _unFilteredWarna.value = list
        }
    }
    fun filterWarna(query: String?) {
        val list = mutableListOf<WarnaModel>()
        if(!query.isNullOrEmpty()) {
            list.addAll(_unFilteredWarna.value!!.filter {
                it.kodeWarna.lowercase(Locale.getDefault()).contains(query.toString().lowercase(
                    Locale.getDefault()))})
        } else {
            list.addAll(_unFilteredWarna.value!!)
        }
        _allWarnaByMerk.value =list
    }
    //Insert New Warna
    fun insertWarna(kodeWarna:String,satuan:String){
        uiScope.launch {
            var warna= WarnaTable()
            val loggedInUsers = SharedPreferencesHelper.getLoggedInUser(getApplication())
            if (loggedInUsers != null) {
                warna.refMerk = refMerk
                warna.kodeWarna = kodeWarna
                warna.satuan = satuan
                warna.warnaRef = UUID.randomUUID().toString()
                warna.createdBy=loggedInUsers
                warna.lastEditedBy=loggedInUsers
                insertWarnaToDao(warna)
            }else{
                Toast.makeText(getApplication(), userNullString, Toast.LENGTH_SHORT).show()
            }
            getWarnaByMerk()
        }
    }
    fun WarnaModel.toWarnaTable(): WarnaTable {
        return WarnaTable(
            idWarna = this.idWarna,
            refMerk = this.refMerk,
            kodeWarna = this.kodeWarna,
            totalPcs = this.totalPcs,
            satuanTotal = this.satuanTotal,
            satuan = this.satuan,
            warnaRef = this.warnaRef,
            lastEditedBy = loggedInUser,
            createdBy = this.createdBy,
            warnaCreatedDate = this.warnaCreatedDate,
            warnaLastEditedDate = Date()
        )
    }

    fun updateWarna(warnaTable:WarnaModel){
        uiScope.launch {
            updateWarnaToDao(warnaTable.toWarnaTable())
            getWarnaByMerk()

        }
    }
    fun deleteWarna(warnaTable:WarnaModel){
        uiScope.launch {
            deleteWarnaToDao(warnaTable.toWarnaTable())
            getWarnaByMerk()
        }
    }
    private suspend fun  insertWarnaToDao(warna:WarnaTable){
        withContext(Dispatchers.IO){
            dataSourceWarna.insert(warna)
        }
    }
    private suspend fun  deleteWarnaToDao(warna:WarnaTable){
        withContext(Dispatchers.IO){
            dataSourceWarna.deleteAnItemWarna(warna.idWarna)
        }
    }
    private suspend fun  updateWarnaToDao(warna:WarnaTable){
        withContext(Dispatchers.IO){
            dataSourceWarna.update(warna)
        }
    }

    //Navigation
    fun onAddWarnaFabClick(){ addWarnaFabM.value = true }
    fun onAddWarnaFabClicked(){ addWarnaFabM.value = false }
    fun onLongClick(v: View): Boolean { return true }

    fun onNavigateToDetailWarna(refMerk:String){ navigateToDetailWarnaM.value = refMerk }
    @SuppressLint("NullSafeMutableLiveData")
    fun onNavigatetedToDetailWarna(){ navigateToDetailWarnaM.value = null }
    fun clearScope() {
        //super.onCleared()

    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}