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
    TRIP_ODOMETER("trip_odometer", "Trip Odometer"),
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
        RaceWindowId.TRIP_ODOMETER to RaceWindowGeometry(
            xFraction = 0.025f,
            yDp = 768f,
            widthFraction = 0.95f,
            heightDp = 136f,
            minWidthDp = 180f,
            minHeightDp = 112f,
            maxHeightDp = 280f,
            zOrder = 10
        ),
        RaceWindowId.INCLINOMETER to RaceWindowGeometry(
            xFraction = 0.025f,
            yDp = 912f,
            widthFraction = 0.95f,
            heightDp = 284f,
            minWidthDp = 200f,
            minHeightDp = 210f,
            maxHeightDp = 420f,
            zOrder = 11
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
        val decodedIds = mutableSetOf<RaceWindowId>()
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
            decodedIds.add(id)
        }
        if (decodedIds.isNotEmpty() && RaceWindowId.TRIP_ODOMETER !in decodedIds) {
            val maxBottom = decodedIds.mapNotNull { base[it] }.maxOfOrNull { it.yDp + it.heightDp } ?: 760f
            val tripTemplate = base[RaceWindowId.TRIP_ODOMETER]!!
            val placedTrip = tripTemplate.copy(yDp = maxBottom + 8f)
            return RaceLayoutSolver.resolveOverlaps(
                baseline = base,
                activeId = RaceWindowId.TRIP_ODOMETER,
                activeGeometry = placedTrip
            )
        }
        return base
    }
}

/**
 * On-device CPU automatic layout organizer for adjustable dashboard gauge windows.
 * - Detects horizontal rows (including 3-box lines like Fuel/Speed/Coolant or RPM/MAP/Load)
 *   and automatically reorders, centers, and aligns gaps when any box is moved or resized.
 * - Automatically wraps overflowing boxes cleanly to the next line and compacts dead vertical gaps.
 * - Mathematically guarantees zero pairwise overlap across all windows.
 */
object RaceLayoutSolver {
    private const val HORIZONTAL_MARGIN = 0.025f
    private const val USABLE_WIDTH = 0.95f
    private const val VERTICAL_GAP_DP = 8f
    private const val MIN_Y_DP = 8f
    private const val MAX_Y_DP = 2400f
    private const val MAX_ROW_FIT_WIDTH = 0.965f

    fun gapXForBoard(boardWidthDp: Float): Float {
        val safeBoardDp = boardWidthDp.coerceAtLeast(240f)
        return (8f / safeBoardDp).coerceIn(0.012f, 0.020f)
    }

    fun overlapsX(a: RaceWindowGeometry, b: RaceWindowGeometry, gapX: Float): Boolean {
        return a.xFraction < (b.xFraction + b.widthFraction + gapX - 0.0005f) &&
            b.xFraction < (a.xFraction + a.widthFraction + gapX - 0.0005f)
    }

