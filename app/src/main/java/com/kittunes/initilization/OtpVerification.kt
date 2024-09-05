package com.kittunes.initilization

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.kittunes.MainActivity
import com.kittunes.R
import com.kittunes.databinding.ActivityOtpVerificationBinding

class OtpVerification : AppCompatActivity() {
    private lateinit var binding: ActivityOtpVerificationBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseFirestore
    private lateinit var loader: ProgressDialog
    private val handler = Handler(Looper.getMainLooper())  // Handler for polling

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtpVerificationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        auth = FirebaseAuth.getInstance()
        database = FirebaseFirestore.getInstance()
        loader = ProgressDialog(this).apply {
            setMessage("Waiting for verification")
            setCancelable(false)
        }
    }

    fun createAccount(view: View) {
        val email = intent?.getStringExtra("email")
        val name = intent?.getStringExtra("name")
        val password = binding.password.text.toString()

        if (validateInputs(email, name, password)) {
            loader.show()
            auth.createUserWithEmailAndPassword(email!!, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        if (user != null) {
                            sendVerificationEmail(user, name!!)
                        } else {
                            showError("Failed to create account. Please try again.")
                            loader.dismiss()
                        }
                    } else {

                        binding.errormsg.visibility = View.VISIBLE
                        binding.gotologin.visibility = View.VISIBLE
                        loader.dismiss()

                    }
                }
        }
    }

    private fun validateInputs(email: String?, name: String?, password: String): Boolean {
        return when {
            email.isNullOrEmpty() -> {
                showError("Email must not be empty.")
                false
            }
            name.isNullOrEmpty() -> {
                showError("Name must not be empty.")
                false
            }
            password.isEmpty() -> {
                showError("Password must not be empty.")
                false
            }
            else -> true
        }
    }

    private fun sendVerificationEmail(user: FirebaseUser, name: String) {
        user.sendEmailVerification().addOnCompleteListener { verificationTask ->
            if (verificationTask.isSuccessful) {
                showToast("Verification email sent to ${user.email}")
                saveUserDataToFirebase(user.uid, name)
                checkEmailVerificationStatus()
            } else {
                showError("Failed to send verification email.")
                loader.dismiss()
            }
        }
    }

    private fun checkEmailVerificationStatus() {
        val user = auth.currentUser
        if (user == null) {
            handleUserUnavailable()
            return
        }

        handler.postDelayed({
            user.reload().addOnSuccessListener {
                if (user.isEmailVerified) {
                    navigateToMainActivity(user.email)
                } else {
                    checkEmailVerificationStatus()
                }
            }.addOnFailureListener { e ->
                handleReloadFailure(e)
            }
        }, 1000)
    }

    private fun handleUserUnavailable() {
        loader.dismiss()
        showError("User is no longer available")
        startActivity(Intent(this, Welcome::class.java))
        finishAffinity()
    }

    private fun handleReloadFailure(e: Exception) {
        Log.e(TAG, "Failed to reload user", e)
        loader.dismiss()
        showError("Failed to check verification status. Please try again.")
    }

    private fun navigateToMainActivity(email: String?) {
        loader.dismiss()
        startActivity(Intent(this, MainActivity::class.java).apply {
            putExtra("email", email)
        })
        finishAffinity()
    }

    private fun saveUserDataToFirebase(uid: String, name: String) {
        database.collection("users").document(uid)
            .set(mapOf("name" to name))
            .addOnSuccessListener {
                Log.d(TAG, "DocumentSnapshot successfully written!")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error writing document", e)
            }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    companion object {
        private const val TAG = "OtpVerification"
    }

    fun gotologin(view: View) {
        startActivity(Intent(this, LoginActivity::class.java))
        finishAffinity()
    }
}