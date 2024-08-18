package com.example.tokomurahinventory.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.tokomurahinventory.R
import com.example.tokomurahinventory.adapters.LogAdapter
import com.example.tokomurahinventory.adapters.LogClickListener
import com.example.tokomurahinventory.adapters.LogDeleteListener
import com.example.tokomurahinventory.adapters.LogLongListener
import com.example.tokomurahinventory.database.DatabaseInventory
import com.example.tokomurahinventory.databinding.FragmentAllTransactionBinding
import com.example.tokomurahinventory.databinding.FragmentExportImportBinding
import com.example.tokomurahinventory.databinding.PopUpAddBarangLogBinding
import com.example.tokomurahinventory.databinding.PopUpFilterBinding
import com.example.tokomurahinventory.models.CountModel
import com.example.tokomurahinventory.models.LogTable
import com.example.tokomurahinventory.utils.DialogUtils
import com.example.tokomurahinventory.utils.SharedPreferencesHelper
import com.example.tokomurahinventory.utils.UpdateStatus
import com.example.tokomurahinventory.utils.dataNotFoundMsgD
import com.example.tokomurahinventory.utils.incorrectInputMsg
import com.example.tokomurahinventory.utils.stokTidakCukup
import com.example.tokomurahinventory.utils.succsessMsg
import com.example.tokomurahinventory.viewmodels.AllTransViewModel
import com.example.tokomurahinventory.viewmodels.AllTransViewModelFactory
import com.example.tokomurahinventory.viewmodels.ExportImportViewModel
import com.example.tokomurahinventory.viewmodels.ExportImportViewModelFactory
import com.example.tokomurahinventory.viewmodels.LogViewModel


class AllTransactionFragment : Fragment() {
    private lateinit var binding: FragmentAllTransactionBinding
    private lateinit var viewModel: AllTransViewModel
    private var isDialogShowing = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding=DataBindingUtil.inflate(inflater,R.layout.fragment_all_transaction,container,false)
        val application = requireNotNull(this.activity).application

        val dataSourceLog = DatabaseInventory.getInstance(application).logDao
        val dataSourcebarangLog = DatabaseInventory.getInstance(application).barangLogDao
        val dataSourceMerk =  DatabaseInventory.getInstance(application).merkDao
        val dataSourceWarna =  DatabaseInventory.getInstance(application).warnaDao
        val dataSourceDetailWarna =  DatabaseInventory.getInstance(application).detailWarnaDao

        val dataSourceUsers =  DatabaseInventory.getInstance(application).usersDao

        val loggedInUser = SharedPreferencesHelper.getLoggedInUser(requireContext()) ?: ""
        val viewModelFactory = AllTransViewModelFactory(dataSourceMerk,dataSourceWarna,dataSourceDetailWarna,dataSourceLog,dataSourcebarangLog,dataSourceUsers,loggedInUser,application)
        binding.lifecycleOwner = viewLifecycleOwner
        viewModel = ViewModelProvider(this,viewModelFactory)
            .get(AllTransViewModel::class.java)
        val adapter  = LogAdapter(
            LogClickListener {
            },
            LogLongListener {
            }, LogDeleteListener {
            },true
        )
        binding.rvLog.adapter = adapter

        viewModel.codeWarnaByMerk.observe(viewLifecycleOwner) { it?.let {}}
        viewModel.allMerkFromDb.observe(viewLifecycleOwner){it?.let {

        }}
        viewModel.isiByWarnaAndMerk.observe(viewLifecycleOwner){it?.let {}}

        viewModel.filteredLog.observe(viewLifecycleOwner){it?.let {
            Log.i("AllTransProbs","$it")
            adapter.submitList(it)
            Log.i("AllTransProbs","${it.size}")
        }}

