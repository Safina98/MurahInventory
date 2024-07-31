package com.example.tokomurahinventory.fragments

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.fragment.app.Fragment
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
import androidx.lifecycle.lifecycleScope
import com.example.tokomurahinventory.R
import com.example.tokomurahinventory.database.DatabaseInventory
import com.example.tokomurahinventory.databinding.FragmentExportImportBinding
import com.example.tokomurahinventory.databinding.FragmentLogBinding
import com.example.tokomurahinventory.utils.SharedPreferencesHelper
import com.example.tokomurahinventory.viewmodels.ExportImportViewModel
import com.example.tokomurahinventory.viewmodels.ExportImportViewModelFactory
import com.example.tokomurahinventory.viewmodels.LogViewModel
import com.example.tokomurahinventory.viewmodels.LogViewModelFactory
import com.example.tokomurahinventory.viewmodels.MerkViewModel
import com.example.tokomurahinventory.viewmodels.MerkViewModelFactory
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.lang.Exception
import java.sql.SQLException


class ExportImportFragment : AuthFragment() {
    private lateinit var binding: FragmentExportImportBinding
    private lateinit var viewModel: ExportImportViewModel
    private val PERMISSION_REQUEST_CODE = 200

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
        binding.btnImportMerk.setOnClickListener {
            importCSVStock()
            //viewModel.generateData()
        }


        return binding.root
    }
    private fun importCSVStock() {
        var fileIntent = Intent(Intent.ACTION_GET_CONTENT)
        fileIntent.type = "text/*"
        try { resultLauncher.launch(fileIntent) }
        catch (e: FileNotFoundException) {
            Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show() }
    }
    private fun importDatabase() {
        val fileIntent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "application/octet-stream" // Use appropriate MIME type for your database file
        }
        try {
            resultLauncherDB.launch(fileIntent.type)
        } catch (e: Exception) {
            Toast.makeText(context, "Error selecting file: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private val resultLauncherDB = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            //importDatabaseFromUri(it)
        }
    }


    private fun importDatabaseFromUriOld(uri: Uri) {
        val contentResolver = requireContext().contentResolver
        val inputStream: InputStream? = contentResolver.openInputStream(uri)
        val databaseFile = File(context?.getDatabasePath("inventory_table.db")!!.absolutePath)
        val tempFile = File.createTempFile("temp_db", ".db", requireContext().cacheDir)

        try {
            inputStream?.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            Log.i("DatabaseImport", "Temporary file created at ${tempFile.absolutePath}")

            // Replace the old database file with the imported one
            if (databaseFile.exists()) {
                databaseFile.delete() // Delete old database file if it exists
                Log.i("DatabaseImport", "Old database file deleted")
            }
            tempFile.copyTo(databaseFile) // Copy new file to the correct location
            tempFile.delete() // Delete temporary file
            Log.i("DatabaseImport", "Database file replaced at ${databaseFile.absolutePath}")

            Toast.makeText(context, "Database imported successfully", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            Log.e("DatabaseImportError", "Error importing database: ${e.message}")
            Toast.makeText(context, "Error importing database: ${e.message}", Toast.LENGTH_SHORT).show()
        }
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
    private fun exportDatabase(fileName: String) {
        val databaseFile = context?.getDatabasePath("inventory_table") ?: return
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
        val databasePath = context.getDatabasePath("inventory_table").absolutePath
        return File(databasePath)
    }



}