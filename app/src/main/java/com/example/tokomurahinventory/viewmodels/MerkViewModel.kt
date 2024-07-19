package com.example.tokomurahinventory.viewmodels

import android.annotation.SuppressLint
import android.app.Application
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.tokomurahinventory.database.MerkDao
import com.example.tokomurahinventory.models.MerkTable
import com.example.tokomurahinventory.models.UsersTable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.math.log

class MerkViewModel(
    val dataSource1 :MerkDao,
    val loggedInUser:String,
    application: Application

): AndroidViewModel(application) {
    private var viewModelJob = Job()
    //ui scope for coroutines
    private val uiScope = CoroutineScope(Dispatchers.Main +  viewModelJob)
    //all merkTable from db
    //var allMerkTable= dataSource1.selectAllMerk()
    private var _allMerkTable = MutableLiveData<List<MerkTable>>()
    val allMerkTable :LiveData<List<MerkTable>> get() = _allMerkTable

    //Add merk fab
    private val addMerkFabM = MutableLiveData<Boolean>()
    val addMerkFab: LiveData<Boolean> get() = addMerkFabM

    //search query
    private val _unFilteredMerk = MutableLiveData<List<MerkTable>>()

    //Add navigation
    private val navigateToWarnaM = MutableLiveData<String>()
    val navigateToWarna: LiveData<String> get() = navigateToWarnaM

    init {
        getAllMerkTable()
    }

    fun getAllMerkTable(){
        uiScope.launch {
            var list = withContext(Dispatchers.IO){
                dataSource1.selectAllMerkList()
            }
            _allMerkTable.value = list
            _unFilteredMerk.value = list
        }
    }


    fun filterMerk(query: String?) {
        val list = mutableListOf<MerkTable>()
        if(!query.isNullOrEmpty()) {
            list.addAll(_unFilteredMerk.value!!.filter {
                it.namaMerk.lowercase(Locale.getDefault()).contains(query.toString().lowercase(
                    Locale.getDefault()))})
        } else {
            list.addAll(_unFilteredMerk.value!!)
        }
        _allMerkTable.value =list
    }
    fun insertMerk(namaMerk:String){
        uiScope.launch {
            var merk= MerkTable()
            merk.namaMerk = namaMerk
            merk.lastEditedBy = loggedInUser
            merk.createdBy=loggedInUser
            merk.merkCreatedDate=Date()
            merk.merkLastEditedDate=Date()
            merk.refMerk = UUID.randomUUID().toString()
            insertMerkToDao(merk)
            getAllMerkTable()
        }
    }
    fun updateMerk(merkTable:MerkTable){
        uiScope.launch {
            merkTable.lastEditedBy = loggedInUser
            merkTable.merkLastEditedDate = Date()
            updateMerkToDao(merkTable)
            getAllMerkTable()
        }
    }
    fun deleteMerk(merkTable: MerkTable){
        uiScope.launch {
            deleteMerkToDao(merkTable)
            getAllMerkTable()
        }
    }

    private suspend fun insertMerkToDao(merkTable: MerkTable){
        withContext(Dispatchers.IO){
            dataSource1.insert(merkTable)
        }

    }
    private suspend fun updateMerkToDao(merkTable: MerkTable){
        withContext(Dispatchers.IO){
            dataSource1.update(merkTable)
        }
    }
    private suspend fun deleteMerkToDao(merkTable: MerkTable){
        withContext(Dispatchers.IO){
            dataSource1.deleteAnItemMerk(merkTable.id)
        }
    }
    //Navigation
    fun onAddMerkFabClick(){ addMerkFabM.value = true }
    fun onAddMerkFabClicked(){ addMerkFabM.value = false }
    fun onLongClick(v: View): Boolean { return true }


    fun onNavigateToWarna(refMerk:String){ navigateToWarnaM.value = refMerk }
    @SuppressLint("NullSafeMutableLiveData")
    fun onNavigatetedToWarna(){ navigateToWarnaM.value = null }
    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}