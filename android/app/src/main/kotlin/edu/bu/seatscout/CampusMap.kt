package edu.bu.seatscout

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
internal fun CampusSpaceMap(areas: List<StudyArea>, focus: StudyArea?, namedRoom: Boolean, onOpen: (StudyArea) -> Unit) {
    val buildings = remember(areas) { areas.filter { it.address in campusCoordinates }.groupBy { it.address } }
    if (buildings.isEmpty()) {
        Text("No mapped buildings match this selection.")
        return
    }
    val addresses = buildings.keys.toList()
    var selectedAddress by rememberSaveable(addresses, focus?.id) {
        mutableStateOf(focus?.address?.takeIf { it in buildings } ?: addresses.first())
    }
    var showRooms by rememberSaveable(addresses) { mutableStateOf(false) }
    var streetMap by rememberSaveable { mutableStateOf(BuildConfig.HAS_MAPS_KEY) }
    val selected = selectedAddress.takeIf { it in buildings } ?: addresses.first()
    val rooms = buildings.getValue(selected)
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        val viewport = Modifier.fillMaxWidth().height(280.dp).clip(RoundedCornerShape(20.dp))
        if (streetMap && BuildConfig.HAS_MAPS_KEY) {
            GoogleCampusMap(addresses, selected, viewport) { selectedAddress = it }
        } else {
            OfflineCampusMap(addresses, selected, viewport) { selectedAddress = it }
        }
        Text(rooms.map { it.building }.distinct().joinToString(" / "), style = MaterialTheme.typography.titleMedium)
        Text(selected, style = MaterialTheme.typography.bodyMedium)
        if (namedRoom && focus != null && focus.address == selected && rooms.size > 1) {
            TextButton(onClick = { onOpen(focus) }) { Text("Open ${focus.name}") }
        }
        FlowControls(addresses, selected, onSelect = { selectedAddress = it }, onOpen = {
            if (rooms.size == 1) onOpen(rooms.first()) else showRooms = true
        }, count = rooms.size)
        if (BuildConfig.HAS_MAPS_KEY) TextButton(onClick = { streetMap = !streetMap }) {
            Text(if (streetMap) "Use offline map" else "Use Google Maps")
        }
    }
    if (showRooms) AlertDialog(
        onDismissRequest = { showRooms = false },
        title = { Text(selected) },
        text = {
            LazyColumn {
                items(rooms, key = { it.id }) { room ->
                    TextButton(onClick = { showRooms = false; onOpen(room) }) { Text(room.name) }
                }
            }
        },
        confirmButton = { TextButton(onClick = { showRooms = false }) { Text("Close") } }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlowControls(addresses: List<String>, selected: String, onSelect: (String) -> Unit, onOpen: () -> Unit, count: Int) {
    val index = addresses.indexOf(selected)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        if (addresses.size > 1) {
            TextButton(onClick = { onSelect(addresses[(index + addresses.size - 1) % addresses.size]) }) { Text("Previous pin") }
            TextButton(onClick = { onSelect(addresses[(index + 1) % addresses.size]) }) { Text("Next pin") }
        }
        FilledTonalButton(onClick = onOpen) { Text(if (count == 1) "Open space" else "View $count spaces") }
    }
}
