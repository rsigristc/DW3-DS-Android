package com.digitaladventure.dw2003.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ExperienceTableTest {
    @Test
    fun returnsCumulativeThresholdAndLevelProgress() {
        assertEquals(10L, ExperienceTable.nextLevel(profileId = 0, level = 1))
        assertEquals(0.5f, ExperienceTable.progress(profileId = 0, level = 1, experience = 5), 0.0001f)
    }

    @Test
    fun maxLevelHasNoNextThreshold() {
        assertNull(ExperienceTable.nextLevel(profileId = 3, level = 99))
        assertEquals(1f, ExperienceTable.progress(profileId = 3, level = 99, experience = 1_000_000), 0.0001f)
    }
}
