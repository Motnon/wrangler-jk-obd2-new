package com.example.ui.i18n

import com.example.obd.AppLanguage
import com.example.obd.DiagnosticCategory
import com.example.obd.DiagnosticReadingStatus
import com.example.obd.DiagnosticTestId

object DiagnosticStrings {

    fun categoryTitle(category: DiagnosticCategory, lang: AppLanguage): String {
        if (lang == AppLanguage.ENGLISH) return category.title
        return when (category) {
            DiagnosticCategory.TEMPERATURES_PRESSURES -> "1. טמפרטורות ולחצים"
            DiagnosticCategory.THROTTLE_PEDAL -> "2. מערכת מצערת ודוושה (ETC / TPS)"
            DiagnosticCategory.FUEL_OXYGEN -> "3. מערכת דלק וחיישני חמצן (Fuel Trims & O2)"
            DiagnosticCategory.IGNITION_MISFIRES -> "4. הצתה והחטאות בעירה (Misfires)"
            DiagnosticCategory.EMISSIONS_EGR_EVAP -> "5. פליטה ומיחזור גזים (EGR ו-EVAP)"
            DiagnosticCategory.ENGINE_ELECTRICAL_TRIP -> "6. מנוע כללי, חשמל ומוני נסיעה"
        }
    }

