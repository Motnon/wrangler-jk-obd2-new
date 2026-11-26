package com.example.obd

enum class RaceWindowId(val key: String, val label: String) {
    SHIFT_LIGHTS("shift_lights", "Shift Lights"),
    FUEL_BAR("fuel_bar", "Fuel"),
    SPEED_DIAL("speed_dial", "Speed"),
    COOLANT_BAR("coolant_bar", "Coolant"),
    THROTTLE_BAR("throttle_bar", "Throttle"),
    RPM_POD("rpm_pod", "RPM"),
    MAP_POD("map_pod", "MAP"),
    LOAD_POD("load_pod", "Load"),
    PEAKS_CARD("peaks_card", "Peaks"),
    INCLINOMETER("inclinometer", "Pitch / Roll")
}

data class RaceWindowGeometry(
    val xFraction: Float,
    val yDp: Float,
    val widthFraction: Float,
    val heightDp: Float,
    val minWidthDp: Float,
    val minHeightDp: Float,
    val maxHeightDp: Float = 520f,
    val zOrder: Int = 0
)

object RaceLayoutDefaults {

    fun defaultLayout(): Map<RaceWindowId, RaceWindowGeometry> = linkedMapOf(
        RaceWindowId.FUEL_BAR to RaceWindowGeometry(
            xFraction = 0.025f,
            yDp = 8f,
            widthFraction = 0.185f,
            heightDp = 248f,
            minWidthDp = 56f,
            minHeightDp = 120f,
            maxHeightDp = 420f,
            zOrder = 1
        ),
        RaceWindowId.SPEED_DIAL to RaceWindowGeometry(
            xFraction = 0.23f,
            yDp = 8f,
            widthFraction = 0.54f,
            heightDp = 248f,
            minWidthDp = 135f,
            minHeightDp = 150f,
            maxHeightDp = 440f,
            zOrder = 2
        ),
        RaceWindowId.COOLANT_BAR to RaceWindowGeometry(
            xFraction = 0.79f,
            yDp = 8f,
            widthFraction = 0.185f,
            heightDp = 248f,
            minWidthDp = 56f,
            minHeightDp = 120f,
            maxHeightDp = 420f,
            zOrder = 3
        ),
        RaceWindowId.SHIFT_LIGHTS to RaceWindowGeometry(
            xFraction = 0.025f,
            yDp = 264f,
            widthFraction = 0.95f,
            heightDp = 72f,
            minWidthDp = 160f,
            minHeightDp = 58f,
            maxHeightDp = 160f,
            zOrder = 4
        ),
        RaceWindowId.THROTTLE_BAR to RaceWindowGeometry(
            xFraction = 0.025f,
            yDp = 344f,
            widthFraction = 0.95f,
            heightDp = 66f,
            minWidthDp = 150f,
            minHeightDp = 56f,
            maxHeightDp = 140f,
            zOrder = 5
        ),
        RaceWindowId.RPM_POD to RaceWindowGeometry(
            xFraction = 0.025f,
            yDp = 418f,
            widthFraction = 0.303f,
            heightDp = 148f,
            minWidthDp = 96f,
            minHeightDp = 110f,
            maxHeightDp = 320f,
            zOrder = 6
        ),
        RaceWindowId.MAP_POD to RaceWindowGeometry(
            xFraction = 0.348f,
            yDp = 418f,
            widthFraction = 0.304f,
            heightDp = 148f,
            minWidthDp = 96f,
            minHeightDp = 110f,
            maxHeightDp = 320f,
            zOrder = 7
        ),
        RaceWindowId.LOAD_POD to RaceWindowGeometry(
            xFraction = 0.672f,
            yDp = 418f,
            widthFraction = 0.303f,
            heightDp = 148f,
            minWidthDp = 96f,
            minHeightDp = 110f,
            maxHeightDp = 320f,
            zOrder = 8
        ),
        RaceWindowId.PEAKS_CARD to RaceWindowGeometry(
            xFraction = 0.025f,
            yDp = 574f,
            widthFraction = 0.95f,
            heightDp = 186f,
            minWidthDp = 180f,
            minHeightDp = 135f,
            maxHeightDp = 340f,
            zOrder = 9
        ),
        RaceWindowId.INCLINOMETER to RaceWindowGeometry(
            xFraction = 0.025f,
            yDp = 768f,
            widthFraction = 0.95f,
            heightDp = 284f,
            minWidthDp = 200f,
            minHeightDp = 210f,
            maxHeightDp = 420f,
            zOrder = 10
        )
    )

