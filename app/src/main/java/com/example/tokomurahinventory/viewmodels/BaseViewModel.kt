package com.example.tokomurahinventory.viewmodels

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tokomurahinventory.utils.SharedPreferencesHelper
import com.example.tokomurahinventory.utils.UserRoles
import kotlinx.coroutines.launch

abstract class BaseAndroidViewModel(application: Application) : AndroidViewModel(application) {

    fun canUserDeleteOrUpdate(context: Context, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val userRole = SharedPreferencesHelper.getUserRole(context)
            if (userRole != null) {
                onResult(userRole == UserRoles.ADMIN ||userRole == UserRoles.EDITOR)
            } else {
                onResult(false)
            }
        }
    }
    fun canUserPerformAction(context: Context, action: UserAction, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val userRole = SharedPreferencesHelper.getUserRole(context)
            val hasAccess = when (userRole) {
                UserRoles.ADMIN -> true  // Admins can perform all actions
                UserRoles.EDITOR -> action in listOf(UserAction.VIEW, UserAction.INSERT)  // Editors can view and edit
                UserRoles.VIEWER -> action == UserAction.VIEW  // Viewers can only view
                else -> false  // No role or unknown role
            }
            onResult(hasAccess)
        }
    }
}
enum class UserAction {
    VIEW,
    EDIT,
    DELETE,
    INSERT
}
