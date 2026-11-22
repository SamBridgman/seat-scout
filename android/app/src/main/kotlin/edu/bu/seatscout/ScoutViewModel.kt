package edu.bu.seatscout

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

internal data class ScoutState(
    val reports: Map<String, Report> = emptyMap(),
    val now: Long = System.currentTimeMillis(),
    val loading: Boolean = true,
    val readFailed: Boolean = false
)

class ScoutViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = (app as SeatScoutApp).repository
    private val preferences = app.getSharedPreferences("seat-scout", Application.MODE_PRIVATE)
    private val savedIds = MutableStateFlow(preferences.getStringSet("saved-areas", emptySet()).orEmpty().toSet())
    internal val favorites = savedIds.asStateFlow()

    internal fun toggleFavorite(area: StudyArea) {
        val current = savedIds.value
        val next = if (area.id in current) current - area.id else current + area.id
        preferences.edit().putStringSet("saved-areas", next).apply()
        savedIds.value = next
    }

    internal val saving = MutableStateFlow(false)
    internal val saveError = MutableStateFlow(false)
    internal val savedArea = MutableStateFlow<String?>(null)

    private val clock = flow {
        while (true) {
            emit(Unit)
            delay(1_000)
        }
    }

    internal val state = combine(repository.reports, clock) { reports, _ ->
        ScoutState(reports.associateBy { it.areaId }, System.currentTimeMillis(), loading = false)
    }.catch { emit(ScoutState(loading = false, readFailed = true)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(0), ScoutState())

    internal fun save(area: StudyArea, seats: Seats, atmosphere: Atmosphere) {
        if (!saving.compareAndSet(false, true)) return
        saveError.value = false
        viewModelScope.launch {
            try {
                repository.save(area, seats, atmosphere)
            } catch (cancelled: CancellationException) {
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
