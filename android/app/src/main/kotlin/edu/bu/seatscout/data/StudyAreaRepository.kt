package edu.bu.seatscout.data

import edu.bu.seatscout.StudyArea
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonPrimitive

internal class StudyAreaRepository(
    private val client: SupabaseClient = SupabaseProvider.client
) {
    suspend fun getStudyAreas(): List<StudyArea> =
        client.from("study_area").select().decodeList<StudyAreaRow>()
            .map { it.toStudyArea() }
}

// JsonPrimitive accepts either database-generated numeric IDs or UUID/text IDs.
@Serializable
private data class StudyAreaRow(
    val id: JsonPrimitive,
    val name: String,
    val building: String,
    val zone: String,
    val power: String,
    val address: String? = null,
    val description: String? = null,
    @SerialName("source_url") val sourceUrl: String? = null,
    val access: String? = null,
    val campus: String? = null,
    @SerialName("verified_on") val verifiedOn: String? = null
) {
    fun toStudyArea() = StudyArea(
        id = id.content,
        name = name,
        building = building,
        zone = zone,
        power = power,
        address = address.orEmpty(),
        description = description.orEmpty(),
        sourceUrl = sourceUrl.orEmpty(),
        access = access ?: "Check official access information",
        campus = campus ?: "Charles River",
        verifiedOn = verifiedOn.orEmpty()
    )
}
