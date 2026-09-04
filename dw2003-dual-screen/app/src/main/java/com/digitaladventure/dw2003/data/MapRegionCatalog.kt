package com.digitaladventure.dw2003.data

enum class ServerRegion(val label: String) { ASUKA("Servidor Asuka"), AMATERASU("Servidor Amaterasu"), UNKNOWN("Servidor desconocido") }
enum class SectorRegion(val label: String) { CENTRAL("Sector Central"), EAST("Sector Este"), SOUTH("Sector Sur"), WEST("Sector Oeste"), NORTH("Sector Norte"), UNKNOWN("Sector desconocido") }
data class MapRegion(val server: ServerRegion, val sector: SectorRegion)

/**
 * Sector bounds follow the Wikimon area lists, not a raw ID cutoff.
 * Asuka Central stops at Wire Forest Entrance / Shell Beach / Plug Cape;
 * Hangyomon's Pond (Lago de Divermon) and Wire Forest belong to East.
 */
object MapRegionCatalog {
    fun resolve(mapId: Int): MapRegion {
        val server = when (mapId) {
            in 0x0200..0x02FF -> ServerRegion.ASUKA
            in 0x0780..0x089F -> ServerRegion.AMATERASU
            else -> ServerRegion.UNKNOWN
        }
        if (server == ServerRegion.UNKNOWN) return MapRegion(server, SectorRegion.UNKNOWN)
        return MapRegion(server, sector(server, mapId))
    }

    private fun sector(server: ServerRegion, mapId: Int): SectorRegion = when (server) {
        ServerRegion.ASUKA -> when (mapId) {
            in 0x0200..0x0220 -> SectorRegion.CENTRAL
            in 0x0221..0x0231 -> SectorRegion.EAST
            in 0x0232..0x0246 -> SectorRegion.SOUTH
            in 0x0247..0x0260 -> SectorRegion.WEST
            in 0x0261..0x02DF -> SectorRegion.NORTH
            else -> SectorRegion.UNKNOWN
        }
        ServerRegion.AMATERASU -> when (mapId) {
            in 0x0780..0x080F -> SectorRegion.CENTRAL
            in 0x0810..0x0824 -> SectorRegion.EAST
            in 0x0825..0x0844 -> SectorRegion.SOUTH
            in 0x0845..0x0854 -> SectorRegion.WEST
            in 0x0855..0x089F -> SectorRegion.NORTH
            else -> SectorRegion.UNKNOWN
        }
        ServerRegion.UNKNOWN -> SectorRegion.UNKNOWN
    }
}
