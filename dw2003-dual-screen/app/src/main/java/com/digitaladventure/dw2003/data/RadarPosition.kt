package com.digitaladventure.dw2003.data

/** Sector anchors in the supplied world-map artwork, not room coordinates. */
object RadarPosition {
    data class Point(val x: Float, val y: Float)

    fun forStage(stageId: Int): Point? = when (MapRegionCatalog.resolve(stageId).sector) {
        SectorRegion.CENTRAL -> Point(0.50f, 0.50f)
        SectorRegion.EAST -> Point(0.75f, 0.46f)
        SectorRegion.SOUTH -> Point(0.66f, 0.73f)
        SectorRegion.WEST -> Point(0.21f, 0.58f)
        SectorRegion.NORTH -> Point(0.40f, 0.25f)
        SectorRegion.UNKNOWN -> null
    }
}
