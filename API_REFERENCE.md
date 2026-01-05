# CarouselView API Reference

Complete API documentation for the `CarouselView` class.

---

## 📦 Package

```kotlin
package com.example.rotatorycarousel.ui.carousel
```

---

## 🎯 Class Overview

```kotlin
class CarouselView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr)
```

A custom view that displays child views in a 3D circular carousel arrangement with smooth rotation and animations.

**Inherits from**: `FrameLayout`

---

## 🔧 Public Properties

### `itemCount: Int` (read-only)

```kotlin
var itemCount = 0
    private set
```

**Description**: The current number of items in the carousel.

**Type**: `Int`

**Access**: Read-only (automatically updated when cards are added/removed)

**Example**:
```kotlin
val count = carousel.itemCount
Log.d("Carousel", "Total cards: $count")
```

---

### `radius: Float`

```kotlin
var radius = 450f
```

**Description**: The base radius of the circular carousel path. Larger values spread cards further apart.

**Type**: `Float`

**Default**: `450f`

**Range**: Recommended 300-800

**Example**:
```kotlin
carousel.radius = 600f  // Larger circle
```

**Note**: This is the base radius. The actual radius animates between `staticRadius` and `movingRadius` during interaction.

---

### `perspective: Float`

```kotlin
var perspective = -0.0015f
```

**Description**: 3D perspective effect strength. More negative values increase perspective distortion.

**Type**: `Float`

**Default**: `-0.0015f`

**Range**: -0.003 to 0

**Example**:
```kotlin
carousel.perspective = -0.002f  // More dramatic 3D effect
```

**Note**: Currently not actively used in the implementation but reserved for future enhancements.

---

### `decelerationRate: Float`

```kotlin
var decelerationRate = 0.95f
```

**Description**: Controls how quickly fling velocity decreases. Higher values = slower deceleration.

**Type**: `Float`

**Default**: `0.95f`

**Range**: 0.0 to 1.0

**Example**:
```kotlin
carousel.decelerationRate = 0.90f  // Stops faster
carousel.decelerationRate = 0.98f  // Continues longer
```

**Physics**: Each frame, `velocity *= decelerationRate`

---

### `bounces: Boolean`

```kotlin
var bounces = false
```

**Description**: Whether the carousel should bounce at boundaries (not fully implemented).

**Type**: `Boolean`

**Default**: `false`

**Example**:
```kotlin
carousel.bounces = true
```

**Note**: Reserved for future boundary behavior.

---

### `wrapEnabled: Boolean`

```kotlin
var wrapEnabled = true
```

**Description**: Enables infinite circular wrapping. When true, carousel loops continuously.

**Type**: `Boolean`

**Default**: `true`

**Example**:
```kotlin
carousel.wrapEnabled = false  // Disable infinite loop
```

---

### `snapToItemBoundary: Boolean`

```kotlin
var snapToItemBoundary = true
```

**Description**: When true, carousel automatically snaps to the nearest card when released or after fling.

**Type**: `Boolean`

**Default**: `true`

**Example**:
```kotlin
carousel.snapToItemBoundary = false  // Free rotation
```

---

### `onItemClickListener: OnItemClickListener?`

```kotlin
var onItemClickListener: OnItemClickListener? = null
```

**Description**: Callback interface for handling card click events.

**Type**: `OnItemClickListener?` (nullable)

**Default**: `null`

**Interface**:
```kotlin
interface OnItemClickListener {
    fun onItemClick(view: View, index: Int)
}
```

**Example**:
```kotlin
carousel.onItemClickListener = object : CarouselView.OnItemClickListener {
    override fun onItemClick(view: View, index: Int) {
        Toast.makeText(context, "Clicked $index", Toast.LENGTH_SHORT).show()
    }
}
```

**Behavior**: Only triggered when a card that is already centered is tapped again.

---

## 🎬 Public Methods

### `addCard(view: View)`

```kotlin
fun addCard(view: View)
```

**Description**: Adds a card view to the carousel.

**Parameters**:
- `view: View` - The view to add as a card

**Returns**: `Unit`

**Side Effects**:
- Adds view to carousel
- Increments `itemCount`
- Updates card positions

**Example**:
```kotlin
val card = CardView(context).apply {
    layoutParams = ViewGroup.MarginLayoutParams(450, 550)
    // ... customize card
}
carousel.addCard(card)
```

**Note**: Cards are added in order. First card added is index 0.

