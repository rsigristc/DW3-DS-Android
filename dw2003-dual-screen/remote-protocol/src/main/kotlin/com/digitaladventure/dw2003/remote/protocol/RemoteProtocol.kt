package com.digitaladventure.dw2003.remote.protocol

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

enum class RemoteButton(val mask: Int) {
    DPAD_UP(1 shl 0),
    DPAD_DOWN(1 shl 1),
    DPAD_LEFT(1 shl 2),
    DPAD_RIGHT(1 shl 3),
    CROSS(1 shl 4),
    CIRCLE(1 shl 5),
    SQUARE(1 shl 6),
    TRIANGLE(1 shl 7),
    L1(1 shl 8),
    R1(1 shl 9),
    L2(1 shl 10),
    R2(1 shl 11),
    START(1 shl 12),
    SELECT(1 shl 13),
    LEFT_STICK(1 shl 14),
    RIGHT_STICK(1 shl 15)
}

enum class RemoteCommand { SAVE_STATE, LOAD_STATE, TOGGLE_SPEED, TOGGLE_MUTE, CYCLE_IMAGE, FAST_TRAVEL, TOGGLE_MOD, SET_MODS_ENABLED, ADD_CUSTOM_MOD, MOVE_PARTY }

data class RemoteCommandFrame(
    val pairingCode: Int,
    val sessionId: Long,
    val sequence: Int,
    val command: RemoteCommand,
    val argument: Int = 0,
    val text: String = "",
    val detail: String = ""
)

data class RemoteInputFrame(
    val pairingCode: Int,
    val sessionId: Long,
    val sequence: Int,
    val buttons: Int,
    val leftX: Float,
    val leftY: Float,
    val rightX: Float,
    val rightY: Float,
    val sentAtNanos: Long
)

data class RemotePartyMember(
    val name: String,
    val level: Int,
    val currentHp: Int,
    val maxHp: Int,
    val currentMp: Int,
    val maxMp: Int,
    val experience: Long,
    val nextLevelExperience: Long,
    val activeForm: String,
    val profileId: Int = 0,
    val trainingPoints: Int = 0,
    val forms: List<RemoteForm> = emptyList(),
    val parameters: List<RemoteValue> = emptyList(),
    val resistances: List<RemoteValue> = emptyList(),
    val statusResistances: List<RemoteValue> = emptyList(),
    val skills: List<RemoteSkill> = emptyList(),
    val equipment: List<RemoteEquipment> = emptyList()
)

data class RemoteForm(val name: String, val level: Int, val active: Boolean)
data class RemoteValue(val name: String, val value: Int, val bonus: Int = 0)
data class RemoteSkill(val name: String, val mp: Int, val power: Int)
data class RemoteEquipment(val slot: String, val name: String, val type: String, val bonuses: String)
data class RemoteTravelDestination(val areaId: Int, val name: String, val region: String, val current: Boolean)
data class RemoteMod(val index: Int, val name: String, val detail: String, val enabled: Boolean, val custom: Boolean)

data class RemoteEnemy(
    val name: String,
    val level: Int,
    val currentHp: Int,
    val maxHp: Int,
    val attacks: String,
    val loot: String,
    val parameters: List<RemoteValue> = emptyList(),
    val resistances: List<RemoteValue> = emptyList(),
    val statusResistances: List<RemoteValue> = emptyList()
)

data class RemoteTelemetryFrame(
    val pairingCode: Int,
    val sequence: Int,
    val sampledAtMillis: Long,
    val mode: String,
    val gameStarted: Boolean,
    val areaId: Int,
    val mapId: Int,
    val locationTitle: String,
    val locationDetail: String,
    val tamerName: String,
    val objective: String,
    val bits: Long,
    val party: List<RemotePartyMember>,
    val storyStage: Int = 0,
    val serverName: String = "",
    val sectorName: String = "",
    val enemies: List<RemoteEnemy> = emptyList(),
    val muted: Boolean = false,
    val fastForward: Boolean = false,
    val stateAvailable: Boolean = false,
    val canReorderParty: Boolean = false,
    val canFastTravel: Boolean = false,
    val travelDestinations: List<RemoteTravelDestination> = emptyList(),
    val modsEnabled: Boolean = false,
    val mods: List<RemoteMod> = emptyList()
)