    fun testTitle(test: DiagnosticTestId, lang: AppLanguage): String {
        if (lang == AppLanguage.ENGLISH) return test.title
        return when (test) {
            // 1. Temperatures & Pressures
            DiagnosticTestId.ENGINE_COOLANT_TEMP -> "טמפרטורת נוזל קירור מנוע"
            DiagnosticTestId.AMBIENT_AIR_TEMP -> "טמפרטורת אוויר חיצוני"
            DiagnosticTestId.INTAKE_AIR_TEMP -> "טמפרטורת אוויר יניקה"
            DiagnosticTestId.CATALYST_TEMP_B1_B2 -> "טמפרטורת ממיר קטליטי (Bank 1 ו-Bank 2)"
            DiagnosticTestId.BAROMETRIC_PRESSURE -> "לחץ ברומטרי מוחלט"
            DiagnosticTestId.MANIFOLD_MAP -> "לחץ מוחלט בסעפת יניקה (MAP)"

            // 2. Throttle & Pedal System (ETC / TPS)
            DiagnosticTestId.THROTTLE_POSITION -> "מיקום מצערת"
            DiagnosticTestId.RELATIVE_THROTTLE_POSITION -> "מיקום מצערת יחסי"
            DiagnosticTestId.ABSOLUTE_THROTTLE_B -> "מיקום מצערת מוחלט B"
            DiagnosticTestId.ABSOLUTE_THROTTLE_D -> "מיקום מצערת מוחלט D"
            DiagnosticTestId.ABSOLUTE_THROTTLE_E -> "מיקום מצערת מוחלט E"
            DiagnosticTestId.COMMANDED_THROTTLE_ACTUATOR -> "פקודת מפעיל מצערת"

            // 3. Fuel System & Oxygen Sensors
            DiagnosticTestId.LONG_TERM_FUEL_TRIM_B1_B2 -> "תיקון דלק לטווח ארוך (Bank 1 ו-Bank 2)"
            DiagnosticTestId.SHORT_TERM_FUEL_TRIM_B1_B2 -> "תיקון דלק לטווח קצר (Bank 1 ו-Bank 2)"
            DiagnosticTestId.COMMANDED_EQUIV_RATIO -> "יחס תערובת אוויר-דלק מבוקש (Lambda)"
            DiagnosticTestId.FUEL_TANK_LEVEL -> "רמת דלק במיכל"
            DiagnosticTestId.O2_S1_B1S1_VOLTAGE -> "חיישן חמצן 1 (מתח) (Bank 1 Sensor 1)"
            DiagnosticTestId.O2_S1_B1S1_STFT -> "חיישן חמצן 1 (תיקון דלק קצר) (Bank 1 Sensor 1)"
            DiagnosticTestId.O2_S2_B1S2_VOLTAGE -> "חיישן חמצן 2 (מתח) (Bank 1 Sensor 2)"
            DiagnosticTestId.O2_S2_B1S2_STFT -> "חיישן חמצן 2 (תיקון דלק קצר) (Bank 1 Sensor 2)"
            DiagnosticTestId.O2_S3_B2S1_VOLTAGE -> "חיישן חמצן 3 (מתח) (Bank 2 Sensor 1)"
            DiagnosticTestId.O2_S3_B2S1_STFT -> "חיישן חמצן 3 (תיקון דלק קצר) (Bank 2 Sensor 1)"
            DiagnosticTestId.O2_S4_B2S2_VOLTAGE -> "חיישן חמצן 4 (מתח) (Bank 2 Sensor 2)"
            DiagnosticTestId.O2_S4_B2S2_STFT -> "חיישן חמצן 4 (תיקון דלק קצר) (Bank 2 Sensor 2)"

            // 4. Ignition & Misfires
            DiagnosticTestId.TIMING_ADVANCE -> "קידום הצתה"
            DiagnosticTestId.MISFIRE_CYL_1 -> "החטאות בעירה צילינדר 1 (בנזין)"
            DiagnosticTestId.MISFIRE_CYL_2 -> "החטאות בעירה צילינדר 2 (בנזין)"
            DiagnosticTestId.MISFIRE_CYL_3 -> "החטאות בעירה צילינדר 3 (בנזין)"
            DiagnosticTestId.MISFIRE_CYL_4 -> "החטאות בעירה צילינדר 4 (בנזין)"
            DiagnosticTestId.MISFIRE_CYL_5 -> "החטאות בעירה צילינדר 5 (בנזין)"
            DiagnosticTestId.MISFIRE_CYL_6 -> "החטאות בעירה צילינדר 6 (בנזין)"

            // 5. Emissions & EGR / EVAP
            DiagnosticTestId.COMMANDED_EGR -> "פקודת EGR"
            DiagnosticTestId.EGR_ERROR -> "שגיאת EGR"
            DiagnosticTestId.COMMANDED_EVAP_PURGE -> "פקודת אוורור אדי דלק (EVAP)"

            // 6. General Engine, Electrical & Trip Counters
            DiagnosticTestId.ENGINE_RPM -> "סל\"ד מנוע (RPM)"
            DiagnosticTestId.VEHICLE_SPEED -> "מהירות רכב"
            DiagnosticTestId.CALCULATED_ENGINE_LOAD -> "עומס מנוע מחושב"
            DiagnosticTestId.ABSOLUTE_LOAD_VALUE -> "ערך עומס מוחלט"
            DiagnosticTestId.CONTROL_MODULE_VOLTAGE -> "מתח יחידת בקרה / מצבר"
            DiagnosticTestId.RUN_TIME_SINCE_START -> "זמן עבודה מהתנעה"
            DiagnosticTestId.DISTANCE_SINCE_CODES_CLEARED -> "מרחק מאיפוס קודי תקלה"
            DiagnosticTestId.DISTANCE_WITH_MIL_ON -> "מרחק נסיעה עם נורת תקלה דולקת"
            DiagnosticTestId.WARM_UPS_SINCE_CODES_CLEARED -> "מחזורי חימום מאיפוס תקלות"
        }
    }

    fun statusBadge(status: DiagnosticReadingStatus, isAlert: Boolean, lang: AppLanguage): String {
        if (lang == AppLanguage.ENGLISH) {
            return when (status) {
                DiagnosticReadingStatus.LIVE -> if (isAlert) "ALERT" else "LIVE"
                DiagnosticReadingStatus.SCANNING -> "SCAN"
                DiagnosticReadingStatus.UNSUPPORTED -> "N/A"
                DiagnosticReadingStatus.ERROR -> "ERR"
                DiagnosticReadingStatus.IDLE -> "IDLE"
            }
        }
        return when (status) {
            DiagnosticReadingStatus.LIVE -> if (isAlert) "התראה" else "חי"
            DiagnosticReadingStatus.SCANNING -> "סורק"
            DiagnosticReadingStatus.UNSUPPORTED -> "לא זמין"
            DiagnosticReadingStatus.ERROR -> "שגיאה"
            DiagnosticReadingStatus.IDLE -> "ממתין"
        }
    }
}
