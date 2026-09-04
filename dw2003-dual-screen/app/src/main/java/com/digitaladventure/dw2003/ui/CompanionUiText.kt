package com.digitaladventure.dw2003.ui

import com.digitaladventure.dw2003.data.AreaCatalog
import com.digitaladventure.dw2003.data.CheatSpec
import com.digitaladventure.dw2003.data.CompanionLanguage
import com.digitaladventure.dw2003.data.CompanionLanguageSetting
import com.digitaladventure.dw2003.data.LocationResolver
import com.digitaladventure.dw2003.data.MapRegionCatalog
import com.digitaladventure.dw2003.data.SectorRegion
import com.digitaladventure.dw2003.data.ServerRegion
import com.digitaladventure.dw2003.model.GameMode

object CompanionUiText {
    fun pick(language: CompanionLanguage, spanish: String, english: String): String =
        if (language == CompanionLanguage.ENGLISH) english else spanish

    fun mode(language: CompanionLanguage, mode: GameMode): String = when (mode) {
        GameMode.EXPLORATION -> pick(language, "Exploración", "Exploration")
        GameMode.BATTLE -> pick(language, "Batalla", "Battle")
        GameMode.MANAGEMENT -> pick(language, "Gestión", "Management")
    }

    fun server(language: CompanionLanguage, server: ServerRegion): String = when (server) {
        ServerRegion.ASUKA -> pick(language, "Servidor Asuka", "Asuka Server")
        ServerRegion.AMATERASU -> pick(language, "Servidor Amaterasu", "Amaterasu Server")
        ServerRegion.UNKNOWN -> pick(language, "Servidor desconocido", "Unknown server")
    }

    fun sector(language: CompanionLanguage, sector: SectorRegion): String = when (sector) {
        SectorRegion.CENTRAL -> pick(language, "Sector Central", "Central Sector")
        SectorRegion.EAST -> pick(language, "Sector Este", "East Sector")
        SectorRegion.SOUTH -> pick(language, "Sector Sur", "South Sector")
        SectorRegion.WEST -> pick(language, "Sector Oeste", "West Sector")
        SectorRegion.NORTH -> pick(language, "Sector Norte", "North Sector")
        SectorRegion.UNKNOWN -> pick(language, "Sector desconocido", "Unknown sector")
    }

    fun area(language: CompanionLanguage, areaId: Int): String {
        if (areaId == 0) return pick(language, "Área desconocida", "Unknown area")
        if (language == CompanionLanguage.SPANISH) return AreaCatalog.name(areaId)
        return englishAreas[areaId]
            ?: AreaCatalog.knownName(areaId)
            ?: "Area 0x${AreaCatalog.hex(areaId)}"
    }

    fun locationTitle(language: CompanionLanguage, areaId: Int, mapId: Int): String {
        val location = LocationResolver.resolve(areaId, mapId)
        return area(language, location.publicMapId)
    }

    fun locationRadar(language: CompanionLanguage, areaId: Int, mapId: Int): String {
        val location = LocationResolver.resolve(areaId, mapId)
        val mapRegion = MapRegionCatalog.resolve(location.publicMapId)
        val region = if (mapRegion.server == ServerRegion.UNKNOWN) {
            MapRegionCatalog.resolve(areaId)
        } else {
            mapRegion
        }
        return "${sector(language, region.sector)} · ${area(language, location.publicMapId)}"
    }

    fun locationDetail(language: CompanionLanguage, areaId: Int, mapId: Int): String {
        val location = LocationResolver.resolve(areaId, mapId)
        return "${locationRadar(language, areaId, mapId)} · 0x${AreaCatalog.hex(location.publicMapId)}"
    }

    fun languageSetting(language: CompanionLanguage, setting: CompanionLanguageSetting): String =
        when (setting) {
            CompanionLanguageSetting.AUTO -> pick(language, "Automático", "Automatic")
            CompanionLanguageSetting.SPANISH -> "Español"
            CompanionLanguageSetting.ENGLISH -> "English"
        }

    fun paneArrangement(language: CompanionLanguage, arrangement: PaneArrangement): String =
        when (arrangement) {
            PaneArrangement.AUTO -> pick(language, "Automático", "Automatic")
            PaneArrangement.GAME_TOP -> pick(language, "Juego arriba · panel abajo", "Game top · panel bottom")
            PaneArrangement.DASHBOARD_TOP -> pick(language, "Panel arriba · juego abajo", "Panel top · game bottom")
            PaneArrangement.GAME_LEFT -> pick(language, "Juego izquierda · panel derecha", "Game left · panel right")
            PaneArrangement.DASHBOARD_LEFT -> pick(language, "Panel izquierda · juego derecha", "Panel left · game right")
        }

