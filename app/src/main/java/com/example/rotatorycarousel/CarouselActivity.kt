package com.example.rotatorycarousel

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.rotatorycarousel.ui.carousel.CarouselView

class CarouselActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Create carousel
        val carousel = CarouselView(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            
            // Configuration
            radius = 450f // Closer spacing when static
            perspective = -0.0015f
            decelerationRate = 0.95f
            wrapEnabled = true
            snapToItemBoundary = true
            
            // Set background color to match the image
            setBackgroundColor(Color.parseColor("#F5F5F5"))
            
            // Handle item clicks
            onItemClickListener = object : CarouselView.OnItemClickListener {
                override fun onItemClick(view: View, index: Int) {
                    Toast.makeText(
                        this@CarouselActivity,
                        "Clicked item $index",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
        
        // Add cards
        val colors = listOf(
            "#FF6B6B" to "Local Data",
            "#4ECDC4" to "Minutes & SMS", 
            "#FFE66D" to "International"
        )
        
        colors.forEach { (colorHex, title) ->
            val card = createCard(title, Color.parseColor(colorHex))
            carousel.addCard(card)
        }
        
        setContentView(carousel)
    }
    
    private fun createCard(title: String, backgroundColor: Int): CardView {
        return CardView(this).apply {
            layoutParams = ViewGroup.MarginLayoutParams(450, 550).apply {
                setMargins(0, 0, 0, 0)
            }
            
            // Rounded corners like in the image
            radius = 40f
            
            // Softer, more prominent shadow
            cardElevation = 24f
            maxCardElevation = 32f
            
            // Add subtle shadow with outlineAmbientShadowColor
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                outlineAmbientShadowColor = Color.parseColor("#40000000")
                outlineSpotShadowColor = Color.parseColor("#40000000")
            }
            
            setCardBackgroundColor(backgroundColor)
            
            // Add content
            val textView = TextView(context).apply {
                text = title
                textSize = 32f
                setTextColor(Color.WHITE)
                gravity = Gravity.CENTER
                setPadding(48, 220, 48, 48)
                
                // Better text rendering
                setTypeface(null, android.graphics.Typeface.BOLD)
            }
            
            addView(textView)
        }
    }
}
