package edu.bu.seatscout

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

private val Burgundy = Color(0xFF781D3B)
private val Rose = Color(0xFFF7E5EB)
private val Cream = Color(0xFFFFFAF6)
private val Paper = Color(0xFFF7F2EE)
private val Ink = Color(0xFF291F24)
internal val CardShape = RoundedCornerShape(16.dp)
internal val AtlasShape = RoundedCornerShape(20.dp)
private val lightPalette = lightColorScheme(
    primary = Burgundy, onPrimary = Color.White, primaryContainer = Rose, onPrimaryContainer = Burgundy,
    background = Paper, onBackground = Ink, surface = Cream, onSurface = Ink,
    surfaceVariant = Color(0xFFEFE5E3), onSurfaceVariant = Color(0xFF62535C),
    outlineVariant = Color(0xFFE2D6DB), secondaryContainer = Rose, onSecondaryContainer = Burgundy
)
private val darkPalette = darkColorScheme(
    primary = Color(0xFFF8B4CB), onPrimary = Color(0xFF4B0925), primaryContainer = Color(0xFF55243B),
    onPrimaryContainer = Color(0xFFFFD9E6), background = Color(0xFF211A1F), onBackground = Color(0xFFF9EFF3),
    surface = Color(0xFF2E242B), onSurface = Color(0xFFF9EFF3),
    surfaceVariant = Color(0xFF40333D), onSurfaceVariant = Color(0xFFDCCCD5),
    outlineVariant = Color(0xFF564751), secondaryContainer = Color(0xFF55243B),
    onSecondaryContainer = Color(0xFFFFD9E6)
)

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val dark = isSystemInDarkTheme()
            SideEffect {
                WindowCompat.getInsetsController(window, window.decorView).apply {
                    isAppearanceLightStatusBars = !dark
                    isAppearanceLightNavigationBars = !dark
                }
            }
            MaterialTheme(colorScheme = if (dark) darkPalette else lightPalette) {
                ScoutNavigation(viewModel())
            }
        }
    }
}

@Composable
private fun ScoutNavigation(model: ScoutViewModel) {
    val nav = rememberNavController()
    val state by model.state.collectAsStateWithLifecycle()
    val favorites by model.favorites.collectAsStateWithLifecycle()
    val savedArea by model.savedArea.collectAsStateWithLifecycle()
    val areas by model.studyAreas.collectAsStateWithLifecycle()
    val areasLoading by model.studyAreasLoading.collectAsStateWithLifecycle()

    NavHost(navController = nav, startDestination = "browse",
        enterTransition = { fadeIn(tween(180)) },
        exitTransition = { fadeOut(tween(180)) },
        popEnterTransition = { fadeIn(tween(180)) },
        popExitTransition = { fadeOut(tween(180)) }) {
        composable("browse") {
            Browse(state, favorites, loading = areasLoading, areas,  openArea = { nav.navigate("details/${it.id}") })
        }
        composable("details/{areaId}") { entry ->
            val area = areas.firstOrNull { it.id == entry.arguments?.getString("areaId") }
                ?: return@composable
            Details(area, state, area.id in favorites, savedArea == area.id,
                onBack = { nav.popBackStack() },
                onFavorite = { model.toggleFavorite(area) },
                onReport = {
                    model.saveError.value = false
                    model.savedArea.value = null
                    nav.navigate("report/${area.id}")
                })
        }
        composable("report/{areaId}") { entry ->
            val area = areas.firstOrNull { it.id == entry.arguments?.getString("areaId") }
                ?: return@composable
            ReportScreen(area, model) {
                if (nav.currentBackStackEntry == entry) nav.popBackStack()
            }
        }
    }
}
