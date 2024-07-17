package com.example.tokomurahinventory.viewmodels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tokomurahinventory.database.DatabaseInventory
import com.example.tokomurahinventory.models.UsersTable
import com.example.tokomurahinventory.utils.SharedPreferencesHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.mindrot.jbcrypt.BCrypt

class AuthViewModel : ViewModel() {

    private val _authenticationState = MutableLiveData<Boolean>(false)
    val authenticationState: LiveData<Boolean> get() = _authenticationState

    fun authenticate(username: String, password: String, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val userDao = DatabaseInventory.getInstance(context).usersDao
            val user = userDao.getUserByUsername(username) // Fetch user by username
            if (user != null && BCrypt.checkpw(password, user.password)) { // Check the hashed password
                _authenticationState.postValue(true) // Authentication successful
                SharedPreferencesHelper.saveUsername(context, username) // Save username
            } else {
                _authenticationState.postValue(false) // Authentication failed
            }
        }
    }

    fun checkAndInsertDefaultUser(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val userDao = DatabaseInventory.getInstance(context).usersDao
            val userCount = userDao.getUserCount()
            if (userCount == 0) {
                // Insert default user if the table is empty
                val defaultUser = UsersTable(userName = "admin", password = hashPassword("1111"), usersRef = "adminRef")
                userDao.insertUser(defaultUser)
            }
        }
    }
    private fun hashPassword(password: String): String {
        return BCrypt.hashpw(password, BCrypt.gensalt())
    }

}
