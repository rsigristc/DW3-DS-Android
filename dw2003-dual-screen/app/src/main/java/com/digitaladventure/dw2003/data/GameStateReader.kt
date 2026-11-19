package com.digitaladventure.dw2003.data

import com.digitaladventure.dw2003.model.AuctionNotice
import com.digitaladventure.dw2003.model.DigievolutionForm
import com.digitaladventure.dw2003.model.DigimonState
import com.digitaladventure.dw2003.model.GameMode
import com.digitaladventure.dw2003.model.HarmonyMemberData
import com.digitaladventure.dw2003.model.GameSnapshot
import com.digitaladventure.dw2003.model.RamProbe

class GameStateReader(private val harmonyCatalog: HarmonyCatalog? = null) {
    private val locationTracker = LocationTracker()

    fun parse(
        main: ByteArray,
        overlaySignature: Long,
        objective: String?,
        features: CompanionRomFeatures = CompanionRomFeatures.PAL,
        overlayStageId: Int? = null,
        battleSetup: ByteArray? = null,
        battleArena: ByteArray? = null,
        battleScan: ByteArray? = null,
        spanishNames: Boolean = true,
        ramProbe: RamProbe? = null,
        overlaySlot: Long = overlaySignature,
        fieldMenuVisible: Boolean? = null,
        flaweMapLoaded: Boolean = false,
        auction: AuctionNotice? = null,
        guideKey: String? = null,
        harmonyActive: Boolean = false,
        harmonyPartner: ByteArray? = null
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
        val mode = OverlaySignatures.mode(overlaySignature, overlaySlot)
        val scene = OverlaySceneResolver.resolve(mode, areaId, mapId, fieldMenuVisible, flaweMapLoaded)
        val publicMapId = if (sessionLooksValid) {
            locationTracker.follow(
                areaId,
                mapId,
                overlayStageId.takeUnless { mode == GameMode.BATTLE }
            )
        } else {
            0
        }
        val location = LocationResolver.resolve(areaId, mapId, publicMapId)
        val mapRegion = MapRegionCatalog.resolve(location.publicMapId)
        val region = if (mapRegion.server == ServerRegion.UNKNOWN) MapRegionCatalog.resolve(areaId) else mapRegion
        val catalog = harmonyCatalog.takeIf { harmonyActive && features.ramBase == CompanionRomFeatures.PAL_RAM_BASE }
        val hasNinth = catalog?.validPartner(harmonyPartner) == true
        val activeProfiles = ACTIVE_PARTY.mapNotNull { address ->
            val profile = u32(main, address - MAIN_BASE).toInt()
            profile.takeIf { it in 0..7 || (it == 8 && hasNinth) }
        }.distinct()

        val story = if (sessionLooksValid) rawStory else 0
        // The title/language screens leave the party pointers at zero. Zero is also
        // Kotemon's valid profile id, so the session gate must be evaluated before
        // interpreting those pointers as a formation.
        val gameStarted = sessionLooksValid && (areaId != 0 || story != 0)
        val party = if (gameStarted) {
            activeProfiles.map { profile ->
                if (profile == 8) parseDigimon(harmonyPartner!!, profile, catalog, spanishNames, 12)
                else parseDigimon(main, profile, catalog, spanishNames)
            }.filter(::isPlausiblePartyMember)
        } else {
            emptyList()
        }
        return GameSnapshot(
            mode = mode,
            scene = scene,
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
            auction = auction.takeIf { gameStarted && features.supportsWalkthrough },
            dri = if (gameStarted && features.supportsWalkthrough) {
                DriCatalog.forParty(guideKey, party)
            } else {
                emptyList()
            },
            party = party,
            enemies = if (mode == GameMode.BATTLE) {
                BattleSetupReader.parse(battleSetup ?: ByteArray(0), battleArena, battleScan, spanishNames)
            } else {
                emptyList()
            },
            ramProbe = ramProbe,
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
            supportsHarmony = catalog != null,
            harmonyRevision = catalog?.revision,
            revealedMapIds = if (gameStarted) FlaweRevealedIcons.fromMain(main, story) else emptySet(),
            isLive = true,
            playTimeSeconds = if (gameStarted) u32(main, PLAY_TIME_SECONDS - MAIN_BASE) else 0
        )
    }

    private fun isPlausiblePartyMember(value: DigimonState): Boolean =
        value.level in 1..99 && value.maxHp in 1..9999 && value.maxMp in 0..9999 &&
            value.currentHp in 0..value.maxHp && value.currentMp in 0..value.maxMp

