package edu.bu.seatscout

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray

// Local planar projection in meters. Across BU the longitude scale changes by less than 0.1%.
private fun meters(lng: Double, lat: Double) =
    Offset(((lng + 71.10) * 82240).toFloat(), ((42.35 - lat) * 111100).toFloat())

@Composable
internal fun OfflineCampusMap(addresses: List<String>, selected: String, modifier: Modifier, onSelect: (String) -> Unit) {
    val context = LocalContext.current
    val streetPaths by produceState<Pair<Path, Path>?>(null) {
        value = withContext(Dispatchers.Default) {
            val minor = Path()
            val major = Path()
            val rows = JSONArray(context.resources.openRawResource(R.raw.campus_streets).bufferedReader().use { it.readText() })
            for (i in 0 until rows.length()) {
                val row = rows.getJSONObject(i)
                val path = if (row.getBoolean("major")) major else minor
                val points = row.getJSONArray("path")
                for (j in 0 until points.length()) {
                    val pair = points.getJSONArray(j)
                    val pt = meters(pair.getDouble(0), pair.getDouble(1))
                    if (j == 0) path.moveTo(pt.x, pt.y) else path.lineTo(pt.x, pt.y)
                }
            }
            minor to major
        }
    }
    val pins = remember(addresses) { addresses.associateWith {
        campusCoordinates.getValue(it).let { p -> meters(p.longitude, p.latitude) }
    } }
    val left = pins.values.minOf { it.x }
    val right = pins.values.maxOf { it.x }
    val top = pins.values.minOf { it.y }
    val bottom = pins.values.maxOf { it.y }
    val midpoint = Offset((left + right) / 2, (top + bottom) / 2)
    var dimensions by remember { mutableStateOf(IntSize.Zero) }
    var zoom by remember(addresses) { mutableFloatStateOf(1f) }
    var pan by remember(addresses) { mutableStateOf(Offset.Zero) }
    val screenCenter = Offset(dimensions.width / 2f, dimensions.height / 2f)
    val base = minOf(dimensions.width / ((right - left).coerceAtLeast(280f) * 1.17f),
        dimensions.height / ((bottom - top).coerceAtLeast(220f) * 1.25f)).coerceAtLeast(.001f)
    val scale = base * zoom
    fun screen(p: Offset) = screenCenter + pan + (p - midpoint) * scale
    val radius = with(LocalDensity.current) { 24.dp.toPx() }
    val dark = isSystemInDarkTheme()
    val land = if (dark) Color(0xff28332f) else Color(0xffe1e8df)
    val minor = if (dark) Color(0xff65726b) else Color(0xfffafaf2)
    val major = if (dark) Color(0xff94a098) else Color(0xffb7a98f)
    val ink = MaterialTheme.colorScheme.primary
    val currentSelect by rememberUpdatedState(onSelect)
    LaunchedEffect(selected, addresses, dimensions) {
        if (dimensions.width > 0) {
            val p = screen(pins.getValue(selected))
            if (p.x !in radius..dimensions.width-radius || p.y !in radius..dimensions.height-radius)
                pan = (midpoint - pins.getValue(selected)) * scale
        }
    }
    Box(modifier.background(land)) {
        Canvas(Modifier.fillMaxSize().onSizeChanged { dimensions = it }
            .semantics { contentDescription = "Offline building map. Use Previous pin and Next pin to explore." }
            .pointerInput(addresses, base) {
                detectTransformGestures { centroid, delta, factor, _ ->
                    val old = zoom
                    zoom = (zoom * factor).coerceIn(.75f, 24f)
                    val anchor = centroid - screenCenter
                    pan = anchor + (pan - anchor) * (zoom / old) + delta
                }
            }
            .pointerInput(addresses, scale, pan, dimensions) {
                detectTapGestures { tap ->
                    val nearest = pins.minByOrNull { (screen(it.value) - tap).getDistance() }
                    if (nearest != null && (screen(nearest.value) - tap).getDistance() <= radius) currentSelect(nearest.key)
                }
            }) {
            val origin = screen(Offset.Zero)
            withTransform({ translate(origin.x, origin.y); scale(scale, scale, Offset.Zero) }) {
                streetPaths?.let { (minorPath, majorPath) ->
                    drawPath(minorPath, minor, style = Stroke(1.5.dp.toPx()/scale))
                    drawPath(majorPath, major, style = Stroke(3.dp.toPx()/scale))
                }
            }
            for ((_, pt) in pins) {
                drawCircle(Color.White, 6.dp.toPx(), screen(pt))
                drawCircle(Color(0xff6c4355), 4.dp.toPx(), screen(pt))
            }
            val active = screen(pins.getValue(selected))
            drawCircle(ink.copy(alpha = .20f), 17.dp.toPx(), active)
            drawCircle(Color.White, 10.dp.toPx(), active)
            drawCircle(ink, 7.dp.toPx(), active)
        }
        Surface(Modifier.align(Alignment.TopEnd).padding(6.dp), shape = MaterialTheme.shapes.medium) {
            TextButton(onClick = { zoom = 1f; pan = Offset.Zero }) { Text("Fit pins") }
        }
        Surface(Modifier.align(Alignment.BottomStart), color = MaterialTheme.colorScheme.surface.copy(alpha = .94f)) {
            Text("Boston SAM · U.S. Census TIGERweb", Modifier.padding(5.dp), style = MaterialTheme.typography.labelSmall)
        }
    }
}
