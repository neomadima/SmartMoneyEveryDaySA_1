package com.example.smartmoneyeverydaysa_1

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.smartmoneyeverydaysa_1.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        val navController = navHostFragment.navController
        
        binding.bottomNavigation.setupWithNavController(navController)

        appBarConfiguration = AppBarConfiguration(setOf(
            R.id.AuthFragment, R.id.DashboardFragment, R.id.AccountsFragment, R.id.SettingsFragment, R.id.ProfileFragment
        ))
        setupActionBarWithNavController(navController, appBarConfiguration)

        binding.fabAdd.setOnClickListener {
            navController.navigate(R.id.AddExpenseFragment)
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.AuthFragment, R.id.DashboardFragment, R.id.AccountsFragment,
                R.id.TransferFragment, R.id.TransactionDetailFragment, R.id.PayFragment,
                R.id.StatementsFragment, R.id.AccountDetailFragment, R.id.AddAccountFragment,
                R.id.AddGoalFragment, R.id.LimitsFragment, R.id.StatementViewFragment, 
                R.id.ProfileFragment, R.id.EditProfileFragment -> {
                    binding.appBarLayout.visibility = View.GONE
                }
                else -> {
                    binding.appBarLayout.visibility = View.VISIBLE
                }
            }

            when (destination.id) {
                R.id.AuthFragment, R.id.AddExpenseFragment, R.id.EditProfileFragment,
                R.id.AddGoalFragment, R.id.AddAccountFragment, R.id.PayFragment, 
                R.id.TransferFragment, R.id.LimitsFragment -> {
                    binding.bottomAppBar.visibility = View.GONE
                    binding.fabAdd.visibility = View.GONE
                }
                else -> {
                    binding.bottomAppBar.visibility = View.VISIBLE
                    binding.fabAdd.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}