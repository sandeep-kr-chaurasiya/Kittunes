package com.kittunes

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.kittunes.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        binding.toolbar.setNavigationOnClickListener {
            if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                binding.drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                binding.drawerLayout.openDrawer(GravityCompat.START)
            }
        }
        // Handle the ImageButton click to open/close the drawer
        binding.menubtn.setOnClickListener {
            if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                binding.drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                binding.drawerLayout.openDrawer(GravityCompat.START)
            }
        }
        // Handle navigation item selection
        binding.navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                // Handle navigation menu item clicks here
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
        binding.bottomNavigation.setOnItemSelectedListener { menuItem ->
            when(menuItem.itemId){
                 R.id.bottom_home->{
                     replaceFragment(HomeFragment())
                     true
                 }
                R.id.bottom_search->{
                    replaceFragment(SearchFragment())
                    true
                }
                R.id.bottom_library->{
                    replaceFragment(LibraryFragment())
                    true
                }
                else->false
            }
        }
        replaceFragment(HomeFragment())
        //Authentication

    }
    private fun replaceFragment(fragment: Fragment){
        supportFragmentManager.beginTransaction().replace(R.id.frame_container, fragment ).commit()

    }
}