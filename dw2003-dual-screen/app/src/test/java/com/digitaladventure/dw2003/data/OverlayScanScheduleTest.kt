package com.digitaladventure.dw2003.data

import org.junit.Assert.*
import org.junit.Test

class OverlayScanScheduleTest {
    @Test fun stableLocationStopsScanningAfterTransitionRetries() {
        val schedule = OverlayScanSchedule()
        val scans = (0L..60_000L step 200L).count { schedule.shouldScan(0x230, 1L, it) }
        assertEquals(3, scans)
    }

    @Test fun newLocationsRetryButRapidTransitionsStayRateLimited() {
        val schedule = OverlayScanSchedule()
        assertTrue(schedule.shouldScan(1, 1, 0))
        assertFalse(schedule.shouldScan(2, 2, 200))
        assertFalse(schedule.shouldScan(3, 3, 400))
        assertTrue(schedule.shouldScan(3, 3, 600))
        assertTrue(schedule.shouldScan(3, 3, 1200))
        assertTrue(schedule.shouldScan(3, 3, 1800))
        assertFalse(schedule.shouldScan(3, 3, 2400))
    }
}
