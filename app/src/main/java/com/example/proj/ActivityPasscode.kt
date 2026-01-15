package com.example.proj.ui.passcode

import android.app.AlertDialog
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.proj.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class ActivityPasscode : Fragment() {

    private lateinit var etPasscode: EditText
    private lateinit var btnConfirm: Button
    private lateinit var btnCancel: Button
    private var isPasscodeSet = false // ✅ Track if a passcode exists

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.activity_passcode, container, false)

        etPasscode = view.findViewById(R.id.etPasscode)
        btnConfirm = view.findViewById(R.id.btnConfirm)
        btnCancel = view.findViewById(R.id.btnCancel)

        checkIfPasscodeExists()

        btnConfirm.setOnClickListener {
            val passcode = etPasscode.text.toString().trim()

            if (passcode.length != 4) {
                Toast.makeText(requireContext(), "Passcode must be 4 digits", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (isPasscodeSet) {
                askForOldPasscodeBeforeChanging(passcode)
            } else {
                savePasscodeToFirestore(passcode)
            }
        }

        btnCancel.setOnClickListener {
            findNavController().navigate(R.id.navigation_settings)
        }

        return view
    }

    private fun checkIfPasscodeExists() {
        val userId = auth.currentUser?.uid ?: return
        val diaryRef = db.collection("Diary_Private").document(userId)

        diaryRef.get().addOnSuccessListener { document ->
            isPasscodeSet = document.exists() && document.getString("passcode") != null
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Failed to check passcode", Toast.LENGTH_SHORT).show()
        }
    }

    private fun askForOldPasscodeBeforeChanging(newPasscode: String) {
        val inputOld = EditText(requireContext()).apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            hint = "Enter current passcode"
        }

        val userId = auth.currentUser?.uid ?: return
        val diaryRef = db.collection("Diary_Private").document(userId)

        diaryRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val savedPasscode = document.getString("passcode") ?: return@addOnSuccessListener

                AlertDialog.Builder(requireContext())
                    .setTitle("Confirm Passcode Change")
                    .setMessage("Enter your current passcode to set a new one.")
                    .setView(inputOld)
                    .setPositiveButton("OK") { _, _ ->
                        val enteredOldPasscode = inputOld.text.toString()
                        if (enteredOldPasscode == savedPasscode) {
                            savePasscodeToFirestore(newPasscode) // ✅ Save new passcode
                        } else {
                            Toast.makeText(requireContext(), "Incorrect current passcode!", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .setNegativeButton("Cancel") { _, _ ->
                        findNavController().navigate(R.id.navigation_settings) // ✅ Exit to Settings
                    }
                    .show()
            }
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Failed to load passcode", Toast.LENGTH_SHORT).show()
        }
    }

    private fun savePasscodeToFirestore(passcode: String) {
        val userId = auth.currentUser?.uid ?: return
        val diaryRef = db.collection("Diary_Private").document(userId)

        diaryRef.set(mapOf("passcode" to passcode), SetOptions.merge())
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Passcode set successfully", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.navigation_settings) // ✅ Redirect to Settings
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to save passcode: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
