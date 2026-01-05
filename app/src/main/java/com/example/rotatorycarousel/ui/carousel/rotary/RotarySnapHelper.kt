package com.example.rotatorycarousel.ui.carousel.rotary

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper

class RotarySnapHelper : SnapHelper() {

    override fun findSnapView(layoutManager: RecyclerView.LayoutManager): View? =
        layoutManager.getChildAt(0)

    override fun findTargetSnapPosition(
        layoutManager: RecyclerView.LayoutManager,
        velocityX: Int,
        velocityY: Int
    ): Int {
        return RecyclerView.NO_POSITION
    }

    override fun calculateDistanceToFinalSnap(
        layoutManager: RecyclerView.LayoutManager,
        targetView: View
    ): IntArray = intArrayOf(0, 0)
}

