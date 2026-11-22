package com.example.obd

object JkDiagnosticLibrary {

    val supportedPids: List<ObdPidDefinition> = listOf(
        ObdPidDefinition("010C", "Engine RPM", "Idle ~650–750 RPM; redline 6,400 (3.6L) / 5,600 (3.8L)", "RPM", "RPM"),
        ObdPidDefinition("0105", "Engine Coolant Temp", "Normal JK range 195–222°F (90–106°C); fan high >226°F", "°F", "°C"),
        ObdPidDefinition("ATRV", "Battery / Alternator", "Resting ~12.6V; running 13.8–14.5V (TIPM monitored)", "V", "V"),
        ObdPidDefinition("010D", "Vehicle Speed", "ECU VSS speed; re-calibrate via JScan/ProCal if >33\" tires", "MPH", "KM/H"),
        ObdPidDefinition("010F", "Intake Air Temp", "Tracks under-hood heat soak on slow rock crawls", "°F", "°C"),
        ObdPidDefinition("0111", "Throttle Position", "Drive-by-wire pedal/throttle plate angle", "%", "%"),
        ObdPidDefinition("0104", "Engine Load", "Calculated volumetric load; watch on steep climbs", "%", "%"),
        ObdPidDefinition("010B", "Intake Manifold (MAP)", "Vacuum at idle ~30–40 kPa; WOT ~95–100 kPa", "PSI", "kPa"),
        ObdPidDefinition("0106", "Short Term Fuel Trim B1", "Healthy closed-loop stays within ±8%", "%", "%"),
        ObdPidDefinition("0107", "Long Term Fuel Trim B1", ">+10% indicates vacuum leak or weak fuel delivery", "%", "%"),
        ObdPidDefinition("0146", "Ambient Air Temp", "Bumper grille sensor used by HVAC & TIPM", "°F", "°C"),
        ObdPidDefinition("0101", "MIL & DTC Monitor", "Check Engine Lamp state and stored fault count", "Count", "Count")
    )

