package com.example.tokomurahinventory.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.tokomurahinventory.database.BarangLogDao
import com.example.tokomurahinventory.database.DetailWarnaDao
import com.example.tokomurahinventory.database.InputLogDao
import com.example.tokomurahinventory.database.LogDao
import com.example.tokomurahinventory.database.WarnaDao

class DetailWarnaViewModelFactory (private val dataSource2: WarnaDao,
                                   private val dataSourceDetailWarna:DetailWarnaDao,
                                   private val dataSourceInputLogDao: InputLogDao,
                                   private val dataSourceLogDao: LogDao,
                                   private val dataSourceBarangLogDao: BarangLogDao,
                                   private val refWarna:String,
                                   private val loggedInUser:String,
                                   private val application: Application
): ViewModelProvider.Factory{
    @Suppress("unchecked_cast")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DetailWarnaViewModel::class.java)) {
            return DetailWarnaViewModel(dataSource2,dataSourceDetailWarna,dataSourceInputLogDao,dataSourceLogDao,dataSourceBarangLogDao,refWarna,loggedInUser,application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}