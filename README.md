# 3D Rotary Carousel for Android

A beautiful, smooth 3D rotary carousel implementation for Android, inspired by iOS iCarousel. This project provides an elegant way to display cards in a circular 3D arrangement with smooth touch interactions and animations.

![Android](https://img.shields.io/badge/Platform-Android-green.svg)
![Kotlin](https://img.shields.io/badge/Language-Kotlin-blue.svg)
![Min SDK](https://img.shields.io/badge/Min%20SDK-30-orange.svg)

---

## 📚 Documentation

This README provides an overview. For detailed information, see:

- 📖 **[README.md](README.md)** (This file) - Project overview and quick start
- 🔧 **[TECHNICAL_GUIDE.md](TECHNICAL_GUIDE.md)** - In-depth technical implementation details
- 📋 **[API_REFERENCE.md](API_REFERENCE.md)** - Complete API documentation
- 🚀 **[INTEGRATION_GUIDE.md](INTEGRATION_GUIDE.md)** - Step-by-step integration instructions
- 📄 **[DOCS_INDEX.md](DOCS_INDEX.md)** - Documentation navigation guide

**New to the project?** Start here, then check the [DOCS_INDEX.md](DOCS_INDEX.md) for learning paths.

---

## 📋 Table of Contents

- [Features](#features)
- [Demo](#demo)
- [Architecture](#architecture)
- [Installation](#installation)
- [Quick Start](#quick-start)
- [Detailed Implementation Guide](#detailed-implementation-guide)
- [Configuration Options](#configuration-options)
- [How It Works](#how-it-works)
- [Building the Project](#building-the-project)
- [License](#license)

---

## ✨ Features

- **3D Circular Arrangement**: Cards positioned in a circular 3D space with depth perception
- **Smooth Touch Controls**: Natural drag gestures to rotate the carousel
- **Fling Gestures**: Swipe to spin with velocity-based deceleration
- **Auto Snapping**: Automatically snaps to the nearest card when released
- **Dynamic Spacing**: Cards spread out during interaction, collapse when static
- **Shortest Path Rotation**: Always rotates in the most efficient direction
- **Tap to Center**: Tap any card to smoothly bring it to center
- **Scale & Alpha Effects**: Cards scale and fade based on their depth position
- **Configurable**: Highly customizable radius, speed, perspective, and more
- **No External Dependencies**: Pure Android implementation

---

## 🎬 Demo

The carousel displays cards in a 3D circular arrangement:
- Front cards are larger and fully opaque
- Back cards are smaller and semi-transparent
- Smooth rotation in both directions
- Dynamic spacing during interaction

---

## 🏗️ Architecture

### Project Structure

```
RotatoryCarousel/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/example/rotatorycarousel/
│   │       │   ├── ui/carousel/
│   │       │   │   └── CarouselView.kt          # Main carousel component
│   │       │   ├── CarouselActivity.kt          # Demo activity
│   │       │   └── MainActivity.kt              # Entry point
│   │       ├── res/
│   │       └── AndroidManifest.xml
│   └── build.gradle.kts
├── build.gradle.kts
└── README.md
```

### Key Components

1. **CarouselView.kt**: The main custom view that handles all carousel logic
2. **CarouselActivity.kt**: Example implementation showing how to use the carousel
3. **MainActivity.kt**: App entry point that launches CarouselActivity

---

## 📦 Installation

### Prerequisites

- Android Studio Arctic Fox or later
- Kotlin 1.9+
- Min SDK: 30 (Android 11)
- Target SDK: 36

### Clone the Repository

```bash
git clone https://github.com/yourusername/RotatoryCarousel.git
cd RotatoryCarousel
```

### Open in Android Studio

1. Open Android Studio
2. Select **File > Open**
3. Navigate to the cloned repository
4. Click **OK**

---

## 🚀 Quick Start

### Basic Usage

```kotlin
// Create carousel
val carousel = CarouselView(context).apply {
    layoutParams = ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT
    )
    
    // Configure
    radius = 450f
    decelerationRate = 0.95f
    snapToItemBoundary = true
}

// Create and add cards
val card = CardView(context).apply {
    layoutParams = ViewGroup.MarginLayoutParams(450, 550)
    radius = 40f
    cardElevation = 24f
    // ... customize card
}
carousel.addCard(card)

// Set as content view
setContentView(carousel)
```

### Handle Click Events

```kotlin
carousel.onItemClickListener = object : CarouselView.OnItemClickListener {
    override fun onItemClick(view: View, index: Int) {
        Toast.makeText(context, "Clicked item $index", Toast.LENGTH_SHORT).show()
    }
}
```

---

## 📖 Detailed Implementation Guide

### Step 1: Understanding the Coordinate System

The carousel uses a 3D coordinate system:
- **X-axis**: Horizontal position (left/right)
- **Y-axis**: Vertical position (up/down) - not used for rotation
- **Z-axis**: Depth (forward/backward)

Cards are positioned on a circle in the XZ plane:
```
X = radius × sin(angle)
Z = radius × cos(angle)
```

### Step 2: Angle Calculation

Each card has a position determined by:
```kotlin
val angleStep = (2 * PI / itemCount).toFloat()
val cardAngle = currentAngle + (index * angleStep)
```

**Key insight**: To center a card at index `i`:
```
currentAngle = -(i × angleStep)
```

This ensures `cardAngle = 0` when the card is at front center.

### Step 3: 3D Positioning

For each card, we calculate:

```kotlin
// Position in 3D space
val x = (currentRadius * sin(angle)).toFloat()
val z = (currentRadius * cos(angle)).toFloat()

// Normalize Z to 0-1 range (0 = back, 1 = front)
val normalizedZ = (z + currentRadius) / (2 * currentRadius)

// Scale: larger at front, smaller at back
val scale = 0.6f + (normalizedZ * 0.4f) // Range: 0.6 to 1.0

// Alpha: transparent at back, opaque at front
val alpha = 0.4f + (normalizedZ * 0.6f) // Range: 0.4 to 1.0
```

### Step 4: Dynamic Spacing

The carousel uses two radius values:
- **Static Radius (450f)**: Cards closer together when idle
- **Moving Radius (650f)**: Cards spread out during interaction

```kotlin
// Expand when user starts dragging
animateRadius(movingRadius, duration = 200)

// Collapse when animation completes
animateRadius(staticRadius, duration = 400)
```

### Step 5: Shortest Path Rotation

To ensure smooth bidirectional rotation:

```kotlin
private fun shortestAngleDelta(from: Float, to: Float): Float {
    val twoPi = (2 * PI).toFloat()
    var delta = (to - from) % twoPi
    
    // Normalize to [-PI, PI]
    if (delta > PI) {
        delta -= twoPi
    } else if (delta < -PI) {
        delta += twoPi
    }
    
    return delta
}
```

This ensures the carousel always rotates the shorter way around the circle.

### Step 6: Touch Handling

Three types of touch interactions:

**A. Dragging**
```kotlin
ACTION_DOWN -> {
    // Stop animations
    // Expand spacing
    // Record initial touch angle
}

ACTION_MOVE -> {
    // Calculate angular change
    val deltaAngle = shortestAngleDelta(lastAngle, currentAngle)
    currentAngle += deltaAngle
    // Update positions
}

ACTION_UP -> {
    // Snap to nearest item
    // Collapse spacing
}
```

**B. Flinging**
```kotlin
override fun onFling(...): Boolean {
    // Calculate angular velocity
    val angleDelta = shortestAngleDelta(angle1, angle2)
    velocity = angleDelta * 2
    startDeceleration()
}
```

**C. Tapping**
```kotlin
override fun onSingleTapUp(e: MotionEvent): Boolean {
    // Find which card was tapped
    // Animate to center that card
    // Trigger click callback if already centered
}
```

### Step 7: Snap Animation

When user releases, snap to nearest card:

```kotlin
private fun snapToNearestItemWithCollapse() {
    val angleStep = (2 * PI / itemCount).toFloat()
    val normalizedAngle = currentAngle % (2 * PI).toFloat()
    
    // Find nearest card index
    val nearestIndex = Math.round(-normalizedAngle / angleStep)
    
    // Calculate target angle to center that card
    val targetAngle = -(nearestIndex * angleStep)
    
    // Animate using shortest path
    animateToAngleShortestPath(targetAngle)
}
```

### Step 8: Z-Ordering

Ensure cards render in correct depth order:

```kotlin
// Sort by Z position for proper drawing order
cards.sortedByDescending { it.translationZ }.forEach { 
    it.bringToFront() 
}
```

Cards further away (negative Z) render first, cards closer (positive Z) render on top.

---

## ⚙️ Configuration Options

### CarouselView Properties

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `radius` | Float | 450f | Base radius of the carousel circle |
| `perspective` | Float | -0.0015f | 3D perspective effect strength |
| `decelerationRate` | Float | 0.95f | How quickly fling velocity decreases (0-1) |
| `bounces` | Boolean | false | Whether carousel bounces at boundaries |
| `wrapEnabled` | Boolean | true | Enable infinite circular wrapping |
| `snapToItemBoundary` | Boolean | true | Auto-snap to nearest item when released |

### Internal Configuration

```kotlin
private val staticRadius = 450f    // Cards closer when static
private val movingRadius = 650f    // Cards spread out when moving
```

### Adjusting Visual Effects

**Scale Range:**
```kotlin
val scale = 0.6f + (normalizedZ * 0.4f)  // 0.6 to 1.0
```
- Increase first value for larger back cards
- Increase second value for more dramatic scaling

**Alpha Range:**
```kotlin
val alpha = 0.4f + (normalizedZ * 0.6f)  // 0.4 to 1.0
```
- Increase first value for less transparency at back
- Adjust second value for fade intensity

---

## 🔧 How It Works

### The Math Behind the Carousel

**1. Circular Positioning**

Cards are positioned on a circle using polar coordinates:
```
angle = currentAngle + (cardIndex × angleStep)
x = radius × sin(angle)
z = radius × cos(angle)
```

**2. Depth Perception**

Z-position determines visual properties:
- Cards with positive Z are in front (larger, opaque)
- Cards with negative Z are in back (smaller, transparent)

**3. Angle Management**

The relationship between card position and angle:
```
For card i to be centered: currentAngle + (i × angleStep) = 0
Therefore: currentAngle = -(i × angleStep)
```

**4. Shortest Path Algorithm**

When rotating from angle A to angle B:
- Calculate difference: `delta = B - A`
- Normalize to [-π, π] range
- If delta > π: subtract 2π (go counter-clockwise)
- If delta < -π: add 2π (go clockwise)

### Animation System

**Value Animator for Smooth Transitions:**
```kotlin
ValueAnimator.ofFloat(0f, 1f).apply {
    duration = 400
    interpolator = DecelerateInterpolator()
    addUpdateListener { animation ->
        val progress = animation.animatedValue as Float
        currentAngle = startAngle + delta * progress
        updateCardPositions()
    }
}
```

**Deceleration for Fling:**
```kotlin
velocity *= decelerationRate  // e.g., 0.95
currentAngle += velocity
```
Each frame reduces velocity until it reaches near-zero.

---

## 🛠️ Building the Project

### Using Gradle

```bash
# Build debug APK
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug

# Build and install
./gradlew assembleDebug installDebug
```

### From Android Studio

1. Click **Build > Make Project** (Ctrl+F9)
2. Click **Run > Run 'app'** (Shift+F10)

### Output Location

Built APK: `app/build/outputs/apk/debug/app-debug.apk`

---

## 🎨 Customization Examples

### Change Number of Cards

```kotlin
val colors = listOf(
    "#FF6B6B" to "Card 1",
    "#4ECDC4" to "Card 2",
    "#FFE66D" to "Card 3",
    "#95E1D3" to "Card 4",
    "#F38181" to "Card 5"
)

colors.forEach { (colorHex, title) ->
    carousel.addCard(createCard(title, Color.parseColor(colorHex)))
}
```

### Adjust Carousel Speed

```kotlin
carousel.decelerationRate = 0.90f  // Faster deceleration
carousel.decelerationRate = 0.98f  // Slower deceleration
```

### Modify Spacing

```kotlin
// Edit CarouselView.kt
private val staticRadius = 350f    // Tighter spacing
private val movingRadius = 550f    // Less expansion
```

### Change Card Size

```kotlin
private fun createCard(...): CardView {
    return CardView(this).apply {
        layoutParams = ViewGroup.MarginLayoutParams(600, 700)  // Larger
        // or
        layoutParams = ViewGroup.MarginLayoutParams(350, 450)  // Smaller
    }
}
```

---

## 🐛 Troubleshooting

### Cards Not Visible
- Ensure cards have non-zero dimensions
- Check if `setLayerType(LAYER_TYPE_SOFTWARE, null)` is enabled

### Jerky Animations
- Reduce `decelerationRate` for smoother stop
- Increase animation duration in `animateToAngleShortestPath()`

### Wrong Card Centers
- Verify angle calculation: `currentAngle = -(index × angleStep)`
- Check `getCurrentItemIndex()` uses negative angle

### Touch Not Responding
- Ensure parent views aren't intercepting touches
- Check `onTouchEvent()` returns true

---

## 📚 Key Learnings

### 1. Coordinate System
- Android uses Y-down coordinate system
- Z-axis positive is toward viewer
- `translationX/Y/Z` for positioning, `rotationX/Y` for rotation

### 2. Angle Normalization
- Always normalize angles to prevent overflow
- Use modulo: `angle % (2 * PI)`
- Handle negative angles properly

### 3. Touch Gestures
- Use `GestureDetector` for fling detection
- Calculate angular velocity from touch coordinates
- `atan2(y, x)` converts coordinates to angle

### 4. Animation Best Practices
- Cancel previous animations before starting new ones
- Use interpolators for natural motion
- Separate concerns (angle animation vs radius animation)

### 5. View Ordering
- `bringToFront()` changes Z-order
- Sort by depth for correct rendering
- Update after every position change

---

## 🔮 Future Enhancements

Potential improvements:
- [ ] Support for vertical carousel orientation
- [ ] Programmatic card insertion/removal with animation
- [ ] Parallax scrolling effects
- [ ] Multiple carousel rings
- [ ] Page indicator dots
- [ ] Accessibility improvements (TalkBack support)
- [ ] Save/restore carousel state
- [ ] Custom interpolators per card
- [ ] Drag sensitivity configuration

---

## 📄 License

This project is open source and available under the MIT License.

---

## 👤 Author

Created with ❤️ for Android developers who want beautiful 3D carousels.

---

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## 📞 Support

If you have questions or need help:
- Open an issue on GitHub
- Check existing documentation
- Review the code comments in `CarouselView.kt`

---

## 🙏 Acknowledgments

- Inspired by iOS iCarousel library
- Android Material Design guidelines
- Community feedback and contributions

---

**Happy Coding! 🚀**