object RemoteProtocol {
    const val PORT = 37_603
    const val VERSION: Int = 5
    const val MAX_PACKET_BYTES = 60_000

    private const val MAGIC = 0x44573352 // DW3R
    private const val TYPE_INPUT = 1
    private const val TYPE_TELEMETRY = 2
    private const val TYPE_COMMAND = 3
    private const val INPUT_BYTES = 48
    private const val COMMAND_HEADER_BYTES = 32

    fun encodeInput(frame: RemoteInputFrame): ByteArray =
        ByteBuffer.allocate(INPUT_BYTES).order(ByteOrder.BIG_ENDIAN).apply {
            putInt(MAGIC)
            put(VERSION.toByte())
            put(TYPE_INPUT.toByte())
            putShort(0)
            putInt(frame.pairingCode)
            putLong(frame.sessionId)
            putInt(frame.sequence)
            putInt(frame.buttons)
            putShort(axisToShort(frame.leftX))
            putShort(axisToShort(frame.leftY))
            putShort(axisToShort(frame.rightX))
            putShort(axisToShort(frame.rightY))
            putLong(frame.sentAtNanos)
            putInt(0)
        }.array()

    fun decodeInput(bytes: ByteArray, length: Int = bytes.size): RemoteInputFrame? {
        if (length != INPUT_BYTES || length > bytes.size) return null
        val buffer = ByteBuffer.wrap(bytes, 0, length).order(ByteOrder.BIG_ENDIAN)
        if (buffer.int != MAGIC || buffer.get().toInt() and 0xff != VERSION) return null
        if (buffer.get().toInt() and 0xff != TYPE_INPUT) return null
        buffer.short
        val pairingCode = buffer.int
        val sessionId = buffer.long
        val sequence = buffer.int
        val buttons = buffer.int
        val leftX = shortToAxis(buffer.short)
        val leftY = shortToAxis(buffer.short)
        val rightX = shortToAxis(buffer.short)
        val rightY = shortToAxis(buffer.short)
        val sentAtNanos = buffer.long
        return RemoteInputFrame(
            pairingCode, sessionId, sequence, buttons,
            leftX, leftY, rightX, rightY, sentAtNanos
        )
    }

    fun encodeCommand(frame: RemoteCommandFrame): ByteArray {
        val output = ByteArrayOutputStream()
        DataOutputStream(output).use { data ->
            data.writeInt(MAGIC)
            data.writeByte(VERSION)
            data.writeByte(TYPE_COMMAND)
            data.writeShort(0)
            data.writeInt(frame.pairingCode)
            data.writeLong(frame.sessionId)
            data.writeInt(frame.sequence)
            data.writeByte(frame.command.ordinal)
            data.write(ByteArray(3))
            data.writeInt(frame.argument)
            data.writeUtf8(frame.text.take(80))
            data.writeUtf8(frame.detail.take(2_000))
        }
        return output.toByteArray().also { require(it.size <= MAX_PACKET_BYTES) }
    }

    fun decodeCommand(bytes: ByteArray, length: Int = bytes.size): RemoteCommandFrame? {
        if (length !in COMMAND_HEADER_BYTES..minOf(bytes.size, MAX_PACKET_BYTES)) return null
        return runCatching {
            DataInputStream(ByteArrayInputStream(bytes, 0, length)).use { data ->
                if (data.readInt() != MAGIC || data.readUnsignedByte() != VERSION) return null
                if (data.readUnsignedByte() != TYPE_COMMAND) return null
                data.readUnsignedShort()
                val pairingCode = data.readInt()
                val sessionId = data.readLong()
                val sequence = data.readInt()
                val command = RemoteCommand.entries.getOrNull(data.readUnsignedByte()) ?: return null
                data.skipBytes(3)
                val argument = data.readInt()
                RemoteCommandFrame(pairingCode, sessionId, sequence, command, argument, data.readUtf8(), data.readUtf8())
            }
        }.getOrNull()
    }

