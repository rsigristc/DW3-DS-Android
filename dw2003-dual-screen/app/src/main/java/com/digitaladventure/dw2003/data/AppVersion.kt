package com.digitaladventure.dw2003.data

object AppVersion {
    fun numericParts(raw: String): List<Int> {
        val core = raw.removePrefix("v").substringBefore("-debug").substringBefore("-poc")
        return Regex("""\d+""").findAll(core).map { it.value.toInt() }.toList()
    }

    fun isNewer(remote: String, local: String): Boolean {
        val left = numericParts(remote)
        val right = numericParts(local)
        if (left.isEmpty() || right.isEmpty()) return false
        val width = maxOf(left.size, right.size)
        repeat(width) { index ->
            val diff = left.getOrElse(index) { 0 } - right.getOrElse(index) { 0 }
            if (diff != 0) return diff > 0
        }
        return false
    }
}
