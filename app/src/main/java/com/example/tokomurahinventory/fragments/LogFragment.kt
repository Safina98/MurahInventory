package com.example.tokomurahinventory.fragments

import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.tokomurahinventory.R
import com.example.tokomurahinventory.adapters.LogAdapter
import com.example.tokomurahinventory.adapters.LogClickListener
import com.example.tokomurahinventory.adapters.LogDeleteListener
import com.example.tokomurahinventory.adapters.LogLongListener
import com.example.tokomurahinventory.database.DatabaseInventory
import com.example.tokomurahinventory.databinding.FragmentLogBinding
import com.example.tokomurahinventory.models.LogTable
import com.example.tokomurahinventory.models.UsersTable
import com.example.tokomurahinventory.utils.DialogUtils
import com.example.tokomurahinventory.utils.SharedPreferencesHelper
import com.example.tokomurahinventory.viewmodels.LogViewModel
import com.example.tokomurahinventory.viewmodels.LogViewModelFactory
import com.example.tokomurahinventory.viewmodels.UsersViewModel
import java.util.Calendar


class LogFragment : AuthFragment(){
    private lateinit var binding: FragmentLogBinding
    private lateinit var viewModel: LogViewModel


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_log,container,false)

        val application = requireNotNull(this.activity).application
        val dataSourceLog = DatabaseInventory.getInstance(application).logDao
        val dataSourcebarangLog = DatabaseInventory.getInstance(application).barangLogDao
        val dataSourceMerk =  DatabaseInventory.getInstance(application).merkDao
        val dataSourceWarna =  DatabaseInventory.getInstance(application).warnaDao
        val dataSourceDetailWarna =  DatabaseInventory.getInstance(application).detailWarnaDao
        binding.lifecycleOwner =this
        val loggedInUser = SharedPreferencesHelper.getLoggedInUser(requireContext()) ?: ""
        viewModel = ViewModelProvider(requireActivity(), LogViewModelFactory(dataSourceMerk,dataSourceWarna,dataSourceDetailWarna,dataSourceLog,dataSourcebarangLog,loggedInUser,application)).get(LogViewModel::class.java)
        binding.viewModel = viewModel

        viewModel.resetTwoWayBindingSub()
        val adapter  = LogAdapter(
            LogClickListener {
               // viewModel.onNavigateToWarna(it.refMerk)
                viewModel.populateMutableLiveData(it)
                viewModel.onAddLogFabClick()
            },
            LogLongListener {
                // Handle item long click
            }, LogDeleteListener {
                DialogUtils.showDeleteDialog(this, viewModel, it, { vm, item -> (vm as LogViewModel).deleteLog(item as LogTable) })
            }
        )
        binding.rvLog.adapter = adapter

        //adapter.submitList(viewModel.logDummy)
        viewModel.allLog.observe(viewLifecycleOwner, Observer {
            it?.let{
                adapter.submitList(it.sortedBy { it.logLastEditedDate })
                adapter.notifyDataSetChanged()
                Log.i("WarnaProb","$it")
            }
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
        viewModel.selectedStartDate.observe(viewLifecycleOwner) {
            viewModel.updateRv4()
        }
        viewModel.selectedEndDate.observe(viewLifecycleOwner) {
            viewModel.updateRv4()
        }
        viewModel.isStartDatePickerClicked.observe(viewLifecycleOwner) {
            if (it==true){
                showDatePickerDialog(1)
                viewModel.onStartDatePickerClicked()
            }
        }
        viewModel.addLogFab.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                this.findNavController().navigate(LogFragmentDirections.actionLogFragmentToInputLogFragment())
                viewModel.onAddLogFabClicked()
            }
        })

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        viewModel.getAllLogTable()
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