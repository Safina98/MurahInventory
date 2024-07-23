package com.example.tokomurahinventory.fragments

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.databinding.DataBindingUtil
import com.example.tokomurahinventory.R
import com.example.tokomurahinventory.databinding.FragmentParentBinding

class ParentFragment : Fragment() {

    private lateinit var binding: FragmentParentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
         binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_parent,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Check if savedInstanceState is null to add fragments only once
        val isPortrait = resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
        val fragmentLeft = MerkFragment()
        val fragmentMiddle=WarnaFragment()
        val fragmentRight = DetailWarnaFragment()
        val fragmentSingle = MerkFragment()
        val fragmentManager: FragmentManager = childFragmentManager
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()

        // Remove existing fragments
        fragmentManager.fragments.forEach { fragment ->
            fragmentTransaction.remove(fragment)
        }
        if (isPortrait) {
            // Show single child fragment
            fragmentTransaction.add(R.id.fragment_container, fragmentSingle)
        } else {
            // Show two child fragments
            fragmentTransaction.add(R.id.fragment_container_left, fragmentLeft)
            fragmentTransaction.add(R.id.fragment_container_middle,fragmentMiddle)
            fragmentTransaction.add(R.id.fragment_container_right, fragmentRight)
        }
        fragmentTransaction.commit()
    }



    private fun addFragmentsBasedOnOrientation() {
        val orientation = resources.configuration.orientation
        updateFragmentsForOrientation(orientation)
    }

    private fun updateFragmentsForOrientation(orientation: Int) {
        val fragmentManager: FragmentManager = childFragmentManager
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()

        // Remove existing fragments
        fragmentManager.fragments.forEach { fragment ->
            fragmentTransaction.remove(fragment)
        }

        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            val fragmentLeft = MerkFragment()
            val fragmentRight = WarnaFragment()
            fragmentTransaction.add(R.id.fragment_container_left, fragmentLeft)
            fragmentTransaction.add(R.id.fragment_container_right, fragmentRight)
        } else {
            val fragmentSingle = MerkFragment()
            fragmentTransaction.add(R.id.fragment_container, fragmentSingle)
        }

        fragmentTransaction.commit()
    }
}
