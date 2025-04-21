package com.app.medifindfinal

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.telephony.SmsManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken
import com.google.gson.Gson

class EmergencyContactActivity : AppCompatActivity() {

    private lateinit var personalContactsLayout: LinearLayout
    private lateinit var doctorContactsLayout: LinearLayout
    private lateinit var hospitalContactsLayout: LinearLayout
    private lateinit var addPersonalContactButton: Button
    private lateinit var addDoctorContactButton: Button
    private lateinit var addHospitalContactButton: Button
    private lateinit var sendAlertButton: Button
    private lateinit var sharedPreferences: SharedPreferences
    private val gson = Gson()
    private val CONTACT_REQUEST_CODE = 100
    private val EDIT_CONTACT_REQUEST_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emergency_contact)

        personalContactsLayout = findViewById(R.id.personalContactsLayout)
        doctorContactsLayout = findViewById(R.id.doctorContactsLayout)
        hospitalContactsLayout = findViewById(R.id.hospitalContactsLayout)
        addPersonalContactButton = findViewById(R.id.addPersonalContactButton)
        addDoctorContactButton = findViewById(R.id.addDoctorContactButton)
        addHospitalContactButton = findViewById(R.id.addHospitalContactButton)
        sendAlertButton = findViewById(R.id.sendAlertButton)
        sharedPreferences = getSharedPreferences("emergency_contacts", Context.MODE_PRIVATE)

        loadContacts()

        addPersonalContactButton.setOnClickListener {
            startAddContactActivity("Personal")
        }
        addDoctorContactButton.setOnClickListener {
            startAddContactActivity("Doctor")
        }
        addHospitalContactButton.setOnClickListener {
            startAddContactActivity("Hospital")
        }

        sendAlertButton.setOnClickListener {
            sendEmergencyAlert()
        }
    }

    override fun onResume() {
        super.onResume()
        loadContacts() // Reload contacts when the activity resumes
    }

    private fun startAddContactActivity(category: String) {
        val intent = Intent(this, AddEmergencyContactActivity::class.java)
        intent.putExtra("category", category)
        startActivityForResult(intent, CONTACT_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (requestCode == CONTACT_REQUEST_CODE || requestCode == EDIT_CONTACT_REQUEST_CODE) {
                loadContacts() // Reload contacts after adding or editing
            }
        }
    }

    private fun loadContacts() {
        personalContactsLayout.removeAllViews()
        doctorContactsLayout.removeAllViews()
        hospitalContactsLayout.removeAllViews()

        loadContactsForCategory("Personal", personalContactsLayout)
        loadContactsForCategory("Doctor", doctorContactsLayout)
        loadContactsForCategory("Hospital", hospitalContactsLayout)
    }

    private fun loadContactsForCategory(category: String, layout: LinearLayout) {
        val json = sharedPreferences.getString(category, null)
        val type = object : TypeToken<MutableList<EmergencyContact>>() {}.type
        val contacts: MutableList<EmergencyContact> = gson.fromJson(json, type) ?: mutableListOf()

        for (contact in contacts) {
            addContactView(layout, contact, category, contacts)
        }
    }

    private fun addContactView(layout: LinearLayout, contact: EmergencyContact, category: String, allContactsInCategory: MutableList<EmergencyContact>) {
        val contactView = LayoutInflater.from(this).inflate(R.layout.item_emergency_contact, layout, false)
        val nameTextView = contactView.findViewById<TextView>(R.id.contactNameTextView)
        val phoneTextView = contactView.findViewById<TextView>(R.id.contactPhoneTextView)
        val callButton = contactView.findViewById<Button>(R.id.callButton)

        nameTextView.text = contact.name
        phoneTextView.text = contact.mobile

        callButton.setOnClickListener {
            val phoneNumber = contact.mobile
            val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber"))
            startActivity(dialIntent)
        }

        contactView.setOnLongClickListener {
            showContactOptionsDialog(contact, category, allContactsInCategory)
            true // Consume the long click event
        }

        layout.addView(contactView)
    }

    private fun showContactOptionsDialog(contact: EmergencyContact, category: String, contactsInCategory: MutableList<EmergencyContact>) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Choose an action")
            .setItems(arrayOf("Delete", "Edit")) { dialog, which ->
                when (which) {
                    0 -> deleteContact(contact, category, contactsInCategory)
                    1 -> editContact(contact, category)
                }
            }
        builder.create().show()
    }

    private fun deleteContact(contactToDelete: EmergencyContact, category: String, contactsInCategory: MutableList<EmergencyContact>) {
        contactsInCategory.remove(contactToDelete)
        saveContacts(category, contactsInCategory)
        loadContacts() // Refresh the UI
    }

    private fun editContact(contactToEdit: EmergencyContact, category: String) {
        val intent = Intent(this, AddEmergencyContactActivity::class.java)
        intent.putExtra("isEdit", true)
        intent.putExtra("category", category)
        intent.putExtra("name", contactToEdit.name)
        intent.putExtra("mobile", contactToEdit.mobile)
        intent.putExtra("email", contactToEdit.email)
        startActivityForResult(intent, EDIT_CONTACT_REQUEST_CODE)
    }

    private fun saveContacts(category: String, contacts: List<EmergencyContact>) {
        val json = gson.toJson(contacts)
        sharedPreferences.edit().putString(category, json).apply()
    }

    private fun sendEmergencyAlert() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.SEND_SMS), 123)
            return
        }

        val personalContacts = getContactNumbers("Personal")
        val doctorContacts = getContactNumbers("Doctor")
        val hospitalContacts = getContactNumbers("Hospital")

        val allNumbers = personalContacts + doctorContacts + hospitalContacts

        if (allNumbers.isEmpty()) {
            Toast.makeText(this, "No emergency contacts saved", Toast.LENGTH_SHORT).show()
            return
        }

        val message = "This is an emergency. Please contact me."

        try {
            val smsManager = SmsManager.getDefault()
            for (number in allNumbers) {
                smsManager.sendTextMessage(number, null, message, null, null)
            }
            Toast.makeText(this, "Emergency alert sent to all contacts", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error sending alert: ${e.message}", Toast.LENGTH_LONG).show()
            Log.e("EmergencyAlert", "Error sending SMS", e)
        }
    }

    private fun getContactNumbers(category: String): List<String> {
        val json = sharedPreferences.getString(category, null)
        val type = object : TypeToken<MutableList<EmergencyContact>>() {}.type
        val contacts: List<EmergencyContact> = gson.fromJson(json, type) ?: emptyList()
        return contacts.map { it.mobile }
    }
}