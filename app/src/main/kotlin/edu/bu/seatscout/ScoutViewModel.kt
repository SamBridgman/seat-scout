package edu.bu.seatscout

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// The reports, current time, and loading/error status displayed by the UI.
internal data class ScoutState(
    val reports: Map<String, Report> = emptyMap(),
    val now: Long = System.currentTimeMillis(),
    val loading: Boolean = true,
    val readFailed: Boolean = false
)

// Coordinates report data and saves; survives screen rotation.
class ScoutViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = (app as SeatScoutApp).repository
    internal val saving = MutableStateFlow(false)
    internal val saveError = MutableStateFlow(false)
    internal val savedArea = MutableStateFlow<String?>(null)

    // Update report ages every second while the UI listens, without querying Room again.
    private val clock = flow {
        while (true) {
            emit(System.currentTimeMillis())
            delay(1_000)
        }
    }

    // Combine database updates and time updates into the latest screen state.
    internal val state = combine(repository.reports, clock) { reports, now ->
        ScoutState(reports.associateBy { it.areaId }, now, loading = false)
    }.catch { emit(ScoutState(loading = false, readFailed = true)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(0), ScoutState())

    // Save immediately using a coroutine so waiting for Room does not freeze the UI.
    // This task cannot survive process death; persistent upload retries would need a queue and WorkManager.
    internal fun save(area: StudyArea, seats: Seats, atmosphere: Atmosphere) {
        // Check and set saving in one atomic step to prevent overlapping saves.
        if (!saving.compareAndSet(false, true)) return
        saveError.value = false
        viewModelScope.launch {
            try {
                repository.save(area, seats, atmosphere)
            } catch (cancelled: CancellationException) {
                // Let coroutine cancellation stop the task instead of treating it as a save error.
                throw cancelled
            } catch (_: Exception) {
                saveError.value = true
                return@launch
            } finally {
                saving.value = false
            }
            savedArea.value = area.id
        }
    }
}
