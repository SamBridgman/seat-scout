package edu.bu.seatscout

import android.graphics.Bitmap
import android.graphics.Canvas
import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.semantics.SemanticsActions
import org.robolectric.Shadows.shadowOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import java.io.File
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [30], qualifiers = "w393dp-h810dp-port-xhdpi")
class PhoneCaptureTest {
    @get:Rule val ui = createAndroidComposeRule<MainActivity>()

    @Test fun scrollAndSearchCatalog() {
        val page = ui.onNode(hasScrollAction() and hasAnyDescendant(hasText("Around campus")))
        for (frame in 0 until 24) {
            page.performSemanticsAction(SemanticsActions.ScrollBy) { scroll -> scroll(0f, 33f) }
            capture("scroll-" + frame.toString().padStart(2, '0'))
        }
        val field = ui.onNode(hasSetTextAction())
        field.performScrollTo().performTextInput("Stone")
        ui.onNodeWithText("Stone Science Library").assertExists()
        capture("search")
    }

    @Test fun campusWalkthrough() {
        capture("01-discover")
        ui.onNodeWithText("Map", useUnmergedTree = true).performClick()
        capture("02-map")
        ui.onNodeWithText("Medical", useUnmergedTree = true).performScrollTo().performClick()
        capture("03-medical")
        ui.onNodeWithText("L12 Quiet Study", useUnmergedTree = true).performScrollTo().performClick()
        capture("04-space")
        ui.onNodeWithContentDescription("Save this space").performClick()
        ui.onNodeWithText("Update conditions").performScrollTo().performClick()
        ui.onNodeWithText("Few seats available").performClick()
        capture("seating")
        ui.onNodeWithText("Continue").performClick()
        ui.onNodeWithText("Quiet").performClick()
        capture("sound")
        ui.onNodeWithText("Continue").performClick()
        capture("05-review")
        ui.onNodeWithText("Save report").performClick()
        ui.waitUntil(10_000) { ui.onAllNodesWithText("Report saved on this device.").fetchSemanticsNodes().isNotEmpty() }
        ui.waitUntil(10_000) { ui.onAllNodesWithText("Updated just now on this device").fetchSemanticsNodes().isNotEmpty() }
        capture("saved-report")
        ui.onNodeWithContentDescription("Back").performClick()
        ui.onNodeWithText("Saved", useUnmergedTree = true).performClick()
        ui.onNodeWithText("L12 Quiet Study").assertExists()
        capture("bookmarks")
    }

    @Test
    @Config(application = KnownSignApplication::class)
    fun recognizedSignOpensCampusPin() {
        ui.onNodeWithText("Find a place from a photo").performClick()
        ui.activityRule.scenario.onActivity { activity ->
            val picker = requireNotNull(shadowOf(activity).nextStartedActivityForResult)
            val photo = Uri.parse("content://test.campus/selected-sign")
            shadowOf(activity).receiveResult(picker.intent, Activity.RESULT_OK, Intent().setData(photo))
        }
        ui.waitUntil(10_000) {
            ui.onAllNodesWithText("Choose a location to see its map pin.").fetchSemanticsNodes().isNotEmpty()
        }
        capture("photo-01-results")
        ui.onNode(hasText("Mugar Memorial Library") and hasText("PAL Study Lounge") and hasClickAction()).performClick()
        ui.onNodeWithText("Open PAL Study Lounge").assertExists()
        capture("photo-02-pin")
        ui.onNodeWithText("Open PAL Study Lounge").performClick()
        ui.onNodeWithText("Observed conditions").assertExists()
    }

    private fun capture(name: String) {
        ui.waitForIdle()
        val directory = File(requireNotNull(System.getProperty("seatscout.demo"))).apply { mkdirs() }
        val image = ui.runOnIdle {
            val view = ui.activity.window.decorView
            Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888).also { view.draw(Canvas(it)) }
        }
        File(directory, "phone-$name.png").outputStream().use {
            check(image.compress(Bitmap.CompressFormat.PNG, 100, it))
        }
    }
}

// Only the native OCR output is fixed. The picker callback, ranking, navigation and map use app code.
class KnownSignApplication : SeatScoutApp() {
    override suspend fun readSign(uri: Uri): String {
        check(uri.toString() == "content://test.campus/selected-sign")
        return "PAL Study Lounge"
    }
}
