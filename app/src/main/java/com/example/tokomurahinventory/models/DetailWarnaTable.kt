package com.example.tokomurahinventory.models

import java.util.Date

data class DetailWarnaTable(
    var id:Int=0,
    var warnaRef:String="",
    var meter:Double=0.0,
    var pcs:Int=0,
    var date:Date = Date()
)