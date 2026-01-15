package com.example.proj

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class Login : AppCompatActivity() {

    private lateinit var edtUser: EditText
    private lateinit var edtPass: EditText
    private lateinit var btnLog: Button
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        edtUser = findViewById(R.id.edt_user)
        edtPass = findViewById(R.id.edt_pass)
        btnLog = findViewById(R.id.btn_log)
        auth = FirebaseAuth.getInstance()

        findViewById<TextView>(R.id.tv_register).setOnClickListener {
            startActivity(Intent(this, Register::class.java))
        }

        btnLog.setOnClickListener {
            val username = edtUser.text.toString().trim()
            val password = edtPass.text.toString().trim()

            when {
                username.isEmpty() || password.isEmpty() ->
                    showToast("Please enter your username and password")
                password.length < 6 ->
                    showToast("Password must be at least 6 characters")
                else ->
                    loginUser(username, password)
            }
        }
    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    showToast("Login Successful!")
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    showToast("Login failed")
                }
            }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

}
