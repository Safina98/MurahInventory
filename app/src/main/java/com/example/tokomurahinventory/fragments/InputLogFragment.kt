package com.example.tokomurahinventory.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.tokomurahinventory.R
import com.example.tokomurahinventory.adapters.AddNetClickListener
import com.example.tokomurahinventory.adapters.CountAdapter
import com.example.tokomurahinventory.adapters.DeleteNetClickListener
import com.example.tokomurahinventory.databinding.FragmentInputLogBinding
import com.example.tokomurahinventory.viewmodels.LogViewModel
import com.example.tokomurahinventory.viewmodels.LogViewModelFactory


class InputLogFragment : Fragment() {

    private lateinit var binding: FragmentInputLogBinding
    private lateinit var viewModel: LogViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_input_log,container,false)

        val application = requireNotNull(this.activity).application
        viewModel = ViewModelProvider(requireActivity(), LogViewModelFactory(application))
            .get(LogViewModel::class.java)
        binding.lifecycleOwner =this
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
        viewModel.navigateToLog.observe(viewLifecycleOwner, Observer {
            if (it==true){
                this.findNavController().navigate(InputLogFragmentDirections.actionInputLogFragmentToLogFragment())
                viewModel.onNavigatedToLog()
            }
        })

        return binding.root
    }


}