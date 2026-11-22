package edu.bu.seatscout

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.max

@Composable
internal fun PhotoLocationFinder(areas: List<StudyArea>, onMap: (PhotoPlace) -> Unit) {
    val app = LocalContext.current.applicationContext as SeatScoutApp
    val scope = rememberCoroutineScope()
    var busy by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("") }
    var matches by remember { mutableStateOf(emptyList<PhotoPlace>()) }
    val picker = rememberLauncherForActivityResult(PickVisualMedia()) { uri ->
        if (uri != null) {
            busy = true
            matches = emptyList()
            status = "Reading the photo on this device…"
            scope.launch {
                try {
                    matches = withContext(Dispatchers.IO) {
                        matchPhotoLocations(app.readSign(uri), areas)
                    }
                    status = if (matches.isEmpty()) "No campus label matched. Try a clearer building name or street address."
                        else "Choose a location to see its map pin."
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: Exception) {
                    status = "Couldn’t read this photo. Try a smaller, clear image of a campus sign."
                } finally {
                    busy = false
                }
            }
        }
    }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        OutlinedButton(
            modifier = Modifier.fillMaxWidth(), enabled = !busy,
            onClick = {
                try { picker.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly)) }
                catch (_: android.content.ActivityNotFoundException) { status = "Photo selection is unavailable on this device." }
            }
        ) { Text("Find a place from a photo") }
        Text("Choose a photo of a building sign or labeled campus map.", style = MaterialTheme.typography.bodySmall)
        if (status.isNotEmpty()) Text(status, Modifier.semantics { liveRegion = LiveRegionMode.Polite })
        matches.forEach { match ->
            TextButton(onClick = { onMap(match) }) {
                Column(Modifier.fillMaxWidth()) {
                    Text(match.area.building)
                    Text(if (match.roomNamed) match.area.name else match.area.address,
                        style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

private const val MAX_BYTES = 12 * 1024 * 1024
private const val MAX_SIDE = 1920

internal suspend fun readCampusSign(resolver: ContentResolver, uri: Uri): String {
    require(uri.scheme == ContentResolver.SCHEME_CONTENT)
    val bytes = resolver.openInputStream(uri)?.use { input ->
        val buffer = ByteArray(32 * 1024)
        val output = ByteArrayOutputStream()
        while (true) {
            currentCoroutineContext().ensureActive()
            val count = input.read(buffer)
            if (count < 0) break
            require(count > 0 && output.size() + count <= MAX_BYTES)
            output.write(buffer, 0, count)
        }
        output.toByteArray()
    } ?: error("Unable to open image")
    currentCoroutineContext().ensureActive()
    val bitmap = decodePhoto(bytes)
    try {
        currentCoroutineContext().ensureActive()
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        return try {
            Tasks.await(recognizer.process(InputImage.fromBitmap(bitmap, 0))).text
        } finally {
            recognizer.close()
        }
    } finally {
        bitmap.recycle()
    }
}

private fun decodePhoto(bytes: ByteArray): Bitmap {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
    require(bounds.outWidth in 1..32_768 && bounds.outHeight in 1..32_768)
    return if (Build.VERSION.SDK_INT >= 28) {
        ImageDecoder.decodeBitmap(ImageDecoder.createSource(ByteBuffer.wrap(bytes))) { decoder, info, _ ->
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            val longest = max(info.size.width, info.size.height)
            if (longest > MAX_SIDE) decoder.setTargetSize(
                max(1, info.size.width * MAX_SIDE / longest), max(1, info.size.height * MAX_SIDE / longest)
            )
        }
    } else {
        val options = BitmapFactory.Options().apply {
            while (max(bounds.outWidth, bounds.outHeight) / inSampleSize.coerceAtLeast(1) > MAX_SIDE)
                inSampleSize = inSampleSize.coerceAtLeast(1) * 2
        }
        val decoded = requireNotNull(BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options))
        val orientation = if (Build.VERSION.SDK_INT >= 24) {
            runCatching { ExifInterface(bytes.inputStream()).getAttributeInt(ExifInterface.TAG_ORIENTATION, 1) }.getOrDefault(1)
        } else 1
        val matrix = Matrix().apply {
            when (orientation) {
                2 -> setScale(-1f, 1f)
                3 -> setRotate(180f)
                4 -> setScale(1f, -1f)
                5 -> { setRotate(90f); postScale(-1f, 1f) }
                6 -> setRotate(90f)
                7 -> { setRotate(270f); postScale(-1f, 1f) }
                8 -> setRotate(270f)
            }
        }
        if (matrix.isIdentity) decoded else Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, matrix, true)
            .also { if (it !== decoded) decoded.recycle() }
    }
}
