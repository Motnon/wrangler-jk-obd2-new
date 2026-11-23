package com.example

import com.example.obd.AppLanguage
import com.example.obd.DiagnosticCategory
import com.example.obd.DiagnosticReadingStatus
import com.example.obd.DiagnosticTestId
import com.example.obd.JkEngineProfile
import com.example.obd.JkSubsystem
import com.example.obd.WranglerModel
import com.example.ui.i18n.AppStrings
import com.example.ui.i18n.DiagnosticStrings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalizationTest {

    @Test
    fun appStrings_switchBetweenEnglishAndHebrew() {
        val en = AppStrings.forLanguage(AppLanguage.ENGLISH)
        val he = AppStrings.forLanguage(AppLanguage.HEBREW)

        assertEquals(AppLanguage.ENGLISH, en.language)
        assertEquals(AppLanguage.HEBREW, he.language)
        assertNotEquals(en.languageCardHeader, he.languageCardHeader)
        assertNotEquals(en.speedUnitKph, he.speedUnitKph)
        assertNotEquals(en.vinPrefix, he.vinPrefix)
        assertTrue(he.languageActiveSummary.contains("עברית"))
    }

    @Test
    fun diagnosticStrings_coverAll50TestsAnd6CategoriesInHebrew() {
        DiagnosticCategory.entries.forEach { category ->
            val heCat = DiagnosticStrings.categoryTitle(category, AppLanguage.HEBREW)
            assertTrue("Missing Hebrew category title for $category", heCat.isNotBlank())
            assertNotEquals("Untranslated category $category", category.title, heCat)
        }

        assertEquals(50, DiagnosticTestId.entries.size)
        DiagnosticTestId.entries.forEach { test ->
            val heTitle = DiagnosticStrings.testTitle(test, AppLanguage.HEBREW)
            assertTrue("Missing Hebrew title for $test", heTitle.isNotBlank())
            assertNotEquals("Untranslated test $test", test.title, heTitle)
        }

        DiagnosticReadingStatus.entries.forEach { status ->
            assertTrue(DiagnosticStrings.statusBadge(status, isAlert = false, lang = AppLanguage.HEBREW).isNotBlank())
            assertTrue(DiagnosticStrings.statusBadge(status, isAlert = true, lang = AppLanguage.HEBREW).isNotBlank())
        }
    }

    @Test
    fun appStrings_coverAllModelsEnginesAndSubsystemsInEnglishAndHebrew() {
        val en = AppStrings.forLanguage(AppLanguage.ENGLISH)
        val he = AppStrings.forLanguage(AppLanguage.HEBREW)

        WranglerModel.entries.forEach { model ->
            assertTrue(en.modelChipLabel(model).contains(model.code))
            assertTrue(he.modelChipLabel(model).contains(model.code))
        }

        JkEngineProfile.entries.forEach { profile ->
            assertTrue(en.engineChipLabel(profile).isNotBlank())
            assertTrue(he.engineChipLabel(profile).isNotBlank())
        }

        assertEquals("TIPM / CAN", en.subsystemLabel(JkSubsystem.TIPM_NETWORK, WranglerModel.JK))
        assertEquals("BCM / SGW / CAN", en.subsystemLabel(JkSubsystem.TIPM_NETWORK, WranglerModel.JL))
        assertEquals("BCM / SGW / CAN", he.subsystemLabel(JkSubsystem.TIPM_NETWORK, WranglerModel.JL))
    }
}
