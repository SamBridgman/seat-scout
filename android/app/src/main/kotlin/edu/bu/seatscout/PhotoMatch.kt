package edu.bu.seatscout

import java.util.Locale

internal data class PhotoPlace(val area: StudyArea, val roomNamed: Boolean)

private fun words(value: String) = value.lowercase(Locale.ROOT)
    .replace("&", " and ").replace("’", "").replace("'", "")
    .replace(Regex("[^a-z0-9]+"), " ").trim().split(" ").filter(String::isNotEmpty)
    .map { when (it) { "ave" -> "avenue"; "rd" -> "road"; "st" -> "street"; else -> it } }

// Generic words on campus signage should never identify a building or a floor.
private val common = setOf("the", "and", "for", "of", "university", "college", "school", "library",
    "memorial", "resources", "resource", "center", "family", "house", "general", "studies",
    "quiet", "silent", "open", "study", "room", "rooms", "seating", "floor", "level",
    "space", "spaces", "area", "reading", "group", "work", "lounge", "basement", "lower",
    "reservable", "team", "medical", "science", "sciences", "educational", "engineering")

internal fun matchPhotoLocations(ocr: String, areas: List<StudyArea>): List<PhotoPlace> {
    val tokens = words(ocr.take(24_000))
    if (tokens.isEmpty()) return emptyList()
    val tokenSet = tokens.toSet()
    val text = " " + tokens.joinToString(" ") + " "
    fun containsPhrase(phrase: List<String>) = phrase.isNotEmpty() &&
        text.contains(" " + phrase.joinToString(" ") + " ")

    val buildings = areas.groupBy { it.building }
    val tokensByBuilding = buildings.keys.associateWith { words(it).toSet() }
    val scores = mutableListOf<Pair<Int, PhotoPlace>>()
    for ((building, rooms) in buildings) {
        val name = words(building)
        val specific = name.filter { it.length >= 4 && it !in common && it in tokenSet }
        val buildingScore = when {
            containsPhrase(name) -> 5
            containsPhrase(words(rooms.first().address)) -> 4
            specific.any { token -> tokensByBuilding.values.count { token in it } == 1 } -> 2
            else -> 0
        }
        val namedRoom = rooms.firstOrNull {
            val room = words(it.name)
            containsPhrase(room) && (buildingScore > 0 ||
                room.any { token -> token.length >= 3 && token !in common && token.none(Char::isDigit) })
        }
        if (namedRoom != null || buildingScore > 0) {
            scores += (buildingScore + if (namedRoom != null) 3 else 0) to
                PhotoPlace(namedRoom ?: rooms.first(), namedRoom != null)
        }
    }
    return scores.sortedByDescending { it.first }.map { it.second }
}
