package com.example.tokomurahinventory.fragments


import com.example.tokomurahinventory.viewmodels.AuthViewModel
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels

abstract class AuthFragment : Fragment() {

    private val authViewModel: AuthViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        authViewModel.authenticationState.observe(this) { isAuthenticated ->
            if (!isAuthenticated) {
                Toast.makeText(requireContext(), "Authentication required", Toast.LENGTH_SHORT).show()
                // Optionally, navigate to login screen or take appropriate action
            }
        }
    }
}
