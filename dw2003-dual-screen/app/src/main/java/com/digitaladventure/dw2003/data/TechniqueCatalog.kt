package com.digitaladventure.dw2003.data

/** Signature techniques available to the eight playable Rookie profiles. */
object TechniqueCatalog {
    private val rookieSignature = listOf(
        "Hot Head",
        "Bear Fist",
        "Swing Swing",
        "Pepper Breath",
        "Vee Headbutt",
        "Pyro Sphere",
        "Diamond Storm",
        "Boom Bubble"
    )

    fun signatureFor(profileId: Int): String? = rookieSignature.getOrNull(profileId)
}
