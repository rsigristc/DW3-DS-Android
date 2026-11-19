package com.digitaladventure.dw2003.remote

object RemoteNetworkAddresses {
    fun looksVirtual(address: String): Boolean {
        val parts = address.split('.').mapNotNull { it.toIntOrNull() }
        if (parts.size != 4) return false
        val a = parts[0]
        val b = parts[1]
        val c = parts[2]
        if (a == 172 && b in 16..31) return true
        if (address == "10.0.2.15" || address == "10.0.3.15") return true
        if (a == 192 && b == 168 && c == 56) return true
        return a == 100 && b in 64..127
    }

    fun sortForDisplay(addresses: List<String>): List<String> =
        addresses.distinct().sortedWith(
            compareBy<String> { looksVirtual(it) }
                .thenBy { !it.startsWith("192.168.") }
                .thenBy { !it.startsWith("10.") }
                .thenBy { it }
        )

    fun preferred(addresses: List<String>): String? = sortForDisplay(addresses).firstOrNull()

    fun formatLabel(addresses: List<String>, port: Int, pairingCode: Int): String {
        val shown = sortForDisplay(addresses)
        val hosts = if (shown.isEmpty()) "IP local" else shown.joinToString(" / ")
        return "$hosts:$port · código $pairingCode"
    }
}
