package com.digitaladventure.dw2003.model

data class AuctionNotice(
    val id: String,
    val itemSpanish: String,
    val itemEnglish: String,
    val bits: Int
) {
    fun item(spanish: Boolean): String = if (spanish) itemSpanish else itemEnglish

    fun label(spanish: Boolean): String {
        val item = item(spanish)
        val place = if (spanish) "El Dorado / Torre de Carga" else "El Dorado / Cargo Tower"
        return if (spanish) {
            "Subasta: $item ($bits Bits) en $place."
        } else {
            "Auction: $item ($bits Bits) at $place."
        }
    }
}
