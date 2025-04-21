package com.app.medifindfinal
import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import com.google.gson.Gson
class AddEmergencyContactActivity : AppCompatActivity() {
    private lateinit var editTextName: EditText
    private lateinit var editTextMobile: EditText
    private lateinit var spinnerCategory: Spinner
    private lateinit var editTextEmail: EditText
    private lateinit var buttonSaveContact: Button
    private lateinit var sharedPreferences: SharedPreferences
    private val gson = Gson() // Initialize Gson here
    private var isEditMode = false
    private lateinit var originalMobile: String
    private lateinit var currentCategory: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_emergency_contact)
        editTextName = findViewById(R.id.editTextName)
        editTextMobile = findViewById(R.id.editTextMobile)
        spinnerCategory = findViewById(R.id.spinnerCategory)
        editTextEmail = findViewById(R.id.editTextEmail)
        buttonSaveContact = findViewById(R.id.buttonSaveContact)
        sharedPreferences = getSharedPreferences("emergency_contacts", Context.MODE_PRIVATE)
        val categories = arrayOf("Personal", "Doctor", "Hospital")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categories)
        spinnerCategory.adapter = adapter
        // Check if we are in edit mode
        isEditMode = intent.getBooleanExtra("isEdit", false)
        if (isEditMode) {
            buttonSaveContact.text = "Update Contact"
            editTextName.setText(intent.getStringExtra("name"))
            editTextMobile.setText(intent.getStringExtra("mobile"))
            originalMobile = intent.getStringExtra("mobile") ?: ""
            currentCategory = intent.getStringExtra("category") ?: "Personal"
            val categoryIndex = categories.indexOf(currentCategory)
            if (categoryIndex != -1) {
                spinnerCategory.setSelection(categoryIndex)
            }
            editTextEmail.setText(intent.getStringExtra("email"))
        } else {
            currentCategory = intent.getStringExtra("category") ?: "Personal"
            val categoryIndex = categories.indexOf(currentCategory)
            if (categoryIndex != -1) {
                spinnerCategory.setSelection(categoryIndex)
            }
        }
        buttonSaveContact.setOnClickListener {
            saveContact()
        }
    }
    private fun saveContact() {
        val name = editTextName.text.toString().trim()
        val mobile = editTextMobile.text.toString().trim()
        val category = spinnerCategory.selectedItem.toString()
        val email = editTextEmail.text.toString().trim()
        val contactToSave = EmergencyContact(name, mobile, category, if (email.isNotEmpty()) email else null)
        if (name.isEmpty() || mobile.isEmpty()) {
            Toast.makeText(this, "Name and Mobile are required", Toast.LENGTH_SHORT).show()
            return
        }
        val existingContacts = getContacts(category)
        if (isEditMode) {
            // Find and update the existing contact
            val index = existingContacts.indexOfFirst { it.mobile == originalMobile && it.category == currentCategory }
            if (index != -1) {
                existingContacts[index] = contactToSave
            }
        } else {
            existingContacts.add(contactToSave)
        }
        saveContacts(category, existingContacts)
        setResult(RESULT_OK)
        finish() // Go back to the emergency contact list
    }
    private fun getContacts(category: String): MutableList<EmergencyContact> {
        val json = sharedPreferences.getString(category, null)
        return if (json != null) {
            val type = object : com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken<MutableList<EmergencyContact>>() {}.type
            gson.fromJson(json, type)
        } else {
            mutableListOf()
        }
    }
    private fun saveContacts(category: String, contacts: List<EmergencyContact>) {
        val json = gson.toJson(contacts)
        sharedPreferences.edit().putString(category, json).apply()
    }
}