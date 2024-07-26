package com.example.tokomurahinventory.viewmodels

import android.app.Application
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.tokomurahinventory.database.BarangLogDao
import com.example.tokomurahinventory.database.DetailWarnaDao
import com.example.tokomurahinventory.database.LogDao
import com.example.tokomurahinventory.database.MerkDao
import com.example.tokomurahinventory.database.UsersDao
import com.example.tokomurahinventory.database.WarnaDao
import com.example.tokomurahinventory.models.BarangLog
import com.example.tokomurahinventory.models.DetailWarnaTable
import com.example.tokomurahinventory.models.LogTable
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
    val dataSourceUsers: UsersDao,
    val loggedInUser:String,
    application: Application
) :AndroidViewModel(application){
    val allMerkFromDb= dataSourceMerk.selectAllMerk()

    private val _insertionCompleted = MutableLiveData<Boolean>()
    val insertionCompleted: LiveData<Boolean>
        get() = _insertionCompleted

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean>
        get() = _isLoading

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

    fun insertCSVBatch(tokensList: List<List<String>>) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                dataSourceMerk.performTransaction {
                    val batchSize = 100 // Define your batch size here
                    for (i in 0 until tokensList.size step batchSize) {
                        val batch = tokensList.subList(i, minOf(i + batchSize, tokensList.size))
                        insertBatch(batch)
                    }
                }
                _insertionCompleted.value = true
            } catch (e: Exception) {
                Log.i("INSERTCSVPROB","exception: $e")
                Toast.makeText(getApplication(), e.toString(), Toast.LENGTH_LONG).show()
            }finally {
                _isLoading.value = false // Hide loading indicator
            }
        }
    }
    private suspend fun insertBatch(batch: List<List<String>>) {
        batch.forEach { tokens ->
            insertCSVN(tokens)
        }
    }
    fun parseDate(dateStr: String): Date {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale("in", "ID"))
        return formatter.parse(dateStr) ?: Date()
    }
    private suspend fun insertCSVN(tokens: List<String>) {
         Log.i("INSERTCSVPROB","size: ${tokens.size}")
        if (tokens.size == 29) {
            // Ensure there are 24 fields
            importMerk(tokens)
        }else if (tokens.size ==5) {
            importUsers(tokens)
        }else {
            importLog(tokens)
        }
        }


    private suspend fun importLog(tokens: List<String>){
        Log.i("INSERTCSVPROB","token ${tokens}")
        val logTable = LogTable().apply {
            userName = tokens[1].trim()
            password = tokens[2].trim()
            namaToko = tokens[3].trim()
            logCreatedDate = parseDate(tokens[4].trim())
            keterangan = tokens[5].trim()
            merk = tokens[6].trim()
            kodeWarna = tokens[7].trim()
            logIsi = tokens[8].trim().toDouble()
            pcs = tokens[9].trim().toInt()
            detailWarnaRef = tokens[10].trim()
            refLog = tokens[11].trim()
            logLastEditedDate = parseDate(tokens[12].trim())
            createdBy = tokens[13].trim()
            lastEditedBy = tokens[14].trim()
            logExtraBool = tokens[15].trim().toBoolean()
            logExtraDouble = tokens[16].trim().toDouble()
            logExtraString = tokens[17].trim()
            logTipe = tokens[18].trim()
        }

        val barangLog = BarangLog().apply {
            refMerk = tokens[20].trim()
            warnaRef = tokens[21].trim()
            detailWarnaRef = tokens[10].trim()  // Matches `LogTable`
            isi = tokens[22].trim().toDouble()
            pcs = tokens[23].trim().toInt()
            barangLogDate = parseDate(tokens[24].trim())
            barangLogRef = tokens[25].trim()
            refLog = tokens[11].trim()
            barangLogExtraBool = tokens[26].trim().toBoolean()
            barangLogExtraDouble = tokens[27].trim().toDouble()
            barangLogExtraString = tokens[28].trim()
            barangLogTipe = tokens[29].trim()
        }

        Log.i("INSERTCSVPROB","log table ${logTable}")
        dataSourceLog.insertLogTable(logTable)
        Log.i("INSERTCSVPROB","log barang ${barangLog}")

        dataSourceBarangLog.insertBarangLogTable(barangLog)
    }
    private suspend fun importUsers(tokens: List<String>){
        val users=UsersTable().apply {
            userName = tokens[1].trim()
            password = tokens[2].trim()
            usersRef = tokens[3].trim()
            usersRole = tokens[4].trim()
        }
        Log.i("INSERTCSVPROB","token ${tokens}")
        Log.i("INSERTCSVPROB","users ${users}")
        dataSourceUsers.insertUsersTable(users)
    }
    fun importMerk(tokens: List<String>){
        Log.i("INSERTCSVPROB","token ${tokens}")
        val merkTable = MerkTable().apply {
            namaMerk = tokens[1].trim()
            refMerk = tokens[2].trim()
            merkCreatedDate = parseDate(tokens[3].trim()) ?: Date()
            merkLastEditedDate = parseDate(tokens[4].trim()) ?: Date()
            user = tokens[5]
            createdBy = tokens[6].trim()
            lastEditedBy = tokens[7].trim()
        }
        Log.i("INSERTCSVPROB","merk table ${merkTable}")
        val warnaTable = WarnaTable().apply {

            kodeWarna = tokens[9].trim()
            totalPcs = tokens[10].trim().toIntOrNull() ?: 0
            satuanTotal = tokens[11].trim().toDoubleOrNull() ?: 0.0
            satuan = tokens[12].trim()
            warnaRef = tokens[13].trim()
            warnaCreatedDate = parseDate(tokens[14].trim()) ?: Date()
            warnaLastEditedDate = parseDate(tokens[15].trim()) ?: Date()
            user = tokens[16].trim()
            createdBy = tokens[17].trim()
            lastEditedBy = tokens[18].trim()
            refMerk = tokens[2].trim()
        }
        Log.i("INSERTCSVPROB","warna table ${warnaTable}")

        val detailWarnaTable = DetailWarnaTable().apply {
            detailWarnaIsi = tokens[20].trim().toDoubleOrNull() ?: 0.0
            detailWarnaPcs = tokens[21].trim().toIntOrNull() ?: 0
            detailWarnaDate = parseDate(tokens[22].trim()) ?: Date()
            detailWarnaLastEditedDate = parseDate(tokens[23].trim()) ?: Date()
            user = tokens[24].trim()
            createdBy = tokens[25].trim()
            lastEditedBy = tokens[26].trim()
            warnaRef = tokens[13].trim()
            detailWarnaRef = tokens[27]
        }
        //Log.i("INSERTCSVPROB","token ${tokens}")


        Log.i("INSERTCSVPROB","detail warna table${detailWarnaTable}")
        dataSourceMerk.insertMerkTable(merkTable)
        dataSourceWarna.insertWarnaTable(warnaTable)
        dataSourceDetailWarna.insertDetailWarnaTable(detailWarnaTable)
        // Use the populated tables as needed


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
                    else -> listOf<Any>()
                }

                for (data in allItems) {
                    val content = when (code.uppercase()) {
                        "MERK" -> {
                            val merkData = data as CombinedDataModel
                            "${escapeCSVField(merkData.merkId.toString())}, ${escapeCSVField(merkData.namaMerk)}, ${escapeCSVField(merkData.refMerk)}, ${escapeCSVField(formatDate(merkData.merkCreatedDate))}, ${escapeCSVField(formatDate(merkData.merkLastEditedDate))}, ${escapeCSVField(merkData.merkUser)},${escapeCSVField(merkData.merkCreatedBy?:"")}, ${escapeCSVField(merkData.merkLastEditedBy?:"")}, ${escapeCSVField(merkData.warnaId.toString())}, ${escapeCSVField(merkData.kodeWarna)}, ${escapeCSVField(merkData.totalPcs.toString())}, ${escapeCSVField(merkData.satuanTotal.toString())}, ${escapeCSVField(merkData.satuan)}, ${escapeCSVField(merkData.warnaRef)}, ${escapeCSVField(formatDate(merkData.warnaCreatedDate))}, ${escapeCSVField(formatDate(merkData.warnaLastEditedDate))},${escapeCSVField(merkData.warnaUser)}, ${escapeCSVField(merkData.warnaCreatedBy?:"")}, ${escapeCSVField(merkData.warnaLastEditedBy?:"")}, ${escapeCSVField(merkData.detailWarnaId.toString())}, ${escapeCSVField(merkData.detailWarnaIsi.toString())}, ${escapeCSVField(merkData.detailWarnaPcs.toString())}, ${escapeCSVField(formatDate(merkData.detailWarnaDate))}, ${escapeCSVField(formatDate(merkData.detailWarnaLastEditedDate))}, ${escapeCSVField(merkData.detailWarnaUser)},${escapeCSVField(merkData.detailWarnaCreatedBy?:"")}, ${escapeCSVField(merkData.detailWarnaLastEditedBy?:"")},${escapeCSVField(merkData.detailWarnaRef?:"")},${merkData.a}"
                        }
                        "LOG" -> {
                            val logData = data as CombinedLogData
                            "${escapeCSVField(logData.logId.toString())}, ${escapeCSVField(logData.userName)}, ${escapeCSVField(logData.password)}, ${escapeCSVField(logData.namaToko)}, ${escapeCSVField(formatDate(logData.logDate))}, ${escapeCSVField(logData.keterangan)}, ${escapeCSVField(logData.merk)}, ${escapeCSVField(logData.kodeWarna)}, ${escapeCSVField(logData.logIsi.toString())}, ${escapeCSVField(logData.logPcs.toString())}, ${escapeCSVField(logData.detailWarnaRef?:"")}, ${escapeCSVField(logData.refLog)}, ${escapeCSVField(formatDate(logData.logLastEditedDate))}, ${escapeCSVField(logData.createdBy?:"")}, ${escapeCSVField(logData.lastEditedBy?:"")}, ${escapeCSVField(logData.logExtraBool.toString())}, ${escapeCSVField(logData.logExtraDouble.toString())}, ${escapeCSVField(logData.logExtraString)},${escapeCSVField(logData.logTipe)}, ${escapeCSVField(logData.barangLogId.toString())}, ${escapeCSVField(logData.refMerk)}, ${escapeCSVField(logData.warnaRef)}, ${escapeCSVField(logData.barangLogIsi.toString())}, ${escapeCSVField(logData.barangLogPcs.toString())}, ${escapeCSVField(formatDate(logData.barangLogDate))}, ${escapeCSVField(logData.barangLogRef)}, ${escapeCSVField(logData.barangLogExtraBool.toString())}, ${escapeCSVField(logData.barangLogExtraDouble.toString())}, ${escapeCSVField(logData.barangLogExtraString)},${escapeCSVField(logData.barangLogTipe)}"
                        }
                        "USERS"->{
                            val userData = data as UsersTable
                            "${userData.id}, ${userData.userName}, ${userData.password}, ${userData.usersRef},${userData.usersRole}"
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
            "MERK"-> "merkId, namaMerk, refMerk, merkCreatedDate, merkLastEditedDate, merkUser,merkCreatedBy, merkLastEditedBy, warnaId, kodeWarna, totalPcs, satuanTotal, satuan, warnaRef, warnaCreatedDate, warnaLastEditedDate, warnaUser,warnaCreatedBy, warnaLastEditedBy, detailWarnaId, detailWarnaIsi, detailWarnaPcs, detailWarnaDate, detailWarnaLastEditedDate, detailWarnaUser,detailWarnaCreatedBy, detailWarnaLastEditedBy,detailWarnaRef,a"
            "USERS" -> "id, userName, password, usersRef"
            "LOG" -> "logId, userName, password, namaToko, logDate, keterangan, merk, kodeWarna, logIsi, logPcs, detailWarnaRef, refLog, logLastEditedDate, createdBy, lastEditedBy, logExtraBool, logExtraDouble, logExtraString, barangLogId, refMerk, warnaRef, barangLogIsi, barangLogPcs, barangLogDate, barangLogRef, barangLogExtraBool, barangLogExtraDouble, barangLogExtraString,barangLogTipe"
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