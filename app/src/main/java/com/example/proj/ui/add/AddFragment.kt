package com.example.proj.ui.add

import android.app.DatePickerDialog
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.proj.R
import com.example.proj.databinding.FragmentAddBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

class AddFragment : Fragment() {

    private var _binding: FragmentAddBinding? = null
    private val binding get() = _binding!!
    private var selectedDate: String = ""
    private val db = FirebaseFirestore.getInstance()
    private var selectedEmotion: String = ""
    private var entryType: String = "Primary"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddBinding.inflate(inflater, container, false)

        binding.apply {

            binding.radioGroup.setOnCheckedChangeListener { _, checkedId ->
                entryType = when (checkedId) {
                    R.id.radioPrimary -> "Diary"
                    R.id.radioPrivate -> "Diary_Private"
                    else -> "Diary"
                }
            }

            btnSave.setOnClickListener { saveDiaryEntry() }
            btnDatePicker.setOnClickListener { showDatePickerDialog() }
            emojiHappy.setOnClickListener { setSelectedView(it, Color.YELLOW, "Always be Happy!", "Happy") }
            emojiSad.setOnClickListener { setSelectedView(it, Color.BLUE, "Don't be sad, good days will come.", "Sad") }
            emojiMad.setOnClickListener { setSelectedView(it, Color.RED, "Calm down, everything will be fine.", "Angry") }
            emojiLove.setOnClickListener { setSelectedView(it, Color.MAGENTA, "Wow so lovely!", "Love") }
            emojiBoring.setOnClickListener { setSelectedView(it, Color.GRAY, "Look around, many things are waiting for you.", "Bored") }
        }

        return binding.root
    }

    private fun saveDiaryEntry() {
        val title = binding.edtTitle.text.toString().trim()
        val content = binding.edtContent.text.toString().trim()
        val collectionName = if (binding.radioPrivate.isChecked) "Diary_Private" else "Diary"

        when {
            title.isEmpty() -> showToast("Please enter a title.")
            selectedDate.isEmpty() -> showToast("Please select a date.")
            content.isEmpty() -> showToast("Please enter a summary or content.")
            selectedEmotion.isEmpty() -> showToast("Please select an emotion.")
            else -> {
                val user = FirebaseAuth.getInstance().currentUser
                val userId = user?.uid ?: return

                val entryType = if (binding.radioPrimary.isChecked) {
                    "Private"
                } else {
                    "Primary" // ถ้าไม่ได้เลือก Private ให้เป็น Primary
                }

                val diary = hashMapOf(
                    "title" to title,
                    "date" to selectedDate,
                    "timestamp" to System.currentTimeMillis(),
                    "summary" to content,
                    "emotion" to selectedEmotion,
                    "userId" to userId,
                    "entryType" to entryType
                )

                val collectionName = if (binding.radioPrimary.isChecked) "Diary" else "Diary_Private"

                db.collection(collectionName)
                    .add(diary)
                    .addOnSuccessListener {
                        showToast("Saved successfully!")
                        parentFragmentManager.setFragmentResult("diary_saved", Bundle())
                        findNavController().navigateUp()
                    }
                    .addOnFailureListener {
                        showToast("Save failed!")
                    }
            }
        }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
            selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
            binding.btnDatePicker.text = "Date: $selectedDate"
        }, year, month, day).apply {
            setOnCancelListener { showToast("No date selected.") }
            show()
        }
    }

    private fun setSelectedView(view: View, color: Int, message: String, emotion: String) {
        binding.emojiHappy.background = null
        binding.emojiSad.background = null
        binding.emojiMad.background = null
        binding.emojiLove.background = null
        binding.emojiBoring.background = null

        view.background = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(color)
        }

        selectedEmotion = emotion
        showToast(message)
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}