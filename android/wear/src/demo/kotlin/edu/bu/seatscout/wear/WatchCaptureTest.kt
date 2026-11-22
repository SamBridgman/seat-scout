package edu.bu.seatscout.wear

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ScrollView
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [30], qualifiers = "w210dp-h210dp-round-watch-xhdpi")
class WatchCaptureTest {
    @Test fun navigateAndBookmarkStudySpace() {
        val home = launch(null)
        capture(home, "01-home")
        val campus = next(home, "Medical")
        capture(campus, "02-campus")
        val library = next(campus, "Alumni Medical Library")
        val details = next(library, "L12 Quiet Study")
        capture(details, "03-detail")
        val save = button(details, "Save space")
        save.performClick()
        assertTrue(save.text == "Remove from saved")
        capture(details, "04-bookmark", save)
        val saved = next(home, "Saved spaces")
        val entry = button(saved, "Alumni Medical Library\nL12 Quiet Study")
        capture(saved, "05-saved", entry)
        val returned = next(saved, entry.text.toString())
        button(returned, "Open in maps").performClick()
        val destination = shadowOf(returned).nextStartedActivity
        assertTrue(destination?.dataString?.contains("72%20East%20Concord") == true)
    }

    private fun launch(intent: Intent?): WatchActivity =
        Robolectric.buildActivity(WatchActivity::class.java, intent).setup().get()

    private fun next(from: WatchActivity, label: String): WatchActivity {
        assertTrue(button(from, label).performClick())
        return launch(requireNotNull(shadowOf(from).nextStartedActivity))
    }

    private fun button(activity: WatchActivity, label: String): Button {
        val list = activity.findViewById<ScrollView>(android.R.id.list).getChildAt(0) as ViewGroup
        return (0 until list.childCount).map { list.getChildAt(it) }.filterIsInstance<Button>()
            .first { it.text == label }
    }

    private fun capture(activity: WatchActivity, name: String, center: View? = null) {
        val scroll = activity.findViewById<ScrollView>(android.R.id.list)
        val size = 420
        val spec = View.MeasureSpec.makeMeasureSpec(size, View.MeasureSpec.EXACTLY)
        scroll.measure(spec, spec)
        scroll.layout(0, 0, size, size)
        if (center != null) {
            val bounds = Rect(0, 0, center.width, center.height)
            scroll.offsetDescendantRectToMyCoords(center, bounds)
            scroll.scrollTo(0, bounds.centerY() - size / 2)
        }
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.clipPath(Path().apply { addCircle(size/2f, size/2f, size/2f, Path.Direction.CW) })
        scroll.draw(canvas)
        val directory = File(System.getProperty("seatscout.demo"))
        directory.mkdirs()
        File(directory, "wear-$name.png").outputStream().use { stream ->
            assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream))
        }
        bitmap.recycle()
    }
}
