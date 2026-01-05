package com.example.rotatorycarousel.circular

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.*

class CircularLayoutManager(
    private val context: Context,
    private val radiusRatio: Float = 0.7f,
    private val angleStep: Float = (2 * PI / 3).toFloat()
) : RecyclerView.LayoutManager() {

    private var scrollOffset = 0f

    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams =
        RecyclerView.LayoutParams(
            RecyclerView.LayoutParams.WRAP_CONTENT,
            RecyclerView.LayoutParams.WRAP_CONTENT
        )

    override fun canScrollHorizontally() = true
    override fun canScrollVertically() = false

    override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        if (itemCount == 0) {
            removeAndRecycleAllViews(recycler)
            return
        }

        detachAndScrapAttachedViews(recycler)
        layoutVisibleChildren(recycler)
    }

    private fun layoutVisibleChildren(recycler: RecyclerView.Recycler) {
        val centerX = width / 2f
        val centerY = height / 2f
        val radius = min(width, height) * radiusRatio

        val centerIndex = scrollOffset.roundToInt()
        val visibleRange = 1

        val start = (centerIndex - visibleRange).coerceAtLeast(0)
        val end = (centerIndex + visibleRange).coerceAtMost(itemCount - 1)

        for (i in start..end) {
            val view = recycler.getViewForPosition(i)
            addView(view)
            measureChildWithMargins(view, 0, 0)

            val offset = i - scrollOffset
            applyCircularTransform(view, offset, centerX, centerY, radius)
        }
    }

    private fun applyCircularTransform(
        view: View,
        offset: Float,
        centerX: Float,
        centerY: Float,
        radius: Float
    ) {
        val angle = offset * angleStep

        val x = centerX + radius * cos(angle) - view.measuredWidth / 2f
        val y = centerY + radius * sin(angle) - view.measuredHeight / 2f

        val distanceFromCenter = abs(offset)
        val scale = (1.0f - distanceFromCenter * 0.3f).coerceIn(0.6f, 1.0f)

        val left = x.toInt()
        val top = y.toInt()

        layoutDecoratedWithMargins(
            view,
            left,
            top,
            left + view.measuredWidth,
            top + view.measuredHeight
        )

        view.apply {
            pivotX = measuredWidth / 2f
            pivotY = measuredHeight / 2f

            scaleX = scale
            scaleY = scale

            val rotationAngle = Math.toDegrees(angle.toDouble()).toFloat()
            rotation = rotationAngle

            alpha = (1.0f - distanceFromCenter * 0.4f).coerceIn(0.5f, 1.0f)
        }
    }

    override fun scrollHorizontallyBy(
        dx: Int,
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State
    ): Int {
        val deltaOffset = dx / (width * 0.1f)
        scrollOffset += deltaOffset

        scrollOffset = scrollOffset.coerceIn(0f, (itemCount - 1).toFloat())

        detachAndScrapAttachedViews(recycler)
        layoutVisibleChildren(recycler)

        return dx
    }
}

