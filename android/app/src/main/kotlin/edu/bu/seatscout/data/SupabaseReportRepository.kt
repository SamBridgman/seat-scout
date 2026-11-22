package edu.bu.seatscout.data

import edu.bu.seatscout.Atmosphere
import edu.bu.seatscout.Report
import edu.bu.seatscout.ReportDao
import edu.bu.seatscout.ReportRepository
import edu.bu.seatscout.Seats
import edu.bu.seatscout.StudyArea
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

internal class SupabaseReportRepository(
    private val dao: ReportDao,
    private val upload: suspend (StudyArea, Seats, Atmosphere) -> Report = ::uploadReport
) : ReportRepository {
    // Local cache of submissions from this device; shared report syncing is separate.
    override val reports = dao.observeReports()

    override suspend fun save(area: StudyArea, seats: Seats, atmosphere: Atmosphere) {
        val confirmed = upload(area, seats, atmosphere)
        dao.save(confirmed)
    }
}

private suspend fun uploadReport(area: StudyArea, seats: Seats, atmosphere: Atmosphere): Report {
    val client = SupabaseProvider.client
    client.auth.awaitInitialization()
    if (client.auth.currentSessionOrNull() == null) {
        client.auth.signInAnonymously()
    }
    val userId = checkNotNull(client.auth.currentUserOrNull()) {
        "Anonymous sign-in did not create a user."
    }.id
    val row = client.from("reports").upsert(
        ReportWrite(userId, area.id, seats.name, atmosphere.name)
    ) {
        onConflict = "user_id,area_id"
        select()
    }.decodeSingle<ReportRow>()
    return Report(row.areaId, Seats.valueOf(row.seats), Atmosphere.valueOf(row.atmosphere), row.savedAt)
}

@Serializable
private data class ReportWrite(
    @SerialName("user_id") val userId: String,
    @SerialName("area_id") val areaId: String,
    val seats: String,
    val atmosphere: String
)

@Serializable
private data class ReportRow(
    @SerialName("area_id") val areaId: String,
    val seats: String,
    val atmosphere: String,
    @SerialName("saved_at") val savedAt: Long
)
