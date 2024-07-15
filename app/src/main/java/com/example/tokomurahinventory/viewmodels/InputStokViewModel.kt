package com.example.tokomurahinventory.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.tokomurahinventory.database.DetailWarnaDao
import com.example.tokomurahinventory.database.InputLogDao
import com.example.tokomurahinventory.database.WarnaDao

class InputStokViewModel (
    val dataSourceInputLog: InputLogDao,
    val loggedInUser:String,
    application: Application):AndroidViewModel(application) {


}