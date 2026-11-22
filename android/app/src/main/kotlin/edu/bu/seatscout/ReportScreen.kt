package edu.bu.seatscout

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
internal fun ReportScreen(area: StudyArea, model: ScoutViewModel, onBack: () -> Unit) {
    var step by rememberSaveable { mutableIntStateOf(1) }
    var seats by rememberSaveable { mutableStateOf<Seats?>(null) }
    var atmosphere by rememberSaveable { mutableStateOf<Atmosphere?>(null) }
    val saving by model.saving.collectAsStateWithLifecycle()
    val error by model.saveError.collectAsStateWithLifecycle()
    val savedArea by model.savedArea.collectAsStateWithLifecycle()
    LaunchedEffect(savedArea) { if (savedArea == area.id) onBack() }
    val previous = { if (step == 1) onBack() else step -= 1 }
    BackHandler(enabled = saving || step > 1) { if (!saving) previous() }
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        key(step) {
            Page(Modifier.safeDrawingPadding()) {
                if (!saving) {
                    TextButton(onClick = previous, modifier = Modifier.heightIn(min = 48.dp)) {
                        Text(if (step == 1) "Cancel" else "Back")
                    }
                }
                Muted("Step $step of 3")
                LinearProgressIndicator(
                    progress = { step / 3f },
                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape)
                )
                PageTitle(when (step) {
                    1 -> "How’s the seating?"
                    2 -> "How does it sound?"
                    else -> "Review your report"
                })
                Muted(area.name)
                when (step) {
                    1 -> {
                        Column(Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Seats.entries.forEach { choice ->
                                Option(choice.label, null, seats == choice) { seats = choice }
                            }
                        }
                    }
                    2 -> {
                        Column(Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Atmosphere.entries.forEach { choice ->
                                Option(choice.label, choice.description, atmosphere == choice) { atmosphere = choice }
                            }
                        }
                    }
                    else -> {
                        DetailPanel {
                            CatalogFact("Seating", seats?.label.orEmpty())
                            CatalogFact("Atmosphere", atmosphere?.label.orEmpty())
                        }
                        Muted("Your report stays on this device and is current for 30 minutes.")
                        if (error) Text("Couldn’t save your report. Try again.", color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
                    }
                }
                Button(
                    onClick = {
                        if (step < 3) step += 1
                        else {
                            val chosenSeats = seats ?: return@Button
                            val chosenAtmosphere = atmosphere ?: return@Button
                            model.save(area, chosenSeats, chosenAtmosphere)
                        }
                    },
                    enabled = !saving && when (step) { 1 -> seats != null; 2 -> atmosphere != null
                        else -> seats != null && atmosphere != null },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp)
                ) { Text(if (saving) "Saving…" else if (step == 3) "Save report" else "Continue") }
            }
        }
    }
}

@Composable
private fun Option(label: String, description: String?, selected: Boolean, onClick: () -> Unit) {
    Surface(shape = RoundedCornerShape(16.dp), color = if (selected) MaterialTheme.colorScheme.primaryContainer
    else MaterialTheme.colorScheme.surface) {
        Row(
            Modifier.fillMaxWidth().heightIn(min = 58.dp)
                .selectable(selected, role = Role.RadioButton, onClick = onClick)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(selected = selected, onClick = null)
            Column {
                Text(label, fontWeight = FontWeight.SemiBold)
                if (description != null) Muted(description)
            }
        }
    }
}
