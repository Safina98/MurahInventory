package com.example.tokomurahinventory.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.tokomurahinventory.models.MerkTable

class MerkViewModel(application: Application): AndroidViewModel(application) {
    var listDummyMerk= mutableListOf<MerkTable>()
    init {
        listDummyMerk.add(MerkTable(1,"CAMARO","sdfas"))
        listDummyMerk.add(MerkTable(1,"CAMARO","sdfas"))
        listDummyMerk.add(MerkTable(1,"CAMARO","sdfas"))
    }
}