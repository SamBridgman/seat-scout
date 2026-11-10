package edu.bu.seatscout

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

// Hosts the three Compose screens and applies the app theme.
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme(colorScheme = lightColorScheme(
                primary = Color(0xFFA62432),
                background = Color(0xFFF9F7F3),
                onBackground = Color(0xFF272421),
                surfaceVariant = Color(0xFFECE7DF),
                onSurfaceVariant = Color(0xFF514B45)
            )) {
                ScoutNavigation(viewModel())
            }
        }
    }
}

// Connects browse, details, and reporting screens using the area ID in each route.
@Composable
private fun ScoutNavigation(model: ScoutViewModel) {
    val nav = rememberNavController()
    val state by model.state.collectAsStateWithLifecycle()
    NavHost(navController = nav, startDestination = "browse") {
        composable("browse") {
            Screen("SeatScout", "Find a study space") {
                Text("Sample campus directory · Reports stay on this device")
                sampleAreas.forEach { area ->
                    OutlinedCard(onClick = { nav.navigate("details/${area.id}") }, modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(area.name, style = MaterialTheme.typography.titleMedium)
                            Text(area.building, style = MaterialTheme.typography.bodySmall)
                            Conditions(state, area)
                        }
                    }
                }
            }
        }
        composable("details/{areaId}") { entry ->
            val area = sampleAreas.firstOrNull { it.id == entry.arguments?.getString("areaId") }
                ?: return@composable
            Screen(area.building, area.name, onBack = { nav.popBackStack() }) {
                Text("Location information", style = MaterialTheme.typography.titleMedium)
                Text("Official study zone: ${area.zone}\nPower: ${area.power}\nSample catalog entry")
                HorizontalDivider()
                Text("Observed conditions", style = MaterialTheme.typography.titleMedium)
                Conditions(state, area)
                Button(onClick = {
                    model.saveError.value = false
                    model.savedArea.value = null
                    nav.navigate("report/${area.id}")
                }) { Text("Update conditions") }
            }
        }
        composable("report/{areaId}") { entry ->
            val area = sampleAreas.firstOrNull { it.id == entry.arguments?.getString("areaId") }
                ?: return@composable
            ReportScreen(area, model, onBack = {
                if (nav.currentBackStackEntry == entry) nav.popBackStack()
            })
        }
    }
}

// Shared screen layout: headings, optional Back button, scrolling, and spacing.
@Composable
private fun Screen(
    context: String,
    title: String,
    onBack: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
        Column(
            Modifier.safeDrawingPadding().verticalScroll(rememberScrollState()).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (onBack != null) TextButton(onClick = onBack) { Text("Back") }
            Text(context, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
            Text(title, style = MaterialTheme.typography.headlineLarge)
            content()
        }
    }
}

// Shows loading/errors, fresh conditions, or Unknown when no usable report exists.
@Composable
private fun Conditions(state: ScoutState, area: StudyArea) {
    val report = state.reports[area.id]
    val age = report?.freshAgeMinutes(state.now)
    when {
        state.loading -> Text("Loading conditions…")
        state.readFailed -> Text("Couldn’t load saved conditions")
        report == null || age == null -> {
            Text("Seats: Unknown · Atmosphere: Unknown")
            Text("No fresh local report", style = MaterialTheme.typography.bodySmall)
        }
        else -> {
            Text(report.seats.label)
            Text(report.atmosphere.label)
            val time = when (age) {
                0L -> "just now"
                1L -> "1 minute ago"
                else -> "$age minutes ago"
            }
            Text("Saved on this device $time", style = MaterialTheme.typography.bodySmall)
        }
    }
}

// Collects both choices, shows save progress, and returns to details after a successful save.
@Composable
private fun ReportScreen(area: StudyArea, model: ScoutViewModel, onBack: () -> Unit) {
    var seats by rememberSaveable { mutableStateOf<Seats?>(null) }
    var atmosphere by rememberSaveable { mutableStateOf<Atmosphere?>(null) }
    val saving by model.saving.collectAsStateWithLifecycle()
    val error by model.saveError.collectAsStateWithLifecycle()
    val savedArea by model.savedArea.collectAsStateWithLifecycle()
    LaunchedEffect(savedArea) {
        if (savedArea == area.id) onBack()
    }
    // Prevent Back navigation during a save; Home and closing the app are still possible.
    BackHandler(enabled = saving) { }
    Screen(area.name, "Update conditions", onBack = if (saving) null else onBack) {
        Text("Choose both conditions based on what you see and hear at this study area")
        Text("Seat availability", style = MaterialTheme.typography.titleMedium)
        Column(Modifier.selectableGroup()) {
            Seats.entries.forEach { choice ->
                Option(choice.label, null, seats == choice, !saving) { seats = choice }
            }
        }
        Text("Atmosphere", style = MaterialTheme.typography.titleMedium)
        Column(Modifier.selectableGroup()) {
            Atmosphere.entries.forEach { choice ->
                Option(choice.label, choice.description, atmosphere == choice, !saving) { atmosphere = choice }
            }
        }
        if (error) Text("Couldn’t save your report. Try again.", color = MaterialTheme.colorScheme.error)
        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = !saving && seats != null && atmosphere != null,
            onClick = {
                val selectedSeats = seats ?: return@Button
                val selectedAtmosphere = atmosphere ?: return@Button
                model.save(area, selectedSeats, selectedAtmosphere)
            }
        ) { Text(if (saving) "Saving…" else "Save report") }
    }
}

// Reusable radio option; the parent form owns the selection and the entire row is clickable.
@Composable
private fun Option(label: String, description: String?, selected: Boolean, enabled: Boolean, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().heightIn(min = 48.dp)
            .selectable(selected, enabled = enabled, role = Role.RadioButton, onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = null, enabled = enabled)
        Column {
            Text(label)
            if (description != null) Text(description, style = MaterialTheme.typography.bodySmall)
        }
    }
}
