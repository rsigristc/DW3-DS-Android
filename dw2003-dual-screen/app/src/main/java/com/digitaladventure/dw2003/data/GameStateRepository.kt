package com.digitaladventure.dw2003.data

import com.digitaladventure.dw2003.model.GameMode
import com.digitaladventure.dw2003.model.GameSnapshot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GameStateRepository {
    private val mutableSnapshot = MutableStateFlow(GameSnapshot.waiting())
    val snapshot: StateFlow<GameSnapshot> = mutableSnapshot.asStateFlow()

    fun publish(snapshot: GameSnapshot) {
        mutableSnapshot.value = snapshot
    }

    fun showDemo(mode: GameMode = mutableSnapshot.value.mode) {
        mutableSnapshot.value = GameSnapshot.demo(mode)
    }
}
