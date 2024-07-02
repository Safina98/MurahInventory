package com.example.tokomurahinventory.viewmodels

import android.annotation.SuppressLint
import android.app.Application
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.tokomurahinventory.database.MerkDao
import com.example.tokomurahinventory.models.MerkTable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class MerkViewModel(
    val dataSource1 :MerkDao,
    application: Application

): AndroidViewModel(application) {
    var listDummyMerk= mutableListOf<MerkTable>()

    //all merkTable from db
    val allMerkTable= dataSource1.selectAllMerk()

    //Add merk fab
    private val addMerkFabM = MutableLiveData<Boolean>()
    val addMerkFab: LiveData<Boolean> get() = addMerkFabM

    //Add navigation
    private val navigateToWarnaM = MutableLiveData<String>()
    val navigateToWarna: LiveData<String> get() = navigateToWarnaM
    init {
        listDummyMerk.add(MerkTable(1,"CAMARO","sdfas"))
        listDummyMerk.add(MerkTable(1,"CAMARO","sdfas"))
        listDummyMerk.add(MerkTable(1,"CAMARO","sdfas"))
    }

    fun insertMerk(namaMerk:String){
        viewModelScope.launch {
            var merk= MerkTable()
            merk.namaMerk = namaMerk
            merk.refMerk = UUID.randomUUID().toString()
            insertMerkToDao(merk)
        }
    }
    private suspend fun insertMerkToDao(merkTable: MerkTable){
        withContext(Dispatchers.IO){
            dataSource1.insert(merkTable)
        }

    }
    //Navigation
    fun onAddMerkFabClick(){ addMerkFabM.value = true }
    fun onAddMerkFabClicked(){ addMerkFabM.value = false }
    fun onLongClick(v: View): Boolean { return true }


    fun onNavigateToWarna(refMerk:String){ navigateToWarnaM.value = refMerk }
    @SuppressLint("NullSafeMutableLiveData")
    fun onNavigatetedToWarna(){ navigateToWarnaM.value = null }
}