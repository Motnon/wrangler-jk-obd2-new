package com.example

import com.example.obd.RaceLayoutDefaults
import com.example.obd.RaceLayoutSolver
import com.example.obd.RaceWindowId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RaceLayoutDefaultsTest {

    @Test
    fun `encode and decode preserves adjusted window geometry`() {
        val original = RaceLayoutDefaults.defaultLayout().toMutableMap()
        val speed = original[RaceWindowId.SPEED_DIAL]!!
        original[RaceWindowId.SPEED_DIAL] = speed.copy(
            xFraction = 0.12f,
            yDp = 140f,
            widthFraction = 0.65f,
            heightDp = 290f,
            zOrder = 25
        )

        val encoded = RaceLayoutDefaults.encode(original)
        val decoded = RaceLayoutDefaults.decode(encoded)

        val restored = decoded[RaceWindowId.SPEED_DIAL]!!
        assertEquals(0.12f, restored.xFraction, 0.001f)
        assertEquals(140f, restored.yDp, 0.01f)
        assertEquals(0.65f, restored.widthFraction, 0.001f)
        assertEquals(290f, restored.heightDp, 0.01f)
        assertEquals(25, restored.zOrder)
    }

    @Test
    fun `decode clamps out of bounds positions and dimensions`() {
        val malformed = "fuel_bar:1.5,-40.0,0.2,900.0,3"
        val decoded = RaceLayoutDefaults.decode(malformed)
        val fuel = decoded[RaceWindowId.FUEL_BAR]!!

        assertTrue(fuel.xFraction in 0f..(1f - fuel.widthFraction))
        assertEquals(0f, fuel.yDp, 0.01f)
        assertEquals(fuel.maxHeightDp, fuel.heightDp, 0.01f)
    }

    @Test
    fun `default layout has zero overlapping windows and places shift lights below speed dial`() {
        val defaults = RaceLayoutDefaults.defaultLayout()
        assertFalse(RaceLayoutSolver.hasAnyOverlap(defaults, boardWidthDp = 400f))
        val speed = defaults[RaceWindowId.SPEED_DIAL]!!
        val shift = defaults[RaceWindowId.SHIFT_LIGHTS]!!
        assertTrue(shift.yDp >= speed.yDp + speed.heightDp)
    }

    @Test
    fun `moving speed dial onto throttle bar automatically pushes boxes without overlap`() {
        val baseline = RaceLayoutDefaults.defaultLayout()
        val movedSpeed = baseline[RaceWindowId.SPEED_DIAL]!!.copy(yDp = 240f)

        val resolved = RaceLayoutSolver.resolveOverlaps(
            baseline = baseline,
            activeId = RaceWindowId.SPEED_DIAL,
            activeGeometry = movedSpeed,
            boardWidthDp = 400f
        )

        assertFalse(RaceLayoutSolver.hasAnyOverlap(resolved, boardWidthDp = 400f))
        assertEquals(240f, resolved[RaceWindowId.SPEED_DIAL]!!.yDp, 0.01f)
        assertTrue(
            resolved[RaceWindowId.THROTTLE_BAR]!!.yDp >=
                resolved[RaceWindowId.SPEED_DIAL]!!.yDp + resolved[RaceWindowId.SPEED_DIAL]!!.heightDp
        )
    }

    @Test
    fun `dragging rpm pod onto map pod cleanly swaps them without overlap`() {
        val baseline = RaceLayoutDefaults.defaultLayout()
        val originalRpm = baseline[RaceWindowId.RPM_POD]!!
        val originalMap = baseline[RaceWindowId.MAP_POD]!!
        val movedRpm = originalRpm.copy(xFraction = originalMap.xFraction, yDp = originalMap.yDp)

        val resolved = RaceLayoutSolver.resolveOverlaps(
            baseline = baseline,
            activeId = RaceWindowId.RPM_POD,
            activeGeometry = movedRpm,
            boardWidthDp = 400f
        )

        assertFalse(RaceLayoutSolver.hasAnyOverlap(resolved, boardWidthDp = 400f))
        val newMap = resolved[RaceWindowId.MAP_POD]!!
        assertEquals(originalRpm.xFraction, newMap.xFraction, 0.01f)
        assertEquals(originalRpm.yDp, newMap.yDp, 0.01f)
    }

    @Test
    fun `resizing fuel bar wider automatically shifts neighboring boxes without overlap`() {
        val baseline = RaceLayoutDefaults.defaultLayout()
        val widenedFuel = baseline[RaceWindowId.FUEL_BAR]!!.copy(widthFraction = 0.36f)

        val resolved = RaceLayoutSolver.resolveOverlaps(
            baseline = baseline,
            activeId = RaceWindowId.FUEL_BAR,
            activeGeometry = widenedFuel,
            boardWidthDp = 400f
        )

        assertFalse(RaceLayoutSolver.hasAnyOverlap(resolved, boardWidthDp = 400f))
        assertEquals(0.36f, resolved[RaceWindowId.FUEL_BAR]!!.widthFraction, 0.001f)
    }
}
