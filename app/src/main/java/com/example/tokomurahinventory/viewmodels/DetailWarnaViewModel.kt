package com.example.tokomurahinventory.viewmodels

import android.app.Application
import android.util.Log
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

    fun DetailWarnaModel.toDetailWarnaTable(): DetailWarnaTable {
        return DetailWarnaTable(
            detailWarnaIsi = this.detailWarnaIsi,
            detailWarnaPcs = this.detailWarnaPcs,
            detailWarnaRef = this.warnaRef
        )
    }



    fun updateDetailWarna(oldDetailWarnaModel:DetailWarnaModel,pcs:Int,isi:Double){
        viewModelScope.launch {
            //for i in pcs, update isi from detail warna where isi = old isi and ref = warna ref
        var list = withContext(Dispatchers.IO){ dataSourceDetailWarna.getFirstDetailWarna(isi,oldDetailWarnaModel.warnaRef,pcs) }
            for (i in list){
                //i.detailWarnaIsi = isi
                updateDetailWarnaToDao(i,isi)
            }
        //updateDetailWarnaToDao(detailWarnaModel.toDetailWarnaTable())
        }
    }
    fun deleteDetailWarna(detailWarnaModel: DetailWarnaModel){
        viewModelScope.launch{

            deleteDetailWarnaToDao(detailWarnaModel.detailWarnaIsi,detailWarnaModel.warnaRef)
        }
    }
    private suspend fun updateDetailWarnaToDao(detailWarnaTable:DetailWarnaTable,newIsi:Double){
        withContext(Dispatchers.IO){
            dataSourceDetailWarna.updateDetailWarna(detailWarnaTable.detailWarnaIsi,newIsi,detailWarnaTable.detailWarnaDate,detailWarnaTable.warnaRef)
        }
    }
    private suspend fun deleteDetailWarnaToDao(isi:Double,warnaRef:String){
        withContext(Dispatchers.IO){
            val records = dataSourceDetailWarna.getDetailWarnaByIsiAndRef(isi, warnaRef)
            Log.i("DETAILWARNAPROB","records $records")
            dataSourceDetailWarna.deteteDetailWarnaByIsi(warnaRef,isi)
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