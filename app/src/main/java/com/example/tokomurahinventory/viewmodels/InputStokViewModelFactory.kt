package com.example.tokomurahinventory.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.tokomurahinventory.database.BarangLogDao
import com.example.tokomurahinventory.database.DetailWarnaDao
import com.example.tokomurahinventory.database.LogDao
import com.example.tokomurahinventory.database.MerkDao
import com.example.tokomurahinventory.database.WarnaDao


class InputStokViewModelFactory
    (private val dataSourceBarangLogDao: BarangLogDao,
     private val dataDetailWarnaDao: DetailWarnaDao,
     private val dataSourceMerk: MerkDao,
     private val dataSourceWarna: WarnaDao,
     private val loggedInUser:String,
     private val dataSourceLog: LogDao,
     private val application: Application
): ViewModelProvider.Factory{
    @Suppress("unchecked_cast")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InputStokViewModel::class.java)) {
            return InputStokViewModel(dataSourceBarangLogDao,dataDetailWarnaDao,dataSourceMerk,dataSourceWarna,dataSourceLog,loggedInUser,application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}