package edu.bu.seatscout

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun CatalogFact(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        Muted(value)
    }
}

@Composable
internal fun Page(
    modifier: Modifier,
    bottomSpace: Dp = 0.dp,
    content: @Composable BoxWithConstraintsScope.() -> Unit
) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        BoxWithConstraints(Modifier.widthIn(max = 920.dp).fillMaxWidth()) {
            Column(Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)) {
                content(this@BoxWithConstraints)
                if (bottomSpace > 0.dp) Spacer(Modifier.height(bottomSpace))
            }
        }
    }
}

@Composable
internal fun DetailPanel(content: @Composable ColumnScope.() -> Unit) {
    val colors = MaterialTheme.colorScheme
    Surface(Modifier.fillMaxWidth(), shape = CardShape, color = colors.surface,
        border = BorderStroke(1.dp, colors.outlineVariant)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp),
            content = content)
    }
}

@Composable
internal fun Heading(title: String) {
    Text(title, modifier = Modifier.semantics { heading() },
        style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, letterSpacing = (-0.6).sp)
}

@Composable
internal fun Muted(value: String) {
    Text(value, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
}

@Composable
internal fun Conditions(state: ScoutState, area: StudyArea, compact: Boolean = false) {
    val report = state.reports[area.id]
    val age = report?.freshAgeMinutes(state.now)
    when {
        state.loading -> Muted("Loading conditions…")
        state.readFailed -> Muted("Couldn’t load saved conditions.")
        report == null || age == null -> {
            if (compact) {
                Text("Conditions unknown", color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall)
            } else {
                Text("Conditions unknown", style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold)
                Muted("No report from the past 30 minutes.")
            }
        }
        else -> {
            val time = when (age) {
                0L -> "just now"
                1L -> "1 minute ago"
                else -> "$age minutes ago"
            }
            if (compact) {
                Text("${report.seats.label}\n${report.atmosphere.label}",
                    style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary)
                Text("Updated $time on this device", color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall)
            } else {
                Text(report.seats.label, style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                Text(report.atmosphere.label, style = MaterialTheme.typography.bodyMedium)
                Muted("Updated $time on this device")
            }
        }
    }
}

internal enum class StrokeIcon { BACK, NEXT, SAVE, CLOSE }

@Composable
internal fun LineIcon(symbol: StrokeIcon, description: String? = null, filled: Boolean = false) {
    val color = LocalContentColor.current
    val accessibility = if (description == null) Modifier else Modifier.semantics {
        contentDescription = description
    }
    Canvas(Modifier.size(22.dp).then(accessibility)) {
        val u = size.width / 24f
        val path = Path().apply {
            when (symbol) {
                StrokeIcon.BACK -> {
                    moveTo(15*u, 5*u); lineTo(8*u, 12*u); lineTo(15*u, 19*u)
                }
                StrokeIcon.NEXT -> {
                    moveTo(9*u, 6*u); lineTo(15*u, 12*u); lineTo(9*u, 18*u)
                }
                StrokeIcon.SAVE -> {
                    moveTo(6*u, 3*u); lineTo(18*u, 3*u); lineTo(18*u, 21*u)
                    lineTo(12*u, 17*u); lineTo(6*u, 21*u); close()
                }
                StrokeIcon.CLOSE -> {
                    moveTo(6*u, 6*u); lineTo(18*u, 18*u)
                    moveTo(18*u, 6*u); lineTo(6*u, 18*u)
                }
            }
        }
        if (filled) drawPath(path, color)
        else drawPath(path, color, style = Stroke(
            1.8f * u, cap = StrokeCap.Round,
            join = StrokeJoin.Round
        ))
    }
}

@Composable
internal fun PageTitle(value: String) {
    Text(value, Modifier.semantics { heading() }, fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.SemiBold, fontSize = 39.sp, lineHeight = 43.sp,
        letterSpacing = (-.8).sp)
}

@Composable
internal fun ResultsHeading(label: String, count: Int) {
    if (LocalDensity.current.fontScale > 1.3f) {
        Column(Modifier.fillMaxWidth().padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Heading(label)
            Muted(if (count == 1) "1 space" else "$count spaces")
        }
        return
    }
    Row(Modifier.fillMaxWidth().padding(top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Text(label, Modifier.weight(1f).semantics { heading() },
            style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold,
            letterSpacing = (-.4).sp)
        Muted(if (count == 1) "1 space" else "$count spaces")
    }
}
