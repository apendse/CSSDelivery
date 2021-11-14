package com.aap.cssdelivery.ui

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.preference.PreferenceManager
import com.aap.cssdelivery.R
import com.aap.cssdelivery.databinding.ActivityMainBinding
import com.aap.cssdelivery.ui.viewmodel.MainViewModel
import com.aap.cssdelivery.utils.DatabaseFactory
const val DEFAULT_BASE_URL = "http://10.0.2.2:8080"
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var mainViewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initialize()
        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        readOrders()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val navController = findNavController(R.id.nav_host_fragment)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.orderListFragment, R.id.statsFragment, R.id.settingsFragment
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.bottomNavigation.setupWithNavController(navController)
        binding.status.visibility = View.GONE
        monitorService()
    }

    private fun monitorService() {
        binding.retry.setOnClickListener {
            readOrders()
        }

        mainViewModel.isKitchenClosed.observe(this, {
            Log.d("CSS", "Received kitchen status")
            handleKitchenStatus(it)
        })
        mainViewModel.isNetworkError.observe(this, {
            Log.d("CSS", "Received server status")
            handleNetworkStatus(it)
        })


    }

    private fun hideStatus() {
        with(binding.status) {
            if (visibility != View.GONE) {
                visibility = View.GONE
            }
        }
    }

    private fun showStatus() {
        with(binding.status) {
            if (this.visibility != View.VISIBLE) {
                visibility = View.VISIBLE
            }
        }
    }

    private fun handleKitchenStatus(isClosed: Boolean) {
        if (isClosed) {
            binding.statusMessage.text = getString(R.string.kitchen_not_open)
            showStatus()
        } else {
            hideStatus()
        }
    }

    @VisibleForTesting
    fun handleNetworkStatus(isNetworkDown: Boolean) {
        if (isNetworkDown) {
            binding.statusMessage.text = getString(R.string.server_is_down)
            showStatus()
        } else {
            hideStatus()
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            val navController = findNavController(R.id.nav_host_fragment)
            return navController.navigateUp()
        }
        return super.onOptionsItemSelected(item)
    }


    private fun initialize() {
        DatabaseFactory.initializeDatabase(applicationContext)
    }

    private fun readOrders() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val address = sharedPreferences.getString("address", DEFAULT_BASE_URL) ?: DEFAULT_BASE_URL
        mainViewModel.fetchOrders("http://$address")
    }
}