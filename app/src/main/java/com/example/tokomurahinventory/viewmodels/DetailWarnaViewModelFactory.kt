package com.example.tokomurahinventory.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.tokomurahinventory.database.WarnaDao

class DetailWarnaViewModelFactory (private val dataSource2: WarnaDao,
                                   private val refWarna:String,
                                   private val application: Application
): ViewModelProvider.Factory{
    @Suppress("unchecked_cast")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DetailWarnaViewModel::class.java)) {
            return DetailWarnaViewModel(dataSource2,refWarna,application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}