    fun overlaps(
        a: RaceWindowGeometry,
        b: RaceWindowGeometry,
        gapX: Float,
        gapY: Float = VERTICAL_GAP_DP
    ): Boolean {
        val hitX = overlapsX(a, b, gapX)
        val hitY = a.yDp < (b.yDp + b.heightDp + gapY - 0.5f) &&
            b.yDp < (a.yDp + a.heightDp + gapY - 0.5f)
        return hitX && hitY
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
        boardWidthDp: Float = 400f,
        snapActiveToRow: Boolean = true
    ): Map<RaceWindowId, RaceWindowGeometry> {
        val gapX = gapXForBoard(boardWidthDp)
        val minY = if (snapActiveToRow) MIN_Y_DP else 0f
        val clampedActive = activeGeometry.copy(
            xFraction = activeGeometry.xFraction.coerceIn(
                0f,
                (1f - activeGeometry.widthFraction).coerceAtLeast(0f)
            ),
            yDp = activeGeometry.yDp.coerceIn(minY, MAX_Y_DP)
        )
        val baselineActive = baseline[activeId]

        // 1) Group non-active baseline boxes into horizontal row bands
        val otherEntries = baseline.entries
            .filter { it.key != activeId }
            .sortedWith(
                compareBy<Map.Entry<RaceWindowId, RaceWindowGeometry>> { it.value.yDp }
                    .thenBy { it.value.xFraction }
            )

        val baselineRows = mutableListOf<MutableList<Pair<RaceWindowId, RaceWindowGeometry>>>()
        for (entry in otherEntries) {
            val existingRow = baselineRows.firstOrNull { row ->
                val anchor = row.first().second
                kotlin.math.abs(entry.value.yDp - anchor.yDp) <= 22f &&
                    row.none { overlapsX(it.second, entry.value, gapX * 0.5f) }
            }
            if (existingRow != null) {
                existingRow.add(entry.key to entry.value)
            } else {
                baselineRows.add(mutableListOf(entry.key to entry.value))
            }
        }

        // 2) Check if activeId belongs on / is being dragged into a multi-box row band
        val matchingRowIndex = findMatchingRowIndex(
            rows = baselineRows,
            active = clampedActive
        )

        val resolved = LinkedHashMap<RaceWindowId, RaceWindowGeometry>(baseline.size)
        val locked = mutableListOf<RaceWindowGeometry>()
        val pendingShelves = mutableListOf<List<Pair<RaceWindowId, RaceWindowGeometry>>>()

        if (matchingRowIndex >= 0) {
            val targetRow = baselineRows[matchingRowIndex]
            val rowY = targetRow.minOf { it.second.yDp }.coerceAtLeast(MIN_Y_DP)
            val combined = (targetRow + (activeId to clampedActive))
                .sortedBy { (id, geom) ->
                    val center = geom.xFraction + geom.widthFraction / 2f
                    if (id == activeId && baselineActive != null) {
                        val baseCenter = baselineActive.xFraction + baselineActive.widthFraction / 2f
                        when {
                            center > baseCenter + 0.01f -> center + 0.02f
                            center < baseCenter - 0.01f -> center - 0.02f
                            else -> center
                        }
                    } else {
                        center
                    }
                }

            if (canFitOnSingleLine(combined.map { it.second }, gapX)) {
                // Entire row (2, 3, or N boxes) fits on one line — reorder & evenly space!
                val packed = packRowSlots(combined, rowY)
                for ((id, geom) in combined) {
                    val finalGeom = if (id == activeId && !snapActiveToRow) {
                        clampedActive
                    } else {
                        packed[id] ?: geom
                    }
                    resolved[id] = finalGeom
                    locked.add(finalGeom)
                }

                // Queue all remaining rows as pending shelves
                for (i in baselineRows.indices) {
                    if (i != matchingRowIndex) {
                        pendingShelves.add(baselineRows[i])
                    }
                }
            } else {
                // Overflow on the line (e.g. box resized wider or full-width box dropped onto 3-box row)
                val wasResizedWiderInRow = baselineActive != null &&
                    kotlin.math.abs(baselineActive.yDp - rowY) <= 22f &&
                    clampedActive.widthFraction > baselineActive.widthFraction + 0.01f

                if (wasResizedWiderInRow) {
                    // Keep activeId + as many ordered line-mates as still fit; wrap overflow cleanly below
                    val kept = mutableListOf(activeId to clampedActive)
                    val overflow = mutableListOf<Pair<RaceWindowId, RaceWindowGeometry>>()
                    for (peer in targetRow) {
                        val candidateList = (kept + peer)
                            .sortedBy { it.second.xFraction + it.second.widthFraction / 2f }
                        if (canFitOnSingleLine(candidateList.map { it.second }, gapX)) {
                            kept.clear()
                            kept.addAll(candidateList)
                        } else {
                            overflow.add(peer)
                        }
                    }
                    val packedKept = packRowSlots(kept, rowY)
                    for ((id, geom) in kept) {
                        val finalGeom = if (id == activeId && !snapActiveToRow) {
                            clampedActive
                        } else {
                            packedKept[id] ?: geom
                        }
                        resolved[id] = finalGeom
                        locked.add(finalGeom)
                    }
                    if (overflow.isNotEmpty()) {
                        val packedOverflow = packRowSlots(
                            overflow.sortedBy { it.second.xFraction },
                            rowY + kept.maxOf { it.second.heightDp } + VERTICAL_GAP_DP
                        )
                        pendingShelves.add(overflow.map { it.first to (packedOverflow[it.first] ?: it.second) })
                    }
                    for (i in baselineRows.indices) {
                        if (i != matchingRowIndex) {
                            pendingShelves.add(baselineRows[i])
                        }
                    }
                } else {
                    // Active box claims its own shelf; existing multi-box row stays grouped cleanly
                    resolved[activeId] = clampedActive
                    locked.add(clampedActive)
                    for (row in baselineRows) {
                        pendingShelves.add(row)
                    }
                }
            }
        } else {
            // Active box is on its own shelf (not joining a baseline row)
            val activePlaced = if (snapActiveToRow) {
                clampedActive.copy(
                    xFraction = snapSingleBoxX(clampedActive, otherEntries.map { it.value }),
                    yDp = clampedActive.yDp.coerceAtLeast(MIN_Y_DP)
                )
            } else {
                clampedActive
            }
            resolved[activeId] = activePlaced
            locked.add(activePlaced)
            for (row in baselineRows) {
                pendingShelves.add(row)
            }
        }

        // 3) Re-center/tidy any multi-box row whose box was removed (e.g. 3 boxes -> 2 boxes)
        val normalizedShelves = pendingShelves
            .map { shelf ->
                val rowY = shelf.minOf { it.second.yDp }.coerceAtLeast(MIN_Y_DP)
                if (shelf.size >= 2 && canFitOnSingleLine(shelf.map { it.second }, gapX)) {
                    val ordered = shelf.sortedBy { it.second.xFraction + it.second.widthFraction / 2f }
                    val packed = packRowSlots(ordered, rowY)
                    ordered.map { it.first to (packed[it.first] ?: it.second) }
                } else {
                    shelf.sortedBy { it.second.xFraction }
                }
            }
            .sortedBy { shelf -> shelf.minOf { it.second.yDp } }

        // 4) Place each pending shelf with upward dead-gap compaction + zero-overlap resolution
        for (shelf in normalizedShelves) {
            if (shelf.size >= 2 && canPlaceShelfTogether(shelf, locked, gapX)) {
                val placedShelf = placeShelfWithoutOverlap(shelf, locked, gapX)
                for ((id, geom) in placedShelf) {
                    resolved[id] = geom
                    locked.add(geom)
                }
            } else {
                // Place items individually if shelf cannot stay intact around activeId
                for ((id, item) in shelf) {
                    val placed = placeSingleBoxWithoutOverlap(
                        box = item,
                        locked = locked,
                        gapX = gapX
                    )
                    resolved[id] = placed
                    locked.add(placed)
                }
            }
        }

        // 5) Final safety guarantee: ensure zero pairwise overlap even in extreme resize cases
        return guaranteeZeroOverlap(resolved, activeId, gapX)
    }

