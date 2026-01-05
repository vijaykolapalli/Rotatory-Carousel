package com.example.rotatorycarousel.circular

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.rotatorycarousel.R
import com.example.rotatorycarousel.ui.carousel.rotary.RotaryAdapter

class CircularDemoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_circular_demo)

        val recyclerView = findViewById<RecyclerView>(R.id.circularRecyclerView)
        recyclerView.apply {
            setHasFixedSize(true)
            clipToPadding = false
            clipChildren = false
            layoutManager = CircularLayoutManager(this@CircularDemoActivity)
            adapter = RotaryAdapter((1..30).map { getString(R.string.rotary_item_label, it) })
        }
    }
}

