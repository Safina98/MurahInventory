package com.example.tokomurahinventory.viewmodels

import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.example.tokomurahinventory.database.BarangLogDao
import com.example.tokomurahinventory.database.DatabaseInventory
import com.example.tokomurahinventory.database.DetailWarnaDao
import com.example.tokomurahinventory.database.InputLogDao
import com.example.tokomurahinventory.database.LogDao
import com.example.tokomurahinventory.database.MerkDao
import com.example.tokomurahinventory.database.UsersDao
import com.example.tokomurahinventory.database.WarnaDao
import com.example.tokomurahinventory.models.DetailWarnaTable
import com.example.tokomurahinventory.models.InputLogTable
import com.example.tokomurahinventory.models.MerkTable
import com.example.tokomurahinventory.models.UsersTable
import com.example.tokomurahinventory.models.WarnaTable
import com.example.tokomurahinventory.models.model.CombinedDataModel
import com.example.tokomurahinventory.models.model.CombinedLogData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExportImportViewModel(
    val dataSourceMerk: MerkDao,
    val dataSourceWarna: WarnaDao,
    val dataSourceDetailWarna: DetailWarnaDao,
    val dataSourceLog: LogDao,
    val dataSourceBarangLog: BarangLogDao,
    val dataSourceInputLog: InputLogDao,
    val dataSourceUsers: UsersDao,
    val loggedInUser:String,
    application: Application
) :AndroidViewModel(application){
    val allMerkFromDb= dataSourceMerk.selectAllMerk()

    //TODO write vendible database
    //TODO write log and BarangLog database
    //TODO write inputLog  database
    //TODO write users database
    init {


    }
    suspend fun getAllMerks(): List<MerkTable> {
        return withContext(Dispatchers.IO){
            dataSourceMerk.selectAllMerkList()
        }
    }
    suspend fun getAllCombinedData(): List<CombinedDataModel> {
        return withContext(Dispatchers.IO){
            dataSourceDetailWarna.getAllCombinedData()
        }
    }
    suspend fun getAllCombinedLogData(): List<CombinedLogData> {
        return withContext(Dispatchers.IO){
            dataSourceLog.getAllCombinedLogData()
        }
    }
    suspend fun getAllUsersData(): List<UsersTable> {
        return withContext(Dispatchers.IO){
            dataSourceUsers.selectAllUsersList()
        }
    }
    suspend fun getAllInputLogData(): List<InputLogTable> {
        return withContext(Dispatchers.IO){
            dataSourceInputLog.selectAllTable()
        }
    }

    fun writeCSV(file: File, code: String) {
        viewModelScope.launch {
            try {
                val content = getMerkHeader(code)
                val fw = FileWriter(file.absoluteFile)
                val bw = BufferedWriter(fw)
                bw.write(content)
                bw.newLine()

                val allItems = when (code.toUpperCase()) {
                    "MERK" -> getAllCombinedData()
                    "LOG" -> getAllCombinedLogData()
                    "USERS" -> getAllUsersData()
                    "INPUT LOG" -> getAllInputLogData()
                    else -> listOf<Any>()
                }

                for (data in allItems) {
                    val content = when (code.toUpperCase()) {
                        "MERK" -> {
                            val merkData = data as CombinedDataModel
                            "${escapeCSVField(merkData.merkId.toString())}, ${escapeCSVField(merkData.namaMerk)}, ${escapeCSVField(merkData.refMerk)}, ${escapeCSVField(formatDate(merkData.merkCreatedDate))}, ${escapeCSVField(formatDate(merkData.merkLastEditedDate))}, ${escapeCSVField(merkData.merkCreatedBy)}, ${escapeCSVField(merkData.merkLastEditedBy)}, ${escapeCSVField(merkData.warnaId.toString())}, ${escapeCSVField(merkData.kodeWarna)}, ${escapeCSVField(merkData.totalPcs.toString())}, ${escapeCSVField(merkData.satuanTotal.toString())}, ${escapeCSVField(merkData.satuan)}, ${escapeCSVField(merkData.warnaRef)}, ${escapeCSVField(formatDate(merkData.warnaCreatedDate))}, ${escapeCSVField(formatDate(merkData.warnaLastEditedDate))}, ${escapeCSVField(merkData.warnaCreatedBy)}, ${escapeCSVField(merkData.warnaLastEditedBy)}, ${escapeCSVField(merkData.detailWarnaId.toString())}, ${escapeCSVField(merkData.detailWarnaIsi.toString())}, ${escapeCSVField(merkData.detailWarnaPcs.toString())}, ${escapeCSVField(formatDate(merkData.detailWarnaDate))}, ${escapeCSVField(formatDate(merkData.detailWarnaLastEditedDate))}, ${escapeCSVField(merkData.detailWarnaCreatedBy)}, ${escapeCSVField(merkData.detailWarnaLastEditedBy)}"
                        }
                        "LOG" -> {
                            val logData = data as CombinedLogData
                            "${escapeCSVField(logData.logId.toString())}, ${escapeCSVField(logData.userName)}, ${escapeCSVField(logData.password)}, ${escapeCSVField(logData.namaToko)}, ${escapeCSVField(formatDate(logData.logDate))}, ${escapeCSVField(logData.keterangan)}, ${escapeCSVField(logData.merk)}, ${escapeCSVField(logData.kodeWarna)}, ${escapeCSVField(logData.logIsi.toString())}, ${escapeCSVField(logData.logPcs.toString())}, ${escapeCSVField(logData.detailWarnaRef)}, ${escapeCSVField(logData.refLog)}, ${escapeCSVField(formatDate(logData.logLastEditedDate))}, ${escapeCSVField(logData.createdBy)}, ${escapeCSVField(logData.lastEditedBy)}, ${escapeCSVField(logData.barangLogId.toString())}, ${escapeCSVField(logData.refMerk)}, ${escapeCSVField(logData.warnaRef)}, ${escapeCSVField(logData.barangLogIsi.toString())}, ${escapeCSVField(logData.barangLogPcs.toString())}, ${escapeCSVField(formatDate(logData.barangLogDate))}, ${escapeCSVField(logData.barangLogRef)}"
                        }
                        "USERS"->{
                            val userData = data as UsersTable
                            "${userData.id}, ${userData.userName}, ${userData.password}, ${userData.usersRef}"
                        }
                        "INPUT LOG"->{
                            val inputLogData = data as InputLogTable
                            "${inputLogData.id},${inputLogData.refMerk}, ${inputLogData.warnaRef}, ${inputLogData.detailWarnaRef},${inputLogData.isi},${inputLogData.pcs},${formatDate(inputLogData.barangLogInsertedDate)},${formatDate(inputLogData.barangLogLastEditedDate)},${inputLogData.createdBy},${inputLogData.lastEditedBy},${inputLogData.inputBarangLogRef}"
                        }
                        else -> ""
                    }
                    bw.write(content)
                    bw.newLine()
                }

                bw.close()
                Toast.makeText(getApplication(), "Success", Toast.LENGTH_SHORT).show()
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(getApplication(), "Failed", Toast.LENGTH_SHORT).show()
            }
        }
    }
    fun escapeCSVField(field: String): String {
        return field.replace("\"", "\"\"").replace("\r\n", " ").replace("\n", " ").replace("\r", " ")
    }
    fun formatDate(date: Date): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale("in", "ID"))
        return formatter.format(date)
    }

    fun getMerkHeader(code:String):String{
        return  when (code){
            "MERK"-> "merkId, namaMerk, refMerk, merkCreatedDate, merkLastEditedDate, merkCreatedBy, merkLastEditedBy, warnaId, kodeWarna, totalPcs, satuanTotal, satuan, warnaRef, warnaCreatedDate, warnaLastEditedDate, warnaCreatedBy, warnaLastEditedBy, detailWarnaId, detailWarnaIsi, detailWarnaPcs, detailWarnaDate, detailWarnaLastEditedDate, detailWarnaCreatedBy, detailWarnaLastEditedBy"
            "USERS" -> "id, userName, password, usersRef"
            "LOG" ->"logId, userName, password, namaToko, logDate, keterangan, merk, kodeWarna, logIsi, logPcs, detailWarnaRef, refLog, logLastEditedDate, createdBy, lastEditedBy, barangLogId, refMerk, warnaRef, barangLogIsi, barangLogPcs, barangLogDate, barangLogRef"
            "INPUT LOG" ->"id, refMerk, warnaRef, detailWarnaRef, isi, pcs, barangLogInsertedDate, barangLogLastEditedDate, createdBy, lastEditedBy, inputBarangLogRef"
            else ->""
        }
    }

    fun exportDataToCSV(context: Context, merks: List<MerkTable>, warnas: List<WarnaTable>, detailWarnas: List<DetailWarnaTable>): File {
        val csvDir = File(context.getExternalFilesDir(null), "csv_exports")
        if (!csvDir.exists()) {
            csvDir.mkdir()
        }

        val merkCsvFile = File(csvDir, "merks.csv")
        merkCsvFile.bufferedWriter().use { out ->
            out.write("ID,Nama Merk,Ref Merk,Merk Created Date,Merk Last Edited Date,Created By,Last Edited By\n")
            merks.forEach { merk ->
                out.write("${merk.id},${merk.namaMerk},${merk.refMerk},${merk.merkCreatedDate},${merk.merkLastEditedDate},${merk.createdBy},${merk.lastEditedBy}\n")
            }
        }

        val warnaCsvFile = File(csvDir, "warnas.csv")
        warnaCsvFile.bufferedWriter().use { out ->
            out.write("ID,Ref Merk,Kode Warna,Total Pcs,Satuan Total,Satuan,Warna Ref,Warna Created Date,Warna Last Edited Date,Created By,Last Edited By\n")
            warnas.forEach { warna ->
                out.write("${warna.idWarna},${warna.refMerk},${warna.kodeWarna},${warna.totalPcs},${warna.satuanTotal},${warna.satuan},${warna.warnaRef},${warna.warnaCreatedDate},${warna.warnaLastEditedDate},${warna.createdBy},${warna.lastEditedBy}\n")
            }
        }

        val detailWarnaCsvFile = File(csvDir, "detail_warnas.csv")
        detailWarnaCsvFile.bufferedWriter().use { out ->
            out.write("ID,Warna Ref,Detail Warna Isi,Detail Warna Pcs,Detail Warna Ref,Detail Warna Date,Detail Warna Last Edited Date,Created By,Last Edited By\n")
            detailWarnas.forEach { detailWarna ->
                out.write("${detailWarna.id},${detailWarna.warnaRef},${detailWarna.detailWarnaIsi},${detailWarna.detailWarnaPcs},${detailWarna.detailWarnaRef},${detailWarna.detailWarnaDate},${detailWarna.detailWarnaLastEditedDate},${detailWarna.createdBy},${detailWarna.lastEditedBy}\n")
            }
        }

        Toast.makeText(context, "CSV files exported to: ${csvDir.absolutePath}", Toast.LENGTH_LONG).show()
        return csvDir // Return the directory containing the CSV files
    }


}