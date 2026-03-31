package myplaces.ui

import android.os.Bundle
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import myplaces.R
import myplaces.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.ThemeApp) // Set theme after splash screen.
        super.onCreate(savedInstanceState)

        _binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.lifecycleOwner = this

        setupNavigation()
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.navHostFragment)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    private fun setupNavigation() {
        val navController: NavController = findNavController(R.id.navHostFragment)
        val appBarConfiguration = AppBarConfiguration(navController.graph)

        setSupportActionBar(binding.toolbar)
        setupActionBarWithNavController(navController, appBarConfiguration)

        navController.addOnDestinationChangedListener { _, destination: NavDestination, _ ->
            val toolBar = supportActionBar ?: return@addOnDestinationChangedListener

            binding.toolbar.setBackgroundColor(
                ContextCompat.getColor(this, R.color.backgroundColor)
            )
            this.window.navigationBarColor = ContextCompat.getColor(this, R.color.backgroundColor)
            this.window.statusBarColor = ContextCompat.getColor(this, R.color.primaryColor)

            when (destination.id) {
                R.id.placesFragment -> setToolBarView(toolBar, showTitle = true, showUpButton = false)
                R.id.addPlaceFragment -> setToolBarView(toolBar, showTitle = true, showUpButton = true)
                R.id.placeDetailFragment -> setToolBarView(toolBar, showTitle = true, showUpButton = true)
                R.id.mapFragment -> setToolBarView(toolBar, showTitle = true, showUpButton = true)
            }
        }
    }

    private fun setToolBarView(
        toolBar: ActionBar, showTitle: Boolean, showUpButton: Boolean
    ) {
        toolBar.setDisplayShowTitleEnabled(showTitle)
        toolBar.setDisplayHomeAsUpEnabled(showUpButton)
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }
}