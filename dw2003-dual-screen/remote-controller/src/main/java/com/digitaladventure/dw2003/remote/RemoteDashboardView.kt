package com.digitaladventure.dw2003.remote

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.view.MotionEvent
import android.view.View
import com.digitaladventure.dw2003.remote.protocol.RemoteCommand
import com.digitaladventure.dw2003.remote.protocol.RemoteEnemy
import com.digitaladventure.dw2003.remote.protocol.RemoteEquipment
import com.digitaladventure.dw2003.remote.protocol.RemotePartyMember
import com.digitaladventure.dw2003.remote.protocol.RemoteTelemetryFrame
import com.digitaladventure.dw2003.remote.protocol.RemoteValue
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class RemoteDashboardView(context: Context) : View(context) {
    var onCommand: (RemoteCommand, Int) -> Unit = { _, _ -> }
    var onAddCustomMod: () -> Unit = {}
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var telemetry: RemoteTelemetryFrame? = null
    private var selectedTab = 0
    private var selectedMember = 0
    private var scroll = 0f
    private var scrollMax = 0f
    private var travelScroll = 0f
    private var travelScrollMax = 0f
    private var explorationPartyScroll = 0f
    private var explorationPartyScrollMax = 0f
    private var battlePartyScroll = 0f
    private var battlePartyScrollMax = 0f
    private var battleEnemyScroll = 0f
    private var battleEnemyScrollMax = 0f
    private var guideScroll = 0f
    private var guideScrollMax = 0f
    private var travelViewport: RectF? = null
    private var explorationPartyViewport: RectF? = null
    private var battlePartyViewport: RectF? = null
    private var battleEnemyViewport: RectF? = null
    private var guideViewport: RectF? = null
    private var activeScrollTarget = ScrollTarget.PAGE
    private var lastTouchY = 0f
    private var dragging = false
    private val hits = mutableListOf<Pair<RectF, () -> Unit>>()
    private val sprites: Map<Int, Bitmap> by lazy {
        mapOf(
            0 to transparentSprite(bitmap(R.drawable.digimon_kotemon)), 1 to transparentSprite(bitmap(R.drawable.digimon_kumamon)),
            2 to transparentSprite(bitmap(R.drawable.digimon_monmon)), 3 to transparentSprite(bitmap(R.drawable.digimon_agumon)),
            4 to transparentSprite(bitmap(R.drawable.digimon_veemon)), 5 to transparentSprite(bitmap(R.drawable.digimon_guilmon)),
            6 to transparentSprite(bitmap(R.drawable.digimon_renamon)), 7 to transparentSprite(bitmap(R.drawable.digimon_patamon))
        )
    }
    private val tamerSprite: Bitmap by lazy { transparentSprite(bitmap(R.drawable.tamer_idle)) }

    fun render(frame: RemoteTelemetryFrame) {
        telemetry = frame
        selectedMember = selectedMember.coerceIn(0, max(0, frame.party.lastIndex))
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawColor(BACKGROUND)
        drawGrid(canvas)
        hits.clear()
        travelViewport = null
        explorationPartyViewport = null
        battlePartyViewport = null
        battleEnemyViewport = null
        guideViewport = null
        val frame = telemetry
        drawHeader(canvas, frame?.locationTitle?.takeIf(String::isNotBlank) ?: "DW2003 POCKET COMPANION", TABS[selectedTab])
        val viewport = RectF(dp(12f), dp(68f), width - dp(12f), height - dp(48f))
        scrollMax = if (frame == null) {
            drawPanel(canvas, viewport, "LISTO PARA EMPAREJAR")
            drawWrapped(canvas, "Conecta con el Fold para recibir mapa, guía y estado del equipo.", viewport.left + dp(12f), viewport.top + dp(43f), viewport.width() - dp(24f), dp(12f), MUTED, 4)
            0f
        } else when (selectedTab) {
            0 -> drawExploration(canvas, viewport, frame)
            1 -> drawBattle(canvas, viewport, frame)
            2 -> drawManagement(canvas, viewport, frame)
            else -> drawMods(canvas, viewport, frame)
        }
        scroll = scroll.coerceIn(0f, scrollMax)
        if (scrollMax > 0f) drawScrollThumb(canvas, viewport, scroll, scrollMax)
        drawTabs(canvas)
    }

    private fun drawExploration(canvas: Canvas, bounds: RectF, frame: RemoteTelemetryFrame): Float {
        val gap = dp(7f)
        val mapWidth = bounds.width() * .36f
        val remaining = bounds.width() - mapWidth - gap * 2
        val teamWidth = remaining * .54f
        val map = RectF(bounds.left, bounds.top, bounds.left + mapWidth, bounds.bottom)
        val team = RectF(map.right + gap, bounds.top, map.right + gap + teamWidth, bounds.bottom)
        val guide = RectF(team.right + gap, bounds.top, bounds.right, bounds.bottom)
        drawPanel(canvas, map, "MAPA · ${frame.sectorName.uppercase()}", Paint.Align.CENTER)
        val imageBottom = min(map.bottom - dp(86f), map.top + dp(125f))
        drawMap(canvas, RectF(map.left + dp(8f), map.top + dp(29f), map.right - dp(8f), imageBottom), frame)
        val travelTop = imageBottom + dp(7f)
        drawText(canvas, "VIAJE RÁPIDO · ${frame.travelDestinations.size} DESTINOS", map.centerX(), travelTop + dp(11f), dp(7f), CYAN, true, Paint.Align.CENTER)
        val travelClip = RectF(map.left + dp(6f), travelTop + dp(16f), map.right - dp(6f), map.bottom - dp(7f))
        travelViewport = travelClip
        explorationPartyViewport = RectF(team.left + dp(5f), team.top + dp(28f), team.right - dp(5f), team.bottom - dp(6f))
        val travelBottom = drawTravelList(canvas, travelClip, frame, travelScroll)
        val partyBottom = drawPartyColumn(canvas, team, frame.party, "EQUIPO ACTIVO", explorationPartyScroll)
        travelScrollMax = max(0f, travelBottom - travelClip.bottom)
        explorationPartyScrollMax = max(0f, partyBottom - (team.bottom - dp(6f)))
        travelScroll = travelScroll.coerceIn(0f, travelScrollMax)
        explorationPartyScroll = explorationPartyScroll.coerceIn(0f, explorationPartyScrollMax)
        if (travelScrollMax > 0f) drawScrollThumb(canvas, travelClip, travelScroll, travelScrollMax)
        explorationPartyViewport?.let { if (explorationPartyScrollMax > 0f) drawScrollThumb(canvas, it, explorationPartyScroll, explorationPartyScrollMax) }
        val guideBox = RectF(guide.left, guide.top, guide.right, guide.top + guide.height() * .53f)
        val tamerBox = RectF(guide.left, guideBox.bottom + gap, guide.right, guide.bottom)
        drawPanel(canvas, guideBox, "GUÍA FLAWE · HISTORIA ${frame.storyStage}", Paint.Align.CENTER)
        val objective = frame.objective.ifBlank { "Sin objetivo disponible." }
        val objectiveClip = RectF(guideBox.left + dp(7f), guideBox.top + dp(29f), guideBox.right - dp(7f), guideBox.bottom - dp(7f))
        guideViewport = objectiveClip
        val objectiveLines = wrappedLineCount(objective, objectiveClip.width() - dp(8f), dp(9f))
        canvas.save()
        canvas.clipRect(objectiveClip)
        canvas.translate(0f, -guideScroll)
        drawWrapped(canvas, objective, objectiveClip.left + dp(4f), objectiveClip.top + dp(12f), objectiveClip.width() - dp(8f), dp(9f), WHITE, objectiveLines)
        canvas.restore()
        guideScrollMax = max(0f, objectiveLines * dp(9f) * 1.35f - objectiveClip.height() + dp(9f))
        guideScroll = guideScroll.coerceIn(0f, guideScrollMax)
        if (guideScrollMax > 0f) drawScrollThumb(canvas, objectiveClip, guideScroll, guideScrollMax)

        drawPanel(canvas, tamerBox, "ESTADO DEL TAMER", Paint.Align.CENTER)
        val spriteHeight = min(dp(58f), tamerBox.height() - dp(45f))
        val spriteWidth = spriteHeight * .62f
        val contentWidth = spriteWidth + dp(10f) + min(dp(76f), tamerBox.width() * .47f)
        val contentLeft = tamerBox.centerX() - contentWidth / 2f
        val tamerBounds = RectF(contentLeft, tamerBox.top + dp(31f), contentLeft + spriteWidth, tamerBox.top + dp(31f) + spriteHeight)
        paint.isFilterBitmap = false
        canvas.drawBitmap(tamerSprite, null, tamerBounds, paint)
        val tamerTextX = tamerBounds.right + dp(10f)
        drawText(canvas, "TAMER · ${frame.tamerName.uppercase()}", tamerTextX, tamerBox.top + dp(45f), dp(8.5f), WHITE, true)
        drawText(canvas, "ACTIVIDAD · EXPLORANDO", tamerTextX, tamerBox.top + dp(62f), dp(6.5f), CYAN, true)
        drawText(canvas, "BITS", tamerTextX, tamerBox.top + dp(80f), dp(6.5f), MUTED, true)
        drawText(canvas, frame.bits.toString(), tamerTextX, tamerBox.top + dp(98f), dp(13f), AMBER, true)
        return 0f
    }

    private fun drawBattle(canvas: Canvas, bounds: RectF, frame: RemoteTelemetryFrame): Float {
        val gap = dp(7f)
        val team = RectF(bounds.left, bounds.top, bounds.centerX() - gap / 2, bounds.bottom)
        val enemies = RectF(bounds.centerX() + gap / 2, bounds.top, bounds.right, bounds.bottom)
        battlePartyViewport = RectF(team.left + dp(5f), team.top + dp(28f), team.right - dp(5f), team.bottom - dp(6f))
        battleEnemyViewport = RectF(enemies.left + dp(5f), enemies.top + dp(28f), enemies.right - dp(5f), enemies.bottom - dp(6f))
        val teamBottom = drawPartyColumn(
            canvas, team, frame.party, "EQUIPO", battlePartyScroll,
            showReorder = true, reorderEnabled = frame.canReorderParty
        )
        drawPanel(canvas, enemies, "ENEMIGOS EN BATALLA")
        val clip = RectF(enemies.left + dp(5f), enemies.top + dp(28f), enemies.right - dp(5f), enemies.bottom - dp(6f))
        var y = clip.top + dp(6f)
        canvas.save(); canvas.clipRect(clip); canvas.translate(0f, -battleEnemyScroll)
        if (frame.enemies.isEmpty()) {
            drawText(canvas, "SIN ENCUENTROS LEÍDOS", clip.left + dp(5f), y + dp(16f), dp(9f), MUTED, true)
            y += dp(65f)
        } else frame.enemies.forEach { enemy ->
            val card = RectF(clip.left, y, clip.right, y + dp(266f))
            drawEnemyCard(canvas, card, enemy); y = card.bottom + gap
        }
        canvas.restore()
        battlePartyScrollMax = max(0f, teamBottom - (team.bottom - dp(6f)))
        battleEnemyScrollMax = max(0f, y - clip.bottom)
        battlePartyScroll = battlePartyScroll.coerceIn(0f, battlePartyScrollMax)
        battleEnemyScroll = battleEnemyScroll.coerceIn(0f, battleEnemyScrollMax)
        battlePartyViewport?.let { if (battlePartyScrollMax > 0f) drawScrollThumb(canvas, it, battlePartyScroll, battlePartyScrollMax) }
        battleEnemyViewport?.let { if (battleEnemyScrollMax > 0f) drawScrollThumb(canvas, it, battleEnemyScroll, battleEnemyScrollMax) }
        return 0f
    }

    private fun drawManagement(canvas: Canvas, bounds: RectF, frame: RemoteTelemetryFrame): Float {
        val member = frame.party.getOrNull(selectedMember)
        if (member == null) {
            drawPanel(canvas, bounds, "GESTIÓN · PARTNER")
            drawText(canvas, "INICIA O CARGA UNA PARTIDA", bounds.left + dp(12f), bounds.top + dp(48f), dp(10f), MUTED, true)
            return 0f
        }
        val inner = RectF(bounds.left, bounds.top, bounds.right - dp(5f), bounds.bottom)
        var y = inner.top
        canvas.save(); canvas.clipRect(bounds); canvas.translate(0f, -scroll)
        val identity = RectF(inner.left, y, inner.right, y + dp(164f))
        drawIdentity(canvas, identity, member, frame.party.size)
        y = identity.bottom + dp(7f)
        val half = (inner.width() - dp(7f)) / 2f
        val statsHeight = dp(148f)
        val parameters = RectF(inner.left, y, inner.left + half, y + statsHeight)
        val resists = RectF(parameters.right + dp(7f), y, inner.right, y + statsHeight)
        drawValuePanel(canvas, parameters, "PARÁMETROS", member.parameters, 3)
        drawValuePanel(canvas, resists, "RESISTENCIAS ELEMENTALES", member.resistances, 4, highlightExtremes = true)
        y = parameters.bottom + dp(7f)
        val status = RectF(inner.left, y, inner.right, y + dp(78f))
        drawValuePanel(canvas, status, "RESISTENCIAS DE ESTADO", member.statusResistances, 2, columns = 3, highlightExtremes = true)
        y = status.bottom + dp(7f)
        val skillsHeight = max(dp(118f), dp(48f) + member.skills.size * dp(34f))
        val equipmentHeight = dp(38f) + max(1, member.equipment.size) * dp(37f)
        val bottom = y + max(skillsHeight, equipmentHeight)
        val skills = RectF(inner.left, y, inner.left + half, bottom)
        val equipment = RectF(skills.right + dp(7f), y, inner.right, bottom)
        drawSkills(canvas, skills, member); drawEquipment(canvas, equipment, member.equipment)
        y = bottom + dp(8f)
        canvas.restore()
        return max(0f, y - bounds.bottom)
    }

    private fun drawMods(canvas: Canvas, bounds: RectF, frame: RemoteTelemetryFrame): Float {
        var y = bounds.top
        canvas.save(); canvas.clipRect(bounds); canvas.translate(0f, -scroll)
        val header = RectF(bounds.left, y, bounds.right - dp(5f), y + dp(94f))
        drawPanel(canvas, header, "MODS Y CÓDIGOS PAL")
        drawWrapped(canvas, "Códigos aplicados a la RAM emulada de Flawe's Mod. Evita activarlos durante secuencias críticas.", header.left + dp(11f), header.top + dp(39f), header.width() - dp(22f), dp(8f), MUTED, 2)
        val add = RectF(header.left + dp(11f), header.bottom - dp(34f), header.right - dp(11f), header.bottom - dp(7f))
        drawButton(canvas, add, "AÑADIR MOD PERSONALIZADO", false)
        addScrolledHit(add) { onAddCustomMod() }
        y = header.bottom + dp(7f)
        if (frame.mods.isEmpty()) {
            val empty = RectF(bounds.left, y, bounds.right - dp(5f), y + dp(72f)); drawPanel(canvas, empty, "SIN MODS DISPONIBLES"); y = empty.bottom
        } else frame.mods.forEach { mod ->
            val row = RectF(bounds.left, y, bounds.right - dp(5f), y + dp(55f))
            paint.color = PANEL; canvas.drawRoundRect(row, dp(6f), dp(6f), paint)
            drawText(canvas, mod.name.uppercase(), row.left + dp(11f), row.top + dp(19f), dp(10f), WHITE, true)
            drawText(canvas, ellipsize(mod.detail, row.width() - dp(120f), dp(7.5f)), row.left + dp(11f), row.top + dp(39f), dp(7.5f), MUTED)
            val toggle = RectF(row.right - dp(92f), row.top + dp(13f), row.right - dp(10f), row.bottom - dp(12f))
            drawButton(canvas, toggle, if (mod.enabled) "ON" else "OFF", mod.enabled)
            addScrolledHit(toggle) { onCommand(RemoteCommand.TOGGLE_MOD, mod.index) }
            y = row.bottom + dp(6f)
        }
        canvas.restore()
        return max(0f, y - bounds.bottom + dp(5f))
    }

    private fun drawTravelList(canvas: Canvas, clip: RectF, frame: RemoteTelemetryFrame, offset: Float): Float {
        var y = clip.top + dp(3f)
        canvas.save(); canvas.clipRect(clip); canvas.translate(0f, -offset)
        if (frame.travelDestinations.isEmpty()) {
            drawWrapped(canvas, "Explora el campo para registrar destinos.", clip.left + dp(3f), y + dp(12f), clip.width() - dp(6f), dp(7f), MUTED, 3); y += dp(48f)
        } else frame.travelDestinations.forEach { destination ->
            val row = RectF(clip.left, y, clip.right, y + dp(25f))
            paint.color = if (destination.current) CYAN_DARK else PANEL_INNER; canvas.drawRoundRect(row, dp(4f), dp(4f), paint)
            drawText(canvas, ellipsize(destination.name.uppercase(), row.width() - dp(58f), dp(7f)), row.left + dp(6f), row.centerY() + dp(3f), dp(7f), WHITE, true)
            drawText(canvas, "0x${destination.areaId.toString(16).uppercase().padStart(4, '0')}", row.right - dp(6f), row.centerY() + dp(3f), dp(6.5f), MUTED, true, Paint.Align.RIGHT)
            if (frame.canFastTravel && !destination.current) addScrolledHit(row, offset) { onCommand(RemoteCommand.FAST_TRAVEL, destination.areaId) }
            y = row.bottom + dp(4f)
        }
        canvas.restore(); return y
    }

    private fun drawPartyColumn(
        canvas: Canvas,
        bounds: RectF,
        party: List<RemotePartyMember>,
        title: String,
        offset: Float,
        showReorder: Boolean = false,
        reorderEnabled: Boolean = false
    ): Float {
        drawPanel(canvas, bounds, title)
        val clip = RectF(bounds.left + dp(5f), bounds.top + dp(28f), bounds.right - dp(5f), bounds.bottom - dp(6f))
        var y = clip.top + dp(6f)
        canvas.save(); canvas.clipRect(clip); canvas.translate(0f, -offset)
        if (party.isEmpty()) {
            drawText(canvas, "SIN EQUIPO EN RAM", clip.left + dp(5f), y + dp(16f), dp(9f), MUTED, true); y += dp(60f)
        } else party.take(3).forEachIndexed { index, member ->
            val card = RectF(clip.left, y, clip.right, y + dp(304f) + max(1, member.forms.size) * dp(15f))
            drawPartyCard(canvas, card, member, index, party.size, showReorder, reorderEnabled, offset); y = card.bottom + dp(7f)
        }
        canvas.restore(); return y
    }

    private fun drawPartyCard(
        canvas: Canvas,
        bounds: RectF,
        member: RemotePartyMember,
        partyIndex: Int,
        partySize: Int,
        showReorder: Boolean,
        reorderEnabled: Boolean,
        scrollOffset: Float
    ) {
        paint.color = PANEL_INNER; canvas.drawRoundRect(bounds, dp(7f), dp(7f), paint)
        val sprite = RectF(bounds.left + dp(8f), bounds.top + dp(8f), bounds.left + dp(52f), bounds.top + dp(52f))
        paint.isFilterBitmap = false
        sprites[member.profileId]?.let { canvas.drawBitmap(it, null, sprite, paint) }
        val x = sprite.right + dp(7f)
        val titleRight = if (showReorder) bounds.right - dp(42f) else bounds.right - dp(8f)
        drawText(canvas, ellipsize(member.name.uppercase(), titleRight - x, dp(10f)), x, bounds.top + dp(16f), dp(10f), WHITE, true)
        drawText(canvas, "NV ${member.level} · TP ${member.trainingPoints}", x, bounds.top + dp(29f), dp(7f), MUTED, true)
        if (showReorder) {
            val up = RectF(bounds.right - dp(36f), bounds.top + dp(6f), bounds.right - dp(8f), bounds.top + dp(28f))
            val down = RectF(bounds.right - dp(36f), bounds.top + dp(31f), bounds.right - dp(8f), bounds.top + dp(53f))
            drawButton(canvas, up, "▲", reorderEnabled && partyIndex > 0)
            drawButton(canvas, down, "▼", reorderEnabled && partyIndex < partySize - 1)
            if (reorderEnabled && partyIndex > 0) addScrolledHit(up, scrollOffset) { onCommand(RemoteCommand.MOVE_PARTY, (partyIndex shl 8) or (partyIndex - 1)) }
            if (reorderEnabled && partyIndex < partySize - 1) addScrolledHit(down, scrollOffset) { onCommand(RemoteCommand.MOVE_PARTY, (partyIndex shl 8) or (partyIndex + 1)) }
        }
        val metricLeft = bounds.left + dp(8f)
        val metricRight = bounds.right - dp(8f)
        drawStackedMetric(canvas, "HP ${member.currentHp}/${member.maxHp}", metricLeft, bounds.top + dp(55f), metricRight, ratio(member.currentHp, member.maxHp), GREEN)
        drawStackedMetric(canvas, "MP ${member.currentMp}/${member.maxMp}", metricLeft, bounds.top + dp(79f), metricRight, ratio(member.currentMp, member.maxMp), BLUE)
        drawStackedMetric(canvas, "EXP ${member.experience}/${member.nextLevelExperience}", metricLeft, bounds.top + dp(103f), metricRight, ratio(member.experience, member.nextLevelExperience), AMBER)
        var sectionY = bounds.top + dp(137f)
        sectionY = drawCompactValues(canvas, bounds, sectionY, "PARÁMETROS", member.parameters, 3, false)
        sectionY = drawCompactValues(canvas, bounds, sectionY, "ELEMENTALES", member.resistances, 4, true)
        sectionY = drawCompactValues(canvas, bounds, sectionY, "ESTADOS", member.statusResistances, 3, true)
        drawText(canvas, "DIGIEVOLUCIONES", bounds.left + dp(8f), sectionY + dp(2f), dp(7f), CYAN, true)
        member.forms.forEachIndexed { index, form ->
            val y = sectionY + dp(18f) + index * dp(15f)
            drawText(canvas, form.name.uppercase(), bounds.left + dp(8f), y, dp(7f), if (form.active) BLUE else WHITE, form.active)
            drawText(canvas, "NV ${form.level}", bounds.right - dp(8f), y, dp(7f), if (form.active) BLUE else MUTED, form.active, Paint.Align.RIGHT)
        }
    }

    private fun drawIdentity(canvas: Canvas, bounds: RectF, member: RemotePartyMember, count: Int) {
        drawPanel(canvas, bounds, "PARTNER / ESTADO · ${selectedMember + 1}/$count")
        val left = RectF(bounds.left + dp(9f), bounds.top + dp(31f), bounds.left + dp(39f), bounds.top + dp(61f))
        val right = RectF(bounds.right - dp(39f), left.top, bounds.right - dp(9f), left.bottom)
        drawButton(canvas, left, "‹", false); drawButton(canvas, right, "›", false)
        addScrolledHit(left) { selectedMember = (selectedMember - 1 + count) % count; scroll = 0f; invalidate() }
        addScrolledHit(right) { selectedMember = (selectedMember + 1) % count; scroll = 0f; invalidate() }
        val sprite = RectF(bounds.left + dp(49f), bounds.top + dp(29f), bounds.left + dp(91f), bounds.top + dp(71f))
        sprites[member.profileId]?.let { canvas.drawBitmap(it, null, sprite, paint) }
        val x = sprite.right + dp(9f)
        drawText(canvas, member.name.uppercase(), x, bounds.top + dp(43f), dp(12f), WHITE, true)
        drawText(canvas, "NV ${member.level} · TP ${member.trainingPoints} · ${member.activeForm.uppercase()}", x, bounds.top + dp(61f), dp(8f), CYAN, true)
        val barLeft = bounds.left + dp(12f)
        val barRight = bounds.right - dp(12f)
        drawStackedMetric(canvas, "HP ${member.currentHp}/${member.maxHp}", barLeft, bounds.top + dp(78f), barRight, ratio(member.currentHp, member.maxHp), GREEN)
        drawStackedMetric(canvas, "MP ${member.currentMp}/${member.maxMp}", barLeft, bounds.top + dp(104f), barRight, ratio(member.currentMp, member.maxMp), BLUE)
        drawStackedMetric(canvas, "EXP ${member.experience}/${member.nextLevelExperience}", barLeft, bounds.top + dp(130f), barRight, ratio(member.experience, member.nextLevelExperience), AMBER)
    }

    private fun drawValuePanel(
        canvas: Canvas,
        bounds: RectF,
        title: String,
        values: List<RemoteValue>,
        rows: Int,
        columns: Int = 2,
        highlightExtremes: Boolean = false
    ) {
        drawPanel(canvas, bounds, title)
        val cellW = (bounds.width() - dp(20f)) / columns
        val cellH = max(dp(16f), (bounds.height() - dp(36f)) / rows)
        val minimum = values.minOfOrNull(RemoteValue::value)
        val maximum = values.maxOfOrNull(RemoteValue::value)
        val hasSpread = highlightExtremes && minimum != maximum
        canvas.save()
        canvas.clipRect(bounds.left, bounds.top + dp(26f), bounds.right, bounds.bottom - dp(4f))
        values.forEachIndexed { index, value ->
            val cellLeft = bounds.left + dp(10f) + (index % columns) * cellW
            val y = bounds.top + dp(30f) + (index / columns) * cellH + cellH * .55f
            val color = when {
                hasSpread && value.value == maximum -> GREEN
                hasSpread && value.value == minimum -> RED
                value.bonus > 0 -> BLUE
                value.bonus < 0 -> RED
                else -> WHITE
            }
            val labelColor = if (hasSpread && (value.value == maximum || value.value == minimum)) {
                color
            } else if (value.bonus != 0) {
                color
            } else {
                MUTED
            }
            drawNameValue(canvas, value.name, value.value.toString(), cellLeft, y, dp(7f), labelColor, color, dp(11f))
        }
        canvas.restore()
    }

    private fun drawSkills(canvas: Canvas, bounds: RectF, member: RemotePartyMember) {
        drawPanel(canvas, bounds, "HABILIDADES ACTIVAS · ${member.activeForm.uppercase()}")
        if (member.skills.isEmpty()) drawText(canvas, "SIN TÉCNICAS IDENTIFICADAS", bounds.left + dp(10f), bounds.top + dp(47f), dp(8f), MUTED, true)
        member.skills.forEachIndexed { index, skill ->
            val y = bounds.top + dp(43f) + index * dp(34f)
            drawText(canvas, skill.name, bounds.left + dp(10f), y, dp(9f), WHITE, true)
            drawText(canvas, "MP ${numberOrDash(skill.mp)} · PODER ${numberOrDash(skill.power)}", bounds.left + dp(10f), y + dp(15f), dp(7f), CYAN, true)
        }
    }

    private fun drawEquipment(canvas: Canvas, bounds: RectF, equipment: List<RemoteEquipment>) {
        drawPanel(canvas, bounds, "EQUIPO Y BONIFICACIONES")
        equipment.forEachIndexed { index, item ->
            val y = bounds.top + dp(39f) + index * dp(37f)
            drawText(canvas, item.slot, bounds.left + dp(10f), y, dp(6.5f), MUTED, true)
            drawText(canvas, ellipsize(item.name, bounds.width() - dp(20f), dp(8f)), bounds.left + dp(10f), y + dp(13f), dp(8f), WHITE, true)
            if (item.bonuses.isNotBlank()) drawText(canvas, ellipsize(item.bonuses, bounds.width() - dp(20f), dp(6.5f)), bounds.left + dp(10f), y + dp(25f), dp(6.5f), CYAN)
        }
    }

    private fun drawEnemyCard(canvas: Canvas, bounds: RectF, enemy: RemoteEnemy) {
        paint.color = Color.rgb(18, 8, 14); canvas.drawRoundRect(bounds, dp(7f), dp(7f), paint)
        drawText(canvas, enemy.name.uppercase(), bounds.left + dp(9f), bounds.top + dp(19f), dp(10f), WHITE, true)
        drawText(canvas, "NV ${enemy.level}", bounds.right - dp(9f), bounds.top + dp(19f), dp(8f), MUTED, true, Paint.Align.RIGHT)
        drawStackedMetric(canvas, "HP ${enemy.currentHp}/${enemy.maxHp}", bounds.left + dp(9f), bounds.top + dp(32f), bounds.right - dp(9f), ratio(enemy.currentHp, enemy.maxHp), RED)
        var sectionY = bounds.top + dp(70f)
        sectionY = drawCompactValues(canvas, bounds, sectionY, "PARÁMETROS", enemy.parameters, 3, false)
        sectionY = drawCompactValues(canvas, bounds, sectionY, "ELEMENTALES", enemy.resistances, 4, true)
        sectionY = drawCompactValues(canvas, bounds, sectionY, "ESTADOS", enemy.statusResistances, 3, true)
        drawText(canvas, ellipsize(enemy.attacks.ifBlank { "Sin técnicas catalogadas" }, bounds.width() - dp(18f), dp(7.5f)), bounds.left + dp(9f), sectionY + dp(4f), dp(7.5f), WHITE)
        drawText(canvas, ellipsize("BOTÍN: ${enemy.loot}", bounds.width() - dp(18f), dp(7f)), bounds.left + dp(9f), sectionY + dp(20f), dp(7f), MUTED)
    }

    private fun drawCompactValues(
        canvas: Canvas,
        bounds: RectF,
        top: Float,
        title: String,
        values: List<RemoteValue>,
        columns: Int,
        highlightExtremes: Boolean
    ): Float {
        if (values.isEmpty()) return top
        drawText(canvas, title, bounds.left + dp(8f), top, dp(6.5f), CYAN, true)
        val minimum = values.minOfOrNull(RemoteValue::value)
        val maximum = values.maxOfOrNull(RemoteValue::value)
        val spread = highlightExtremes && minimum != maximum
        val inset = dp(8f)
        val cellWidth = (bounds.width() - inset * 2) / columns
        val rowHeight = dp(16f)
        values.forEachIndexed { index, value ->
            val color = when {
                value.bonus > 0 -> BLUE
                value.bonus < 0 -> RED
                spread && value.value == maximum -> GREEN
                spread && value.value == minimum -> RED
                else -> WHITE
            }
            val label = value.name.take(3).uppercase()
            val cellLeft = bounds.left + inset + (index % columns) * cellWidth
            val y = top + dp(17f) + (index / columns) * rowHeight
            drawNameValue(canvas, label, value.value.toString(), cellLeft, y, dp(7f), MUTED, color)
        }
        val rows = (values.size + columns - 1) / columns
        return top + dp(20f) + rows * rowHeight
    }

    private fun drawMap(canvas: Canvas, bounds: RectF, frame: RemoteTelemetryFrame) {
        val image = bitmap(mapResource(frame.serverName, frame.sectorName)); val scale = min(bounds.width() / image.width, bounds.height() / image.height)
        val w = image.width * scale; val h = image.height * scale
        val destination = RectF(bounds.centerX() - w / 2, bounds.centerY() - h / 2, bounds.centerX() + w / 2, bounds.centerY() + h / 2)
        paint.isFilterBitmap = true; canvas.drawBitmap(image, null, destination, paint)
        paint.style = Paint.Style.STROKE; paint.strokeWidth = dp(1.5f); paint.color = CYAN; canvas.drawRect(destination, paint); paint.style = Paint.Style.FILL
    }

    private fun mapResource(server: String, sector: String): Int {
        val amaterasu = server.contains("amaterasu", true)
        return when {
            amaterasu && sector.contains("central", true) -> R.drawable.map_amaterasu_central
            amaterasu && sector.contains("este", true) -> R.drawable.map_amaterasu_east
            amaterasu && sector.contains("sur", true) -> R.drawable.map_amaterasu_south
            amaterasu && sector.contains("oeste", true) -> R.drawable.map_amaterasu_west
            amaterasu && sector.contains("norte", true) -> R.drawable.map_amaterasu_north
            amaterasu -> R.drawable.map_amaterasu_overview
            sector.contains("central", true) -> R.drawable.map_asuka_central
            sector.contains("este", true) -> R.drawable.map_asuka_east
            sector.contains("sur", true) -> R.drawable.map_asuka_south
            sector.contains("oeste", true) -> R.drawable.map_asuka_west
            sector.contains("norte", true) -> R.drawable.map_asuka_north
            else -> R.drawable.map_asuka_overview
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                lastTouchY = event.y
                dragging = false
                activeScrollTarget = resolveScrollTarget(event.x, event.y)
            }
            MotionEvent.ACTION_MOVE -> {
                val delta = lastTouchY - event.y; if (abs(delta) > dp(2f)) dragging = true
                lastTouchY = event.y
                when (activeScrollTarget) {
                    ScrollTarget.TRAVEL -> travelScroll = (travelScroll + delta).coerceIn(0f, travelScrollMax)
                    ScrollTarget.EXPLORATION_PARTY -> explorationPartyScroll = (explorationPartyScroll + delta).coerceIn(0f, explorationPartyScrollMax)
                    ScrollTarget.BATTLE_PARTY -> battlePartyScroll = (battlePartyScroll + delta).coerceIn(0f, battlePartyScrollMax)
                    ScrollTarget.BATTLE_ENEMIES -> battleEnemyScroll = (battleEnemyScroll + delta).coerceIn(0f, battleEnemyScrollMax)
                    ScrollTarget.GUIDE -> guideScroll = (guideScroll + delta).coerceIn(0f, guideScrollMax)
                    ScrollTarget.PAGE -> scroll = (scroll + delta).coerceIn(0f, scrollMax)
                }
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                if (!dragging) {
                    if (event.y >= height - dp(48f)) {
                        selectedTab = (event.x / (width / TABS.size.toFloat())).toInt().coerceIn(TABS.indices); scroll = 0f; invalidate()
                    } else hits.lastOrNull { it.first.contains(event.x, event.y) }?.second?.invoke()
                }
                performClick()
            }
        }
        return true
    }

    private fun resolveScrollTarget(x: Float, y: Float): ScrollTarget = when {
        selectedTab == 0 && travelViewport?.contains(x, y) == true -> ScrollTarget.TRAVEL
        selectedTab == 0 && explorationPartyViewport?.contains(x, y) == true -> ScrollTarget.EXPLORATION_PARTY
        selectedTab == 0 && guideViewport?.contains(x, y) == true -> ScrollTarget.GUIDE
        selectedTab == 1 && battlePartyViewport?.contains(x, y) == true -> ScrollTarget.BATTLE_PARTY
        selectedTab == 1 && battleEnemyViewport?.contains(x, y) == true -> ScrollTarget.BATTLE_ENEMIES
        else -> ScrollTarget.PAGE
    }

    override fun performClick(): Boolean { super.performClick(); return true }
    private fun addScrolledHit(bounds: RectF, offset: Float = scroll, action: () -> Unit) = hits.add(RectF(bounds.left, bounds.top - offset, bounds.right, bounds.bottom - offset) to action)

    private fun drawHeader(canvas: Canvas, title: String, section: String) {
        drawText(canvas, "● ${title.uppercase()}", dp(14f), dp(27f), dp(16f), WHITE, true)
        drawText(canvas, section, width - dp(14f), dp(48f), dp(9f), CYAN, true, Paint.Align.RIGHT)
        paint.color = CYAN_DARK; canvas.drawRect(dp(14f), dp(59f), width - dp(14f), dp(61f), paint)
    }

    private fun drawTabs(canvas: Canvas) {
        val top = height - dp(44f); val itemWidth = width / TABS.size.toFloat()
        TABS.forEachIndexed { index, label ->
            val bounds = RectF(index * itemWidth, top, (index + 1) * itemWidth, height.toFloat())
            paint.color = if (index == selectedTab) Color.rgb(8, 105, 126) else PANEL; canvas.drawRect(bounds, paint)
            drawText(canvas, label, bounds.centerX(), bounds.centerY() + dp(4f), dp(8f), if (index == selectedTab) WHITE else MUTED, true, Paint.Align.CENTER)
        }
    }

    private fun drawPanel(canvas: Canvas, bounds: RectF, title: String, align: Paint.Align = Paint.Align.LEFT) {
        paint.style = Paint.Style.FILL; paint.color = PANEL; canvas.drawRoundRect(bounds, dp(7f), dp(7f), paint)
        paint.style = Paint.Style.STROKE; paint.strokeWidth = dp(1f); paint.color = CYAN_DARK; canvas.drawRoundRect(bounds, dp(7f), dp(7f), paint)
        val x = when (align) { Paint.Align.CENTER -> bounds.centerX(); Paint.Align.RIGHT -> bounds.right - dp(10f); else -> bounds.left + dp(10f) }
        paint.style = Paint.Style.FILL; drawText(canvas, title, x, bounds.top + dp(20f), dp(8f), CYAN, true, align)
    }

    private fun drawButton(canvas: Canvas, bounds: RectF, text: String, active: Boolean) {
        paint.color = if (active) CYAN else CYAN_DARK; canvas.drawRoundRect(bounds, dp(5f), dp(5f), paint)
        drawText(canvas, text, bounds.centerX(), bounds.centerY() + dp(4f), dp(8f), if (active) BACKGROUND else WHITE, true, Paint.Align.CENTER)
    }

    private fun drawMetric(canvas: Canvas, label: String, labelX: Float, baseline: Float, barX: Float, barWidth: Float, fraction: Float, color: Int) {
        drawText(canvas, label, labelX, baseline, dp(7f), WHITE, true)
        paint.color = Color.rgb(10, 31, 39); canvas.drawRoundRect(RectF(barX, baseline - dp(6f), barX + barWidth, baseline - dp(1f)), dp(3f), dp(3f), paint)
        paint.color = color; canvas.drawRoundRect(RectF(barX, baseline - dp(6f), barX + barWidth * fraction.coerceIn(0f, 1f), baseline - dp(1f)), dp(3f), dp(3f), paint)
    }

    private fun drawStackedMetric(canvas: Canvas, label: String, left: Float, top: Float, right: Float, fraction: Float, color: Int) {
        drawText(canvas, label, left, top + dp(7f), dp(7f), WHITE, true)
        paint.color = Color.rgb(10, 31, 39)
        canvas.drawRoundRect(RectF(left, top + dp(12f), right, top + dp(18f)), dp(3f), dp(3f), paint)
        paint.color = color
        canvas.drawRoundRect(RectF(left, top + dp(12f), left + (right - left) * fraction.coerceIn(0f, 1f), top + dp(18f)), dp(3f), dp(3f), paint)
    }

    private fun drawScrollThumb(canvas: Canvas, viewport: RectF, offset: Float, maximum: Float) {
        val h = max(dp(24f), viewport.height() * viewport.height() / (viewport.height() + maximum)); val top = viewport.top + (viewport.height() - h) * (offset / maximum)
        paint.color = CYAN; canvas.drawRoundRect(RectF(viewport.right - dp(3f), top, viewport.right, top + h), dp(2f), dp(2f), paint)
    }

    private enum class ScrollTarget { PAGE, TRAVEL, EXPLORATION_PARTY, BATTLE_PARTY, BATTLE_ENEMIES, GUIDE }

    private fun drawGrid(canvas: Canvas) {
        paint.color = Color.rgb(4, 27, 36); paint.strokeWidth = dp(.7f); val step = dp(34f)
        var x = 0f; while (x < width) { canvas.drawLine(x, 0f, x, height.toFloat(), paint); x += step }
        var y = 0f; while (y < height) { canvas.drawLine(0f, y, width.toFloat(), y, paint); y += step }
    }

    private fun drawWrapped(canvas: Canvas, text: String, x: Float, y: Float, maxWidth: Float, size: Float, color: Int, maxLines: Int) {
        val lines = mutableListOf<String>(); var current = ""; prepareText(size, color, false, Paint.Align.LEFT)
        text.replace('\n', ' ').split(Regex("\\s+")).filter(String::isNotBlank).forEach { word ->
            val candidate = if (current.isBlank()) word else "$current $word"
            if (paint.measureText(candidate) > maxWidth && current.isNotBlank()) { lines += current; current = word } else current = candidate
        }
        if (current.isNotBlank()) lines += current
        lines.take(maxLines).forEachIndexed { index, line -> drawText(canvas, line, x, y + index * size * 1.35f, size, color) }
    }

    private fun wrappedLineCount(text: String, maxWidth: Float, size: Float): Int {
        var lines = 0
        var current = ""
        prepareText(size, WHITE, false, Paint.Align.LEFT)
        text.replace('\n', ' ').split(Regex("\\s+")).filter(String::isNotBlank).forEach { word ->
            val candidate = if (current.isBlank()) word else "$current $word"
            if (paint.measureText(candidate) > maxWidth && current.isNotBlank()) {
                lines++
                current = word
            } else {
                current = candidate
            }
        }
        if (current.isNotBlank()) lines++
        return max(1, lines)
    }

    private fun ellipsize(text: String, width: Float, size: Float): String {
        prepareText(size, WHITE, false, Paint.Align.LEFT); if (paint.measureText(text) <= width) return text
        var end = text.length; while (end > 0 && paint.measureText(text.substring(0, end) + "…") > width) end--
        return text.take(end) + "…"
    }

    private fun drawText(canvas: Canvas, text: String, x: Float, y: Float, size: Float, color: Int, bold: Boolean = false, align: Paint.Align = Paint.Align.LEFT) {
        prepareText(size, color, bold, align); canvas.drawText(text, x, y, paint)
    }

    private fun drawNameValue(
        canvas: Canvas,
        name: String,
        value: String,
        x: Float,
        y: Float,
        nameSize: Float,
        nameColor: Int,
        valueColor: Int,
        valueSize: Float = nameSize
    ) {
        drawText(canvas, name, x, y, nameSize, nameColor, true)
        prepareText(nameSize, nameColor, true, Paint.Align.LEFT)
        drawText(canvas, value, x + paint.measureText(name) + dp(5f), y, valueSize, valueColor, true)
    }

    private fun prepareText(size: Float, color: Int, bold: Boolean, align: Paint.Align) {
        paint.style = Paint.Style.FILL; paint.color = color; paint.textSize = size; paint.textAlign = align
        paint.typeface = Typeface.create(Typeface.DEFAULT, if (bold) Typeface.BOLD else Typeface.NORMAL)
    }

    private fun numberOrDash(value: Int) = if (value < 0) "—" else value.toString()
    private fun bitmap(id: Int) = BitmapFactory.decodeResource(resources, id)

    /** Removes only near-white pixels connected to an image edge, preserving white sprite details. */
    private fun transparentSprite(source: Bitmap): Bitmap {
        val width = source.width
        val height = source.height
        val pixels = IntArray(width * height)
        source.getPixels(pixels, 0, width, 0, 0, width, height)
        val visited = BooleanArray(pixels.size)
        val queue = java.util.ArrayDeque<Int>()
        fun isBackground(index: Int): Boolean {
            val color = pixels[index]
            return Color.alpha(color) > 0 && Color.red(color) >= 242 && Color.green(color) >= 242 && Color.blue(color) >= 242
        }
        fun seed(index: Int) {
            if (!visited[index] && isBackground(index)) { visited[index] = true; queue.add(index) }
        }
        for (x in 0 until width) { seed(x); seed((height - 1) * width + x) }
        for (y in 0 until height) { seed(y * width); seed(y * width + width - 1) }
        while (queue.isNotEmpty()) {
            val index = queue.removeFirst()
            pixels[index] = pixels[index] and 0x00ffffff
            val x = index % width
            val y = index / width
            if (x > 0) seed(index - 1)
            if (x + 1 < width) seed(index + 1)
            if (y > 0) seed(index - width)
            if (y + 1 < height) seed(index + width)
        }
        return Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888)
    }
    private fun ratio(value: Int, maximum: Int) = if (maximum <= 0) 0f else value.toFloat() / maximum
    private fun ratio(value: Long, maximum: Long) = if (maximum <= 0L) 0f else value.toFloat() / maximum
    private fun dp(value: Float) = value * resources.displayMetrics.density

    companion object {
        private val TABS = listOf("EXPLORACIÓN", "BATALLA", "GESTIÓN", "MODS")
        private val BACKGROUND = Color.rgb(2, 10, 16); private val PANEL = Color.rgb(4, 24, 33)
        private val PANEL_INNER = Color.rgb(6, 35, 46); private val CYAN = Color.rgb(31, 213, 242)
        private val CYAN_DARK = Color.rgb(10, 91, 112); private val WHITE = Color.rgb(228, 246, 250)
        private val MUTED = Color.rgb(111, 158, 175); private val GREEN = Color.rgb(86, 220, 118)
        private val BLUE = Color.rgb(67, 167, 238); private val AMBER = Color.rgb(238, 181, 62)
        private val RED = Color.rgb(232, 88, 88)
    }
}