    fun encode(layout: Map<RaceWindowId, RaceWindowGeometry>): String {
        return layout.entries.joinToString(";") { (id, g) ->
            "${id.key}:${g.xFraction},${g.yDp},${g.widthFraction},${g.heightDp},${g.zOrder}"
        }
    }

    fun decode(raw: String): Map<RaceWindowId, RaceWindowGeometry> {
        val base = defaultLayout().toMutableMap()
        if (raw.isBlank()) return base
        val entries = raw.split(";")
        for (entry in entries) {
            val parts = entry.split(":")
            if (parts.size != 2) continue
            val id = RaceWindowId.entries.firstOrNull { it.key == parts[0] } ?: continue
            val nums = parts[1].split(",")
            if (nums.size < 4) continue
            val x = nums[0].toFloatOrNull() ?: continue
            val y = nums[1].toFloatOrNull() ?: continue
            val w = nums[2].toFloatOrNull() ?: continue
            val h = nums[3].toFloatOrNull() ?: continue
            val z = nums.getOrNull(4)?.toIntOrNull() ?: base[id]!!.zOrder
            val template = base[id]!!
            val safeW = w.coerceIn(0.12f, 1.0f)
            val safeX = x.coerceIn(0f, (1f - safeW).coerceAtLeast(0f))
            val safeY = y.coerceAtLeast(0f)
            val safeH = h.coerceIn(template.minHeightDp, template.maxHeightDp)
            base[id] = template.copy(
                xFraction = safeX,
                yDp = safeY,
                widthFraction = safeW,
                heightDp = safeH,
                zOrder = z
            )
        }
        return base
    }
}

/**
 * Automatic non-overlapping layout solver for adjustable dashboard gauge windows.
 * Whenever the user moves or resizes [activeId], neighboring windows automatically
 * swap into the vacated slot or smoothly push aside horizontally/vertically so no
 * two windows ever overlap on top of each other.
 */
object RaceLayoutSolver {
    private const val HORIZONTAL_MARGIN = 0.025f
    private const val VERTICAL_GAP_DP = 8f
    private const val MIN_Y_DP = 8f
    private const val MAX_Y_DP = 2400f

    fun gapXForBoard(boardWidthDp: Float): Float {
        val safeBoardDp = boardWidthDp.coerceAtLeast(240f)
        return (8f / safeBoardDp).coerceIn(0.012f, 0.020f)
    }

    fun overlaps(
        a: RaceWindowGeometry,
        b: RaceWindowGeometry,
        gapX: Float,
        gapY: Float = VERTICAL_GAP_DP
    ): Boolean {
        val overlapX = a.xFraction < (b.xFraction + b.widthFraction + gapX - 0.0005f) &&
            b.xFraction < (a.xFraction + a.widthFraction + gapX - 0.0005f)
        val overlapY = a.yDp < (b.yDp + b.heightDp + gapY - 0.5f) &&
            b.yDp < (a.yDp + a.heightDp + gapY - 0.5f)
        return overlapX && overlapY
    }

    fun hasAnyOverlap(
        layout: Map<RaceWindowId, RaceWindowGeometry>,
        boardWidthDp: Float = 400f
    ): Boolean {
        val gapX = gapXForBoard(boardWidthDp)
        val items = layout.values.toList()
        for (i in items.indices) {
            for (j in (i + 1) until items.size) {
                if (overlaps(items[i], items[j], gapX, VERTICAL_GAP_DP)) {
                    return true
                }
            }
        }
        return false
    }

