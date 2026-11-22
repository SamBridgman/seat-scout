package edu.bu.seatscout

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CampusMapCoverageTest {
    @Test
    fun everyStudyLocationHasAPinInsideTheCampusRegion() {
        assertEquals(sampleAreas.map { it.address }.toSet(), campusCoordinates.keys)
        for ((address, pin) in campusCoordinates) {
            assertTrue(address, pin.latitude in 42.333..42.356)
            assertTrue(address, pin.longitude in -71.119..-71.068)
        }
    }
}
