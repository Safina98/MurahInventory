package com.example.tokomurahinventory.fragments

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.example.tokomurahinventory.R
import com.example.tokomurahinventory.database.DatabaseInventory
import com.example.tokomurahinventory.databinding.FragmentExportImportBinding
import com.example.tokomurahinventory.databinding.PopUpAutocompleteTextviewBinding
import com.example.tokomurahinventory.utils.SharedPreferencesHelper
import com.example.tokomurahinventory.utils.UserRoles
import com.example.tokomurahinventory.viewmodels.ExportImportViewModel
import com.example.tokomurahinventory.viewmodels.ExportImportViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.zip.*


class ExportImportFragment : AuthFragment() {
    private lateinit var binding: FragmentExportImportBinding
    private lateinit var viewModel: ExportImportViewModel
    private val PERMISSION_REQUEST_CODE = 200
    private  val TAG = "ZipDB"
    private var dialog: AlertDialog? = null


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
        binding.lifecycleOwner = viewLifecycleOwner
        viewModel = ViewModelProvider(this,viewModelFactory)
            .get(ExportImportViewModel::class.java)
        if (checkPermission()) {
            //Toast.makeText(context, "Permission Granted", Toast.LENGTH_SHORT).show();
        } else {
            requestPermission()
        }
        binding.viewModel = viewModel
        viewModel.allMerkFromDb.observe(viewLifecycleOwner) { }

