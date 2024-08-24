package com.kittunes

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.kittunes.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null) {
            redirectToWelcome()
            return
        }
        // Initialize toolbar and navigation
        setupToolbar()
        setupDrawerNavigation()
        setupBottomNavigation()
        // Load the default fragment
        replaceFragment(HomeFragment())
        binding.currentsong.setOnClickListener {
            val bottomSheetFragment = SongDetailBottomSheetFragment()
            bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
        }

        val retrofitBuilder=Retrofit.Builder()
            .baseUrl("https://spotify23.p.rapidapi.com/search/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiInterface::class.java)
        val retrofitData=retrofitBuilder.getData("Arijit singh")

        retrofitData.enqueue(object : Callback<MyData> {
            override fun onResponse(call: Call<MyData>, response: Response<MyData>) {
                if (response.isSuccessful) {
                    val dataList = response.body()
                    if (dataList != null) {
                        val songTitleTextView: TextView = findViewById(R.id.dummy)
                        songTitleTextView.text = dataList.toString()
                    } else {
                        Log.e("Retrofit", "Data list is null")
                    }
                } else {
                    Log.e("Retrofit", "Response error: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<MyData>, t: Throwable) {
                Log.e("Retrofit", "Failure: ${t.message}")
            }
        })
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            toggleDrawer()
        }
        binding.menubtn.setOnClickListener {
            toggleDrawer()
        }
    }
    private fun toggleDrawer() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    private fun setupDrawerNavigation() {
        binding.navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_logout -> {
                    auth.signOut()
                    redirectToWelcome()
                }
                // Add other cases if needed
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.bottom_home -> {
                    replaceFragment(HomeFragment())
                    true
                }
                R.id.bottom_search -> {
                    replaceFragment(SearchFragment())
                    true
                }
                R.id.bottom_library -> {
                    replaceFragment(LibraryFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_container, fragment)
            .commit()
    }

    private fun redirectToWelcome() {
        startActivity(Intent(this, Welcome::class.java))
        finishAffinity()
    }
}