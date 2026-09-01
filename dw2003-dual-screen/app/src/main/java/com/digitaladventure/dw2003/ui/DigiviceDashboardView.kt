package com.digitaladventure.dw2003.ui

import android.annotation.SuppressLint
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
import com.digitaladventure.dw2003.R
import com.digitaladventure.dw2003.data.AreaCatalog
import com.digitaladventure.dw2003.data.CheatCatalog
import com.digitaladventure.dw2003.data.FastTravelCatalog
import com.digitaladventure.dw2003.data.LocationResolver
import com.digitaladventure.dw2003.data.MapRegionCatalog
import com.digitaladventure.dw2003.data.ServerRegion
import com.digitaladventure.dw2003.data.SectorRegion
import com.digitaladventure.dw2003.model.DigimonState
import com.digitaladventure.dw2003.model.GameMode
import com.digitaladventure.dw2003.model.GameSnapshot
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

@SuppressLint("ViewConstructor")
class DigiviceDashboardView(
    context: Context,
    private val actions: DashboardActions
) : View(context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val numberFormat = NumberFormat.getIntegerInstance(Locale("es", "MX"))
    private val hitTargets = mutableListOf<Pair<RectF, () -> Unit>>()
    private var snapshot = GameSnapshot.waiting()
    private val dashboardPreferences = context.getSharedPreferences("dw2003_dashboard", Context.MODE_PRIVATE)
    private var selectedMode = dashboardPreferences.getString("selected_mode", null)
        ?.let { stored -> GameMode.entries.firstOrNull { it.name == stored } }
        ?: GameMode.EXPLORATION
    private var selectedPartyIndex = 0
    private var fishingPreview = false
    private var travelMenuOpen = false
    private var travelScroll = 0f
    private var modsScroll = 0f
    private var selectedTab = dashboardPreferences.getString("selected_tab", null)
        ?: GameMode.EXPLORATION.name
    var controlsVisible: Boolean = true
        set(value) { field = value; invalidate() }
    var modsEnabled: Boolean = false
        set(value) {
            field = value
            if (!value && selectedTab == TAB_MODS) {
                selectedTab = GameMode.EXPLORATION.name
                selectedMode = GameMode.EXPLORATION
            }
            invalidate()
        }
    var enabledCheats: Set<String> = emptySet()
        set(value) { field = value; invalidate() }
    var visitedMaps: Set<Int> = emptySet()
        set(value) { field = value; invalidate() }
    private val tamerIdle: Bitmap by lazy { transparentSprite(R.drawable.tamer_idle) }
    private val tamerFishing: Bitmap by lazy { transparentSprite(R.drawable.tamer_fishing) }
    private val digimonSprites: Map<Int, Bitmap> by lazy {
        mapOf(
            0 to transparentSprite(R.drawable.digimon_kotemon),
            1 to transparentSprite(R.drawable.digimon_kumamon),
            2 to transparentSprite(R.drawable.digimon_monmon),
            3 to transparentSprite(R.drawable.digimon_agumon),
            4 to transparentSprite(R.drawable.digimon_veemon),
            5 to transparentSprite(R.drawable.digimon_guilmon),
            6 to transparentSprite(R.drawable.digimon_renamon),
            7 to transparentSprite(R.drawable.digimon_patamon)
        )
    }
    private val radarBitmaps: Map<Pair<ServerRegion, SectorRegion>, Bitmap> by lazy {
        mapOf(
            (ServerRegion.ASUKA to SectorRegion.UNKNOWN) to bitmap(R.drawable.map_asuka_overview),
            (ServerRegion.ASUKA to SectorRegion.CENTRAL) to bitmap(R.drawable.map_asuka_central),
            (ServerRegion.ASUKA to SectorRegion.EAST) to bitmap(R.drawable.map_asuka_east),
            (ServerRegion.ASUKA to SectorRegion.SOUTH) to bitmap(R.drawable.map_asuka_south),
            (ServerRegion.ASUKA to SectorRegion.WEST) to bitmap(R.drawable.map_asuka_west),
            (ServerRegion.ASUKA to SectorRegion.NORTH) to bitmap(R.drawable.map_asuka_north),
            (ServerRegion.AMATERASU to SectorRegion.UNKNOWN) to bitmap(R.drawable.map_amaterasu_overview),
            (ServerRegion.AMATERASU to SectorRegion.CENTRAL) to bitmap(R.drawable.map_amaterasu_central),
            (ServerRegion.AMATERASU to SectorRegion.EAST) to bitmap(R.drawable.map_amaterasu_east),
            (ServerRegion.AMATERASU to SectorRegion.SOUTH) to bitmap(R.drawable.map_amaterasu_south),
            (ServerRegion.AMATERASU to SectorRegion.WEST) to bitmap(R.drawable.map_amaterasu_west),
            (ServerRegion.AMATERASU to SectorRegion.NORTH) to bitmap(R.drawable.map_amaterasu_north)
        )
    }

    init {
        isClickable = true
        isFocusable = true
        contentDescription = "Panel complementario de Digimon World 2003"
    }

    fun submitSnapshot(value: GameSnapshot) {
        val selectedProfile = snapshot.party.getOrNull(selectedPartyIndex)?.profileId
        snapshot = value
        if (!value.fishingAvailable) fishingPreview = false
        selectedPartyIndex = selectedProfile
            ?.let { profile -> value.party.indexOfFirst { it.profileId == profile }.takeIf { it >= 0 } }
            ?: selectedPartyIndex.coerceIn(0, max(0, value.party.lastIndex))
        invalidate()
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        hitTargets.clear()
        canvas.drawColor(BACKGROUND)
        drawGrid(canvas)

        val margin = dp(12f)
        val headerBottom = dp(68f)
        val tabTop = height - dp(52f)
        drawHeader(canvas, margin, headerBottom)

        val content = RectF(margin, headerBottom + dp(6f), width - margin, tabTop - dp(7f))
        if (travelMenuOpen) {
            drawFastTravelMenu(canvas, content)
        } else if (!snapshot.gameStarted && selectedTab != TAB_MODS) {
            drawWaitingForGame(canvas, content)
        } else {
            when (selectedTab) {
                TAB_MODS -> drawMods(canvas, content)
                GameMode.BATTLE.name -> drawBattle(canvas, content)
                GameMode.MANAGEMENT.name -> drawManagement(canvas, content)
                else -> drawExploration(canvas, content)
            }
        }
        drawTabs(canvas, tabTop)
    }

    private fun drawHeader(canvas: Canvas, margin: Float, bottom: Float) {
        drawText(canvas, snapshot.locationTitle.uppercase(), margin, dp(27f), dp(17f), WHITE, true)
        drawText(canvas, "${snapshot.locationDetail.uppercase()} · HISTORIA ${snapshot.storyStage}", margin, dp(49f), dp(9f), MUTED)
        val status = when {
            !snapshot.gameStarted -> "○ ESPERANDO PARTIDA"
            snapshot.isLive -> "● RAM EN VIVO"
            else -> "◇ DEMO"
        }
        val appButton = RectF(width - margin - dp(58f), dp(8f), width - margin, dp(35f))
        val padButton = RectF(appButton.left - dp(64f), dp(8f), appButton.left - dp(6f), dp(35f))
        paint.color = Color.rgb(8, 43, 56)
        canvas.drawRoundRect(appButton, dp(6f), dp(6f), paint)
        canvas.drawRoundRect(padButton, dp(6f), dp(6f), paint)
        drawText(canvas, "⚙ APP", appButton.centerX(), appButton.centerY() + dp(4f), dp(9f), CYAN, true, Paint.Align.CENTER)
        drawText(canvas, if (controlsVisible) "PAD ON" else "PAD OFF", padButton.centerX(), padButton.centerY() + dp(4f), dp(8f), if (controlsVisible) CYAN else MUTED, true, Paint.Align.CENTER)
        hitTargets += appButton to actions.onAppSettings
        hitTargets += padButton to actions.onToggleControls
        val statusColor = if (snapshot.gameStarted && snapshot.isLive) GREEN else AMBER
        drawText(canvas, status, padButton.left - dp(8f), dp(27f), dp(10f), statusColor, true, Paint.Align.RIGHT)
        val tabLabel = if (selectedTab == TAB_MODS) "Mods" else selectedMode.label
        drawText(canvas, tabLabel.uppercase(), width - margin, dp(49f), dp(10f), CYAN, true, Paint.Align.RIGHT)
        paint.color = CYAN_DARK
        canvas.drawRect(margin, bottom - dp(2f), width - margin, bottom, paint)
    }

    private fun drawWaitingForGame(canvas: Canvas, bounds: RectF) {
        drawPanel(canvas, bounds, "SESIÓN DE JUEGO")
        drawText(canvas, "PARTIDA AÚN NO INICIADA", bounds.centerX(), bounds.centerY() - dp(16f), dp(18f), WHITE, true, Paint.Align.CENTER)
        drawWrapped(
            canvas,
            "Completa la introducción o carga una partida. El área, el objetivo y la formación aparecerán cuando la RAM contenga una sesión válida.",
            bounds.left + dp(24f),
            bounds.centerY() + dp(15f),
            bounds.width() - dp(48f),
            dp(11f),
            MUTED,
            4
        )
    }

    private fun drawExploration(canvas: Canvas, bounds: RectF) {
        val wide = bounds.width() > dp(620f)
        if (wide) {
            val left = RectF(bounds.left, bounds.top, bounds.left + bounds.width() * .48f, bounds.bottom)
            val right = RectF(left.right + dp(8f), bounds.top, bounds.right, bounds.bottom)
            drawRadar(canvas, left)
            drawObjectiveAndTamer(canvas, right)
        } else {
            val radar = RectF(bounds.left, bounds.top, bounds.right, bounds.top + bounds.height() * .48f)
            drawRadar(canvas, radar)
            drawObjectiveAndTamer(canvas, RectF(bounds.left, radar.bottom + dp(7f), bounds.right, bounds.bottom))
        }
    }

    private fun drawRadar(canvas: Canvas, bounds: RectF) {
        val location = LocationResolver.resolve(snapshot.areaId, snapshot.mapId)
        val mapRegion = MapRegionCatalog.resolve(location.publicMapId)
        val region = if (mapRegion.server == ServerRegion.UNKNOWN) MapRegionCatalog.resolve(snapshot.areaId) else mapRegion
        drawPanel(canvas, bounds, "RADAR REGIONAL · ${snapshot.serverName.uppercase()}")
        val map = RectF(bounds.left + dp(13f), bounds.top + dp(31f), bounds.right - dp(13f), bounds.bottom - dp(25f))
        val radar = radarBitmaps[region.server to region.sector]
            ?: radarBitmaps[region.server to SectorRegion.UNKNOWN]
        if (radar != null) {
            paint.isFilterBitmap = true
            canvas.drawBitmap(radar, null, map, paint)
            paint.color = Color.argb(55, 0, 20, 28)
            canvas.drawRect(map, paint)
        }
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp(2f)
        paint.color = CYAN
        canvas.drawCircle(map.centerX(), map.centerY(), dp(7f), paint)
        canvas.drawLine(map.centerX() - dp(12f), map.centerY(), map.centerX() + dp(12f), map.centerY(), paint)
        canvas.drawLine(map.centerX(), map.centerY() - dp(12f), map.centerX(), map.centerY() + dp(12f), paint)
        paint.style = Paint.Style.FILL
        drawText(canvas, snapshot.radarLabel.uppercase(), bounds.centerX(), bounds.bottom - dp(8f), dp(7.5f), MUTED, true, Paint.Align.CENTER)
        if (snapshot.gameStarted) {
            drawText(canvas, "TOCA EL MAPA PARA VIAJE RÁPIDO", bounds.centerX(), bounds.top + dp(28f), dp(7f), CYAN, true, Paint.Align.CENTER)
            hitTargets += map to {
                travelMenuOpen = true
                travelScroll = 0f
                invalidate()
            }
        }
    }

    private fun drawObjectiveAndTamer(canvas: Canvas, bounds: RectF) {
        val objectiveHeight = min(bounds.height() * .38f, dp(118f))
        val objective = RectF(bounds.left, bounds.top, bounds.right, bounds.top + objectiveHeight)
        drawPanel(canvas, objective, "OBJETIVO ACTUAL")
        drawWrapped(canvas, snapshot.objective, objective.left + dp(12f), objective.top + dp(38f), objective.width() - dp(24f), dp(11f), WHITE, 5)

        val tamerBounds = RectF(bounds.left, objective.bottom + dp(7f), bounds.right, bounds.bottom)
        drawPanel(canvas, tamerBounds, "ESTADO DEL TAMER")
        val showFishing = snapshot.isFishing || fishingPreview
        val sprite = if (showFishing) tamerFishing else tamerIdle
        val spriteBounds = RectF(
            tamerBounds.left + dp(16f),
            tamerBounds.top + dp(34f),
            tamerBounds.left + min(tamerBounds.width() * .37f, dp(112f)),
            tamerBounds.bottom - dp(15f)
        )
        drawPixelBitmap(canvas, sprite, spriteBounds, dp(92f))
        val textX = max(spriteBounds.right + dp(13f), tamerBounds.left + tamerBounds.width() * .43f)
        drawText(canvas, "TAMER · ${snapshot.tamerName.uppercase()}", textX, tamerBounds.top + dp(47f), dp(14f), WHITE, true)
        val activity = when {
            snapshot.isFishing -> "PESCANDO"
            fishingPreview -> "VISTA DE PESCA"
            else -> "EXPLORANDO"
        }
        drawText(canvas, "ACTIVIDAD  $activity", textX, tamerBounds.top + dp(69f), dp(9f), CYAN, true)
        drawText(canvas, "BITS", textX, tamerBounds.top + dp(96f), dp(8f), MUTED, true)
        drawText(canvas, formatNumber(snapshot.bits), textX, tamerBounds.top + dp(119f), dp(18f), AMBER, true)
        val fishingText = if (snapshot.fishingAvailable) "PESCA DISPONIBLE · TOCA PARA PREVISUALIZAR" else "SIN PUNTO DE PESCA EN ESTA ÁREA"
        drawWrapped(canvas, fishingText, textX, tamerBounds.top + dp(143f), tamerBounds.right - textX - dp(12f), dp(7.5f), MUTED, 2)
        if (snapshot.fishingAvailable) {
            hitTargets += tamerBounds to {
                fishingPreview = !fishingPreview
                invalidate()
            }
        }
    }

    private fun drawBattle(canvas: Canvas, bounds: RectF) {
        drawPanel(canvas, bounds, "TELEMETRÍA DE BATALLA")
        val party = snapshot.party.take(3)
        val horizontal = bounds.width() > dp(560f)
        party.forEachIndexed { index, digimon ->
            val card = if (horizontal) {
                val gap = dp(7f)
                val cardW = (bounds.width() - dp(20f) - gap * 2) / 3f
                RectF(bounds.left + dp(10f) + index * (cardW + gap), bounds.top + dp(31f), bounds.left + dp(10f) + index * (cardW + gap) + cardW, bounds.bottom - dp(24f))
            } else {
                val cardH = (bounds.height() - dp(40f)) / max(1, party.size)
                RectF(bounds.left + dp(9f), bounds.top + dp(30f) + index * cardH, bounds.right - dp(9f), bounds.top + dp(30f) + (index + 1) * cardH - dp(5f))
            }
            drawDigimonCard(canvas, card, digimon, compact = !horizontal, partyIndex = index)
        }
        val note = if (snapshot.canReorderParty) {
            "TOCA ▲▼ PARA CAMBIAR EL ORDEN DE SALIDA"
        } else {
            "REORDENAR SOLO FUERA DE BATALLA O EVENTOS"
        }
        drawText(canvas, note, bounds.centerX(), bounds.bottom - dp(12f), dp(7.5f), if (snapshot.canReorderParty) CYAN else MUTED, true, Paint.Align.CENTER)
    }

    private fun drawManagement(canvas: Canvas, bounds: RectF) {
        val selected = snapshot.party.getOrNull(selectedPartyIndex) ?: return
        val wide = bounds.width() > dp(570f)
        if (wide) {
            val identity = RectF(bounds.left, bounds.top, bounds.left + bounds.width() * .26f, bounds.bottom)
            val details = RectF(identity.right + dp(7f), bounds.top, bounds.right, bounds.bottom)
            val parameters = RectF(details.left, details.top, details.left + details.width() * .30f, details.bottom)
            val elements = RectF(parameters.right + dp(7f), details.top, parameters.right + dp(7f) + details.width() * .30f, details.bottom)
            val equipment = RectF(elements.right + dp(7f), details.top, details.right, details.bottom)
            drawIdentity(canvas, identity, selected, vertical = true)
            drawParameters(canvas, parameters, selected)
            drawElementsAndSkills(canvas, elements, selected)
            drawEquipment(canvas, equipment, selected)
        } else {
            val identityHeight = min(dp(210f), max(dp(200f), bounds.height() * .34f))
            val identity = RectF(bounds.left, bounds.top, bounds.right, bounds.top + identityHeight)
            val details = RectF(bounds.left, identity.bottom + dp(7f), bounds.right, bounds.bottom)
            drawIdentity(canvas, identity, selected, vertical = false)
            if (details.width() > dp(350f)) {
                val parameters = RectF(details.left, details.top, details.left + details.width() * .43f, details.bottom)
                val right = RectF(parameters.right + dp(7f), details.top, details.right, details.bottom)
                val elements = RectF(right.left, right.top, right.right, right.top + right.height() * .47f)
                drawParameters(canvas, parameters, selected)
                drawElementsAndSkills(canvas, elements, selected)
                drawEquipment(canvas, RectF(right.left, elements.bottom + dp(7f), right.right, right.bottom), selected)
            } else {
                val parameters = RectF(details.left, details.top, details.right, details.top + details.height() * .29f)
                val elements = RectF(details.left, parameters.bottom + dp(7f), details.right, parameters.bottom + dp(7f) + details.height() * .31f)
                drawParameters(canvas, parameters, selected)
                drawElementsAndSkills(canvas, elements, selected)
                drawEquipment(canvas, RectF(details.left, elements.bottom + dp(7f), details.right, details.bottom), selected)
            }
        }
    }

    private fun drawPartyRow(canvas: Canvas, digimon: DigimonState, x: Float, y: Float, width: Float, height: Float) {
        paint.color = PANEL_INNER
        canvas.drawRoundRect(RectF(x, y, x + width, y + height), dp(5f), dp(5f), paint)
        val left = x + dp(8f)
        drawText(canvas, digimon.name.uppercase(), left, y + dp(13f), dp(9f), WHITE, true)
        drawText(canvas, "LV ${digimon.level}", x + width - dp(8f), y + dp(13f), dp(8f), CYAN, true, Paint.Align.RIGHT)
        val labelWidth = min(dp(126f), width * .43f)
        val barX = left + labelWidth
        val barW = max(dp(24f), width - labelWidth - dp(16f))
        drawMetricLine(canvas, "HP ${digimon.currentHp}/${digimon.maxHp}", left, y + dp(25f), barX, barW, digimon.hpFraction, GREEN)
        drawMetricLine(canvas, "MP ${digimon.currentMp}/${digimon.maxMp}", left, y + dp(37f), barX, barW, digimon.mpFraction, BLUE)
        val next = digimon.nextLevelExperience
        val expLabel = if (next == null) "EXP ${digimon.experience} · MÁX." else "EXP ${digimon.experience}/$next"
        drawMetricLine(canvas, expLabel, left, y + dp(49f), barX, barW, digimon.experienceFraction, AMBER)
    }

    private fun drawDigimonCard(canvas: Canvas, bounds: RectF, digimon: DigimonState, compact: Boolean, partyIndex: Int = -1) {
        paint.color = PANEL_INNER
        canvas.drawRoundRect(bounds, dp(7f), dp(7f), paint)
        val badge = RectF(bounds.left + dp(9f), bounds.top + dp(10f), bounds.left + dp(49f), bounds.top + dp(50f))
        paint.color = CYAN_DARK
        canvas.drawRoundRect(badge, dp(7f), dp(7f), paint)
        digimonSprites[digimon.profileId]?.let { drawPixelBitmap(canvas, it, RectF(badge.left + dp(4f), badge.top + dp(4f), badge.right - dp(4f), badge.bottom - dp(4f)), dp(34f)) }
        drawText(canvas, digimon.name.uppercase(), badge.right + dp(8f), bounds.top + dp(24f), if (compact) dp(13f) else dp(12f), WHITE, true)
        drawText(canvas, "LV ${digimon.level} · TP ${digimon.trainingPoints}", badge.right + dp(8f), bounds.top + dp(43f), dp(9f), MUTED, true)
        if (partyIndex >= 0 && snapshot.party.size > 1) {
            val up = RectF(bounds.right - dp(36f), bounds.top + dp(8f), bounds.right - dp(8f), bounds.top + dp(32f))
            val down = RectF(bounds.right - dp(36f), bounds.top + dp(34f), bounds.right - dp(8f), bounds.top + dp(58f))
            val enabled = snapshot.canReorderParty
            paint.color = if (enabled) CYAN_DARK else Color.rgb(8, 28, 36)
            canvas.drawRoundRect(up, dp(5f), dp(5f), paint)
            canvas.drawRoundRect(down, dp(5f), dp(5f), paint)
            drawText(canvas, "▲", up.centerX(), up.centerY() + dp(5f), dp(12f), if (enabled && partyIndex > 0) WHITE else MUTED, true, Paint.Align.CENTER)
            drawText(canvas, "▼", down.centerX(), down.centerY() + dp(5f), dp(12f), if (enabled && partyIndex < snapshot.party.lastIndex) WHITE else MUTED, true, Paint.Align.CENTER)
            if (enabled && partyIndex > 0) {
                hitTargets += up to { actions.onPartyMove(partyIndex, partyIndex - 1) }
            }
            if (enabled && partyIndex < snapshot.party.lastIndex) {
                hitTargets += down to { actions.onPartyMove(partyIndex, partyIndex + 1) }
            }
        }
        val x = bounds.left + dp(9f)
        val w = bounds.width() - dp(18f)
        drawBar(canvas, x, bounds.top + dp(61f), w, dp(8f), digimon.hpFraction, GREEN)
        drawText(canvas, "HP ${digimon.currentHp} / ${digimon.maxHp}", x, bounds.top + dp(82f), dp(9f), WHITE)
        drawBar(canvas, x, bounds.top + dp(91f), w, dp(7f), digimon.mpFraction, BLUE)
        if (bounds.height() > dp(125f)) drawText(canvas, "MP ${digimon.currentMp} / ${digimon.maxMp}", x, bounds.top + dp(112f), dp(9f), WHITE)
        if (bounds.height() > dp(145f)) {
            drawBar(canvas, x, bounds.top + dp(121f), w, dp(7f), digimon.experienceFraction, AMBER)
            val next = digimon.nextLevelExperience
            val exp = if (next == null) "EXP ${digimon.experience} · NIVEL MÁX." else "EXP ${digimon.experience} / $next · FALTAN ${digimon.experienceRemaining}"
            drawText(canvas, exp, x, bounds.top + dp(143f), dp(8f), WHITE)
        }
        if (bounds.height() > dp(165f)) {
            drawText(canvas, "DIGIEVOLUCIÓN · ${digimon.activeDigievolutionName.uppercase()}", x, bounds.top + dp(161f), dp(7.5f), CYAN, true)
        }
    }

    private fun drawIdentity(canvas: Canvas, bounds: RectF, digimon: DigimonState, vertical: Boolean) {
        drawPanel(canvas, bounds, "PARTNER / ESTADO  ·  ${selectedPartyIndex + 1}/${snapshot.party.size}")
        val arrowSize = dp(34f)
        val arrowY = bounds.top + dp(29f)
        val leftArrow = RectF(bounds.left + dp(8f), arrowY, bounds.left + dp(8f) + arrowSize, arrowY + arrowSize)
        val rightArrow = RectF(bounds.right - dp(8f) - arrowSize, arrowY, bounds.right - dp(8f), arrowY + arrowSize)
        paint.color = PANEL_INNER
        canvas.drawRoundRect(leftArrow, dp(5f), dp(5f), paint)
        canvas.drawRoundRect(rightArrow, dp(5f), dp(5f), paint)
        drawText(canvas, "‹", leftArrow.centerX(), leftArrow.centerY() + dp(7f), dp(22f), CYAN, true, Paint.Align.CENTER)
        drawText(canvas, "›", rightArrow.centerX(), rightArrow.centerY() + dp(7f), dp(22f), CYAN, true, Paint.Align.CENTER)
        hitTargets += leftArrow to { selectParty(-1) }
        hitTargets += rightArrow to { selectParty(1) }
        val portrait = RectF(bounds.centerX() - dp(18f), bounds.top + dp(28f), bounds.centerX() + dp(18f), bounds.top + dp(64f))
        digimonSprites[digimon.profileId]?.let { drawPixelBitmap(canvas, it, portrait, dp(34f)) }
        drawText(canvas, digimon.name.uppercase(), bounds.centerX(), bounds.top + dp(80f), if (vertical) dp(15f) else dp(17f), WHITE, true, Paint.Align.CENTER)
        drawText(canvas, "NIVEL ${digimon.level}  ·  TP ${digimon.trainingPoints}", bounds.centerX(), bounds.top + dp(96f), dp(8f), CYAN, true, Paint.Align.CENTER)
        val x = bounds.left + dp(12f)
        val w = bounds.width() - dp(24f)
        drawLabeledBar(canvas, "HP ${digimon.currentHp}/${digimon.maxHp}", x, bounds.top + dp(107f), w, digimon.hpFraction, GREEN)
        drawLabeledBar(canvas, "MP ${digimon.currentMp}/${digimon.maxMp}", x, bounds.top + dp(129f), w, digimon.mpFraction, BLUE)
        val next = digimon.nextLevelExperience
        val exp = if (next == null) "EXP ${digimon.experience} · NIVEL MÁX." else "EXP ${digimon.experience}/$next · FALTAN ${digimon.experienceRemaining}"
        drawLabeledBar(canvas, exp, x, bounds.top + dp(151f), w, digimon.experienceFraction, AMBER)
        drawText(canvas, "DIGIEVOLUCIÓN · ${digimon.activeDigievolutionName.uppercase()}  NV ${digimon.activeDigievolutionLevel}", x, bounds.top + dp(181f), dp(7.5f), CYAN, true)
    }

    private fun drawParameters(canvas: Canvas, bounds: RectF, digimon: DigimonState) {
        drawPanel(canvas, bounds, "PARÁMETROS EN RAM")
        val values = listOf("FUERZA" to digimon.strength, "DEFENSA" to digimon.defense, "ESPÍRITU" to digimon.spirit, "SABIDURÍA" to digimon.wisdom, "VELOCIDAD" to digimon.speed, "CARISMA" to digimon.charisma)
        val columns = if (bounds.width() > dp(300f)) 3 else 2
        val rows = (values.size + columns - 1) / columns
        val cellW = (bounds.width() - dp(20f)) / columns
        val cellH = (bounds.height() - dp(35f)) / rows
        values.forEachIndexed { index, value ->
            val x = bounds.left + dp(10f) + (index % columns) * cellW
            val y = bounds.top + dp(42f) + (index / columns) * cellH
            drawText(canvas, value.first, x, y, dp(8f), MUTED, true)
            drawText(canvas, value.second.toString(), x, y + dp(21f), dp(17f), WHITE, true)
        }
    }

    private fun drawElementsAndSkills(canvas: Canvas, bounds: RectF, digimon: DigimonState) {
        drawPanel(canvas, bounds, "RESISTENCIAS ELEMENTALES")
        val names = listOf("FUEGO", "AGUA", "HIELO", "VIENTO", "RAYO", "MÁQUINA", "OSCURIDAD")
        val values = names.zip(digimon.tolerances + List(max(0, names.size - digimon.tolerances.size)) { 0 })
        val columns = if (bounds.width() > dp(310f)) 4 else 2
        val rows = (values.size + columns - 1) / columns
        val skillHeight = dp(56f)
        val gridHeight = max(dp(80f), bounds.height() - dp(35f) - skillHeight)
        val cellW = (bounds.width() - dp(20f)) / columns
        val cellH = gridHeight / rows
        values.forEachIndexed { index, value ->
            val x = bounds.left + dp(10f) + (index % columns) * cellW
            val y = bounds.top + dp(39f) + (index / columns) * cellH
            drawText(canvas, value.first, x, y, dp(7.5f), MUTED, true)
            drawText(canvas, value.second.toString(), x, y + dp(18f), dp(14f), WHITE, true)
        }
        val skillTop = bounds.bottom - skillHeight
        paint.color = CYAN_DARK
        canvas.drawRect(bounds.left + dp(10f), skillTop, bounds.right - dp(10f), skillTop + dp(1f), paint)
        drawText(canvas, "HABILIDADES · ${digimon.activeDigievolutionName.uppercase()}", bounds.left + dp(10f), skillTop + dp(18f), dp(7.5f), CYAN, true)
        val skills = digimon.activeSkills
        val text = if (skills.isEmpty()) "Sin técnicas identificadas" else skills.joinToString("  ·  ") { skill ->
            "${skill.name} · MP ${skill.mp?.toString() ?: "—"}"
        }
        drawWrapped(canvas, text, bounds.left + dp(10f), skillTop + dp(39f), bounds.width() - dp(20f), dp(8f), WHITE, 2)
    }

    private fun drawEquipment(canvas: Canvas, bounds: RectF, digimon: DigimonState) {
        drawPanel(canvas, bounds, "EQUIPO Y BONIFICACIONES")
        val slots = listOf("CABEZA", "CUERPO", "MANO DER.", "MANO IZQ.", "ACCESORIO 1", "ACCESORIO 2")
        val rowHeight = (bounds.height() - dp(30f)) / slots.size
        slots.forEachIndexed { index, slot ->
            val top = bounds.top + dp(27f) + index * rowHeight
            val info = digimon.equippedItems.getOrNull(index)
            if (index > 0) {
                paint.color = Color.rgb(8, 49, 61)
                canvas.drawRect(bounds.left + dp(9f), top - dp(2f), bounds.right - dp(9f), top - dp(1f), paint)
            }
            drawText(canvas, slot, bounds.left + dp(10f), top + dp(9f), dp(6.5f), MUTED, true)
            val name = info?.name ?: "— VACÍO —"
            drawText(canvas, name, bounds.left + dp(10f), top + dp(21f), dp(8f), WHITE, true)
            if (info != null && rowHeight > dp(31f)) {
                drawText(canvas, info.stats, bounds.left + dp(10f), top + dp(32f), dp(6.5f), CYAN)
            }
        }
    }

    private fun drawMetricLine(canvas: Canvas, label: String, labelX: Float, baseline: Float, barX: Float, barWidth: Float, fraction: Float, color: Int) {
        drawText(canvas, label, labelX, baseline, dp(7.5f), WHITE, true)
        drawBar(canvas, barX, baseline - dp(6f), barWidth, dp(5f), fraction, color)
    }

    private fun drawLabeledBar(canvas: Canvas, label: String, x: Float, y: Float, width: Float, fraction: Float, color: Int) {
        drawText(canvas, label, x, y, dp(8f), WHITE, true)
        drawBar(canvas, x, y + dp(5f), width, dp(7f), fraction, color)
    }

    private fun selectParty(delta: Int) {
        if (snapshot.party.isEmpty()) return
        selectedPartyIndex = (selectedPartyIndex + delta + snapshot.party.size) % snapshot.party.size
        invalidate()
    }

    private fun drawTabs(canvas: Canvas, top: Float) {
        val tabs = visibleTabs()
        val itemWidth = width / tabs.size.toFloat()
        tabs.forEachIndexed { index, tab ->
            val rect = RectF(index * itemWidth, top, (index + 1) * itemWidth, height.toFloat())
            val selected = tab.first == selectedTab
            paint.color = if (selected) Color.rgb(9, 65, 82) else Color.rgb(3, 21, 29)
            canvas.drawRect(rect, paint)
            if (selected) {
                paint.color = CYAN
                canvas.drawRect(rect.left, rect.top, rect.right, rect.top + dp(3f), paint)
            }
            drawText(canvas, tab.second.uppercase(), rect.centerX(), rect.centerY() + dp(5f), dp(10f), if (selected) WHITE else MUTED, true, Paint.Align.CENTER)
            hitTargets += rect to {
                travelMenuOpen = false
                selectedTab = tab.first
                GameMode.entries.firstOrNull { it.name == tab.first }?.let { selectedMode = it }
                dashboardPreferences.edit()
                    .putString("selected_tab", tab.first)
                    .putString("selected_mode", selectedMode.name)
                    .apply()
                invalidate()
            }
        }
    }

    private fun visibleTabs(): List<Pair<String, String>> {
        val tabs = GameMode.entries.map { it.name to it.label }.toMutableList()
        if (modsEnabled) tabs += TAB_MODS to "Mods"
        return tabs
    }

    private fun drawFastTravelMenu(canvas: Canvas, bounds: RectF) {
        drawPanel(canvas, bounds, "VIAJE RÁPIDO · DESTINOS VISITADOS")
        val close = RectF(bounds.right - dp(78f), bounds.top + dp(6f), bounds.right - dp(10f), bounds.top + dp(28f))
        paint.color = PANEL_INNER
        canvas.drawRoundRect(close, dp(5f), dp(5f), paint)
        drawText(canvas, "CERRAR", close.centerX(), close.centerY() + dp(4f), dp(8f), WHITE, true, Paint.Align.CENTER)
        hitTargets += close to {
            travelMenuOpen = false
            invalidate()
        }
        val openMap = RectF(bounds.left + dp(10f), bounds.top + dp(34f), bounds.right - dp(10f), bounds.top + dp(66f))
        paint.color = CYAN_DARK
        canvas.drawRoundRect(openMap, dp(5f), dp(5f), paint)
            drawText(canvas, "ABRIR PESTAÑA MAPA", openMap.centerX(), openMap.centerY() + dp(4f), dp(10f), WHITE, true, Paint.Align.CENTER)
        hitTargets += openMap to {
            travelMenuOpen = false
            actions.onOpenGameMap()
            invalidate()
        }
        val groups = FastTravelCatalog.groups(snapshot.storyStage, visitedMaps + snapshot.areaId, snapshot.areaId)
        val listTop = openMap.bottom + dp(6f)
        if (!snapshot.canFastTravel) {
            drawWrapped(
                canvas,
                "El traslado desde esta lista solo está disponible fuera de batalla, menús de evento y pantallas de guardado. Abrir pestaña Mapa usa START y lleva a MAPA desde la pestaña actual.",
                bounds.left + dp(16f),
                listTop + dp(4f),
                bounds.width() - dp(32f),
                dp(10f),
                AMBER,
                3
            )
        } else if (groups.isEmpty()) {
            drawWrapped(
                canvas,
                "Todavía no hay destinos visitados. Explora el campo para añadir localidades, o abre el mapa del juego para el viaje de Flawe's Mod.",
                bounds.left + dp(16f),
                listTop + dp(4f),
                bounds.width() - dp(32f),
                dp(10f),
                MUTED,
                4
            )
        }
        val headerOffset = when {
            !snapshot.canFastTravel -> dp(58f)
            groups.isEmpty() -> dp(58f)
            else -> 0f
        }
        var y = listTop + headerOffset - travelScroll
        val rowHeight = dp(28f)
        groups.forEach { group ->
            if (y >= listTop && y < bounds.bottom - dp(8f)) {
                drawText(
                    canvas,
                    "${group.server.label.uppercase()} · ${group.sector.label.uppercase()}",
                    bounds.left + dp(12f),
                    y + dp(16f),
                    dp(8f),
                    CYAN,
                    true
                )
            }
            y += dp(22f)
            group.destinations.forEach { destination ->
                val row = RectF(bounds.left + dp(10f), y, bounds.right - dp(10f), y + rowHeight)
                if (row.top >= listTop && row.top < bounds.bottom - dp(8f)) {
                    paint.color = if (destination.areaId == snapshot.areaId) CYAN_DARK else PANEL_INNER
                    canvas.drawRoundRect(row, dp(5f), dp(5f), paint)
                    drawText(canvas, destination.name.uppercase(), row.left + dp(10f), row.centerY() + dp(4f), dp(10f), WHITE, true)
                    drawText(canvas, "0x${AreaCatalog.hex(destination.areaId)}", row.right - dp(10f), row.centerY() + dp(4f), dp(8f), MUTED, true, Paint.Align.RIGHT)
                    if (snapshot.canFastTravel && destination.areaId != snapshot.areaId) {
                        hitTargets += row to {
                            travelMenuOpen = false
                            actions.onFastTravel(destination.areaId)
                            invalidate()
                        }
                    }
                }
                y += rowHeight + dp(4f)
            }
        }
        val maxScroll = max(0f, y + travelScroll - bounds.bottom + dp(8f))
        travelScroll = travelScroll.coerceIn(0f, maxScroll)
    }

    private fun drawMods(canvas: Canvas, bounds: RectF) {
        drawPanel(canvas, bounds, "MODS Y CÓDIGOS PAL")
        drawWrapped(
            canvas,
            "Los códigos se aplican sobre la RAM emulada de SLES-03936 / Flawe's Mod. Úsalos fuera de secuencias críticas.",
            bounds.left + dp(12f),
            bounds.top + dp(34f),
            bounds.width() - dp(24f),
            dp(9f),
            MUTED,
            2
        )
        var y = bounds.top + dp(72f) - modsScroll
        CheatCatalog.all.forEach { cheat ->
            val row = RectF(bounds.left + dp(10f), y, bounds.right - dp(10f), y + dp(52f))
            if (row.bottom > bounds.top + dp(64f) && row.top < bounds.bottom - dp(8f)) {
                paint.color = PANEL_INNER
                canvas.drawRoundRect(row, dp(6f), dp(6f), paint)
                val enabled = cheat.id in enabledCheats
                val toggle = RectF(row.right - dp(72f), row.top + dp(12f), row.right - dp(10f), row.bottom - dp(12f))
                paint.color = if (enabled) CYAN else CYAN_DARK
                canvas.drawRoundRect(toggle, dp(5f), dp(5f), paint)
                drawText(canvas, if (enabled) "ON" else "OFF", toggle.centerX(), toggle.centerY() + dp(4f), dp(10f), if (enabled) Color.rgb(2, 16, 22) else WHITE, true, Paint.Align.CENTER)
                drawText(canvas, cheat.label.uppercase(), row.left + dp(10f), row.top + dp(18f), dp(11f), WHITE, true)
                drawText(canvas, cheat.detail, row.left + dp(10f), row.top + dp(36f), dp(8f), MUTED)
                hitTargets += toggle to { actions.onCheatToggle(cheat.id, !enabled) }
            }
            y += dp(58f)
        }
        val maxScroll = max(0f, y + modsScroll - bounds.bottom + dp(8f))
        modsScroll = modsScroll.coerceIn(0f, maxScroll)
    }

    private fun bitmap(resourceId: Int): Bitmap = BitmapFactory.decodeResource(resources, resourceId)

    private fun transparentSprite(resourceId: Int): Bitmap {
        val source = bitmap(resourceId).copy(Bitmap.Config.ARGB_8888, true)
        val pixels = IntArray(source.width * source.height)
        source.getPixels(pixels, 0, source.width, 0, 0, source.width, source.height)
        val masked = TransparencyMask.clearEdgeConnectedWhite(pixels, source.width, source.height)
        source.setPixels(masked, 0, source.width, 0, 0, source.width, source.height)
        return source
    }

    private fun drawPanel(canvas: Canvas, bounds: RectF, title: String) {
        paint.style = Paint.Style.FILL
        paint.color = PANEL
        canvas.drawRoundRect(bounds, dp(7f), dp(7f), paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp(1f)
        paint.color = CYAN_DARK
        canvas.drawRoundRect(bounds, dp(7f), dp(7f), paint)
        paint.style = Paint.Style.FILL
        drawText(canvas, title, bounds.left + dp(10f), bounds.top + dp(19f), dp(9f), CYAN, true)
    }

    private fun drawBar(canvas: Canvas, x: Float, y: Float, width: Float, height: Float, fraction: Float, color: Int) {
        paint.color = Color.rgb(10, 31, 39)
        canvas.drawRoundRect(RectF(x, y, x + width, y + height), height / 2, height / 2, paint)
        paint.color = color
        canvas.drawRoundRect(RectF(x, y, x + width * fraction.coerceIn(0f, 1f), y + height), height / 2, height / 2, paint)
    }

    private fun drawPixelBitmap(canvas: Canvas, bitmap: Bitmap, bounds: RectF, maxSize: Float) {
        val scale = min(min(bounds.width() / bitmap.width, bounds.height() / bitmap.height), maxSize / max(bitmap.width, bitmap.height))
        val drawWidth = bitmap.width * scale
        val drawHeight = bitmap.height * scale
        val destination = RectF(
            bounds.centerX() - drawWidth / 2f,
            bounds.centerY() - drawHeight / 2f,
            bounds.centerX() + drawWidth / 2f,
            bounds.centerY() + drawHeight / 2f
        )
        paint.isFilterBitmap = false
        canvas.drawBitmap(bitmap, null, destination, paint)
        paint.isFilterBitmap = true
    }

    private fun formatNumber(value: Long): String = numberFormat.format(value)

    private fun drawGrid(canvas: Canvas) {
        paint.color = Color.rgb(4, 27, 36)
        paint.strokeWidth = dp(.7f)
        val step = dp(34f)
        var x = 0f
        while (x < width) { canvas.drawLine(x, 0f, x, height.toFloat(), paint); x += step }
        var y = 0f
        while (y < height) { canvas.drawLine(0f, y, width.toFloat(), y, paint); y += step }
    }

    private fun drawText(canvas: Canvas, text: String, x: Float, y: Float, size: Float, color: Int, bold: Boolean = false, align: Paint.Align = Paint.Align.LEFT) {
        paint.color = color
        paint.textSize = size
        paint.textAlign = align
        paint.typeface = if (bold) Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD) else Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        canvas.drawText(text, x, y, paint)
    }

    private fun drawWrapped(canvas: Canvas, text: String, x: Float, y: Float, maxWidth: Float, size: Float, color: Int, maxLines: Int) {
        val words = text.replace('\n', ' ').split(Regex("\\s+")).filter(String::isNotBlank)
        val lines = mutableListOf<String>()
        var current = ""
        paint.textSize = size
        for (word in words) {
            val proposed = if (current.isEmpty()) word else "$current $word"
            if (paint.measureText(proposed) <= maxWidth) current = proposed else {
                if (current.isNotEmpty()) lines += current
                current = word
            }
            if (lines.size == maxLines) break
        }
        if (current.isNotEmpty() && lines.size < maxLines) lines += current
        lines.take(maxLines).forEachIndexed { index, line -> drawText(canvas, line, x, y + index * size * 1.35f, size, color) }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            hitTargets.lastOrNull { it.first.contains(event.x, event.y) }?.second?.invoke()
            performClick()
        }
        if (event.action == MotionEvent.ACTION_MOVE && event.historySize > 0) {
            val delta = event.getHistoricalY(0) - event.y
            if (travelMenuOpen) {
                travelScroll = (travelScroll + delta).coerceAtLeast(0f)
                invalidate()
            } else if (selectedTab == TAB_MODS) {
                modsScroll = (modsScroll + delta).coerceAtLeast(0f)
                invalidate()
            }
        }
        return true
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    private fun dp(value: Float) = value * resources.displayMetrics.density

    companion object {
        private val BACKGROUND = Color.rgb(2, 10, 16)
        private val PANEL = Color.rgb(4, 24, 33)
        private val PANEL_INNER = Color.rgb(6, 35, 46)
        private val CYAN = Color.rgb(31, 213, 242)
        private val CYAN_DARK = Color.rgb(10, 91, 112)
        private val WHITE = Color.rgb(228, 246, 250)
        private val MUTED = Color.rgb(111, 158, 175)
        private val GREEN = Color.rgb(86, 220, 118)
        private val BLUE = Color.rgb(38, 155, 241)
        private val AMBER = Color.rgb(244, 181, 61)
        private const val TAB_MODS = "MODS"
    }
}
