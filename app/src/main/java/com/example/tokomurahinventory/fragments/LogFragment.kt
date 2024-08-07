package com.example.tokomurahinventory.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.tokomurahinventory.R
import com.example.tokomurahinventory.adapters.LogAdapter
import com.example.tokomurahinventory.adapters.LogClickListener
import com.example.tokomurahinventory.adapters.LogDeleteListener
import com.example.tokomurahinventory.adapters.LogLongListener
import com.example.tokomurahinventory.database.DatabaseInventory
import com.example.tokomurahinventory.databinding.FragmentLogBinding
import com.example.tokomurahinventory.models.LogTable
import com.example.tokomurahinventory.utils.DialogUtils
import com.example.tokomurahinventory.utils.SharedPreferencesHelper
import com.example.tokomurahinventory.viewmodels.LogViewModel
import com.example.tokomurahinventory.viewmodels.LogViewModelFactory
import java.util.Calendar


class LogFragment : AuthFragment(){
    private lateinit var binding: FragmentLogBinding
    private lateinit var viewModel: LogViewModel
    private var isDialogShowing = false

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
                DialogUtils.showCreratedEdited(requireContext(),it.createdBy?: it.userName ?: "User has been deleted",it.lastEditedBy?: it.userName ?: "User has been deleted",it.logCreatedDate,it.logLastEditedDate,null)
            }, LogDeleteListener {
                DialogUtils.showDeleteDialog(this, viewModel, it, { vm, item -> (vm as LogViewModel).deleteLog(item as LogTable) })
            }
        )
        binding.rvLog.adapter = adapter
        viewModel.isLogLoading.observe(viewLifecycleOwner) { isLoading ->
            Log.i("LoadLogProbs", "isLoading observer : $isLoading")
            if (isLoading) {
                binding.progressBarLog.visibility = View.VISIBLE
                binding.rvLog.visibility = View.GONE
                binding.textCrashed.visibility = View.GONE
            } else {
                // This should only hide the ProgressBar if not loading
                binding.progressBarLog.visibility = View.GONE
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
        binding.textCrashed.setOnClickListener{
            viewModel.updateRv4()
        }
        //adapter.submitList(viewModel.logDummy)
        viewModel.allLog.observe(viewLifecycleOwner, Observer {
            it?.let{
                adapter.submitList(it.sortedByDescending { it.logLastEditedDate })
                adapter.notifyDataSetChanged()
                Log.i("dataSize","${it.size}")
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
        Log.i("DATEDIALOGPROB","show date dilaog called")
        viewModel.setInitialStartDateAndEndDate()
    //viewModel.getAllLogTable()
    }
    private fun showDatePickerDialog(code:Int) {

        if (isDialogShowing) return

        isDialogShowing = true
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
        dialog.setOnDismissListener {
            isDialogShowing = false
        }
        dialog.show()
    }
}