---

### `clearCards()`

```kotlin
fun clearCards()
```

**Description**: Removes all cards from the carousel.

**Parameters**: None

**Returns**: `Unit`

**Side Effects**:
- Removes all child views
- Resets `itemCount` to 0
- Clears internal card list

**Example**:
```kotlin
carousel.clearCards()
// Add new set of cards
carousel.addCard(newCard1)
carousel.addCard(newCard2)
```

---

### `getCurrentItemIndex(): Int`

```kotlin
fun getCurrentItemIndex(): Int
```

**Description**: Returns the index of the card currently centered in the carousel.

**Parameters**: None

**Returns**: `Int` - Index of centered card, or -1 if no cards

**Example**:
```kotlin
val currentIndex = carousel.getCurrentItemIndex()
Log.d("Carousel", "Centered card: $currentIndex")
```

**Algorithm**:
```kotlin
val normalizedAngle = (currentAngle % (2 * PI)).toFloat()
val index = Math.round(-normalizedAngle / angleStep).toInt() % itemCount
return if (index < 0) index + itemCount else index
```

---

### `scrollToItem(index: Int, animated: Boolean = true)`

```kotlin
fun scrollToItem(index: Int, animated: Boolean = true)
```

**Description**: Scrolls the carousel to center a specific card.

**Parameters**:
- `index: Int` - The index of the card to center (0-based)
- `animated: Boolean` - Whether to animate the transition (default: `true`)

**Returns**: `Unit`

**Example**:
```kotlin
// Scroll to card 2 with animation
carousel.scrollToItem(2)

// Jump to card 0 instantly
carousel.scrollToItem(0, animated = false)
```

**Behavior**: 
- Uses shortest path rotation
- Triggers radius collapse animation
- Respects `animated` parameter

---

### `setItemCount(count: Int)`

```kotlin
fun setItemCount(count: Int)
```

**Description**: Manually sets the number of items (rarely needed).

**Parameters**:
- `count: Int` - The number of items

**Returns**: `Unit`

**Example**:
```kotlin
carousel.setItemCount(5)
```

**Note**: Normally you don't need this - `addCard()` automatically updates the count.

---

## 🎨 OnItemClickListener Interface

```kotlin
interface OnItemClickListener {
    fun onItemClick(view: View, index: Int)
}
```

### Method: `onItemClick`

```kotlin
fun onItemClick(view: View, index: Int)
```

**Description**: Called when a centered card is tapped.

**Parameters**:
- `view: View` - The card view that was clicked
- `index: Int` - The index of the clicked card

**Example Implementation**:
```kotlin
carousel.onItemClickListener = object : CarouselView.OnItemClickListener {
    override fun onItemClick(view: View, index: Int) {
        // Handle click
        if (view is CardView) {
            // Do something with the card
        }
    }
}
```

**Trigger Conditions**:
1. Card is tapped
2. Card is already centered (at index 0 position)
3. If card is not centered, it will animate to center instead of triggering callback

---

## 📋 Usage Examples

### Basic Setup

```kotlin
val carousel = CarouselView(context).apply {
    layoutParams = ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT
    )
}

// Add cards
for (i in 0..4) {
    val card = createCard("Item $i")
    carousel.addCard(card)
}

setContentView(carousel)
```

---

### Custom Configuration

```kotlin
carousel.apply {
    radius = 500f
    decelerationRate = 0.92f
    snapToItemBoundary = true
    wrapEnabled = true
}
```

---

### Handling Clicks

```kotlin
carousel.onItemClickListener = object : CarouselView.OnItemClickListener {
    override fun onItemClick(view: View, index: Int) {
        when (index) {
            0 -> handleFirstCard()
            1 -> handleSecondCard()
            else -> handleOtherCard(index)
        }
    }
}
```

---

### Programmatic Navigation

```kotlin
// Navigate to next card
val current = carousel.getCurrentItemIndex()
val next = (current + 1) % carousel.itemCount
carousel.scrollToItem(next)

// Navigate to previous card
val prev = if (current == 0) carousel.itemCount - 1 else current - 1
carousel.scrollToItem(prev)

// Jump to first card
carousel.scrollToItem(0, animated = false)
```

---

### Dynamic Card Management

```kotlin
// Replace all cards
carousel.clearCards()

val newData = listOf("A", "B", "C", "D")
newData.forEach { item ->
    val card = createCard(item)
    carousel.addCard(card)
}

// Scroll to first new card
carousel.scrollToItem(0)
```

