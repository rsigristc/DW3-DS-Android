package com.digitaladventure.dw2003.remote

import com.digitaladventure.dw2003.remote.protocol.RemoteInputFrame
import com.digitaladventure.dw2003.remote.protocol.RemoteCommand
import com.digitaladventure.dw2003.remote.protocol.RemoteCommandFrame
import com.digitaladventure.dw2003.remote.protocol.RemoteProtocol
import com.digitaladventure.dw2003.remote.protocol.RemoteTelemetryFrame
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.SocketTimeoutException
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.random.Random

class RemoteControllerClient(
    private val onTelemetry: (RemoteTelemetryFrame) -> Unit,
    private val onConnectionChanged: (Boolean) -> Unit
) {
    private val sequence = AtomicInteger()
    private val sessionId = Random.nextLong()
    private var scheduler: ScheduledExecutorService? = null
    private var socket: DatagramSocket? = null
    @Volatile private var endpoint: InetSocketAddress? = null
    @Volatile private var pairingCode = 0
    @Volatile private var running = false
    @Volatile private var state = ControllerState()
    @Volatile private var lastTelemetryAt = 0L

    fun connect(host: String, code: Int) {
        stop()
        lastTelemetryAt = 0L
        val address = InetAddress.getByName(host)
        pairingCode = code
        endpoint = InetSocketAddress(address, RemoteProtocol.PORT)
        running = true
        socket = DatagramSocket().apply { soTimeout = 250 }
        Thread(::receiveLoop, "DW2003-Remote-RX").apply { isDaemon = true; start() }
        scheduler = Executors.newSingleThreadScheduledExecutor().also { executor ->
            executor.scheduleWithFixedDelay(::sendCurrent, 0, HEARTBEAT_MS, TimeUnit.MILLISECONDS)
            executor.scheduleWithFixedDelay(::checkConnection, HEARTBEAT_MS, HEARTBEAT_MS, TimeUnit.MILLISECONDS)
        }
    }

    fun update(next: ControllerState) {
        state = next
        scheduler?.execute(::sendCurrent)
    }

    fun sendNeutral() {
        state = ControllerState()
        sendCurrent()
    }

    fun sendCommand(command: RemoteCommand, argument: Int = 0, text: String = "", detail: String = "") {
        val target = endpoint ?: return
        val datagramSocket = socket ?: return
        val bytes = RemoteProtocol.encodeCommand(
            RemoteCommandFrame(pairingCode, sessionId, sequence.incrementAndGet(), command, argument, text, detail)
        )
        scheduler?.execute {
            runCatching { datagramSocket.send(DatagramPacket(bytes, bytes.size, target)) }
        }
    }

    fun stop() {
        if (running) runCatching { sendNeutral() }
        running = false
        scheduler?.shutdownNow()
        scheduler = null
        socket?.close()
        socket = null
        endpoint = null
        lastTelemetryAt = 0L
        onConnectionChanged(false)
    }

    private fun sendCurrent() {
        val target = endpoint ?: return
        val datagramSocket = socket ?: return
        val current = state
        val bytes = RemoteProtocol.encodeInput(
            RemoteInputFrame(
                pairingCode = pairingCode,
                sessionId = sessionId,
                sequence = sequence.incrementAndGet(),
                buttons = current.buttons,
                leftX = current.leftX,
                leftY = current.leftY,
                rightX = current.rightX,
                rightY = current.rightY,
                sentAtNanos = System.nanoTime()
            )
        )
        runCatching { datagramSocket.send(DatagramPacket(bytes, bytes.size, target)) }
    }

    private fun receiveLoop() {
        val buffer = ByteArray(RemoteProtocol.MAX_PACKET_BYTES)
        while (running) {
            try {
                val packet = DatagramPacket(buffer, buffer.size)
                socket?.receive(packet) ?: break
                val telemetry = RemoteProtocol.decodeTelemetry(packet.data, packet.length) ?: continue
                if (telemetry.pairingCode != pairingCode) continue
                lastTelemetryAt = System.nanoTime()
                onTelemetry(telemetry)
                onConnectionChanged(true)
            } catch (_: SocketTimeoutException) {
                // Used to observe stop and connection timeout.
            } catch (_: Exception) {
                if (!running) break
            }
        }
    }

    private fun checkConnection() {
        val connected = lastTelemetryAt != 0L && System.nanoTime() - lastTelemetryAt < CONNECTION_TIMEOUT_NANOS
        onConnectionChanged(connected)
    }

    data class ControllerState(
        val buttons: Int = 0,
        val leftX: Float = 0f,
        val leftY: Float = 0f,
        val rightX: Float = 0f,
        val rightY: Float = 0f
    )

    companion object {
        private const val HEARTBEAT_MS = 250L
        private const val CONNECTION_TIMEOUT_NANOS = 1_500_000_000L
    }
}
