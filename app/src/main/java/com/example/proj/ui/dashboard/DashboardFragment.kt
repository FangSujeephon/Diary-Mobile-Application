package com.example.proj.ui.dashboard

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.proj.databinding.FragmentDashboardBinding
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.*

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var pieChart: PieChart
    private val db = FirebaseFirestore.getInstance()
    private var currentCategory = "Primary"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        pieChart = binding.pieChart
        setupPieChart()

        // Switch Between Primary and Private
        binding.switchCategory.setOnCheckedChangeListener { _, isChecked ->
            currentCategory = if (isChecked) "Private" else "Primary"
            loadChartData()
        }

        // Load initial chart data
        loadChartData()

        return root
    }

    private fun setupPieChart() {
        pieChart.apply {
            description.isEnabled = false
            isDrawHoleEnabled = true
            setHoleColor(Color.WHITE)
            setUsePercentValues(true)
            setEntryLabelTextSize(12f)
            setEntryLabelColor(Color.BLACK)
            legend.isEnabled = true
        }
    }

    private fun loadChartData() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val collectionName = if (currentCategory == "Private") "Diary_Private" else "Diary"

        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -30)
        val last30DaysTimestamp = calendar.timeInMillis

        db.collection(collectionName)
            .whereEqualTo("userId", userId)
            .whereGreaterThanOrEqualTo("timestamp", last30DaysTimestamp)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshots ->
                val emotionMap = hashMapOf(
                    "Happy" to 0,
                    "Sad" to 0,
                    "Bored" to 0,
                    "Love" to 0,
                    "Angry" to 0
                )

                snapshots.forEach { document ->
                    val emotion = document.getString("emotion") ?: "Neutral"
                    if (emotionMap.containsKey(emotion)) {
                        emotionMap[emotion] = emotionMap[emotion]!! + 1
                    }
                }

                updatePieChart(emotionMap)
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreError", "Error getting documents: ${e.message}")
                showNoData()
            }
    }

    private fun updatePieChart(emotionMap: Map<String, Int>) {
        val entries = ArrayList<PieEntry>()
        val colors = ArrayList<Int>()

        val total = emotionMap.values.sum()
        if (total == 0) {
            showNoData()
            return
        }

        val colorMap = mapOf(
            "Happy" to Color.YELLOW,
            "Sad" to Color.BLUE,
            "Bored" to Color.GRAY,
            "Love" to Color.MAGENTA,
            "Angry" to Color.RED
        )

        var happyPercentage = 0f
        var sadPercentage = 0f
        var boredPercentage = 0f
        var lovePercentage = 0f
        var angryPercentage = 0f

        for ((emotion, count) in emotionMap) {
            if (count > 0) {
                val percentage = (count.toFloat() / total) * 100f
                entries.add(PieEntry(percentage, emotion))
                colors.add(colorMap[emotion] ?: Color.LTGRAY)

                when (emotion) {
                    "Happy" -> happyPercentage = percentage
                    "Sad" -> sadPercentage = percentage
                    "Bored" -> boredPercentage = percentage
                    "Love" -> lovePercentage = percentage
                    "Angry" -> angryPercentage = percentage
                }
            }
        }

        val dataSet = PieDataSet(entries, "Mood Overview")
        dataSet.colors = colors
        val data = PieData(dataSet)
        data.setValueTextSize(14f)

        pieChart.data = data
        pieChart.invalidate()

        binding.happyPercentage.text = "😊 Happy                                     ${happyPercentage.toInt()}%"
        binding.sadPercentage.text = "😢 Sad                                       ${sadPercentage.toInt()}%"
        binding.boredPercentage.text = "😴 Bored                                     ${boredPercentage.toInt()}%"
        binding.lovePercentage.text = "😍 Love                                      ${lovePercentage.toInt()}%"
        binding.angryPercentage.text = "😡 Angry                                     ${angryPercentage.toInt()}%"
    }

    private fun showNoData() {
        val entries = arrayListOf(PieEntry(100f, "No Data"))
        val dataSet = PieDataSet(entries, "No Data Available")
        dataSet.colors = listOf(Color.LTGRAY)
        dataSet.valueTextSize = 14f
        dataSet.valueTextColor = Color.BLACK

        pieChart.data = PieData(dataSet)
        pieChart.invalidate()

        // Reset UI percentages
        binding.happyPercentage.text = "😊 Happy: 0%"
        binding.sadPercentage.text = "😢 Sad: 0%"
        binding.lovePercentage.text = "😍 Love: 0%"
        binding.angryPercentage.text = "😡 Angry: 0%"
        binding.boredPercentage.text = "😐 Bored: 0%"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
