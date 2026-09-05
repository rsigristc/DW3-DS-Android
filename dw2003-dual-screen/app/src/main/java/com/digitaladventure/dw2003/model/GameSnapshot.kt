package com.digitaladventure.dw2003.model

data class GameSnapshot(
    val mode: GameMode,
    val areaId: Int,
    val areaName: String,
    val locationTitle: String,
    val locationDetail: String,
    val radarLabel: String,
    val locationRoom: String?,
    val mapId: Int,
    val mapName: String,
    val serverName: String,
    val sectorName: String,
    val tamerName: String,
    val storyStage: Int,
    val objective: String,
    val party: List<DigimonState>,
    val bits: Long,
    val fishingAvailable: Boolean,
    val isFishing: Boolean,
    val gameStarted: Boolean,
    val canReorderParty: Boolean,
    val canFastTravel: Boolean,
    val supportsWalkthrough: Boolean = true,
    val supportsFastTravel: Boolean = true,
    val isLive: Boolean,
    val sampledAtMillis: Long = System.currentTimeMillis()
) {
    companion object {
        fun waiting() = GameSnapshot(
            mode = GameMode.EXPLORATION,
            areaId = 0,
            areaName = "Área desconocida",
            locationTitle = "Área desconocida",
            locationDetail = "Sector desconocido · Área desconocida · 0x0000",
            radarLabel = "Sector desconocido · Área desconocida",
            locationRoom = null,
            mapId = 0,
            mapName = "Mapa desconocido",
            serverName = "Servidor desconocido",
            sectorName = "Sector desconocido",
            tamerName = "—",
            storyStage = 0,
            objective = "Inicia o carga una partida para activar el panel complementario.",
            party = emptyList(),
            bits = 0,
            fishingAvailable = false,
            isFishing = false,
            gameStarted = false,
            canReorderParty = false,
            canFastTravel = false,
            supportsWalkthrough = true,
            supportsFastTravel = true,
            isLive = true
        )

        fun demo(mode: GameMode = GameMode.EXPLORATION) = GameSnapshot(
            mode = mode,
            areaId = 0x021F,
            areaName = "Central Park",
            locationTitle = "Playa de Conchas",
            locationDetail = "Sector Central · Playa de Conchas · 0x021F",
            radarLabel = "Sector Central · Playa de Conchas",
            locationRoom = null,
            mapId = 0x021F,
            mapName = "Playa de Conchas",
            serverName = "Servidor Asuka",
            sectorName = "Sector Central",
            tamerName = "Junior",
            storyStage = 12,
            objective = "Explora Central Park y localiza la entrada al Bosque Alambre.",
            party = listOf(
                DigimonState(
                    5, "Guilmon", 28, 18420, 12, 1280, 1280, 680, 680, 119, 91, 104, 87, 96, 72,
                    listOf(20, 8, 4, 12, 16, 5, 9), listOf(148, 0, 238, 0, 331, 0),
                    activeDigievolutionId = 367,
                    activeDigievolutionLevel = 25,
                    unlockedForms = listOf(
                        DigievolutionForm(0, "Guilmon", 28, false),
                        DigievolutionForm(367, "Growlmon", 25, true)
                    )
                ),
                DigimonState(6, "Renamon", 27, 16940, 9, 1420, 1420, 540, 540, 93, 88, 126, 118, 121, 80, listOf(8, 11, 13, 16, 12, 7, 10), listOf(162, 0, 245, 0, 332, 0)),
                DigimonState(3, "Agumon", 26, 15120, 7, 1180, 1180, 620, 620, 125, 98, 82, 75, 89, 76, listOf(18, 6, 5, 8, 10, 7, 4), listOf(134, 0, 240, 0, 330, 0))
            ),
            bits = 24_560,
            fishingAvailable = true,
            isFishing = true,
            gameStarted = true,
            canReorderParty = true,
            canFastTravel = true,
            supportsWalkthrough = true,
            supportsFastTravel = true,
            isLive = false
        )
    }
}
