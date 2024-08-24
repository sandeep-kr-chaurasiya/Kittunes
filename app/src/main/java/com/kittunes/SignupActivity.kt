package com.kittunes

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.kittunes.databinding.ActivitySignupBinding

class SignupActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.signup)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.back.setOnClickListener {
            startActivity(Intent(this, Welcome::class.java))
        }
        binding.gotologin.setOnClickListener{
            startActivity(Intent(this, LoginActivity::class.java))
            finishAffinity()
        }
    }

    fun next(view: View) {
        val name = binding.name.text.toString().trim()
        val email = binding.email.text.toString().trim()

        if (validateInputs(name, email)) {
            val intent = Intent(this, OtpVerification::class.java).apply {
                putExtra("email", email)
                putExtra("name", name)
            }
            startActivity(intent)
        }
    }

    private fun validateInputs(name: String, email: String): Boolean {
        if (name.isEmpty()) {
            binding.name.error = "Please enter your name"
            binding.name.requestFocus()
            return false
        }
        if (email.isEmpty()) {
            binding.email.error = "Please enter your email"
            binding.email.requestFocus()
            return false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.email.error = "Please enter a valid email address"
            binding.email.requestFocus()
            return false
        }

        return true
    }

}