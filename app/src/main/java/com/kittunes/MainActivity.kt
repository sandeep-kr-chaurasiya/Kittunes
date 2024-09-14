package com.kittunes

import SharedViewModel
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
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

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val sharedViewModel: SharedViewModel by viewModels()
    private var musicService: MusicService? = null
    private var isBound = false

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

        // Start and bind to MusicService
        val serviceIntent = Intent(this, MusicService::class.java)
        startService(serviceIntent)
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)

        // Observe current song changes
        sharedViewModel.currentSong.observe(this) { currentSong ->
            currentSong?.let { updateSongData(it) }
        }

        binding.currentsong.setOnClickListener {
            sharedViewModel.currentSong.value?.let { song ->
                val bottomSheetFragment = SongDetailBottomFragment.newInstance(song, autoPlay = false)
                bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
            } ?: Log.d("MainActivity", "No current song to display")
        }
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            val musicBinder = binder as MusicService.MusicBinder
            musicService = musicBinder.getService()
            isBound = true
            Log.d("MainActivity", "MusicService bound")
        }
        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
            Log.d("MainActivity", "MusicService unbound")
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
        updatePlayPauseButton()

        if (musicService?.currentSong != song) {
            musicService?.playSong(song)
        }
    }

    private fun updatePlayPauseButton() {
        val isPlaying = musicService?.isPlaying ?: false
        val playPauseButton = binding.playButton
        playPauseButton.setImageResource(
            if (isPlaying) R.drawable.pause else R.drawable.play
        )
        playPauseButton.setOnClickListener {
            if (isPlaying) {
                musicService?.pausePlayback()
                sharedViewModel.setCurrentPosition(musicService?.mediaPlayer?.currentPosition ?: 0)
            } else {
                musicService?.resumePlayback()
                sharedViewModel.setCurrentPosition(musicService?.mediaPlayer?.currentPosition ?: 0)
            }
            updatePlayPauseButton()
        }
    }

    private fun redirectToWelcome() {
        startActivity(Intent(this, Welcome::class.java))
        finishAffinity()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isBound) {
            unbindService(serviceConnection)
            isBound = false
            Log.d("MainActivity", "Unbound MusicService")
        }
        stopService(Intent(this, MusicService::class.java))
    }
}