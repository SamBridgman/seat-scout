package edu.bu.seatscout

internal data class CampusCoordinate(val latitude: Double, val longitude: Double)

// City of Boston SAMLocator, WGS84. Each address matched at score 100 on 2026-11-18.
// https://geocoder.boston.gov/arcgis/rest/services/SAMLocator/GeocodeServer
internal val campusCoordinates = mapOf(
    "100 Bay State Road" to CampusCoordinate(42.349896, -71.0978985),
    "154 Riverway" to CampusCoordinate(42.342795, -71.1055395),
    "162 Riverway" to CampusCoordinate(42.342579, -71.1057555),
    "2 Silber Way" to CampusCoordinate(42.349698, -71.1006795),
    "37 Pilgrim Road" to CampusCoordinate(42.342507, -71.105391),
    "38 Cummington Mall" to CampusCoordinate(42.348438, -71.1023085),
    "595 Commonwealth Avenue" to CampusCoordinate(42.349590, -71.099784),
    "640 Commonwealth Avenue" to CampusCoordinate(42.348852, -71.102322),
    "665 Commonwealth Avenue" to CampusCoordinate(42.349959, -71.1032265),
    "675 Commonwealth Avenue" to CampusCoordinate(42.350211, -71.105004),
    "72 East Concord Street" to CampusCoordinate(42.336207, -71.072487),
    "725 Commonwealth Avenue" to CampusCoordinate(42.350301, -71.105733),
    "771 Commonwealth Avenue" to CampusCoordinate(42.351066, -71.107947),
    "775 Commonwealth Avenue" to CampusCoordinate(42.350751, -71.1090405),
    "871 Commonwealth Avenue" to CampusCoordinate(42.351498, -71.1145755)
)
