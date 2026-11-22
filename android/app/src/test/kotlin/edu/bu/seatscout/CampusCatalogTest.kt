package edu.bu.seatscout

import java.net.URI
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CampusCatalogTest {
    @Test
    fun catalogPreservesStoredIdsAndHasOfficialSources() {
        val ids = sampleAreas.map { it.id }
        assertEquals(ids.size, ids.distinct().size)
        assertTrue(ids.containsAll(listOf("mugar4", "pal", "stone", "pardee", "science")))
        sampleAreas.forEach { area ->
            val source = URI(area.sourceUrl)
            assertEquals("https", source.scheme)
            assertTrue(source.host == "bu.edu" || source.host.endsWith(".bu.edu"))
            assertTrue(area.address.isNotBlank())
            assertTrue(area.access.isNotBlank())
            assertTrue(area.description.isNotBlank())
        }
        assertEquals(setOf("Charles River", "Fenway", "Medical"), sampleAreas.map { it.campus }.toSet())
    }
}
