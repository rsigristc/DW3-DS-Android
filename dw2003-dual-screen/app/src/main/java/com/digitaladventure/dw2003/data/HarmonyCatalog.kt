package com.digitaladventure.dw2003.data

import org.json.JSONObject

/** Disc-specific factual data, generated from Harmony's real MIPS routines. */
class HarmonyCatalog(json: String) {
    data class Skill(
        val id: Int, val name: String, val nameEs: String, val level: Int,
        val mastery: Int?, val power: Int, val signature: Boolean, val costs: List<Int>
    )
    data class Condition(val kind: Int, val id: Int, val value: Int)
    data class Profile(
        val id: Int, val name: String, val skills: List<Skill>, val status: List<Int>,
        val rules: Map<Int, List<Condition>>, val costPercent: Int
    )

    private val root = JSONObject(json)
    val revision: String = root.getString("revision")
    val ninthPartner: Boolean = root.optBoolean("ninthPartner", true)
    private val hookOffset = root.getInt("hookOffset")
    private val hook = hex(root.getString("hook"))
    private val profileOffset = root.getInt("profileOffset")
    private val fingerprint = hex(root.getString("profileFingerprint"))
    val profiles: List<Profile> = root.getJSONArray("profiles").let { array ->
        List(array.length()) { index ->
            val p = array.getJSONObject(index)
            val skills = p.getJSONArray("skills")
            val rules = p.getJSONObject("rules")
            Profile(p.getInt("id"), p.getString("name"), List(skills.length()) { n ->
                val s = skills.getJSONObject(n)
                Skill(s.getInt("id"), s.getString("name"), s.getString("nameEs"),
                    s.getInt("level"), if (s.isNull("mastery")) null else s.getInt("mastery"),
                    s.getInt("power"), s.getBoolean("signature"),
                    s.getJSONArray("costs").let { c -> List(c.length()) { c.getInt(it) } })
            }, p.getJSONArray("status").let { s -> List(s.length()) { s.getInt(it) } },
                rules.keys().asSequence().associate { key ->
                    val conditions = rules.getJSONArray(key)
                    key.toInt() to List(conditions.length()) { n ->
                        val c = conditions.getJSONArray(n)
                        Condition(c.getInt(0), c.getInt(1), c.getInt(2))
                    }
                }, p.getInt("costPercent"))
        }
    }
    private val byId = profiles.associateBy { it.id }
    private val skillsById = root.getJSONArray("techniques").let { array ->
        List(array.length()) { index ->
            val s = array.getJSONObject(index)
            Skill(s.getInt("id"), s.getString("name"), s.getString("nameEs"), 1, null,
                s.getInt("power"), false,
                s.getJSONArray("costs").let { c -> List(c.length()) { c.getInt(it) } })
        }.associateBy { it.id }
    }

    fun matches(read: (Int, Int) -> ByteArray): Boolean =
        read(hookOffset, hook.size).contentEquals(hook) &&
            read(profileOffset, fingerprint.size).contentEquals(fingerprint)

    fun profile(id: Int): Profile? = byId[id]
    fun rookieId(member: Int): Int? = ROOKIE_IDS.getOrNull(member)
    fun routes(member: Int): List<Profile> = profiles.filter { member in it.rules }

    fun validPartner(record: ByteArray?): Boolean = ninthPartner && record != null && record.size == RECORD_LENGTH &&
        GameStateReader.u32(record, 4) == 11L

    fun technique(formId: Int, id: Int, flags: Int, equipment: List<Int>, spanish: Boolean): TechniqueInfo? {
        val skill = byId[formId]?.skills?.firstOrNull { it.id == id } ?: skillsById[id] ?: return null
        val first = ringIndex(equipment.getOrNull(4))
        val second = ringIndex(equipment.getOrNull(5))
        val loaded = if (flags and 0x4000 != 0) 9 else 0
        val index = loaded + first * 3 + second
        val cost = byId[formId]?.skills?.firstOrNull { it.id == id }?.costs?.getOrNull(index)
            ?: skillsById[id]?.costs?.getOrNull(index)?.let {
                (it * (byId[formId]?.costPercent ?: 100) + 99) / 100
            }
        return TechniqueInfo(skillName(skill, spanish), cost, skill.level.coerceAtLeast(1), skill.power)
    }