    val jkDtcCatalog: List<DtcCodeInfo> = listOf(
        DtcCodeInfo(
            code = "P0128",
            title = "Coolant Thermostat Rationality (Temp Below Regulating)",
            subsystem = JkSubsystem.POWERTRAIN,
            severity = DtcSeverity.WARNING,
            affectedEngines = "3.6L Pentastar & 3.8L V6",
            description = "Coolant failed to reach ~180°F within expected warm-up time, typically a stuck-open thermostat or faulty ECT sensor.",
            trailGuidance = "Safe to finish trail at normal speed; monitor live coolant gauge on Cockpit tab and replace OEM Mopar thermostat housing when back."
        ),
        DtcCodeInfo(
            code = "P0300",
            title = "Random / Multiple Cylinder Misfire Detected",
            subsystem = JkSubsystem.POWERTRAIN,
            severity = DtcSeverity.CRITICAL,
            affectedEngines = "3.6L Pentastar & 3.8L V6",
            description = "Misfires across multiple cylinders. On 2012–2013 3.6L Pentastar, often linked to left cylinder head valve seats, fouled plugs, or intake plenum leaks.",
            trailGuidance = "Avoid heavy throttle if CEL flashes (catalyst damage risk). Check coil pack boots for water ingress after deep crossings."
        ),
        DtcCodeInfo(
            code = "P0302",
            title = "Cylinder 2 Misfire Detected",
            subsystem = JkSubsystem.POWERTRAIN,
            severity = DtcSeverity.CRITICAL,
            affectedEngines = "3.6L Pentastar (Left Bank)",
            description = "Isolated misfire on Cylinder 2. Inspect ignition coil, spark plug gap, injector connector, and rocker arm tick on 3.6L engines.",
            trailGuidance = "Swap coil #2 with #4 to confirm coil fault before tearing down plenum."
        ),
        DtcCodeInfo(
            code = "P0304",
            title = "Cylinder 4 Misfire Detected",
            subsystem = JkSubsystem.POWERTRAIN,
            severity = DtcSeverity.CRITICAL,
            affectedEngines = "3.6L Pentastar & 3.8L V6",
            description = "Isolated misfire on Cylinder 4. Common on 3.8L with degraded spark plug wires or 3.6L coil/plug wear.",
            trailGuidance = "Check plug wire seating (3.8L) or coil harness latch (3.6L)."
        ),
        DtcCodeInfo(
            code = "P0456",
            title = "EVAP System Very Small Leak Detected",
            subsystem = JkSubsystem.EVAP,
            severity = DtcSeverity.INFO,
            affectedEngines = "All 2007–2018 Wrangler JK",
            description = "0.020-inch EVAP leak. Very common on Wrangler JK due to loose fuel cap, cracked ESIM switch, or trail-damaged purge/charcoal canister hoses.",
            trailGuidance = "Inspect and click gas cap 3 times, then inspect rear charcoal canister lines above rear axle for rock/brush damage."
        ),
        DtcCodeInfo(
            code = "P0457",
            title = "EVAP System Leak Detected (Fuel Cap Loose / Off)",
            subsystem = JkSubsystem.EVAP,
            severity = DtcSeverity.INFO,
            affectedEngines = "All 2007–2018 Wrangler JK",
            description = "Large EVAP pressure loss detected immediately after refueling or cap seal failure.",
            trailGuidance = "Clean dust/mud off filler neck O-ring and re-tighten fuel cap until it clicks."
        ),
        DtcCodeInfo(
            code = "P0440",
            title = "EVAP System General Malfunction",
            subsystem = JkSubsystem.EVAP,
            severity = DtcSeverity.INFO,
            affectedEngines = "All 2007–2018 Wrangler JK",
            description = "Purge valve or ESIM (Evaporative System Integrity Monitor) failed self-test.",
            trailGuidance = "No drivability impact on trail; inspect white plastic ESIM module attached to canister."
        ),
        DtcCodeInfo(
            code = "P0520",
            title = "Engine Oil Pressure Sensor / Switch Circuit",
            subsystem = JkSubsystem.POWERTRAIN,
            severity = DtcSeverity.CRITICAL,
            affectedEngines = "3.6L Pentastar (2012–2018)",
            description = "Oil pressure sensor circuit out of range, frequently caused by oil cooler housing sensor seepage or failing 2-pin/3-pin sender under intake.",
            trailGuidance = "Verify physical oil level on dipstick and listen for valvetrain noise before driving further."
        ),
        DtcCodeInfo(
            code = "P06DD",
            title = "Engine Oil Pressure Control Circuit Stuck Off",
            subsystem = JkSubsystem.POWERTRAIN,
            severity = DtcSeverity.CRITICAL,
            affectedEngines = "3.6L Pentastar Dual-Stage Pump",
            description = "Dual-stage oil pump solenoid failed to switch to high-pressure mode above ~3,500 RPM, or oil cooler bypass valve is missing/damaged.",
            trailGuidance = "Keep RPM under 3,000 RPM and check oil level & filter cap center stem immediately."
        ),
        DtcCodeInfo(
            code = "P0420",
            title = "Catalyst System Efficiency Below Threshold (Bank 1)",
            subsystem = JkSubsystem.POWERTRAIN,
            severity = DtcSeverity.WARNING,
            affectedEngines = "3.6L & 3.8L V6",
            description = "Bank 1 catalytic converter efficiency degraded or downstream O2 sensor lazy.",
            trailGuidance = "Check exhaust manifold/Y-pipe for rock impacts or cracked 3.8L manifold flanges."
        ),
        DtcCodeInfo(
            code = "P0430",
            title = "Catalyst System Efficiency Below Threshold (Bank 2)",
            subsystem = JkSubsystem.POWERTRAIN,
            severity = DtcSeverity.WARNING,
            affectedEngines = "3.6L & 3.8L V6",
            description = "Bank 2 catalytic converter efficiency below threshold or exhaust leak ahead of downstream O2 sensor.",
            trailGuidance = "Inspect driver-side exhaust flange and O2 sensor wiring clearance near front driveshaft."
        ),
        DtcCodeInfo(
            code = "P0171",
            title = "Fuel System Too Lean (Bank 1)",
            subsystem = JkSubsystem.POWERTRAIN,
            severity = DtcSeverity.WARNING,
            affectedEngines = "3.6L & 3.8L V6",
            description = "Long-term fuel trim exceeding positive threshold due to unmetered air (cracked intake boot, intake plenum gasket) or low fuel pressure.",
            trailGuidance = "Watch LTFT B1 on Terminal/Cockpit; inspect aftermarket cold-air intake couplers for dislodged clamps."
        ),
        DtcCodeInfo(
            code = "P0174",
            title = "Fuel System Too Lean (Bank 2)",
            subsystem = JkSubsystem.POWERTRAIN,
            severity = DtcSeverity.WARNING,
            affectedEngines = "3.6L & 3.8L V6",
            description = "Bank 2 running lean. Combined with P0171, points strongly to vacuum leak after throttle body or failing fuel pump.",
            trailGuidance = "Check brake booster vacuum line and PCV hose behind throttle body."
        ),
        DtcCodeInfo(
            code = "P0113",
            title = "Intake Air Temperature Sensor 1 Circuit High",
            subsystem = JkSubsystem.POWERTRAIN,
            severity = DtcSeverity.WARNING,
            affectedEngines = "All JK Models",
            description = "Open IAT sensor circuit (-40°F reading). Often happens if IAT connector was left unplugged after air filter service.",
            trailGuidance = "Check 2-pin IAT sensor plug on airbox intake tube."
        ),
        DtcCodeInfo(
            code = "P0700",
            title = "Transmission Control System (MIL Request)",
            subsystem = JkSubsystem.TRANSMISSION,
            severity = DtcSeverity.CRITICAL,
            affectedEngines = "42RLE (4-spd) & W5A580 (5-spd Auto)",
            description = "TCM requested Check Engine Light due to an internal transmission fault (often limp-home 2nd gear).",
            trailGuidance = "Inspect transmission pan harness plug for mud/water intrusion and verify ATF level hot in Neutral."
        ),
        DtcCodeInfo(
            code = "P0740",
            title = "Torque Converter Clutch Circuit Performance",
            subsystem = JkSubsystem.TRANSMISSION,
            severity = DtcSeverity.WARNING,
            affectedEngines = "42RLE & W5A580 Automatics",
            description = "TCC lockup slip or solenoid delay, sometimes triggered by overheating ATF on steep slow climbs in 4-High.",
            trailGuidance = "Shift transfer case into 4-Low on steep grades to reduce torque converter heat buildup."
        ),
        DtcCodeInfo(
            code = "U0100",
            title = "Lost Communication with ECM / PCM",
            subsystem = JkSubsystem.TIPM_NETWORK,
            severity = DtcSeverity.CRITICAL,
            affectedEngines = "All JK TIPM / CAN-C Bus",
            description = "CAN-C bus communication dropout between TIPM, cluster, or PCM. Often caused by low battery voltage, loose battery terminals, or TIPM relay corrosion.",
            trailGuidance = "Check battery voltage (must be >12.2V resting), tighten battery clamps, and check M25/J13 TIPM fuses."
        ),
        DtcCodeInfo(
            code = "U0101",
            title = "Lost Communication with TCM",
            subsystem = JkSubsystem.TIPM_NETWORK,
            severity = DtcSeverity.CRITICAL,
            affectedEngines = "Automatic JK Models",
            description = "CAN bus lost communication with Transmission Control Module. Inspect W5A580 conductor plate plug or 42RLE bulkhead harness.",
            trailGuidance = "Check transmission harness near exhaust crossover for melted insulation."
        ),
        DtcCodeInfo(
            code = "U0121",
            title = "Lost Communication with ABS Module",
            subsystem = JkSubsystem.TIPM_NETWORK,
            severity = DtcSeverity.WARNING,
            affectedEngines = "All JK (ESP / ABS)",
            description = "CAN bus dropout with Anti-Lock Brake / ESP module. Can cause dash Christmas-tree lights and disable sway-bar/lockers.",
            trailGuidance = "Inspect ABS module connector behind driver headlight & check battery terminal voltage."
        ),
        DtcCodeInfo(
            code = "U0141",
            title = "Lost Communication with TIPM / Front Control Module",
            subsystem = JkSubsystem.TIPM_NETWORK,
            severity = DtcSeverity.CRITICAL,
            affectedEngines = "All JK (2007–2018)",
            description = "Totally Integrated Power Module (TIPM) failed to respond on CAN-IHS/CAN-C bus. Classic JK symptom of loose battery ground or moisture inside TIPM.",
            trailGuidance = "Check main ground strap on passenger inner fender and battery negative post."
        ),
        DtcCodeInfo(
            code = "C1020",
            title = "Left Rear Wheel Speed Sensor Circuit",
            subsystem = JkSubsystem.ABS_CHASSIS,
            severity = DtcSeverity.WARNING,
            affectedEngines = "All JK Dana 35 / Dana 44",
            description = "ABS / Traction / ESP warning caused by sheared wheel speed sensor wire, mud-packed tone ring, or failing rear axle bearing seal.",
            trailGuidance = "4WD still operates mechanically, though BLD/ESP traction control may be disabled. Inspect sensor lead routed along rear control arm."
        ),
        DtcCodeInfo(
            code = "C102A",
            title = "Left Rear Wheel Speed Comparative Performance",
            subsystem = JkSubsystem.ABS_CHASSIS,
            severity = DtcSeverity.WARNING,
            affectedEngines = "All JK",
            description = "Intermittent wheel speed signal drop vs other 3 wheels; frequently caused by rear axle shaft play or stretched sensor harness at full droop.",
            trailGuidance = "Verify sufficient slack in ABS wire at full suspension articulation."
        ),
        DtcCodeInfo(
            code = "C121C",
            title = "Torque Request Signal Denied (Steering / Axle)",
            subsystem = JkSubsystem.ABS_CHASSIS,
            severity = DtcSeverity.INFO,
            affectedEngines = "Lifted JK Models",
            description = "ESP/BAS triggered because steering wheel is off-center after trail tie-rod/drag-link hit.",
            trailGuidance = "Re-center drag link turnbuckle so steering wheel sits straight while tracking ahead."
        ),
        DtcCodeInfo(
            code = "C2200",
            title = "Anti-Lock Brake Module Internal Fault",
            subsystem = JkSubsystem.ABS_CHASSIS,
            severity = DtcSeverity.CRITICAL,
            affectedEngines = "2007–2018 JK ABS HCU",
            description = "Internal ABS ECU hardware fault or severe under-voltage event during cranking.",
            trailGuidance = "Verify charging system voltage (>13.6V running) and clear codes after battery check."
        ),
        DtcCodeInfo(
            code = "B162C",
            title = "Left Low Beam Control Circuit High (TIPM)",
            subsystem = JkSubsystem.TIPM_NETWORK,
            severity = DtcSeverity.INFO,
            affectedEngines = "JK with LED Headlight Swap",
            description = "TIPM detected open/low-current circuit on headlight driver, very common when installing aftermarket LED headlights without anti-flicker PWM resistors.",
            trailGuidance = "Add CAN-bus anti-flicker harness or enable LED headlight mode in BCM/TIPM."
        ),
        DtcCodeInfo(
            code = "B210A",
            title = "System Voltage Low (TIPM / Cluster)",
            subsystem = JkSubsystem.TIPM_NETWORK,
            severity = DtcSeverity.WARNING,
            affectedEngines = "All JK Models",
            description = "System voltage dropped below ~9.5V during winch pull or weak AGM battery cranking.",
            trailGuidance = "Monitor battery voltage on Cockpit; keep engine at ~1,500 RPM during heavy winching."
        )
    )

