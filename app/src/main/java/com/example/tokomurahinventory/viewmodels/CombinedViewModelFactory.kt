package com.example.tokomurahinventory.viewmodels


import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.tokomurahinventory.database.MerkDao
import com.example.tokomurahinventory.database.WarnaDao

class CombinedViewModelFactory(
    private val merkDao: MerkDao,
    private val warnaDao: WarnaDao,
    private val refMerk: String?,
    private val loggedInUser: String,
    private val application: Application
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CombinedViewModel::class.java)) {
            return CombinedViewModel(
                merkDao,
                warnaDao,
                refMerk,
                loggedInUser,
                application
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