    fun resolveOverlaps(
        baseline: Map<RaceWindowId, RaceWindowGeometry>,
        activeId: RaceWindowId,
        activeGeometry: RaceWindowGeometry,
        boardWidthDp: Float = 400f
    ): Map<RaceWindowId, RaceWindowGeometry> {
        val gapX = gapXForBoard(boardWidthDp)
        val safeBoardDp = boardWidthDp.coerceAtLeast(240f)
        val clampedActive = activeGeometry.copy(
            xFraction = activeGeometry.xFraction.coerceIn(
                0f,
                (1f - activeGeometry.widthFraction).coerceAtLeast(0f)
            ),
            yDp = activeGeometry.yDp.coerceIn(0f, MAX_Y_DP)
        )
        val baselineActive = baseline[activeId]

        val resolved = LinkedHashMap<RaceWindowId, RaceWindowGeometry>(baseline.size)
        resolved[activeId] = clampedActive
        val locked = mutableListOf(clampedActive)

        val remaining = baseline.entries
            .filter { it.key != activeId }
            .sortedWith(compareBy<Map.Entry<RaceWindowId, RaceWindowGeometry>> { it.value.yDp }
                .thenBy { it.value.xFraction })

        for (index in remaining.indices) {
            val (id, original) = remaining[index]
            val unplacedTail = remaining.subList(index + 1, remaining.size).map { it.value }

            val directBlockers = locked.filter { overlaps(original, it, gapX, VERTICAL_GAP_DP) }
            if (directBlockers.isEmpty()) {
                resolved[id] = original
                locked.add(original)
                continue
            }

            val candidates = LinkedHashSet<RaceWindowGeometry>()
            val maxValidX = (1f - original.widthFraction).coerceAtLeast(0f)

            // 1) Swap candidate into activeId's vacated baseline slot if it directly hits activeId
            if (baselineActive != null && overlaps(original, clampedActive, gapX, VERTICAL_GAP_DP)) {
                val swapX = baselineActive.xFraction.coerceIn(0f, maxValidX)
                val swapY = baselineActive.yDp.coerceIn(0f, MAX_Y_DP)
                candidates.add(original.copy(xFraction = swapX, yDp = swapY))
            }

            // 2) Horizontal & vertical push candidates around every blocker
            for (blocker in directBlockers) {
                val leftX = blocker.xFraction - gapX - original.widthFraction
                if (leftX >= HORIZONTAL_MARGIN - 0.002f) {
                    candidates.add(original.copy(xFraction = leftX.coerceIn(0f, maxValidX)))
                }

                val rightX = blocker.xFraction + blocker.widthFraction + gapX
                if (rightX + original.widthFraction <= 1f - HORIZONTAL_MARGIN + 0.002f) {
                    candidates.add(original.copy(xFraction = rightX.coerceIn(0f, maxValidX)))
                }

                val aboveY = blocker.yDp - VERTICAL_GAP_DP - original.heightDp
                if (aboveY >= MIN_Y_DP) {
                    candidates.add(original.copy(yDp = aboveY))
                }

                val belowY = blocker.yDp + blocker.heightDp + VERTICAL_GAP_DP
                candidates.add(original.copy(yDp = belowY.coerceAtMost(MAX_Y_DP)))
            }

            // 3) Guaranteed collision-free vertical cascade fallback
            candidates.add(pushDownUntilClear(original, locked, gapX))

            // Keep only strictly collision-free candidates; if horizontal/above collided with a second box,
            // also allow pushing those horizontal candidates down to resolve chains.
            val expandedCandidates = mutableListOf<RaceWindowGeometry>()
            for (cand in candidates) {
                if (locked.none { overlaps(cand, it, gapX, VERTICAL_GAP_DP) }) {
                    expandedCandidates.add(cand)
                } else {
                    expandedCandidates.add(pushDownUntilClear(cand, locked, gapX))
                }
            }

            val best = expandedCandidates.minByOrNull { cand ->
                val dxDp = kotlin.math.abs(cand.xFraction - original.xFraction) * safeBoardDp
                val dyDp = kotlin.math.abs(cand.yDp - original.yDp)
                val futureHits = unplacedTail.count { overlaps(cand, it, gapX, VERTICAL_GAP_DP) }
                val sameRowBonus = if (kotlin.math.abs(cand.yDp - original.yDp) < 2f) -20f else 0f
                val swapBonus = if (
                    baselineActive != null &&
                    kotlin.math.abs(cand.xFraction - baselineActive.xFraction) < 0.02f &&
                    kotlin.math.abs(cand.yDp - baselineActive.yDp) < 4f &&
                    futureHits == 0
                ) -30f else 0f
                dxDp + dyDp + futureHits * 140f + sameRowBonus + swapBonus
            } ?: pushDownUntilClear(original, locked, gapX)

            resolved[id] = best
            locked.add(best)
        }

        return resolved
    }

    private fun pushDownUntilClear(
        start: RaceWindowGeometry,
        locked: List<RaceWindowGeometry>,
        gapX: Float
    ): RaceWindowGeometry {
        var currentY = start.yDp.coerceAtLeast(0f)
        var guard = 0
        while (guard++ < 40) {
            val probe = start.copy(yDp = currentY)
            val hits = locked.filter { overlaps(probe, it, gapX, VERTICAL_GAP_DP) }
            if (hits.isEmpty()) return probe
            val nextY = hits.maxOf { it.yDp + it.heightDp + VERTICAL_GAP_DP }
            currentY = if (nextY > currentY) nextY else currentY + VERTICAL_GAP_DP
        }
        return start.copy(yDp = currentY.coerceAtMost(MAX_Y_DP))
    }
}

