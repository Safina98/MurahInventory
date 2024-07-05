package com.example.tokomurahinventory.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.tokomurahinventory.R
import com.example.tokomurahinventory.adapters.LogAdapter
import com.example.tokomurahinventory.adapters.LogClickListener
import com.example.tokomurahinventory.adapters.LogLongListener
import com.example.tokomurahinventory.adapters.MerkAdapter
import com.example.tokomurahinventory.adapters.MerkClickListener
import com.example.tokomurahinventory.adapters.MerkLongListener
import com.example.tokomurahinventory.database.DatabaseInventory
import com.example.tokomurahinventory.databinding.FragmentLogBinding
import com.example.tokomurahinventory.databinding.FragmentMerkBinding
import com.example.tokomurahinventory.viewmodels.LogViewModel
import com.example.tokomurahinventory.viewmodels.LogViewModelFactory
import com.example.tokomurahinventory.viewmodels.MerkViewModel
import com.example.tokomurahinventory.viewmodels.MerkViewModelFactory


class LogFragment : Fragment() {
    private lateinit var binding: FragmentLogBinding
    private lateinit var viewModel: LogViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_log,container,false)

        val application = requireNotNull(this.activity).application
        //val dataSource1 = DatabaseInventory.getInstance(application).merkDao
       // val viewModelFactory = LogViewModelFactory(application)
        binding.lifecycleOwner =this
        //val viewModel = ViewModelProvider(this,viewModelFactory).get(LogViewModel::class.java)
        viewModel = ViewModelProvider(requireActivity(), LogViewModelFactory(application))
            .get(LogViewModel::class.java)
        binding.viewModel = viewModel
        val adapter  = LogAdapter(
            LogClickListener {
               // viewModel.onNavigateToWarna(it.refMerk)
            },
            LogLongListener {
                // Handle item long click
            }
        )

        binding.rvLog.adapter = adapter

        adapter.submitList(viewModel.logDummy)

        viewModel.addLogFab.observe(viewLifecycleOwner, Observer {
            if (it==true){
                this.findNavController().navigate(LogFragmentDirections.actionLogFragmentToInputLogFragment())
                viewModel.onAddLogFabClicked()
            }
        })

        return binding.root
    }
}