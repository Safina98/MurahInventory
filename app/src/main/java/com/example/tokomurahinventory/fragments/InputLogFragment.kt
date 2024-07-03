package com.example.tokomurahinventory.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.example.tokomurahinventory.R
import com.example.tokomurahinventory.adapters.AddNetClickListener
import com.example.tokomurahinventory.adapters.CountAdapter
import com.example.tokomurahinventory.adapters.DeleteNetClickListener
import com.example.tokomurahinventory.database.DatabaseInventory
import com.example.tokomurahinventory.databinding.FragmentInputLogBinding
import com.example.tokomurahinventory.databinding.FragmentLogBinding
import com.example.tokomurahinventory.databinding.FragmentMerkBinding
import com.example.tokomurahinventory.viewmodels.LogViewModel
import com.example.tokomurahinventory.viewmodels.LogViewModelFactory
import com.example.tokomurahinventory.viewmodels.MerkViewModel
import com.example.tokomurahinventory.viewmodels.MerkViewModelFactory


class InputLogFragment : Fragment() {

    private lateinit var binding: FragmentInputLogBinding
    private val viewModel: LogViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_input_log,container,false)

        val application = requireNotNull(this.activity).application
        //val dataSource1 = DatabaseInventory.getInstance(application).merkDao
        val viewModelFactory = LogViewModelFactory(application)
        binding.lifecycleOwner =this
        val viewModel = ViewModelProvider(this,viewModelFactory)
            .get(LogViewModel::class.java)
        binding.viewModel = viewModel

        val adapter  = CountAdapter(
            AddNetClickListener { countModel, position ->
            },
            DeleteNetClickListener{ countModel, position ->
                viewModel.deleteCountModel(countModel,position)
            } ,
            viewModel, this
        )

        binding.rvAddBarang.adapter = adapter
        viewModel.countModelList.observe(viewLifecycleOwner){it?.let {
            adapter.submitList(it)
        }}

        return binding.root
    }


}