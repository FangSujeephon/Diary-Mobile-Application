package com.example.proj.ui.notifications

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.proj.StartHome
import com.example.proj.databinding.FragmentNotificationsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import android.app.AlertDialog
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        auth = FirebaseAuth.getInstance()
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val currentUser = auth.currentUser
        if (currentUser != null) {
            binding.textView5.text = currentUser.displayName ?: "No Name"
            binding.textView7.text = currentUser.email ?: "No Email"
        }

        binding.back.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.tvDelete.setOnClickListener {
            confirmDeleteAccount()
        }

        return root
    }

    private fun confirmDeleteAccount() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Delete Account")
        builder.setMessage("Are you sure you want to delete your account? All your data will be lost forever.")

        builder.setPositiveButton("Yes") { _, _ ->
            deleteUserData()
        }

        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }
        builder.create().show()
    }

    private fun deleteUserData() {
        val user: FirebaseUser? = auth.currentUser
        val userId = user?.uid ?: return

        db.collection("Diary")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { snapshots ->
                for (document in snapshots) {
                    db.collection("Diary").document(document.id).delete()
                }
                deleteAccountFromFirebase()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to delete data", Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteAccountFromFirebase() {
        val user: FirebaseUser? = auth.currentUser
        if (user != null) {
            val email = user.email
            if (email != null && user.providerData.any { it.providerId == EmailAuthProvider.PROVIDER_ID }) {
                reAuthenticateUser {
                    user.delete().addOnCompleteListener { handleDeleteAccount(it.isSuccessful) }
                }
            } else {
                user.delete().addOnCompleteListener { handleDeleteAccount(it.isSuccessful) }
            }
        }
    }

    private fun handleDeleteAccount(success: Boolean) {
        if (success) {
            Toast.makeText(requireContext(), "Account deleted successfully", Toast.LENGTH_SHORT).show()
            val intent = Intent(requireContext(), StartHome::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        } else {
            Toast.makeText(requireContext(), "Failed to delete account. Please try again.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun reAuthenticateUser(onReAuthenticated: () -> Unit) {
        val user = auth.currentUser ?: return
        val email = user.email ?: return

        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Re-authenticate")
        builder.setMessage("Please enter your password to delete your account.")

        val input = android.widget.EditText(requireContext())
        input.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        builder.setView(input)

        builder.setPositiveButton("Confirm") { _, _ ->
            val password = input.text.toString()
            if (password.isNotEmpty()) {
                val credential = EmailAuthProvider.getCredential(email, password)
                user.reauthenticate(credential)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            onReAuthenticated()
                        } else {
                            Toast.makeText(requireContext(), "Re-authentication failed.", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(requireContext(), "Password cannot be empty.", Toast.LENGTH_SHORT).show()
            }
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        builder.create().show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
