package edu.bu.seatscout

internal data class StudyArea(
    val id: String,
    val name: String,
    val building: String,
    val zone: String,
    val power: String,
    val address: String = "",
    val description: String = "",
    val sourceUrl: String = "",
    val access: String = "Check official access information",
    val campus: String = "Charles River",
    val verifiedOn: String = ""
)
