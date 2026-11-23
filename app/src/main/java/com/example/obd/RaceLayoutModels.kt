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
        RaceWindowId.SHIFT_LIGHTS to RaceWindowGeometry(
            xFraction = 0.025f,
            yDp = 8f,
            widthFraction = 0.95f,
            heightDp = 72f,
            minWidthDp = 160f,
            minHeightDp = 58f,
            maxHeightDp = 160f,
            zOrder = 1
        ),
        RaceWindowId.FUEL_BAR to RaceWindowGeometry(
            xFraction = 0.025f,
            yDp = 88f,
            widthFraction = 0.185f,
            heightDp = 248f,
            minWidthDp = 56f,
            minHeightDp = 120f,
            maxHeightDp = 420f,
            zOrder = 2
        ),
        RaceWindowId.SPEED_DIAL to RaceWindowGeometry(
            xFraction = 0.23f,
            yDp = 88f,
            widthFraction = 0.54f,
            heightDp = 248f,
            minWidthDp = 135f,
            minHeightDp = 150f,
            maxHeightDp = 440f,
            zOrder = 3
        ),
        RaceWindowId.COOLANT_BAR to RaceWindowGeometry(
            xFraction = 0.79f,
            yDp = 88f,
            widthFraction = 0.185f,
            heightDp = 248f,
            minWidthDp = 56f,
            minHeightDp = 120f,
            maxHeightDp = 420f,
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
