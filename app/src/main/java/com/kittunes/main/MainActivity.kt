package com.kittunes.main

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.kittunes.api_data.Data
import com.kittunes.R
import com.kittunes.SearchFragment
import com.kittunes.databinding.ActivityMainBinding
import com.kittunes.fragments.HomeFragment
import com.kittunes.fragments.LibraryFragment
import com.kittunes.fragments.SongDetailBottomFragment
import com.kittunes.initilization.Welcome
import com.kittunes.player.MusicService
import com.kittunes.player.SharedViewModel
import com.kittunes.player.ViewModelFactory

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var musicService: MusicService? = null
    private var isBound = false
    private val sharedViewModel: SharedViewModel by viewModels {
        ViewModelFactory(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupUserAuthentication()
        setupNavigationComponents()
        startMusicService()

        sharedViewModel.currentSong.observe(this) { currentSong ->
            currentSong?.let { updateSongData(it) }
        }

        sharedViewModel.isPlaying.observe(this) { isPlaying ->
            updatePlayPauseButton(isPlaying)
        }

        binding.currentsong.setOnClickListener {
            sharedViewModel.currentSong.value?.let { currentSong ->
                showSongDetailBottomFragment(currentSong, sharedViewModel.isPlaying.value ?: false)
            } ?: Log.d(TAG, "No current song to display")
        }
    }

    private fun setupUserAuthentication() {
        auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null) {
            redirectToWelcome()
            return
        }
        setupUserDetails()
    }

    @SuppressLint("SetTextI18n")
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
                    Log.e(TAG, "Error fetching user details", exception)
                    binding.username.text = "Error fetching username"
                }
        } ?: run {
            binding.username.text = "User not logged in"
            Log.d(TAG, "User is not logged in")
        }
    }

    private fun setupNavigationComponents() {
        setupToolbar()
        setupDrawerNavigation()
        setupBottomNavigation()
        replaceFragment(HomeFragment())
    }

    private fun startMusicService() {
        val serviceIntent = Intent(this, MusicService::class.java)
        startService(serviceIntent)
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
        restorePlaybackState()
    }

    private fun restorePlaybackState() {
        val currentSong = sharedViewModel.currentSong.value
        val isPlaying = sharedViewModel.isPlaying.value ?: false
        binding.currentsong.visibility = if (currentSong != null) View.VISIBLE else View.GONE
        currentSong?.let { updateSongData(it) }
        updatePlayPauseButton(isPlaying)

        if (isBound) {
            if (currentSong != null) {
                musicService?.prepareSong(currentSong)
            }
            if (isPlaying) {
                musicService?.resumePlayback()
            } else {
                musicService?.pausePlayback()
            }
        }
    }

    private fun showSongDetailBottomFragment(currentSong: Data, isPlaying: Boolean) {
        val bottomSheetFragment = SongDetailBottomFragment.newInstance(currentSong, autoPlay = false)
        bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
        updatePlayPauseButton(isPlaying)
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            val musicBinder = binder as? MusicService.MusicBinder
            musicService = musicBinder?.getService()
            isBound = true
            Log.d(TAG, "MusicService bound")
            restorePlaybackState()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
            musicService = null
            Log.d(TAG, "MusicService unbound")
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
                else -> Log.e(TAG, "Unknown drawer item selected")
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.bottom_home -> replaceFragment(HomeFragment())
                R.id.bottom_search -> replaceFragment(SearchFragment())
                R.id.bottom_library -> replaceFragment(LibraryFragment())
                else -> {
                    Log.e(TAG, "Unknown bottom navigation item selected")
                    return@setOnItemSelectedListener false
                }
            }
            true
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_container, fragment)
            .commit()
    }

    private fun updateSongData(song: Data) {
        Log.d(TAG, "Updating song data: ${song.title}")
        binding.title.text = song.title
        binding.artist.text = song.artist.name
        Glide.with(this)
            .load(song.album.cover_medium)
            .into(binding.cover)

        if (musicService?.currentSong != song) {
            musicService?.prepareSong(song)
        }
    }

    private fun updatePlayPauseButton(isPlaying: Boolean) {
        binding.btnPlayPause.setImageResource(if (isPlaying) R.drawable.pause else R.drawable.play1)
        binding.btnPlayPause.setOnClickListener {
            if (isPlaying) {
                musicService?.pausePlayback()
                sharedViewModel.setPlayingState(false)
            } else {
                musicService?.resumePlayback()
                sharedViewModel.setPlayingState(true)
            }
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
            Log.d(TAG, "Unbound MusicService")
        }
        stopService(Intent(this, MusicService::class.java))
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}