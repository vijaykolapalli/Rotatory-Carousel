# Technical Implementation Guide

This document provides an in-depth technical explanation of the 3D Rotary Carousel implementation.

---

## 📐 Mathematical Foundation

### Circular Motion in 3D Space

The carousel arranges cards on a circle in the XZ plane (horizontal circle viewed from above).

#### Basic Circle Equations

For a circle with radius `r` and angle `θ`:
```
x = r × sin(θ)
z = r × cos(θ)
```

Where:
- `x` = horizontal displacement from center
- `z` = depth (distance forward/backward from screen)
- `θ` = angle in radians (0 to 2π)

#### Angle Distribution

With `n` cards, they are evenly distributed:
```
angleStep = 2π / n
cardAngle[i] = currentAngle + (i × angleStep)
```

Example with 3 cards:
- Card 0: currentAngle + 0° 
- Card 1: currentAngle + 120°
- Card 2: currentAngle + 240°

---

## 🎯 Centering Logic

### The Core Formula

For a card at index `i` to be centered (at angle = 0):
```
currentAngle + (i × angleStep) = 0
currentAngle = -(i × angleStep)
```

### Why Negative?

Cards rotate around the circle as `currentAngle` increases:
- Positive `currentAngle` → cards rotate clockwise
- To bring card `i` to center → need to "undo" its position → use negative

### Example

With 3 cards (angleStep = 120° = 2.094 radians):

| Action | currentAngle | Card 0 Position | Card 1 Position | Card 2 Position |
|--------|--------------|-----------------|-----------------|-----------------|
| Initial | 0 | 0° (center) | 120° (right) | 240° (left) |
| Tap Card 1 | -2.094 | -120° (left) | 0° (center) | 120° (right) |
| Tap Card 2 | -4.189 | -240° (back) | -120° (left) | 0° (center) |

---

## 🔄 Shortest Path Rotation

### The Problem

When rotating from angle A to angle B on a circle:
- Direct path: B - A
- But circles wrap around at 2π
- Example: Going from 350° to 10° is shorter going forward (20°) than backward (340°)

### The Solution

```kotlin
private fun shortestAngleDelta(from: Float, to: Float): Float {
    val twoPi = (2 * PI).toFloat()
    var delta = (to - from) % twoPi
    
    // Normalize to [-π, π]
    if (delta > PI) {
        delta -= twoPi  // Go the other way
    } else if (delta < -π) {
        delta += twoPi  // Go the other way
    }
    
    return delta
}
```

### Step-by-Step Example

**Scenario**: Rotate from 350° to 10°

```
1. Calculate raw delta:
   delta = 10° - 350° = -340°

2. Normalize:
   -340° < -180°, so add 360°
   delta = -340° + 360° = 20°

3. Result: Rotate forward 20° (not backward 340°)
```

### Visual Representation

```
        0°/360°
           |
      10° /|\ 350°
         / | \
        |  |  |
   270° |  •  | 90°
        |     |
         \   /
          \ /
          180°

From 350° to 10°:
  Direct: -340° ❌
  Shortest: +20° ✅
```

---

## 🎨 Depth-Based Visual Effects

### Normalization

Convert Z-coordinate to 0-1 range:
```kotlin
val normalizedZ = (z + currentRadius) / (2 * currentRadius)
```

Where:
- `z` ranges from `-currentRadius` to `+currentRadius`
- After normalization: 0 (back) to 1 (front)

### Scale Calculation

```kotlin
val scale = 0.6f + (normalizedZ * 0.4f)
```

Breakdown:
- Base scale: 0.6 (minimum size at back)
- Variable scale: normalizedZ × 0.4 (0 to 0.4)
- Total range: 0.6 to 1.0

**Effect**: Front cards are 1.67× larger than back cards

### Alpha Calculation

```kotlin
val alpha = 0.4f + (normalizedZ * 0.6f)
```

Breakdown:
- Base alpha: 0.4 (40% opaque at back)
- Variable alpha: normalizedZ × 0.6 (0 to 0.6)
- Total range: 0.4 to 1.0

**Effect**: Front cards fully opaque, back cards semi-transparent

### Elevation for Z-Ordering

```kotlin
elevation = (z + currentRadius) * 2
```

Why multiply by 2?
- Ensures sufficient separation for Android's renderer
- Higher elevation = rendered on top
- Cards at front have higher elevation

---

## 📱 Touch Interaction System

### 1. Touch Angle Calculation

Convert screen coordinates to angle:
```kotlin
val centerX = width / 2f
val centerY = height / 2f
val angle = atan2(event.y - centerY, event.x - centerX)
```