    fun encodeTelemetry(frame: RemoteTelemetryFrame): ByteArray {
        val output = ByteArrayOutputStream()
        DataOutputStream(output).use { data ->
            data.writeInt(MAGIC)
            data.writeByte(VERSION)
            data.writeByte(TYPE_TELEMETRY)
            data.writeShort(0)
            data.writeInt(frame.pairingCode)
            data.writeInt(frame.sequence)
            data.writeLong(frame.sampledAtMillis)
            data.writeBoolean(frame.gameStarted)
            data.writeInt(frame.areaId)
            data.writeInt(frame.mapId)
            data.writeLong(frame.bits)
            data.writeUtf8(frame.mode)
            data.writeUtf8(frame.locationTitle)
            data.writeUtf8(frame.locationDetail)
            data.writeUtf8(frame.tamerName)
            data.writeUtf8(frame.objective)
            data.writeInt(frame.storyStage)
            data.writeUtf8(frame.serverName)
            data.writeUtf8(frame.sectorName)
            data.writeBoolean(frame.muted)
            data.writeBoolean(frame.fastForward)
            data.writeBoolean(frame.stateAvailable)
            data.writeBoolean(frame.canReorderParty)
            val party = frame.party.take(8)
            data.writeByte(party.size)
            party.forEach { member ->
                data.writeUtf8(member.name)
                data.writeInt(member.level)
                data.writeInt(member.currentHp)
                data.writeInt(member.maxHp)
                data.writeInt(member.currentMp)
                data.writeInt(member.maxMp)
                data.writeLong(member.experience)
                data.writeLong(member.nextLevelExperience)
                data.writeUtf8(member.activeForm)
                data.writeInt(member.profileId)
                data.writeInt(member.trainingPoints)
                val forms = member.forms.take(16)
                data.writeByte(forms.size)
                forms.forEach { form ->
                    data.writeUtf8(form.name)
                    data.writeInt(form.level)
                    data.writeBoolean(form.active)
                }
                data.writeRemoteValues(member.parameters)
                data.writeRemoteValues(member.resistances)
                data.writeRemoteValues(member.statusResistances)
                data.writeByte(member.skills.take(16).size)
                member.skills.take(16).forEach { skill ->
                    data.writeUtf8(skill.name); data.writeInt(skill.mp); data.writeInt(skill.power)
                }
                data.writeByte(member.equipment.take(8).size)
                member.equipment.take(8).forEach { item ->
                    data.writeUtf8(item.slot); data.writeUtf8(item.name)
                    data.writeUtf8(item.type); data.writeUtf8(item.bonuses)
                }
            }
            val enemies = frame.enemies.take(8)
            data.writeByte(enemies.size)
            enemies.forEach { enemy ->
                data.writeUtf8(enemy.name)
                data.writeInt(enemy.level)
                data.writeInt(enemy.currentHp)
                data.writeInt(enemy.maxHp)
                data.writeUtf8(enemy.attacks)
                data.writeUtf8(enemy.loot)
                data.writeRemoteValues(enemy.parameters)
                data.writeRemoteValues(enemy.resistances)
                data.writeRemoteValues(enemy.statusResistances)
            }
            data.writeBoolean(frame.canFastTravel)
            data.writeByte(frame.travelDestinations.take(32).size)
            frame.travelDestinations.take(32).forEach { destination ->
                data.writeInt(destination.areaId); data.writeUtf8(destination.name)
                data.writeUtf8(destination.region); data.writeBoolean(destination.current)
            }
            data.writeBoolean(frame.modsEnabled)
            data.writeByte(frame.mods.take(32).size)
            frame.mods.take(32).forEach { mod ->
                data.writeInt(mod.index); data.writeUtf8(mod.name); data.writeUtf8(mod.detail)
                data.writeBoolean(mod.enabled); data.writeBoolean(mod.custom)
            }
        }
        return output.toByteArray().also { require(it.size <= MAX_PACKET_BYTES) }
    }

