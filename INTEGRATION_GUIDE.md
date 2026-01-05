# Integration Guide

Step-by-step guide to integrate the 3D Rotary Carousel into your Android project.

---

## 🚀 Quick Integration (5 Minutes)

### Step 1: Copy the Source File

Copy `CarouselView.kt` to your project:

```
YourProject/
└── app/
    └── src/
        └── main/
            └── java/
                └── com/
                    └── yourpackage/
                        └── ui/
                            └── carousel/
                                └── CarouselView.kt  ← Copy here
```

Update the package declaration in `CarouselView.kt`:
```kotlin
package com.yourpackage.ui.carousel
```

---

### Step 2: Add Dependencies

In your `app/build.gradle.kts`:

```kotlin
dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.cardview:cardview:1.0.0")
}
```

---

### Step 3: Create Your Activity/Fragment

**Option A: In an Activity**

```kotlin
package com.yourpackage

import android.os.Bundle
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.yourpackage.ui.carousel.CarouselView

class YourActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val carousel = CarouselView(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        
        // Add your cards
        addCards(carousel)
        
        setContentView(carousel)
    }
    
    private fun addCards(carousel: CarouselView) {
        // See Step 4
    }
}
```

**Option B: In a Fragment**

```kotlin
package com.yourpackage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.yourpackage.ui.carousel.CarouselView

class YourFragment : Fragment() {
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val carousel = CarouselView(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        
        addCards(carousel)
        
        return carousel
    }
    
    private fun addCards(carousel: CarouselView) {
        // See Step 4
    }
}
```

---

### Step 4: Create and Add Cards

```kotlin
private fun addCards(carousel: CarouselView) {
    val items = listOf(
        "#FF6B6B" to "Item 1",
        "#4ECDC4" to "Item 2",
        "#FFE66D" to "Item 3"
    )
    
    items.forEach { (color, title) ->
        val card = createCard(title, android.graphics.Color.parseColor(color))
        carousel.addCard(card)
    }
}

private fun createCard(title: String, backgroundColor: Int): androidx.cardview.widget.CardView {
    return androidx.cardview.widget.CardView(this).apply {
        layoutParams = ViewGroup.MarginLayoutParams(450, 550)
        radius = 40f
        cardElevation = 24f
        setCardBackgroundColor(backgroundColor)
        
        // Add TextView
        val textView = android.widget.TextView(context).apply {
            text = title
            textSize = 24f
            setTextColor(android.graphics.Color.WHITE)
            gravity = android.view.Gravity.CENTER
            setPadding(32, 32, 32, 32)
        }
        
        addView(textView)
    }
}
```

---

### Step 5: Run Your App

Build and run! You should now see the 3D carousel.

---

## 🎨 Customization Options

### Change Card Appearance

```kotlin
private fun createCard(title: String, backgroundColor: Int): CardView {
    return CardView(this).apply {
        // Size
        layoutParams = ViewGroup.MarginLayoutParams(600, 700)  // Larger cards
        
        // Corners
        radius = 60f  // More rounded
        
        // Shadow
        cardElevation = 32f  // More prominent shadow
        maxCardElevation = 40f
        
        // Background
        setCardBackgroundColor(backgroundColor)
        
        // Add image + text
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            
            val image = ImageView(context).apply {
                setImageResource(R.drawable.your_image)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    400
                )
            }
            addView(image)
            
            val text = TextView(context).apply {
                text = title
                textSize = 20f
                setPadding(24, 24, 24, 24)
            }
            addView(text)
        }
        
        addView(layout)
    }
}
```

---

### Configure Carousel Behavior

```kotlin
val carousel = CarouselView(this).apply {
    // Adjust circle size
    radius = 600f  // Larger circle
    
    // Adjust rotation speed
    decelerationRate = 0.90f  // Faster stop
    // or
    decelerationRate = 0.98f  // Slower, longer spin
    
    // Enable/disable auto-snap
    snapToItemBoundary = true  // Snap to cards
    // or
    snapToItemBoundary = false  // Free rotation
    
    // Background color
    setBackgroundColor(android.graphics.Color.parseColor("#F0F0F0"))
}
```

---

### Handle Card Clicks

```kotlin
carousel.onItemClickListener = object : CarouselView.OnItemClickListener {
    override fun onItemClick(view: View, index: Int) {
        // Navigate to detail screen
        val intent = Intent(this@YourActivity, DetailActivity::class.java)
        intent.putExtra("index", index)
        startActivity(intent)
        
        // Or show dialog
        AlertDialog.Builder(this@YourActivity)
            .setTitle("Card $index")
            .setMessage("You clicked card at index $index")
            .show()
        
        // Or update UI
        textView.text = "Selected: $index"
    }
}
```

---

## 🔄 Loading Data Dynamically

### From Network API

