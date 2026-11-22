package com.example

import com.example.obd.RaceLayoutDefaults
import com.example.obd.RaceWindowId
import org.junit.Assert.assertEquals
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
}
