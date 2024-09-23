package com.kittunes.initilization

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.kittunes.main.MainActivity
import com.kittunes.R
import com.kittunes.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        auth = FirebaseAuth.getInstance()
        binding.back.setOnClickListener {
            startActivity(Intent(this, Welcome::class.java))
            finish()
        }
        binding.gotosignup.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }

    fun logIn(view: View) {
        val email = binding.email.text.toString().trim()
        val password = binding.password.text.toString()
        if (validateInputs(email, password)) {
            signInUser(email, password)
        }
    }

    private fun signInUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null && user.isEmailVerified) {
                        navigateToMainActivity()
                    } else {
                        showError("Please verify your email address.")
                        auth.signOut()
                    }
                } else {
                    val exception = task.exception
                    if (exception is FirebaseAuthException) {
                        handleSignInError(exception)
                    } else {
                        showError("Sign-in failed. Please check your credentials and try again.")
                    }
                }
            }
    }

    private fun handleSignInError(exception: FirebaseAuthException) {
        Log.e("SignInError", "Error code: ${exception.errorCode}")
        Log.e("SignInError", "Error message: ${exception.message}")

        when (exception.errorCode) {
            "ERROR_INVALID_EMAIL" -> showError("The email address is not formatted correctly. Please enter a valid email.")
            "ERROR_USER_NOT_FOUND" -> showError("No account found with this email. Please sign up.")
            "ERROR_WRONG_PASSWORD" -> showError("Incorrect password. Please try again.")
            "ERROR_INVALID_CREDENTIAL" -> showError("The supplied credentials are incorrect or have expired. Please try again.")
            "ERROR_USER_DISABLED" -> showError("This account has been disabled. Please contact support.")
            "ERROR_EMAIL_ALREADY_IN_USE" -> showError("The email address is already in use by another account.")
            "ERROR_TOO_MANY_REQUESTS" -> showError("We have blocked all requests from this device due to unusual activity. Please try again later.")
            "ERROR_OPERATION_NOT_ALLOWED" -> showError("This operation is not allowed. Please enable the necessary settings in Firebase.")
            // Handle other specific error codes as needed
            else -> showError("Sign-in failed. Please check your credentials and try again.")
        }
    }

    private fun validateInputs(email: String, password: String): Boolean {
        var isValid = true
        if (email.isEmpty()) {
            binding.email.error = "Please enter your email"
            isValid = false
        }

        if (password.isEmpty()) {
            binding.password.error = "Please enter your password"
            isValid = false
        } else if (password.length < 8) {
            binding.password.error = "Password must be at least 8 characters"
            isValid = false
        }

        return isValid
    }


    private fun navigateToMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finishAffinity()
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

}