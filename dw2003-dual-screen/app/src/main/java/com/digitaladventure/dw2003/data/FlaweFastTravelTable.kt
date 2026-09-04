package com.digitaladventure.dw2003.data

/**
 * Flawe 2.0 world-map icons recovered from
 * [dmw_2003_patcher](https://github.com/markisha64/dmw_2003_patcher)
 * `patches/fast_travel` (SLES_039.36 IPS at file offset `0x095500`).
 *
 * Each `beq v1, t0` compares the hovered ASKMAP index, then the handler
 * writes the destination `MAP_ID`. Names follow ddw3 `esaskmap` / `ssaskmap`.
 * Amaterasu reuses the same icon indices on a different map; this table only
 * contains the Asuka-server MAP_IDs hardcoded in that IPS blob.
 */
object FlaweFastTravelTable {
    data class Icon(val mapId: Int, val iconCode: Int)

    val asukaIcons: List<Icon> = listOf(
        Icon(0x0200, 0x14), // Asuka City
        Icon(0x021D, 0x1E), // Central Park
        Icon(0x021E, 0x16), // Wire Forest Entrance
        Icon(0x021F, 0x21), // Shell Beach
        Icon(0x0220, 0x12), // Plug Cape
        Icon(0x0222, 0x15), // Wire Forest
        Icon(0x0223, 0x0C), // Forest Inn
        Icon(0x0225, 0x0E), // Protocol Ruins / Protocol Forest
        Icon(0x0227, 0x17), // Divermon's Lake
        Icon(0x0228, 0x07), // Duel Island
        Icon(0x0229, 0x18), // Wind Prairie
        Icon(0x022A, 0x19), // Kicking Forest
        Icon(0x022B, 0x10), // Tyranno Valley
        Icon(0x022C, 0x1F), // East Station
        Icon(0x022E, 0x0F), // Seiryu City
        Icon(0x0232, 0x20), // South Station
        Icon(0x0234, 0x2B), // Bulk Swamp / Bulk Bridge
        Icon(0x0235, 0x26), // Bios Swamp
        Icon(0x0237, 0x2C), // Tranquil Swamp
        Icon(0x0238, 0x0D), // Swamp Inn
        Icon(0x0239, 0x25), // Shaman House
        Icon(0x023A, 0x2D), // Jungle Grave
        Icon(0x023B, 0x2E), // Phoenix Bay
        Icon(0x023C, 0x29), // Ether Jungle
        Icon(0x023D, 0x13), // South Cape
        Icon(0x023E, 0x2A), // Suzaku City
        Icon(0x0242, 0x24), // Jungle Shrine
        Icon(0x0247, 0x1C), // South Badland
        Icon(0x0248, 0x23), // Noise Desert
        Icon(0x0249, 0x1A), // Pelche Oasis
        Icon(0x024A, 0x1D), // North Badland W
        Icon(0x024B, 0x1B), // North Badland E
        Icon(0x024C, 0x11), // Bullet Valley
        Icon(0x024D, 0x08), // Dum Dum Factory
        Icon(0x0258, 0x28), // Mobius Desert
        Icon(0x025B, 0x27), // Mirage Tower
        Icon(0x025D, 0x22), // Byakko City
        Icon(0x0261, 0x0A), // Boot Mountain
        Icon(0x0262, 0x04), // Snow Mountain
        Icon(0x0263, 0x0B), // Mountain Inn
        Icon(0x0264, 0x05), // Freeze Mountain
        Icon(0x0265, 0x03), // Kulon Mine
        Icon(0x0266, 0x02), // Lake of Ice
        Icon(0x0267, 0x01), // Legendary Gym
        Icon(0x0268, 0x09), // Kulon Pit
        Icon(0x026F, 0x06)  // Genbu City
    )

    private val codeByMapId: Map<Int, Int> = asukaIcons.associate { it.mapId to it.iconCode }
    val asukaMapIds: Set<Int> = codeByMapId.keys

    fun iconCode(mapId: Int): Int? = codeByMapId[mapId]
}