    fun cheatLabel(language: CompanionLanguage, cheat: CheatSpec): String = when (cheat.id) {
        "infinite_bits" -> pick(language, "Bits máximos", "Max bits")
        "no_random_battles" -> pick(language, "Sin batallas aleatorias", "No random battles")
        "infinite_hp_battle" -> pick(language, "HP infinito en batalla", "Infinite HP in battle")
        "infinite_mp_battle" -> pick(language, "MP infinito en batalla", "Infinite MP in battle")
        else -> cheat.label
    }

    fun cheatDetail(language: CompanionLanguage, cheat: CheatSpec): String = when (cheat.id) {
        "infinite_bits" -> pick(
            language,
            "Mantiene 99.999.968 Bits en la RAM del Tamer.",
            "Keeps 99,999,968 Bits in the Tamer RAM."
        )
        "no_random_battles" -> pick(
            language,
            "Evita encuentros en el campo. No afecta jefes ni eventos.",
            "Prevents field encounters. Bosses and events are unchanged."
        )
        "infinite_hp_battle" -> pick(
            language,
            "Solo mientras el overlay de combate está activo.",
            "Only while the battle overlay is active."
        )
        "infinite_mp_battle" -> pick(
            language,
            "Solo mientras el overlay de combate está activo.",
            "Only while the battle overlay is active."
        )
        else -> cheat.detail
    }

    fun equipmentType(language: CompanionLanguage, value: String): String {
        if (language == CompanionLanguage.SPANISH) return value
        return value
            .replace("Arma 1 mano", "1H Weapon")
            .replace("Arma 2 manos", "2H Weapon")
            .replace("Cabeza", "Head")
            .replace("Cuerpo", "Body")
            .replace("Escudo", "Shield")
            .replace("Accesorio", "Accessory")
    }

    fun equipmentStats(language: CompanionLanguage, value: String): String {
        if (language == CompanionLanguage.SPANISH) return value
        return value
            .replace("FUEGO", "FIRE")
            .replace("FUE", "STR")
            .replace("ESP", "SPI")
            .replace("SAB", "WIS")
            .replace("VEL", "SPD")
            .replace("CAR", "CHA")
    }

