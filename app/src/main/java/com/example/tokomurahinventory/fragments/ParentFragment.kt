package com.example.tokomurahinventory.fragments

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
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
        updateUI()
    }

   fun updateUI() {
        val isPortrait = resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
        Log.d("ParentFragment", "Updating UI - Portrait: $isPortrait")

        val fragmentLeft = MerkFragment()
        val fragmentMiddle = WarnaFragment()
        val fragmentRight = DetailWarnaFragment()
        val fragmentSingle = MerkFragment()
        val fragmentManager = childFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        // Check if layout containers are available
        val containerLeft = view?.findViewById<View>(R.id.fragment_container_left)
        val containerMiddle = view?.findViewById<View>(R.id.fragment_container_middle)
        val containerRight = view?.findViewById<View>(R.id.fragment_container_right)

        if (isPortrait) {
            if (containerLeft == null) {
                Log.e("ParentFragment", "Portrait layout container not found")
            }
            fragmentTransaction.replace(R.id.fragment_container, fragmentSingle)
        } else {
            if (containerLeft == null || containerMiddle == null || containerRight == null) {
                Log.e("ParentFragment", "Landscape layout containers are not available")
            } else {
                fragmentTransaction.replace(R.id.fragment_container_left, fragmentLeft)
                fragmentTransaction.replace(R.id.fragment_container_middle, fragmentMiddle)
                fragmentTransaction.replace(R.id.fragment_container_right, fragmentRight)
            }
        }

        fragmentTransaction.commit()
    }


}
