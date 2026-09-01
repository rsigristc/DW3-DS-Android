package com.digitaladventure.dw2003.data

enum class ServerRegion(val label: String) { ASUKA("Servidor Asuka"), AMATERASU("Servidor Amaterasu"), UNKNOWN("Servidor desconocido") }
enum class SectorRegion(val label: String) { CENTRAL("Sector Central"), EAST("Sector Este"), SOUTH("Sector Sur"), WEST("Sector Oeste"), NORTH("Sector Norte"), UNKNOWN("Sector desconocido") }
data class MapRegion(val server: ServerRegion, val sector: SectorRegion)

object MapRegionCatalog {
    fun resolve(mapId: Int): MapRegion {
        val server = when (mapId) {
            in 0x0200..0x02FF -> ServerRegion.ASUKA
            in 0x0780..0x089F -> ServerRegion.AMATERASU
            else -> ServerRegion.UNKNOWN
        }
        if (server == ServerRegion.UNKNOWN) return MapRegion(server, SectorRegion.UNKNOWN)

        val sector = when (server) {
            ServerRegion.ASUKA -> when (mapId) {
                in 0x0200..0x0228 -> SectorRegion.CENTRAL
                in 0x0229..0x0231 -> SectorRegion.EAST
                in 0x0232..0x0246 -> SectorRegion.SOUTH
                in 0x0247..0x026F -> SectorRegion.WEST
                in 0x0270..0x02DF -> SectorRegion.NORTH
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
        return MapRegion(server, sector)
    }
}
