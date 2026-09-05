package com.digitaladventure.dw2003.data

import com.digitaladventure.dw2003.model.DigievolutionForm
import com.digitaladventure.dw2003.model.DigimonState
import com.digitaladventure.dw2003.model.GameMode
import com.digitaladventure.dw2003.model.GameSnapshot

class GameStateReader {
    private val locationTracker = LocationTracker()

    fun parse(
        main: ByteArray,
        overlaySignature: Long,
        objective: String?,
        features: CompanionRomFeatures = CompanionRomFeatures.PAL,
        overlayStageId: Int? = null
    ): GameSnapshot {
        require(main.size >= MAIN_LENGTH) { "Incomplete DW2003 RAM window" }

        val rawAreaId = u16(main, AREA - MAIN_BASE)
        val rawMapId = u16(main, MAP_ID - MAIN_BASE)
        val rawStory = u16(main, STORY_STAGE - MAIN_BASE)
        val sessionLooksValid = rawStory in 0..300 && (
            (rawAreaId == 0 && rawMapId == 0) ||
                AreaCatalog.knownName(rawAreaId) != null ||
                AreaCatalog.knownName(rawMapId) != null
            )
        val areaId = if (sessionLooksValid) rawAreaId else 0
        val mapId = if (sessionLooksValid) rawMapId.takeIf { it != 0 } ?: areaId else 0
        val publicMapId = if (sessionLooksValid) {
            locationTracker.follow(areaId, mapId, overlayStageId)
        } else {
            0
        }
        val location = LocationResolver.resolve(areaId, mapId, publicMapId)
        val mapRegion = MapRegionCatalog.resolve(location.publicMapId)
        val region = if (mapRegion.server == ServerRegion.UNKNOWN) MapRegionCatalog.resolve(areaId) else mapRegion
        val mode = when (overlaySignature) {
            FIGHTST2_SIGNATURE -> GameMode.BATTLE
            STSTATUS_SIGNATURE -> GameMode.MANAGEMENT
            else -> GameMode.EXPLORATION
        }
        val activeProfiles = ACTIVE_PARTY.mapNotNull { address ->
            val profile = u32(main, address - MAIN_BASE).toInt()
            profile.takeIf { it in DIGIMON_NAMES.indices }
        }.distinct()

        val story = if (sessionLooksValid) rawStory else 0
        // The title/language screens leave the party pointers at zero. Zero is also
        // Kotemon's valid profile id, so the session gate must be evaluated before
        // interpreting those pointers as a formation.
        val gameStarted = sessionLooksValid && (areaId != 0 || story != 0)
        val party = if (gameStarted) {
            activeProfiles.map { parseDigimon(main, it) }.filter(::isPlausiblePartyMember)
        } else {
            emptyList()
        }
        return GameSnapshot(
            mode = mode,
            areaId = areaId,
            areaName = AreaCatalog.name(areaId),
            locationTitle = location.title,
            locationDetail = location.detail,
            radarLabel = location.radarLabel,
            locationRoom = location.roomName,
            mapId = mapId,
            publicMapId = location.publicMapId,
            mapName = location.mapLabel,
            serverName = region.server.label,
            sectorName = region.sector.label,
            tamerName = if (gameStarted) parseTamerName(main) else "—",
            storyStage = story,
            objective = when {
                !gameStarted -> WalkthroughCatalog.START_PROMPT_ES
                !features.supportsWalkthrough -> WalkthroughCatalog.UNAVAILABLE_ES
                else -> objective?.takeIf { it.length >= 4 } ?: WalkthroughCatalog.SYNC_PROMPT_ES
            },
            party = party,
            bits = if (gameStarted) u32(main, BITS - MAIN_BASE) else 0,
            fishingAvailable = AreaCatalog.supportsFishing(location.publicMapId) ||
                AreaCatalog.supportsFishing(areaId),
            // FIELDSTG has no dedicated fishing overlay in ddw3; the tamer stays
            // on the field module. A live RAM dump while fishing is still needed
            // before this flag can flip automatically.
            isFishing = false,
            gameStarted = gameStarted,
            canReorderParty = LocationResolver.canReorderParty(areaId, mapId, mode, gameStarted),
            canFastTravel = features.supportsFastTravel &&
                LocationResolver.canFastTravel(areaId, mapId, mode, gameStarted),
            supportsWalkthrough = features.supportsWalkthrough,
            supportsFastTravel = features.supportsFastTravel,
            isLive = true
        )
    }

    private fun isPlausiblePartyMember(value: DigimonState): Boolean =
        value.level in 1..99 && value.maxHp in 1..9999 && value.maxMp in 0..9999 &&
            value.currentHp in 0..value.maxHp && value.currentMp in 0..value.maxMp

