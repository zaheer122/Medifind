package com.app.medifindfinal
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
class SignupActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var signupButton: Button
    private lateinit var loginPromptTextView: TextView
    private lateinit var errorTextView: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)
        auth = FirebaseAuth.getInstance()
        // Initialize UI elements
        emailEditText = findViewById(R.id.editTextSignupEmail)
        passwordEditText = findViewById(R.id.editTextSignupPassword)
        confirmPasswordEditText = findViewById(R.id.editTextSignupConfirmPassword)
        signupButton = findViewById(R.id.buttonSignupEmailPassword)
        loginPromptTextView = findViewById(R.id.textViewSignupLoginPrompt)
        errorTextView = findViewById(R.id.textViewSignupError)
        signupButton.setOnClickListener {
            signupWithEmailPassword()
        }
        loginPromptTextView.setOnClickListener {
            finish() // Just go back to Login Activity
        }
    }
    private fun signupWithEmailPassword() {
        val email = emailEditText.text.toString()
        val password = passwordEditText.text.toString()
        val confirmPassword = confirmPasswordEditText.text.toString()
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
            errorTextView.text = "Please fill in all fields."
            errorTextView.visibility = View.VISIBLE
            return
        }
        if (password != confirmPassword) {
            errorTextView.text = "Passwords do not match."
            errorTextView.visibility = View.VISIBLE
            return
        }
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Signup success
                    Toast.makeText(this, "Signup Successful.", Toast.LENGTH_SHORT).show()
                    finish() // Go back to Login Activity after signup
                } else {
                    // If signup fails, display a message to the user.
                    errorTextView.text = "Signup failed: ${task.exception?.message}"
                    errorTextView.visibility = View.VISIBLE
                }
            }
    }
}