        binding.btnFilter.setOnClickListener {
            setupDialog(null,1)
        }
        viewModel.isLogLoading.observe(viewLifecycleOwner) { isLoading ->
            Log.i("LoadLogProbs", "isLoading observer : $isLoading")
            if (isLoading) {
                binding.progressBarLog.visibility = View.VISIBLE
                binding.rvLog.visibility = View.GONE
                binding.btnFilter.visibility=View.GONE
                binding.textCrashed.visibility = View.GONE
            } else {
                // This should only hide the ProgressBar if not loading
                binding.progressBarLog.visibility = View.GONE
                binding.btnFilter.visibility=View.VISIBLE
                // Check if the data loading was successful or not
                if (viewModel.isLoadCrashed.value == true) {
                    binding.rvLog.visibility = View.GONE
                    binding.textCrashed.visibility = View.VISIBLE
                } else {
                    binding.rvLog.visibility = View.VISIBLE
                    binding.textCrashed.visibility = View.GONE
                }
            }
        }
        viewModel.isLoadCrashed.observe(viewLifecycleOwner) { hasCrashed ->
            if (hasCrashed) {
                // Only show crash message if there was an actual crash
                binding.textCrashed.visibility = View.VISIBLE
                binding.rvLog.visibility = View.GONE
            } else {
                // Hide the crash message if there's no crash
                binding.textCrashed.visibility = View.GONE
                // RecyclerView visibility will be handled by the isLogLoading observer
            }
        }
        binding.searchBarLog.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.filterLogQuery(newText)
                return true
            }
        })
        return binding.root
    }

    private fun setupDialog(inputStokLogModel: CountModel?, code: Int) {
        if (isDialogShowing) return
        isDialogShowing = true

        val dialogBinding = DataBindingUtil.inflate<PopUpFilterBinding>(
            LayoutInflater.from(context), R.layout.pop_up_filter, null, false)

        val autoCompleteMerk = dialogBinding.txtMerk
        val autoCompleteWarna = dialogBinding.txtWarna
        val autoCompleteIsi = dialogBinding.txtIsi
        //val etPcs = dialogBinding.txtPcs

        val oldCountModel = inputStokLogModel?.copy()
        // Initialize the adapter for the AutoCompleteTextView
        // Initialize the adapter for the AutoCompleteTextView with a mutable list
        val merkAdapter = ArrayAdapter<String>(requireContext(), android.R.layout.simple_dropdown_item_1line, mutableListOf())
        autoCompleteMerk.setAdapter(merkAdapter)

        val warnaAdapter = ArrayAdapter<String>(requireContext(), android.R.layout.simple_dropdown_item_1line, mutableListOf())
        autoCompleteWarna.setAdapter(warnaAdapter)

        val isiAdapter = ArrayAdapter<String>(requireContext(), android.R.layout.simple_dropdown_item_1line, mutableListOf())
        autoCompleteIsi.setAdapter(isiAdapter)


        if (inputStokLogModel != null) {
            autoCompleteMerk.setText(inputStokLogModel.merkBarang)
            autoCompleteWarna.setText(inputStokLogModel.kodeBarang)
            if (inputStokLogModel.isi != null) {
                autoCompleteIsi.setText(inputStokLogModel.isi.toString())
            }
            if (inputStokLogModel.psc != 0) {
                //etPcs.setText(inputStokLogModel.psc.toString())
            }
            if (inputStokLogModel.merkBarang != null) {
                viewModel.getWarnaByMerkNew(inputStokLogModel.merkBarang!!)
            }
            if (inputStokLogModel.kodeBarang != null) {
                viewModel.getIsiByWarnaAndMerk(inputStokLogModel.merkBarang!!, inputStokLogModel.kodeBarang!!)
            }
        }

        // Fetch and observe data
        viewModel.allMerkFromDb.observe(viewLifecycleOwner) { allMerk ->
            merkAdapter.clear()
            merkAdapter.addAll(allMerk.sortedBy { it })
        }

        autoCompleteMerk.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val merk = s.toString()
                if (merk.isNotEmpty()) {
                    viewModel.getWarnaByMerkNew(merk)
                }
            }
        })

        viewModel.codeWarnaByMerk.observe(viewLifecycleOwner) { warnaList ->
            warnaAdapter.clear()
            warnaAdapter.addAll(warnaList?.sortedBy { it } ?: emptyList())
        }

        autoCompleteWarna.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val warna = s.toString()
                val merk = autoCompleteMerk.text.toString()
                if (warna.isNotEmpty() && merk.isNotEmpty()) {
                    viewModel.getIsiByWarnaAndMerk(merk, warna)
                }
            }
        })

        viewModel.isiByWarnaAndMerk.observe(viewLifecycleOwner) { isiList ->
            // Update the UI or another adapter if needed
            isiAdapter.clear()
            isiAdapter.addAll(isiList?.sortedBy { it }?.map { it.toString() } ?: emptyList())
        }

        // Create and show the dialog
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .setPositiveButton("OK", null) // Set null for now, we will override the action
            .setNegativeButton("Cancel") { dialog, _ ->

                dialog.dismiss()
            }
            .setOnDismissListener {
                // Handle item removal only when dialog is dismissed

                isDialogShowing = false
            }
            .create()

        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                val namaMerk = autoCompleteMerk.text.toString().trim()
                val kodeWarna = autoCompleteWarna.text.toString().trim()
                val isi = autoCompleteIsi.text.toString().trim().toDoubleOrNull()
                viewModel. updateRv(namaMerk,kodeWarna,isi)
                dialog.dismiss()
                /*
                if (inputStokLogModel != null) {
                    val namaMerk = autoCompleteMerk.text.toString().trim()
                    val kodeWarna = autoCompleteWarna.text.toString().trim()
                    val isi = autoCompleteIsi.text.toString().trim().toDoubleOrNull()
                    //val pcs = etPcs.text.toString().trim().toIntOrNull()
                    if (namaMerk.isNotEmpty() && kodeWarna.isNotEmpty() && isi != null && pcs != null) {
                        inputStokLogModel.merkBarang = namaMerk
                        inputStokLogModel.kodeBarang = kodeWarna
                        inputStokLogModel.isi = isi
                        inputStokLogModel.psc = pcs

                        viewModel.updateCountModel(inputStokLogModel, oldCountModel!!) { status ->
                            when (status) {
                                UpdateStatus.SUCCESS -> {
                                    Toast.makeText(requireContext(), succsessMsg, Toast.LENGTH_SHORT).show()
                                    dialog.dismiss() // Dismiss the dialog after updating
                                }
                                UpdateStatus.MERK_NOT_PRESENT -> Toast.makeText(requireContext(), "Merk $dataNotFoundMsgD", Toast.LENGTH_SHORT).show()
                                UpdateStatus.WARNA_NOT_PRESENT -> Toast.makeText(requireContext(), "Warna $dataNotFoundMsgD", Toast.LENGTH_SHORT).show()
                                UpdateStatus.ISI_NOT_PRESENT -> Toast.makeText(requireContext(), "Isi $dataNotFoundMsgD", Toast.LENGTH_SHORT).show()
                                UpdateStatus.PCS_NOT_READY_IN_STOCK -> Toast.makeText(requireContext(), stokTidakCukup, Toast.LENGTH_SHORT).show()
                                else -> {
                                    Toast.makeText(requireContext(), incorrectInputMsg, Toast.LENGTH_SHORT).show()
                                    // Optionally, you can log the status here if needed
                                }
                            }
                        }
                    } else {
                        Toast.makeText(context, "Gagal mengubah data ", Toast.LENGTH_SHORT).show()
                    }
                }
                 */
            }
        }

        dialog.show()
    }

}