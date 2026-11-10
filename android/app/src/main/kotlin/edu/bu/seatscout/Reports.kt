package edu.bu.seatscout

import android.app.Application
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

internal enum class Seats(val label: String) {
    MANY("Many seats available"), FEW("Few seats available"), FULL("Full")
}

internal enum class Atmosphere(val label: String, val description: String) {
    QUIET("Quiet", "Little audible disturbance"),
    MODERATE("Moderate", "Background sound or occasional conversation"),
    NOISY("Noisy", "Frequent conversation or disruptive sound")
}

internal data class StudyArea(val id: String, val name: String, val building: String, val zone: String, val power: String)

internal val sampleAreas = listOf(
    StudyArea("mugar4", "4th Floor Open Study", "Mugar Memorial Library", "Silent", "Available"),
    StudyArea("pal", "PAL Study Lounge", "Mugar Memorial Library", "Collaborative", "Available"),
    StudyArea("stone", "Open Seating", "Stone Science Library", "Silent", "Unverified")
)

@Entity(tableName = "reports")
internal data class Report(
    @PrimaryKey val areaId: String,
    val seats: Seats,
    val atmosphere: Atmosphere,
    val savedAt: Long
)

internal const val MINUTE_MS = 60_000L

internal fun Report.freshAgeMinutes(now: Long): Long? {
    val age = now - savedAt
    return if (age in 0 until 30 * MINUTE_MS) age / MINUTE_MS else null
}

@Dao
internal interface ReportDao {
    @Query("SELECT * FROM reports")
    fun observeReports(): Flow<List<Report>>

    @Upsert
    suspend fun save(report: Report)
}

@Database(entities = [Report::class], version = 1)
internal abstract class SeatScoutDatabase : RoomDatabase() {
    abstract fun reports(): ReportDao
}

internal class ReportRepository(private val dao: ReportDao) {
    val reports = dao.observeReports()

    suspend fun save(area: StudyArea, seats: Seats, atmosphere: Atmosphere) {
        dao.save(Report(area.id, seats, atmosphere, System.currentTimeMillis()))
    }
}

class SeatScoutApp : Application() {
    internal val repository by lazy {
        val database = Room.databaseBuilder(this, SeatScoutDatabase::class.java, "seat-scout.db").build()
        ReportRepository(database.reports())
    }
}
