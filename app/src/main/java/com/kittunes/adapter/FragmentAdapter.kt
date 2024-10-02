package com.kittunes.adapter

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.kittunes.fragments.HomeFragment
import com.kittunes.fragments.SearchFragment
import com.kittunes.playlist.LibraryFragment

class FragmentAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 3 // Updated to reflect the actual number of fragments

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> HomeFragment()
            1 -> SearchFragment()
            2 -> LibraryFragment()
            else -> throw IllegalStateException("Invalid fragment position")
        }
    }
}