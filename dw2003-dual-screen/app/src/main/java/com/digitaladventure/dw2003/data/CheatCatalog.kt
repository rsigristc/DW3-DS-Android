package com.digitaladventure.dw2003.data

data class CheatSpec(
    val id: String,
    val label: String,
    val detail: String,
    val code: String,
    val battleOnly: Boolean = false
)

object CheatCatalog {
    val all: List<CheatSpec> = listOf(
        CheatSpec(
            id = "infinite_bits",
            label = "Bits máximos",
            detail = "Mantiene 99.999.968 Bits en la RAM del Tamer.",
            code = "80048DA0 E0FF+80048DA2 05F5"
        ),
        CheatSpec(
            id = "no_random_battles",
            label = "Sin batallas aleatorias",
            detail = "Evita encuentros en el campo. No afecta jefes ni eventos.",
            code = "80048D64 E0FF+80048D66 05F5"
        ),
        CheatSpec(
            id = "infinite_hp_battle",
            label = "HP infinito en batalla",
            detail = "Solo mientras el overlay de combate está activo.",
            code = "800A4478 03E7+800A4476 03E7",
            battleOnly = true
        ),
        CheatSpec(
            id = "infinite_mp_battle",
            label = "MP infinito en batalla",
            detail = "Solo mientras el overlay de combate está activo.",
            code = "800A447A 03E7+800A447C 03E7",
            battleOnly = true
        )
    )

    fun byId(id: String): CheatSpec? = all.firstOrNull { it.id == id }
}
