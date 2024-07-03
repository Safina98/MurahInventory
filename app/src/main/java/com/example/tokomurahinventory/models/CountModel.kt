package com.example.tokomurahinventory.models

data class CountModel(
    var id:Int,
    var kodeBarang: String,
    var isi: Double,
    var psc:Int,
    var logRef:String
)