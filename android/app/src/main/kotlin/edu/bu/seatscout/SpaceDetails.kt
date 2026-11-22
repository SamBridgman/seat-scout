package edu.bu.seatscout

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
internal fun Details(area: StudyArea, state: ScoutState, favorite: Boolean, saved: Boolean,
    onBack: () -> Unit, onFavorite: () -> Unit, onReport: () -> Unit) {
    val context = LocalContext.current
    var actionError by rememberSaveable { mutableStateOf<String?>(null) }
    fun openLink(uri: Uri, error: String) {
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, uri))
            actionError = null
        } catch (_: android.content.ActivityNotFoundException) {
            actionError = error
        }
    }
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Page(Modifier.safeDrawingPadding()) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                    LineIcon(StrokeIcon.BACK, "Back")
                }
                FilledTonalIconToggleButton(checked = favorite, onCheckedChange = { onFavorite() },
                    modifier = Modifier.size(48.dp)) {
                    LineIcon(StrokeIcon.SAVE,
                        if (favorite) "Remove from saved spaces" else "Save this space", filled = favorite)
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(area.building, style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                PageTitle(area.name)
                if (area.address.isNotBlank()) Muted(area.address)
            }
            DetailPanel {
                Heading("Observed conditions")
                Conditions(state, area)
                if (saved) Text("Report submitted successfully.", color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
                Button(onReport, Modifier.fillMaxWidth().heightIn(min = 50.dp)) { Text("Update conditions") }
            }
            Heading("About this space")
            if (area.description.isNotBlank()) {
                Text(area.description, style = MaterialTheme.typography.bodyLarge)
            }
            DetailPanel {
                CatalogFact("Study zone", area.zone)
                CatalogFact("Power outlets", area.power)
                if (area.access.isNotBlank()) CatalogFact("Access", area.access)
                if (area.campus.isNotBlank()) CatalogFact("Campus", area.campus)
            }
            OutlinedButton(onClick = {
                val destination = listOf(area.building, area.address, "Boston University")
                    .filter { it.isNotBlank() }.joinToString(", ")
                openLink(Uri.parse("geo:0,0?q=" + Uri.encode(destination)),
                    "No map app is available on this device.")
            }, Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("Open in maps") }
            if (area.sourceUrl.isNotBlank()) {
                TextButton(onClick = {
                    openLink(Uri.parse(area.sourceUrl), "No browser is available on this device.")
                }, Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("View BU source") }
                if (area.verifiedOn.isNotBlank()) Muted("Source checked ${area.verifiedOn}")
            }
            actionError?.let { error ->
                Text(error, color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
            }
        }
    }
}
