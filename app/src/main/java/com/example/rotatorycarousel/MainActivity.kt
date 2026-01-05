package com.example.rotatorycarousel

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.rotatorycarousel.ui.carousel.rotary.RotaryAdapter
import com.example.rotatorycarousel.ui.carousel.rotary.RotaryLayoutManager
import com.example.rotatorycarousel.ui.carousel.rotary.RotarySnapHelper

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Launch CarouselActivity
        startActivity(Intent(this, CarouselActivity::class.java))
        finish()
        return
        
        setContentView(R.layout.activity_main)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)

        recyclerView.apply {
            clipChildren = false
            clipToPadding = false
            layoutManager = RotaryLayoutManager(
                context = this@MainActivity,
                radius = 700,
                angleInterval = 30f
            )
            adapter = RotaryAdapter((1..3).map { "Item $it" })
            
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                    val center = rv.width / 2f
                    val radius = 600f
                    val arcAngle = 55f

                    for (i in 0 until rv.childCount) {
                        val child = rv.getChildAt(i)
                        val childCenter = child.x + (child.width / 2f)

                        val distanceFromCenter = (childCenter - center) / center
                        val angle = distanceFromCenter * arcAngle

                        val rad = Math.toRadians(angle.toDouble())

                        val translateZ = (Math.cos(rad) * radius).toFloat()
                        val translateY = (Math.sin(rad) * radius * 0.40f).toFloat()

                        child.translationZ = -translateZ
                        child.translationY = translateY * -1

                        val scale = 0.8f + (1 - kotlin.math.abs(distanceFromCenter)) * 0.2f
                        child.scaleX = scale
                        child.scaleY = scale

                        child.rotationY = 0f
                        child.rotationX = 0f
                    }
                }
            })
        }

        RotarySnapHelper().attachToRecyclerView(recyclerView)
    }
}