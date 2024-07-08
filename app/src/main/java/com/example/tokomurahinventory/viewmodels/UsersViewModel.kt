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
import java.util.Locale
import java.util.UUID

class UsersViewModel(
    val dataSourceUsers:UsersDao,
    application: Application): AndroidViewModel(application){

    //val usersList = dataSourceUsers.selectAllUsers()
    private val _addUserFab = MutableLiveData<Boolean>()
    val addUserFab: LiveData<Boolean> get() = _addUserFab
    var dummyModel = mutableListOf<UsersTable>()

    private var _usersList = MutableLiveData<List<UsersTable>>()
    val usersList :LiveData<List<UsersTable>> get() = _usersList

    private val _unFilteredUsers = MutableLiveData<List<UsersTable>>()
    init {
        getAllUserTable()
    }

    fun getAllUserTable(){
        viewModelScope.launch {
            var list = withContext(Dispatchers.IO){
                dataSourceUsers.selectAllUsersList()
            }
            _usersList.value = list
            _unFilteredUsers.value = list
        }
    }


    fun filterUsers(query: String?) {
        val list = mutableListOf<UsersTable>()
        if(!query.isNullOrEmpty()) {
            list.addAll(_unFilteredUsers.value!!.filter {
                it.userName.lowercase(Locale.getDefault()).contains(query.toString().lowercase(
                    Locale.getDefault()))})
        } else {
            list.addAll(_unFilteredUsers.value!!)
        }
        _usersList.value =list
    }
    fun insertUser(nama:String, password:String){
        viewModelScope.launch {
            var user = UsersTable()
            user.userName= nama
            user.password = password
            user.usersRef = UUID.randomUUID().toString()
            insertUsersToDao(user)
            getAllUserTable()
        }
    }
    fun updateUser(usersTable: UsersTable){
        viewModelScope.launch {
            updateUsersToDao(usersTable)
            getAllUserTable()
        }
    }
    fun deleteUser(usersTable: UsersTable){
        viewModelScope.launch {
            deleteUsersToDao(usersTable)
            getAllUserTable()
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