    private fun parseDigimon(
        main: ByteArray, profileId: Int, catalog: HarmonyCatalog? = null,
        spanish: Boolean = true, base: Int = STATS - MAIN_BASE + profileId * PROFILE_STRIDE
    ): DigimonState {
        val activeDigievolutionId = u16(main, base - 4)
        val activeDigievolutionLevel = findDigievolutionLevel(main, base, activeDigievolutionId)
        val partnerLevel = u16(main, base + 0x1C)
        val maxHp = u16(main, base + 0x22)
        val maxMp = u16(main, base + 0x26)
        val rookieId = catalog?.rookieId(profileId)
        val rookieName = rookieId?.let { catalog?.profile(it)?.name } ?: DIGIMON_NAMES[profileId]
        val formId = if (activeDigievolutionId == 0 || activeDigievolutionId == 0xFFFF) rookieId else activeDigievolutionId
        val form = formId?.let { catalog?.profile(it) }
        val equipment = (0 until 6).map { u16(main, base + 0x3C0 + it * 2) }
        val harmonySkills = if (catalog != null && form != null) {
            if (formId == rookieId) {
                form.skills.filter { it.signature }.mapNotNull { catalog.technique(form.id, it.id, 0, equipment, spanish) }
            } else {
                val record = (0 until DIGIEVOLUTION_RECORDS).map { base + DIGIEVOLUTION_OFFSET + it * DIGIEVOLUTION_STRIDE }
                    .firstOrNull { u16(main, it) == activeDigievolutionId }
                if (record == null) emptyList() else (0 until 6).mapNotNull { index ->
                    val encoded = u16(main, record + 8 + index * 2)
                    val id = encoded and 0x1FFF
                    if (id == 0) null else catalog.technique(form.id, id, encoded, equipment, spanish)
                }.distinctBy { it.name }
            }
        } else emptyList()
        val unlockedForms = parseUnlockedForms(main, base, rookieName, partnerLevel, activeDigievolutionId, catalog, profileId, spanish)
        val equippedIds = List(3) { u16(main, base + DIGIEVOLUTION_SLOTS_OFFSET + it * 2) }.distinct()
        val equippedForms = equippedIds.mapNotNull { id ->
            unlockedForms.firstOrNull { it.id == id && it.id != 0 }
        }
        return DigimonState(
            profileId = profileId,
            name = rookieName,
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
            equipmentIds = equipment,
            activeDigievolutionId = activeDigievolutionId,
            activeDigievolutionLevel = activeDigievolutionLevel,
            unlockedForms = unlockedForms,
            equippedForms = equippedForms,
            harmonyData = form?.let {
                val detail = buildString {
                    append(if (spanish) "TÉCNICAS APRENDIDAS (equipo actual)\n" else "LEARNED TECHNIQUES (current equipment)\n")
                    harmonySkills.forEach { skill ->
                        append("• ${skill.name} · MP ${skill.mp ?: "—"} · ${if (spanish) "Poder" else "Power"} ${skill.power ?: "—"}\n")
                    }
                    append("\n").append(catalog?.details(profileId, it.id, spanish).orEmpty())
                }
                HarmonyMemberData(it.name, harmonySkills, it.status, detail)
            }
        )
    }

    private fun findDigievolutionLevel(main: ByteArray, base: Int, activeId: Int): Int {
        if (activeId == 0 || activeId == 0xFFFF) return 1
        repeat(DIGIEVOLUTION_RECORDS) { index ->
            val record = base + DIGIEVOLUTION_OFFSET + index * DIGIEVOLUTION_STRIDE
            if (u16(main, record) == activeId) return (main[record + 2].toInt() and 0xFF).coerceIn(1, 99)
        }
        return 1
    }

    private fun parseUnlockedForms(
        main: ByteArray,
        base: Int,
        rookieName: String,
        partnerLevel: Int,
        activeId: Int,
        catalog: HarmonyCatalog? = null, member: Int = 0, spanish: Boolean = true
    ): List<DigievolutionForm> {
        val rookieActive = activeId == 0 || activeId == 0xFFFF || (member == 8 && activeId == 0x6100)
        val forms = mutableListOf(DigievolutionForm(0, rookieName, partnerLevel, rookieActive, catalog?.rookieId(member)?.let { catalog?.details(member, it, spanish) }))
        val seen = mutableSetOf<Int>()
        repeat(DIGIEVOLUTION_RECORDS) { index ->
            val record = base + DIGIEVOLUTION_OFFSET + index * DIGIEVOLUTION_STRIDE
            val id = u16(main, record)
            val skillLevel = main[record + 2].toInt() and 0xFF
            if (id == 0 || id == 0xFFFF || skillLevel !in 1..99 || !seen.add(id)) return@repeat
            val name = catalog?.profile(id)?.name ?: DigievolutionCatalog.name(id) ?: "Forma 0x${id.toString(16).uppercase()}"
            forms += DigievolutionForm(
                id = id,
                name = name,
                level = skillLevel,
                active = !rookieActive && id == activeId,
                details = catalog?.details(member, id, spanish)
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
        const val PLAY_TIME_SECONDS = 0x48D7C
        const val STATS = 0x4949C
        const val STORY_STAGE = 0x4B370
        const val MAP_ID = 0x4B3F8
        const val PAL_LANGUAGE = PalLanguage.ADDRESS
        const val PROFILE_STRIDE = 0x3DC
        const val DIGIEVOLUTION_SLOTS_OFFSET = 0x48
        const val DIGIEVOLUTION_OFFSET = 0x50
        const val DIGIEVOLUTION_STRIDE = 0x14
        const val DIGIEVOLUTION_RECORDS = 44
        val ACTIVE_PARTY = listOf(0x48DA4, 0x48DA8, 0x48DAC)
        val DIGIMON_NAMES = listOf("Kotemon", "Kumamon", "Monmon", "Agumon", "Veemon", "Guilmon", "Renamon", "Patamon", "Terriermon")
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
