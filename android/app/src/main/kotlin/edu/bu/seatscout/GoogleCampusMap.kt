package edu.bu.seatscout

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

@Composable
internal fun GoogleCampusMap(addresses: List<String>, selected: String, modifier: Modifier, onSelect: (String) -> Unit) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val view = remember(context) { MapView(context).apply { onCreate(null) } }
    var map by remember(view) { mutableStateOf<GoogleMap?>(null) }
    val currentSelect by rememberUpdatedState(onSelect)
    DisposableEffect(view, lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> view.onStart()
                Lifecycle.Event.ON_RESUME -> view.onResume()
                Lifecycle.Event.ON_PAUSE -> view.onPause()
                Lifecycle.Event.ON_STOP -> view.onStop()
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        var alive = true
        view.getMapAsync { if (alive) map = it }
        onDispose {
            alive = false
            lifecycle.removeObserver(observer)
            if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) view.onPause()
            if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) view.onStop()
            view.onDestroy()
        }
    }
    AndroidView(factory = { view }, modifier = modifier)
    LaunchedEffect(map, addresses) {
        val active = map ?: return@LaunchedEffect
        active.clear()
        active.uiSettings.isMapToolbarEnabled = false
        active.uiSettings.isZoomControlsEnabled = true
        addresses.forEach { address ->
            val p = campusCoordinates.getValue(address)
            active.addMarker(MarkerOptions().position(LatLng(p.latitude, p.longitude)).title(address))?.tag = address
        }
        active.setOnMarkerClickListener { marker ->
            (marker.tag as? String)?.let { currentSelect(it) }
            marker.showInfoWindow()
            true
        }
    }
    LaunchedEffect(map, selected) {
        val p = campusCoordinates.getValue(selected)
        map?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(p.latitude, p.longitude), 16f))
    }
}
