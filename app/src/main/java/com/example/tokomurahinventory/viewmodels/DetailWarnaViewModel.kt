package com.example.tokomurahinventory.viewmodels

import android.app.Application
import android.util.Log
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.tokomurahinventory.database.WarnaDao
import com.example.tokomurahinventory.models.DetailWarnaTable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DetailWarnaViewModel(val dataSourceWarna : WarnaDao,
                           val refWarna:String,
                           application: Application
): AndroidViewModel(application) {
    //Add detail warna fab
    private val _addDetailWarnaFab = MutableLiveData<Boolean>()
    val addDetailWarnaFab: LiveData<Boolean> get() = _addDetailWarnaFab
    var dummyDetail = mutableListOf<DetailWarnaTable>()

    val warna = dataSourceWarna.selectWarnaByWarnaRef(refWarna)

    fun insertDetailWarna(pcs: Int, isi: Double) {
        viewModelScope.launch {
            var detailWarnaTable = DetailWarnaTable()
            detailWarnaTable.warnaRef = refWarna
            detailWarnaTable.meter = isi
            detailWarnaTable.pcs = pcs
            insertDetailWarnaToDao(detailWarnaTable)
        }
    }

    private suspend fun insertDetailWarnaToDao(detailWarnaTable: DetailWarnaTable) {
        withContext(Dispatchers.IO) {
            dummyDetail.add(detailWarnaTable)
        }
    }

    fun onAddWarnaFabClick() { _addDetailWarnaFab.value = true }
    fun onAddWarnaFabClicked() { _addDetailWarnaFab.value = false }
    fun onLongClick(v: View): Boolean { return false }
}