package tw.edu.niu.investment_android

import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import tw.edu.niu.investment_android.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        // 取得 NavController
        val navController = findNavController(R.id.nav_host_fragment_content_main)

        // 將 FAB 按下時改為導向 InputsFragment（nav_inputs）
        binding.appBarMain.fab.setOnClickListener {
            // 檢查目前是否已在 InputsFragment，避免重複導航
            if (navController.currentDestination?.id != R.id.nav_inputs) {
                navController.navigate(R.id.nav_inputs)
            }
        }

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView

        // 將各目錄列為頂層，含 inputs
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home,
                R.id.nav_gallery,
                R.id.nav_slideshow,
                R.id.nav_inputs,
                R.id.nav_outputs
            ),
            drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}
