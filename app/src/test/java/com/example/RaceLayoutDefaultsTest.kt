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
        // Speed dial fits beside widened Fuel bar, while Coolant bar wraps cleanly below
        assertEquals(8f, resolved[RaceWindowId.SPEED_DIAL]!!.yDp, 0.01f)
        assertTrue(resolved[RaceWindowId.COOLANT_BAR]!!.yDp >= 256f)
    }

    @Test
    fun `moving fuel bar between speed dial and coolant bar auto-organizes the 3-box line cleanly`() {
        val baseline = RaceLayoutDefaults.defaultLayout()
        // Drag Fuel bar rightward onto the right half of Speed dial with slight Y wobble (18dp)
        val movedFuel = baseline[RaceWindowId.FUEL_BAR]!!.copy(xFraction = 0.56f, yDp = 18f)

        val resolved = RaceLayoutSolver.resolveOverlaps(
            baseline = baseline,
            activeId = RaceWindowId.FUEL_BAR,
            activeGeometry = movedFuel,
            boardWidthDp = 400f,
            snapActiveToRow = true
        )

        assertFalse(RaceLayoutSolver.hasAnyOverlap(resolved, boardWidthDp = 400f))
        val speed = resolved[RaceWindowId.SPEED_DIAL]!!
        val fuel = resolved[RaceWindowId.FUEL_BAR]!!
        val coolant = resolved[RaceWindowId.COOLANT_BAR]!!

        // All 3 boxes stay aligned on y = 8dp in the new left-to-right order [SPEED, FUEL, COOLANT]
        assertEquals(8f, speed.yDp, 0.01f)
        assertEquals(8f, fuel.yDp, 0.01f)
        assertEquals(8f, coolant.yDp, 0.01f)
        assertTrue(speed.xFraction + speed.widthFraction < fuel.xFraction)
        assertTrue(fuel.xFraction + fuel.widthFraction < coolant.xFraction)
    }

    @Test
    fun `dragging rpm pod to far right reorders 3-box pod line and snaps to clean row alignment`() {
        val baseline = RaceLayoutDefaults.defaultLayout()
        val movedRpm = baseline[RaceWindowId.RPM_POD]!!.copy(xFraction = 0.72f, yDp = 432f)

        val resolved = RaceLayoutSolver.resolveOverlaps(
            baseline = baseline,
            activeId = RaceWindowId.RPM_POD,
            activeGeometry = movedRpm,
            boardWidthDp = 400f,
            snapActiveToRow = true
        )

        assertFalse(RaceLayoutSolver.hasAnyOverlap(resolved, boardWidthDp = 400f))
        val map = resolved[RaceWindowId.MAP_POD]!!
        val load = resolved[RaceWindowId.LOAD_POD]!!
        val rpm = resolved[RaceWindowId.RPM_POD]!!

        assertEquals(418f, map.yDp, 0.01f)
        assertEquals(418f, load.yDp, 0.01f)
        assertEquals(418f, rpm.yDp, 0.01f)
        assertTrue(map.xFraction < load.xFraction && load.xFraction < rpm.xFraction)
    }

    @Test
    fun `moving throttle bar down compacts below rows upward so no dead vertical gap remains`() {
        val baseline = RaceLayoutDefaults.defaultLayout()
        val movedThrottle = baseline[RaceWindowId.THROTTLE_BAR]!!.copy(yDp = 700f)

        val resolved = RaceLayoutSolver.resolveOverlaps(
            baseline = baseline,
            activeId = RaceWindowId.THROTTLE_BAR,
            activeGeometry = movedThrottle,
            boardWidthDp = 400f,
            snapActiveToRow = true
        )

        assertFalse(RaceLayoutSolver.hasAnyOverlap(resolved, boardWidthDp = 400f))
        // RPM/MAP/LOAD pods compact up cleanly right below Shift Lights (bottom 336 + 8 = 344)
        assertEquals(344f, resolved[RaceWindowId.RPM_POD]!!.yDp, 0.01f)
        assertEquals(344f, resolved[RaceWindowId.MAP_POD]!!.yDp, 0.01f)
        assertEquals(344f, resolved[RaceWindowId.LOAD_POD]!!.yDp, 0.01f)
    }
}
