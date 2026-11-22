package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Wrangler OBD", appName)
    }

    @Test
    fun `navigate tabs and search JK DTC encyclopedia`() {
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("ecu_connect_button").assertIsDisplayed()

        // Navigate to DTC Scan tab and filter for P0456
        composeRule.onNodeWithTag("nav_tab_scanner").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("dtc_search_input").performTextInput("P0456")
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("dtc_card_P0456").assertIsDisplayed()

        // Navigate to Terminal tab
        composeRule.onNodeWithTag("nav_tab_terminal").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("ELM327 AT / HEX TERMINAL").assertIsDisplayed()

        // Navigate to Garage tab
        composeRule.onNodeWithTag("nav_tab_garage").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("save_rig_profile_button").assertIsDisplayed()
    }
}
