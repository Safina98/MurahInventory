package com.example.tokomurahinventory.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.tokomurahinventory.database.WarnaDao

class WarnaViewModelFactory(
    private val dataSource2: WarnaDao,
    private val refMerk:String,
    private val loggedInUser:String,
    private val application: Application
): ViewModelProvider.Factory{
    @Suppress("unchecked_cast")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WarnaViewModel::class.java)) {
            return WarnaViewModel(dataSource2,refMerk,loggedInUser,application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}