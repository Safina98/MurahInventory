package com.example.tokomurahinventory.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.tokomurahinventory1.R
import com.example.tokomurahinventory.adapters.DeleteInputStokLogClickListener
import com.example.tokomurahinventory.adapters.InputStokLogAdapter
import com.example.tokomurahinventory.adapters.InputStokLogClickListener
import com.example.tokomurahinventory.adapters.InputStokLogLongListener
import com.example.tokomurahinventory.adapters.UpdateInputStokLogClickListener
import com.example.tokomurahinventory.database.DatabaseInventory
import com.example.tokomurahinventory1.databinding.FragmentInputStokBinding
import com.example.tokomurahinventory1.databinding.PopUpAddBarangLogBinding
import com.example.tokomurahinventory.models.model.InputStokLogModel
import com.example.tokomurahinventory.utils.DialogUtils
import com.example.tokomurahinventory.utils.SharedPreferencesHelper
import com.example.tokomurahinventory.utils.UpdateStatus
import com.example.tokomurahinventory.utils.dataNotFoundMsgD
import com.example.tokomurahinventory.utils.incorrectInputMsg
import com.example.tokomurahinventory.utils.stokTidakCukup
import com.example.tokomurahinventory.utils.succsessMsg
import com.example.tokomurahinventory.viewmodels.InputStokViewModel
import com.example.tokomurahinventory.viewmodels.InputStokViewModelFactory
import java.util.Calendar


class InputStokFragment : AuthFragment() {

    private lateinit var binding: FragmentInputStokBinding
    private val viewModel: InputStokViewModel by viewModels()
    private var isDialogShowing = false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_input_stok,container,false)
        val application = requireNotNull(this.activity).application

        val dataSourceBarangLog = DatabaseInventory.getInstance(application).barangLogDao
        val dataSourceDetailWarna = DatabaseInventory.getInstance(application).detailWarnaDao
        val dataSourceMerk = DatabaseInventory.getInstance(application).merkDao
        val dataSourceLog = DatabaseInventory.getInstance(application).logDao
        val dataSourceWarna = DatabaseInventory.getInstance(application).warnaDao
        val loggedInUser = SharedPreferencesHelper.getLoggedInUser(requireContext()) ?: ""
        val viewModelFactory = InputStokViewModelFactory(dataSourceBarangLog,dataSourceDetailWarna,dataSourceMerk,dataSourceWarna,loggedInUser,dataSourceLog,application)
        binding.lifecycleOwner = viewLifecycleOwner

        val viewModel = ViewModelProvider(this,viewModelFactory)
            .get(InputStokViewModel::class.java)
        binding.viewModel = viewModel
        val adapter  = InputStokLogAdapter(
            InputStokLogClickListener {
            }, InputStokLogLongListener{

            }, UpdateInputStokLogClickListener{
                clearSearchQuery()
                 setupDialog(it)
                //viewModel.updateInputStok(it)
            }, DeleteInputStokLogClickListener {
                clearSearchQuery()
                DialogUtils.showDeleteDialog(this, viewModel, it, { vm, item -> (vm as InputStokViewModel).deleteInputStok(item as InputStokLogModel) })
            }
        )
        binding.rvInputStokLog.adapter=adapter
        viewModel.isInputLogLoading.observe(viewLifecycleOwner) { isLoading ->
            Log.i("LoadLogProbs", "isLoading observer : $isLoading")
            if (isLoading) {
                binding.progressBarInputLog.visibility = View.VISIBLE
                binding.rvInputStokLog.visibility = View.GONE
                binding.textCrashed.visibility = View.GONE
            } else {
                // This should only hide the ProgressBar if not loading
                binding.progressBarInputLog.visibility = View.GONE
                // Check if the data loading was successful or not
                if (viewModel.isLoadCrashed.value == true) {
                    binding.rvInputStokLog.visibility = View.GONE
                    binding.textCrashed.visibility = View.VISIBLE
                } else {
                    binding.rvInputStokLog.visibility = View.VISIBLE
                    binding.textCrashed.visibility = View.GONE
                }
            }
        }

        viewModel.isLoadCrashed.observe(viewLifecycleOwner) { hasCrashed ->
            if (hasCrashed) {
                // Only show crash message if there was an actual crash
                binding.textCrashed.visibility = View.VISIBLE
                binding.rvInputStokLog.visibility = View.GONE
            } else {
                // Hide the crash message if there's no crash
                binding.textCrashed.visibility = View.GONE
                // RecyclerView visibility will be handled by the isLogLoading observer
            }
        }
        binding.textCrashed.setOnClickListener{
            viewModel.updateRv4()
        }
        viewModel.inputLogModel.observe(viewLifecycleOwner, Observer {it?.let{
            adapter.submitList(it.sortedByDescending { it.barangLogInsertedDate })
            adapter.notifyDataSetChanged()
        }
            Log.i("DataSize","data size: ${it.size}")
        })
        binding.searchBarLog.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.filterLog(newText)
                return true
            }
        })
        viewModel.isStartDatePickerClicked.observe(viewLifecycleOwner) {
            if (it==true){
                //clearSearchQuery()
                showDatePickerDialog()
                viewModel.onStartDatePickerClicked()
            }
        }
        viewModel.selectedStartDate.observe(viewLifecycleOwner) {
            //viewModel.updateRv4()
        }
        viewModel.selectedEndDate.observe(this.viewLifecycleOwner) {it?.let{
            Log.i("InputStokLogProbs", "EndDate observer $it")
            //viewModel.updateRv4()
            }
        }

        return binding.root
    }


    private fun setupDialog(inputStokLogModel: InputStokLogModel?) {
        if (isDialogShowing) return

        isDialogShowing = true
        val dialogBinding = DataBindingUtil.inflate<PopUpAddBarangLogBinding>(
            LayoutInflater.from(context),
            R.layout.pop_up_add_barang_log,
            null,
            false
        )
        val oldCountModel = inputStokLogModel?.copy()
        val autoCompleteMerk = dialogBinding.txtMerk
        val autoCompleteWarna = dialogBinding.txtWarna
        val autoCompleteIsi = dialogBinding.txtIsi
        val etPcs = dialogBinding.txtPcs

        // Initialize the adapter for the AutoCompleteTextView
        val merkAdapter = ArrayAdapter<String>(requireContext(), android.R.layout.simple_dropdown_item_1line, emptyList())
        autoCompleteMerk.setAdapter(merkAdapter)

        val warnaAdapter = ArrayAdapter<String>(requireContext(), android.R.layout.simple_dropdown_item_1line, emptyList())
        autoCompleteWarna.setAdapter(warnaAdapter)

        if (inputStokLogModel!=null){
            autoCompleteMerk.setText(inputStokLogModel.namaMerk)
            autoCompleteWarna.setText(inputStokLogModel.kodeWarna)
            Log.i("InsertLogTry", "old log barang warna ${inputStokLogModel.kodeWarna}")
            autoCompleteIsi.setText(inputStokLogModel.isi.toString())
            etPcs.setText(inputStokLogModel.pcs.toString())
        }
        viewModel.getWarnaByMerkNew(inputStokLogModel!!.namaMerk)
        // Fetch and observe data
        viewModel.allMerkFromDb.observe(viewLifecycleOwner) { allMerk ->
            merkAdapter.clear()
            merkAdapter.addAll(allMerk)
        }
        autoCompleteMerk.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
            override fun afterTextChanged(s: Editable?) {
                val merk = s.toString()
                if (merk.isNotEmpty()) {
                    viewModel.getWarnaByMerk(merk)
                }
            }
        })
        viewModel.codeWarnaByMerk.observe(viewLifecycleOwner) { warnaList ->
            warnaAdapter.clear()
            warnaAdapter.addAll(warnaList ?: emptyList())
        }
        autoCompleteWarna.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val warna = s.toString()
                val merk = autoCompleteMerk.text.toString()
                if (warna.isNotEmpty() && merk.isNotEmpty()) {
                    viewModel.getIsiByWarnaAndMerk(merk, warna)
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        viewModel.isiByWarnaAndMerk.observe(viewLifecycleOwner) { isiList ->
            // Update the UI or another adapter if needed
        }

        // Show dialog
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .setOnDismissListener {
                isDialogShowing = false
            }
            .create()

        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK") { _, _ ->
            // This click listener won't be triggered automatically; we handle it manually below
        }

        dialog.show()

