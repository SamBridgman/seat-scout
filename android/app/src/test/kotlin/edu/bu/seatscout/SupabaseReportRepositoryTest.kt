package edu.bu.seatscout

import edu.bu.seatscout.data.SupabaseReportRepository
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class SupabaseReportRepositoryTest {
    private val area = StudyArea("42", "Study", "Library", "Silent", "Available")

    @Test fun failedUploadDoesNotSaveLocalReport() = runBlocking {
        val dao = FakeDao()
        val failure = IllegalStateException("Upload rejected")
        val repository = SupabaseReportRepository(dao) { _, _, _ -> throw failure }
        try {
            repository.save(area, Seats.FEW, Atmosphere.QUIET)
            fail("Expected upload failure")
        } catch (error: IllegalStateException) {
            assertSame(failure, error)
        }
        assertNull(dao.saved)
    }

    @Test fun localReportUsesConfirmedServerTimestamp() = runBlocking {
        val dao = FakeDao()
        val confirmed = Report(area.id, Seats.FEW, Atmosphere.QUIET, 123456789L)
        val repository = SupabaseReportRepository(dao) { _, _, _ ->
            assertNull(dao.saved)
            confirmed
        }
        repository.save(area, Seats.FEW, Atmosphere.QUIET)
        assertEquals(confirmed, dao.saved)
    }

    private class FakeDao : ReportDao {
        var saved: Report? = null
        override fun observeReports() = flowOf(emptyList<Report>())
        override suspend fun save(report: Report) { saved = report }
    }
}
