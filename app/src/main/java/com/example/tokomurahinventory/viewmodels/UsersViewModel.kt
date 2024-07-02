package com.example.tokomurahinventory.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.tokomurahinventory.database.MerkDao
import com.example.tokomurahinventory.models.UsersTable

class UsersViewModel(application: Application): AndroidViewModel(application){

    var dummyModel = mutableListOf<UsersTable>()
    init {
        dummyModel.add(UsersTable(1,"ss","ss","ss"))
        dummyModel.add(UsersTable(2,"ss","ss","ss"))
    }

}