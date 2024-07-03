package com.example.tokomurahinventory.models

import java.util.Date

data class LogTable(
    var id:Int = 0,
    var user:String="",
    var password:String="",
    var namaToko:String="",
    var date: Date =Date(),
    var keterangan:String="",
    var merk:String="",
    var kodeWarna:String="",
    var isi:Double=0.0,
    var pcs:Int=0,
    var detailWarnaRef:String="",
    var logRef:String=""
)