// Manually set the positive button click listener
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            if (inputStokLogModel != null) {
                val namaMerk = autoCompleteMerk.text.toString().trim()
                val kodeWarna = autoCompleteWarna.text.toString().trim()
                val isi = autoCompleteIsi.text.toString().trim().toDoubleOrNull()
                val pcs = etPcs.text.toString().trim().toIntOrNull()

                if (namaMerk.isNotEmpty() && kodeWarna.isNotEmpty() && isi != null && pcs != null) {
                    viewModel.checkIfDataExist(namaMerk, kodeWarna, isi, pcs,inputStokLogModel,oldCountModel!!) { status ->
                        when (status) {
                            UpdateStatus.SUCCESS -> {
                                Toast.makeText(requireContext(), succsessMsg, Toast.LENGTH_SHORT).show()
                                inputStokLogModel.namaMerk = namaMerk
                                inputStokLogModel.kodeWarna = kodeWarna
                                inputStokLogModel.isi = isi
                                inputStokLogModel.pcs = pcs
                                viewModel.updateInputStok(inputStokLogModel)
                                dialog.dismiss() // Dismiss the dialog after updating
                            }
                            UpdateStatus.MERK_NOT_PRESENT -> {
                                Toast.makeText(requireContext(), "Merk $dataNotFoundMsgD", Toast.LENGTH_SHORT).show()
                            }
                            UpdateStatus.WARNA_NOT_PRESENT -> {
                                Toast.makeText(requireContext(), "Warna $dataNotFoundMsgD", Toast.LENGTH_SHORT).show()
                            }
                            UpdateStatus.ISI_NOT_PRESENT -> {
                                Toast.makeText(requireContext(), "Isi $dataNotFoundMsgD", Toast.LENGTH_SHORT).show()
                            }
                            UpdateStatus.PCS_NOT_READY_IN_STOCK -> {
                                Toast.makeText(requireContext(), stokTidakCukup, Toast.LENGTH_SHORT).show()
                            }
                            else -> {
                                Toast.makeText(requireContext(), incorrectInputMsg, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else {
                    Toast.makeText(context, "Gagal mengubah data", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }

    private fun showDatePickerDialog() {
        if (isDialogShowing) return
        isDialogShowing = true
        clearSearchQuery()
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
                viewModel.setStartDateRange(startDate,endDate)
                viewModel.updateRv4()
                //viewModel.setEndDateRange(endDate)
            }
            .setNegativeButton("Cancel", null)
            .create()
        dialog.setOnDismissListener {
            isDialogShowing = false
        }

        dialog.show()
    }
    override fun onResume() {
        super.onResume()
        Log.i("InputStokLogProbs", "onResume called")
        clearSearchQuery()
        viewModel.updateRv4()
        viewModel.setInitialStartDateAndEndDate()
    }
    fun clearSearchQuery() {
        binding.searchBarLog.setQuery("", false)
        binding.searchBarLog.clearFocus()
    }
    override fun onPause() {
        super.onPause()
        Log.i("InputStokLogProbs", "onPause called")
       // viewModel.clearUiScope()
    }
}