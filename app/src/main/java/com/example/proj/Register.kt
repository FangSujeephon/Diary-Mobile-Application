package com.example.proj

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest

class Register : AppCompatActivity() {

    private lateinit var edtUsername: EditText
    private lateinit var edtEmail: EditText
    private lateinit var edtPassword: EditText
    private lateinit var edtConfirmPassword: EditText
    private lateinit var checkTerms: CheckBox
    private lateinit var btnCreateAccount: Button
    private lateinit var tvLogin: TextView

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()

        edtUsername = findViewById(R.id.btn_username)
        edtEmail = findViewById(R.id.btn_email)
        edtPassword = findViewById(R.id.btn_password)
        edtConfirmPassword = findViewById(R.id.btn_confirm)
        checkTerms = findViewById(R.id.checkBox)
        btnCreateAccount = findViewById(R.id.btn_create)
        tvLogin = findViewById(R.id.tv_login)

        tvLogin.setOnClickListener {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            finish()
        }

        btnCreateAccount.setOnClickListener {
            registerUser()
        }
    }

    private fun registerUser() {
        val username = edtUsername.text.toString().trim()
        val email = edtEmail.text.toString().trim()
        val password = edtPassword.text.toString().trim()
        val confirmPassword = edtConfirmPassword.text.toString().trim()

        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (!(email.endsWith("@gmail.com") || email.endsWith("@kkumail.com"))) {
            Toast.makeText(
                this,
                "Email must end with @gmail.com or @kkumail.com",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (password.length < 6) {
            Toast.makeText(this, "Password must be at least 6 characters long", Toast.LENGTH_SHORT)
                .show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }

        if (!checkTerms.isChecked) {
            Toast.makeText(this, "You must accept the Terms and Conditions", Toast.LENGTH_SHORT)
                .show()
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // กำหนด displayName หลังจากลงทะเบียนสำเร็จ
                    val user = auth.currentUser
                    user?.updateProfile(
                        userProfileChangeRequest {
                            displayName = username // ตั้งชื่อผู้ใช้
                        }
                    )

                    Toast.makeText(this, "Register Successful!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, Login::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Register failed", Toast.LENGTH_SHORT).show()
                }
            }
    }
}