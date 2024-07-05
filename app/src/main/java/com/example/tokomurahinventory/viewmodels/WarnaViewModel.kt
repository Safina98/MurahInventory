package com.example.tokomurahinventory.viewmodels

import android.annotation.SuppressLint
import android.app.Application
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.tokomurahinventory.database.WarnaDao
import com.example.tokomurahinventory.models.MerkTable
import com.example.tokomurahinventory.models.WarnaTable
import com.example.tokomurahinventory.models.model.WarnaModel
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
    //val allWarnaByMerk = dataSourceWarna.selectWarnaByMerk(refMerk)
    val allWarnaByMerk  = dataSourceWarna.getWarnaWithTotalPcs(refMerk)

    //Add warna fab
    private val addWarnaFabM = MutableLiveData<Boolean>()
    val addWanraFab: LiveData<Boolean> get() = addWarnaFabM

    //Navigate to detail warna
    private val navigateToDetailWarnaM = MutableLiveData<String>()
    val navigateToDetailWarna: LiveData<String> get() = navigateToDetailWarnaM

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
    fun WarnaModel.toWarnaTable(): WarnaTable {
        return WarnaTable(
            idWarna = this.idWarna,
            refMerk = this.refMerk,
            kodeWarna = this.kodeWarna,
            totalPcs = this.totalPcs,
            satuanTotal = this.satuanTotal,
            satuan = this.satuan,
            warnaRef = this.warnaRef
        )
    }

    fun updateWarna(warnaTable:WarnaModel){
        viewModelScope.launch {
            updateWarnaToDao(warnaTable.toWarnaTable())

        }
    }
    fun deleteWarna(warnaTable:WarnaModel){
        viewModelScope.launch {
            deleteWarnaToDao(warnaTable.toWarnaTable())
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

}