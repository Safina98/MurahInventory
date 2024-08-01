package com.example.tokomurahinventory.fragments

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.example.tokomurahinventory.R
import com.example.tokomurahinventory.database.DatabaseInventory
import com.example.tokomurahinventory.databinding.FragmentExportImportBinding
import com.example.tokomurahinventory.utils.SharedPreferencesHelper
import com.example.tokomurahinventory.viewmodels.ExportImportViewModel
import com.example.tokomurahinventory.viewmodels.ExportImportViewModelFactory
import java.io.*
import java.util.zip.*


class ExportImportFragment : AuthFragment() {
    private lateinit var binding: FragmentExportImportBinding
    private lateinit var viewModel: ExportImportViewModel
    private val PERMISSION_REQUEST_CODE = 200
    private  val TAG = "ZipDB"

    var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        Log.i("Insert Csv", "result Launcher")
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            var isFirstLine = true
            Log.i("InsertCsv", "result Launcher if " + data?.data?.path.toString())
            val tokensList = mutableListOf<List<String>>()
            try {
                context?.contentResolver?.openInputStream(data!!.data!!)?.bufferedReader()
                    ?.forEachLine { line ->
                        if (!isFirstLine) {
                            val tokens: List<String> = line.split(",")
                            tokensList.add(tokens)
                        }
                        isFirstLine = false
                    }
                viewModel.insertCSVBatch(tokensList)
            } catch (e: java.lang.Exception) {
                Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show()
                Log.e("Insert Csv", "Error reading CSV: $e")
            }

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_export_import,container,false)

        val application = requireNotNull(this.activity).application
        val database =DatabaseInventory.getInstance(application)
        val dataSourceLog = DatabaseInventory.getInstance(application).logDao
        val dataSourcebarangLog = DatabaseInventory.getInstance(application).barangLogDao
        val dataSourceMerk =  DatabaseInventory.getInstance(application).merkDao
        val dataSourceWarna =  DatabaseInventory.getInstance(application).warnaDao
        val dataSourceDetailWarna =  DatabaseInventory.getInstance(application).detailWarnaDao

        val dataSourceUsers =  DatabaseInventory.getInstance(application).usersDao

        val loggedInUser = SharedPreferencesHelper.getLoggedInUser(requireContext()) ?: ""
        val viewModelFactory = ExportImportViewModelFactory(dataSourceMerk,dataSourceWarna,dataSourceDetailWarna,dataSourceLog,dataSourcebarangLog,dataSourceUsers,loggedInUser,application)
        binding.lifecycleOwner =this
        viewModel = ViewModelProvider(this,viewModelFactory)
            .get(ExportImportViewModel::class.java)
        if (checkPermission()) {
            //Toast.makeText(context, "Permission Granted", Toast.LENGTH_SHORT).show();
        } else {
            requestPermission()
        }
        binding.viewModel = viewModel

        viewModel.isLoading.observe(viewLifecycleOwner) {
            //viewModel.updateRv4()
            if (it==true){
                binding.progressBar.visibility = View.VISIBLE
                binding.labelProgres.visibility = View.VISIBLE
                binding.btnExportLog.visibility = View.GONE
                binding.btnExportUsers.visibility = View.GONE
                binding.btnExportMerk.visibility = View.GONE
                binding.btnImportMerk.visibility = View.GONE
                binding.exportHeader.visibility = View.GONE
                binding.importHeader.visibility = View.GONE

            }else{
                binding.progressBar.visibility = View.GONE
                binding.labelProgres.visibility = View.GONE
                binding.btnExportLog.visibility = View.VISIBLE
                binding.btnExportUsers.visibility = View.VISIBLE
                binding.btnExportMerk.visibility = View.VISIBLE
                binding.btnImportMerk.visibility = View.VISIBLE
                binding.exportHeader.visibility = View.VISIBLE
                binding.importHeader.visibility = View.VISIBLE
            }
        }

        binding.btnExportMerk.setOnClickListener {
            exportStockCSV("Daftar Merk Toko Murah","MERK")
        }

        binding.btnExportUsers.setOnClickListener {
            exportStockCSV("Daftar Users Toko Murah","USERS")
        }
        binding.btnExportLog.setOnClickListener {
            exportStockCSV("Daftar Log Toko Murah","LOG")
        }
        binding.btnExportLog.setOnClickListener {
            exportDatabase("Daftar Log Toko Murah")
        }
        binding.btnImportMerk.setOnClickListener {
            importCSVStock()
           // viewModel.generateData()
        }
        binding.btnExportDatabase.setOnClickListener {
            shareDatabaseBackup(requireContext())
            Log.i("ZipDB","shareDatabaseBackup called")
        }
        binding.btnImportMerkNew.setOnClickListener {
            closeDatabase(database)
            importZipFile()
        }
        return binding.root
    }
    fun closeDatabase(database: DatabaseInventory) {
        database.close()
    }
    private fun importCSVStock() {
        var fileIntent = Intent(Intent.ACTION_GET_CONTENT)
        fileIntent.type = "text/*"
        try { resultLauncher.launch(fileIntent) }
        catch (e: FileNotFoundException) {
            Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show() }
    }

    private fun checkPermission(): Boolean {
        // checking of permissions.
        val permission1 = ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val permission2 = ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)
        return permission1 == PackageManager.PERMISSION_GRANTED && permission2 == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        // requesting permissions if not provided.
        ActivityCompat.requestPermissions(requireActivity(),arrayOf<String>(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSION_REQUEST_CODE)
    }
    private fun exportStockCSV(fileName:String, code:String) {
        val fileName = fileName
        //var file:File
        val file = File(context?.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName + ".csv")
        Log.i("FilePath","else path: " +file.path.toString())
        viewModel.writeCSV(file,code)
        viewModel.csvWriteComplete.observe(viewLifecycleOwner, Observer {
            if (it!=null){
                val photoURI: Uri = FileProvider.getUriForFile(this.requireContext(), requireContext().applicationContext.packageName + ".provider",file)
                val shareIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_STREAM, photoURI)
                    type = "text/*"
                }
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                try {
                    startActivity(Intent.createChooser(shareIntent, "Stok"))
                }catch (e : Exception){
                    Log.i("error_msg",e.toString())
                }
            }
        })
    }
    private fun importZipFile() {
        Log.i("ZipDB","importZipFile started")
        val fileIntent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "application/zip"
        }

        try {
            resultLauncherNew.launch(fileIntent.type)
        } catch (e: FileNotFoundException) {
            Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show()
        }
    }
    private val resultLauncherNew = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            Log.i("ZipDB","result Launcer new")
            val tempFile = readFileFromUri(requireContext(), it)
            Log.i("ZipDB","file ${tempFile?.name}")
            Log.i("ZipDB","file path ${tempFile?.absolutePath}")
            if (tempFile?.exists() == true) {
                try {
                    extractZipFile(tempFile)
                } catch (e: IOException) {
                    Log.e("ZipDB", "Error extracting zip file", e)
                    Toast.makeText(requireContext(), "Error extracting file", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "File does not exist", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun extractZipFile(zipFile: File) {
        val zipInputStream = ZipInputStream(FileInputStream(zipFile))
        var zipEntry: ZipEntry? = zipInputStream.nextEntry

        while (zipEntry != null) {
            val outputFile = File(requireContext().getDatabasePath(zipEntry.name).parent, zipEntry.name)
            if (zipEntry.isDirectory) {
                outputFile.mkdirs()
            } else {
                FileOutputStream(outputFile).use { outputStream ->
                    val buffer = ByteArray(1024)
                    var length: Int
                    while (zipInputStream.read(buffer).also { length = it } > 0) {
                        outputStream.write(buffer, 0, length)
                    }
                }
            }
            zipEntry = zipInputStream.nextEntry
        }
        zipInputStream.closeEntry()
        zipInputStream.close()

        Log.i("ZipDB", "Files extracted successfully")
        Toast.makeText(requireContext(), "Files extracted successfully", Toast.LENGTH_SHORT).show()
    }
    fun extractAndReplaceDatabase(zipFile: File) {
        val context = requireContext() // Ensure to use the correct context
        val dbDir = context.getDatabasePath("inventory_table.db").parentFile
        val dbName = "inventory_table.db" // Update if the name is different

        try {
            val zipInputStream = ZipInputStream(FileInputStream(zipFile))
            var zipEntry: ZipEntry?

            while (zipInputStream.nextEntry.also { zipEntry = it } != null) {
                if (zipEntry?.name?.endsWith(".db") == true) {
                    val outputFile = File(dbDir, dbName)
                    outputFile.outputStream().use { output ->
                        zipInputStream.copyTo(output)
                    }
                    Log.i("ZipDB", "Successfully extracted and replaced database file: ${outputFile.absolutePath}")
                }
            }
            zipInputStream.closeEntry()
            zipInputStream.close()
            reinitializeRoom(requireContext(),"/data/data/com.example.tokomurahinventory/databases/inventory_table.db")
        } catch (e: IOException) {
            Log.e("ZipDB", "Error extracting database from zip file", e)
        }
    }

    private fun readFileFromUri(context: Context, uri: Uri): File? {
        return try {
            // Get the content resolver to open an input stream from the URI
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                // Create a temporary file to store the contents
                val tempFile = File.createTempFile("imported_db", ".zip", context.cacheDir)
                FileOutputStream(tempFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
                tempFile
            }
        } catch (e: SecurityException) {
            Log.e("FileReadError", "SecurityException while reading file from URI: ${e.localizedMessage}", e)
            null
        } catch (e: IOException) {
            Log.e("FileReadError", "IOException while reading file from URI: ${e.localizedMessage}", e)
            null
        } catch (e: Exception) {
            Log.e("FileReadError", "Exception while reading file from URI: ${e.localizedMessage}", e)
            null
        }
    }
    fun replaceDatabase(context: Context, newDatabaseFilePath: String) {
        val databaseName = "inventory_table.db"
        val dbPath = context.getDatabasePath(databaseName).absolutePath
        val walPath = "$dbPath-wal"
        val shmPath = "$dbPath-shm"

        // Step 1: Delete existing database and associated files
        deleteFileSafely(dbPath)
        deleteFileSafely(walPath)
        deleteFileSafely(shmPath)

        // Step 2: Copy the new database file to the database path
        try {
            val newDatabaseFile = File(newDatabaseFilePath)
            val destinationFile = File(dbPath)
            copyFile(newDatabaseFile, destinationFile)
            reinitializeRoom(requireContext(),destinationFile.absolutePath)
        } catch (e: IOException) {
            Log.e(TAG, "Error copying new database file: ${e.localizedMessage}", e)
        }
    }

    private fun deleteFileSafely(filePath: String) {
        val file = File(filePath)
        if (file.exists()) {
            if (file.delete()) {
                Log.i(TAG, "Deleted: $filePath")
            } else {
                Log.e(TAG, "Failed to delete: $filePath")
            }
        }
    }

    private fun copyFile(sourceFile: File, destinationFile: File) {
        FileInputStream(sourceFile).use { input ->
            FileOutputStream(destinationFile).use { output ->
                input.copyTo(output)
            }
        }
    }


    fun replaceDatabaseFiles(context: Context, extractedFiles: List<File>) {
        Log.i("ZipDB","replace database called")
        val dbPath = context.getDatabasePath("inventory_table.db").absolutePath
        Log.i("ZipDB","dbPath: ${dbPath}")
        val walPath = "$dbPath-wal"
        Log.i("ZipDB","walPath: ${walPath}")
        val shmPath = "$dbPath-shm"
        Log.i("ZipDB","shmPath: ${shmPath}")
        //val dbshm: File = File(dbfile.getPath() + "-shm")
        //val dbwal: File = File(dbfile.getPath() + "-wal")
       // deleteWalAndShmFiles(requireContext())
        // Define old and new paths

        val fileMap = mapOf(
            "$dbPath" to extractedFiles.find { it.name == "inventory_table.db" },
            walPath to extractedFiles.find { it.name.endsWith("-wal") },
            shmPath to extractedFiles.find { it.name.endsWith("-shm") }
        )

        // Replace the old database files with the new ones
        fileMap.forEach { (oldPath, newFile) ->
            newFile?.let {
                File(oldPath).delete() // Remove old file if it exists
                deleteWalAndShmFiles(requireContext(),"inventory_table.db")
                it.renameTo(File(oldPath)) // Rename new file to old file's path
            }
        }

        reinitializeRoom(requireContext(),dbPath)
        Log.i("ZipDB","replace database done")
    }
    fun reinitializeRoom(context: Context, newDatabaseFilePath: String) {
        context.deleteDatabase("inventory_table.db") // Delete the existing database

        val newDatabaseFile = File(newDatabaseFilePath)

        val db = Room.databaseBuilder(
            context.applicationContext,
            DatabaseInventory::class.java,
            "inventory_table.db"
        )
            .createFromFile(newDatabaseFile)
            .build()

        // Optionally, access DAO or perform database operations
    }

    fun importDatabase(context: Context, zipFile: File) {
        val extractDir = File(context.filesDir, "inventory_table.db")
        extractDir.mkdirs()

        // Extract ZIP file
        extractZipFile(context, zipFile, extractDir)

        // Find the extracted database file
        val extractedDatabaseFile = File(extractDir, "inventory_table.db") // Replace with your actual database file name

        // Define the destination file path
        val destinationFile = context.getDatabasePath("inventory_table.db") // Use the same name as your Room database

        // Copy database file to the Room database path
        copyDatabaseFile(context, extractedDatabaseFile, destinationFile)
        reinitializeRoom(requireContext(),destinationFile.absolutePath)
        // Optionally, delete the extracted files if no longer needed
        extractedDatabaseFile.delete()
        extractDir.deleteRecursively()

    }
    fun copyDatabaseFile(context: Context, sourceFile: File, destinationFile: File) {
        sourceFile.inputStream().use { inputStream ->
            destinationFile.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
    }
    fun extractZipFile(context: Context, zipFile: File, extractTo: File) {
        ZipInputStream(FileInputStream(zipFile)).use { zipInput ->
            var entry: ZipEntry? = zipInput.nextEntry
            while (entry != null) {
                val file = File(extractTo, entry.name)
                if (entry.isDirectory) {
                    file.mkdirs()
                } else {
                    file.parentFile?.mkdirs()
                    file.outputStream().use { output ->
                        zipInput.copyTo(output)
                    }
                }
                zipInput.closeEntry()
                entry = zipInput.nextEntry
            }
        }
    }
    fun deleteDatabaseAndFiles(context: Context, databaseName: String) {
        // Get the database path
        val dbPath = context.getDatabasePath(databaseName).absolutePath

        // Create File objects for the database, -wal, and -shm files
        val dbFile = File(dbPath)
        val walFile = File("$dbPath-wal")
        val shmFile = File("$dbPath-shm")

        // Delete the database file if it exists
        if (dbFile.exists()) {
            if (dbFile.delete()) {
                Log.i("ZipDB ", "Successfully deleted database file: ${dbFile.absolutePath}")
            } else {
                Log.e("ZipDB", "Failed to delete database file: ${dbFile.absolutePath}")
            }
        } else {
            Log.i("ZipDB ", "Database file does not exist: ${dbFile.absolutePath}")
        }

        // Delete the -wal file if it exists
        if (walFile.exists()) {
            if (walFile.delete()) {
                Log.i("ZipDB ", "Successfully deleted -wal file: ${walFile.absolutePath}")
            } else {
                Log.e("ZipDB", "Failed to delete -wal file: ${walFile.absolutePath}")
            }
        } else {
            Log.i("DeleteFiles", "-wal file does not exist: ${walFile.absolutePath}")
        }

        // Delete the -shm file if it exists
        if (shmFile.exists()) {
            if (shmFile.delete()) {
                Log.i("ZipDB", "Successfully deleted -shm file: ${shmFile.absolutePath}")
            } else {
                Log.e("ZipDB", "Failed to delete -shm file: ${shmFile.absolutePath}")
            }
        } else {
            Log.i("ZipDB", "-shm file does not exist: ${shmFile.absolutePath}")
        }
    }


    fun deleteWalAndShmFiles(context: Context, databaseName: String) {
        // Get the database path
        val dbPath = context.getDatabasePath(databaseName).absolutePath

        // Create File objects for the -wal and -shm files
        val walFile = File("$dbPath-wal")
        val shmFile = File("$dbPath-shm")

        // Delete the -wal file if it exists
        if (walFile.exists()) {
            if (walFile.delete()) {
                Log.i("DeleteFiles", "Successfully deleted -wal file: ${walFile.absolutePath}")
            } else {
                Log.e("DeleteFiles", "Failed to delete -wal file: ${walFile.absolutePath}")
            }
        } else {
            Log.i("DeleteFiles", "-wal file does not exist: ${walFile.absolutePath}")
        }

        // Delete the -shm file if it exists
        if (shmFile.exists()) {
            if (shmFile.delete()) {
                Log.i("DeleteFiles", "Successfully deleted -shm file: ${shmFile.absolutePath}")
            } else {
                Log.e("DeleteFiles", "Failed to delete -shm file: ${shmFile.absolutePath}")
            }
        } else {
            Log.i("DeleteFiles", "-shm file does not exist: ${shmFile.absolutePath}")
        }
    }
    private fun extractZipFile(context: Context, zipFile: File): List<File> {
        Log.i("ZipDB","extracted file started")
        val extractedFiles = mutableListOf<File>()
        try {
            ZipInputStream(FileInputStream(zipFile)).use { zipInputStream ->
                var zipEntry: ZipEntry?
                while (zipInputStream.nextEntry.also { zipEntry = it } != null) {
                    val extractedFile = File(context.getDatabasePath("inventory_table.db").parentFile, zipEntry!!.name)
                    Log.i("ZipDB","extractedFile name: ${extractedFile.name}")
                    extractedFiles.add(extractedFile)

                    // Create directories if necessary
                    extractedFile.parentFile?.mkdirs()


                    // Write the zip entry to the file
                    FileOutputStream(extractedFile).use { fos ->
                        zipInputStream.copyTo(fos)
                    }
                    zipInputStream.closeEntry()
                }
            }
        } catch (e: IOException) {
            Log.e("ZipDB", "Error extracting ZIP file: ${e.localizedMessage}", e)
        }
        Log.i("ZipDB","extractedFiles size: ${extractedFiles.size}")
        return extractedFiles
    }


    fun zipDatabaseFiles(context: Context, databaseName: String): File {
        val dbPath = context.getDatabasePath(databaseName).absolutePath
        Log.i("ZipDB","zipFile ${dbPath}.db")
        val walPath = "$dbPath-wal"
        Log.i("ZipDB","walPath ${walPath}")
        val shmPath = "$dbPath-shm"
        Log.i("ZipDB","shmPath ${shmPath}")
        val zipFile = File(context.externalCacheDir, "database_backup.zip")
        Log.i("ZipDB","zipFile ${zipFile.absolutePath}")
        Log.i("ZipDB","zipFile ${zipFile.name}")
        ZipOutputStream(FileOutputStream(zipFile)).use { zipOut ->
            addFileToZip(zipOut, File(dbPath), "")
            addFileToZip(zipOut, File(walPath), "")
            addFileToZip(zipOut, File(shmPath), "")
        }

        Log.i("ZipDB","zipFile ${zipFile.absolutePath}")
        Log.i("ZipDB","zipFile ${zipFile.name}")
        return zipFile
    }

    private fun addFileToZip(zipOut: ZipOutputStream, file: File, parentDir: String) {
        FileInputStream(file).use { fis ->
            val zipEntry = ZipEntry("$parentDir${file.name}")
            zipOut.putNextEntry(zipEntry)
            fis.copyTo(zipOut)
            zipOut.closeEntry()
        }
    }
    fun shareDatabaseBackup(context: Context) {
        Log.i("ZipDB","shareDatabaseBackup called")
        val zipFile = zipDatabaseFiles(context, "inventory_table.db")
        Log.i("ZipDB","zipFile ${zipFile.name}")
try {
    val fileUri: Uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        zipFile
    )
    Log.i("ZipDB", "fileuri ${fileUri}")
    val shareIntent: Intent = Intent(Intent.ACTION_SEND).apply {
        putExtra(Intent.EXTRA_STREAM, fileUri)
        type = "application/zip"  // Change to "application/zip" for ZIP files
    }
    Log.i("ZipDB", "shareintent ${shareIntent.dataString}")
    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)


    context.startActivity(Intent.createChooser(shareIntent, "Share database file"))
}        catch (e: Exception) {
            Log.e("ZipDB", "Error sharing database file: ${e.localizedMessage}", e)
        }
    }


    private fun exportDatabase(fileName: String) {
        val databaseFile = context?.getDatabasePath("inventory_table.db") ?: return
        val destinationFile = File(context?.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "$fileName.db")
        // Copy the database file to the destination
        try {
            databaseFile.inputStream().use { input ->
                destinationFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        } catch (e: IOException) {
            Log.e("FileCopyError", "Error copying database file", e)
            return
        }
        // Share the database file
        val fileUri: Uri = FileProvider.getUriForFile(
            this.requireContext(),
            "${requireContext().applicationContext.packageName}.provider",
            destinationFile
        )

        val shareIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, fileUri)
            type = "application/octet-stream"
        }
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        try {
            startActivity(Intent.createChooser(shareIntent, "Share database file"))
        } catch (e: Exception) {
            Log.e("ShareError", "Error sharing database file", e)
        }
    }

    fun getDatabaseFile(context: Context): File {
        val databasePath = context.getDatabasePath("inventory_table.db").absolutePath
        return File(databasePath)
    }



}