    private fun findMatchingRowIndex(
        rows: List<List<Pair<RaceWindowId, RaceWindowGeometry>>>,
        active: RaceWindowGeometry
    ): Int {
        var bestIdx = -1
        var bestDiff = Float.MAX_VALUE
        val activeBottom = active.yDp + active.heightDp

        for (i in rows.indices) {
            val row = rows[i]
            val rowY = row.minOf { it.second.yDp }
            val rowH = row.maxOf { it.second.heightDp }
            val rowBottom = rowY + rowH
            val threshold = (minOf(active.heightDp, rowH) * 0.45f).coerceIn(28f, 90f)
            val dy = kotlin.math.abs(active.yDp - rowY)
            val verticalOverlap = active.yDp < (rowBottom + 16f) && activeBottom > (rowY - 16f)
            if (dy <= threshold && verticalOverlap && dy < bestDiff) {
                bestDiff = dy
                bestIdx = i
            }
        }
        return bestIdx
    }

    private fun canFitOnSingleLine(items: List<RaceWindowGeometry>, gapX: Float): Boolean {
        if (items.size <= 1) return true
        val sumW = items.sumOf { it.widthFraction.toDouble() }.toFloat()
        val minTotal = sumW + gapX * (items.size - 1)
        return minTotal <= MAX_ROW_FIT_WIDTH
    }

