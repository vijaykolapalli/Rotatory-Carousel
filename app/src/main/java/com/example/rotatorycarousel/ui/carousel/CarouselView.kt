package com.example.rotatorycarousel.ui.carousel

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI
import kotlin.math.atan2

/**
 * 3D Rotary Carousel implementation matching iOS iCarousel rotary type
 * Features:
 * - Circular arrangement of cards in 3D space
 * - Smooth touch-based rotation
 * - Velocity-based flinging with deceleration
 * - Automatic snapping to item boundaries
 * - Configurable radius, perspective, and spacing
 */
class CarouselView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    // Configuration
    var itemCount = 0
        private set
    var radius = 600f // Distance from center in pixels
    var perspective = -0.002f // Perspective effect (-1/500 default like iCarousel)
    var decelerationRate = 0.95f // How quickly to slow down when flicked
    var bounces = false // Whether to bounce at edges (for non-wrapped carousels)
    var wrapEnabled = true // Whether carousel wraps around
    var snapToItemBoundary = true // Auto-snap to nearest item
    
    // State
    private var currentAngle = 0f // Current rotation angle in radians
    private var targetAngle = 0f // Target angle for animations
    private var velocity = 0f // Current rotation velocity
    private var lastTouchAngle = 0f
    private val cards = mutableListOf<View>()
    
    // Animation
    private var animator: ValueAnimator? = null
    private var isAnimating = false
    private var isDragging = false
    
    // Gesture detection
    private val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            if (e1 == null) return false
            
            // Calculate angular velocity based on touch movement
            val centerX = width / 2f
            val centerY = height / 2f
            
            val angle1 = atan2(e1.y - centerY, e1.x - centerX)
            val angle2 = atan2(e2.y - centerY, e2.x - centerX)
            
            velocity = (angle2 - angle1) * 2 // Multiply for more responsive fling
            startDeceleration()
            return true
        }
        
        override fun onSingleTapUp(e: MotionEvent): Boolean {
            handleTap(e)
            return true
        }
    })
    
    // Listener interface
    interface OnItemClickListener {
        fun onItemClick(view: View, index: Int)
    }
    
    var onItemClickListener: OnItemClickListener? = null
    
    init {
        // Enable layer type for 3D transformations
        setLayerType(View.LAYER_TYPE_SOFTWARE, null)
    }
    
    /**
     * Set the number of items in the carousel
     */
    fun setItemCount(count: Int) {
        itemCount = count
        requestLayout()
    }
    
    /**
     * Add a card view to the carousel
     */
    fun addCard(view: View) {
        cards.add(view)
        addView(view)
        itemCount = cards.size
        updateCardPositions()
    }
    
    /**
     * Remove all cards
     */
    fun clearCards() {
        cards.clear()
        removeAllViews()
        itemCount = 0
    }
    
    /**
     * Get the currently centered item index
     */
    fun getCurrentItemIndex(): Int {
        if (itemCount == 0) return -1
        val angleStep = (2 * PI / itemCount).toFloat()
        val normalizedAngle = (currentAngle % (2 * PI)).toFloat()
        val index = (normalizedAngle / angleStep).toInt() % itemCount
        return if (index < 0) index + itemCount else index
    }
    
    /**
     * Scroll to specific item index with animation
     */
    fun scrollToItem(index: Int, animated: Boolean = true) {
        if (itemCount == 0) return
        
        val angleStep = (2 * PI / itemCount).toFloat()
        val targetAngle = index * angleStep
        
        if (animated) {
            animateToAngle(targetAngle)
        } else {
            currentAngle = targetAngle
            updateCardPositions()
        }
    }
    
    private fun updateCardPositions() {
        if (itemCount == 0 || cards.isEmpty()) return
        
        val angleStep = (2 * PI / itemCount).toFloat()
        val centerX = width / 2f
        val centerY = height / 2f
        
        cards.forEachIndexed { index, card ->
            val angle = currentAngle + (index * angleStep)
            
            // Calculate 3D position
            val x = (radius * sin(angle)).toFloat()
            val z = (radius * cos(angle)).toFloat()
            
            // Normalize Z to 0-1 range (0 = back, 1 = front)
            val normalizedZ = (z + radius) / (2 * radius)
            
            // Scale: larger at front (1.0), smaller at back (0.6)
            val scale = 0.6f + (normalizedZ * 0.4f) // Range: 0.6 to 1.0
            
            // Alpha: slightly transparent at back, fully opaque at front
            val alpha = 0.4f + (normalizedZ * 0.6f) // Range: 0.4 to 1.0
            
            // Apply transformations
            card.apply {
                // Position
                translationX = x
                translationZ = z
                
                // Scale based on depth - larger at front
                scaleX = scale
                scaleY = scale
                
                // Alpha based on depth
                this.alpha = alpha
                
                // NO rotation - cards always face forward
                rotationY = 0f
                
                // Elevation for proper Z-ordering
                elevation = (z + radius) * 2
            }
        }
        
        // Sort by Z position for proper drawing order
        cards.sortedByDescending { it.translationZ }.forEach { it.bringToFront() }
    }
    
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val handled = gestureDetector.onTouchEvent(event)
        
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                stopAllAnimations()
                isDragging = true
                
                val centerX = width / 2f
                val centerY = height / 2f
                lastTouchAngle = atan2(event.y - centerY, event.x - centerX)
                return true
            }
            
            MotionEvent.ACTION_MOVE -> {
                if (isDragging) {
                    val centerX = width / 2f
                    val centerY = height / 2f
                    val currentTouchAngle = atan2(event.y - centerY, event.x - centerX)
                    
                    val deltaAngle = currentTouchAngle - lastTouchAngle
                    currentAngle += deltaAngle
                    lastTouchAngle = currentTouchAngle
                    
                    updateCardPositions()
                    return true
                }
            }
            
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isDragging = false
                if (!handled && snapToItemBoundary) {
                    snapToNearestItem()
                }
                return true
            }
        }
        
        return handled || super.onTouchEvent(event)
    }
    
    private fun handleTap(event: MotionEvent) {
        // Find which card was tapped
        cards.forEachIndexed { index, card ->
            val location = IntArray(2)
            card.getLocationOnScreen(location)
            
            val cardX = location[0]
            val cardY = location[1]
            
            if (event.rawX >= cardX && event.rawX <= cardX + card.width &&
                event.rawY >= cardY && event.rawY <= cardY + card.height) {
                
                // Check if it's the centered item or if we should scroll to it
                val currentIndex = getCurrentItemIndex()
                if (index == currentIndex) {
                    onItemClickListener?.onItemClick(card, index)
                } else {
                    scrollToItem(index, animated = true)
                }
            }
        }
    }
    
    private fun snapToNearestItem() {
        if (itemCount == 0) return
        
        val angleStep = (2 * PI / itemCount).toFloat()
        val normalizedAngle = currentAngle % (2 * PI).toFloat()
        val nearestIndex = Math.round(normalizedAngle / angleStep)
        val targetAngle = nearestIndex * angleStep
        
        animateToAngle(targetAngle)
    }
    
    private fun animateToAngle(target: Float) {
        stopAllAnimations()
        
        val startAngle = currentAngle
        val endAngle = target
        
        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 300
            interpolator = DecelerateInterpolator()
            
            addUpdateListener { animation ->
                val progress = animation.animatedValue as Float
                currentAngle = startAngle + (endAngle - startAngle) * progress
                updateCardPositions()
            }
            
            start()
        }
    }
    
    private fun startDeceleration() {
        stopAllAnimations()
        isAnimating = true
        
        post(object : Runnable {
            override fun run() {
                if (!isAnimating || abs(velocity) < 0.001f) {
                    isAnimating = false
                    velocity = 0f
                    if (snapToItemBoundary) {
                        snapToNearestItem()
                    }
                    return
                }
                
                currentAngle += velocity
                velocity *= decelerationRate
                
                updateCardPositions()
                post(this)
            }
        })
    }
    
    private fun stopAllAnimations() {
        isAnimating = false
        velocity = 0f
        animator?.cancel()
        animator = null
    }
    
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        
        // Center all cards in the layout
        cards.forEach { card ->
            val cardWidth = card.measuredWidth
            val cardHeight = card.measuredHeight
            val centerX = (width - cardWidth) / 2
            val centerY = (height - cardHeight) / 2
            card.layout(centerX, centerY, centerX + cardWidth, centerY + cardHeight)
        }
        
        updateCardPositions()
    }
    
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        
        // Measure all cards
        cards.forEach { card ->
            measureChild(card, widthMeasureSpec, heightMeasureSpec)
        }
    }
}