    private val englishAreas = mapOf(
        0x1000 to "Menu Screen", 0x1300 to "Card Shop", 0x1400 to "Reward Screen",
        0x0500 to "Starter Pack Selection", 0x0C01 to "Save Screen", 0x0A00 to "Training",
        0x0D00 to "Piximon Screen", 0x0E03 to "Story Sequence", 0x0F00 to "Shop",
        0x0600 to "Battle",
        0x0200 to "Asuka City", 0x0201 to "Asuka City 2", 0x0202 to "Asuka Bridge",
        0x0203 to "Main Lobby", 0x0204 to "Main Lobby", 0x0206 to "Digimon Lab",
        0x0207 to "Registration Room", 0x0208 to "Arena Front Desk",
        0x0209 to "Digimon Arena",
        0x020A to "Asuka Inn 1F", 0x020B to "Underground Path", 0x020C to "Asuka Inn 2F",
        0x020D to "Smith's Shop", 0x020E to "Junk Shop", 0x020F to "Lamb Chop",
        0x0210 to "Cargo Tower", 0x0211 to "Yellow Cruiser", 0x0212 to "Underwater Tunnel",
        0x0213 to "El Dorado", 0x0214 to "Admin Center 1F",
        0x0215 to "Basement Stairs", 0x0216 to "Prison Tower",
        0x0217 to "Admin Center 2F", 0x0218 to "Master Room",
        0x0219 to "A.o.A Headquarters", 0x021A to "Admin Center B1F",
        0x021B to "Asuka Sewers", 0x021C to "Control Room",
        0x021D to "Central Park", 0x021E to "Wire Forest Entrance", 0x021F to "Shell Beach",
        0x0220 to "Plug Cape", 0x0221 to "West Wire Forest", 0x0222 to "East Wire Forest",
        0x0223 to "Forest Inn", 0x0224 to "Forest Inn Basement",
        0x0225 to "Protocol Forest", 0x0226 to "Protocol Ruins", 0x0227 to "Divermon's Lake",
        0x0228 to "Duel Island", 0x0229 to "Wind Prairie", 0x022A to "Kicking Forest",
        0x022B to "Tyranno Valley", 0x022C to "East Station", 0x022D to "Deep Crevice",
        0x022E to "Seiryu City", 0x022F to "Zephyr Tower", 0x0230 to "Seiryu Tower",
        0x0231 to "Gale Tower", 0x0232 to "South Station", 0x0233 to "Bulk Swamp",
        0x0234 to "Bulk Bridge", 0x0235 to "Bios Swamp", 0x0236 to "Reliability Spot",
        0x0237 to "Tranquil Swamp", 0x0238 to "Swamp Inn", 0x0239 to "Shaman House",
        0x023A to "Jungle Grave", 0x023B to "Phoenix Bay", 0x023C to "Ether Jungle",
        0x023D to "South Cape", 0x023E to "Suzaku City", 0x023F to "Suzaku Inn",
        0x0240 to "Suzaku Hall", 0x0241 to "Suzaku Underground Lake",
        0x0242 to "Jungle Shrine", 0x0243 to "Catacomb", 0x0244 to "Catacomb",
        0x0245 to "Bug Maze", 0x0246 to "Bug Maze Pit", 0x0247 to "South Badland",
        0x0248 to "Noise Desert", 0x0249 to "Pelche Oasis", 0x024A to "North Badland W",
        0x024B to "North Badland E", 0x024C to "Bullet Valley", 0x024D to "Dum Dum Factory",
        0x024E to "Duct Room 01", 0x024F to "Duct Room 02", 0x0250 to "Duct Room 03",
        0x0251 to "Duct Room 04", 0x0252 to "Operation Room", 0x0253 to "Secret Stairs",
        0x0254 to "Sewers", 0x0255 to "Secret Room", 0x0256 to "Pump Room", 0x0257 to "South Noise Desert",
        0x0258 to "Mobius Desert", 0x0259 to "Mobius Desert 2", 0x025A to "Mirage Tower",
        0x025B to "Mirage Hall", 0x025C to "Mirage Room", 0x025D to "Byakko City",
        0x025E to "Byakko Dome", 0x025F to "Storage Room", 0x0260 to "Underground Cave",
        0x0261 to "Boot Mountain", 0x0262 to "Snow Mountain", 0x0263 to "Mountain Inn",
        0x0264 to "Freeze Mountain", 0x0265 to "Kulon Mine", 0x0266 to "Lake of Ice",
        0x0267 to "Legendary Gym", 0x0268 to "Kulon Pit",
        0x0269 to "Kulon Weapons", 0x026A to "Ice Dungeon",
        0x026B to "Fire Dungeon", 0x026C to "Dark Dungeon",
        0x026D to "Chamber Room", 0x026E to "Battle Gate",
        0x026F to "Genbu City",
        0x02D7 to "Street Corner", 0x02D8 to "Online Center", 0x02D9 to "Chamber Room",
        0x02DA to "Magasta B1F", 0x02DB to "Magasta B2F", 0x02DC to "Magasta 1F",
        0x02DD to "Gunslinger 1F", 0x02DE to "Gunslinger 2F", 0x02DF to "Control Room",
        0x02E0 to "Undersea 0", 0x02E1 to "Undersea 1", 0x02E2 to "Undersea 2",
        0x02E3 to "Undersea 3", 0x02E4 to "Undersea 4", 0x02E5 to "Undersea 5",
        0x02E6 to "Undersea 6", 0x02E7 to "Undersea 7",
        0x0780 to "Amaterasu City", 0x0785 to "Amaterasu Bridge",
        0x0790 to "Amaterasu Inn 1F", 0x0795 to "Amaterasu Inn 2F",
        0x0800 to "Wedge's Shop", 0x0805 to "Amaterasu Sewers",
        0x0810 to "Qing Long City", 0x0820 to "Qing Long Tower",
        0x0825 to "Zhu Que City", 0x0830 to "Zhu Que Inn", 0x0835 to "Zhu Que Hall",
        0x0840 to "Zhu Que Underground Lake", 0x0845 to "Bai Hu City",
        0x0850 to "Bai Hu Dome", 0x0855 to "Xuan Wu City"
    )
}
