package com.digitaladventure.dw2003.model

data class DriNotice(
    val id: String,
    val rookie: String,
    val ultimate: String,
    val minLevel: Int,
    val maxLevel: Int,
    val combinedLevel: Int,
    val ready: Boolean,
    val agentSpanish: String,
    val agentEnglish: String,
    val ultimateSpanish: String,
    val ultimateEnglish: String
) {
    fun label(spanish: Boolean): String {
        val agent = if (spanish) agentSpanish else agentEnglish
        val hunt = if (spanish) ultimateSpanish else ultimateEnglish
        val range = "$minLevel–$maxLevel"
        return if (ready) {
            if (spanish) {
                "DRI listo: $rookie. Nivel total $combinedLevel ($range). Agente: $agent. DDNA: $ultimate en $hunt."
            } else {
                "DRI ready: $rookie. Combined level $combinedLevel ($range). Agent: $agent. DDNA: $ultimate at $hunt."
            }
        } else if (combinedLevel > maxLevel) {
            if (spanish) {
                "$rookie DRI: el equipo suma $combinedLevel. Deja Digimon en el laboratorio hasta $range. Agente: $agent."
            } else {
                "$rookie DRI: party totals $combinedLevel. Store Digimon in the lab until $range. Agent: $agent."
            }
        } else {
            if (spanish) {
                "$rookie DRI: el equipo suma $combinedLevel; hace falta $range. Agente: $agent."
            } else {
                "$rookie DRI: party totals $combinedLevel; need $range. Agent: $agent."
            }
        }
    }
}
