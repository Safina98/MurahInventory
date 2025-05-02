package com.example.tokomurahinventory


import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.work.WorkManager
import com.example.tokomurahinventory1.databinding.ActivityMainBinding
import com.example.tokomurahinventory.fragments.ParentFragment
import com.example.tokomurahinventory.utils.AppLifecycleObserver
import com.example.tokomurahinventory.utils.CleanupWorker
import com.example.tokomurahinventory.utils.SharedPreferencesHelper
import com.example.tokomurahinventory.viewmodels.AuthViewModel
import com.example.tokomurahinventory1.R
import org.w3c.dom.Text
import java.util.concurrent.TimeUnit



class MainActivity : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var drawerLayout: DrawerLayout
    private val authViewModel: AuthViewModel by viewModels()
    private var dialog: AlertDialog? = null
    private var isInputUNShowing: Boolean = false
    private lateinit var appLifecycleObserver: AppLifecycleObserver
    private var originalOrientation: Int = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
        drawerLayout = binding.drawerLayout
        SharedPreferencesHelper.initialize(this)

        drawerLayout = binding.drawerLayout
        SharedPreferencesHelper.initialize(this)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.myNavHostFragment) as NavHostFragment
        val navController = navHostFragment.navController

        // Setup NavigationUI
        NavigationUI.setupActionBarWithNavController(this, navController, drawerLayout)
        NavigationUI.setupWithNavController(binding.navView, navController)

        appLifecycleObserver = AppLifecycleObserver(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(appLifecycleObserver)

        // Check if the table is empty and insert a default user if needed
        authViewModel.checkAndInsertDefaultUser(applicationContext)
        checkAuthentication()
        authViewModel.showLoginDialog.observe(this) { shouldShow ->
            if (shouldShow) {
                showLoginDialog()
            }
        }
        originalOrientation = requestedOrientation
    }

    override fun onSupportNavigateUp(): Boolean {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.myNavHostFragment) as NavHostFragment
        val navController = navHostFragment.navController
        return NavigationUI.navigateUp(navController, drawerLayout) || super.onSupportNavigateUp()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.myNavHostFragment) as NavHostFragment
        val navController = navHostFragment.navController

        return when (item.itemId) {
            R.id.action_logout -> {
                Log.d("AppDebug", "Logout action triggered.")
                logout()
                true
            }
            else -> {
                // Delegate the navigation drawer toggle to NavigationUI
                NavigationUI.onNavDestinationSelected(item, navController) || super.onOptionsItemSelected(item)
            }
        }
    }


    private fun checkAuthentication() {
        authViewModel.authenticationState.observe(this) { isAuthenticated ->
            if (isAuthenticated == true) {
                Log.d("AppDebug", "Check authentication Login successful.")
                Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
                dialog?.dismiss() // Dismiss the dialog if it's showing
            } else {
                Log.d("AppDebug", "Check authentication Login failed. Invalid username or password.")
                Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show()
                showLoginDialog() // Show dialog if authentication failed
            }
        }
    }

    private fun showLoginDialog() {
        if (isFinishing || isDestroyed || (dialog != null && dialog!!.isShowing)) {
            Log.d("AppDebug", "Activity is finishing or destroyed, or dialog is already showing. Not showing login dialog.")
            return
        }
        Log.d("AppDebug", "Showing login dialog.")
        val dialogView = LayoutInflater.from(this).inflate(R.layout.pop_up_login, null)
        val etUsername = dialogView.findViewById<EditText>(R.id.etUsername)
        val etPassword = dialogView.findViewById<EditText>(R.id.etPassword)
        val btnLogin = dialogView.findViewById<Button>(R.id.btnLogin)
        val spinner = dialogView.findViewById<Spinner>(R.id.spinner_role)
        val txtForgetPassword = dialogView.findViewById<TextView>(R.id.txt_forget_password)
        txtForgetPassword.setOnClickListener {
            showPopUpInputUserName()
        }
        spinner.visibility = View.GONE
        dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        btnLogin.setOnClickListener {
            val username = etUsername.text.toString().lowercase().trim()
            val password = etPassword.text.toString().trim()
            Log.d("AppDebug", "Attempting login with username: $username")
            if (username.isNotEmpty() && password.isNotEmpty()) {
                authViewModel.authenticate(username, password, applicationContext) { isAuthenticated ->
                    if (isAuthenticated) {
                        Log.d("AppDebug", "Login successful.")
                        Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
                        dialog!!.dismiss() // Dismiss the dialog after successful login
                    } else {
                        Log.d("AppDebug", "Login failed. Invalid username or password.")
                        Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Log.d("AppDebug", "Username or password is empty.")
                Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show()
            }
        }

        dialog!!.show()
    }

    fun showPopUpInputUserName() {
        if (isFinishing || isDestroyed || isInputUNShowing) {
            Log.d("AppDebug", "Activity is finishing or destroyed, or dialog is already showing. Not showing login dialog.")
            return
        }
        isInputUNShowing = true
        val dialogView = LayoutInflater.from(this).inflate(R.layout.pop_up_autocomplete_textview, null)
        val usernameEt = dialogView.findViewById<TextView>(R.id.autocomplete_text)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("Enter Username")
            .setPositiveButton("OK") { dialogInterface, _ ->
                val enteredUsername = usernameEt.text.toString().trim()
                Log.d("AppDebug", "Entered Username: $enteredUsername")
                authViewModel.getAdminPassword(enteredUsername, this) { originalPassword ->
                    if (originalPassword != null) {
                        val trimmedPassword = originalPassword.dropWhile { it == '0' }
                        val passwordToUse = if (trimmedPassword.isEmpty()) "0" else trimmedPassword
                        val p = passwordToUse.toLong() * 328481L  // Use Long for multiplication

                        Log.d("AppDebug", "Encrypted Password: $p")  // Log the encrypted password
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_EMAIL, arrayOf("safinas413@gmail.com"))
                            putExtra(Intent.EXTRA_SUBJECT, "Encrypted Password")
                            putExtra(Intent.EXTRA_TEXT, "Kode: $p")
                        }
                        if (intent.resolveActivity(packageManager) != null) {
                            startActivity(Intent.createChooser(intent, "Send Message"))
                            Log.d("AppDebug", "WhatsApp intent started successfully.")
                            dialogInterface.dismiss()  // Dismiss the dialog only if the intent is successfully started
                        } else {
                            Log.e("WhatsAppError", "No WhatsApp client available to send the message.")
                        }

                    } else {
                        Log.e("PasswordError", "Failed to retrieve password")
                        // Don't dismiss the dialog if the password is null
                    }
                }
            }
            .setNegativeButton("Cancel") { dialogInterface, _ ->
                Log.d("AppDebug", "Cancel clicked")
                dialogInterface.dismiss()
            }
            .setOnDismissListener {
                isInputUNShowing = false
                Log.d("AppDebug", "Dialog dismissed")
            }
            .create()

        dialog.show()
    }




    fun sendEncryptedPassword(userName:String) {

    }



    override fun onResume() {
        super.onResume()
        //Log.d("AppDebug", "Activity resumed. Checking authentication.")

    }

    fun logout() {
        Log.d("AppDebug", "Logout action triggered.")

        SharedPreferencesHelper.clearUsername(this)
        authViewModel.setAuthenticationState(null)

    }




    override fun onDestroy() {
        super.onDestroy()
        /*
        if (this::dialog!!.isInitialized && dialog!!.isShowing) {
            Log.d("AppDebug", "Dismiss dialog in onDestroy.")
            dialog!!.dismiss()
        }

         */
    }
    fun resetOrientation() {
        requestedOrientation = originalOrientation
    }

    
}
