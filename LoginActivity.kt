package com.app.medifindfinal
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var googleLoginButton: com.google.android.gms.common.SignInButton
    private lateinit var signupPromptTextView: TextView
    private lateinit var errorTextView: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        auth = FirebaseAuth.getInstance()
        //Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("35133857533-1l00bre3f0au13ua5j68a5i0bc9cdlka.apps.googleusercontent.com") // Replace with your web client ID from google-services.json
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        // Initialize UI elements
        emailEditText = findViewById(R.id.editTextLoginEmail)
        passwordEditText = findViewById(R.id.editTextLoginPassword)
        loginButton = findViewById(R.id.buttonLoginEmailPassword)
        googleLoginButton = findViewById(R.id.buttonLoginGoogle)
        signupPromptTextView = findViewById(R.id.textViewLoginSignupPrompt)
        errorTextView = findViewById(R.id.textViewLoginError)
        loginButton.setOnClickListener {
            loginWithEmailPassword()
        }
        googleLoginButton.setOnClickListener {
            signInWithGoogle()
        }
        signupPromptTextView.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }
    private fun loginWithEmailPassword() {
        val email = emailEditText.text.toString()
        val password = passwordEditText.text.toString()
        if (android.text.TextUtils.isEmpty(email) || android.text.TextUtils.isEmpty(password)) {
            errorTextView.text = "Please enter email and password."
            errorTextView.visibility = View.VISIBLE
            return
        }
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Login success, update UI with the signed-in user's information
                    Toast.makeText(this, "Login Successful.", Toast.LENGTH_SHORT).show()
                    navigateToDashboard() // Or navigate to Appointment Activity directly if that was the flow
                } else {
                    // If sign in fails, display a message to the user.
                    errorTextView.text = "Authentication failed: ${task.exception?.message}"
                    errorTextView.visibility = View.VISIBLE
                }
            }
    }
    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Toast.makeText(this, "Google sign in failed: ${e.message}", Toast.LENGTH_SHORT).show()
                errorTextView.text = "Google sign in failed: ${e.message}"
                errorTextView.visibility = View.VISIBLE
            }
        }
    }
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Toast.makeText(this, "Google Login Successful.", Toast.LENGTH_SHORT).show()
                    navigateToDashboard() // Or navigate to Appointment Activity
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(this, "Firebase Google Authentication failed.", Toast.LENGTH_SHORT).show()
                    errorTextView.text = "Firebase Google Authentication failed: ${task.exception?.message}"
                    errorTextView.visibility = View.VISIBLE
                }
            }
    }
    private fun navigateToDashboard() {
        val intent = Intent(this, DashboardActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK // To prevent going back to LoginActivity
        startActivity(intent)
        finish() // Optional, if you want to close LoginActivity completely
    }
    companion object {
        private const val RC_SIGN_IN = 123
    }
}