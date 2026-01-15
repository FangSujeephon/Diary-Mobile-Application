package com.example.proj

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MyAdapter(private val diaryList: List<Users>) :
    RecyclerView.Adapter<MyAdapter.ViewHolder>() {

    private var onItemClickListener: ((Users) -> Unit)? = null

    fun setOnItemClickListener(listener: (Users) -> Unit) {
        onItemClickListener = listener
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val timeTxt: TextView = view.findViewById(R.id.timeTxt)
        val nameTxt: TextView = view.findViewById(R.id.nameTxt)
        val summaryTxt: TextView = view.findViewById(R.id.summaryTxt)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.model, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val diary = diaryList[position]

        holder.timeTxt.text = diary.time
        holder.nameTxt.text = diary.title
        holder.summaryTxt.text = diary.summary

        holder.itemView.setOnClickListener {
            onItemClickListener?.invoke(diary)
        }
    }

    override fun getItemCount() = diaryList.size
}