```kotlin
class YourActivity : AppCompatActivity() {
    private lateinit var carousel: CarouselView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        carousel = CarouselView(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        
        setContentView(carousel)
        loadDataFromNetwork()
    }
    
    private fun loadDataFromNetwork() {
        // Show loading
        showLoading()
        
        // Fetch data (use your preferred networking library)
        yourApiService.getItems { items ->
            runOnUiThread {
                hideLoading()
                populateCarousel(items)
            }
        }
    }
    
    private fun populateCarousel(items: List<YourDataModel>) {
        carousel.clearCards()
        
        items.forEach { item ->
            val card = createCardFromModel(item)
            carousel.addCard(card)
        }
    }
    
    private fun createCardFromModel(item: YourDataModel): CardView {
        return CardView(this).apply {
            // Bind data to view
            layoutParams = ViewGroup.MarginLayoutParams(450, 550)
            
            val textView = TextView(context).apply {
                text = item.title
                // ... more binding
            }
            
            addView(textView)
        }
    }
}
```

---

### From Database (Room)

```kotlin
class YourViewModel : ViewModel() {
    val items: LiveData<List<YourEntity>> = repository.getAllItems()
}

class YourActivity : AppCompatActivity() {
    private lateinit var carousel: CarouselView
    private lateinit var viewModel: YourViewModel
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        carousel = CarouselView(this)
        setContentView(carousel)
        
        viewModel = ViewModelProvider(this).get(YourViewModel::class.java)
        
        viewModel.items.observe(this) { items ->
            updateCarousel(items)
        }
    }
    
    private fun updateCarousel(items: List<YourEntity>) {
        carousel.clearCards()
        items.forEach { item ->
            val card = createCardFromEntity(item)
            carousel.addCard(card)
        }
    }
}
```

---

## 🎯 Advanced Use Cases

### Programmatic Navigation

```kotlin
// Navigate to next card
fun goToNext() {
    val current = carousel.getCurrentItemIndex()
    val next = (current + 1) % carousel.itemCount
    carousel.scrollToItem(next)
}

// Navigate to previous card
fun goToPrevious() {
    val current = carousel.getCurrentItemIndex()
    val prev = if (current == 0) carousel.itemCount - 1 else current - 1
    carousel.scrollToItem(prev)
}

// Add navigation buttons
val btnNext = findViewById<Button>(R.id.btnNext)
val btnPrev = findViewById<Button>(R.id.btnPrevious)

btnNext.setOnClickListener { goToNext() }
btnPrev.setOnClickListener { goToPrevious() }
```

---

### Auto-Play Carousel

```kotlin
class AutoPlayCarousel : AppCompatActivity() {
    private lateinit var carousel: CarouselView
    private val handler = Handler(Looper.getMainLooper())
    private var autoPlayRunnable: Runnable? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        carousel = CarouselView(this)
        // ... setup carousel
        
        startAutoPlay()
    }
    
    private fun startAutoPlay() {
        autoPlayRunnable = object : Runnable {
            override fun run() {
                val current = carousel.getCurrentItemIndex()
                val next = (current + 1) % carousel.itemCount
                carousel.scrollToItem(next)
                
                handler.postDelayed(this, 3000) // Every 3 seconds
            }
        }
        handler.postDelayed(autoPlayRunnable!!, 3000)
    }
    
    private fun stopAutoPlay() {
        autoPlayRunnable?.let { handler.removeCallbacks(it) }
    }
    
    override fun onPause() {
        super.onPause()
        stopAutoPlay()
    }
    
    override fun onResume() {
        super.onResume()
        startAutoPlay()
    }
}
```

---

### Page Indicator

```kotlin
class CarouselWithIndicator : AppCompatActivity() {
    private lateinit var carousel: CarouselView
    private lateinit var indicatorLayout: LinearLayout
    private val indicators = mutableListOf<View>()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        
        carousel = CarouselView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f  // Weight
            )
        }
        mainLayout.addView(carousel)
        
        indicatorLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                100
            )
        }
        mainLayout.addView(indicatorLayout)
        
        setContentView(mainLayout)
        
        addCardsAndIndicators()
        setupIndicatorUpdates()
    }
    
    private fun addCardsAndIndicators() {
        val items = listOf("Item 1", "Item 2", "Item 3")
        
        items.forEach { title ->
            carousel.addCard(createCard(title))
            
            val indicator = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(24, 24).apply {
                    setMargins(8, 0, 8, 0)
                }
                setBackgroundResource(R.drawable.indicator_inactive)
            }
            indicatorLayout.addView(indicator)
            indicators.add(indicator)
        }
        
        updateIndicators(0)
    }
    
    private fun setupIndicatorUpdates() {
        // Update indicators when carousel moves
        // You may need to add a listener to CarouselView for this
        carousel.viewTreeObserver.addOnGlobalLayoutListener {
            val currentIndex = carousel.getCurrentItemIndex()
            if (currentIndex >= 0) {
                updateIndicators(currentIndex)
            }
        }
    }
    
    private fun updateIndicators(activeIndex: Int) {
        indicators.forEachIndexed { index, indicator ->
            indicator.setBackgroundResource(
                if (index == activeIndex) R.drawable.indicator_active
                else R.drawable.indicator_inactive
            )
        }
    }
}
```

---

### Custom Card Animation

