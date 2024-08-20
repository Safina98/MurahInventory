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
import android.widget.DatePicker
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
import java.util.Calendar


class AllTransactionFragment : Fragment() {
    private lateinit var binding: FragmentAllTransactionBinding
    private lateinit var viewModel: AllTransViewModel
    private var isDialogShowing = false
    private var isDateDialogShowing = false

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
        binding.viewModel=viewModel
        val adapter  = LogAdapter(
            LogClickListener {
            },
            LogLongListener {
            }, LogDeleteListener {
            },true
        )
        binding.rvLog.adapter = adapter

        viewModel.codeWarnaByMerk.observe(viewLifecycleOwner) { it?.let {}}
        viewModel.allMerkFromDb.observe(viewLifecycleOwner){it?.let {}}
        viewModel.isiByWarnaAndMerk.observe(viewLifecycleOwner){it?.let {}}

        viewModel.filteredLog.observe(viewLifecycleOwner){it?.let {
            //Log.i("AllTransProbs","$it")
            adapter.submitList(it)
            adapter.notifyDataSetChanged()
            Log.i("AllTransProbs", "Data size: ${it.size}")
        }}

        binding.btnFilter.setOnClickListener {
            clearSearchQuery()
            setupDialog(null,1)
        }
        binding.filterIg.setOnClickListener {
            clearSearchQuery()
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
        binding.viewModel=viewModel
        val autoCompleteMerk = dialogBinding.txtMerk
        val autoCompleteWarna = dialogBinding.txtWarna
        val autoCompleteIsi = dialogBinding.txtIsi
        val tipeSpinner = dialogBinding.spinnerTipe
        val textDate=dialogBinding.txtDate
        val calendarIcon=dialogBinding.calenderIcon
        val txtReset=dialogBinding.txtReset

        viewModel._dateRangeString.observe(viewLifecycleOwner, Observer {
            it?.let {
                textDate.setText(it)
            }
        })
        if (viewModel.mutableMerk.value!=null &&viewModel.mutableMerk.value!="") {
            autoCompleteMerk.setText(viewModel.mutableMerk.value)
        }
        if (viewModel.mutableKode.value!=null &&viewModel.mutableKode.value!="") {
            autoCompleteWarna.setText(viewModel.mutableKode.value)
        }
        if (viewModel.mutableIsi.value!=null &&viewModel.mutableIsi.value!=""&&viewModel.mutableIsi.value!="-"&&viewModel.mutableIsi.value!="Semua") {
            autoCompleteIsi.setText(viewModel.mutableIsi.value)
        }
        val tipeArray = resources.getStringArray(R.array.masuk_keluar_spinner)
        viewModel.mutableTipe.value?.takeIf { it.isNotEmpty() }?.let { selectedValue ->
            val position = tipeArray.indexOf(selectedValue)
            if (position != -1) tipeSpinner.setSelection(position)
        } ?: Log.e("SpinnerError", "Selected value is null or empty.")

        textDate.setOnClickListener { showDatePickerDialog() }
        calendarIcon.setOnClickListener{ showDatePickerDialog() }
        txtReset.setOnClickListener {
            viewModel.updateDateRangeString(null,null)
            viewModel.setStartAndEndDateRange(null,null)
        }

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
                val selectedItem = tipeSpinner.selectedItem.toString()
                //viewModel. updateRv(namaMerk,kodeWarna,isi,selectedItem)
                viewModel.checkIfDataExist(namaMerk,kodeWarna,isi) { status ->
                    when (status) {
                        UpdateStatus.SUCCESS -> {
                            viewModel.updateRv(namaMerk,kodeWarna,isi,selectedItem)
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
            }
        }

        dialog.show()
    }
    private fun showDatePickerDialog() {
        if (isDateDialogShowing) return
        isDateDialogShowing = true
        //clearSearchQuery()
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.pop_up_date_picker, null)
        val datePickerStart = dialogView.findViewById<DatePicker>(R.id.datePickerStart)
        val datePickerEnd = dialogView.findViewById<DatePicker>(R.id.datePickerEnd)
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Select Date Range")
            .setView(dialogView)
            .setPositiveButton("OK") { _, _ ->
                val startYear = datePickerStart.year
                val startMonth = datePickerStart.month
                val startDay = datePickerStart.dayOfMonth
                val endYear = datePickerEnd.year
                val endMonth = datePickerEnd.month
                val endDay = datePickerEnd.dayOfMonth

                val startDate = Calendar.getInstance().apply {
                    set(startYear, startMonth, startDay, 0, 0, 1) // Set time to start of the day
                    set(Calendar.MILLISECOND, 0)
                }.time

                val endDate = Calendar.getInstance().apply {
                    set(endYear, endMonth, endDay, 23, 59, 58) // Set time to end of the day
                    set(Calendar.MILLISECOND, 999)
                }.time

                viewModel.updateDateRangeString(startDate,endDate)
                viewModel.setStartAndEndDateRange(startDate,endDate)
                //viewModel.updateRv4()
                // viewModel.setEndDateRange(endDate)
            }
            .setNegativeButton("Cancel", null)
            .create()
        dialog.setOnDismissListener {
            isDateDialogShowing = false
        }
        dialog.show()
    }
    fun clearSearchQuery() {
        binding.searchBarLog.setQuery("", false)
        binding.searchBarLog.clearFocus()
    }

    override fun onStart() {
        super.onStart()
        clearSearchQuery()
    }

}