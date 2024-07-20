    package com.example.tokomurahinventory.models.model
    
    import java.util.Date
    
    data class CombinedLogData(
        //log
        val logId: Int,
        val userName: String,
        val password: String,
        val namaToko: String,
        val logDate: Date,
        val keterangan: String,
        val merk: String,
        val kodeWarna: String,
        val logIsi: Double,
        val logPcs: Int,
        val detailWarnaRef: String,
        val refLog: String,
        val logLastEditedDate: Date,
        val createdBy: String,
        val lastEditedBy: String,
        var logExtraBool: Boolean = false,
        var logExtraDouble: Double= 0.0,
        var logExtraString: String = "",
        var logTipe:String="",

        //barang log
        val barangLogId: Int,
        val refMerk: String,
        val warnaRef: String,
        val barangLogIsi: Double,
        val barangLogPcs: Int,
        val barangLogDate: Date,
        val barangLogRef: String,
        var barangLogExtraBool: Boolean = false,
        var barangLogExtraDouble: Double= 0.0,
        var barangLogExtraString: String = "",
        var barangLogTipe: String = ""
    )
