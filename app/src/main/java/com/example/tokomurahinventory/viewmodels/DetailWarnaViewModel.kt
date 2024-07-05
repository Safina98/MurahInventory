package com.example.tokomurahinventory.viewmodels

import android.app.Application
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.tokomurahinventory.database.DetailWarnaDao
import com.example.tokomurahinventory.database.WarnaDao
import com.example.tokomurahinventory.models.DetailWarnaTable
import com.example.tokomurahinventory.models.model.DetailWarnaModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class DetailWarnaViewModel(val dataSourceWarna : WarnaDao,
                           val dataSourceDetailWarna:DetailWarnaDao,
                           val refWarna:String,
                           application: Application
): AndroidViewModel(application) {
    //Add detail warna fab
    private val _addDetailWarnaFab = MutableLiveData<Boolean>()
    val addDetailWarnaFab: LiveData<Boolean> get() = _addDetailWarnaFab
    var dummyDetail = mutableListOf<DetailWarnaTable>()
    //delete?
    val warna = dataSourceWarna.selectWarnaByWarnaRef(refWarna)
    //detail warna
    //val detailWarnaList = dataSourceDetailWarna.selectDetailWarnaByWarnaIdGroupByIsi(refWarna)
    val detailWarnaList = dataSourceDetailWarna.getDetailWarnaSummary(refWarna)

    fun insertDetailWarna(pcs: Int, isi: Double) {
        viewModelScope.launch {
            for (i in 1..pcs) {
                var detailWarnaTable = DetailWarnaTable()
                detailWarnaTable.warnaRef = refWarna
                detailWarnaTable.detailWarnaIsi = isi
                detailWarnaTable.detailWarnaPcs = 1
                detailWarnaTable.detailWarnaRef = UUID.randomUUID().toString()
                insertDetailWarnaToDao(detailWarnaTable)
            }
        }
    }
    /*
    fun DetailWarnaModel.toDetailWarnaTable(): DetailWarnaTable {
        return DetailWarnaTable(
            id = this.id,
            warnaRef = this.warnaRef,
            detailWarnaIsi = this.detailWarnaIsi,
            detailWarnaPcs = 1,
            detailWarnaDate = this.detailWarnaDate,
            detailWarnaRef = this.detailWarnaRef
        )
    }

     */

    fun updateDetailWarna(detailWarnaModel:DetailWarnaModel){
        viewModelScope.launch {
            //updateDetailWarnaToDao(detailWarnaModel.toDetailWarnaTable())
        }
    }
    fun deleteDetailWarna(detailWarnaModel: DetailWarnaModel){
        viewModelScope.launch{
           // deleteDetailWarnaToDao(detailWarnaModel.toDetailWarnaTable())
        }
    }
    private suspend fun updateDetailWarnaToDao(detailWarnaTable:DetailWarnaTable){
        withContext(Dispatchers.IO){
            dataSourceDetailWarna.update(detailWarnaTable)
        }
    }
    private suspend fun deleteDetailWarnaToDao(detailWarnaTable:DetailWarnaTable){
        withContext(Dispatchers.IO){
            dataSourceDetailWarna.deleteAnItemMerk(detailWarnaTable.id)
        }
    }
    private suspend fun insertDetailWarnaToDao(detailWarnaTable: DetailWarnaTable) {
        withContext(Dispatchers.IO) {
            //dummyDetail.add(detailWarnaTable)
            dataSourceDetailWarna.insert(detailWarnaTable)
        }
    }

    fun onAddWarnaFabClick() { _addDetailWarnaFab.value = true }
    fun onAddWarnaFabClicked() { _addDetailWarnaFab.value = false }
    fun onLongClick(v: View): Boolean { return false }
}