package com.gkiss01.meetdeb

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.gkiss01.meetdeb.data.User
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {
    private val viewModelKoin: ActivityViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun changeNavGraphToStart() {

    }

    fun changeNavGraphToMain() {

//        val navController = findNavController(R.id.main_nav_host_fragment)
////        navController.addOnDestinationChangedListener { _, destination, _ ->
////            if(destination.id == R.id.eventCreateFragment) {
////                main_navigationView.visibility = View.GONE
////            } else {
////                main_navigationView.visibility = View.VISIBLE
////            }
////        }
//
//        main_navigationView.setupWithNavController(navController)
//        main_navigationView.setOnNavigationItemReselectedListener {  }
//
//        val appBarConfiguration = AppBarConfiguration(setOf(R.id.eventsFragment, R.id.profileFragment))
//        setSupportActionBar(main_toolbarView)
//        setupActionBarWithNavController(navController, appBarConfiguration)
    }

//    override fun onSupportNavigateUp(): Boolean {
//        val navController = findNavController(R.id.main_nav_host_fragment)
//        return navController.navigateUp() || super.onSupportNavigateUp()
//    }

    companion object {
        lateinit var instance: MainActivity
    }

    init {
        instance = this
    }

    fun getActiveUser(): User? = viewModelKoin.activeUser.value?.data
}
