package com.example.tokomurahinventory.fragments

import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.tokomurahinventory.R
import com.example.tokomurahinventory.adapters.AddNetClickListener
import com.example.tokomurahinventory.adapters.BarangLogIsiClickListener
import com.example.tokomurahinventory.adapters.BarangLogKodeClickListener
import com.example.tokomurahinventory.adapters.BarangLogMerkClickListener
import com.example.tokomurahinventory.adapters.BarangLogPcsClickListener
import com.example.tokomurahinventory.adapters.CountAdapter
import com.example.tokomurahinventory.adapters.DeleteNetClickListener
import com.example.tokomurahinventory.database.DatabaseInventory
import com.example.tokomurahinventory.database.OnWarnaDataLoadedCallback
import com.example.tokomurahinventory.databinding.FragmentInputLogBinding
import com.example.tokomurahinventory.databinding.PopUpAddBarangLogBinding
import com.example.tokomurahinventory.databinding.PopUpAutocompleteTextviewBinding
import com.example.tokomurahinventory.models.CountModel
import com.example.tokomurahinventory.models.model.InputStokLogModel
import com.example.tokomurahinventory.utils.SharedPreferencesHelper
import com.example.tokomurahinventory.utils.UpdateStatus
import com.example.tokomurahinventory.viewmodels.LogViewModel
import com.example.tokomurahinventory.viewmodels.LogViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class InputLogFragment : AuthFragment() {

    private lateinit var binding: FragmentInputLogBinding
    private lateinit var viewModel: LogViewModel
    private var isDialogShowing = false
    private var dialog: AlertDialog? = null
    private lateinit var adapter: CountAdapter
    private lateinit var progressBar: ProgressBar


    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_input_log,container,false)
        progressBar = binding.progressBar
        val application = requireNotNull(this.activity).application
        val dataSourceLog = DatabaseInventory.getInstance(application).logDao
        val dataSourcebarangLog = DatabaseInventory.getInstance(application).barangLogDao
        val dataSourceMerk =  DatabaseInventory.getInstance(application).merkDao
        val dataSourceWarna =  DatabaseInventory.getInstance(application).warnaDao
        val dataSourceDetailWarna =  DatabaseInventory.getInstance(application).detailWarnaDao
        // val viewModelFactory = LogViewModelFactory(application)
        binding.lifecycleOwner = this
        val loggedInUser = SharedPreferencesHelper.getLoggedInUser(requireContext()) ?: ""
        viewModel = ViewModelProvider(
            requireActivity(),
            LogViewModelFactory(dataSourceMerk, dataSourceWarna, dataSourceDetailWarna, dataSourceLog, dataSourcebarangLog, loggedInUser,application)
        ).get(LogViewModel::class.java)

        binding.viewModel = viewModel
        viewModel.codeWarnaByMerk.observe(viewLifecycleOwner, Observer {  })

        adapter = CountAdapter(
            AddNetClickListener { countModel, position -> handleAddNetClick(countModel, position) },
            DeleteNetClickListener { countModel, position -> handleDeleteNetClick(countModel, position) },
            BarangLogMerkClickListener { countModel, position ->
                setupDialog(countModel)
               // handleBarangLogMerkClick(countModel, position)
                                       },
            BarangLogKodeClickListener { countModel, position -> setupDialog(countModel) },
            BarangLogIsiClickListener { countModel, position -> setupDialog(countModel) },
            BarangLogPcsClickListener { countModel, position -> setupDialog(countModel) },
            viewModel,
            this
        )


        binding.rvAddBarang.adapter = adapter

        binding.btnAddNewCountmodel.setOnClickListener {
            clearEditText()
            val countModel = viewModel.addNewCountItemBtn()
            setupDialog(countModel)
        }


        viewModel.countModelList.observe(viewLifecycleOwner) { it?.let {
            Log.i("InsertLogTry",it.toString())
            adapter.submitList(it)
            adapter.notifyDataSetChanged()
        }}
        viewModel.allMerkFromDb.observe(viewLifecycleOwner){it?.let {}}

        viewModel.navigateToLog.observe(viewLifecycleOwner, Observer {
            clearEditText()
            if (it == true) {
                this.findNavController().navigate(InputLogFragmentDirections.actionInputLogFragmentToLogFragment())
                viewModel.onNavigatedToLog()
            }
        })


        return binding.root
    }
    private fun handleAddNetClick(countModel: CountModel, position: Int) {
        // Handle the AddNet click action
    }
    private fun handleDeleteNetClick(countModel: CountModel, position: Int) {
        clearEditText()
        viewModel.deleteCountModel(countModel.id)
        notifyAnItemDeleted(position)
    }
    private fun handleBarangLogMerkClick(countModel: CountModel, position: Int) {
       // showLoadingIndicator()
        clearEditText()
        showPopUpDialog(countModel.id, "Merk")
       // hideLoadingIndicator()
    }

    private fun handleBarangLogKodeClick(countModel: CountModel, position: Int) {
        clearEditText()
        countModel.merkBarang?.let {
            showLoadingIndicator()
            viewModel.getWarnaByMerkOld(it)
            viewModel.codeWarnaByMerk.observe(viewLifecycleOwner) { colors ->
                if (colors != null) {
                    hideLoadingIndicator()
                    showPopUpDialog(countModel.id, "Warna")
                }
            }
        } ?: Toast.makeText(context, "Masukkan Merk", Toast.LENGTH_SHORT).show()
        //hideLoadingIndicator()
    }

    private fun handleBarangLogIsiClick(countModel: CountModel, position: Int) {
        clearEditText()
        showLoadingIndicator()
        if (countModel.merkBarang != null && countModel.kodeBarang != null) {
            viewModel.getIsiByWarnaAndMerk(countModel.merkBarang!!, countModel.kodeBarang!!)
            viewModel.isiByWarnaAndMerk.observe(viewLifecycleOwner) { isi ->
                if (isi != null) {
                    hideLoadingIndicator()
                    showPopUpDialog(countModel.id, "Isi")
                }
            }
        } else {
            hideLoadingIndicator()
            Toast.makeText(context, "Masukkan Merk dan warna", Toast.LENGTH_SHORT).show()
        }

    }

    private fun handleBarangLogPcsClick(countModel: CountModel, position: Int) {
        clearEditText()
        showLoadingIndicator()
        countModel.isi?.let {
            showPopUpDialog(countModel.id, "Pcs")
        } ?: Toast.makeText(context, "Masukkan Merk, warna dan isi", Toast.LENGTH_SHORT).show()
        hideLoadingIndicator()
    }

    fun showPopUpDialog(position: Int, code: String) {
        // Dismiss any existing dialog
        dialog?.dismiss()

        // Determine title and suggestions based on the code
        val title = when (code) {
            "Merk" -> "Merk"
            "Warna" -> "Kode Warna"
            "Isi" -> "Isi"
            else -> "Pcs"
        }

        val suggestions = when (code) {
            "Merk" -> viewModel.allMerkFromDb.value?.toTypedArray() ?: emptyArray()
            "Warna" -> viewModel.codeWarnaByMerk.value?.toTypedArray() ?: emptyArray()
            "Isi" -> viewModel.isiByWarnaAndMerk.value?.map { it.toString() }?.toTypedArray() ?: emptyArray()
            else -> emptyArray()
        }

        // Inflate the layout using DataBindingUtil
        val binding = DataBindingUtil.inflate<PopUpAutocompleteTextviewBinding>(
            LayoutInflater.from(context),
            R.layout.pop_up_autocomplete_textview,
            null,
            false
        ).apply {
            viewModel = this@InputLogFragment.viewModel

            // Access AutoCompleteTextView within TextInputLayout
            val autoCompleteTextView: AutoCompleteTextView = root.findViewById(R.id.autocomplete_text)
            autoCompleteTextView.apply {
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, suggestions)
                setAdapter(adapter)
                setOnClickListener { showDropDown() }
                setOnFocusChangeListener { _, hasFocus ->
                    if (hasFocus) showDropDown()
                }
                inputType = if (code in listOf("Isi", "Pcs"))
                    InputType.TYPE_CLASS_NUMBER
                else
                    InputType.TYPE_CLASS_TEXT
            }
        }

        // Create and show the dialog
        dialog = AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setView(binding.root)
            .setPositiveButton("OK") { dialog, _ ->
                handlePositiveClick(binding.root.findViewById<AutoCompleteTextView>(R.id.autocomplete_text).text.toString(), code, position)
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ ->
                viewModel.setLiveDataToNull()
                dialog.dismiss()
            }
            .create()
            .apply { show() }
    }

    private fun handlePositiveClick(input: String, code: String, position: Int) {
        when (code) {
            "Merk" -> viewModel.updateMerk(position, input)
            "Warna" -> viewModel.updateKode(position, input)
            "Isi" -> viewModel.updateIsi(position, input.toDouble())
            "Pcs" -> viewModel.updatePcs(position, input.toInt())
        }
        viewModel.setLiveDataToNull()
    }



    fun clearEditText(){
        binding.inputPembeli.clearFocus()
        binding.inputKet.clearFocus()
    }
    private fun notifyAnItemDeleted(position: Int) {
            adapter.notifyItemRemoved(position)
    }
    private fun showLoadingIndicator() {
        progressBar.visibility = View.VISIBLE
    }

    private fun hideLoadingIndicator() {
        progressBar.visibility = View.GONE
    }




    private fun setupDialog(inputStokLogModel: CountModel?) {
        if (isDialogShowing) return
        isDialogShowing = true

        val dialogBinding = DataBindingUtil.inflate<PopUpAddBarangLogBinding>(
            LayoutInflater.from(context), R.layout.pop_up_add_barang_log, null, false)

        val autoCompleteMerk = dialogBinding.txtMerk
        val autoCompleteWarna = dialogBinding.txtWarna
        val autoCompleteIsi = dialogBinding.txtIsi
        val etPcs = dialogBinding.txtPcs

        val oldCountModel = inputStokLogModel?.copy()
        Log.i("NEWPOPUPPROB","oldCOuntModel ${oldCountModel}")
        // Initialize the adapter for the AutoCompleteTextView
        val merkAdapter = ArrayAdapter<String>(requireContext(), android.R.layout.simple_dropdown_item_1line, emptyList())
        autoCompleteMerk.setAdapter(merkAdapter)

        val warnaAdapter = ArrayAdapter<String>(requireContext(), android.R.layout.simple_dropdown_item_1line, emptyList())
        autoCompleteWarna.setAdapter(warnaAdapter)

        val isiAdapter = ArrayAdapter<String>(requireContext(), android.R.layout.simple_dropdown_item_1line, mutableListOf())
        autoCompleteIsi.setAdapter(isiAdapter)

        if (inputStokLogModel != null) {
            autoCompleteMerk.setText(inputStokLogModel.merkBarang)
            autoCompleteWarna.setText(inputStokLogModel.kodeBarang)
            if (inputStokLogModel.isi!=null){autoCompleteIsi.setText(inputStokLogModel.isi.toString())}
            if (inputStokLogModel.psc!=0)etPcs.setText(inputStokLogModel.psc.toString())
            if (inputStokLogModel.merkBarang!=null)viewModel.getWarnaByMerkNew(inputStokLogModel.merkBarang!!)
            if (inputStokLogModel.kodeBarang!=null)viewModel.getIsiByWarnaAndMerk(inputStokLogModel.merkBarang!!,inputStokLogModel.kodeBarang!!)
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
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
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
            isiAdapter.addAll(isiList?.sortedBy { it }?.map { it.toString() }?: emptyList())
        }

        // Create and show the dialog
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .setPositiveButton("OK", null) // Set null for now, we will override the action
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .setOnDismissListener {
                isDialogShowing = false
            }
            .create()

        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                if (inputStokLogModel != null) {
                    val namaMerk = autoCompleteMerk.text.toString().trim()
                    val kodeWarna = autoCompleteWarna.text.toString().trim()
                    val isi = autoCompleteIsi.text.toString().trim().toDoubleOrNull()
                    val pcs = etPcs.text.toString().trim().toIntOrNull()
                    if (namaMerk.isNotEmpty() && kodeWarna.isNotEmpty() && isi != null && pcs != null) {
                        inputStokLogModel.merkBarang = namaMerk
                        inputStokLogModel.kodeBarang = kodeWarna
                        inputStokLogModel.isi = isi
                        inputStokLogModel.psc = pcs
                        viewModel.updateCountModel(inputStokLogModel,oldCountModel!!) { status ->
                            when (status) {
                                UpdateStatus.SUCCESS -> {
                                    Toast.makeText(requireContext(), "Berhasil", Toast.LENGTH_SHORT).show()
                                    dialog.dismiss()
                                }
                                UpdateStatus.MERK_NOT_PRESENT -> Toast.makeText(requireContext(), "Merk tidak ada di database", Toast.LENGTH_SHORT).show()
                                UpdateStatus.WARNA_NOT_PRESENT -> Toast.makeText(requireContext(), "Warna tidak ada di database", Toast.LENGTH_SHORT).show()
                                UpdateStatus.ISI_NOT_PRESENT -> Toast.makeText(requireContext(), "Isi tidak ada di database", Toast.LENGTH_SHORT).show()
                                UpdateStatus.PCS_NOT_READY_IN_STOCK -> Toast.makeText(requireContext(), "Jumlah barang tidak cukup", Toast.LENGTH_SHORT).show()
                                else -> {
                                    Toast.makeText(requireContext(), "Gagal mengubah data ", Toast.LENGTH_SHORT).show()
                                    // Optionally, you can log the status here if needed
                                }
                            }
                        }
                    } else {
                        Toast.makeText(context, "Gagal mengubah data ", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
            dialog.setOnDismissListener {
                isDialogShowing = false
            }
        dialog.show()
    }

}