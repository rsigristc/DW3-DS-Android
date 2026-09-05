package com.digitaladventure.dw2003.data

import com.digitaladventure.dw2003.model.GameSnapshot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GameStateRepositoryTest {
    @Test
    fun suppressesTimestampOnlyUpdatesButPublishesRealChanges() {
        val repository = GameStateRepository()
        val first = GameSnapshot.demo().copy(sampledAtMillis = 100)
        repository.publish(first)
        repository.publish(first.copy(sampledAtMillis = 200))
        assertEquals(100L, repository.snapshot.value.sampledAtMillis)
        repository.publish(first.copy(bits = first.bits + 1, sampledAtMillis = 300))
        assertEquals(300L, repository.snapshot.value.sampledAtMillis)
    }

    @Test
    fun keepsLastLiveSessionWhenALaterPollLooksUnstarted() {
        val repository = GameStateRepository()
        val live = GameSnapshot.demo().copy(isLive = true, gameStarted = true)
        repository.publish(live)
        repository.publish(GameSnapshot.waiting())

        assertTrue(repository.snapshot.value.gameStarted)
        assertEquals(live.tamerName, repository.snapshot.value.tamerName)
        assertEquals(live.bits, repository.snapshot.value.bits)
    }

    @Test
    fun stillPublishesTheFirstWaitingSnapshot() {
        val repository = GameStateRepository()
        repository.publish(GameSnapshot.waiting())
        assertTrue(!repository.snapshot.value.gameStarted)
    }
}