    fun details(member: Int, formId: Int, spanish: Boolean): String? {
        val p = byId[formId] ?: return null
        val requirements = p.rules[member]
        return buildString {
            append(revision).append("\n\n")
            if (requirements != null) {
                append(if (spanish) "REQUISITOS\n" else "REQUIREMENTS\n")
                requirements.forEach { append("• ").append(conditionText(it, spanish)).append('\n') }
                append('\n')
            }
            append(if (spanish) "TÉCNICAS (MP sin accesorios)\n" else "TECHNIQUES (MP without accessories)\n")
            p.skills.forEach { skill ->
                append("• ").append(skillName(skill, spanish))
                append(" · DV ").append(skill.level.coerceAtLeast(1))
                append(" · MP ").append(skill.costs.first())
                append(if (spanish) " · Poder " else " · Power ").append(skill.power)
                skill.mastery?.let { append(if (spanish) " · Dominio DV " else " · Mastery DV ").append(it) }
                if (skill.signature) append(if (spanish) " · Propia" else " · Signature")
                append('\n')
            }
        }
    }

    fun requirementSummary(profile: Profile, member: Int, spanish: Boolean): String? =
        profile.rules[member]?.firstOrNull()?.let { conditionText(it, spanish) }

    private fun conditionText(c: Condition, spanish: Boolean): String {
        val owner = when {
            c.kind in 24..31 -> c.kind - 24
            c.kind >= 40 -> 8
            c.kind >= 36 -> 4
            c.kind >= 32 -> 1
            c.kind >= 20 -> 2
            c.kind >= 16 -> 0
            c.kind >= 12 -> 5
            c.kind >= 8 -> 7
            c.kind >= 4 -> 6
            else -> 3
        }
        val ownerName = rookieId(owner)?.let { byId[it]?.name }.orEmpty()
        if (c.kind in 24..31 || c.kind % 4 == 1) {
            return "$ownerName: ${byId[c.id]?.name ?: c.id} DV ${c.value}"
        }
        if (c.kind % 4 == 2) return "$ownerName: ${byId[c.id]?.name ?: c.id} EXP ${c.value}"
        if (c.kind % 4 == 3) return if (spanish) "Avance de historia requerido" else "Story progress required"
        val labels = if (spanish) STAT_ES else STAT_EN
        return "$ownerName: ${labels.getOrElse(c.id) { if (spanish) "Parámetro" else "Stat" }} ${c.value}"
    }

    private fun skillName(s: Skill, spanish: Boolean): String =
        (if (spanish) s.nameEs else s.name).takeIf { it.isNotBlank() && it != "a" }
            ?: (if (spanish) "Técnica #${s.id}" else "Technique #${s.id}")

    private fun ringIndex(id: Int?): Int = when (id) { 0x143 -> 1; 0x144 -> 2; else -> 0 }
    private fun hex(s: String) = s.chunked(2).map { it.toInt(16).toByte() }.toByteArray()

    companion object {
        fun loadAll(assets: android.content.res.AssetManager): List<HarmonyCatalog> =
            listOf("catalog-v50.json", "catalog-beta15.json").map { file ->
                HarmonyCatalog(assets.open("harmony/$file").bufferedReader().use { it.readText() })
            }

        const val PARTNER_OFFSET = 0xB1400
        const val RECORD_LENGTH = 0x3DC
        private val ROOKIE_IDS = listOf(383, 385, 384, 3, 145, 366, 373, 31, 0x6100)
        private val STAT_ES = listOf("NV", "TP", "HP", "HP máx.", "MP", "MP máx.", "FUE", "DEF", "ESP", "SAB", "VEL", "CAR", "Fuego", "Agua", "Hielo", "Viento", "Rayo", "Oscuridad", "Máquina")
        private val STAT_EN = listOf("LV", "TP", "HP", "Max HP", "MP", "Max MP", "STR", "DEF", "SPR", "WIS", "SPD", "CHA", "Fire", "Water", "Ice", "Wind", "Thunder", "Dark", "Machine")
    }
}
