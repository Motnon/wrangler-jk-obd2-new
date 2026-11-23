package com.example.obd

object JkDiagnosticLibrary {

    val supportedPids: List<ObdPidDefinition> = listOf(
        ObdPidDefinition("010C", "Engine RPM", "Idle ~650–750 RPM; redline 6,000–6,400 petrol / 4,400 diesel", "RPM", "RPM"),
        ObdPidDefinition("0105", "Engine Coolant Temp", "Normal range 90–106°C (195–222°F); fan high >108°C", "°C", "°C"),
        ObdPidDefinition(
            "015C",
            "Engine Oil Temp (JL)",
            "Wrangler JL native oil temp sensor (normal 95–118°C under load)",
            "°C",
            "°C",
            models = setOf(WranglerModel.JL)
        ),
        ObdPidDefinition("ATRV", "Battery / Alternator", "Resting ~12.6V; running 13.8–14.5V (check JL Aux ESS battery if <12.2V)", "V", "V"),
        ObdPidDefinition("010D", "Vehicle Speed", "ECU VSS speed; calibrate in Garage when running 33\"–37\"+ tires", "KPH", "KPH"),
        ObdPidDefinition("010F", "Intake Air Temp", "Tracks under-hood heat soak on slow rock crawls", "°C", "°C"),
        ObdPidDefinition("0111", "Throttle Position", "Drive-by-wire pedal/throttle plate angle", "%", "%"),
        ObdPidDefinition("0104", "Engine Load", "Calculated volumetric load; watch on steep climbs", "%", "%"),
        ObdPidDefinition("010B", "Intake Manifold (MAP)", "Vacuum at idle ~30–40 kPa; boosted JL 2.0T / EcoDiesel >100 kPa", "PSI", "kPa"),
        ObdPidDefinition("0133", "Barometric Pressure", "Ambient altitude pressure (~101 kPa sea level)", "PSI", "kPa"),
        ObdPidDefinition(
            "0123",
            "Fuel Rail Pressure (JL)",
            "JL 2.0T GDI & 3.0L EcoDiesel high-pressure fuel rail",
            "kPa",
            "kPa",
            models = setOf(WranglerModel.JL)
        ),
        ObdPidDefinition(
            "0170",
            "Turbo Boost Control (JL)",
            "Wrangler JL 2.0L Hurricane Turbo & 3.0L EcoDiesel boost pressure",
            "kPa",
            "kPa",
            models = setOf(WranglerModel.JL)
        ),
        ObdPidDefinition(
            "015B",
            "4xe Hybrid Battery % (JL)",
            "Wrangler JL 4xe PHEV high-voltage battery pack remaining life",
            "%",
            "%",
            models = setOf(WranglerModel.JL)
        ),
        ObdPidDefinition("0106", "Short Term Fuel Trim B1", "Healthy closed-loop stays within ±8%", "%", "%"),
        ObdPidDefinition("0107", "Long Term Fuel Trim B1", ">+10% indicates vacuum leak or weak fuel delivery", "%", "%"),
        ObdPidDefinition("012F", "Fuel Tank Level", "2-door ~17.5–18.6 gal / 4-door ~21.5–22.5 gal sender", "%", "%"),
        ObdPidDefinition("0146", "Ambient Air Temp", "Bumper grille sensor used by HVAC & TIPM/BCM", "°C", "°C"),
        ObdPidDefinition(
            "0151",
            "Fuel Type (JL/JK)",
            "ECU reported fuel classification (Gasoline / Diesel / PHEV 4xe)",
            "",
            ""
        ),
        ObdPidDefinition("0101", "MIL & DTC Monitor", "Check Engine Lamp state and stored fault count", "Count", "Count")
    )

    fun supportedPidsFor(model: WranglerModel): List<ObdPidDefinition> {
        return supportedPids.filter { model in it.models }
    }

    val jkDtcCatalog: List<DtcCodeInfo>
        get() = WranglerDtcCatalog.allCodes

    fun lookupCode(code: String, model: WranglerModel = WranglerModel.JK): DtcCodeInfo {
        val normalized = code.trim().uppercase()
        return WranglerDtcCatalog.allCodes.firstOrNull { it.code == normalized } ?: DtcCodeInfo(
            code = normalized,
            title = inferGenericTitle(normalized, model),
            subsystem = inferSubsystem(normalized),
            severity = if (normalized.startsWith("P0") || normalized.startsWith("U")) DtcSeverity.WARNING else DtcSeverity.INFO,
            affectedEngines = "Jeep ${model.displayName} / SAE J2012",
            description = "Standard SAE/Stellantis fault code reported by the ${model.displayName} ECU ($normalized). Capture Freeze Frame & verify live sensor trims.",
            trailGuidance = if (model == WranglerModel.JL) {
                "Check recent mods, main & Aux ESS battery voltage, CAN star splice behind glovebox, and note JL SGW bypass is needed to clear via Mode 04."
            } else {
                "Check recent mods, loose connectors, and battery voltage before clearing code."
            },
            models = setOf(model)
        )
    }

    private fun inferSubsystem(code: String): JkSubsystem = when {
        code.startsWith("U") -> JkSubsystem.TIPM_NETWORK
        code.startsWith("C") -> JkSubsystem.ABS_CHASSIS
        code.startsWith("P07") || code.startsWith("P08") -> JkSubsystem.TRANSMISSION
        code.startsWith("P044") || code.startsWith("P045") || code.startsWith("P049") -> JkSubsystem.EVAP
        else -> JkSubsystem.POWERTRAIN
    }

    private fun inferGenericTitle(code: String, model: WranglerModel): String = when (code.firstOrNull()) {
        'P' -> "Powertrain Fault ($code)"
        'C' -> "Chassis / ABS / 4WD Fault ($code)"
        'B' -> if (model == WranglerModel.JL) "Body / BCM Lighting Fault ($code)" else "Body / TIPM Lighting Fault ($code)"
        'U' -> "CAN-Bus / Module Comm Fault ($code)"
        else -> "Diagnostic Trouble Code ($code)"
    }
}
