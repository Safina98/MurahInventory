package com.example.tokomurahinventory.models

import java.util.Date


data class BarangLog(
    var id:Int=0,
    //merk_table merkRef
    var refMerk:String="",
    //warna_table warnaRef
    var refWarna:String="",
    //detail_wanra_table,
    var refDetailWarna:String="",
    var isi:Double=0.0,
    //detail_wanra_table
    var pcs:Int=0,
    var date: Date =Date(),
    //log_table
    var logRef:String=""
)