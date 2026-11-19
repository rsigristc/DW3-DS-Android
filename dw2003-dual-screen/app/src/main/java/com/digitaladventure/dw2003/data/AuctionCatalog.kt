package com.digitaladventure.dw2003.data

import com.digitaladventure.dw2003.model.AuctionNotice

/**
 * Side auctions from Flawe's story beats (not extra map guesses).
 * Each window starts at the first guide key after the FAQ trigger
 * and ends when the next auction's key becomes current.
 */
object AuctionCatalog {
    private data class Spec(
        val id: String,
        val itemSpanish: String,
        val itemEnglish: String,
        val bits: Int,
        val startKey: String
    )

    private val specs = listOf(
        Spec("divine_barrier", "Barrera Divina", "Divine Barrier", 800, "7d14-7d10"),
        Spec("hazard_shield", "Escudo Salvaje", "Hazard Shield", 800, "7518-7514"),
        Spec("sniper_shield", "Escudo Francotirador", "Sniper Shield", 1_600, "6ebc-6eb8"),
        Spec("dramon_shield", "Escudo Dramon", "Dramon Shield", 1_600, "6b4c-6afc"),
        Spec("yin_yang_rod", "Varita Yin Yang", "Yin Yang Wand", 2_700, "6350-634c"),
        Spec("hazard_fang", "Colmillo Salvaje", "Hazard Fang", 2_700, "6030-6000"),
        Spec("sniper_cannon", "Cañón Francotirador", "Sniper Cannon", 4_000, "5134-5130"),
        Spec("ronin_blade", "Hoja Ronin", "Ronin Blade", 4_000, "4e28-4e04"),
        Spec("sniper_goggle", "Gafas Francotirador", "Sniper Goggle", 6_000, "4b8c-4b3c"),
        Spec("divine_crown", "Corona Divina", "Divine Crown", 6_000, "48a8-48a4"),
        Spec("brave_helmet", "Casco Valiente", "Brave Helmet", 8_700, "41dc-41b4"),
        Spec("ronin_helmet", "Casco Ronin", "Ronin Helmet", 8_700, "35cc-3578"),
        Spec("yin_yang_suit", "Traje Yin Yang", "Yin Yang Suit", 11_500, "32a8-3258"),
        Spec("ronin_armor", "Armadura Ronin", "Ronin Armor", 17_500, "31e4-31bc"),
        Spec("brave_armor", "Armadura Valiente", "Brave Armor", 32_000, "2eb8-2eb4")
    )

    fun forGuideKey(key: String?): AuctionNotice? {
        val current = order(key) ?: return null
        specs.forEachIndexed { index, spec ->
            val start = order(spec.startKey) ?: return@forEachIndexed
            val end = specs.getOrNull(index + 1)?.startKey?.let(::order) ?: POST_GAME
            if (start >= current && current > end) {
                return AuctionNotice(spec.id, spec.itemSpanish, spec.itemEnglish, spec.bits)
            }
        }
        return null
    }

    internal fun order(key: String?): Int? =
        key?.substringBefore('-')?.toIntOrNull(16)

    private val POST_GAME = order("28a0-287c") ?: Int.MIN_VALUE
}
