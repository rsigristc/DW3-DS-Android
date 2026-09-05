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
import android.view.ViewConfiguration
import com.digitaladventure.dw2003.R
import com.digitaladventure.dw2003.data.AreaCatalog
import com.digitaladventure.dw2003.data.CheatCatalog
import com.digitaladventure.dw2003.data.CheatSpec
import com.digitaladventure.dw2003.data.CompanionLanguage
import com.digitaladventure.dw2003.data.FastTravelCatalog
import com.digitaladventure.dw2003.data.LocationResolver
import com.digitaladventure.dw2003.data.RadarPosition
import com.digitaladventure.dw2003.data.MapRegionCatalog
import com.digitaladventure.dw2003.data.ServerRegion
import com.digitaladventure.dw2003.data.SectorRegion
import com.digitaladventure.dw2003.data.WalkthroughCatalog
import com.digitaladventure.dw2003.model.DigimonState
import com.digitaladventure.dw2003.model.GameMode
import com.digitaladventure.dw2003.model.GameSnapshot
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

@SuppressLint("ViewConstructor")
class DigiviceDashboardView(
    context: Context,
    private val actions: DashboardActions
) : View(context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
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
    private var pageScroll = 0f
    private var pageScrollMax = 0f
    private var pageScrollKey = ""
    private var pageViewport: RectF? = null
    private var gestureDragging = false
    private var gestureStartY = 0f
    private var lastTouchY = 0f
    private var selectedTab = dashboardPreferences.getString("selected_tab", null)
        ?: GameMode.EXPLORATION.name
    var controlsVisible: Boolean = true
        set(value) { if (field == value) return; field = value; invalidate() }
    var gameHudVisible: Boolean = false
        set(value) { if (field == value) return; field = value; invalidate() }
    var quickFastForward: Boolean = false
        set(value) { if (field == value) return; field = value; invalidate() }
    var quickMuted: Boolean = false
        set(value) { if (field == value) return; field = value; invalidate() }
    var quickStateAvailable: Boolean = false
        set(value) { if (field == value) return; field = value; invalidate() }
    var battleScale: BattleScale = BattleScale.BATTLE_2X
        set(value) { if (field == value) return; field = value; invalidate() }
    var modsEnabled: Boolean = false
        set(value) {
            if (field == value) return
            field = value
            if (!value && selectedTab == TAB_MODS) {
                selectedTab = GameMode.EXPLORATION.name
                selectedMode = GameMode.EXPLORATION
                pageScroll = 0f
                pageScrollKey = selectedTab
            }
            invalidate()
        }
    var enabledCheats: Set<String> = emptySet()
        set(value) { if (field == value) return; field = value.toSet(); invalidate() }
    var customCheats: List<CheatSpec> = emptyList()
        set(value) { if (field == value) return; field = value; invalidate() }
    var visitedMaps: Set<Int> = emptySet()
        set(value) { if (field == value) return; field = value.toSet(); invalidate() }
    var language: CompanionLanguage = CompanionLanguage.SPANISH
        set(value) {
            if (field == value) return
            field = value
            contentDescription = tr(
                "Panel complementario de Digimon World 2003",
                "Digimon World 2003 companion panel"
            )
            invalidate()
        }
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
        contentDescription = tr(
            "Panel complementario de Digimon World 2003",
            "Digimon World 2003 companion panel"
        )
    }

    private fun tr(spanish: String, english: String): String =
        CompanionUiText.pick(language, spanish, english)

    private val numberFormat: NumberFormat
        get() = NumberFormat.getIntegerInstance(
            if (language == CompanionLanguage.ENGLISH) Locale.US else Locale("es", "MX")
        )

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
        pageViewport = null
        if (!isSideColumn()) {
            pageScroll = 0f
            pageScrollMax = 0f
        }
        canvas.drawColor(BACKGROUND)
        drawGrid(canvas)

        val margin = dp(12f)
        val headerBottom = if (gameHudVisible) dp(68f) else dp(104f)
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
        val title = CompanionUiText.area(language, snapshot.publicMapId)
        val detail = CompanionUiText.locationDetail(language, snapshot.publicMapId, snapshot.publicMapId)
        drawText(canvas, title.uppercase(), margin, dp(27f), dp(17f), WHITE, true)
        drawText(
            canvas,
            "${detail.uppercase()} · ${tr("HISTORIA", "STORY")} ${snapshot.storyStage}",
            margin,
            dp(49f),
            dp(9f),
            MUTED
        )
        val status = when {
            !snapshot.gameStarted -> tr("○ ESPERANDO PARTIDA", "○ WAITING FOR GAME")
            snapshot.isLive -> tr("● RAM EN VIVO", "● LIVE RAM")
            else -> "◇ DEMO"
        }
        val appButton = RectF(width - margin - dp(58f), dp(8f), width - margin, dp(35f))
        val hudButton = RectF(appButton.left - dp(64f), dp(8f), appButton.left - dp(6f), dp(35f))
        val padButton = RectF(hudButton.left - dp(64f), dp(8f), hudButton.left - dp(6f), dp(35f))
        paint.color = Color.rgb(8, 43, 56)
        canvas.drawRoundRect(appButton, dp(6f), dp(6f), paint)
        canvas.drawRoundRect(padButton, dp(6f), dp(6f), paint)
        paint.color = if (gameHudVisible) Color.rgb(8, 105, 126) else Color.rgb(8, 43, 56)
        canvas.drawRoundRect(hudButton, dp(6f), dp(6f), paint)
        drawText(canvas, "⚙ APP", appButton.centerX(), appButton.centerY() + dp(4f), dp(9f), CYAN, true, Paint.Align.CENTER)
        drawText(canvas, if (controlsVisible) "PAD ON" else "PAD OFF", padButton.centerX(), padButton.centerY() + dp(4f), dp(8f), if (controlsVisible) CYAN else MUTED, true, Paint.Align.CENTER)
        drawText(canvas, if (gameHudVisible) "HUD ON" else "HUD OFF", hudButton.centerX(), hudButton.centerY() + dp(4f), dp(8f), if (gameHudVisible) CYAN else MUTED, true, Paint.Align.CENTER)
        hitTargets += appButton to actions.onAppSettings
        hitTargets += padButton to actions.onToggleControls
        hitTargets += hudButton to actions.onToggleGameHud
        val statusColor = if (snapshot.gameStarted && snapshot.isLive) GREEN else AMBER
        drawText(canvas, status, padButton.left - dp(8f), dp(27f), dp(10f), statusColor, true, Paint.Align.RIGHT)
        val tabLabel = if (selectedTab == TAB_MODS) "Mods" else CompanionUiText.mode(language, selectedMode)
        drawText(canvas, tabLabel.uppercase(), width - margin, dp(49f), dp(10f), CYAN, true, Paint.Align.RIGHT)
        if (!gameHudVisible) {
            drawCompanionQuickBar(canvas, margin, dp(58f), bottom - dp(6f))
        }
        paint.color = CYAN_DARK
        canvas.drawRect(margin, bottom - dp(2f), width - margin, bottom, paint)
    }

    private fun drawCompanionQuickBar(canvas: Canvas, margin: Float, top: Float, bottom: Float) {
        val gap = dp(5f)
        val actionsToDraw = listOf(
            QuickAction.SAVE_STATE,
            QuickAction.LOAD_STATE,
            QuickAction.TOGGLE_SPEED,
            QuickAction.TOGGLE_MUTE,
            QuickAction.PICK_SCALE
        )
        val available = width - margin * 2 - gap * (actionsToDraw.size - 1)
        val itemWidth = available / actionsToDraw.size
        actionsToDraw.forEachIndexed { index, action ->
            val left = margin + index * (itemWidth + gap)
            val rect = RectF(left, top, left + itemWidth, bottom)
            val active = (action == QuickAction.TOGGLE_SPEED && quickFastForward) ||
                (action == QuickAction.TOGGLE_MUTE && quickMuted) ||
                (action == QuickAction.PICK_SCALE && battleScale != BattleScale.OFF)
            paint.color = if (active) Color.rgb(8, 105, 126) else Color.rgb(8, 43, 56)
            canvas.drawRoundRect(rect, dp(6f), dp(6f), paint)
            val dimmed = action == QuickAction.LOAD_STATE && !quickStateAvailable
            val label = if (action == QuickAction.PICK_SCALE) {
                CompanionUiText.battleScaleShort(language, battleScale)
            } else {
                CompanionUiText.quickAction(language, action, quickStateAvailable, quickFastForward, quickMuted)
            }
            drawText(
                canvas,
                label,
                rect.centerX(),
                rect.centerY() + dp(3.5f),
                dp(if (width < dp(420f)) 7f else 8.5f),
                if (dimmed) MUTED else WHITE,
                true,
                Paint.Align.CENTER
            )
            if (!dimmed) {
                hitTargets += rect to { actions.onQuickAction(action) }
            }
        }
    }

    private fun drawWaitingForGame(canvas: Canvas, bounds: RectF) {
        drawPanel(canvas, bounds, tr("SESIÓN DE JUEGO", "GAME SESSION"))
        drawText(
            canvas,
            tr("PARTIDA AÚN NO INICIADA", "GAME NOT STARTED YET"),
            bounds.centerX(),
            bounds.centerY() - dp(16f),
            dp(18f),
            WHITE,
            true,
            Paint.Align.CENTER
        )
        drawWrapped(
            canvas,
            tr(
                "Completa la introducción o carga una partida. El área, el objetivo y la formación aparecerán cuando la RAM contenga una sesión válida.",
                "Finish the intro or load a save. Area, objective and party appear once RAM holds a valid session."
            ),
            bounds.left + dp(24f),
            bounds.centerY() + dp(15f),
            bounds.width() - dp(48f),
            dp(11f),
            MUTED,
            4
        )
    }

    private fun isSideColumn(): Boolean = height > width

    private fun drawExploration(canvas: Canvas, bounds: RectF) {
        if (!isSideColumn()) {
            val left = RectF(bounds.left, bounds.top, bounds.left + bounds.width() * .48f, bounds.bottom)
            val right = RectF(left.right + dp(8f), bounds.top, bounds.right, bounds.bottom)
            drawRadar(canvas, left)
            withScrollablePage(canvas, right) { markBottom ->
                markBottom(drawObjectiveAndTamer(canvas, right, fillRemaining = true))
            }
            return
        }
        withScrollablePage(canvas, bounds) { markBottom ->
            val radarHeight = radarPanelHeight(bounds.width())
            val radar = RectF(bounds.left, bounds.top, bounds.right, bounds.top + radarHeight)
            drawRadar(canvas, radar)
            val rest = RectF(bounds.left, radar.bottom + dp(7f), bounds.right, radar.bottom + dp(7f) + dp(320f))
            markBottom(drawObjectiveAndTamer(canvas, rest))
        }
    }

    private fun currentRadarBitmap(): Bitmap? {
        val location = LocationResolver.resolve(snapshot.publicMapId, snapshot.publicMapId)
        val mapRegion = MapRegionCatalog.resolve(location.publicMapId)
        val region = if (mapRegion.server == ServerRegion.UNKNOWN) MapRegionCatalog.resolve(snapshot.areaId) else mapRegion
        return radarBitmaps[region.server to region.sector]
            ?: radarBitmaps[region.server to SectorRegion.UNKNOWN]
    }

    private fun radarPanelHeight(panelWidth: Float): Float {
        val innerWidth = max(dp(80f), panelWidth - dp(26f))
        val radar = currentRadarBitmap()
        val mapHeight = if (radar != null && radar.width > 0) {
            innerWidth * radar.height / radar.width
        } else {
            innerWidth * 0.72f
        }
        return mapHeight + dp(62f)
    }

    private fun drawRadar(canvas: Canvas, bounds: RectF) {
        val location = LocationResolver.resolve(snapshot.publicMapId, snapshot.publicMapId)
        val mapRegion = MapRegionCatalog.resolve(location.publicMapId)
        val region = if (mapRegion.server == ServerRegion.UNKNOWN) MapRegionCatalog.resolve(snapshot.areaId) else mapRegion
        val server = CompanionUiText.server(language, region.server)
        drawPanel(
            canvas,
            bounds,
            "${tr("RADAR REGIONAL", "REGIONAL RADAR")} · ${server.uppercase()}",
            Paint.Align.CENTER
        )
        val available = RectF(bounds.left + dp(13f), bounds.top + dp(38f), bounds.right - dp(13f), bounds.bottom - dp(24f))
        val radar = currentRadarBitmap()
        val map = if (radar != null && radar.width > 0 && radar.height > 0) {
            fittedBitmapRect(available, radar.width.toFloat(), radar.height.toFloat())
        } else {
            available
        }
        if (radar != null) {
            paint.isFilterBitmap = true
            canvas.drawBitmap(radar, null, map, paint)
            paint.color = Color.argb(55, 0, 20, 28)
            canvas.drawRect(map, paint)
        }
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp(2f)
        paint.color = CYAN
        RadarPosition.forStage(snapshot.publicMapId)?.let { point ->
            val x = map.left + map.width() * point.x
            val y = map.top + map.height() * point.y
            canvas.drawCircle(x, y, dp(7f), paint)
            canvas.drawLine(x - dp(12f), y, x + dp(12f), y, paint)
            canvas.drawLine(x, y - dp(12f), x, y + dp(12f), paint)
        }
        paint.style = Paint.Style.FILL
        drawText(
            canvas,
            CompanionUiText.locationRadar(language, snapshot.publicMapId, snapshot.publicMapId).uppercase(),
            bounds.centerX(),
            bounds.bottom - dp(8f),
            dp(7.5f),
            MUTED,
            true,
            Paint.Align.CENTER
        )
        if (snapshot.gameStarted && snapshot.supportsFastTravel) {
            drawText(
                canvas,
                tr("TOCA EL MAPA PARA VIAJE RÁPIDO", "TAP THE MAP FOR FAST TRAVEL"),
                bounds.centerX(),
                bounds.top + dp(31f),
                dp(7f),
                CYAN,
                true,
                Paint.Align.CENTER
            )
            hitContent(map) {
                travelMenuOpen = true
                travelScroll = 0f
                invalidate()
            }
        }
    }

    private fun drawObjectiveAndTamer(canvas: Canvas, bounds: RectF, fillRemaining: Boolean = false): Float {
        val objectiveText = WalkthroughCatalog.localized(
            snapshot.objective, snapshot.storyStage, language,
            snapshot.mapId.takeIf { it != 0 } ?: snapshot.areaId
        )
        val objectiveLines = wrappedLines(objectiveText, bounds.width() - dp(24f), dp(11f))
        val objectiveHeight = max(dp(118f), dp(44f) + objectiveLines.size * dp(11f) * 1.35f)
        val objective = RectF(bounds.left, bounds.top, bounds.right, bounds.top + objectiveHeight)
        drawPanel(canvas, objective, tr("OBJETIVO ACTUAL", "CURRENT OBJECTIVE"))
        drawWrapped(
            canvas,
            objectiveText,
            objective.left + dp(12f),
            objective.top + dp(38f),
            objective.width() - dp(24f),
            dp(11f),
            WHITE,
            objectiveLines.size
        )

        val tamerBottom = max(if (fillRemaining) bounds.bottom else 0f, objective.bottom + dp(7f) + dp(178f))
        val tamerBounds = RectF(bounds.left, objective.bottom + dp(7f), bounds.right, tamerBottom)
        drawPanel(canvas, tamerBounds, tr("ESTADO DEL TAMER", "TAMER STATUS"))
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
            snapshot.isFishing -> tr("PESCANDO", "FISHING")
            fishingPreview -> tr("VISTA DE PESCA", "FISHING PREVIEW")
            else -> tr("EXPLORANDO", "EXPLORING")
        }
        drawText(canvas, "${tr("ACTIVIDAD", "ACTIVITY")}  $activity", textX, tamerBounds.top + dp(69f), dp(9f), CYAN, true)
        drawText(canvas, "BITS", textX, tamerBounds.top + dp(96f), dp(8f), MUTED, true)
        drawText(canvas, formatNumber(snapshot.bits), textX, tamerBounds.top + dp(119f), dp(18f), AMBER, true)
        val fishingText = when {
            snapshot.isFishing -> tr("PESCANDO AHORA", "FISHING NOW")
            snapshot.fishingAvailable -> tr("PESCA DISPONIBLE · TOCA PARA PREVISUALIZAR", "FISHING AVAILABLE · TAP TO PREVIEW")
            else -> tr("SIN PUNTO DE PESCA EN ESTA ÁREA", "NO FISHING SPOT IN THIS AREA")
        }
        drawWrapped(canvas, fishingText, textX, tamerBounds.top + dp(143f), tamerBounds.right - textX - dp(12f), dp(7.5f), MUTED, 2)
        if (snapshot.fishingAvailable) {
            hitContent(tamerBounds) {
                fishingPreview = !fishingPreview
                invalidate()
            }
        }
        return tamerBounds.bottom
    }

    private fun drawBattle(canvas: Canvas, bounds: RectF) {
        if (!isSideColumn()) {
            drawStackedBattle(canvas, bounds)
            return
        }
        withScrollablePage(canvas, bounds) { markBottom ->
            var y = bounds.top
            val header = RectF(bounds.left, y, bounds.right, y + dp(30f))
            drawPanel(canvas, header, tr("TELEMETRÍA DE BATALLA", "BATTLE TELEMETRY"))
            y = header.bottom + dp(7f)
            if (snapshot.party.isEmpty()) {
                drawWrapped(
                    canvas,
                    tr(
                        "No hay equipo en RAM todavía. Carga una partida; en USA el panel usa el bloque de memoria NTSC.",
                        "No party in RAM yet. Load a save; on USA the panel reads the NTSC save block."
                    ),
                    bounds.left + dp(12f),
                    y + dp(8f),
                    bounds.width() - dp(24f),
                    dp(11f),
                    MUTED,
                    4
                )
                return@withScrollablePage
            }
            snapshot.party.take(3).forEachIndexed { index, digimon ->
                val card = RectF(bounds.left + dp(9f), y, bounds.right - dp(9f), y + battleCardHeight(digimon))
                drawDigimonCard(canvas, card, digimon, compact = true, partyIndex = index, expanded = true)
                y = card.bottom + dp(7f)
            }
            markBottom(drawBattleNote(canvas, bounds.centerX(), y + dp(10f)) + dp(12f))
        }
    }

    private fun drawStackedBattle(canvas: Canvas, bounds: RectF) {
        drawPanel(canvas, bounds, tr("TELEMETRÍA DE BATALLA", "BATTLE TELEMETRY"))
        val party = snapshot.party.take(3)
        if (party.isEmpty()) {
            drawWrapped(
                canvas,
                tr(
                    "No hay equipo en RAM todavía. Carga una partida; en USA el panel usa el bloque de memoria NTSC.",
                    "No party in RAM yet. Load a save; on USA the panel reads the NTSC save block."
                ),
                bounds.left + dp(12f),
                bounds.top + dp(38f),
                bounds.width() - dp(24f),
                dp(11f),
                MUTED,
                4
            )
            return
        }
        val gap = dp(7f)
        val cardW = (bounds.width() - dp(20f) - gap * 2) / max(1, party.size)
        party.forEachIndexed { index, digimon ->
            val card = RectF(
                bounds.left + dp(10f) + index * (cardW + gap),
                bounds.top + dp(31f),
                bounds.left + dp(10f) + index * (cardW + gap) + cardW,
                bounds.bottom - dp(24f)
            )
            drawDigimonCard(canvas, card, digimon, compact = false, partyIndex = index, expanded = false)
        }
        drawBattleNote(canvas, bounds.centerX(), bounds.bottom - dp(12f))
    }

    private fun drawBattleNote(canvas: Canvas, x: Float, y: Float): Float {
        val note = if (snapshot.canReorderParty) {
            tr("TOCA ▲▼ PARA CAMBIAR EL ORDEN DE SALIDA", "TAP ▲▼ TO CHANGE BATTLE ORDER")
        } else {
            tr("REORDENAR SOLO FUERA DE BATALLA O EVENTOS", "REORDER ONLY OUTSIDE BATTLE OR EVENTS")
        }
        drawText(canvas, note, x, y, dp(7.5f), if (snapshot.canReorderParty) CYAN else MUTED, true, Paint.Align.CENTER)
        return y
    }

    private fun drawManagement(canvas: Canvas, bounds: RectF) {
        val selected = snapshot.party.getOrNull(selectedPartyIndex)
        if (selected == null) {
            drawPanel(canvas, bounds, tr("PARTNER / ESTADO", "PARTNER / STATUS"))
            drawWrapped(
                canvas,
                tr(
                    "No hay equipo en RAM todavía. Carga una partida; en USA el panel usa el bloque de memoria NTSC.",
                    "No party in RAM yet. Load a save; on USA the panel reads the NTSC save block."
                ),
                bounds.left + dp(12f),
                bounds.top + dp(38f),
                bounds.width() - dp(24f),
                dp(11f),
                MUTED,
                4
            )
            return
        }
        if (!isSideColumn()) {
            val identity = RectF(bounds.left, bounds.top, bounds.left + bounds.width() * .26f, bounds.bottom)
            val details = RectF(identity.right + dp(7f), bounds.top, bounds.right, bounds.bottom)
            val stats = RectF(details.left, details.top, details.left + details.width() * .30f, details.bottom)
            val skills = RectF(stats.right + dp(7f), details.top, stats.right + dp(7f) + details.width() * .30f, details.bottom)
            val equipment = RectF(skills.right + dp(7f), details.top, details.right, details.bottom)
            drawIdentity(canvas, identity, selected, vertical = true)
            drawParametersAndResists(canvas, stats, selected)
            drawActiveSkills(canvas, skills, selected)
            drawEquipment(canvas, equipment, selected)
            return
        }
        withScrollablePage(canvas, bounds) { markBottom ->
            var y = bounds.top
            val identity = RectF(bounds.left, y, bounds.right, y + identityPanelHeight(selected))
            drawIdentity(canvas, identity, selected, vertical = false)
            y = identity.bottom + dp(7f)
            val parameters = RectF(bounds.left, y, bounds.right, y + parametersPanelHeight())
            drawParameters(canvas, parameters, selected)
            y = parameters.bottom + dp(7f)
            val resists = RectF(bounds.left, y, bounds.right, y + resistsPanelHeight())
            drawResists(canvas, resists, selected)
            y = resists.bottom + dp(7f)
            val skills = RectF(bounds.left, y, bounds.right, y + skillsPanelHeight(selected))
            drawActiveSkills(canvas, skills, selected)
            y = skills.bottom + dp(7f)
            val equipment = RectF(bounds.left, y, bounds.right, y + equipmentPanelHeight())
            drawEquipment(canvas, equipment, selected)
            markBottom(equipment.bottom)
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
        val expLabel = if (next == null) {
            "EXP ${digimon.experience} · ${tr("MÁX.", "MAX")}"
        } else {
            "EXP ${digimon.experience}/$next"
        }
        drawMetricLine(canvas, expLabel, left, y + dp(49f), barX, barW, digimon.experienceFraction, AMBER)
    }

    private fun drawDigimonCard(
        canvas: Canvas,
        bounds: RectF,
        digimon: DigimonState,
        compact: Boolean,
        partyIndex: Int = -1,
        expanded: Boolean = false
    ) {
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
                hitContent(up) { actions.onPartyMove(partyIndex, partyIndex - 1) }
            }
            if (enabled && partyIndex < snapshot.party.lastIndex) {
                hitContent(down) { actions.onPartyMove(partyIndex, partyIndex + 1) }
            }
        }
        val x = bounds.left + dp(9f)
        val w = bounds.width() - dp(18f)
        drawBar(canvas, x, bounds.top + dp(61f), w, dp(8f), digimon.hpFraction, GREEN)
        drawText(canvas, "HP ${digimon.currentHp} / ${digimon.maxHp}", x, bounds.top + dp(82f), dp(9f), WHITE)
        if (expanded || bounds.height() > dp(125f)) {
            drawBar(canvas, x, bounds.top + dp(91f), w, dp(7f), digimon.mpFraction, BLUE)
            drawText(canvas, "MP ${digimon.currentMp} / ${digimon.maxMp}", x, bounds.top + dp(112f), dp(9f), WHITE)
        }
        if (expanded || bounds.height() > dp(145f)) {
            drawBar(canvas, x, bounds.top + dp(121f), w, dp(7f), digimon.experienceFraction, AMBER)
            val next = digimon.nextLevelExperience
            val exp = if (next == null) {
                "EXP ${digimon.experience} · ${tr("NIVEL MÁX.", "MAX LEVEL")}"
            } else {
                "EXP ${digimon.experience} / $next · ${tr("FALTAN", "LEFT")} ${digimon.experienceRemaining}"
            }
            drawText(canvas, exp, x, bounds.top + dp(143f), dp(8f), WHITE)
        }
        if (expanded || bounds.height() > dp(165f)) {
            drawFormList(canvas, digimon, x, bounds.top + dp(161f), bounds.right - dp(9f), bounds.bottom - dp(6f))
        }
    }

    private fun drawIdentity(canvas: Canvas, bounds: RectF, digimon: DigimonState, vertical: Boolean) {
        drawPanel(canvas, bounds, "${tr("PARTNER / ESTADO", "PARTNER / STATUS")}  ·  ${selectedPartyIndex + 1}/${snapshot.party.size}")
        val arrowSize = dp(34f)
        val arrowY = bounds.top + dp(29f)
        val leftArrow = RectF(bounds.left + dp(8f), arrowY, bounds.left + dp(8f) + arrowSize, arrowY + arrowSize)
        val rightArrow = RectF(bounds.right - dp(8f) - arrowSize, arrowY, bounds.right - dp(8f), arrowY + arrowSize)
        paint.color = PANEL_INNER
        canvas.drawRoundRect(leftArrow, dp(5f), dp(5f), paint)
        canvas.drawRoundRect(rightArrow, dp(5f), dp(5f), paint)
        drawText(canvas, "‹", leftArrow.centerX(), leftArrow.centerY() + dp(7f), dp(22f), CYAN, true, Paint.Align.CENTER)
        drawText(canvas, "›", rightArrow.centerX(), rightArrow.centerY() + dp(7f), dp(22f), CYAN, true, Paint.Align.CENTER)
        hitContent(leftArrow) { selectParty(-1) }
        hitContent(rightArrow) { selectParty(1) }
        val portrait = RectF(bounds.centerX() - dp(18f), bounds.top + dp(28f), bounds.centerX() + dp(18f), bounds.top + dp(64f))
        digimonSprites[digimon.profileId]?.let { drawPixelBitmap(canvas, it, portrait, dp(34f)) }
        drawText(canvas, digimon.name.uppercase(), bounds.centerX(), bounds.top + dp(80f), if (vertical) dp(15f) else dp(17f), WHITE, true, Paint.Align.CENTER)
        drawText(canvas, "${tr("NIVEL", "LEVEL")} ${digimon.level}  ·  TP ${digimon.trainingPoints}", bounds.centerX(), bounds.top + dp(96f), dp(8f), CYAN, true, Paint.Align.CENTER)
        val x = bounds.left + dp(12f)
        val w = bounds.width() - dp(24f)
        drawLabeledBar(canvas, "HP ${digimon.currentHp}/${digimon.maxHp}", x, bounds.top + dp(107f), w, digimon.hpFraction, GREEN)
        drawLabeledBar(canvas, "MP ${digimon.currentMp}/${digimon.maxMp}", x, bounds.top + dp(129f), w, digimon.mpFraction, BLUE)
        val next = digimon.nextLevelExperience
        val exp = if (next == null) {
            "EXP ${digimon.experience} · ${tr("NIVEL MÁX.", "MAX LEVEL")}"
        } else {
            "EXP ${digimon.experience}/$next · ${tr("FALTAN", "LEFT")} ${digimon.experienceRemaining}"
        }
        drawLabeledBar(canvas, exp, x, bounds.top + dp(151f), w, digimon.experienceFraction, AMBER)
        drawFormList(canvas, digimon, x, bounds.top + dp(176f), bounds.right - dp(12f), bounds.bottom - dp(8f))
    }

    private fun drawFormList(
        canvas: Canvas,
        digimon: DigimonState,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float
    ) {
        if (bottom - top < dp(22f)) return
        drawText(canvas, tr("DIGIEVOLUCIONES", "DIGIVOLUTIONS"), left, top, dp(7.5f), CYAN, true)
        val forms = digimon.displayedForms
        val rowHeight = dp(16f)
        forms.forEachIndexed { index, form ->
            val y = top + dp(16f) + index * rowHeight
            if (y + dp(2f) > bottom) return
            val color = if (form.active) BLUE else WHITE
            drawText(canvas, form.name.uppercase(), left, y, dp(8f), color, form.active)
            drawText(
                canvas,
                "NV ${form.level}",
                right,
                y,
                dp(8f),
                color,
                form.active,
                Paint.Align.RIGHT
            )
        }
    }

    private fun drawParametersAndResists(canvas: Canvas, bounds: RectF, digimon: DigimonState) {
        val gap = dp(7f)
        val split = bounds.top + (bounds.height() - gap) / 2f
        drawParameters(canvas, RectF(bounds.left, bounds.top, bounds.right, split), digimon)
        drawResists(canvas, RectF(bounds.left, split + gap, bounds.right, bounds.bottom), digimon)
    }

    private fun drawParameters(canvas: Canvas, bounds: RectF, digimon: DigimonState) {
        drawPanel(canvas, bounds, tr("PARÁMETROS", "PARAMETERS"))
        val bonuses = digimon.equipmentBonuses
        val values = listOf(
            Triple(tr("FUERZA", "STRENGTH"), digimon.totalStrength, bonuses.strength),
            Triple(tr("DEFENSA", "DEFENSE"), digimon.totalDefense, bonuses.defense),
            Triple(tr("ESPÍRITU", "SPIRIT"), digimon.totalSpirit, bonuses.spirit),
            Triple(tr("SABIDURÍA", "WISDOM"), digimon.totalWisdom, bonuses.wisdom),
            Triple(tr("VELOCIDAD", "SPEED"), digimon.totalSpeed, bonuses.speed),
            Triple(tr("CARISMA", "CHARISMA"), digimon.totalCharisma, bonuses.charisma)
        )
        drawBonusGrid(canvas, bounds, values, columns = 2, valueSize = dp(15f))
    }

    private fun drawResists(canvas: Canvas, bounds: RectF, digimon: DigimonState) {
        drawPanel(canvas, bounds, tr("RESISTENCIAS ELEMENTALES", "ELEMENTAL RESISTS"))
        val names = listOf(
            tr("FUEGO", "FIRE"),
            tr("AGUA", "WATER"),
            tr("HIELO", "ICE"),
            tr("VIENTO", "WIND"),
            tr("RAYO", "THUNDER"),
            tr("MÁQUINA", "MACHINE"),
            tr("OSCURIDAD", "DARKNESS")
        )
        val totals = digimon.totalResistances
        val values = names.mapIndexed { index, name ->
            Triple(name, totals.getOrElse(index) { 0 }, digimon.equipmentBonuses.resistance(index))
        }
        drawBonusGrid(canvas, bounds, values, columns = 2, valueSize = dp(13f), labelSize = dp(7f))
    }

    private fun drawBonusGrid(
        canvas: Canvas,
        bounds: RectF,
        values: List<Triple<String, Int, Int>>,
        columns: Int,
        valueSize: Float,
        labelSize: Float = dp(7.5f)
    ) {
        val rows = (values.size + columns - 1) / columns
        val cellW = (bounds.width() - dp(20f)) / columns
        val cellH = (bounds.height() - dp(32f)) / rows
        values.forEachIndexed { index, value ->
            val x = bounds.left + dp(10f) + (index % columns) * cellW
            val y = bounds.top + dp(36f) + (index / columns) * cellH
            val color = bonusColor(value.third)
            drawText(canvas, value.first, x, y, labelSize, if (value.third == 0) MUTED else color, true)
            drawText(canvas, value.second.toString(), x, y + dp(16f), valueSize, color, true)
        }
    }

    private fun drawActiveSkills(canvas: Canvas, bounds: RectF, digimon: DigimonState) {
        drawPanel(canvas, bounds, tr("HABILIDADES ACTIVAS", "ACTIVE SKILLS"))
        drawText(
            canvas,
            digimon.activeDigievolutionName.uppercase(),
            bounds.left + dp(10f),
            bounds.top + dp(36f),
            dp(7.5f),
            CYAN,
            true
        )
        val skills = digimon.activeSkills
        if (skills.isEmpty()) {
            drawWrapped(
                canvas,
                tr("Sin técnicas identificadas", "No techniques identified"),
                bounds.left + dp(10f),
                bounds.top + dp(54f),
                bounds.width() - dp(20f),
                dp(8f),
                MUTED,
                3
            )
            return
        }
        val rowHeight = max(dp(36f), (bounds.height() - dp(46f)) / max(3, skills.size))
        skills.forEachIndexed { index, skill ->
            val top = bounds.top + dp(46f) + index * rowHeight
            if (index > 0) {
                paint.color = Color.rgb(8, 49, 61)
                canvas.drawRect(bounds.left + dp(10f), top - dp(3f), bounds.right - dp(10f), top - dp(2f), paint)
            }
            drawText(canvas, skill.name, bounds.left + dp(10f), top + dp(11f), dp(9f), WHITE, true)
            val mp = skill.mp?.toString() ?: "—"
            val power = skill.power?.toString() ?: "—"
            drawText(
                canvas,
                "${tr("MP", "MP")} $mp  ·  ${tr("PODER", "POWER")} $power",
                bounds.left + dp(10f),
                top + dp(25f),
                dp(7.5f),
                CYAN,
                true
            )
        }
    }

    private fun bonusColor(delta: Int): Int = when {
        delta > 0 -> BLUE
        delta < 0 -> RED
        else -> WHITE
    }

    private fun drawEquipment(canvas: Canvas, bounds: RectF, digimon: DigimonState) {
        drawPanel(canvas, bounds, tr("EQUIPO Y BONIFICACIONES", "EQUIPMENT AND BONUSES"))
        val slots = listOf(
            tr("CABEZA", "HEAD"),
            tr("CUERPO", "BODY"),
            tr("MANO DER.", "RIGHT HAND"),
            tr("MANO IZQ.", "LEFT HAND"),
            tr("ACCESORIO 1", "ACCESSORY 1"),
            tr("ACCESORIO 2", "ACCESSORY 2")
        )
        val rowHeight = (bounds.height() - dp(30f)) / slots.size
        slots.forEachIndexed { index, slot ->
            val top = bounds.top + dp(27f) + index * rowHeight
            val info = digimon.equippedItems.getOrNull(index)
            if (index > 0) {
                paint.color = Color.rgb(8, 49, 61)
                canvas.drawRect(bounds.left + dp(9f), top - dp(2f), bounds.right - dp(9f), top - dp(1f), paint)
            }
            drawText(canvas, slot, bounds.left + dp(10f), top + dp(9f), dp(6.5f), MUTED, true)
            val name = info?.name ?: tr("— VACÍO —", "— EMPTY —")
            drawText(canvas, name, bounds.left + dp(10f), top + dp(21f), dp(8f), WHITE, true)
            if (info != null && rowHeight > dp(31f)) {
                val stats = listOfNotNull(
                    CompanionUiText.equipmentType(language, info.type).takeIf { it.isNotBlank() },
                    CompanionUiText.equipmentStats(language, info.stats)
                ).joinToString(" · ")
                drawText(canvas, stats, bounds.left + dp(10f), top + dp(32f), dp(6.5f), CYAN)
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
                pageScroll = 0f
                pageScrollKey = tab.first
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
        val tabs = GameMode.entries.map { it.name to CompanionUiText.mode(language, it) }.toMutableList()
        if (modsEnabled) tabs += TAB_MODS to "Mods"
        return tabs
    }

    private fun drawFastTravelMenu(canvas: Canvas, bounds: RectF) {
        drawPanel(canvas, bounds, tr("VIAJE RÁPIDO · DESTINOS VISITADOS", "FAST TRAVEL · VISITED DESTINATIONS"))
        val close = RectF(bounds.right - dp(78f), bounds.top + dp(6f), bounds.right - dp(10f), bounds.top + dp(28f))
        paint.color = PANEL_INNER
        canvas.drawRoundRect(close, dp(5f), dp(5f), paint)
        drawText(canvas, tr("CERRAR", "CLOSE"), close.centerX(), close.centerY() + dp(4f), dp(8f), WHITE, true, Paint.Align.CENTER)
        hitTargets += close to {
            travelMenuOpen = false
            invalidate()
        }
        val openMap = RectF(bounds.left + dp(10f), bounds.top + dp(34f), bounds.right - dp(10f), bounds.top + dp(66f))
        if (snapshot.supportsFastTravel) {
            paint.color = CYAN_DARK
            canvas.drawRoundRect(openMap, dp(5f), dp(5f), paint)
            drawText(canvas, tr("ABRIR PESTAÑA MAPA", "OPEN MAP TAB"), openMap.centerX(), openMap.centerY() + dp(4f), dp(10f), WHITE, true, Paint.Align.CENTER)
            hitTargets += openMap to {
                travelMenuOpen = false
                actions.onOpenGameMap()
                invalidate()
            }
        }
        val currentIcon = FastTravelCatalog.iconId(snapshot.publicMapId)
        val groups = FastTravelCatalog.groups(
            snapshot.storyStage,
            visitedMaps,
            snapshot.publicMapId,
            snapshot.publicMapId
        )
        val destinationCount = groups.sumOf { it.destinations.size }
        if (destinationCount > 0) {
            drawText(
                canvas,
                tr("$destinationCount DESTINOS VISITADOS", "$destinationCount VISITED DESTINATIONS"),
                bounds.left + dp(12f),
                openMap.bottom + dp(14f),
                dp(8f),
                MUTED,
                true
            )
        }
        val listTop = openMap.bottom + if (destinationCount > 0) dp(20f) else dp(6f)
        if (!snapshot.supportsFastTravel) {
            drawWrapped(
                canvas,
                tr(
                    "El viaje rápido de Flawe no está disponible en Digimon World 3 USA.",
                    "Flawe fast travel is not available on Digimon World 3 USA."
                ),
                bounds.left + dp(16f),
                listTop + dp(4f),
                bounds.width() - dp(32f),
                dp(10f),
                AMBER,
                3
            )
        } else if (!snapshot.canFastTravel) {
            drawWrapped(
                canvas,
                tr(
                    "El viaje rápido no está disponible durante batallas o eventos.",
                    "Fast travel is unavailable during battles or events."
                ),
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
                tr(
                    "Todavía no hay destinos visitados. Explora el campo para añadir localidades, o abre el mapa del juego para el viaje de Flawe's Mod.",
                    "No visited destinations yet. Explore the field to add locations, or open the in-game map for Flawe's Mod travel."
                ),
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
        val rowHeight = dp(24f)
        groups.forEach { group ->
            if (y >= listTop && y < bounds.bottom - dp(8f)) {
                drawText(
                    canvas,
                    "${CompanionUiText.server(language, group.server).uppercase()} · ${CompanionUiText.sector(language, group.sector).uppercase()}",
                    bounds.left + dp(12f),
                    y + dp(16f),
                    dp(8f),
                    CYAN,
                    true
                )
            }
            y += dp(16f)
            group.destinations.forEach { destination ->
                val row = RectF(bounds.left + dp(10f), y, bounds.right - dp(10f), y + rowHeight)
                if (row.top >= listTop && row.top < bounds.bottom - dp(8f)) {
                    paint.color = if (destination.areaId == currentIcon) CYAN_DARK else PANEL_INNER
                    canvas.drawRoundRect(row, dp(5f), dp(5f), paint)
                    drawText(canvas, CompanionUiText.area(language, destination.areaId).uppercase(), row.left + dp(10f), row.centerY() + dp(4f), dp(10f), WHITE, true)
                    drawText(canvas, "0x${AreaCatalog.hex(destination.areaId)}", row.right - dp(10f), row.centerY() + dp(4f), dp(8f), MUTED, true, Paint.Align.RIGHT)
                    if (snapshot.canFastTravel && destination.areaId != currentIcon) {
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
        drawPanel(canvas, bounds, tr("MODS Y CÓDIGOS PAL", "MODS AND PAL CODES"))
        drawWrapped(
            canvas,
            tr(
                "Los códigos se aplican sobre la RAM emulada de SLES-03936 / Flawe's Mod. Úsalos fuera de secuencias críticas.",
                "Codes are applied to the emulated SLES-03936 / Flawe's Mod RAM. Use them outside critical sequences."
            ),
            bounds.left + dp(12f),
            bounds.top + dp(34f),
            bounds.width() - dp(24f),
            dp(9f),
            MUTED,
            2
        )
        val add = RectF(bounds.left + dp(10f), bounds.top + dp(68f), bounds.right - dp(10f), bounds.top + dp(100f))
        paint.color = CYAN_DARK
        canvas.drawRoundRect(add, dp(6f), dp(6f), paint)
        drawText(
            canvas,
            tr("AÑADIR MOD PERSONALIZADO", "ADD CUSTOM MOD"),
            add.centerX(),
            add.centerY() + dp(4f),
            dp(10f),
            WHITE,
            true,
            Paint.Align.CENTER
        )
        hitTargets += add to { actions.onAddCustomCheat() }
        var y = add.bottom + dp(8f) - modsScroll
        (CheatCatalog.all + customCheats).forEach { cheat ->
            val row = RectF(bounds.left + dp(10f), y, bounds.right - dp(10f), y + dp(52f))
            if (row.bottom > add.bottom + dp(4f) && row.top < bounds.bottom - dp(8f)) {
                paint.color = PANEL_INNER
                canvas.drawRoundRect(row, dp(6f), dp(6f), paint)
                val enabled = cheat.id in enabledCheats
                val toggle = RectF(row.right - dp(72f), row.top + dp(12f), row.right - dp(10f), row.bottom - dp(12f))
                paint.color = if (enabled) CYAN else CYAN_DARK
                canvas.drawRoundRect(toggle, dp(5f), dp(5f), paint)
                drawText(canvas, if (enabled) "ON" else "OFF", toggle.centerX(), toggle.centerY() + dp(4f), dp(10f), if (enabled) Color.rgb(2, 16, 22) else WHITE, true, Paint.Align.CENTER)
                val custom = cheat.id.startsWith("custom_")
                if (custom) {
                    val remove = RectF(toggle.left - dp(54f), row.top + dp(12f), toggle.left - dp(8f), row.bottom - dp(12f))
                    paint.color = Color.rgb(72, 24, 28)
                    canvas.drawRoundRect(remove, dp(5f), dp(5f), paint)
                    drawText(canvas, "✕", remove.centerX(), remove.centerY() + dp(4f), dp(11f), RED, true, Paint.Align.CENTER)
                    hitTargets += remove to { actions.onRemoveCustomCheat(cheat.id) }
                }
                drawText(canvas, CompanionUiText.cheatLabel(language, cheat).uppercase(), row.left + dp(10f), row.top + dp(18f), dp(11f), WHITE, true)
                val detail = if (custom) cheat.code else CompanionUiText.cheatDetail(language, cheat)
                drawText(canvas, detail, row.left + dp(10f), row.top + dp(36f), dp(8f), MUTED)
                hitTargets += toggle to { actions.onCheatToggle(cheat.id, !enabled) }
            }
            y += dp(58f)
        }
        val maxScroll = max(0f, y + modsScroll - bounds.bottom + dp(8f))
        modsScroll = modsScroll.coerceIn(0f, maxScroll)
    }

    private fun withScrollablePage(canvas: Canvas, viewport: RectF, draw: ((Float) -> Unit) -> Unit) {
        if (pageScrollKey != selectedTab) {
            pageScroll = 0f
            pageScrollKey = selectedTab
        }
        pageViewport = viewport
        var contentBottom = viewport.top
        canvas.save()
        canvas.clipRect(viewport)
        canvas.translate(0f, -pageScroll)
        draw { bottom -> contentBottom = max(contentBottom, bottom) }
        canvas.restore()
        pageScrollMax = max(0f, contentBottom - viewport.bottom)
        pageScroll = pageScroll.coerceIn(0f, pageScrollMax)
        if (pageScrollMax > 0f) {
            drawScrollThumb(canvas, viewport)
        }
    }

    private fun hitContent(bounds: RectF, action: () -> Unit) {
        if (pageViewport != null) {
            hitInPage(bounds, action)
        } else {
            hitTargets += bounds to action
        }
    }

    private fun hitInPage(bounds: RectF, action: () -> Unit) {
        val viewport = pageViewport ?: return
        val screen = RectF(bounds.left, bounds.top - pageScroll, bounds.right, bounds.bottom - pageScroll)
        if (screen.bottom <= viewport.top || screen.top >= viewport.bottom) return
        val clipped = RectF(
            screen.left,
            max(screen.top, viewport.top),
            screen.right,
            min(screen.bottom, viewport.bottom)
        )
        if (clipped.height() > 0f) {
            hitTargets += clipped to action
        }
    }

    private fun fittedBitmapRect(available: RectF, srcW: Float, srcH: Float): RectF {
        if (srcW <= 0f || srcH <= 0f) return available
        val scale = min(available.width() / srcW, available.height() / srcH)
        val width = srcW * scale
        val height = srcH * scale
        val left = available.centerX() - width / 2f
        val top = available.centerY() - height / 2f
        return RectF(left, top, left + width, top + height)
    }

    private fun formListHeight(digimon: DigimonState): Float =
        dp(16f) + max(1, digimon.displayedForms.size) * dp(16f) + dp(8f)

    private fun battleCardHeight(digimon: DigimonState): Float =
        dp(168f) + formListHeight(digimon)

    private fun identityPanelHeight(digimon: DigimonState): Float =
        dp(176f) + formListHeight(digimon)

    private fun parametersPanelHeight(): Float = dp(36f) + 3 * dp(42f) + dp(12f)

    private fun resistsPanelHeight(): Float = dp(36f) + 4 * dp(38f) + dp(12f)

    private fun skillsPanelHeight(digimon: DigimonState): Float {
        val rows = max(1, digimon.activeSkills.size)
        return dp(52f) + rows * dp(40f) + dp(12f)
    }

    private fun equipmentPanelHeight(): Float = dp(30f) + 6 * dp(40f) + dp(10f)

    private fun drawScrollThumb(canvas: Canvas, viewport: RectF) {
        val trackHeight = viewport.height()
        val thumbHeight = max(dp(24f), trackHeight * trackHeight / (trackHeight + pageScrollMax))
        val travel = trackHeight - thumbHeight
        val thumbTop = viewport.top + if (pageScrollMax > 0f) travel * (pageScroll / pageScrollMax) else 0f
        paint.color = Color.argb(170, 31, 213, 242)
        canvas.drawRoundRect(
            RectF(viewport.right - dp(4f), thumbTop, viewport.right - dp(1f), thumbTop + thumbHeight),
            dp(2f),
            dp(2f),
            paint
        )
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

    private fun drawPanel(canvas: Canvas, bounds: RectF, title: String, titleAlign: Paint.Align = Paint.Align.LEFT) {
        paint.style = Paint.Style.FILL
        paint.color = PANEL
        canvas.drawRoundRect(bounds, dp(7f), dp(7f), paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp(1f)
        paint.color = CYAN_DARK
        canvas.drawRoundRect(bounds, dp(7f), dp(7f), paint)
        paint.style = Paint.Style.FILL
        val titleX = when (titleAlign) {
            Paint.Align.CENTER -> bounds.centerX()
            Paint.Align.RIGHT -> bounds.right - dp(10f)
            else -> bounds.left + dp(10f)
        }
        drawText(canvas, title, titleX, bounds.top + dp(19f), dp(9f), CYAN, true, titleAlign)
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
        wrappedLines(text, maxWidth, size).take(maxLines).forEachIndexed { index, line ->
            drawText(canvas, line, x, y + index * size * 1.35f, size, color)
        }
    }

    private fun wrappedLines(text: String, maxWidth: Float, size: Float): List<String> {
        val words = text.replace('\n', ' ').split(Regex("\\s+")).filter(String::isNotBlank)
        val lines = mutableListOf<String>()
        var current = ""
        paint.textSize = size
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        for (word in words) {
            val proposed = if (current.isEmpty()) word else "$current $word"
            if (paint.measureText(proposed) <= maxWidth) current = proposed else {
                if (current.isNotEmpty()) lines += current
                current = word
            }
        }
        if (current.isNotEmpty()) lines += current
        return lines
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                gestureDragging = false
                gestureStartY = event.y
                lastTouchY = event.y
            }
            MotionEvent.ACTION_MOVE -> {
                val slop = ViewConfiguration.get(context).scaledTouchSlop
                if (abs(event.y - gestureStartY) > slop) {
                    gestureDragging = true
                }
                val delta = lastTouchY - event.y
                lastTouchY = event.y
                if (gestureDragging && applyScroll(delta)) {
                    invalidate()
                }
            }
            MotionEvent.ACTION_UP -> {
                if (!gestureDragging) {
                    hitTargets.lastOrNull { it.first.contains(event.x, event.y) }?.second?.invoke()
                    performClick()
                }
                gestureDragging = false
            }
            MotionEvent.ACTION_CANCEL -> gestureDragging = false
        }
        return true
    }

    private fun applyScroll(delta: Float): Boolean {
        if (delta == 0f) return false
        return when {
            travelMenuOpen -> {
                val next = (travelScroll + delta).coerceAtLeast(0f)
                if (next == travelScroll) false else {
                    travelScroll = next
                    true
                }
            }
            selectedTab == TAB_MODS -> {
                val next = (modsScroll + delta).coerceAtLeast(0f)
                if (next == modsScroll) false else {
                    modsScroll = next
                    true
                }
            }
            pageScrollMax > 0f -> {
                val next = (pageScroll + delta).coerceIn(0f, pageScrollMax)
                if (next == pageScroll) false else {
                    pageScroll = next
                    true
                }
            }
            else -> false
        }
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
        private val RED = Color.rgb(232, 88, 88)
        private val AMBER = Color.rgb(244, 181, 61)
        private const val TAB_MODS = "MODS"
    }
}