```kotlin
carousel.onItemClickListener = object : CarouselView.OnItemClickListener {
    override fun onItemClick(view: View, index: Int) {
        // Pulse animation
        view.animate()
            .scaleX(1.1f)
            .scaleY(1.1f)
            .setDuration(200)
            .withEndAction {
                view.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(200)
                    .start()
            }
            .start()
    }
}
```

---

## 📱 Screen Orientation

Handle configuration changes:

```kotlin
class YourActivity : AppCompatActivity() {
    private lateinit var carousel: CarouselView
    private var currentIndex = 0
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        carousel = CarouselView(this)
        addCards(carousel)
        
        // Restore position
        savedInstanceState?.let {
            currentIndex = it.getInt("currentIndex", 0)
            carousel.scrollToItem(currentIndex, animated = false)
        }
        
        setContentView(carousel)
    }
    
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        currentIndex = carousel.getCurrentItemIndex()
        outState.putInt("currentIndex", currentIndex)
    }
}
```

---

## 🎨 Theming

Create themed cards:

```kotlin
private fun createThemedCard(
    title: String,
    isDarkTheme: Boolean
): CardView {
    return CardView(this).apply {
        layoutParams = ViewGroup.MarginLayoutParams(450, 550)
        radius = 40f
        cardElevation = 24f
        
        if (isDarkTheme) {
            setCardBackgroundColor(Color.parseColor("#2C2C2C"))
        } else {
            setCardBackgroundColor(Color.WHITE)
        }
        
        val textView = TextView(context).apply {
            text = title
            textSize = 24f
            setTextColor(if (isDarkTheme) Color.WHITE else Color.BLACK)
            gravity = Gravity.CENTER
        }
        
        addView(textView)
    }
}
```

---

## ⚡ Performance Tips

### 1. Reuse Cards (If Possible)

```kotlin
// Instead of recreating, update existing cards
private fun updateCardContent(card: View, newData: YourData) {
    (card as? CardView)?.let {
        val textView = it.getChildAt(0) as? TextView
        textView?.text = newData.title
    }
}
```

### 2. Lazy Load Images

```kotlin
private fun createCard(item: YourData): CardView {
    return CardView(this).apply {
        // ... setup
        
        val imageView = ImageView(context)
        
        // Use Glide, Picasso, or Coil
        Glide.with(context)
            .load(item.imageUrl)
            .placeholder(R.drawable.placeholder)
            .into(imageView)
        
        addView(imageView)
    }
}
```

### 3. Limit Card Count

```kotlin
// For large datasets, show only a subset
private fun addCards(carousel: CarouselView, allItems: List<Item>) {
    val maxCards = 10
    val itemsToShow = allItems.take(maxCards)
    
    itemsToShow.forEach { item ->
        carousel.addCard(createCard(item))
    }
}
```

---

## 🐛 Troubleshooting

### Issue: Cards overlap incorrectly

**Solution**: Ensure proper Z-ordering in CarouselView

### Issue: Touch events not working

**Solution**: Check parent views aren't intercepting touches
```kotlin
parentLayout.requestDisallowInterceptTouchEvent(true)
```

### Issue: Animation stutters

**Solution**: Reduce card complexity or adjust deceleration
```kotlin
carousel.decelerationRate = 0.92f
```

---

## 📦 Complete Example Project Structure

```
YourApp/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/yourpackage/
│   │       │   ├── ui/
│   │       │   │   └── carousel/
│   │       │   │       └── CarouselView.kt
│   │       │   ├── activities/
│   │       │   │   └── MainActivity.kt
│   │       │   ├── models/
│   │       │   │   └── YourDataModel.kt
│   │       │   └── utils/
│   │       │       └── CardFactory.kt
│   │       └── res/
│   │           ├── drawable/
│   │           ├── layout/
│   │           └── values/
│   └── build.gradle.kts
└── build.gradle.kts
```

---

## ✅ Integration Checklist

- [ ] Copied `CarouselView.kt` to project
- [ ] Updated package declaration
- [ ] Added required dependencies
- [ ] Created activity/fragment
- [ ] Implemented `createCard()` method
- [ ] Added cards to carousel
- [ ] Configured carousel properties
- [ ] Added click listener (optional)
- [ ] Tested on device/emulator
- [ ] Handled configuration changes
- [ ] Added error handling

---

## 🎓 Next Steps

1. Read [API_REFERENCE.md](API_REFERENCE.md) for complete API documentation
2. Check [TECHNICAL_GUIDE.md](TECHNICAL_GUIDE.md) for implementation details
3. Customize visual appearance to match your app theme
4. Add analytics/tracking to card interactions
5. Consider adding accessibility features

---

## 💡 Tips & Best Practices

1. **Keep cards simple** - Complex layouts may impact performance
2. **Test on real devices** - Emulators may not show smooth animations
3. **Handle edge cases** - Empty states, single card, etc.
4. **Use appropriate card sizes** - 400-600dp width works well
5. **Provide visual feedback** - Highlight selected/centered card
6. **Consider accessibility** - Add content descriptions
7. **Test rotation** - Ensure state is preserved
8. **Optimize images** - Use appropriate resolutions
9. **Monitor memory** - Release resources when done
10. **Test touch interactions** - Ensure all gestures work smoothly

---

**Happy Integrating! 🎉**

