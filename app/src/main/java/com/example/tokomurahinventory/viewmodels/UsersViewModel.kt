package com.example.tokomurahinventory.viewmodels

import android.app.Application
import android.view.View
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.tokomurahinventory.database.UsersDao
import com.example.tokomurahinventory.models.UsersTable
import com.example.tokomurahinventory.utils.SharedPreferencesHelper
import com.example.tokomurahinventory.utils.UserRoles
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.mindrot.jbcrypt.BCrypt
import java.util.Locale
import java.util.UUID

class UsersViewModel(
    val dataSourceUsers:UsersDao,
    val loggedInUser:String,
    application: Application): BaseAndroidViewModel(application){
    //val usersList = dataSourceUsers.selectAllUsers()
    private val _addUserFab = MutableLiveData<Boolean>()
    val addUserFab: LiveData<Boolean> get() = _addUserFab


    private var viewModelJob = Job()
    //ui scope for coroutines
    private val uiScope = CoroutineScope(Dispatchers.Main +  viewModelJob)

    private var _usersList = MutableLiveData<List<UsersTable>>()
    val usersList :LiveData<List<UsersTable>> get() = _usersList

    private val _unFilteredUsers = MutableLiveData<List<UsersTable>>()
    init {
        getAllUserTable()
    }

    fun getAllUserTable(){
        uiScope.launch {
            val list = withContext(Dispatchers.IO){
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
    fun insertUser(nama: String, password: String,userRole:String) {
        uiScope.launch {
            val userExists = withContext(Dispatchers.IO) {
                dataSourceUsers.checkUserExists(nama) > 0
            }

            if (!userExists) {
                val newUser = UsersTable(
                    userName = nama,
                    password = hashPassword(password),
                    usersRef = UUID.randomUUID().toString(),
                    usersRole = userRole
                )
                insertUsersToDao(newUser)
                getAllUserTable()
            } else {
                // Handle the case where the username already exists
                // e.g., show a toast message
                onAddUserFabClick()
                withContext(Dispatchers.Main) {
                    Toast.makeText(getApplication(), "Username already exists", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    private fun hashPassword(password: String): String {
        return BCrypt.hashpw(password, BCrypt.gensalt())
    }
    fun updateUser(usersTable: UsersTable){
        uiScope.launch {
            if (!isPasswordHashed(usersTable.password)) {
                usersTable.password = hashPassword(usersTable.password)
            }
            updateUsersToDao(usersTable)
            getAllUserTable()
        }
    }
    fun isPasswordHashed(password: String): Boolean {
        return password.startsWith("$2a$") || password.startsWith("$2b$") || password.startsWith("$2y$")
    }

    fun checkIfUserDeletingItSelf(userRole: String): Boolean {
        val loggedInUsers = SharedPreferencesHelper.getLoggedInUser(getApplication())
        return userRole.equals(loggedInUsers, ignoreCase = true)
    }

    fun deleteUser(usersTable: UsersTable){
        uiScope.launch {
            //check if deleted user if admin
            //check if admin deleting it self
            // if admin deleting it self, give warning
            //check from database if there is mode than one admin in the database
            if (usersTable.usersRole==UserRoles.ADMIN) {
                if (isMoreThanOneAdminInDb()){
                    deleteUsersToDao(usersTable)
                }else{
                    Toast.makeText(getApplication(),"Hanya ada 1 admin di database. Buat admin baru sebelum menghapus",Toast.LENGTH_SHORT).show()
                }
            }else deleteUsersToDao(usersTable)
            getAllUserTable()
        }
    }

    private suspend fun isMoreThanOneAdminInDb():Boolean{
        return  withContext(Dispatchers.IO){
            val adminCount = dataSourceUsers.countAdmins("Admin")
            adminCount > 1
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
           // dataSourceUsers.updateCreatedByForDeletedUser(usersTable.userName,usersTable.userName)
            //dataSourceUsers.updateLastEditedByForDeletedUser(usersTable.userName,usersTable.userName)
            //dataSourceUsers.deleteAnItemUser(usersTable.id)
            dataSourceUsers.deleteUserWithReferences(usersTable, usersTable.userName)
        }
    }

    fun onAddUserFabClick() {
        canUserDeleteOrUpdate(getApplication()){canDeleteOrUpdate ->
            if (canDeleteOrUpdate) {
                // Allow delete/update operation
                _addUserFab.value = true
            } else {
                Toast.makeText(getApplication(), "You don't have permission to perform this action", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun onAddUserFabClicked() { _addUserFab.value = false }
    fun onLongClick(v: View): Boolean { return true }
    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}