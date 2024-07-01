package com.example.tokomurahinventory.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.tokomurahinventory.models.MerkDummyModel
import java.lang.reflect.Modifier

class MerkViewModel(application: Application): AndroidViewModel(application) {
    var listDummyMerk= mutableListOf<MerkDummyModel>()
    init {
        listDummyMerk.add(MerkDummyModel(1,"CAMARO","sdfas"))
        listDummyMerk.add(MerkDummyModel(1,"CAMARO","sdfas"))
        listDummyMerk.add(MerkDummyModel(1,"CAMARO","sdfas"))
    }
}