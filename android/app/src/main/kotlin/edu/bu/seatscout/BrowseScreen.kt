package edu.bu.seatscout

import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

private enum class Section(val label: String) { DISCOVER("Discover"), MAP("Map"), SAVED("Saved") }

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun Browse(
    state: ScoutState,
    favorites: Set<String>,
    loading: Boolean,
    areas: List<StudyArea>,
    openArea: (StudyArea) -> Unit
) {
    val campuses = areas.map { it.campus }.filter { it.isNotBlank() }.distinct()

    var tab by rememberSaveable { mutableStateOf(Section.DISCOVER) }
    var query by rememberSaveable { mutableStateOf("") }
    var quiet by rememberSaveable { mutableStateOf(false) }
    var power by rememberSaveable { mutableStateOf(false) }
    var selectedCampus by rememberSaveable { mutableStateOf<String?>(null) }
    var focusedAreaId by rememberSaveable { mutableStateOf<String?>(null) }
    var namedRoom by rememberSaveable { mutableStateOf(false) }
    val focus = LocalFocusManager.current
    val search = query.trim()
    val filtered = search.isNotEmpty() || quiet || power
    val places = when (tab) {
        Section.SAVED -> areas.filter { it.id in favorites }
        Section.MAP -> areas.filter { selectedCampus == null || selectedCampus == it.campus }
        Section.DISCOVER -> areas.filter { area ->
            val report = state.reports[area.id]?.takeIf { it.freshAgeMinutes(state.now) != null }
            listOf(area.name, area.building, area.campus, area.address).any {
                it.contains(search, ignoreCase = true)
            } && (!quiet || report?.atmosphere == Atmosphere.QUIET) &&
                (!power || area.power == "Available")
        }
    }
    val density = LocalDensity.current
    val keyboardOpen = WindowInsets.ime.getBottom(density) > 0
    val scene = rememberGraphicsLayer()
    val lens = rememberGraphicsLayer()
    val backdrop = remember(scene, lens) { DockBackdrop(scene, lens) }
    val glass = backdrop.takeIf { Build.VERSION.SDK_INT >= 31 && !keyboardOpen }
    var dockHeight by remember { mutableStateOf(92.dp) }
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Box(Modifier.fillMaxSize()) {
            val capture = if (glass != null) Modifier.recordDockBackdrop(glass) else Modifier
            key(tab) {
                Page(
                    capture.safeDrawingPadding().imePadding(),
                    bottomSpace = if (keyboardOpen) 0.dp else dockHeight + 16.dp
                ) {
                    when (tab) {
                        Section.DISCOVER -> {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("Seat-Scout", color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                                PageTitle("Find a study space")
                            }
                            OutlinedTextField(
                                value = query, onValueChange = { query = it },
                                modifier = Modifier.fillMaxWidth(), singleLine = true,
                                label = { Text("Search spaces") },
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                keyboardActions = KeyboardActions(onSearch = { focus.clearFocus() }),
                                shape = RoundedCornerShape(16.dp),
                                leadingIcon = { SearchIcon() },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                ),
                                trailingIcon = if (query.isNotEmpty()) {
                                    { IconButton(onClick = { query = "" }) { LineIcon(StrokeIcon.CLOSE, "Clear search") } }
                                } else null
                            )
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                ScoutFilterChip("Quiet now", quiet) { quiet = !quiet }
                                ScoutFilterChip("Power outlets", power) { power = !power }
                            }
                            if (!filtered && !keyboardOpen) {
                                PhotoLocationFinder(areas) { match ->
                                    focusedAreaId = match.area.id
                                    selectedCampus = match.area.campus
                                    namedRoom = match.roomNamed
                                    tab = Section.MAP
                                }
                                CampusAtlas {
                                    focus.clearFocus()
                                    selectedCampus = null
                                    focusedAreaId = null
                                    tab = Section.MAP
                                }
                            }
                            ResultsHeading(if (filtered) "Search results" else "Around campus", places.size)
                        }
                        Section.MAP -> {
                            PageTitle("Campus map")
                            CampusSpaceMap(places, places.find { it.id == focusedAreaId }, namedRoom, openArea)
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                ScoutFilterChip("All campuses", selectedCampus == null) { selectedCampus = null; focusedAreaId = null }
                                campuses.forEach { campus ->
                                    ScoutFilterChip(campus, selectedCampus == campus) { selectedCampus = campus; focusedAreaId = null }
                                }
                            }
                            ResultsHeading("Study spaces", places.size)
                        }
                        Section.SAVED -> PageTitle("Saved spaces")
                    }
                    if (places.isEmpty()) {
                        Surface(shape = CardShape, color = MaterialTheme.colorScheme.surface) {
                            Column(Modifier.fillMaxWidth().padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)) {

                                if (loading) {
                                    Surface(Modifier.fillMaxSize()) {
                                        Column(
                                            modifier = Modifier.fillMaxSize(),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            CircularProgressIndicator()
                                            Spacer(Modifier.height(16.dp))
                                            Text("Loading study spaces…")
                                        }
                                    }

                                }

                                if (tab == Section.SAVED) {
                                    Heading("Keep a space handy")
                                    Muted("Save a space from its details to find it here.")
                                    TextButton(onClick = { tab = Section.DISCOVER },
                                        modifier = Modifier.heightIn(min = 48.dp)) { Text("Explore study spaces") }
                                } else {
                                    Muted(if (quiet) "Quiet now uses reports from this device from the past 30 minutes."
                                        else "No spaces match your search and filters.")
                                    TextButton(
                                        onClick = { query = ""; quiet = false; power = false },
                                        modifier = Modifier.heightIn(min = 48.dp)
                                    ) { Text("Reset filters") }
                                }
                            }
                        }
                    }
                    Directory(places, state, maxWidth >= 620.dp, openArea)
                }
            }
            if (!keyboardOpen) {
                ScoutDock(
                    selected = tab,
                    onSelect = { focus.clearFocus(); tab = it },
                    backdrop = glass,
                    modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding()
                        .padding(horizontal = 18.dp, vertical = 10.dp)
                        .widthIn(max = 460.dp).fillMaxWidth()
                        .onSizeChanged { dockHeight = with(density) { it.height.toDp() } }
                )
            }
        }
    }
}

