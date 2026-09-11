package com.digitaladventure.dw2003.remote

import com.digitaladventure.dw2003.remote.protocol.RemoteInputFrame
import com.digitaladventure.dw2003.remote.protocol.RemoteCommandFrame
import com.digitaladventure.dw2003.remote.protocol.RemoteProtocol
import com.digitaladventure.dw2003.remote.protocol.RemoteTelemetryFrame
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.Inet4Address
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.net.SocketTimeoutException
import java.util.Collections
import java.util.concurrent.atomic.AtomicReference

class RemoteCompanionServer(
    private val pairingCode: Int,
    private val onInput: (RemoteInputFrame) -> Unit,
    private val onCommand: (RemoteCommandFrame) -> Unit = {},
    private val onConnectionChanged: (Boolean) -> Unit = {}
) {
    private val latestTelemetry = AtomicReference<RemoteTelemetryFrame?>(null)
    private var socket: DatagramSocket? = null
    private var worker: Thread? = null
    @Volatile private var running = false
    @Volatile private var client: InetSocketAddress? = null
    @Volatile private var connected = false

    fun start() {
        if (running) return
        running = true
        worker = Thread(::runLoop, "DW2003-Remote").apply {
            isDaemon = true
            start()
        }
    }

    fun stop() {
        running = false
        socket?.close()
        socket = null
        worker?.interrupt()
        worker = null
        client = null
        setConnected(false)
    }

    fun publish(frame: RemoteTelemetryFrame) {
        latestTelemetry.set(frame)
    }

    fun connectionLabel(): String =
        "${localIpv4Address() ?: "IP local"}:${RemoteProtocol.PORT} · código $pairingCode"

    private fun runLoop() {
        val datagramSocket = runCatching { DatagramSocket(RemoteProtocol.PORT) }.getOrNull() ?: run {
            running = false
            return
        }
        socket = datagramSocket
        datagramSocket.soTimeout = RECEIVE_TIMEOUT_MS
        val buffer = ByteArray(256)
        var lastInputAt = 0L
        var activeSession = Long.MIN_VALUE
        var lastSequence = Int.MIN_VALUE
        var lastCommandSequence = Int.MIN_VALUE

        while (running) {
            try {
                val packet = DatagramPacket(buffer, buffer.size)
                datagramSocket.receive(packet)
                val frame = RemoteProtocol.decodeInput(packet.data, packet.length)
                if (frame != null) {
                    if (frame.pairingCode != pairingCode) continue
                    if (frame.sessionId != activeSession) {
                        activeSession = frame.sessionId
                        lastSequence = Int.MIN_VALUE
                        lastCommandSequence = Int.MIN_VALUE
                    }
                    if (lastSequence != Int.MIN_VALUE && frame.sequence <= lastSequence) continue
                    lastSequence = frame.sequence
                    lastInputAt = System.nanoTime()
                    client = InetSocketAddress(packet.address, packet.port)
                    setConnected(true)
                    onInput(frame)
                } else {
                    val command = RemoteProtocol.decodeCommand(packet.data, packet.length) ?: continue
                    if (command.pairingCode != pairingCode) continue
                    if (command.sessionId != activeSession) {
                        activeSession = command.sessionId
                        lastSequence = Int.MIN_VALUE
                        lastCommandSequence = Int.MIN_VALUE
                    }
                    if (lastCommandSequence != Int.MIN_VALUE && command.sequence <= lastCommandSequence) continue
                    lastCommandSequence = command.sequence
                    lastInputAt = System.nanoTime()
                    client = InetSocketAddress(packet.address, packet.port)
                    setConnected(true)
                    onCommand(command)
                }
            } catch (_: SocketTimeoutException) {
                // The timeout also drives telemetry and disconnect detection.
            } catch (_: Exception) {
                if (running) continue else break
            }

            if (lastInputAt != 0L && System.nanoTime() - lastInputAt > INPUT_TIMEOUT_NANOS) {
                onInput(
                    RemoteInputFrame(pairingCode, activeSession, lastSequence + 1, 0, 0f, 0f, 0f, 0f, System.nanoTime())
                )
                lastInputAt = 0L
                client = null
                setConnected(false)
            }

            val telemetry = latestTelemetry.get()
            val endpoint = client
            // Telemetry is also the companion's connection acknowledgement. Resend the
            // latest snapshot for every input heartbeat even when game RAM is unchanged.
            if (telemetry != null && endpoint != null) {
                val bytes = RemoteProtocol.encodeTelemetry(telemetry)
                runCatching { datagramSocket.send(DatagramPacket(bytes, bytes.size, endpoint)) }
            }
        }
        datagramSocket.close()
    }

    private fun setConnected(value: Boolean) {
        if (connected == value) return
        connected = value
        onConnectionChanged(value)
    }

    companion object {
        private const val RECEIVE_TIMEOUT_MS = 40
        private const val INPUT_TIMEOUT_NANOS = 750_000_000L

        fun localIpv4Address(): String? = runCatching {
            Collections.list(NetworkInterface.getNetworkInterfaces())
                .asSequence()
                .filter { it.isUp && !it.isLoopback }
                .flatMap { Collections.list(it.inetAddresses).asSequence() }
                .filterIsInstance<Inet4Address>()
                .firstOrNull { it.isSiteLocalAddress }
                ?.hostAddress
        }.getOrNull()
    }
}
