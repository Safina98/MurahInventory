package com.example.tokomurahinventory.models

import java.util.Date

data class BarangLog(
    var id:Int=0,
    var kodeBarang:String="",
    var isi:Double=0.0,
    var pcs:Int=0,
    var date: Date =Date(),
    var logRef:String=""
)