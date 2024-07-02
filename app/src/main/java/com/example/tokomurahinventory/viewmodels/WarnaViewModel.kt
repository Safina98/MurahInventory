package com.example.tokomurahinventory.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.tokomurahinventory.database.WarnaDao
import com.example.tokomurahinventory.models.MerkTable
import com.example.tokomurahinventory.models.WarnaTable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class WarnaViewModel(
    val dataSourceWarna : WarnaDao,
    val refMerk:String,
    application: Application
): AndroidViewModel(application) {

    //all warna by merk
    val allWarnaByMerk = dataSourceWarna.selectWarnaByMerk(refMerk)

    //Add warna fab
    private val addWarnaFabM = MutableLiveData<Boolean>()
    val addWanraFab: LiveData<Boolean> get() = addWarnaFabM

    //Insert New Warna
    fun insertWarna(kodeWarna:String,satuan:String){
        viewModelScope.launch {
            var warna= WarnaTable()
            warna.refMerk = refMerk
            warna.kodeWarna = kodeWarna
            warna.satuan = satuan
            warna.warnaRef = UUID.randomUUID().toString()
            insertWarnaToDao(warna)
        }
    }
    private suspend fun  insertWarnaToDao(warna:WarnaTable){
        withContext(Dispatchers.IO){
            dataSourceWarna.insert(warna)
        }
    }

    //Navigation
    fun onAddWarnaFabClick(){ addWarnaFabM.value = true }
    fun onAddWarnaFabClicked(){ addWarnaFabM.value = false }

}