@Composable
private fun CampusAtlas(onExplore: () -> Unit) {
    val dark = isSystemInDarkTheme()
    val land = if (dark) Color(0xFF292F2E) else Color(0xFFE8E9DF)
    val river = if (dark) Color(0xFF314747) else Color(0xFFC8DEDD)
    val blocks = if (dark) Color(0xFF3D4840) else Color(0xFFD2D9CB)
    val road = if (dark) Color(0xFF6D786C) else Color(0xFFFCF9EF)
    val panel = if (dark) Color(0xFF222627) else Color(0xFFFFFCF6)
    val label = if (dark) Color(0xFFF6F5EF) else Color(0xFF2D3B38)
    val interaction = Modifier.clickable(
        role = Role.Button, onClickLabel = "Open campus map", onClick = onExplore
    )
    Box(Modifier.fillMaxWidth().clip(AtlasShape).background(land).then(interaction)) {
        Canvas(Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height
            drawPath(Path().apply {
                moveTo(0f, 0f)
                lineTo(w, 0f)
                lineTo(w, h * .20f)
                cubicTo(w * .70f, h * .26f, w * .52f, h * .06f, w * .22f, h * .23f)
                quadraticTo(w * .08f, h * .31f, 0f, h * .30f)
                close()
            }, river)
            for (row in 0..3) {
                for (column in 0..11) {
                    val x = w * (column * .091f - .02f)
                    val y = h * (.30f + row * .19f + column * .007f)
                    drawRoundRect(blocks, Offset(x, y),
                        Size(w * (.061f + (column % 3) * .005f), h * .105f),
                        cornerRadius = CornerRadius(2.dp.toPx()))
                }
            }
            for (street in 1..9) {
                val x = w * (street * .112f)
                drawLine(road, Offset(x, h * .32f), Offset(x - w * .07f, h), 2.dp.toPx())
            }
            drawLine(road, Offset(0f, h * .62f), Offset(w, h * .72f), 7.dp.toPx())
        }
        Row(Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Surface(Modifier.weight(1f, fill = false), shape = RoundedCornerShape(12.dp),
                color = panel, contentColor = label) {
                Column(Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                    Text("Explore campus", style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold)
                    Text("Boston University", style = MaterialTheme.typography.bodySmall)
                }
            }
            Spacer(Modifier.width(8.dp))
            Surface(shape = CircleShape, color = panel, contentColor = label) {
                Box(Modifier.size(36.dp), contentAlignment = Alignment.Center) {
                    LineIcon(StrokeIcon.NEXT)
                }
            }
        }
    }
}

