package com.kittunes

import SharedViewModel
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.kittunes.Api_Data.Data
import com.kittunes.databinding.ActivityMainBinding
import com.kittunes.fragments.HomeFragment
import com.kittunes.fragments.LibraryFragment
import com.kittunes.fragments.SearchFragment
import com.kittunes.fragments.SongDetailBottomFragment
import com.kittunes.services.MusicService
import com.kittunes.initilization.Welcome
import android.media.MediaPlayer

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val sharedViewModel: SharedViewModel by viewModels()
    private var mediaPlayer: MediaPlayer? = null

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

        setupUserDetails()
        setupToolbar()
        setupDrawerNavigation()
        setupBottomNavigation()

        // Load the default fragment
        replaceFragment(HomeFragment())

        // Observe current song changes
        sharedViewModel.currentSong.observe(this, Observer { currentSong ->
            currentSong?.let { updateSongData(it) }
        })

        binding.currentsong.setOnClickListener {
            sharedViewModel.currentSong.value?.let { song ->
                val bottomSheetFragment = SongDetailBottomFragment.newInstance(song)
                bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
            } ?: Log.d("MainActivity", "No current song to display")
        }
    }

    private fun setupUserDetails() {
        auth.currentUser?.uid?.let { userId ->
            firestore.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    val username = document?.getString("name") ?: "User"
                    val firstName = username.split(" ").firstOrNull() ?: "User"
                    binding.username.text = "Welcome $firstName"
                    findViewById<TextView>(R.id.drawerusername).text = firstName
                }
                .addOnFailureListener { exception ->
                    Log.e("MainActivity", "Error fetching user details", exception)
                    binding.username.text = "Error fetching username"
                }
        } ?: run {
            binding.username.text = "User not logged in"
            Log.d("MainActivity", "User is not logged in")
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { toggleDrawer() }
        binding.menubtn.setOnClickListener { toggleDrawer() }
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
                else -> Log.e("MainActivity", "Unknown drawer item selected")
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
                else -> {
                    Log.e("MainActivity", "Unknown bottom navigation item selected")
                    false
                }
            }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_container, fragment)
            .commit()
    }

    private fun updateSongData(song: Data) {
        Log.d("MainActivity", "Updating song data: ${song.title}")
        binding.songTitle.text = song.title
        binding.artistName.text = song.artist.name
        Glide.with(this)
            .load(song.album.cover_medium)
            .into(binding.songImage)


        // Update play/pause button state
        val playButton = binding.currentsong.findViewById<ImageButton>(R.id.play_button)
        playButton.setImageResource(
            if (mediaPlayer?.isPlaying == true) R.drawable.pause
            else R.drawable.play
        )
    }

    private fun redirectToWelcome() {
        startActivity(Intent(this, Welcome::class.java))
        finishAffinity()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
    }
}