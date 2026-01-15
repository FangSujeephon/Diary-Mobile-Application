package com.example.proj.ui.home

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.proj.DiaryDetailActivity
import com.example.proj.Users
import com.example.proj.MyAdapter
import com.example.proj.databinding.FragmentHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()
    private val diaryList = mutableListOf<Users>()
    private lateinit var myAdapter: MyAdapter
    private var isPrivateAccessed = false

    private val sharedPreferences by lazy {
        requireContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        myAdapter = MyAdapter(diaryList)
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = myAdapter
        }

        myAdapter.setOnItemClickListener { diary ->
            val intent = Intent(requireContext(), DiaryDetailActivity::class.java).apply {
                putExtra("diaryId", diary.diaryId)
                putExtra("collectionName", if (isPrivateAccessed) "Diary_Private" else "Diary")
                putExtra("time", diary.time)
                putExtra("title", diary.title)
                putExtra("summary", diary.summary)
                putExtra("emotion", diary.emotion)
            }
            diaryDetailLauncher.launch(intent)
        }

        val isFirstLogin = sharedPreferences.getBoolean("is_first_login", true)

        if (isFirstLogin) {
            isPrivateAccessed = false
            sharedPreferences.edit()
                .putBoolean("is_first_login", false)
                .putBoolean("last_selected_private", false)
                .apply()
        }

        isPrivateAccessed = sharedPreferences.getBoolean("last_selected_private", false)

        if (isPrivateAccessed) {
            showPrivateContent()
        } else {
            showPrimaryContent()
        }

        binding.btnPrimary.setOnClickListener {
            if (!isPrivateAccessed) return@setOnClickListener
            isPrivateAccessed = false
            saveLastSelectedMode()
            showPrimaryContent()
        }

        binding.btnPrivate.setOnClickListener {
            if (isPrivateAccessed) {
                Toast.makeText(
                    requireContext(),
                    "You are already in private mode!",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                showPasswordDialog()
            }
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        val lastSelected = sharedPreferences.getBoolean("last_selected_private", false)
        if (lastSelected != isPrivateAccessed) {
            isPrivateAccessed = lastSelected
            if (isPrivateAccessed) {
                showPrivateContent()
            } else {
                showPrimaryContent()
            }
        }
    }

    private val diaryDetailLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                val data = result.data
                val diaryDeleted = data?.getBooleanExtra("diaryDeleted", false) ?: false
                val isPrivateFromDetail = data?.getBooleanExtra("isPrivate", false) ?: false
                val deletedDiaryId = data?.getStringExtra("deletedDiaryId") ?: ""

                isPrivateAccessed = isPrivateFromDetail
                saveLastSelectedMode()

                if (diaryDeleted) {
                    removeDiaryFromList(deletedDiaryId)
                }
            }
        }

    private fun loadDiaryData() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val collectionName = if (isPrivateAccessed) "Diary_Private" else "Diary"

        db.collection(collectionName)
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshots ->
                diaryList.clear()
                for (document in snapshots) {
                    val diaryId = document.id
                    val date = document.getString("date") ?: ""
                    val title = document.getString("title") ?: ""
                    val summary = document.getString("summary") ?: ""
                    val emotion = document.getString("emotion") ?: "neutral"

                    diaryList.add(Users(diaryId, date, title, summary, emotion))
                }
                myAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreError", "Error getting documents: ", e)
            }
    }

    private fun showPrivateContent() {
        binding.privateContent.visibility = View.VISIBLE
        binding.primaryContent.visibility = View.GONE
        binding.recyclerView.visibility = View.VISIBLE
        saveLastSelectedMode()
        loadDiaryData()
    }

    private fun showPrimaryContent() {
        binding.privateContent.visibility = View.GONE
        binding.primaryContent.visibility = View.VISIBLE
        binding.recyclerView.visibility = View.VISIBLE
        saveLastSelectedMode()
        loadDiaryData()
    }

    private fun showPasswordDialog() {
        val input = EditText(requireContext())
        input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val userRef = db.collection("Diary_Private").document(userId)

        userRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val savedPasscode = document.getString("passcode")
                if (!savedPasscode.isNullOrEmpty()) {
                    // ✅ ถ้ามีรหัส ให้กรอกเพื่อเข้า Private Mode
                    AlertDialog.Builder(requireContext())
                        .setTitle("Enter Passcode")
                        .setView(input)
                        .setPositiveButton("OK") { _, _ ->
                            val enteredPasscode = input.text.toString()
                            if (enteredPasscode == savedPasscode) {
                                isPrivateAccessed = true
                                showPrivateContent()
                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    "Incorrect passcode!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                } else {
                    AlertDialog.Builder(requireContext())
                        .setTitle("No Passcode Set")
                        .setMessage("Please set your passcode first.")
                        .setPositiveButton("OK", null)
                        .show()
                }
            } else {
                AlertDialog.Builder(requireContext())
                    .setTitle("No Passcode Set")
                    .setMessage("Please set your passcode first.")
                    .setPositiveButton("OK", null)
                    .show()
            }
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Failed to load passcode", Toast.LENGTH_SHORT).show()
        }
    }


    private fun removeDiaryFromList(diaryId: String) {
        diaryList.removeAll { it.diaryId == diaryId }
        myAdapter.notifyDataSetChanged()
    }

    private fun saveLastSelectedMode(isLogin: Boolean = false) {
        val editor = sharedPreferences.edit()
        if (isLogin) {
            editor.putBoolean("last_selected_private", false)
        } else {
            editor.putBoolean("last_selected_private", isPrivateAccessed)
        }
        editor.apply()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
