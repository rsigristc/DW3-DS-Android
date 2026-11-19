package com.digitaladventure.dw2003.remote

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RemoteNetworkAddressesTest {
    @Test
    fun prefersLanOverNoxNat() {
        assertEquals(
            "192.168.100.23",
            RemoteNetworkAddresses.preferred(listOf("172.16.21.15", "192.168.100.23"))
        )
        assertTrue(RemoteNetworkAddresses.looksVirtual("172.16.21.15"))
        assertFalse(RemoteNetworkAddresses.looksVirtual("192.168.100.23"))
    }

    @Test
    fun formatsEveryAddress() {
        assertEquals(
            "192.168.1.8 / 172.16.21.15:37603 · código 452037",
            RemoteNetworkAddresses.formatLabel(
                listOf("172.16.21.15", "192.168.1.8"),
                37603,
                452037
            )
        )
    }
}
