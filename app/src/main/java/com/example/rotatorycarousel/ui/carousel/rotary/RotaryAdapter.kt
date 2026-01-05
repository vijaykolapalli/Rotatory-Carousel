package com.example.rotatorycarousel.ui.carousel.rotary

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.rotatorycarousel.R

class RotaryAdapter(private val items: List<String>) :
    RecyclerView.Adapter<RotaryAdapter.Holder>() {

    class Holder(view: View) : RecyclerView.ViewHolder(view) {
        val text: TextView = view.findViewById(R.id.text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder =
        Holder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_rotary, parent, false)
        )

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.text.text = items[position]
    }

    override fun getItemCount() = items.size
}