`atan2(y, x)` returns angle in radians from -π to π:
```
        90° (π/2)
           |
           |
180° ------+------ 0°
  (-π)     |      (0)
           |
        -90° (-π/2)
```

### 2. Drag Gesture

**Goal**: Rotate carousel based on finger movement around the circle

**Implementation**:
```kotlin
ACTION_DOWN -> {
    lastTouchAngle = atan2(event.y - centerY, event.x - centerX)
}

ACTION_MOVE -> {
    currentTouchAngle = atan2(event.y - centerY, event.x - centerX)
    deltaAngle = shortestAngleDelta(lastTouchAngle, currentTouchAngle)
    currentAngle += deltaAngle
    lastTouchAngle = currentTouchAngle
}
```

**Why shortest path?** 
Prevents jumps when finger crosses the -π/π boundary.

### 3. Fling Gesture

**Goal**: Continue rotation with momentum after release

**Velocity Calculation**:
```kotlin
val angle1 = atan2(e1.y - centerY, e1.x - centerX)
val angle2 = atan2(e2.y - centerY, e2.x - centerX)
val angleDelta = shortestAngleDelta(angle1, angle2)
velocity = angleDelta * 2  // Amplify for responsiveness
```

**Deceleration Loop**:
```kotlin
post(object : Runnable {
    override fun run() {
        if (abs(velocity) < 0.001f) {
            snapToNearest()
            return
        }
        
        currentAngle += velocity
        velocity *= decelerationRate  // e.g., 0.95
        updateCardPositions()
        post(this)  // Next frame
    }
})
```

**Physics**: Exponential decay
- Frame 0: velocity = v₀
- Frame 1: velocity = v₀ × 0.95
- Frame 2: velocity = v₀ × 0.95²
- Frame n: velocity = v₀ × 0.95ⁿ

### 4. Tap Detection

**Hit Testing**:
```kotlin
cards.forEachIndexed { index, card ->
    val location = IntArray(2)
    card.getLocationOnScreen(location)
    
    if (event.rawX in (cardX..cardX + card.width) &&
        event.rawY in (cardY..cardY + card.height)) {
        // Card tapped!
        scrollToItem(index, animated = true)
    }
}
```

**Behavior**:
- If tapped card not centered → animate to center
- If already centered → trigger click callback

---

## 🎬 Animation System

### 1. Angle Animation

**Smooth rotation to target angle**:

```kotlin
private fun animateToAngleShortestPath(target: Float) {
    val startAngle = currentAngle
    val delta = shortestAngleDelta(startAngle, target)
    val endAngle = startAngle + delta
    
    ValueAnimator.ofFloat(0f, 1f).apply {
        duration = 400
        interpolator = DecelerateInterpolator()
        
        addUpdateListener { animation ->
            val progress = animation.animatedValue as Float
            currentAngle = startAngle + delta * progress
            updateCardPositions()
        }
        start()
    }
}
```

**Key points**:
- Animate from 0 to 1 (progress)
- Interpolate actual angle: `start + (delta × progress)`
- Update positions every frame
- Use `DecelerateInterpolator` for natural stop

### 2. Radius Animation

**Dynamic spacing effect**:

```kotlin
private fun animateRadius(targetRadius: Float, duration: Long) {
    val startRadius = currentRadius
    
    ValueAnimator.ofFloat(startRadius, targetRadius).apply {
        this.duration = duration
        interpolator = DecelerateInterpolator()
        
        addUpdateListener { animation ->
            currentRadius = animation.animatedValue as Float
            updateCardPositions()
        }
        start()
    }
}
```

**Usage**:
- User touches → expand to `movingRadius`
- User releases → collapse to `staticRadius`
- Creates "breathing" effect

### 3. Simultaneous Animations

**Run angle + radius animations together**:

```kotlin
animateToAngleShortestPath(targetAngle)  // Rotate
animateRadius(staticRadius)              // Collapse
```

Both run independently but update the same `updateCardPositions()` method.

---

## 🔍 Position Update Pipeline

The core rendering method called every frame:

```kotlin
private fun updateCardPositions() {
    val angleStep = (2 * PI / itemCount).toFloat()
    
    cards.forEachIndexed { index, card ->
        // 1. Calculate angle
        val angle = currentAngle + (index * angleStep)
        
        // 2. Convert to 3D position
        val x = (currentRadius * sin(angle)).toFloat()
        val z = (currentRadius * cos(angle)).toFloat()
        
        // 3. Normalize depth
        val normalizedZ = (z + currentRadius) / (2 * currentRadius)
        
        // 4. Calculate visual effects
        val scale = 0.6f + (normalizedZ * 0.4f)
        val alpha = 0.4f + (normalizedZ * 0.6f)
        
        // 5. Apply transformations
        card.apply {
            translationX = x
            translationZ = z
            scaleX = scale
            scaleY = scale
            this.alpha = alpha
            rotationY = 0f  // Cards always face forward
            elevation = (z + currentRadius) * 2
        }
    }
    
    // 6. Fix Z-ordering
    cards.sortedByDescending { it.translationZ }.forEach { 
        it.bringToFront() 
    }
}
```

