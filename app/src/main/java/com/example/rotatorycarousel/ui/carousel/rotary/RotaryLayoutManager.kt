package com.example.rotatorycarousel.ui.carousel.rotary

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.*

class RotaryLayoutManager(
    private val context: Context,
    private val radius: Int = 600,
    private val angleInterval: Float = 20f
) : RecyclerView.LayoutManager() {

    private var offsetAngle = 0f

    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
        return RecyclerView.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun canScrollHorizontally() = true

    override fun scrollHorizontallyBy(dx: Int, recycler: RecyclerView.Recycler, state: RecyclerView.State): Int {
        offsetAngle += dx * 0.15f
        onLayoutChildren(recycler, state)
        return dx
    }

    override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        detachAndScrapAttachedViews(recycler)

        if (itemCount == 0) return

        val centerX = width / 2
        val centerY = height / 2

        for (i in 0 until itemCount) {

            val view = recycler.getViewForPosition(i)
            addView(view)
            measureChildWithMargins(view, 0, 0)

            val angle = (i * angleInterval) - offsetAngle
            val rad = Math.toRadians(angle.toDouble())

            // TRUE ROTARY GEOMETRY
            val x = (centerX + radius * sin(rad)).toInt()
            val y = (centerY - radius * cos(rad)).toInt()

            val w = getDecoratedMeasuredWidth(view)
            val h = getDecoratedMeasuredHeight(view)

            layoutDecorated(view, x - w/2, y - h/2, x + w/2, y + h/2)

            // SCALE BASED ON DEPTH
            val depth = cos(rad).toFloat()
            view.scaleX = 0.7f + depth * 0.3f
            view.scaleY = 0.7f + depth * 0.3f

            // KEEP ITEMS FACING FORWARD
            view.rotationY = 0f
        }
    }
}
