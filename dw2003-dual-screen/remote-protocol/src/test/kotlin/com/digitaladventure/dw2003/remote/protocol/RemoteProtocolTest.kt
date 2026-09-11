package com.digitaladventure.dw2003.remote.protocol

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RemoteProtocolTest {
    @Test
    fun inputRoundTripPreservesState() {
        val input = RemoteInputFrame(
            pairingCode = 123456,
            sessionId = 42L,
            sequence = 7,
            buttons = RemoteButton.CROSS.mask or RemoteButton.DPAD_UP.mask,
            leftX = -.75f,
            leftY = .5f,
            rightX = 1f,
            rightY = -1f,
            sentAtNanos = 99L
        )

        val decoded = RemoteProtocol.decodeInput(RemoteProtocol.encodeInput(input))!!

        assertEquals(input.pairingCode, decoded.pairingCode)
        assertEquals(input.sessionId, decoded.sessionId)
        assertEquals(input.sequence, decoded.sequence)
        assertEquals(input.buttons, decoded.buttons)
        assertEquals(input.leftX, decoded.leftX, .0001f)
        assertEquals(input.leftY, decoded.leftY, .0001f)
        assertEquals(input.rightX, decoded.rightX, .0001f)
        assertEquals(input.rightY, decoded.rightY, .0001f)
    }

    @Test
    fun telemetryRoundTripPreservesDashboardFields() {
        val telemetry = RemoteTelemetryFrame(
            pairingCode = 654321,
            sequence = 3,
            sampledAtMillis = 1000,
            mode = "BATALLA",
            gameStarted = true,
            areaId = 0x021f,
            mapId = 0x0200,
            locationTitle = "Central Park",
            locationDetail = "Sector Central",
            tamerName = "Junior",
            objective = "Encuentra a Guilmon",
            bits = 24_560,
            party = listOf(
                RemotePartyMember(
                    "Guilmon", 28, 900, 1000, 300, 500, 100L, 200L, "Growlmon",
                    parameters = listOf(RemoteValue("FUERZA", 120, 10)),
                    resistances = listOf(RemoteValue("FUEGO", 80, 5)),
                    statusResistances = listOf(RemoteValue("VENENO", 50)),
                    skills = listOf(RemoteSkill("Pyro Sphere", 20, 90)),
                    equipment = listOf(RemoteEquipment("CABEZA", "Red Cap", "Cabeza", "+27 DEF"))
                )
            ),
            canFastTravel = true,
            canReorderParty = true,
            enemies = listOf(
                RemoteEnemy(
                    "Kunemon", 6, 180, 220, "Static Elect", "Power Charge",
                    parameters = listOf(RemoteValue("FUERZA", 50)),
                    resistances = listOf(RemoteValue("RAYO", 160)),
                    statusResistances = listOf(RemoteValue("VENENO", 99))
                )
            ),
            travelDestinations = listOf(RemoteTravelDestination(0x200, "Ciudad Asuka", "ASUKA · CENTRAL", false)),
            modsEnabled = true,
            mods = listOf(RemoteMod(0, "Bits máximos", "RAM", true, false))
        )

        assertEquals(telemetry, RemoteProtocol.decodeTelemetry(RemoteProtocol.encodeTelemetry(telemetry)))
    }

    @Test
    fun commandRoundTripPreservesAction() {
        val command = RemoteCommandFrame(123456, 42L, 8, RemoteCommand.FAST_TRAVEL, 0x021d)

        assertEquals(command, RemoteProtocol.decodeCommand(RemoteProtocol.encodeCommand(command)))

        val custom = RemoteCommandFrame(
            123456,
            42L,
            9,
            RemoteCommand.ADD_CUSTOM_MOD,
            text = "Prueba",
            detail = "80012345 00FF"
        )
        assertEquals(custom, RemoteProtocol.decodeCommand(RemoteProtocol.encodeCommand(custom)))

        val move = RemoteCommandFrame(123456, 42L, 10, RemoteCommand.MOVE_PARTY, (2 shl 8) or 1)
        assertEquals(move, RemoteProtocol.decodeCommand(RemoteProtocol.encodeCommand(move)))
    }

    @Test
    fun malformedPacketsAreRejected() {
        assertNull(RemoteProtocol.decodeInput(ByteArray(4)))
        assertNull(RemoteProtocol.decodeTelemetry(byteArrayOf(1, 2, 3)))
    }
}
