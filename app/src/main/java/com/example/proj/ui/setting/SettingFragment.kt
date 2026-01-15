package com.example.proj.ui.setting

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.proj.R
import com.google.firebase.auth.FirebaseAuth

class SettingFragment : Fragment() {

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.activity_setting, container, false)
        auth = FirebaseAuth.getInstance()

        val tvAccount = view.findViewById<TextView>(R.id.tvAccount)
        val tvLogout = view.findViewById<TextView>(R.id.tv_logout)
        val tvPasscode = view.findViewById<TextView>(R.id.tvPasscode)
        val tvTerms = view.findViewById<TextView>(R.id.tvTerm)

        tvTerms.setOnClickListener {
            findNavController().navigate(R.id.navigation_terms)
        }

        tvPasscode.setOnClickListener {
            findNavController().navigate(R.id.navigation_passcode)
        }

        tvAccount.setOnClickListener {
            findNavController().navigate(R.id.navigation_notifications)
        }

        tvLogout.setOnClickListener {
            showLogoutDialog()
        }

        return view
    }

    private fun showLogoutDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Logout")
        builder.setMessage("Do you want to log out?")

        builder.setPositiveButton("Yes") { _, _ ->
            Toast.makeText(requireContext(), "Logged out successfully!", Toast.LENGTH_SHORT).show()
            auth.signOut()

            val intent = Intent(requireContext(), com.example.proj.StartHome::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }

        builder.create().show()
    }
}
