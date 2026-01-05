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
    var radius = 450f // Reduced radius for closer cards when static
    var perspective = -0.0015f
    var decelerationRate = 0.95f
    var bounces = false
    var wrapEnabled = true
    var snapToItemBoundary = true
    
    // Dynamic spacing
    private var currentRadius = 450f // Animated radius value
    private val staticRadius = 450f // Cards closer when static
    private val movingRadius = 650f // Cards spread out when moving
    private var radiusAnimator: ValueAnimator? = null
    
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
            
            // Calculate 3D position using currentRadius (animated)
            val x = (currentRadius * sin(angle)).toFloat()
            val z = (currentRadius * cos(angle)).toFloat()
            
            // Normalize Z to 0-1 range (0 = back, 1 = front)
            val normalizedZ = (z + currentRadius) / (2 * currentRadius)
            
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
                elevation = (z + currentRadius) * 2
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
                
                // Expand spacing when user starts dragging
                animateRadius(movingRadius, duration = 200)
                
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
                if (!handled) {
                    // Snap to nearest item and collapse spacing
                    snapToNearestItemWithCollapse()
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
                
                // Always scroll tapped card to center
                scrollToItem(index, animated = true)
                
                // Only trigger click callback if it's already centered
                val currentIndex = getCurrentItemIndex()
                if (index == currentIndex) {
                    onItemClickListener?.onItemClick(card, index)
                }
            }
        }
    }
    
    private fun snapToNearestItemWithCollapse() {
        if (itemCount == 0) return
        
        val angleStep = (2 * PI / itemCount).toFloat()
        val normalizedAngle = currentAngle % (2 * PI).toFloat()
        val nearestIndex = Math.round(normalizedAngle / angleStep)
        val targetAngle = nearestIndex * angleStep
        
        // Animate to target angle and collapse spacing simultaneously
        animateToAngleAndCollapse(targetAngle)
    }
    
    private fun animateToAngleAndCollapse(target: Float) {
        stopAllAnimations()
        
        val startAngle = currentAngle
        val endAngle = target
        
        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 400
            interpolator = DecelerateInterpolator()
            
            addUpdateListener { animation ->
                val progress = animation.animatedValue as Float
                currentAngle = startAngle + (endAngle - startAngle) * progress
                updateCardPositions()
            }
            
            start()
        }
        
        // Simultaneously collapse spacing
        animateRadius(staticRadius, duration = 400)
    }
    
    private fun animateRadius(targetRadius: Float, duration: Long = 300) {
        radiusAnimator?.cancel()
        
        val startRadius = currentRadius
        
        radiusAnimator = ValueAnimator.ofFloat(startRadius, targetRadius).apply {
            this.duration = duration
            interpolator = DecelerateInterpolator()
            
            addUpdateListener { animation ->
                currentRadius = animation.animatedValue as Float
                updateCardPositions()
            }
            
            start()
        }
    }
    
    private fun snapToNearestItem() {
        if (itemCount == 0) return
        
        val angleStep = (2 * PI / itemCount).toFloat()
        val normalizedAngle = currentAngle % (2 * PI).toFloat()
        val nearestIndex = Math.round(normalizedAngle / angleStep)
        val targetAngle = nearestIndex * angleStep
        
        animateToAngleAndCollapse(targetAngle)
    }
    
    private fun animateToAngle(target: Float) {
        animateToAngleAndCollapse(target)
    }
    
    private fun startDeceleration() {
        stopAllAnimations()
        isAnimating = true
        
        // Expand spacing during fling
        animateRadius(movingRadius, duration = 150)
        
        post(object : Runnable {
            override fun run() {
                if (!isAnimating || abs(velocity) < 0.001f) {
                    isAnimating = false
                    velocity = 0f
                    if (snapToItemBoundary) {
                        snapToNearestItemWithCollapse()
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
        radiusAnimator?.cancel()
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