    fun decodeTelemetry(bytes: ByteArray, length: Int = bytes.size): RemoteTelemetryFrame? {
        if (length !in 1..minOf(bytes.size, MAX_PACKET_BYTES)) return null
        return runCatching {
            DataInputStream(ByteArrayInputStream(bytes, 0, length)).use { data ->
                if (data.readInt() != MAGIC || data.readUnsignedByte() != VERSION) return null
                if (data.readUnsignedByte() != TYPE_TELEMETRY) return null
                data.readUnsignedShort()
                val pairingCode = data.readInt()
                val sequence = data.readInt()
                val sampledAtMillis = data.readLong()
                val gameStarted = data.readBoolean()
                val areaId = data.readInt()
                val mapId = data.readInt()
                val bits = data.readLong()
                val mode = data.readUtf8()
                val locationTitle = data.readUtf8()
                val locationDetail = data.readUtf8()
                val tamerName = data.readUtf8()
                val objective = data.readUtf8()
                val storyStage = data.readInt()
                val serverName = data.readUtf8()
                val sectorName = data.readUtf8()
                val muted = data.readBoolean()
                val fastForward = data.readBoolean()
                val stateAvailable = data.readBoolean()
                val canReorderParty = data.readBoolean()
                val partyCount = data.readUnsignedByte().coerceAtMost(8)
                val party = List(partyCount) {
                    RemotePartyMember(
                        name = data.readUtf8(),
                        level = data.readInt(),
                        currentHp = data.readInt(),
                        maxHp = data.readInt(),
                        currentMp = data.readInt(),
                        maxMp = data.readInt(),
                        experience = data.readLong(),
                        nextLevelExperience = data.readLong(),
                        activeForm = data.readUtf8(),
                        profileId = data.readInt(),
                        trainingPoints = data.readInt(),
                        forms = List(data.readUnsignedByte().coerceAtMost(16)) {
                            RemoteForm(data.readUtf8(), data.readInt(), data.readBoolean())
                        },
                        parameters = data.readRemoteValues(),
                        resistances = data.readRemoteValues(),
                        statusResistances = data.readRemoteValues(),
                        skills = List(data.readUnsignedByte().coerceAtMost(16)) {
                            RemoteSkill(data.readUtf8(), data.readInt(), data.readInt())
                        },
                        equipment = List(data.readUnsignedByte().coerceAtMost(8)) {
                            RemoteEquipment(data.readUtf8(), data.readUtf8(), data.readUtf8(), data.readUtf8())
                        }
                    )
                }
                val enemies = List(data.readUnsignedByte().coerceAtMost(8)) {
                    RemoteEnemy(
                        name = data.readUtf8(), level = data.readInt(), currentHp = data.readInt(), maxHp = data.readInt(),
                        attacks = data.readUtf8(), loot = data.readUtf8(), parameters = data.readRemoteValues(),
                        resistances = data.readRemoteValues(), statusResistances = data.readRemoteValues()
                    )
                }
                val canFastTravel = data.readBoolean()
                val travelDestinations = List(data.readUnsignedByte().coerceAtMost(32)) {
                    RemoteTravelDestination(data.readInt(), data.readUtf8(), data.readUtf8(), data.readBoolean())
                }
                val modsEnabled = data.readBoolean()
                val mods = List(data.readUnsignedByte().coerceAtMost(32)) {
                    RemoteMod(data.readInt(), data.readUtf8(), data.readUtf8(), data.readBoolean(), data.readBoolean())
                }
                RemoteTelemetryFrame(
                    pairingCode, sequence, sampledAtMillis, mode, gameStarted,
                    areaId, mapId, locationTitle, locationDetail, tamerName,
                    objective, bits, party, storyStage, serverName, sectorName,
                    enemies, muted, fastForward, stateAvailable, canReorderParty,
                    canFastTravel, travelDestinations, modsEnabled, mods
                )
            }
        }.getOrNull()
    }

    private fun axisToShort(value: Float): Short =
        (value.takeIf(Float::isFinite)?.coerceIn(-1f, 1f)?.times(32_767f) ?: 0f).toInt().toShort()

    private fun shortToAxis(value: Short): Float =
        if (value.toInt() == Short.MIN_VALUE.toInt()) -1f else value / 32_767f

    private fun DataOutputStream.writeUtf8(value: String) {
        val bytes = value.toByteArray(Charsets.UTF_8)
        require(bytes.size <= 8_192) { "Remote string is too long" }
        writeShort(bytes.size)
        write(bytes)
    }

    private fun DataOutputStream.writeRemoteValues(values: List<RemoteValue>) {
        writeByte(values.take(16).size)
        values.take(16).forEach { value ->
            writeUtf8(value.name); writeInt(value.value); writeInt(value.bonus)
        }
    }

    private fun DataInputStream.readRemoteValues(): List<RemoteValue> =
        List(readUnsignedByte().coerceAtMost(16)) { RemoteValue(readUtf8(), readInt(), readInt()) }

    private fun DataInputStream.readUtf8(): String {
        val size = readUnsignedShort()
        require(size <= 8_192 && size <= available()) { "Invalid remote string length" }
        return ByteArray(size).also(::readFully).toString(Charsets.UTF_8)
    }
}