    fun lookupCode(code: String): DtcCodeInfo {
        val normalized = code.trim().uppercase()
        return jkDtcCatalog.firstOrNull { it.code == normalized } ?: DtcCodeInfo(
            code = normalized,
            title = inferGenericTitle(normalized),
            subsystem = inferSubsystem(normalized),
            severity = if (normalized.startsWith("P0") || normalized.startsWith("U")) DtcSeverity.WARNING else DtcSeverity.INFO,
            affectedEngines = "Chrysler / Jeep SAE J2012",
            description = "Standard SAE/Manufacturer fault code reported by the Wrangler JK ECU ($normalized). Capture Freeze Frame & verify live sensor trims.",
            trailGuidance = "Check recent mods, loose connectors, and battery voltage before clearing code."
        )
    }

    private fun inferSubsystem(code: String): JkSubsystem = when {
        code.startsWith("U") -> JkSubsystem.TIPM_NETWORK
        code.startsWith("C") -> JkSubsystem.ABS_CHASSIS
        code.startsWith("P07") || code.startsWith("P08") -> JkSubsystem.TRANSMISSION
        code.startsWith("P044") || code.startsWith("P045") -> JkSubsystem.EVAP
        else -> JkSubsystem.POWERTRAIN
    }

    private fun inferGenericTitle(code: String): String = when (code.firstOrNull()) {
        'P' -> "Powertrain Fault ($code)"
        'C' -> "Chassis / ABS / 4WD Fault ($code)"
        'B' -> "Body / TIPM Lighting Fault ($code)"
        'U' -> "CAN-Bus / Module Comm Fault ($code)"
        else -> "Diagnostic Trouble Code ($code)"
    }
}
