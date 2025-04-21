package com.app.medifindfinal


import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText

class FeedbackActivity : AppCompatActivity() {

    private lateinit var editTextFeedback: TextInputEditText
    private lateinit var editTextEmail: TextInputEditText
    private lateinit var buttonSubmitFeedback: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feedback)

        // Optional: Add an Up button to the action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Feedback"

        editTextFeedback = findViewById(R.id.editTextFeedback)
        editTextEmail = findViewById(R.id.editTextEmail)
        buttonSubmitFeedback = findViewById(R.id.buttonSubmitFeedback)

        buttonSubmitFeedback.setOnClickListener {
            submitFeedback()
        }
    }

    private fun submitFeedback() {
        val feedback = editTextFeedback.text.toString().trim()
        val email = editTextEmail.text.toString().trim()

        if (feedback.isEmpty()) {
            editTextFeedback.error = "Feedback cannot be empty"
            return
        }

        // Here you can implement how the feedback is sent.
        // For simplicity, we'll use an email intent.
        // You could also save to a database (like Firebase Firestore)
        // or send to a backend service.

        val recipientEmail = "shadowmorbius@gmail.com" // **Replace with the email address where you want to receive feedback**
        val subject = "Feedback from MediFind App"
        val body = "Feedback: $feedback\n\nUser Email: ${if (email.isNotEmpty()) email else "Not provided"}"

        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:") // Only email apps should handle this
            putExtra(Intent.EXTRA_EMAIL, arrayOf(recipientEmail))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }

        // Check if there's an email client available to handle the intent
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            Toast.makeText(this, "No email client installed.", Toast.LENGTH_SHORT).show()
        }

        // Optional: Clear the form after submitting
        editTextFeedback.text?.clear()
        editTextEmail.text?.clear()
    }

    // Handle the Up button click
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}