        viewModel.isLoading.observe(viewLifecycleOwner) {
            if (it==true){
                loading()
            }else{
                loaded()
            }
        }
        binding.btnExportMerkpdf.setOnClickListener{
            showPopUpDialog()
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
        binding.btnImportMerk.setOnClickListener {
            importCSVStock()
           //viewModel.generateData()

        }
        binding.btnExportDatabase.setOnClickListener {
            loading()
            shareDatabaseBackup(requireContext())
        }
        binding.btnImportMerkNew.setOnClickListener {
            closeDatabase(database)
            importZipFile()
            reopenDatabase()
        }
        return binding.root
    }
    fun closeDatabase(database: DatabaseInventory) {
        database.close()
    }
    fun reopenDatabase(): DatabaseInventory {
        return Room.databaseBuilder(
            requireContext().applicationContext,
            DatabaseInventory::class.java,
            "inventory_table.db"
        ).build()
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
                Log.i("csvWriteComplete","csvWriteComplete: " +it)
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
                viewModel.setIsCsvCompleteToNull()
            }
        })
    }
    private fun importZipFile() {
        loading()
        Log.i("ZipDB", "importZipFile started")
        val fileIntent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "application/zip"
        }
        try {
            resultLauncherNew.launch(fileIntent.type)
        } catch (e: Exception) {
            loaded()
            Toast.makeText(context, "Error selecting file", Toast.LENGTH_SHORT).show()
        }
    }

    private val resultLauncherNew = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            Log.i("ZipDB", "result Launcher new")
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    val tempFile = readFileFromUri(requireContext(), it)
                    Log.i("ZipDB", "file ${tempFile?.name}")
                    Log.i("ZipDB", "file path ${tempFile?.absolutePath}")
                    if (tempFile?.exists() == true) {
                        try {
                            extractZipFile(tempFile)
                        } catch (e: IOException) {
                            Log.e("ZipDB", "Error extracting zip file", e)
                            withContext(Dispatchers.Main) {
                                loaded()
                                Toast.makeText(requireContext(), "Error extracting file", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            loaded()
                            Toast.makeText(requireContext(), "File does not exist", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                // Ensure progress bar is hidden after processing
            }
        }
    }


    private fun extractZipFile(zipFile: File) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
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
                withContext(Dispatchers.Main) {
                    loaded()
                    Toast.makeText(requireContext(), "Files extracted successfully", Toast.LENGTH_SHORT).show()
                }
            } catch (e: IOException) {
                Log.e("ZipDB", "Error extracting zip file", e)
                withContext(Dispatchers.Main) {
                    loaded()
                    Toast.makeText(requireContext(), "Error extracting file", Toast.LENGTH_SHORT).show()
                }
            }
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
       // viewModel.writingDone()
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
    private fun shareDatabaseBackup(context: Context) {
        //viewModel.writingInProgress()
        CoroutineScope(Dispatchers.Main).launch {
            try {
                // Perform background work
                val zipFile = withContext(Dispatchers.IO) {
                    zipDatabaseFiles(context, "inventory_table.db")
                }
                // Update UI with the result
                val fileUri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", zipFile)
                val shareIntent: Intent = Intent(Intent.ACTION_SEND).apply {
                    putExtra(Intent.EXTRA_STREAM, fileUri)
                    type = "application/zip"
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(shareIntent, "Share database file"))

            } catch (e: Exception) {
                Log.e("ZipDB", "Error sharing database file: ${e.localizedMessage}", e)
            } finally {
                // Ensure UI is updated whether success or failure
                loaded()
            }
        }
        // Show loading UI
        loading()
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

    fun showPopUpDialog() {
        // Dismiss any existing dialog
        dialog?.dismiss()
        // Determine title and suggestions based on the code
        val title ="Merk"
        val suggestions =  viewModel.allMerkFromDb.value?.toTypedArray() ?: emptyArray()
        // Inflate the layout using DataBindingUtil
        val binding = DataBindingUtil.inflate<PopUpAutocompleteTextviewBinding>(
            LayoutInflater.from(context),
            R.layout.pop_up_autocomplete_textview,
            null,
            false
        )
            // Access AutoCompleteTextView within TextInputLayout
            val autoCompleteTextView: AutoCompleteTextView = binding.autocompleteText
            autoCompleteTextView.apply {
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, suggestions)
                setAdapter(adapter)

            }
        dialog = AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setView(binding.root)
            .setPositiveButton("Pilih") { dialog, _ ->
               // viewModel.updateMerk(position, input)
                val input = autoCompleteTextView.text.toString().trim()
                exportPDFBook(input)
                dialog.dismiss()
            }
            .setNegativeButton("Batal") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .apply { show() }
    }

    //create pdf file
    private fun exportPDFBook(merk:String) {
        val fileName = merk
        val file = File(context?.getExternalFilesDir(null), "Stok "+fileName+".pdf")

        Log.i("filepath",""+file.path.toString())
        viewModel.generatePDF(file,merk)
        val photoURI:Uri = FileProvider.getUriForFile(this.requireContext(), requireContext().applicationContext.packageName + ".provider",file)
        val shareIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, photoURI)
            type = "application/pdf"
        }
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        try {
            startActivity(Intent.createChooser(shareIntent, "book"))
        }catch (e : java.lang.Exception){
            Log.i("error_msg",e.toString())
        }
    }


    fun loading(){
        binding.progressBar.visibility = View.VISIBLE
        binding.labelProgres.visibility = View.VISIBLE
        binding.btnExportLog.visibility = View.GONE
        binding.btnExportUsers.visibility = View.GONE
        binding.btnExportMerk.visibility = View.GONE
        binding.btnImportMerk.visibility = View.GONE
        binding.exportHeader.visibility = View.GONE
        binding.importHeader.visibility = View.GONE
        binding.btnImportMerkNew.visibility = View.GONE
        binding.btnExportDatabase.visibility = View.GONE
        binding.btnExportMerkpdf.visibility =View.GONE
        binding.exportStokPerMerk.visibility = View.GONE
    }
    fun loaded(){
        val userRole = SharedPreferencesHelper.getUserRole(requireContext())
        binding.progressBar.visibility = View.GONE
        binding.labelProgres.visibility = View.GONE
        binding.btnExportLog.visibility = View.VISIBLE
        binding.btnExportUsers.visibility = View.VISIBLE
        binding.btnExportMerk   .visibility = View.VISIBLE
        binding.exportHeader.visibility = View.VISIBLE
        binding.btnExportDatabase.visibility = View.VISIBLE
        binding.btnExportMerkpdf.visibility =View.VISIBLE
        binding.exportStokPerMerk.visibility = View.VISIBLE
       if (userRole==UserRoles.ADMIN) {
           binding.importHeader.visibility = View.VISIBLE
           binding.btnImportMerkNew.visibility = View.VISIBLE
           binding.btnImportMerk.visibility = View.VISIBLE
       }

    }




}