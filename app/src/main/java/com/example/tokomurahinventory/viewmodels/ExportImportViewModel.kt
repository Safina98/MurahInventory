package com.example.tokomurahinventory.viewmodels

import android.app.Application
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
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
//import com.example.tokomurahinventory.utils.DataGenerator
import com.example.tokomurahinventory.utils.MASUKKELUAR
import com.example.tokomurahinventory.utils.SharedPreferencesHelper
import com.example.tokomurahinventory.utils.formatDateToStringNullable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.io.IOException
import java.lang.reflect.WildcardType
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID


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

    private val _insertionCompleted = MutableLiveData<Boolean>()
    val insertionCompleted: LiveData<Boolean>
        get() = _insertionCompleted

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean>
        get() = _isLoading

    private val _csvWriteComplete = MutableLiveData<Unit?>()
    val csvWriteComplete: LiveData<Unit?> get() = _csvWriteComplete

    val allMerkFromDb = dataSourceMerk.selectAllNamaMerk()


 //   private val dataGenerator: DataGenerator = DataGenerator(dataSourceBarangLog,dataSourceDetailWarna,dataSourceLog,dataSourceMerk,dataSourceWarna)

    //click export merk
    //muncul pop up autocomplete untuk pilih merk
    //pilih ok
    //show loading
    //write pdf
    //show intent
    //finnished

    fun generateData() {
        viewModelScope.launch {

            try {

                Log.i("GeneratingDummy","staring")
                val allMerk = withContext(Dispatchers.IO){dataSourceMerk.selectAllMerkList()}
                _isLoading.value=true
                //dataGenerator.populateMerk(getApplication(),allMerk)
               // dataGenerator.populateLog(getApplication(),allMerk)
                _isLoading.value=false
            //dataGenerator.populateLog(allMerk)
            } catch (e: Exception) {
               // _isLoading.value=false
                Log.i("GeneratingDummy","$e")
            }
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
            dataSourceUsers.selectAllUsersListForExport()
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
        return try {
            formatter.parse(dateStr) ?: Date()
        } catch (e: ParseException) {
            Date()  // Return today's date if parsing fails
        }
    }
    private suspend fun insertCSVN(tokens: List<String>) {
         Log.i("INSERTCSVPROB","size: ${tokens.size}")
        if (tokens.size == 31) {
            importMerk(tokens)
        }else if (tokens.size ==5) {
            importUsers(tokens)
        }else {
            //importMerk(tokens)
            importLog(tokens)
        }
        }

    private suspend fun insertNewMerk(tokens: List<String>){
        val loggedInUsers = SharedPreferencesHelper.getLoggedInUser(getApplication())
        val merkTable = MerkTable().apply {
            namaMerk = tokens[1].trim()
            refMerk = UUID.randomUUID().toString()
            merkCreatedDate = Date()
            merkLastEditedDate = Date()
            user = loggedInUsers
            createdBy = loggedInUsers
            lastEditedBy = loggedInUsers
        }
        dataSourceMerk.insertMerkTable(merkTable)
    }
    private suspend fun insertNewWarna(tokens: List<String>){
        val loggedInUsers = SharedPreferencesHelper.getLoggedInUser(getApplication())
        val refMerkk = dataSourceMerk.getMerkRefByName(tokens[111])!!
        val warnaTable = WarnaTable().apply {
            kodeWarna = tokens[9].trim()
            totalPcs = tokens[10].trim().toIntOrNull() ?: 0
            satuanTotal = tokens[11].trim().toDoubleOrNull() ?: 0.0
            satuan = tokens[12].trim()
            warnaRef = UUID.randomUUID().toString()
            warnaCreatedDate = Date()
            warnaLastEditedDate = Date()
            user = loggedInUsers
            createdBy = loggedInUsers
            lastEditedBy = loggedInUsers
            refMerk = refMerkk
        }
        dataSourceWarna.insertWarnaTable(warnaTable)
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
        if (barangLog.detailWarnaRef!=""){
            Log.i("INSERTCSVPROB","log table ${logTable}")
            dataSourceLog.insertLogTable(logTable)
            Log.i("INSERTCSVPROB","log barang ${barangLog}")
            dataSourceBarangLog.insertBarangLogTable(barangLog)
        }

    }
    private suspend fun importUsers(tokens: List<String>){
        val users=UsersTable().apply {
            userName = tokens[1].trim()
            password = if (tokens[2].trim()=="0") "0000" else tokens[2].trim()
            usersRef = tokens[3].trim()
            usersRole = tokens[4].trim()
        }
        Log.i("INSERTCSVPROB","token ${tokens}")
        Log.i("INSERTCSVPROB","users ${users}")
        dataSourceUsers.insertUsersTable(users)
    }
    fun importMerk(tokens: List<String>){
        //Log.i("INSERTCSVPROB","token ${tokens}")

        val merkTable = MerkTable().apply {
            namaMerk = tokens[1].trim()
            refMerk = tokens[2].trim()
            merkCreatedDate = parseDate(tokens[3].trim())
            merkLastEditedDate = parseDate(tokens[4].trim())
            user = tokens[5]
            createdBy = tokens[6].trim()
            lastEditedBy = tokens[7].trim()
        }
        //Log.i("INSERTCSVPROB","merk table ${merkTable}")
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
        //Log.i("INSERTCSVPROB","warna table ${warnaTable}")
        val detailWarnaTable = DetailWarnaTable().apply {
            detailWarnaIsi = tokens[20].trim().toDoubleOrNull() ?: 0.0
            detailWarnaPcs = tokens[21].trim().toIntOrNull() ?: 0
            Log.i("INSERTCSVPROB","detail warna date token 29 :${tokens[22]}")
            detailWarnaDate = if (tokens[22]=="-"||tokens[22]==""||tokens[22]=="null") { Date() } else parseDate(tokens[22].trim()) ?: Date()
            Log.i("INSERTCSVPROB","detail warna last edited date token 30 :${tokens[23]}")
            detailWarnaLastEditedDate = if (tokens[23]=="-"||tokens[23]==""||tokens[23]==":null") {Date()} else parseDate(tokens[23].trim()) ?: Date()
            user = if (tokens[24].trim()=="") tokens[25] else tokens[24].trim()
            createdBy = if (tokens[25].trim()=="") null else tokens[25].trim()
            lastEditedBy = if (tokens[26].trim()=="") null else tokens[26].trim()
            warnaRef = tokens[13].trim()
            detailWarnaRef = tokens[27]
            detailWarnaKet=tokens[28]
            dateIn=if (tokens[29]=="-"||tokens[29]=="") {null} else parseDate(tokens[29])
            dateOut=if (tokens[30]=="-"||tokens[30]=="") {null} else parseDate(tokens[30])
        }
        if (tokens[26].trim()!=""){
            Log.i("INSERTCSVPROB","token ${tokens}")
        }
        //Log.i("INSERTCSVPROB","detailwarna table ${detailWarnaTable}")
        dataSourceMerk.insertMerkTable(merkTable)
        dataSourceWarna.insertWarnaTable(warnaTable)
        dataSourceDetailWarna.insertDetailWarnaTable(detailWarnaTable)
        // Use the populated tables as needed
    }

    fun writeCSV(file: File, code: String) {
        viewModelScope.launch {
            Log.i("ExportProbs","write csv called")
            _csvWriteComplete.value = null
            _isLoading.value = true
            try {
                val content = getMerkHeader(code)
                val fw = FileWriter(file.absoluteFile)
                val bw = BufferedWriter(fw)
                bw.write(content)
                bw.newLine()

                Log.i("ExportProbs","write csv stok kode $code")
                val allItems = when (code.uppercase()) {
                    "MERK" -> getAllCombinedData()
                    "LOG" -> getAllCombinedLogData()
                    "USERS" -> getAllUsersData()
                    else -> listOf<Any>()
                }
                Log.i("dataSize","${allItems.size}")
                var i = 0
                withContext(Dispatchers.IO){
                    for (data in allItems) {
                        val content = when (code.uppercase()) {
                            "MERK" -> {
                                val merkData = data as CombinedDataModel
                                "${escapeCSVField(merkData.merkId.toString())}, ${escapeCSVField(merkData.namaMerk)}, ${escapeCSVField(merkData.refMerk)}, ${escapeCSVField(formatDate(merkData.merkCreatedDate)?:"")}, ${escapeCSVField(formatDate(merkData.merkLastEditedDate)?:"")}, ${escapeCSVField(merkData.merkUser)},${escapeCSVField(merkData.merkCreatedBy?:"")}, ${escapeCSVField(merkData.merkLastEditedBy?:"")}, ${escapeCSVField(merkData.warnaId.toString())}, ${escapeCSVField(merkData.kodeWarna)}, ${escapeCSVField(merkData.totalPcs.toString())}, ${escapeCSVField(merkData.satuanTotal.toString())}, ${escapeCSVField(merkData.satuan)}, ${escapeCSVField(merkData.warnaRef)}, ${escapeCSVField(formatDate(merkData.warnaCreatedDate)?:"")}, ${escapeCSVField(formatDate(merkData.warnaLastEditedDate)?:"")},${escapeCSVField(merkData.warnaUser)}, ${escapeCSVField(merkData.warnaCreatedBy?:"")}, ${escapeCSVField(merkData.warnaLastEditedBy?:"")}, ${escapeCSVField(merkData.detailWarnaId.toString())}, ${escapeCSVField(merkData.detailWarnaIsi.toString())}, ${escapeCSVField(merkData.detailWarnaPcs.toString())}, ${escapeCSVField(formatDate(merkData.detailWarnaDate)?:"")}, ${escapeCSVField(formatDate(merkData.detailWarnaLastEditedDate)?:"")}, ${escapeCSVField(merkData.detailWarnaUser)},${escapeCSVField(merkData.detailWarnaCreatedBy?:"")}, ${escapeCSVField(merkData.detailWarnaLastEditedBy?:"")},${escapeCSVField(merkData.detailWarnaRef?:"")},${escapeCSVField(merkData.a?:"")},${escapeCSVField(formatDate(merkData.dateIn))},${escapeCSVField(formatDate(merkData.dateOut))}"
                            }
                            "LOG" -> {
                                val logData = data as CombinedLogData
                                "${escapeCSVField(logData.logId.toString())}, ${escapeCSVField(logData.userName)}, ${escapeCSVField(logData.password)}, ${escapeCSVField(logData.namaToko)}, ${escapeCSVField(formatDate(logData.logDate))}, ${escapeCSVField(logData.keterangan)}, ${escapeCSVField(logData.merk)}, ${escapeCSVField(logData.kodeWarna)}, ${escapeCSVField(logData.logIsi.toString())}, ${escapeCSVField(logData.logPcs.toString())}, ${escapeCSVField(logData.detailWarnaRef?:"")}, ${escapeCSVField(logData.refLog)}, ${escapeCSVField(formatDate(logData.logLastEditedDate))}, ${escapeCSVField(logData.createdBy?:"")}, ${escapeCSVField(logData.lastEditedBy?:"")}, ${escapeCSVField(logData.logExtraBool.toString())}, ${escapeCSVField(logData.logExtraDouble.toString())}, ${escapeCSVField(logData.logExtraString)},${escapeCSVField(logData.logTipe)}, ${escapeCSVField(logData.barangLogId.toString())}, ${escapeCSVField(logData.refMerk)}, ${escapeCSVField(logData.warnaRef)}, ${escapeCSVField(logData.barangLogIsi.toString())}, ${escapeCSVField(logData.barangLogPcs.toString())}, ${escapeCSVField(formatDate(logData.barangLogDate))}, ${escapeCSVField(logData.barangLogRef)}, ${escapeCSVField(logData.barangLogExtraBool.toString())}, ${escapeCSVField(logData.barangLogExtraDouble.toString())}, ${escapeCSVField(logData.barangLogExtraString)},${escapeCSVField(logData.barangLogTipe)}"
                            }
                            "USERS"->{
                                val userData = data as UsersTable
                                "${userData.id}, ${escapeCSVField(userData.userName)}, ${escapeCSVField(userData.password)}, ${escapeCSVField(userData.usersRef)},${escapeCSVField(userData.usersRole)}"
                            }
                            else -> ""
                        }


                        i = i+1
                        bw.write(content)
                        bw.newLine()
                    }
                    Log.i("INSERTCSVPROB","i: $i")

                }

                bw.close()
                Toast.makeText(getApplication(), "Success", Toast.LENGTH_SHORT).show()

            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(getApplication(), "Failed", Toast.LENGTH_SHORT).show()
            }
            _isLoading.value = false
            _csvWriteComplete.postValue(Unit)  // Notify observers
        }
    }
    fun escapeCSVField(field: String?): String? {
        return field?.replace("\"", "\"\"")?.replace("\r\n", " ")?.replace("\n", " ")?.replace("\r", " ")?.replace(","," ")
    }
    fun formatDate(date: Date?): String? {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale("in", "ID"))
        return if (date!=null) formatter.format(date) else null
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

    fun generatePDF(file: File, merk: String) {
        viewModelScope.launch {
            _isLoading.value = true
            withContext(Dispatchers.IO) {
                val pdfDocument = PdfDocument()
                var pageNumber = 1
                var page = createNewPage(pdfDocument, pageNumber)
                var canvas: Canvas = page.canvas
                val paint = Paint().apply {
                    color = Color.BLACK
                    textSize = 16f
                }
                val paintBold = Paint().apply {
                    color = Color.BLACK
                    textSize = 18f
                    typeface = Typeface.DEFAULT_BOLD
                }
                val paintHeader = Paint().apply {
                    color = Color.BLACK
                    textSize = 20f
                    typeface = Typeface.DEFAULT_BOLD
                }
                var y = 30f
                var x =30f

                canvas.drawText("Stok $merk", x, y, paintHeader)
                y += 30f
                val refMerk = dataSourceMerk.getMerkRefByName(merk)!!
                val warnaByMerk = dataSourceWarna.getWarnaWithTotalPcsList(refMerk).sortedBy { it.kodeWarna }

                for (i in warnaByMerk) {
                    y += 30f
                    x =30f
                    // Check if the content fits on the current page
                    if (y + 50f > page.info.pageHeight) { // Adjust threshold as needed
                        pdfDocument.finishPage(page)
                        pageNumber++
                        page = createNewPage(pdfDocument, pageNumber)
                        canvas = page.canvas
                        y = 30f
                    }

                    val detail = dataSourceDetailWarna.getDetailWarnaSummaryList(i.warnaRef)
                    canvas.drawText("Kode ${i.kodeWarna}; ", x, y, paintBold)
                    y += 20f
                    canvas.drawText("Total pcs: ${i.totalDetailPcs} Total isi: ${i.satuanTotal} ${i.satuan}", x, y, paint)
                    y += 5f

                    for (j in detail) {
                        // Check if the content fits on the current page
                        x=40f
                        if (y + 50f > page.info.pageHeight) { // Adjust threshold as needed
                            pdfDocument.finishPage(page)
                            pageNumber++
                            page = createNewPage(pdfDocument, pageNumber)
                            canvas = page.canvas
                            y = 30f
                        }
                        y += 20f
                        canvas.drawText("Isi ${j.detailWarnaIsi} ${j.satuan}; Stok: ${j.detailWarnaPcs} pcs", x, y, paint)

                    }
                }

                // Finish the last page
                pdfDocument.finishPage(page)

                // Save the PDF document
                try {
                    FileOutputStream(file).use { outputStream ->
                        pdfDocument.writeTo(outputStream)
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                } finally {
                    pdfDocument.close()
                }
            }
            _isLoading.value = false
        }
    }

    fun createNewPage(pdfDocument: PdfDocument, pageNumber: Int): PdfDocument.Page {
        val pageInfo = PdfDocument.PageInfo.Builder(595, 841, pageNumber).create()
        return pdfDocument.startPage(pageInfo)
    }

    fun setIsCsvCompleteToNull(){
        _csvWriteComplete.value=null
    }
    fun stopLoading() {
        _isLoading.value = false // Hide loading indicator
    }


}