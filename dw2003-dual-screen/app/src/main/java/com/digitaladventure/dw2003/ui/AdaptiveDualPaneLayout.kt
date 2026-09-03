package com.digitaladventure.dw2003.ui

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.window.layout.FoldingFeature

class AdaptiveDualPaneLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ViewGroup(context, attrs) {
    private var foldBounds: Rect? = null
    private var foldOrientation: FoldingFeature.Orientation? = null
    private var foldSeparating = false
    private var externalDashboardActive = false
    private var arrangement = PaneArrangement.AUTO

    fun setFold(feature: FoldingFeature?) {
        foldBounds = feature?.bounds
        foldOrientation = feature?.orientation
        foldSeparating = feature?.isSeparating == true
        requestLayout()
    }

    fun setGameOnly(value: Boolean) {
        externalDashboardActive = value
        requestLayout()
    }

    fun setArrangement(value: PaneArrangement) {
        arrangement = value
        requestLayout()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        setMeasuredDimension(width, height)
        if (childCount == 0) return

        if (!shouldShowDashboard(width) || childCount == 1) {
            measureExact(getChildAt(0), width, height)
            if (childCount > 1) measureExact(getChildAt(1), 0, 0)
            return
        }

        val split = resolveSplit(width, height)
        measureExact(getChildAt(0), split.first.width(), split.first.height())
        measureExact(getChildAt(1), split.second.width(), split.second.height())
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        if (childCount == 0) return
        if (!shouldShowDashboard(width) || childCount == 1) {
            getChildAt(0).layout(0, 0, width, height)
            if (childCount > 1) getChildAt(1).layout(0, 0, 0, 0)
            return
        }
        val split = resolveSplit(width, height)
        getChildAt(0).layout(split.first.left, split.first.top, split.first.right, split.first.bottom)
        getChildAt(1).layout(split.second.left, split.second.top, split.second.right, split.second.bottom)
    }

    private fun resolveSplit(width: Int, height: Int): Pair<Rect, Rect> {
        if (arrangement != PaneArrangement.AUTO) {
            return resolveForcedSplit(width, height)
        }
        val hinge = foldBounds
        if (foldSeparating && hinge != null && foldOrientation == FoldingFeature.Orientation.VERTICAL) {
            val split = hinge.centerX().takeIf { it in 1 until width } ?: width / 2
            val gapLeft = hinge.left.coerceIn(1, width - 1)
            val gapRight = hinge.right.coerceIn(gapLeft, width - 1)
            return Rect(0, 0, if (hinge.width() > 0) gapLeft else split, height) to
                Rect(if (hinge.width() > 0) gapRight else split, 0, width, height)
        }
        if (foldSeparating && hinge != null && foldOrientation == FoldingFeature.Orientation.HORIZONTAL) {
            val split = hinge.centerY().takeIf { it in 1 until height } ?: height / 2
            val gapTop = hinge.top.coerceIn(1, height - 1)
            val gapBottom = hinge.bottom.coerceIn(gapTop, height - 1)
            return Rect(0, 0, width, if (hinge.height() > 0) gapTop else split) to
                Rect(0, if (hinge.height() > 0) gapBottom else split, width, height)
        }

        val wide = width >= dp(600) && width > height * 1.08f
        return if (wide) {
            val splitX = (width * 0.56f).toInt()
            Rect(0, 0, splitX, height) to Rect(splitX, 0, width, height)
        } else {
            val splitY = (height * 0.53f).toInt()
            Rect(0, 0, width, splitY) to Rect(0, splitY, width, height)
        }
    }

    private fun resolveForcedSplit(width: Int, height: Int): Pair<Rect, Rect> =
        when (arrangement) {
            PaneArrangement.GAME_LEFT,
            PaneArrangement.DASHBOARD_LEFT -> {
                val dashboardFirst = arrangement == PaneArrangement.DASHBOARD_LEFT
                val hinge = foldBounds?.takeIf {
                    foldSeparating && foldOrientation == FoldingFeature.Orientation.VERTICAL
                }
                val split = hinge?.centerX()?.coerceIn(1, width - 1)
                    ?: (width * if (dashboardFirst) 0.44f else 0.56f).toInt()
                val leftEnd = hinge?.left?.coerceIn(1, width - 1) ?: split
                val rightStart = hinge?.right?.coerceIn(leftEnd, width - 1) ?: split
                val first = Rect(0, 0, leftEnd, height)
                val second = Rect(rightStart, 0, width, height)
                if (dashboardFirst) second to first else first to second
            }
            PaneArrangement.GAME_TOP,
            PaneArrangement.DASHBOARD_TOP -> {
                val dashboardFirst = arrangement == PaneArrangement.DASHBOARD_TOP
                val hinge = foldBounds?.takeIf {
                    foldSeparating && foldOrientation == FoldingFeature.Orientation.HORIZONTAL
                }
                val split = hinge?.centerY()?.coerceIn(1, height - 1)
                    ?: (height * if (dashboardFirst) 0.47f else 0.53f).toInt()
                val topEnd = hinge?.top?.coerceIn(1, height - 1) ?: split
                val bottomStart = hinge?.bottom?.coerceIn(topEnd, height - 1) ?: split
                val first = Rect(0, 0, width, topEnd)
                val second = Rect(0, bottomStart, width, height)
                if (dashboardFirst) second to first else first to second
            }
            PaneArrangement.AUTO -> error("La distribución automática se resuelve antes")
        }

    private fun measureExact(child: View, width: Int, height: Int) {
        child.measure(
            MeasureSpec.makeMeasureSpec(width.coerceAtLeast(0), MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(height.coerceAtLeast(0), MeasureSpec.EXACTLY)
        )
    }

    private fun shouldShowDashboard(width: Int): Boolean = PanePolicy.shouldShowDashboard(
        widthPx = width,
        density = resources.displayMetrics.density,
        externalDashboardActive = externalDashboardActive
    )

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
