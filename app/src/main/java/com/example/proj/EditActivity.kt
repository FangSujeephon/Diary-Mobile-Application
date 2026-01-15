package com.example.proj

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.proj.databinding.ActivityEditBinding
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class EditActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditBinding
    private val db = FirebaseFirestore.getInstance()
    private lateinit var diaryId: String
    private lateinit var collectionName: String
    private var selectedEmotion: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding = ActivityEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        diaryId = intent.getStringExtra("diaryId") ?: ""
        collectionName = intent.getStringExtra("collectionName") ?: "Diary"
        val time = intent.getStringExtra("time") ?: ""
        val title = intent.getStringExtra("title") ?: ""
        val summary = intent.getStringExtra("summary") ?: ""
        val emotion = intent.getStringExtra("emotion") ?: "neutral"

        binding.edtTitle.setText(title)
        binding.edtContent.setText(summary)
        binding.btnDatePicker.text = time
        selectedEmotion = emotion

        updateSelectedEmotion(emotion)

        binding.btnDatePicker.setOnClickListener {
            showDatePickerDialog()
        }

        binding.btnSave.setOnClickListener {
            saveUpdatedDiary()
        }

        binding.emojiHappy.setOnClickListener { setSelectedEmotion("Happy", Color.YELLOW) }
        binding.emojiSad.setOnClickListener { setSelectedEmotion("Sad", Color.BLUE) }
        binding.emojiMad.setOnClickListener { setSelectedEmotion("Angry", Color.RED) }
        binding.emojiLove.setOnClickListener { setSelectedEmotion("Love", Color.MAGENTA) }
        binding.emojiBoring.setOnClickListener { setSelectedEmotion("Bored", Color.GRAY) }

        binding.back.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            val selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
            binding.btnDatePicker.text = selectedDate
        }, year, month, day).show()
    }

    private fun saveUpdatedDiary() {
        val updatedTitle = binding.edtTitle.text.toString().trim()
        val updatedTime = binding.btnDatePicker.text.toString().trim()
        val updatedSummary = binding.edtContent.text.toString().trim()

        if (updatedTitle.isEmpty() || updatedTime.isEmpty() || updatedSummary.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val updatedData = mapOf(
            "title" to updatedTitle,
            "date" to updatedTime,
            "summary" to updatedSummary,
            "emotion" to selectedEmotion
        )

        db.collection(collectionName).document(diaryId)
            .update(updatedData)
            .addOnSuccessListener {
                Toast.makeText(this, "Diary updated successfully", Toast.LENGTH_SHORT).show()

                val resultIntent = Intent().apply {
                    putExtra("updatedTime", updatedTime)
                    putExtra("updatedTitle", updatedTitle)
                    putExtra("updatedSummary", updatedSummary)
                    putExtra("updatedEmotion", selectedEmotion)
                }
                setResult(RESULT_OK, resultIntent)

                val intentHome = Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                }
                startActivity(intentHome)
                finish() // ปิด EditActivity
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to update diary", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setSelectedEmotion(emotion: String, color: Int) {
        selectedEmotion = emotion
        updateSelectedEmotion(emotion, color)
    }

    private fun updateSelectedEmotion(emotion: String, color: Int = Color.LTGRAY) {
        binding.emojiHappy.background = null
        binding.emojiSad.background = null
        binding.emojiMad.background = null
        binding.emojiLove.background = null
        binding.emojiBoring.background = null

        val selectedView = when (emotion) {
            "Happy" -> binding.emojiHappy
            "Sad" -> binding.emojiSad
            "Angry" -> binding.emojiMad
            "Love" -> binding.emojiLove
            "Bored" -> binding.emojiBoring
            else -> null
        }

        selectedView?.background = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(color)
        }
    }
}