@Composable
private fun Directory(places: List<StudyArea>, state: ScoutState, wide: Boolean, openArea: (StudyArea) -> Unit) {
    val buildings = places.groupBy { it.building }.entries.toList()
    val columns = if (wide) 2 else 1
    buildings.chunked(columns).forEach { row ->
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            row.forEach { (building, areas) ->
                Surface(Modifier.weight(1f), shape = CardShape,
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                    Column {
                        Text(building, Modifier.padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 8.dp)
                            .semantics { heading() },
                            style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        areas.forEach { area ->
                            HorizontalDivider(Modifier.padding(horizontal = 16.dp),
                                color = MaterialTheme.colorScheme.outlineVariant)
                            Row(Modifier.fillMaxWidth().heightIn(min = 56.dp)
                                .clickable(role = Role.Button) { openArea(area) }
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(area.name, style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium)
                                    Conditions(state, area, compact = true)
                                }
                                LineIcon(StrokeIcon.NEXT)
                            }
                        }
                    }
                }
            }
            if (row.size < columns) Spacer(Modifier.weight(1f))
        }
    }
}

@Composable
private fun ScoutDock(
    selected: Section,
    onSelect: (Section) -> Unit,
    backdrop: DockBackdrop?,
    modifier: Modifier
) {
    val shape = RoundedCornerShape(28.dp)
    val colors = MaterialTheme.colorScheme
    val dark = isSystemInDarkTheme()
    val rim = Brush.verticalGradient(listOf(
        Color.White.copy(alpha = if (dark) .30f else .94f),
        colors.outlineVariant.copy(alpha = .88f)
    ))
    NavigationBar(
        modifier = modifier.shadow(8.dp, shape).clip(shape)
            .dockGlass(backdrop, colors.surface, dark).border(1.dp, rim, shape),
        containerColor = Color.Transparent,
        tonalElevation = 0.dp,
        windowInsets = WindowInsets(0, 0, 0, 0)
    ) {
        Section.entries.forEach { section ->
            NavigationBarItem(
                selected = section == selected,
                onClick = { onSelect(section) },
                icon = { SectionIcon(section) },
                label = { Text(if (section == Section.DISCOVER && LocalDensity.current.fontScale > 1.3f)
                    "Find" else section.label, fontWeight = FontWeight.SemiBold) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = colors.onPrimaryContainer,
                    selectedTextColor = colors.onSurface,
                    indicatorColor = colors.primaryContainer,
                    unselectedIconColor = colors.onSurfaceVariant,
                    unselectedTextColor = colors.onSurfaceVariant
                )
            )
        }
    }
}

@Composable
private fun SectionIcon(section: Section) {
    val color = LocalContentColor.current
    Canvas(Modifier.size(22.dp)) {
        val w = size.width
        val s = w / 24f
        fun stroke(path: Path) = drawPath(path, color, style = Stroke(1.8f * s))
        when (section) {
            Section.DISCOVER -> {
                drawCircle(color, radius = w * .43f, style = Stroke(1.8f * s))
                stroke(Path().apply { moveTo(16 * s, 8 * s); lineTo(14 * s, 14 * s)
                    lineTo(8 * s, 16 * s); lineTo(10 * s, 10 * s); close() })
            }
            Section.MAP -> stroke(Path().apply {
                moveTo(2 * s, 6 * s); lineTo(8 * s, 3 * s); lineTo(16 * s, 6 * s); lineTo(22 * s, 3 * s)
                lineTo(22 * s, 18 * s); lineTo(16 * s, 21 * s); lineTo(8 * s, 18 * s); lineTo(2 * s, 21 * s); close()
                moveTo(8 * s, 3 * s); lineTo(8 * s, 18 * s); moveTo(16 * s, 6 * s); lineTo(16 * s, 21 * s)
            })
            Section.SAVED -> stroke(Path().apply {
                moveTo(6 * s, 3 * s); lineTo(18 * s, 3 * s); lineTo(18 * s, 21 * s)
                lineTo(12 * s, 17 * s); lineTo(6 * s, 21 * s); close()
            })
        }
    }
}

@Composable
private fun SearchIcon() {
    val color = LocalContentColor.current
    Canvas(Modifier.size(20.dp)) {
        val line = 1.7.dp.toPx()
        val center = Offset(size.width * .42f, size.height * .42f)
        drawCircle(color, size.width * .27f, center,
            style = Stroke(line))
        drawLine(color, Offset(size.width * .62f, size.height * .62f),
            Offset(size.width * .85f, size.height * .85f), line,
            cap = StrokeCap.Round)
    }
}

@Composable
private fun ScoutFilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected, onClick = onClick, label = { Text(label) },
        modifier = Modifier.heightIn(min = 48.dp),
        shape = RoundedCornerShape(12.dp),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true, selected = selected,
            borderColor = MaterialTheme.colorScheme.outlineVariant
        )
    )
}
