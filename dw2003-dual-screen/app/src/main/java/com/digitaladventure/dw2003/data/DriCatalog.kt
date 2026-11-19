package com.digitaladventure.dw2003.data

import com.digitaladventure.dw2003.model.DigimonState
import com.digitaladventure.dw2003.model.DriNotice

/**
 * DRI agents from the DW3 FAQ (Neoseeker / GameFAQs 51616).
 * Story unlocks follow Flawe pair keys; locations are not guessed from the current map.
 */
object DriCatalog {
    private data class Spec(
        val id: String,
        val profileId: Int,
        val rookie: String,
        val ultimate: String,
        val minLevel: Int,
        val maxLevel: Int,
        val startKey: String,
        val agentSpanish: String,
        val agentEnglish: String,
        val ultimateSpanish: String,
        val ultimateEnglish: String
    )

    private val specs = listOf(
        Spec(
            "agumon", 3, "Agumon", "MetalGreymon", 30, 59, "7d14-7d10",
            "Valle de Tyranno, a la derecha tras la escalera larga",
            "Tyranno Valley, right after the long ladder",
            "Pantano Yermo", "Bulk Swamp"
        ),
        Spec(
            "guilmon", 5, "Guilmon", "WarGrowlmon", 30, 59, "7d14-7d10",
            "Ciudad Seiryu, baja la escalera",
            "Seiryu City, down the ladder",
            "Jungla de Éter, pasaje entre árboles", "Ether Jungle, passage hidden by trees"
        ),
        Spec(
            "veemon", 4, "Veemon", "Paildramon", 1, 29, "6350-634c",
            "Ruinas Protocolo, después del Líder Byakko",
            "Protocol Ruins, after the Byakko Leader",
            "Puente Yermo", "Bulk Bridge"
        ),
        Spec(
            "patamon", 7, "Patamon", "MagnaAngemon", 45, 74, "6564-654c",
            "Central Park, cerca del muelle",
            "Central Park, near the dock",
            "Desierto Sur, sube la escalera y sigue el camino",
            "South Badlands, climb the ladder and follow the path"
        ),
        Spec(
            "renamon", 6, "Renamon", "Taomon", 45, 74, "6564-654c",
            "Central Park, encima del gimnasio",
            "Central Park, on top of the Training Gym",
            "Desierto Norte Oeste, primera pantalla",
            "North Badlands West, first screen"
        ),
        Spec(
            "kotemon", 0, "Kotemon", "Kyukimon", 60, 89, "6250-61ec",
            "Desierto Mobius, primera pantalla entre tres pilares",
            "Mobius Desert, first screen between the three pillars",
            "Lago Subterráneo Suzaku (Submarimon detrás de Sala Suzaku)",
            "Suzaku UG Lake (Submarimon behind Suzaku Hall)"
        ),
        Spec(
            "kumamon", 1, "Kumamon", "GrapLeomon", 60, 89, "6250-61ec",
            "Desierto Mobius, noroeste y luego suroeste",
            "Mobius Desert, northwest then southwest",
            "Jungla de Éter, Submarimon desde Bahía Fénix y a la izquierda en el lecho",
            "Ether Jungle, Submarimon from Phoenix Bay then left in the seabed"
        ),
        Spec(
            "monmon", 2, "Monmon", "Armormon", 60, 89, "6250-61ec",
            "Desierto Mobius, noroeste varias veces",
            "Mobius Desert, northwest several times",
            "Isla del Duelo (Submarimon desde el muelle de Lago de Divermon)",
            "Duel Island (Submarimon from Divermon's Lake dock)"
        )
    )

    fun forParty(guideKey: String?, party: List<DigimonState>): List<DriNotice> =
        forState(guideKey, party.sumOf { it.level }, party.map { it.profileId }.toSet())

    fun forState(guideKey: String?, combinedLevel: Int, ownedProfileIds: Set<Int>): List<DriNotice> {
        val current = order(guideKey) ?: return emptyList()
        return specs.mapNotNull { spec ->
            val start = order(spec.startKey) ?: return@mapNotNull null
            if (start < current) return@mapNotNull null
            if (spec.profileId in ownedProfileIds) return@mapNotNull null
            val ready = combinedLevel in spec.minLevel..spec.maxLevel
            DriNotice(
                id = spec.id,
                rookie = spec.rookie,
                ultimate = spec.ultimate,
                minLevel = spec.minLevel,
                maxLevel = spec.maxLevel,
                combinedLevel = combinedLevel,
                ready = ready,
                agentSpanish = spec.agentSpanish,
                agentEnglish = spec.agentEnglish,
                ultimateSpanish = spec.ultimateSpanish,
                ultimateEnglish = spec.ultimateEnglish
            )
        }
    }

    internal fun order(key: String?): Int? =
        key?.substringBefore('-')?.toIntOrNull(16)
}
