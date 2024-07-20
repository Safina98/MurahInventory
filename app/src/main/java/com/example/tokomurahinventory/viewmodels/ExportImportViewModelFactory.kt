package com.example.tokomurahinventory.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.tokomurahinventory.database.BarangLogDao
import com.example.tokomurahinventory.database.DetailWarnaDao
import com.example.tokomurahinventory.database.LogDao
import com.example.tokomurahinventory.database.MerkDao
import com.example.tokomurahinventory.database.UsersDao
import com.example.tokomurahinventory.database.WarnaDao

class ExportImportViewModelFactory(
    private val dataSourceMerk: MerkDao,
    private val dataSourceWarna: WarnaDao,
    private val dataSourceDetailWarna: DetailWarnaDao,
    private val dataSourceLog: LogDao,
    private val dataSourceBarangLog: BarangLogDao,
    private val dataSourceUsers: UsersDao,
    private val loggedInUser:String,
    private val application: Application
) : ViewModelProvider.Factory{
    @Suppress("unchecked_cast")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExportImportViewModel::class.java)) {
            return ExportImportViewModel(dataSourceMerk,dataSourceWarna,dataSourceDetailWarna,dataSourceLog,dataSourceBarangLog,dataSourceUsers,loggedInUser,application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}