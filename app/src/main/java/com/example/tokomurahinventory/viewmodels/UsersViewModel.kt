package com.example.tokomurahinventory.viewmodels

import android.app.Application
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.tokomurahinventory.models.UsersTable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class UsersViewModel(application: Application): AndroidViewModel(application){


    private val _addUserFab = MutableLiveData<Boolean>()
    val addUserFab: LiveData<Boolean> get() = _addUserFab
    var dummyModel = mutableListOf<UsersTable>()

    fun insertUser(nama:String, password:String){
        viewModelScope.launch {
            var user = UsersTable()
            user.nama= nama
            user.passrord = password
            user.usersRef = UUID.randomUUID().toString()
            insertUsersToDao(user)
        }
    }

    private suspend fun insertUsersToDao(usersTable: UsersTable){
        withContext(Dispatchers.IO){
            dummyModel.add(usersTable)
        }
    }

    fun onAddUserFabClick() { _addUserFab.value = true }
    fun onAddUserFabClicked() { _addUserFab.value = false }
    fun onLongClick(v: View): Boolean { return true }

}