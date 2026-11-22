package edu.bu.seatscout

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PhotoMatchTest {
    @Test fun buildingSignDoesNotAssertASpecificFloor() {
        val found = matchPhotoLocations("BU MUGAR MEMORIAL LIBRARY", sampleAreas).single()
        assertEquals("Mugar Memorial Library", found.area.building)
        assertFalse(found.roomNamed)
    }

    @Test fun namedRoomAppearsOnBuildingPin() {
        val found = matchPhotoLocations("PAL\nSTUDY LOUNGE", sampleAreas).single()
        assertEquals("pal", found.area.id)
        assertTrue(found.roomNamed)
    }

    @Test fun streetAbbreviationsPreserveMultipleBuildingsAtTheSameAddress() {
        val found = matchPhotoLocations("725 Commonwealth Ave.", sampleAreas)
        assertEquals(2, found.map { it.area.building }.distinct().size)
    }

    @Test fun genericSignAndPartialNumberDoNotMatch() {
        assertTrue(matchPhotoLocations("Study lounge / quiet study room", sampleAreas).isEmpty())
        assertTrue(matchPhotoLocations("1771 Commonwealth Ave", sampleAreas).isEmpty())
    }
}
