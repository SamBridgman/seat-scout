package edu.bu.seatscout

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ReportTest {
    // Check freshness at save time, just before expiration, exactly at 30 minutes, and before the save time.
    @Test
    fun reportExpiresAtThirtyMinutes() {
        val savedAt = 100 * MINUTE_MS
        val report = Report("mugar4", Seats.FEW, Atmosphere.QUIET, savedAt)
        assertEquals(0L, report.freshAgeMinutes(savedAt))
        assertEquals(29L, report.freshAgeMinutes(savedAt + 30 * MINUTE_MS - 1))
        assertNull(report.freshAgeMinutes(savedAt + 30 * MINUTE_MS))
        assertNull(report.freshAgeMinutes(savedAt - 1))
    }
}