---

## 🎯 Integration Patterns

### In an Activity

```kotlin
class MyActivity : AppCompatActivity() {
    private lateinit var carousel: CarouselView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        carousel = CarouselView(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            onItemClickListener = object : CarouselView.OnItemClickListener {
                override fun onItemClick(view: View, index: Int) {
                    handleCardClick(index)
                }
            }
        }
        
        loadCards()
        setContentView(carousel)
    }
    
    private fun loadCards() {
        // Add your cards
    }
    
    private fun handleCardClick(index: Int) {
        // Handle click
    }
}
```

---

### In a Fragment

```kotlin
class MyFragment : Fragment() {
    private var carousel: CarouselView? = null
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return CarouselView(requireContext()).apply {
            carousel = this
            // Configure
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        carousel = null
    }
}
```

---

### With Data Binding

```kotlin
class CarouselAdapter(private val carousel: CarouselView) {
    
    fun submitList(items: List<ItemData>) {
        carousel.clearCards()
        items.forEach { item ->
            val card = createCardFromData(item)
            carousel.addCard(card)
        }
    }
    
    private fun createCardFromData(item: ItemData): View {
        return CardView(carousel.context).apply {
            // Bind data to view
        }
    }
}
```

---

## ⚠️ Important Notes

### Thread Safety
- All methods must be called on the UI thread
- Use `runOnUiThread` if calling from background thread

### Memory Management
- Clear cards when view is destroyed
- Avoid memory leaks with click listeners

```kotlin
override fun onDestroy() {
    carousel.onItemClickListener = null
    carousel.clearCards()
    super.onDestroy()
}
```

### Performance
- Limit to ~20 cards for optimal performance
- Use simple card layouts
- Avoid heavy view hierarchies

### Layout
- Works best in full-screen layouts
- Needs sufficient space for circular arrangement
- Minimum recommended size: 500x500dp

---

## 🐛 Common Issues

### Cards Not Showing

**Problem**: Added cards but nothing appears

**Solution**:
```kotlin
// Ensure cards have non-zero size
card.layoutParams = ViewGroup.MarginLayoutParams(450, 550)

// Or use wrap_content with content inside
card.layoutParams = ViewGroup.MarginLayoutParams(
    ViewGroup.LayoutParams.WRAP_CONTENT,
    ViewGroup.LayoutParams.WRAP_CONTENT
)
```

---

### Clicks Not Working

**Problem**: `onItemClick` not called

**Solution**:
- Ensure card is already centered (tap once to center, tap again to trigger)
- Check listener is set before interaction
- Verify touch isn't blocked by parent views

---

### Jerky Animation

**Problem**: Animation stutters

**Solution**:
```kotlin
// Reduce deceleration rate
carousel.decelerationRate = 0.90f

// Simplify card layouts
// Use hardware acceleration
carousel.setLayerType(View.LAYER_TYPE_HARDWARE, null)
```

---

### Wrong Card Centered

**Problem**: `getCurrentItemIndex()` returns unexpected value

**Solution**:
- Check if cards added in correct order
- Verify no manual angle modifications
- Ensure `scrollToItem()` completes before checking

---

## 📊 Property Comparison Table

| Property | Type | Default | Range | Purpose |
|----------|------|---------|-------|---------|
| `itemCount` | Int | 0 | 0+ | Number of cards (read-only) |
| `radius` | Float | 450f | 300-800 | Circle size |
| `perspective` | Float | -0.0015f | -0.003 to 0 | 3D depth effect |
| `decelerationRate` | Float | 0.95f | 0.0-1.0 | Fling slowdown |
| `bounces` | Boolean | false | true/false | Boundary bounce |
| `wrapEnabled` | Boolean | true | true/false | Infinite loop |
| `snapToItemBoundary` | Boolean | true | true/false | Auto-snap |

---

## 🔗 Related Classes

- `FrameLayout` - Parent class
- `View` - Base class for cards
- `CardView` - Commonly used for cards
- `ValueAnimator` - Used internally for animations
- `GestureDetector` - Used internally for touch handling

---

## 📚 See Also

- [README.md](README.md) - Project overview and quick start
- [TECHNICAL_GUIDE.md](TECHNICAL_GUIDE.md) - In-depth technical details
- [Android Custom Views](https://developer.android.com/guide/topics/ui/custom-components)

---

**End of API Reference**

