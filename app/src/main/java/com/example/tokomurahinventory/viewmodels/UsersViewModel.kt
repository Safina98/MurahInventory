package com.example.tokomurahinventory.viewmodels

import android.app.Application
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.tokomurahinventory.database.UsersDao
import com.example.tokomurahinventory.models.UsersTable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class UsersViewModel(
    val dataSourceUsers:UsersDao,
    application: Application): AndroidViewModel(application){


    val usersList = dataSourceUsers.selectAllUsers()
    private val _addUserFab = MutableLiveData<Boolean>()
    val addUserFab: LiveData<Boolean> get() = _addUserFab
    var dummyModel = mutableListOf<UsersTable>()

    fun insertUser(nama:String, password:String){
        viewModelScope.launch {
            var user = UsersTable()
            user.userName= nama
            user.passrord = password
            user.usersRef = UUID.randomUUID().toString()
            insertUsersToDao(user)
        }
    }
    fun updateUser(usersTable: UsersTable){
        viewModelScope.launch {
            updateUsersToDao(usersTable)
        }
    }
    fun deleteUser(usersTable: UsersTable){
        viewModelScope.launch {
        deleteUsersToDao(usersTable)
        }
    }

    private suspend fun insertUsersToDao(usersTable: UsersTable){
        withContext(Dispatchers.IO){
            dataSourceUsers.insert(usersTable)
        }
    }
    private suspend fun updateUsersToDao(usersTable: UsersTable){
        withContext(Dispatchers.IO){
            dataSourceUsers.update(usersTable)
        }
    }
    private suspend fun deleteUsersToDao(usersTable: UsersTable){
        withContext(Dispatchers.IO){
            dataSourceUsers.deleteAnItemUser(usersTable.id)
        }
    }

    fun onAddUserFabClick() { _addUserFab.value = true }
    fun onAddUserFabClicked() { _addUserFab.value = false }
    fun onLongClick(v: View): Boolean { return true }

}