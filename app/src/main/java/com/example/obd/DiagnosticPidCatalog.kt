package com.example.obd

enum class DiagnosticCategory(
    val number: Int,
    val title: String,
    val shortLabel: String
) {
    TEMPERATURES_PRESSURES(1, "1. Temperatures & Pressures", "Temps & MAP"),
    THROTTLE_PEDAL(2, "2. Throttle & Pedal System (ETC / TPS)", "Throttle"),
    FUEL_OXYGEN(3, "3. Fuel System & Oxygen Sensors (Fuel Trims & O2)", "Fuel & O2"),
    IGNITION_MISFIRES(4, "4. Ignition & Misfires", "Misfires"),
    EMISSIONS_EGR_EVAP(5, "5. Emissions & Exhaust Gas Recirculation (EGR & EVAP)", "EGR / EVAP"),
    ENGINE_ELECTRICAL_TRIP(6, "6. General Engine, Electrical & Trip Counters", "Engine & Trip")
}

enum class DiagnosticTestId(
    val key: String,
    val category: DiagnosticCategory,
    val title: String,
    val pidBadge: String,
    val primaryCommands: List<String>,
    val fallbackCommand: String? = null,
    val unit: String
) {
    // 1. Temperatures & Pressures
    ENGINE_COOLANT_TEMP(
        key = "coolant_temp",
        category = DiagnosticCategory.TEMPERATURES_PRESSURES,
        title = "Engine Coolant Temperature",
        pidBadge = "0105",
        primaryCommands = listOf("0105"),
        unit = "°C"
    ),
    AMBIENT_AIR_TEMP(
        key = "ambient_temp",
        category = DiagnosticCategory.TEMPERATURES_PRESSURES,
        title = "Ambient air temperature",
        pidBadge = "0146",
        primaryCommands = listOf("0146"),
        unit = "°C"
    ),
    INTAKE_AIR_TEMP(
        key = "intake_temp",
        category = DiagnosticCategory.TEMPERATURES_PRESSURES,
        title = "Intake air temperature",
        pidBadge = "010F",
        primaryCommands = listOf("010F"),
        unit = "°C"
    ),
    CATALYST_TEMP_B1_B2(
        key = "catalyst_temp_b1_b2",
        category = DiagnosticCategory.TEMPERATURES_PRESSURES,
        title = "Catalyst Temperature (Bank 1 & Bank 2)",
        pidBadge = "013C / 013D",
        primaryCommands = listOf("013C", "013D"),
        unit = "°C"
    ),
    BAROMETRIC_PRESSURE(
        key = "baro_pressure",
        category = DiagnosticCategory.TEMPERATURES_PRESSURES,
        title = "Absolute Barometric Pressure",
        pidBadge = "0133",
        primaryCommands = listOf("0133"),
        unit = "kPa"
    ),
    MANIFOLD_MAP(
        key = "manifold_map",
        category = DiagnosticCategory.TEMPERATURES_PRESSURES,
        title = "Intake manifold absolute pressure (MAP)",
        pidBadge = "010B",
        primaryCommands = listOf("010B"),
        unit = "kPa"
    ),
    ENGINE_OIL_TEMP(
        key = "engine_oil_temp",
        category = DiagnosticCategory.TEMPERATURES_PRESSURES,
        title = "Engine oil temperature (JL)",
        pidBadge = "015C",
        primaryCommands = listOf("015C"),
        unit = "°C"
    ),
    TURBO_BOOST_PRESSURE(
        key = "turbo_boost",
        category = DiagnosticCategory.TEMPERATURES_PRESSURES,
        title = "Turbocharger boost pressure (JL 2.0T / EcoDiesel)",
        pidBadge = "0170",
        primaryCommands = listOf("0170"),
        unit = "kPa"
    ),
    CHARGE_AIR_COOLER_TEMP(
        key = "charge_air_temp",
        category = DiagnosticCategory.TEMPERATURES_PRESSURES,
        title = "Charge air cooler temperature (JL 2.0T / Diesel)",
        pidBadge = "0177",
        primaryCommands = listOf("0177"),
        unit = "°C"
    ),

    // 2. Throttle & Pedal System (ETC / TPS)
    THROTTLE_POSITION(
        key = "throttle_pos",
        category = DiagnosticCategory.THROTTLE_PEDAL,
        title = "Throttle position",
        pidBadge = "0111",
        primaryCommands = listOf("0111"),
        unit = "%"
    ),
    RELATIVE_THROTTLE_POSITION(
        key = "rel_throttle_pos",
        category = DiagnosticCategory.THROTTLE_PEDAL,
        title = "Relative throttle position",
        pidBadge = "0145",
        primaryCommands = listOf("0145"),
        unit = "%"
    ),
    ABSOLUTE_THROTTLE_B(
        key = "abs_throttle_b",
        category = DiagnosticCategory.THROTTLE_PEDAL,
        title = "Absolute throttle position B",
        pidBadge = "0147",
        primaryCommands = listOf("0147"),
        unit = "%"
    ),
    ABSOLUTE_THROTTLE_D(
        key = "abs_throttle_d",
        category = DiagnosticCategory.THROTTLE_PEDAL,
        title = "Absolute throttle position D",
        pidBadge = "0149",
        primaryCommands = listOf("0149"),
        unit = "%"
    ),
    ABSOLUTE_THROTTLE_E(
        key = "abs_throttle_e",
        category = DiagnosticCategory.THROTTLE_PEDAL,
        title = "Absolute throttle position E",
        pidBadge = "014A",
        primaryCommands = listOf("014A"),
        unit = "%"
    ),
    COMMANDED_THROTTLE_ACTUATOR(
        key = "cmd_throttle_actuator",
        category = DiagnosticCategory.THROTTLE_PEDAL,
        title = "Commanded throttle actuator",
        pidBadge = "014C",
        primaryCommands = listOf("014C"),
        unit = "%"
    ),

    // 3. Fuel System & Oxygen Sensors (Fuel Trims & O2)
    LONG_TERM_FUEL_TRIM_B1_B2(
        key = "ltft_b1_b2",
        category = DiagnosticCategory.FUEL_OXYGEN,
        title = "Long term fuel trim (Bank 1 & Bank 2)",
        pidBadge = "0107 / 0109",
        primaryCommands = listOf("0107", "0109"),
        unit = "%"
    ),
    SHORT_TERM_FUEL_TRIM_B1_B2(
        key = "stft_b1_b2",
        category = DiagnosticCategory.FUEL_OXYGEN,
        title = "Short term fuel trim (Bank 1 & Bank 2)",
        pidBadge = "0106 / 0108",
        primaryCommands = listOf("0106", "0108"),
        unit = "%"
    ),
    COMMANDED_EQUIV_RATIO(
        key = "equiv_ratio",
        category = DiagnosticCategory.FUEL_OXYGEN,
        title = "Fuel Air commanded equivalence ratio",
        pidBadge = "0144",
        primaryCommands = listOf("0144"),
        unit = "λ"
    ),
    FUEL_TANK_LEVEL(
        key = "fuel_tank_level",
        category = DiagnosticCategory.FUEL_OXYGEN,
        title = "Fuel Tank Level Input",
        pidBadge = "012F",
        primaryCommands = listOf("012F"),
        unit = "%"
    ),
    FUEL_RAIL_PRESSURE(
        key = "fuel_rail_pressure",
        category = DiagnosticCategory.FUEL_OXYGEN,
        title = "Fuel rail pressure (JL GDI / EcoDiesel)",
        pidBadge = "0123 / 0122",
        primaryCommands = listOf("0123"),
        fallbackCommand = "0122",
        unit = "kPa"
    ),
    O2_S1_B1S1_VOLTAGE(
        key = "o2_s1_volts",
        category = DiagnosticCategory.FUEL_OXYGEN,
        title = "Oxygen sensor 1 (voltage) (Bank 1 Sensor 1)",
        pidBadge = "0114",
        primaryCommands = listOf("0114"),
        unit = "V"
    ),
    O2_S1_B1S1_STFT(
        key = "o2_s1_stft",
        category = DiagnosticCategory.FUEL_OXYGEN,
        title = "Oxygen sensor 1 (short term fuel trim) (Bank 1 Sensor 1)",
        pidBadge = "0114",
        primaryCommands = listOf("0114"),
        unit = "%"
    ),
    O2_S2_B1S2_VOLTAGE(
        key = "o2_s2_volts",
        category = DiagnosticCategory.FUEL_OXYGEN,
        title = "Oxygen sensor 2 (voltage) (Bank 1 Sensor 2)",
        pidBadge = "0115",
        primaryCommands = listOf("0115"),
        unit = "V"
    ),
    O2_S2_B1S2_STFT(
        key = "o2_s2_stft",
        category = DiagnosticCategory.FUEL_OXYGEN,
        title = "Oxygen sensor 2 (short term fuel trim) (Bank 1 Sensor 2)",
        pidBadge = "0115",
        primaryCommands = listOf("0115"),
        unit = "%"
    ),
    O2_S3_B2S1_VOLTAGE(
        key = "o2_s3_volts",
        category = DiagnosticCategory.FUEL_OXYGEN,
        title = "Oxygen sensor 3 (voltage) (Bank 2 Sensor 1)",
        pidBadge = "0116 / 0118",
        primaryCommands = listOf("0116"),
        fallbackCommand = "0118",
        unit = "V"
    ),
    O2_S3_B2S1_STFT(
        key = "o2_s3_stft",
        category = DiagnosticCategory.FUEL_OXYGEN,
        title = "Oxygen sensor 3 (short term fuel trim) (Bank 2 Sensor 1)",
        pidBadge = "0116 / 0118",
        primaryCommands = listOf("0116"),
        fallbackCommand = "0118",
        unit = "%"
    ),
    O2_S4_B2S2_VOLTAGE(
        key = "o2_s4_volts",
        category = DiagnosticCategory.FUEL_OXYGEN,
        title = "Oxygen sensor 4 (voltage) (Bank 2 Sensor 2)",
        pidBadge = "0117 / 0119",
        primaryCommands = listOf("0117"),
        fallbackCommand = "0119",
        unit = "V"
    ),
    O2_S4_B2S2_STFT(
        key = "o2_s4_stft",
        category = DiagnosticCategory.FUEL_OXYGEN,
        title = "Oxygen sensor 4 (short term fuel trim) (Bank 2 Sensor 2)",
        pidBadge = "0117 / 0119",
        primaryCommands = listOf("0117"),
        fallbackCommand = "0119",
        unit = "%"
    ),

    // 4. Ignition & Misfires
    TIMING_ADVANCE(
        key = "timing_advance",
        category = DiagnosticCategory.IGNITION_MISFIRES,
        title = "Timing advance",
        pidBadge = "010E",
        primaryCommands = listOf("010E"),
        unit = "°"
    ),
    MISFIRE_CYL_1(
        key = "misfire_cyl_1",
        category = DiagnosticCategory.IGNITION_MISFIRES,
        title = "Misfire Cyl 1 (petrol)",
        pidBadge = "06A2",
        primaryCommands = listOf("06A2"),
        unit = "cnt"
    ),
    MISFIRE_CYL_2(
        key = "misfire_cyl_2",
        category = DiagnosticCategory.IGNITION_MISFIRES,
        title = "Misfire Cyl 2 (petrol)",
        pidBadge = "06A3",
        primaryCommands = listOf("06A3"),
        unit = "cnt"
    ),
    MISFIRE_CYL_3(
        key = "misfire_cyl_3",
        category = DiagnosticCategory.IGNITION_MISFIRES,
        title = "Misfire Cyl 3 (petrol)",
        pidBadge = "06A4",
        primaryCommands = listOf("06A4"),
        unit = "cnt"
    ),
    MISFIRE_CYL_4(
        key = "misfire_cyl_4",
        category = DiagnosticCategory.IGNITION_MISFIRES,
        title = "Misfire Cyl 4 (petrol)",
        pidBadge = "06A5",
        primaryCommands = listOf("06A5"),
        unit = "cnt"
    ),
    MISFIRE_CYL_5(
        key = "misfire_cyl_5",
        category = DiagnosticCategory.IGNITION_MISFIRES,
        title = "Misfire Cyl 5 (petrol)",
        pidBadge = "06A6",
        primaryCommands = listOf("06A6"),
        unit = "cnt"
    ),
    MISFIRE_CYL_6(
        key = "misfire_cyl_6",
        category = DiagnosticCategory.IGNITION_MISFIRES,
        title = "Misfire Cyl 6 (petrol)",
        pidBadge = "06A7",
        primaryCommands = listOf("06A7"),
        unit = "cnt"
    ),
    MISFIRE_CYL_7(
        key = "misfire_cyl_7",
        category = DiagnosticCategory.IGNITION_MISFIRES,
        title = "Misfire Cyl 7 (JL 392 HEMI V8)",
        pidBadge = "06A8",
        primaryCommands = listOf("06A8"),
        unit = "cnt"
    ),
    MISFIRE_CYL_8(
        key = "misfire_cyl_8",
        category = DiagnosticCategory.IGNITION_MISFIRES,
        title = "Misfire Cyl 8 (JL 392 HEMI V8)",
        pidBadge = "06A9",
        primaryCommands = listOf("06A9"),
        unit = "cnt"
    ),

    // 5. Emissions & Exhaust Gas Recirculation (EGR & EVAP)
    COMMANDED_EGR(
        key = "cmd_egr",
        category = DiagnosticCategory.EMISSIONS_EGR_EVAP,
        title = "Commanded EGR",
        pidBadge = "012C",
        primaryCommands = listOf("012C"),
        unit = "%"
    ),
    EGR_ERROR(
        key = "egr_error",
        category = DiagnosticCategory.EMISSIONS_EGR_EVAP,
        title = "EGR Error",
        pidBadge = "012D",
        primaryCommands = listOf("012D"),
        unit = "%"
    ),
    COMMANDED_EVAP_PURGE(
        key = "cmd_evap_purge",
        category = DiagnosticCategory.EMISSIONS_EGR_EVAP,
        title = "Commanded evaporative purge",
        pidBadge = "012E",
        primaryCommands = listOf("012E"),
        unit = "%"
    ),

    // 6. General Engine, Electrical & Trip Counters
    ENGINE_RPM(
        key = "engine_rpm",
        category = DiagnosticCategory.ENGINE_ELECTRICAL_TRIP,
        title = "Engine RPM",
        pidBadge = "010C",
        primaryCommands = listOf("010C"),
        unit = "RPM"
    ),
    VEHICLE_SPEED(
        key = "vehicle_speed",
        category = DiagnosticCategory.ENGINE_ELECTRICAL_TRIP,
        title = "Vehicle speed",
        pidBadge = "010D",
        primaryCommands = listOf("010D"),
        unit = "KPH"
    ),
    CALCULATED_ENGINE_LOAD(
        key = "calc_engine_load",
        category = DiagnosticCategory.ENGINE_ELECTRICAL_TRIP,
        title = "Calculated engine load",
        pidBadge = "0104",
        primaryCommands = listOf("0104"),
        unit = "%"
    ),
    ABSOLUTE_LOAD_VALUE(
        key = "abs_load_value",
        category = DiagnosticCategory.ENGINE_ELECTRICAL_TRIP,
        title = "Absolute load value",
        pidBadge = "0143",
        primaryCommands = listOf("0143"),
        unit = "%"
    ),
    CONTROL_MODULE_VOLTAGE(
        key = "ctrl_module_voltage",
        category = DiagnosticCategory.ENGINE_ELECTRICAL_TRIP,
        title = "Control module voltage",
        pidBadge = "0142",
        primaryCommands = listOf("0142"),
        fallbackCommand = "ATRV",
        unit = "V"
    ),
    RUN_TIME_SINCE_START(
        key = "run_time_start",
        category = DiagnosticCategory.ENGINE_ELECTRICAL_TRIP,
        title = "Run time since engine start",
        pidBadge = "011F",
        primaryCommands = listOf("011F"),
        unit = "time"
    ),
    DISTANCE_SINCE_CODES_CLEARED(
        key = "dist_since_clear",
        category = DiagnosticCategory.ENGINE_ELECTRICAL_TRIP,
        title = "Distance traveled since codes cleared",
        pidBadge = "0131",
        primaryCommands = listOf("0131"),
        unit = "km"
    ),
    DISTANCE_WITH_MIL_ON(
        key = "dist_with_mil",
        category = DiagnosticCategory.ENGINE_ELECTRICAL_TRIP,
        title = "Distance traveled with MIL on",
        pidBadge = "0121",
        primaryCommands = listOf("0121"),
        unit = "km"
    ),
    WARM_UPS_SINCE_CODES_CLEARED(
        key = "warm_ups_clear",
        category = DiagnosticCategory.ENGINE_ELECTRICAL_TRIP,
        title = "Warm ups since codes cleared",
        pidBadge = "0130",
        primaryCommands = listOf("0130"),
        unit = "cnt"
    ),
    HYBRID_BATTERY_LIFE(
        key = "hybrid_battery_life",
        category = DiagnosticCategory.ENGINE_ELECTRICAL_TRIP,
        title = "HV Hybrid battery pack remaining (JL 4xe)",
        pidBadge = "015B",
        primaryCommands = listOf("015B"),
        unit = "%"
    );

    companion object {
        private val byKey = entries.associateBy { it.key }

        fun groupedByCategory(): List<Pair<DiagnosticCategory, List<DiagnosticTestId>>> {
            return DiagnosticCategory.entries.map { cat ->
                cat to entries.filter { it.category == cat }
            }
        }

        fun encodeSet(selected: Set<DiagnosticTestId>): String {
            return entries.filter { it in selected }.joinToString(",") { it.key }
        }

        fun decodeSet(csv: String): Set<DiagnosticTestId> {
            if (csv.isBlank()) return emptySet()
            return csv.split(",")
                .mapNotNull { byKey[it.trim()] }
                .toSet()
        }
    }
}

enum class DiagnosticReadingStatus {
    IDLE,
    SCANNING,
    LIVE,
    UNSUPPORTED,
    ERROR
}

data class DiagnosticReading(
    val testId: DiagnosticTestId,
    val formattedValue: String = "--",
    val status: DiagnosticReadingStatus = DiagnosticReadingStatus.IDLE,
    val isAlert: Boolean = false,
    val updatedAtMs: Long = 0L
)
