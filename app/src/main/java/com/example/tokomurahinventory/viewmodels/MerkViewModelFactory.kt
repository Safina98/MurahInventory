package com.example.tokomurahinventory.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.tokomurahinventory.database.MerkDao

class MerkViewModelFactory(
    private val dataSource1: MerkDao,
    private val application: Application
): ViewModelProvider.Factory{
    @Suppress("unchecked_cast")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MerkViewModel::class.java)) {
            return MerkViewModel(dataSource1,application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}