package edu.bu.seatscout.wear

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.InputDevice
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import edu.bu.seatscout.StudyArea
import edu.bu.seatscout.sampleAreas
import kotlin.math.roundToInt

class WatchActivity : Activity() {
    private lateinit var scroll: ScrollView
    private lateinit var column: LinearLayout
    private val bookmarks by lazy { getSharedPreferences("watch", MODE_PRIVATE) }
    private fun saved() = bookmarks.getStringSet("bookmarks", emptySet()).orEmpty()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        scroll = ScrollView(this).apply {
            id = android.R.id.list
            isFillViewport = true
            isVerticalScrollBarEnabled = false
            setBackgroundColor(Color.rgb(22, 17, 21))
            isFocusableInTouchMode = true
            setOnGenericMotionListener { _, event ->
                if (event.action == MotionEvent.ACTION_SCROLL &&
                    event.isFromSource(InputDevice.SOURCE_ROTARY_ENCODER)) {
                    val units = ViewConfiguration.get(this@WatchActivity).scaledVerticalScrollFactor
                    scrollBy(0, (-event.getAxisValue(MotionEvent.AXIS_SCROLL) * units).roundToInt())
                    true
                } else false
            }
        }
        val config = resources.configuration
        val side = dp(if (config.isScreenRound) (config.screenWidthDp * .13f).roundToInt() else 12)
        val end = dp(if (config.isScreenRound) (config.screenHeightDp * .22f).roundToInt() else 16)
        column = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(side, end, side, end)
        }
        scroll.addView(column)
        setContentView(scroll)
        scroll.requestFocus()
    }

    override fun onResume() {
        super.onResume()
        column.removeAllViews()
        val area = sampleAreas.find { it.id == intent.getStringExtra("area") }
        val campus = intent.getStringExtra("campus")
        val building = intent.getStringExtra("building")
        when {
            area != null -> details(area)
            intent.getBooleanExtra("saved", false) -> {
                title("Saved spaces")
                val spaces = sampleAreas.filter { it.id in saved() }
                if (spaces.isEmpty()) body("Save a space on this watch to find it here.")
                spaces.forEach { space -> action("${space.building}\n${space.name}") { open("area", space.id) } }
            }
            campus != null && building != null -> {
                title(building)
                sampleAreas.filter { it.campus == campus && it.building == building }
                    .forEach { space -> action(space.name) { open("area", space.id) } }
            }
            campus != null -> {
                title(campus)
                sampleAreas.filter { it.campus == campus }.groupBy { it.building }.toSortedMap()
                    .forEach { (name, spaces) ->
                        action(name) {
                            if (spaces.size == 1) open("area", spaces.first().id)
                            else startActivity(Intent(this, WatchActivity::class.java)
                                .putExtra("campus", campus).putExtra("building", name))
                        }
                    }
            }
            else -> {
                title("Seat-Scout")
                action("Saved spaces", accented = true) {
                    startActivity(Intent(this, WatchActivity::class.java).putExtra("saved", true))
                }
                sampleAreas.map { it.campus }.distinct().sorted().forEach { action(it) { open("campus", it) } }
            }
        }
        if (!isTaskRoot) action("Back") { finish() }
    }

    private fun details(area: StudyArea) {
        title(area.name)
        body(area.building)
        body(area.address)
        body("Study zone: ${area.zone}\nAccess: ${area.access}")
        val bookmark = action(saveLabel(area), accented = true) {}
        bookmark.setOnClickListener {
            val current = saved()
            val updated = if (area.id in current) current - area.id else current + area.id
            bookmarks.edit().putStringSet("bookmarks", updated).apply()
            bookmark.text = saveLabel(area)
        }
        action("Open in maps") {
            val query = Uri.encode("${area.building}, ${area.address}, Boston, MA")
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=$query")))
            } catch (_: ActivityNotFoundException) {
                Toast.makeText(this, "No maps app on this watch. Use the address above.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun saveLabel(area: StudyArea) = if (area.id in saved()) "Remove from saved" else "Save space"

    private fun open(key: String, value: String) =
        startActivity(Intent(this, WatchActivity::class.java).putExtra(key, value))

    private fun title(message: String) = column.addView(TextView(this).apply {
        text = message
        gravity = Gravity.CENTER
        setTextColor(Color.rgb(255, 245, 246))
        typeface = Typeface.SERIF
        textSize = 23f
        isAccessibilityHeading = true
        setPadding(0, 0, 0, dp(10))
    })

    private fun body(message: String) = column.addView(TextView(this).apply {
        text = message
        gravity = Gravity.CENTER
        setTextColor(Color.rgb(231, 218, 223))
        textSize = 15f
        setPadding(0, 0, 0, dp(8))
    })

    private fun action(label: String, accented: Boolean = false, click: () -> Unit): Button {
        val button = Button(this).apply {
            text = label
            isAllCaps = false
            textSize = 15f
            minimumHeight = dp(48)
            setPadding(dp(10), dp(8), dp(10), dp(8))
            setTextColor(Color.WHITE)
            backgroundTintList = null
            background = RippleDrawable(ColorStateList.valueOf(0x40FFFFFF), GradientDrawable().apply {
                cornerRadius = dp(20).toFloat()
                setColor(if (accented) Color.rgb(112, 40, 66) else Color.rgb(48, 39, 46))
                setStroke(dp(1), Color.rgb(103, 82, 95))
            }, null)
            setOnClickListener { click() }
        }
        column.addView(button, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(5) })
        return button
    }

    private fun dp(value: Int) = (resources.displayMetrics.density * value).roundToInt()
}
