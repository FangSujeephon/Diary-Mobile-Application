package com.example.proj

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.proj.databinding.ActivityDiaryDetailBinding
import com.google.firebase.firestore.FirebaseFirestore

class DiaryDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDiaryDetailBinding
    private val db = FirebaseFirestore.getInstance()
    private lateinit var diaryId: String
    private lateinit var collectionName: String
    private var isPrivate = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding = ActivityDiaryDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        diaryId = intent.getStringExtra("diaryId") ?: run {
            Log.e("DiaryDetailActivity", "Error: Missing diary ID")
            Toast.makeText(this, "Error: Missing diary ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        collectionName = intent.getStringExtra("collectionName") ?: "Diary"
        isPrivate = collectionName == "Diary_Private"

        loadDiaryData()

        binding.back.setOnClickListener {
            val resultIntent = Intent()
            resultIntent.putExtra("isPrivate", isPrivate)
            setResult(RESULT_OK, resultIntent)
            finish()
        }

        binding.btnEdit.setOnClickListener {
            val editIntent = Intent(this, EditActivity::class.java).apply {
                putExtra("diaryId", diaryId)
                putExtra("collectionName", collectionName)
                putExtra("time", binding.timeTxt.text.toString())
                putExtra("title", binding.nameTxt.text.toString())
                putExtra("summary", binding.summaryTxt.text.toString())
                putExtra("emotion", intent.getStringExtra("emotion"))
            }
            startActivity(editIntent)
        }

        binding.btnDelete.setOnClickListener {
            confirmDeleteDiary()
        }
    }

    private fun loadDiaryData() {
        db.collection(collectionName).document(diaryId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val time = document.getString("date") ?: ""
                    val title = document.getString("title") ?: ""
                    val summary = document.getString("summary") ?: ""
                    val emotion = document.getString("emotion") ?: "neutral"

                    binding.timeTxt.text = time
                    binding.nameTxt.text = title
                    binding.summaryTxt.text = summary

                    val emotionResId = when (emotion.lowercase()) {
                        "happy" -> R.drawable.happy
                        "sad" -> R.drawable.sad
                        "angry", "mad" -> R.drawable.mad
                        "love" -> R.drawable.fin
                        "bored", "boring" -> R.drawable.neutral_
                        else -> R.drawable.neutral_
                    }
                    binding.emotionIcon.setImageResource(emotionResId)
                } else {
                    Log.e("DiaryDetailActivity", "Diary not found in Firestore")
                    Toast.makeText(this, "Diary not found!", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .addOnFailureListener {
                Log.e("FirestoreError", "Failed to fetch diary data: ${it.message}")
            }
    }

    private fun confirmDeleteDiary() {
        AlertDialog.Builder(this)
            .setTitle("Delete Diary")
            .setMessage("Are you sure you want to delete this diary?")
            .setPositiveButton("Yes") { _, _ -> deleteDiary() }
            .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }

    private fun deleteDiary() {
        db.collection(collectionName).document(diaryId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Diary deleted successfully", Toast.LENGTH_SHORT).show()

                val resultIntent = Intent()
                resultIntent.putExtra("diaryDeleted", true)
                resultIntent.putExtra("isPrivate", isPrivate)
                resultIntent.putExtra("deletedDiaryId", diaryId)
                setResult(RESULT_OK, resultIntent)

                finish()
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreError", "Failed to delete diary: ${e.message}")
                Toast.makeText(this, "Failed to delete diary", Toast.LENGTH_SHORT).show()
            }
    }

}