    private fun packRowSlots(
        orderedItems: List<Pair<RaceWindowId, RaceWindowGeometry>>,
        rowY: Float
    ): Map<RaceWindowId, RaceWindowGeometry> {
        val result = LinkedHashMap<RaceWindowId, RaceWindowGeometry>(orderedItems.size)
        if (orderedItems.isEmpty()) return result
        if (orderedItems.size == 1) {
            val (id, g) = orderedItems.first()
            val safeX = g.xFraction.coerceIn(0f, (1f - g.widthFraction).coerceAtLeast(0f))
            result[id] = g.copy(xFraction = safeX, yDp = rowY)
            return result
        }
        val totalW = orderedItems.sumOf { it.second.widthFraction.toDouble() }.toFloat()
        val rawGap = (USABLE_WIDTH - totalW) / (orderedItems.size - 1).toFloat()
        val gap = rawGap.coerceIn(0.014f, 0.028f)
        val occupied = totalW + gap * (orderedItems.size - 1)
        var cursorX = if (occupied <= USABLE_WIDTH + 0.002f) {
            HORIZONTAL_MARGIN + ((USABLE_WIDTH - occupied) / 2f).coerceAtLeast(0f)
        } else {
            ((1f - occupied) / 2f).coerceAtLeast(0f)
        }
        for ((id, g) in orderedItems) {
            val safeX = cursorX.coerceIn(0f, (1f - g.widthFraction).coerceAtLeast(0f))
            result[id] = g.copy(xFraction = safeX, yDp = rowY)
            cursorX = safeX + g.widthFraction + gap
        }
        return result
    }

    private fun snapSingleBoxX(
        box: RaceWindowGeometry,
        peers: List<RaceWindowGeometry>
    ): Float {
        val maxX = (1f - box.widthFraction).coerceAtLeast(0f)
        if (box.widthFraction >= 0.90f) {
            return HORIZONTAL_MARGIN.coerceIn(0f, maxX)
        }
        if (kotlin.math.abs(box.xFraction - HORIZONTAL_MARGIN) <= 0.02f) {
            return HORIZONTAL_MARGIN.coerceIn(0f, maxX)
        }
        val rightAlignedX = (1f - HORIZONTAL_MARGIN - box.widthFraction).coerceIn(0f, maxX)
        if (kotlin.math.abs(box.xFraction - rightAlignedX) <= 0.02f) {
            return rightAlignedX
        }
        for (peer in peers) {
            if (kotlin.math.abs(box.xFraction - peer.xFraction) <= 0.018f &&
                peer.xFraction <= maxX
            ) {
                return peer.xFraction
            }
        }
        return box.xFraction.coerceIn(0f, maxX)
    }

    private fun canPlaceShelfTogether(
        shelf: List<Pair<RaceWindowId, RaceWindowGeometry>>,
        locked: List<RaceWindowGeometry>,
        gapX: Float
    ): Boolean {
        // Check if pushing the shelf horizontally intact avoids locking conflicts or can shift cleanly below/above
        val shelfMinX = shelf.minOf { it.second.xFraction }
        val shelfMaxR = shelf.maxOf { it.second.xFraction + it.second.widthFraction }
        // If only full-width or clear below/above locked boxes, it can always stay together!
        return shelfMaxR - shelfMinX <= 1.0f
    }

    private fun placeShelfWithoutOverlap(
        shelf: List<Pair<RaceWindowId, RaceWindowGeometry>>,
        locked: List<RaceWindowGeometry>,
        gapX: Float
    ): List<Pair<RaceWindowId, RaceWindowGeometry>> {
        val baseY = shelf.minOf { it.second.yDp }.coerceAtLeast(MIN_Y_DP)

        // Compact upward to eliminate dead vertical gaps left behind when another box moved away
        val ceilingY = shelf.maxOf { (_, item) ->
            locked.filter { overlapsX(item, it, gapX) && it.yDp < baseY }
                .maxOfOrNull { it.yDp + it.heightDp + VERTICAL_GAP_DP }
                ?: MIN_Y_DP
        }
        var candidateY = ceilingY.coerceAtLeast(MIN_Y_DP)

        var guard = 0
        while (guard++ < 40) {
            val shifted = shelf.map { (id, g) -> id to g.copy(yDp = candidateY) }
            val hits = shifted.flatMap { (_, g) ->
                locked.filter { overlaps(g, it, gapX, VERTICAL_GAP_DP) }
            }
            if (hits.isEmpty()) return shifted
            val nextY = hits.maxOf { it.yDp + it.heightDp + VERTICAL_GAP_DP }
            candidateY = if (nextY > candidateY) nextY else candidateY + VERTICAL_GAP_DP
        }
        return shelf.map { (id, g) -> id to g.copy(yDp = candidateY.coerceAtMost(MAX_Y_DP)) }
    }