**Execution Flow**:
```
Touch/Animation
      ↓
Update currentAngle
      ↓
updateCardPositions()
      ↓
For each card:
  - Calculate angle
  - Calculate 3D position
  - Calculate scale/alpha
  - Apply transformations
      ↓
Sort by depth
      ↓
Render to screen
```

---

## 🏗️ View Lifecycle

### Initialization

```kotlin
init {
    setLayerType(View.LAYER_TYPE_SOFTWARE, null)
}
```

Why `LAYER_TYPE_SOFTWARE`?
- Enables proper 3D transformation rendering
- Required for `translationZ` and `elevation`
- May impact performance on complex layouts

### Measurement

```kotlin
override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    
    cards.forEach { card ->
        measureChild(card, widthMeasureSpec, heightMeasureSpec)
    }
}
```

**Purpose**: Determine card dimensions before layout.

### Layout

```kotlin
override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
    super.onLayout(changed, left, top, right, bottom)
    
    cards.forEach { card ->
        val cardWidth = card.measuredWidth
        val cardHeight = card.measuredHeight
        val centerX = (width - cardWidth) / 2
        val centerY = (height - cardHeight) / 2
        card.layout(centerX, centerY, centerX + cardWidth, centerY + cardHeight)
    }
    
    updateCardPositions()
}
```

**Strategy**: 
1. Place all cards at view center
2. Use transformations (`translationX/Z`) to position
3. Avoids complex layout calculations

---

## ⚡ Performance Considerations

### Optimization Techniques

1. **Minimize allocations in `updateCardPositions()`**
   - No object creation in hot path
   - Reuse primitives

2. **Cancel previous animations**
   ```kotlin
   private fun stopAllAnimations() {
       animator?.cancel()
       radiusAnimator?.cancel()
   }
   ```

3. **Use hardware acceleration where possible**
   - But fallback to software for Z-transforms

4. **Efficient sorting**
   ```kotlin
   cards.sortedByDescending { it.translationZ }
   ```
   - O(n log n) but only n cards (small n)

### Potential Bottlenecks

1. **Too many cards** (>20)
   - Consider recycling pattern
   - Limit visible cards

2. **Complex card layouts**
   - Heavy view hierarchies slow rendering
   - Use ConstraintLayout or simple layouts

3. **Frequent `bringToFront()`**
   - Triggers layout invalidation
   - Acceptable for small card counts

---

## 🧪 Testing Strategies

### Unit Tests

Test mathematical functions:
```kotlin
@Test
fun testShortestAngleDelta() {
    assertEquals(0.35f, shortestAngleDelta(0f, 0.35f), 0.01f)
    assertEquals(0.35f, shortestAngleDelta(6.28f, 0.35f), 0.01f)
    assertEquals(-0.35f, shortestAngleDelta(0.35f, 0f), 0.01f)
}
```

### UI Tests

Test interactions:
```kotlin
@Test
fun testCardTapCenters() {
    val carousel = CarouselView(context)
    // Add cards
    // Simulate tap on card 1
    // Assert card 1 is centered
}
```

### Manual Testing Checklist

- [ ] Drag left/right smoothly
- [ ] Fling with momentum
- [ ] Tap cards to center
- [ ] Edge wrapping works
- [ ] No visual glitches
- [ ] Performance smooth (60fps)

---

## 🔧 Debugging Tips

### Enable Debug Logging

Add to `updateCardPositions()`:
```kotlin
Log.d("Carousel", "Card $index: angle=$angle, x=$x, z=$z, scale=$scale")
```

### Visualize Angles

Draw debug overlay:
```kotlin
override fun onDraw(canvas: Canvas) {
    super.onDraw(canvas)
    
    canvas.drawText(
        "Angle: %.2f".format(currentAngle),
        50f, 50f, debugPaint
    )
}
```

### Check Touch Areas

Highlight card bounds:
```kotlin
card.setBackgroundResource(R.drawable.debug_border)
```

---

## 📚 Further Reading

- [Android Custom Views Guide](https://developer.android.com/guide/topics/ui/custom-components)
- [Android Animation Framework](https://developer.android.com/guide/topics/graphics/prop-animation)
- [Matrix Transformations](https://developer.android.com/reference/android/graphics/Matrix)
- [Circular Motion Math](https://en.wikipedia.org/wiki/Circular_motion)

---

**End of Technical Guide**

