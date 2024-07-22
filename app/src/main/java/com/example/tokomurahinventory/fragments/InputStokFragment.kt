package com.example.tokomurahinventory.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.tokomurahinventory.R
import com.example.tokomurahinventory.adapters.DeleteInputStokLogClickListener
import com.example.tokomurahinventory.adapters.DeleteMerkClickListener
import com.example.tokomurahinventory.adapters.InputStokLogAdapter
import com.example.tokomurahinventory.adapters.InputStokLogClickListener
import com.example.tokomurahinventory.adapters.InputStokLogLongListener
import com.example.tokomurahinventory.adapters.MerkAdapter
import com.example.tokomurahinventory.adapters.MerkClickListener
import com.example.tokomurahinventory.adapters.MerkLongListener
import com.example.tokomurahinventory.adapters.UpdateInputStokLogClickListener
import com.example.tokomurahinventory.adapters.UpdateMerkClickListener
import com.example.tokomurahinventory.database.DatabaseInventory
import com.example.tokomurahinventory.databinding.FragmentInputStokBinding
import com.example.tokomurahinventory.models.MerkTable
import com.example.tokomurahinventory.models.model.InputStokLogModel
import com.example.tokomurahinventory.utils.DialogUtils

import com.example.tokomurahinventory.utils.SharedPreferencesHelper
import com.example.tokomurahinventory.viewmodels.DetailWarnaViewModel
import com.example.tokomurahinventory.viewmodels.DetailWarnaViewModelFactory
import com.example.tokomurahinventory.viewmodels.InputStokViewModel
import com.example.tokomurahinventory.viewmodels.InputStokViewModelFactory
import com.example.tokomurahinventory.viewmodels.MerkViewModel
import java.util.Calendar


class InputStokFragment : AuthFragment() {

    private lateinit var binding: FragmentInputStokBinding
    private val viewModel: InputStokViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_input_stok,container,false)
        val application = requireNotNull(this.activity).application

        val dataSourceBarangLog = DatabaseInventory.getInstance(application).barangLogDao
        val dataSourceDetailWarna = DatabaseInventory.getInstance(application).detailWarnaDao
        val loggedInUser = SharedPreferencesHelper.getLoggedInUser(requireContext()) ?: ""
        val viewModelFactory = InputStokViewModelFactory(dataSourceBarangLog,dataSourceDetailWarna,loggedInUser,application)
        binding.lifecycleOwner =this
        val viewModel = ViewModelProvider(this,viewModelFactory)
            .get(InputStokViewModel::class.java)
        binding.viewModel = viewModel
        val adapter  = InputStokLogAdapter(
            InputStokLogClickListener {

            }, InputStokLogLongListener{

            }, UpdateInputStokLogClickListener{

            }, DeleteInputStokLogClickListener {
                DialogUtils.showDeleteDialog(this, viewModel, it, { vm, item -> (vm as InputStokViewModel).deleteInputStok(item as InputStokLogModel) })
            }

        )
        binding.rvInputStokLog.adapter=adapter
        viewModel.inputLogModel.observe(viewLifecycleOwner, Observer {
            adapter.submitList(it)
            adapter.notifyDataSetChanged()
            Log.i("INPUTLOGTRY","$it")
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
                showDatePickerDialog(1)
                viewModel.onStartDatePickerClicked()
            }
        }
        viewModel.selectedStartDate.observe(viewLifecycleOwner) {
            //viewModel.updateRv4()
        }
        viewModel.selectedEndDate.observe(viewLifecycleOwner) {
            viewModel.updateRv4()
        }



        return binding.root
    }
    private fun showDatePickerDialog(code:Int) {
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
                    set(startYear, startMonth, startDay, 0, 0, 0) // Set time to start of the day
                    set(Calendar.MILLISECOND, 0)
                }.time

                val endDate = Calendar.getInstance().apply {
                    set(endYear, endMonth, endDay, 23, 59, 59) // Set time to end of the day
                    set(Calendar.MILLISECOND, 999)
                }.time
                viewModel.updateDateRangeString(startDate,endDate)
                viewModel.setStartDateRange(startDate)
                viewModel.setEndDateRange(endDate)
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }
}