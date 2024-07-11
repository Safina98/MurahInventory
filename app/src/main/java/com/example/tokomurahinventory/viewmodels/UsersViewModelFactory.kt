package com.example.tokomurahinventory.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.tokomurahinventory.database.MerkDao
import com.example.tokomurahinventory.database.UsersDao

class UsersViewModelFactory(
    private val dataSource1: UsersDao,
    private val loggedInUser: String,
    private val application: Application
): ViewModelProvider.Factory{
    @Suppress("unchecked_cast")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UsersViewModel::class.java)) {
            return UsersViewModel(dataSource1,loggedInUser,application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}