    private fun placeSingleBoxWithoutOverlap(
        box: RaceWindowGeometry,
        locked: List<RaceWindowGeometry>,
        gapX: Float
    ): RaceWindowGeometry {
        val ceilingY = locked
            .filter { overlapsX(box, it, gapX) && it.yDp < box.yDp }
            .maxOfOrNull { it.yDp + it.heightDp + VERTICAL_GAP_DP }
            ?: MIN_Y_DP

        val compactStart = box.copy(yDp = ceilingY.coerceAtLeast(MIN_Y_DP))
        val directHits = locked.filter { overlaps(compactStart, it, gapX, VERTICAL_GAP_DP) }
        if (directHits.isEmpty()) return compactStart

        val maxValidX = (1f - box.widthFraction).coerceAtLeast(0f)
        val candidates = mutableListOf<RaceWindowGeometry>()

        for (blocker in directHits) {
            val leftX = blocker.xFraction - gapX - box.widthFraction
            if (leftX >= HORIZONTAL_MARGIN - 0.002f) {
                candidates.add(compactStart.copy(xFraction = leftX.coerceIn(0f, maxValidX)))
            }
            val rightX = blocker.xFraction + blocker.widthFraction + gapX
            if (rightX + box.widthFraction <= 1f - HORIZONTAL_MARGIN + 0.002f) {
                candidates.add(compactStart.copy(xFraction = rightX.coerceIn(0f, maxValidX)))
            }
        }
        candidates.add(pushDownUntilClear(compactStart, locked, gapX))

        val validCandidates = candidates.map { cand ->
            if (locked.none { overlaps(cand, it, gapX, VERTICAL_GAP_DP) }) {
                cand
            } else {
                pushDownUntilClear(cand, locked, gapX)
            }
        }
        return validCandidates.minByOrNull { cand ->
            kotlin.math.abs(cand.yDp - compactStart.yDp) +
                kotlin.math.abs(cand.xFraction - compactStart.xFraction) * 320f
        } ?: pushDownUntilClear(compactStart, locked, gapX)
    }

    private fun pushDownUntilClear(
        start: RaceWindowGeometry,
        locked: List<RaceWindowGeometry>,
        gapX: Float
    ): RaceWindowGeometry {
        var currentY = start.yDp.coerceAtLeast(MIN_Y_DP)
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

    private fun guaranteeZeroOverlap(
        input: Map<RaceWindowId, RaceWindowGeometry>,
        activeId: RaceWindowId,
        gapX: Float
    ): Map<RaceWindowId, RaceWindowGeometry> {
        val result = LinkedHashMap<RaceWindowId, RaceWindowGeometry>(input.size)
        val activeGeom = input[activeId]
        val locked = mutableListOf<RaceWindowGeometry>()
        if (activeGeom != null) {
            result[activeId] = activeGeom
            locked.add(activeGeom)
        }
        val others = input.entries
            .filter { it.key != activeId }
            .sortedWith(compareBy<Map.Entry<RaceWindowId, RaceWindowGeometry>> { it.value.yDp }
                .thenBy { it.value.xFraction })

        for ((id, geom) in others) {
            val fixed = if (locked.none { overlaps(geom, it, gapX, VERTICAL_GAP_DP) }) {
                geom
            } else {
                pushDownUntilClear(geom, locked, gapX)
            }
            result[id] = fixed
            locked.add(fixed)
        }
        return result
    }
}