    private fun parseDigimon(main: ByteArray, profileId: Int): DigimonState {
        val base = STATS - MAIN_BASE + profileId * PROFILE_STRIDE
        val activeDigievolutionId = u16(main, base - 4)
        val activeDigievolutionLevel = findDigievolutionLevel(main, base, activeDigievolutionId)
        val partnerLevel = u16(main, base + 0x1C)
        val maxHp = u16(main, base + 0x22)
        val maxMp = u16(main, base + 0x26)
        return DigimonState(
            profileId = profileId,
            name = DIGIMON_NAMES[profileId],
            experience = u32(main, base + 0x18),
            level = partnerLevel,
            trainingPoints = u16(main, base + 0x1E),
            currentHp = u16(main, base + 0x20).coerceAtMost(maxHp),
            maxHp = maxHp,
            currentMp = u16(main, base + 0x24).coerceAtMost(maxMp),
            maxMp = maxMp,
            strength = u16(main, base + 0x28),
            defense = u16(main, base + 0x2A),
            spirit = u16(main, base + 0x2C),
            wisdom = u16(main, base + 0x2E),
            speed = u16(main, base + 0x30),
            charisma = u16(main, base + 0x32),
            tolerances = (0 until 7).map { u16(main, base + 0x34 + it * 2) },
            equipmentIds = (0 until 6).map { u16(main, base + 0x3C0 + it * 2) },
            activeDigievolutionId = activeDigievolutionId,
            activeDigievolutionLevel = activeDigievolutionLevel,
            unlockedForms = parseUnlockedForms(
                main,
                base,
                DIGIMON_NAMES[profileId],
                partnerLevel,
                activeDigievolutionId
            )
        )
    }

    private fun findDigievolutionLevel(main: ByteArray, base: Int, activeId: Int): Int {
        if (activeId == 0 || activeId == 0xFFFF) return 1
        repeat(DIGIEVOLUTION_RECORDS) { index ->
            val record = base + DIGIEVOLUTION_OFFSET + index * DIGIEVOLUTION_STRIDE
            if (u16(main, record) == activeId) return u16(main, record + 2).coerceIn(1, 99)
        }
        return 1
    }

    private fun parseUnlockedForms(
        main: ByteArray,
        base: Int,
        rookieName: String,
        partnerLevel: Int,
        activeId: Int
    ): List<DigievolutionForm> {
        val rookieActive = activeId == 0 || activeId == 0xFFFF
        val forms = mutableListOf(DigievolutionForm(0, rookieName, partnerLevel, rookieActive))
        val seen = mutableSetOf<Int>()
        repeat(DIGIEVOLUTION_RECORDS) { index ->
            val record = base + DIGIEVOLUTION_OFFSET + index * DIGIEVOLUTION_STRIDE
            val id = u16(main, record)
            val skillLevel = u16(main, record + 2)
            if (id == 0 || id == 0xFFFF || skillLevel !in 1..99 || !seen.add(id)) return@repeat
            val name = DigievolutionCatalog.name(id) ?: "Forma 0x${id.toString(16).uppercase()}"
            forms += DigievolutionForm(
                id = id,
                name = name,
                level = skillLevel,
                active = !rookieActive && id == activeId
            )
        }
        return forms
    }

    private fun parseTamerName(main: ByteArray): String {
        val start = PLAYER_NAME - MAIN_BASE
        val decoded = DwTextDecoder.decode(main.copyOfRange(start, (start + PLAYER_NAME_LENGTH).coerceAtMost(main.size)))
        return decoded.takeIf { name ->
            name.length in 1..10 && name.count { it.isLetterOrDigit() } >= (name.length + 1) / 2
        } ?: "Junior"
    }

    companion object {
        const val MAIN_BASE = 0x48D00
        const val MAIN_LENGTH = 0x2800
        const val OVERLAY_BASE = 0x80000
        const val SCRATCH_BASE = 0x0B200
        const val SCRATCH_LENGTH = 0x40
        const val AREA = 0x48D68
        const val PLAYER_NAME = 0x48D88
        const val PLAYER_NAME_LENGTH = 12
        const val BITS = 0x48DA0
        const val STATS = 0x4949C
        const val STORY_STAGE = 0x4B370
        const val MAP_ID = 0x4B3F8
        const val PAL_LANGUAGE = PalLanguage.ADDRESS
        const val PROFILE_STRIDE = 0x3DC
        const val DIGIEVOLUTION_OFFSET = 0x50
        const val DIGIEVOLUTION_STRIDE = 0x14
        const val DIGIEVOLUTION_RECORDS = 44
        val ACTIVE_PARTY = listOf(0x48DA4, 0x48DA8, 0x48DAC)
        val DIGIMON_NAMES = listOf("Kotemon", "Kumamon", "Monmon", "Agumon", "Veemon", "Guilmon", "Renamon", "Patamon")
        const val FIGHTST2_SIGNATURE = 0x80085390L
        const val STSTATUS_SIGNATURE = 0x8008428CL

        fun u16(bytes: ByteArray, offset: Int): Int {
            if (offset !in bytes.indices || offset + 1 !in bytes.indices) return 0
            return (bytes[offset].toInt() and 0xFF) or ((bytes[offset + 1].toInt() and 0xFF) shl 8)
        }

        fun u32(bytes: ByteArray, offset: Int): Long {
            if (offset !in bytes.indices || offset + 3 !in bytes.indices) return 0
            return (bytes[offset].toLong() and 0xFF) or
                ((bytes[offset + 1].toLong() and 0xFF) shl 8) or
                ((bytes[offset + 2].toLong() and 0xFF) shl 16) or
                ((bytes[offset + 3].toLong() and 0xFF) shl 24)
        }
    }
}
