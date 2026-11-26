package com.example.obd

/**
 * Comprehensive Jeep Wrangler JK (2007–2018) & Wrangler JL (2018–Present) DTC encyclopedia
 * covering 3.6L Pentastar, 3.8L V6, 2.8L CRD, 2.0L Turbo GME, 3.0L EcoDiesel, 4xe PHEV,
 * 6.4L 392 HEMI, ZF 850RE/8HP75/W5A580/42RLE transmissions, TIPM/BCM/SGW/Aux battery, and 4WD/ABS.
 */
object WranglerDtcCatalog {

    private val jkOnly = setOf(WranglerModel.JK)
    private val jlOnly = setOf(WranglerModel.JL)
    private val both = setOf(WranglerModel.JK, WranglerModel.JL)

    val allCodes: List<DtcCodeInfo> = listOf(
        // --- General & Cooling ---
        DtcCodeInfo(
            code = "P0128",
            title = "Coolant Thermostat Rationality (Temp Below Regulating)",
            subsystem = JkSubsystem.POWERTRAIN,
            severity = DtcSeverity.WARNING,
            affectedEngines = "JK 3.6L/3.8L & JL 3.6L/2.0T",
            description = "Coolant failed to reach regulating temperature within expected warm-up window, typically a stuck-open thermostat or failing ECT sensor.",
            trailGuidance = "Safe to drive at moderate speed; watch live coolant gauge and replace OEM Mopar thermostat.",
            models = both
        ),
        DtcCodeInfo(
            code = "P00B7",
            title = "Engine Coolant Flow Low / Performance (Turbo Cooling)",
            subsystem = JkSubsystem.POWERTRAIN,
            severity = DtcSeverity.CRITICAL,
            affectedEngines = "JL 2.0L Hurricane Turbo (2018–Present)",
            description = "Insufficient coolant flow on 2.0T engine. Very common on Wrangler JL 2.0L Turbo due to weeping O-ring at turbo coolant return line/plastic Y-fitting or loosened water-pump inlet bolts.",
            trailGuidance = "Inspect turbo coolant lines and expansion tank immediately! Top off OAT coolant before climbing.",
            models = jlOnly
        ),
        DtcCodeInfo(
            code = "P2181",
            title = "Cooling System Performance",
            subsystem = JkSubsystem.POWERTRAIN,
            severity = DtcSeverity.WARNING,
            affectedEngines = "JL 2.0L Turbo & 3.6L Pentastar",
            description = "Thermostat or electric grille shutter/auxiliary cooling pump failing to regulate engine coolant temperature within target range.",
            trailGuidance = "Monitor Cockpit coolant °C closely on steep climbs and verify fan high-speed engagement.",
            models = jlOnly
        ),

        // --- Misfires & Ignition (I4 / V6 / 392 V8) ---
        DtcCodeInfo(
            code = "P0300",
            title = "Random / Multiple Cylinder Misfire Detected",
            subsystem = JkSubsystem.POWERTRAIN,
            severity = DtcSeverity.CRITICAL,
            affectedEngines = "JK & JL Petrol / 4xe / 392",
            description = "Misfires across multiple cylinders. On 3.6L Pentastar: intake cam rocker tick/valve seat, fouled plugs, or intake seal; on JL 2.0T: direct-injection fuel pressure or coil moisture.",
            trailGuidance = "Avoid heavy throttle if CEL flashes (catalyst damage risk). Check coil connectors after water crossings.",
            models = both
        ),
        DtcCodeInfo(
            code = "P0301",
            title = "Cylinder 1 Misfire Detected",
            subsystem = JkSubsystem.POWERTRAIN,
            severity = DtcSeverity.CRITICAL,
            affectedEngines = "All JK & JL Petrol Engines",
            description = "Isolated misfire on Cylinder 1 from ignition coil, spark plug, injector, or Pentastar rocker follower wear.",
            trailGuidance = "Swap coil #1 with adjacent cylinder and monitor Mode 06 Misfire Cyl 1–4 counters in PID Scan.",
            models = both
        ),
        DtcCodeInfo(
            code = "P0302",
            title = "Cylinder 2 Misfire Detected",
            subsystem = JkSubsystem.POWERTRAIN,
            severity = DtcSeverity.CRITICAL,
            affectedEngines = "JK/JL 3.6L Pentastar & 2.0T",
            description = "Isolated misfire on Cylinder 2. Inspect ignition coil, spark plug gap, injector connector, and rocker arm tick on 3.6L engines.",
            trailGuidance = "Swap coil #2 with #4 to confirm coil fault before tearing down plenum.",
            models = both
        ),
        DtcCodeInfo(
            code = "P0303",
            title = "Cylinder 3 Misfire Detected",
            subsystem = JkSubsystem.POWERTRAIN,
            severity = DtcSeverity.CRITICAL,
            affectedEngines = "All JK & JL Petrol Engines",
            description = "Isolated misfire on Cylinder 3 due to coil-on-plug fault, fouled plug, or injector harness pinch.",
            trailGuidance = "Watch live Mode 06 Cyl 3 counter on PID Scan while idling.",
            models = both
        ),
        DtcCodeInfo(
            code = "P0304",
            title = "Cylinder 4 Misfire Detected",
            subsystem = JkSubsystem.POWERTRAIN,
            severity = DtcSeverity.CRITICAL,
            affectedEngines = "JK 3.6L/3.8L & JL 2.0T/3.6L",
            description = "Isolated misfire on Cylinder 4. Common on JK 3.8L plug wires or 3.6L/2.0T coil/plug wear.",
            trailGuidance = "Check plug wire seating (3.8L) or coil harness latch (3.6L / 2.0T).",
            models = both
        ),
        DtcCodeInfo(
            code = "P0305",
            title = "Cylinder 5 Misfire Detected",
            subsystem = JkSubsystem.POWERTRAIN,
            severity = DtcSeverity.CRITICAL,
            affectedEngines = "JK/JL 3.6L V6 & 392 V8",
            description = "Cylinder 5 ignition or fuel misfire (driver-side bank on 3.6L Pentastar).",
            trailGuidance = "Check coil connector latch and spark plug well for water/oil contamination.",
            models = both
        ),
        DtcCodeInfo(
            code = "P0306",
            title = "Cylinder 6 Misfire Detected",
            subsystem = JkSubsystem.POWERTRAIN,
            severity = DtcSeverity.CRITICAL,
            affectedEngines = "JK/JL 3.6L V6 & 392 V8",
            description = "Cylinder 6 misfire (rear driver cylinder on V6). Often fouled spark plug, coil pack, or intake gasket seepage.",
            trailGuidance = "Monitor Mode 06 Cyl 6 misfire count on PID Scan.",
            models = both
        ),
        DtcCodeInfo(
            code = "P0307",
            title = "Cylinder 7 Misfire Detected",
            subsystem = JkSubsystem.POWERTRAIN,
            severity = DtcSeverity.CRITICAL,
            affectedEngines = "JL 6.4L 392 HEMI V8",
            description = "Isolated misfire on Cylinder 7 on Wrangler Rubicon 392 V8 (coil packs, dual spark plugs, or MDS lifter).",
            trailGuidance = "Monitor Mode 06 Misfire Cyl 7 (06A8) in PID Scan; avoid WOT until verified.",
            models = jlOnly
        ),
        DtcCodeInfo(
            code = "P0308",
            title = "Cylinder 8 Misfire Detected",
            subsystem = JkSubsystem.POWERTRAIN,
            severity = DtcSeverity.CRITICAL,
            affectedEngines = "JL 6.4L 392 HEMI V8",
            description = "Isolated misfire on Cylinder 8 on Wrangler Rubicon 392 V8.",
            trailGuidance = "Monitor Mode 06 Misfire Cyl 8 (06A9) in PID Scan and check rear passenger coil harness.",
            models = jlOnly
        ),

        // --- JL 2.0L Turbo (Hurricane GME) Specific ---
        DtcCodeInfo(
            code = "P0299",
            title = "Turbocharger Underboost Condition",
            subsystem = JkSubsystem.POWERTRAIN,
            severity = DtcSeverity.WARNING,
            affectedEngines = "JL 2.0L Turbo, 4xe & 3.0L EcoDiesel",
            description = "Actual boost pressure (PID 0170 / MAP) fell significantly below ECM target. Common causes on JL: popped intercooler charge pipe boot, leaking wastegate actuator rod, or clogged air filter.",
            trailGuidance = "Check lower and upper charge-air silicone hoses for blown clamps after deep mud or steep hits.",
            models = jlOnly
        ),
        DtcCodeInfo(
            code = "P0234",
            title = "Turbocharger Overboost Condition",
            subsystem = JkSubsystem.POWERTRAIN,
            severity = DtcSeverity.CRITICAL,
            affectedEngines = "JL 2.0L Turbo & 3.0L EcoDiesel",
            description = "Boost pressure exceeded safe limit; ECU cuts throttle/fuel (limp mode) to protect pistons. Usually sticking electronic wastegate actuator or MAP/boost sensor drift.",
            trailGuidance = "Cycle ignition to clear temporary limp mode if hose is intact; avoid high RPM climbing.",
            models = jlOnly
        ),
        DtcCodeInfo(
            code = "P0087",
            title = "Fuel Rail / System Pressure Too Low",
            subsystem = JkSubsystem.POWERTRAIN,
            severity = DtcSeverity.CRITICAL,
            affectedEngines = "JL 2.0L Turbo GDI & 3.0L EcoDiesel",
            description = "High-pressure fuel pump (HPFP) or lift pump failed to achieve target rail pressure (PID 0123). Can trigger no-start or limp mode.",
            trailGuidance = "Check Fuel Rail Pressure on PID Scan while cranking/idling; verify fuel filter/water separator (EcoDiesel).",
            models = jlOnly
        ),
        DtcCodeInfo(
            code = "P0016",
            title = "Crankshaft / Camshaft Position Correlation (Bank 1 Sensor A)",
            subsystem = JkSubsystem.POWERTRAIN,
            severity = DtcSeverity.CRITICAL,
            affectedEngines = "JK/JL 3.6L Pentastar & JL 2.0L Turbo",
            description = "Timing correlation mismatch caused by stuck VVT oil control valve/phaser, low oil level, or stretched timing chain.",
            trailGuidance = "Inspect oil level and cleanliness immediately before running higher RPM.",
            models = both
        ),
        DtcCodeInfo(
            code = "P0017",
            title = "Crankshaft / Camshaft Exhaust Correlation (Bank 1 Sensor B)",
            subsystem = JkSubsystem.POWERTRAIN,
            severity = DtcSeverity.CRITICAL,
            affectedEngines = "JL 2.0L Turbo & 3.6L Pentastar",
            description = "Exhaust camshaft phase lags ECM target. Often dirty VVT solenoid or low oil pressure.",
            trailGuidance = "Check oil level and inspect VVT actuator connector on front timing cover.",
            models = both
        ),

        // --- Oil Pressure & Pentastar Valvetrain (JK & JL) ---
        DtcCodeInfo(
            code = "P0520",
            title = "Engine Oil Pressure Sensor / Switch Circuit",
            subsystem = JkSubsystem.POWERTRAIN,
            severity = DtcSeverity.CRITICAL,
            affectedEngines = "JK & JL 3.6L Pentastar / 3.0L EcoDiesel",
            description = "Oil pressure sensor circuit out of range, frequently caused by oil cooler housing sensor seepage or failing sender under intake valley.",
            trailGuidance = "Verify physical oil level on dipstick and listen for valvetrain noise before driving further.",
            models = both
        ),
        DtcCodeInfo(
            code = "P0522",
            title = "Engine Oil Pressure Sensor Circuit Low",
            subsystem = JkSubsystem.POWERTRAIN,
            severity = DtcSeverity.CRITICAL,
            affectedEngines = "JK & JL 3.6L Pentastar / 392",
            description = "Oil pressure voltage below threshold — either shorted sensor/oil filter housing leak or actual low mechanical oil pressure.",
            trailGuidance = "Shut down engine if knocking is heard; check oil valley for pooled oil.",
            models = both
        ),
        DtcCodeInfo(
            code = "P06DD",
            title = "Engine Oil Pressure Control Circuit Stuck Off",
            subsystem = JkSubsystem.POWERTRAIN,
            severity = DtcSeverity.CRITICAL,
            affectedEngines = "JK & JL 3.6L Pentastar Dual-Stage Pump",
            description = "Dual-stage oil pump solenoid failed to switch to high-pressure mode above ~3,500 RPM, or oil filter bypass/center stem is missing.",
            trailGuidance = "Keep RPM under 3,000 RPM and inspect oil filter cap center stem immediately.",
            models = both
        ),

        // --- JL 3.0L EcoDiesel Specific ---
        DtcCodeInfo(
            code = "P20EE",
            title = "SCR NOx Catalyst Efficiency Below Threshold (Bank 1)",
            subsystem = JkSubsystem.POWERTRAIN,
            severity = DtcSeverity.WARNING,
            affectedEngines = "JL 3.0L EcoDiesel V6 (2020–2023)",
            description = "Selective Catalytic Reduction efficiency below target. Often crystallized DEF injector nozzle, weak DEF fluid, or failing downstream NOx sensor.",
            trailGuidance = "Verify DEF tank level and quality; clean crusty white DEF deposits around injector tip when cool.",
            models = jlOnly
        ),
        DtcCodeInfo(
            code = "P207F",
            title = "Reductant (DEF) Quality Performance",
            subsystem = JkSubsystem.POWERTRAIN,
            severity = DtcSeverity.WARNING,
            affectedEngines = "JL 3.0L EcoDiesel V6",
            description = "Stale, contaminated, or diluted Diesel Exhaust Fluid detected — may trigger 200-mile/320-km speed-limp countdown if ignored.",
            trailGuidance = "Top off with fresh ISO 22241 certified DEF; avoid driving long remote trails until countdown clears.",
            models = jlOnly
        ),
        DtcCodeInfo(
            code = "P2463",
            title = "Diesel Particulate Filter (DPF) Restriction — Soot Accumulation",
            subsystem = JkSubsystem.POWERTRAIN,
            severity = DtcSeverity.CRITICAL,
            affectedEngines = "JL 3.0L EcoDiesel & JK 2.8L CRD",
            description = "DPF soot load exceeded passive/active regeneration limit due to prolonged slow low-RPM trail crawling without highway regen.",
            trailGuidance = "As soon as off trail, sustain 70+ KPH highway cruise for 20–30 minutes to complete active DPF regeneration.",
            models = both
        ),
        DtcCodeInfo(
            code = "P24AE",
            title = "Particulate Matter (PM) Sensor Circuit",
            subsystem = JkSubsystem.POWERTRAIN,
            severity = DtcSeverity.WARNING,
            affectedEngines = "JL 3.0L EcoDiesel V6",
            description = "Exhaust PM soot sensor heater or sensing element fault behind DPF.",
            trailGuidance = "Inspect PM sensor tailpipe harness along rear frame rail for trail snags or heat shielding tears.",
            models = jlOnly
        ),
        DtcCodeInfo(
            code = "P2681",
            title = "Engine Coolant Bypass Valve Control Circuit",
            subsystem = JkSubsystem.POWERTRAIN,
            severity = DtcSeverity.WARNING,
            affectedEngines = "JL 3.0L EcoDiesel V6",
            description = "Three-way electric coolant bypass actuator open/sticking — prevents proper warm-up or EGR cooler flow.",
            trailGuidance = "Monitor live coolant °C; safe to limp home if temp stays under 105°C.",
            models = jlOnly
        ),
        DtcCodeInfo(
            code = "P2002",
            title = "DPF Efficiency Below Threshold (Bank 1)",
            subsystem = JkSubsystem.POWERTRAIN,
            severity = DtcSeverity.WARNING,
            affectedEngines = "JL 3.0L EcoDiesel & JK 2.8L CRD",
            description = "Differential pressure across DPF indicates cracked substrate or plugged exhaust pressure sensor hose.",
            trailGuidance = "Check rubber differential pressure hoses near firewall/exhaust tunnel for splits.",
            models = both
        ),

        // --- JL 4xe Plug-In Hybrid Specific ---
        DtcCodeInfo(
            code = "P0BBE",
            title = "Hybrid Battery Pack Voltage Variation Exceeded Limit",
            subsystem = JkSubsystem.POWERTRAIN,
            severity = DtcSeverity.CRITICAL,
            affectedEngines = "JL Wrangler 4xe PHEV (2021+)",
            description = "High-Voltage (HV) battery cell group imbalance detected during charge/discharge — well-known 4xe cell module TSB (covered under HV warranty).",
            trailGuidance = "Hybrid/e-Save may disable and fallback to gas engine only; inspect Battery Pack % (PID 015B) and visit dealer.",
            models = jlOnly
        ),
        DtcCodeInfo(
            code = "P0B24",
            title = "Hybrid Battery 'A' Voltage Unstable / Sense Fault",
            subsystem = JkSubsystem.POWERTRAIN,
            severity = DtcSeverity.CRITICAL,
            affectedEngines = "JL Wrangler 4xe PHEV",
            description = "BPCM detected erratic HV cell sense voltage or loose HV service disconnect interlock under rear seat.",
            trailGuidance = "Avoid deep water crossings until HV warning clears; check green HV interlock latch under rear bench.",
            models = jlOnly
        ),
        DtcCodeInfo(
            code = "P061B",
            title = "Internal Control Module Torque Calculation Performance",
            subsystem = JkSubsystem.POWERTRAIN,
            severity = DtcSeverity.CRITICAL,
            affectedEngines = "JL 4xe PHEV & 2.0T",
            description = "PCM/HCP torque plausibility mismatch (frequent on 4xe during abrupt regen-to-4Low transitions or aftermarket pedal controllers).",
            trailGuidance = "Remove inline throttle-pedal boosters if installed; full sleep cycle (15 min off) often resets transient fault.",
            models = jlOnly
        ),
        DtcCodeInfo(
            code = "P1444",
            title = "Hybrid / EV Battery Coolant Heater or Aux Pump Performance",
            subsystem = JkSubsystem.POWERTRAIN,
            severity = DtcSeverity.WARNING,
            affectedEngines = "JL Wrangler 4xe PHEV",
            description = "Electric HV battery coolant heater / IDCM loop flow or pump fault — very common JL 4xe winter TSB causing 'Service Charging System'.",
            trailGuidance = "Safe to drive in Hybrid/e-Save mode; check dual coolant reservoirs under hood.",
            models = jlOnly
        ),
        DtcCodeInfo(
            code = "U0293",
            title = "Lost Communication with Hybrid Powertrain Control Module (HCP)",
            subsystem = JkSubsystem.TIPM_NETWORK,
            severity = DtcSeverity.CRITICAL,
            affectedEngines = "JL Wrangler 4xe PHEV",
            description = "CAN-C bus lost communication with Hybrid Control Processor / P1 motor inverter.",
            trailGuidance = "Check 12V battery terminals (4xe requires healthy 12V AGM to close HV contactors!).",
            models = jlOnly
        ),

        // --- Emissions, O2, Intake & Fuel Trims ---
        DtcCodeInfo(
            code = "P0420",
            title = "Catalyst System Efficiency Below Threshold (Bank 1)",
            subsystem = JkSubsystem.POWERTRAIN,
            severity = DtcSeverity.WARNING,
            affectedEngines = "All JK & JL Petrol Engines",
            description = "Bank 1 catalytic converter efficiency degraded or downstream O2 sensor lazy.",
            trailGuidance = "Check exhaust manifold/Y-pipe for rock impacts or cracked manifold flanges.",
            models = both
        ),
        DtcCodeInfo(
            code = "P0430",
            title = "Catalyst System Efficiency Below Threshold (Bank 2)",
            subsystem = JkSubsystem.POWERTRAIN,
            severity = DtcSeverity.WARNING,
            affectedEngines = "JK/JL 3.6L, 3.8L & 392 V8",
            description = "Bank 2 catalytic converter efficiency below threshold or exhaust leak ahead of downstream O2 sensor.",
            trailGuidance = "Inspect driver-side exhaust flange and O2 sensor harness clearance near front driveshaft.",
            models = both
        ),
        DtcCodeInfo(
            code = "P0171",
            title = "Fuel System Too Lean (Bank 1)",
            subsystem = JkSubsystem.POWERTRAIN,
            severity = DtcSeverity.WARNING,
            affectedEngines = "All JK & JL Petrol Engines",
            description = "Long-term fuel trim exceeding positive threshold due to unmetered air (cracked intake boot, intake plenum gasket) or weak fuel pump.",
            trailGuidance = "Watch LTFT B1/B2 on PID Scan; inspect cold-air intake couplers for dislodged clamps.",
            models = both
        ),
        DtcCodeInfo(
            code = "P0174",
            title = "Fuel System Too Lean (Bank 2)",
            subsystem = JkSubsystem.POWERTRAIN,
            severity = DtcSeverity.WARNING,
            affectedEngines = "JK/JL V6 & 392 V8",
            description = "Bank 2 running lean. Combined with P0171, points strongly to vacuum leak after throttle body or low fuel pressure.",
            trailGuidance = "Check brake booster vacuum hose and PCV line behind throttle body.",
            models = both
        ),
        DtcCodeInfo(
            code = "P0113",
            title = "Intake Air Temperature Sensor 1 Circuit High",
            subsystem = JkSubsystem.POWERTRAIN,
            severity = DtcSeverity.WARNING,
            affectedEngines = "All JK & JL Models",
            description = "Open IAT sensor circuit (-40°C reading). Often happens if IAT connector was left unplugged after air filter service.",
            trailGuidance = "Check 2-pin IAT sensor plug on airbox intake tube.",
            models = both
        ),
        DtcCodeInfo(
            code = "P065A",
            title = "Generator / 48V eTorque MGU Charging System Performance",
            subsystem = JkSubsystem.POWERTRAIN,
            severity = DtcSeverity.WARNING,
            affectedEngines = "JL 3.6L & 2.0T eTorque (48V MGU)",
            description = "eTorque belt-starter generator or 48V DC-DC converter failed to maintain 12V/48V charge balance.",
            trailGuidance = "Monitor battery voltage closely on Cockpit; avoid heavy winch pulls if alternator drops below 13.0V.",
            models = jlOnly
        ),

        // --- EVAP (JK & JL) ---
        DtcCodeInfo(
            code = "P0456",
            title = "EVAP System Very Small Leak Detected",
            subsystem = JkSubsystem.EVAP,
            severity = DtcSeverity.INFO,
            affectedEngines = "All Wrangler JK & JL",
            description = "0.020-inch EVAP leak. Common due to loose gas cap, cracked ESIM switch, or rock/brush damage on charcoal canister lines.",
            trailGuidance = "Inspect fuel cap seal (click 3×) and check EVAP hoses tucked above rear axle/driver frame rail.",
            models = both
        ),
        DtcCodeInfo(
            code = "P0457",
            title = "EVAP System Leak Detected (Fuel Cap Loose / Off)",
            subsystem = JkSubsystem.EVAP,
            severity = DtcSeverity.INFO,
            affectedEngines = "All Wrangler JK & JL",
            description = "Large EVAP pressure loss detected immediately after refueling or cap seal failure.",
            trailGuidance = "Clean dust/mud off filler neck O-ring and re-tighten fuel cap.",
            models = both
        ),
        DtcCodeInfo(
            code = "P0496",
            title = "EVAP System High Purge Flow (Stuck Open Purge Solenoid)",
            subsystem = JkSubsystem.EVAP,
            severity = DtcSeverity.WARNING,
            affectedEngines = "JL 2.0T / 3.6L & JK 3.6L",
            description = "Purge solenoid valve stuck open drawing raw fuel vapor into manifold — causes hard starting immediately after fueling.",
            trailGuidance = "Hold throttle ~10% while cranking if flooded after refueling; replace purge valve under hood.",
            models = both
        ),
        DtcCodeInfo(
            code = "P0440",
            title = "EVAP System General Malfunction",
            subsystem = JkSubsystem.EVAP,
            severity = DtcSeverity.INFO,
            affectedEngines = "All Wrangler JK & JL",
            description = "Purge valve or ESIM (Evaporative System Integrity Monitor) failed self-test.",
            trailGuidance = "No drivability impact on trail; inspect white plastic ESIM module attached to canister.",
            models = both
        ),

        // --- Transmissions (JK 42RLE / W5A580 & JL 850RE / 8HP75 ZF 8-Speed) ---
        DtcCodeInfo(
            code = "P0700",
            title = "Transmission Control System (MIL Request)",
            subsystem = JkSubsystem.TRANSMISSION,
            severity = DtcSeverity.CRITICAL,
            affectedEngines = "JK 42RLE/W5A580 & JL 850RE/8HP75",
            description = "TCM requested Check Engine Light due to an internal transmission fault (often limp-home gear hold).",
            trailGuidance = "Inspect transmission pan harness plug for mud/water intrusion and verify ATF temp/level.",
            models = both
        ),
        DtcCodeInfo(
            code = "P0730",
            title = "Incorrect Gear Ratio (ZF 8-Speed / Auto)",
            subsystem = JkSubsystem.TRANSMISSION,
            severity = DtcSeverity.CRITICAL,
            affectedEngines = "JL 850RE / 8HP75 & JK W5A580",
            description = "Measured input/output shaft RPM ratio disagrees with commanded gear — often mismatched tire/gear coding without Tazer/JScan calibration or low ATF.",
            trailGuidance = "If recently re-geared or fitted 35\"–37\" tires on JL, re-program tire size & axle ratio in BCM/TCM!",
            models = both
        ),
        DtcCodeInfo(
            code = "P0740",
            title = "Torque Converter Clutch Circuit Performance",
            subsystem = JkSubsystem.TRANSMISSION,
            severity = DtcSeverity.WARNING,
            affectedEngines = "JK 42RLE/W5A580 & JL 850RE/8HP75",
            description = "TCC lockup slip or solenoid delay, sometimes triggered by overheating ATF on steep slow climbs in 4-High.",
            trailGuidance = "Shift transfer case into 4-Low on steep grades to reduce torque converter heat buildup.",
            models = both
        ),
        DtcCodeInfo(
            code = "P0882",
            title = "TCM Power Input Signal Low",
            subsystem = JkSubsystem.TRANSMISSION,
            severity = DtcSeverity.CRITICAL,
            affectedEngines = "JK & JL Automatics",
            description = "Transmission Control Module saw voltage drop below threshold — classic JL Auxiliary ESS battery failure or JK TIPM relay issue.",
            trailGuidance = "Load-test main & Aux ESS batteries; weak JL Aux battery pulls main system voltage down on start.",
            models = both
        ),

        // --- TIPM (JK) & BCM / SGW / Aux ESS Battery / CAN-Bus (JL) ---
        DtcCodeInfo(
            code = "U0001",
            title = "High Speed CAN Communication Bus Off / Fault",
            subsystem = JkSubsystem.TIPM_NETWORK,
            severity = DtcSeverity.CRITICAL,
            affectedEngines = "JL (Star CAN Splice Block) & JK",
            description = "Total or intermittent CAN-C bus short/open causing dash 'Christmas tree', dead gauges, or power steering dropout. Famous Wrangler JL issue: green CAN-C star connector behind glovebox!",
            trailGuidance = "On JL: pull glovebox down and inspect green/white CAN star junction blocks for loose terminals or corrosion.",
            models = both
        ),
        DtcCodeInfo(
            code = "U0140",
            title = "Lost Communication with Body Control Module (BCM)",
            subsystem = JkSubsystem.TIPM_NETWORK,
            severity = DtcSeverity.CRITICAL,
            affectedEngines = "All 2018+ Wrangler JL (BCM / SGW)",
            description = "CAN bus lost contact with JL BCM. Frequently caused by failing small Auxiliary Start/Stop battery under fuse box, loose green CAN star connector, or bad aftermarket SGW bypass cable.",
            trailGuidance = "Check JL Aux ESS battery health & green CAN star connector behind glovebox; ensure SGW 12+8 bypass is seated.",
            models = jlOnly
        ),
        DtcCodeInfo(
            code = "U0141",
            title = "Lost Communication with TIPM / Front Control Module",
            subsystem = JkSubsystem.TIPM_NETWORK,
            severity = DtcSeverity.CRITICAL,
            affectedEngines = "All JK (2007–2018)",
            description = "Totally Integrated Power Module (TIPM) failed to respond on CAN-IHS/CAN-C bus. Classic JK symptom of loose battery ground or moisture inside TIPM.",
            trailGuidance = "Check main ground strap on passenger inner fender and battery negative post.",
            models = jkOnly
        ),
        DtcCodeInfo(
            code = "U0100",
            title = "Lost Communication with ECM / PCM",
            subsystem = JkSubsystem.TIPM_NETWORK,
            severity = DtcSeverity.CRITICAL,
            affectedEngines = "All JK TIPM & JL BCM/CAN-C",
            description = "CAN-C bus communication dropout between TIPM/BCM, cluster, or PCM. Often caused by low battery voltage, loose terminals, or CAN splice resistance.",
            trailGuidance = "Check resting voltage (>12.2V) and tighten main & auxiliary battery ground clamps.",
            models = both
        ),
        DtcCodeInfo(
            code = "U0101",
            title = "Lost Communication with TCM",
            subsystem = JkSubsystem.TIPM_NETWORK,
            severity = DtcSeverity.CRITICAL,
            affectedEngines = "Automatic JK & JL Models",
            description = "CAN bus lost communication with Transmission Control Module. Inspect W5A580/850RE/8HP75 bulkhead connector or exhaust-side harness routing.",
            trailGuidance = "Check transmission harness near exhaust crossover for melted insulation or trail mud.",
            models = both
        ),
        DtcCodeInfo(
            code = "U0121",
            title = "Lost Communication with ABS Module",
            subsystem = JkSubsystem.TIPM_NETWORK,
            severity = DtcSeverity.WARNING,
            affectedEngines = "All JK & JL (ESP / ABS)",
            description = "CAN bus dropout with Anti-Lock Brake / ESP module. Triggers dash warnings and disables lockers/sway-bar disconnect.",
            trailGuidance = "Inspect ABS module connector & check main/aux battery terminal voltage.",
            models = both
        ),
        DtcCodeInfo(
            code = "B210A",
            title = "System Voltage Low (TIPM / BCM / Aux Battery)",
            subsystem = JkSubsystem.TIPM_NETWORK,
            severity = DtcSeverity.WARNING,
            affectedEngines = "All JK & JL (ESS Aux Battery)",
            description = "System voltage dropped below ~9.5V during winching or due to a failing JL Auxiliary Start/Stop motorcycle-style battery draining the main AGM.",
            trailGuidance = "Monitor Battery Volts on Cockpit; on JL with 'ESS Unavailable' messages, disconnect/replace Aux battery.",
            models = both
        ),
        DtcCodeInfo(
            code = "B2204",
            title = "ECU Configuration Mismatch (Tire / Axle / LED Mod)",
            subsystem = JkSubsystem.TIPM_NETWORK,
            severity = DtcSeverity.INFO,
            affectedEngines = "JL & JK After Re-Gear / Tazer / JScan",
            description = "BCM/TCM detected tire diameter, axle ratio, or lighting configuration mismatch after aftermarket programming or module swap.",
            trailGuidance = "Run BCM/PCM configuration sync / proxy alignment after programming new tire size or lockers.",
            models = both
        ),
        DtcCodeInfo(
            code = "B162C",
            title = "Left Low Beam Control Circuit High (TIPM)",
            subsystem = JkSubsystem.TIPM_NETWORK,
            severity = DtcSeverity.INFO,
            affectedEngines = "JK with LED Headlight Swap",
            description = "TIPM detected low current draw on headlight circuit when LED headlights are installed without anti-flicker CAN resistors.",
            trailGuidance = "Add CAN-bus anti-flicker harness or enable LED headlight mode.",
            models = jkOnly
        ),

        // --- ABS / 4WD / Axles (JK & JL FAD) ---
        DtcCodeInfo(
            code = "C140F",
            title = "Front Axle Disconnect (FAD) Actuator Position / Circuit",
            subsystem = JkSubsystem.ABS_CHASSIS,
            severity = DtcSeverity.WARNING,
            affectedEngines = "2018+ Wrangler JL Front Dana 30/44",
            description = "JL Front Axle Disconnect motor or fork position sensor failed to engage/disengage when shifting 2H <-> 4H/4L (causes flashing 'SERV 4WD').",
            trailGuidance = "Inspect 4-pin FAD actuator harness on passenger side of front axle tube for rock/stick damage!",
            models = jlOnly
        ),
        DtcCodeInfo(
            code = "C1020",
            title = "Left Rear Wheel Speed Sensor Circuit",
            subsystem = JkSubsystem.ABS_CHASSIS,
            severity = DtcSeverity.WARNING,
            affectedEngines = "All JK & JL Dana Axles",
            description = "ABS / Traction / ESP warning caused by sheared wheel speed sensor wire, mud-packed tone ring, or failing axle bearing seal.",
            trailGuidance = "4WD still operates mechanically, though traction control/hill descent disables. Inspect rear sensor lead.",
            models = both
        ),
        DtcCodeInfo(
            code = "C102A",
            title = "Left Rear Wheel Speed Comparative Performance",
            subsystem = JkSubsystem.ABS_CHASSIS,
            severity = DtcSeverity.WARNING,
            affectedEngines = "All JK & JL Models",
            description = "Intermittent wheel speed signal drop vs other 3 wheels; frequently caused by stretched sensor wire at full suspension droop.",
            trailGuidance = "Verify slack in ABS wire clips at full articulation.",
            models = both
        ),
        DtcCodeInfo(
            code = "C121C",
            title = "Torque Request Signal Denied (Steering Off-Center)",
            subsystem = JkSubsystem.ABS_CHASSIS,
            severity = DtcSeverity.INFO,
            affectedEngines = "Lifted JK & JL Models",
            description = "ESP/BAS triggered because steering wheel is off-center after trail tie-rod or drag-link hit.",
            trailGuidance = "Re-center drag link turnbuckle so steering wheel sits straight while tracking ahead.",
            models = both
        ),
        DtcCodeInfo(
            code = "C2200",
            title = "Anti-Lock Brake Module Internal Fault",
            subsystem = JkSubsystem.ABS_CHASSIS,
            severity = DtcSeverity.CRITICAL,
            affectedEngines = "JK & JL ABS Hydraulic Control Unit",
            description = "Internal ABS ECU hardware fault or severe under-voltage event during cranking.",
            trailGuidance = "Verify charging voltage (>13.6V running) and clear codes after battery check.",
            models = both
        ),

        // --- Additional Jeep Wrangler JK Specific Codes ---
        DtcCodeInfo(
            code = "P0340",
            title = "Camshaft Position Sensor A Circuit (Bank 1)",
            subsystem = JkSubsystem.POWERTRAIN,
            severity = DtcSeverity.CRITICAL,
            affectedEngines = "JK 3.8L V6 & 3.6L Pentastar",
            description = "Missing or erratic camshaft position signal causing extended cranking, stalling, or limp mode on Wrangler JK.",
            trailGuidance = "Inspect cam sensor harness on passenger cylinder head and verify sensor bolt tightness.",
            models = jkOnly
        ),
        DtcCodeInfo(
            code = "P0344",
            title = "Camshaft Position Sensor A Intermittent (Bank 1)",
            subsystem = JkSubsystem.POWERTRAIN,
            severity = DtcSeverity.WARNING,
            affectedEngines = "JK 3.6L Pentastar & 3.8L V6",
            description = "Intermittent cam sensor signal from damaged wiring near alternator bracket, dirty reluctor wheel, or failing Hall-effect sensor.",
            trailGuidance = "Check cam sensor pigtail insulation and oil level; replace with OEM Mopar cam sensor.",
            models = jkOnly
        ),
        DtcCodeInfo(
            code = "P0335",
            title = "Crankshaft Position Sensor A Circuit",
            subsystem = JkSubsystem.POWERTRAIN,
            severity = DtcSeverity.CRITICAL,
            affectedEngines = "JK 3.8L V6 & 3.6L Pentastar",
            description = "Crankshaft position sensor dropout — very common Wrangler JK cause of sudden hot stall and no-tachometer cranking.",
            trailGuidance = "If JK stalls hot and restarts after cooling 15 min, CKP sensor heat-soak failure is likely.",
            models = jkOnly
        ),
        DtcCodeInfo(
            code = "P0339",
            title = "Crankshaft Position Sensor A Circuit Intermittent",
            subsystem = JkSubsystem.POWERTRAIN,
            severity = DtcSeverity.WARNING,
            affectedEngines = "JK 3.6L / 3.8L V6",
            description = "Momentary loss of crankshaft tooth synchronization from mud/water in bellhousing connector or worn CKP sensor.",
            trailGuidance = "Clean lower bellhousing CKP connector on passenger side after water crossings.",
            models = jkOnly
        ),
        DtcCodeInfo(
            code = "P0507",
            title = "Idle Air Control / ETC RPM Higher Than Expected",
            subsystem = JkSubsystem.POWERTRAIN,
            severity = DtcSeverity.WARNING,
            affectedEngines = "JK 3.8L V6 & 3.6L Pentastar",
            description = "Idle speed persistently above target due to carboned electronic throttle body bore, split PCV hose, or intake plenum leak.",
            trailGuidance = "Clean throttle body blade and inspect vacuum hoses around upper intake manifold.",
            models = jkOnly
        ),
        DtcCodeInfo(
            code = "P0108",
            title = "Manifold Absolute Pressure (MAP) Circuit High",
            subsystem = JkSubsystem.POWERTRAIN,
            severity = DtcSeverity.WARNING,
            affectedEngines = "JK 3.8L V6 & 3.6L Pentastar",
            description = "MAP sensor reading stuck near max voltage (~5V) due to broken vacuum reference, failed sensor, or wiring open.",
            trailGuidance = "Watch Intake MAP (kPa) live on Cockpit; unplug/re-seat MAP connector on intake manifold.",
            models = jkOnly
        ),
        DtcCodeInfo(
            code = "P0455",
            title = "EVAP System Large Leak Detected",
            subsystem = JkSubsystem.EVAP,
            severity = DtcSeverity.INFO,
            affectedEngines = "All Wrangler JK (2007–2018)",
            description = "Gross EVAP system vapor leak — classic Wrangler JK issue from torn EVAP canister hose behind rear axle or missing fuel cap.",
            trailGuidance = "Visually inspect charcoal canister and hard/rubber hoses above rear track bar mount.",
            models = jkOnly
        ),
        DtcCodeInfo(
            code = "P0562",
            title = "System Voltage Low",
            subsystem = JkSubsystem.POWERTRAIN,
            severity = DtcSeverity.WARNING,
            affectedEngines = "All Wrangler JK",
            description = "PCM sensed battery bus below ~10.8V while engine running — weak alternator, failing battery cell, or corroded TIPM main cable.",
            trailGuidance = "Check live ATRV battery volts; turn off off-road light bars and winch until voltage stabilizes >13.2V.",
            models = jkOnly
        ),
        DtcCodeInfo(
            code = "P2096",
            title = "Post Catalyst Fuel Trim System Too Lean (Bank 1)",
            subsystem = JkSubsystem.POWERTRAIN,
            severity = DtcSeverity.WARNING,
            affectedEngines = "JK 3.8L V6 & 3.6L Pentastar",
            description = "Downstream O2 sensor detects lean exhaust after catalyst — classic Wrangler JK 3.8L cracked passenger exhaust manifold.",
            trailGuidance = "Listen for cold-start exhaust tick on passenger manifold; monitor STFT B1 on PID Scan.",
            models = jkOnly
        ),
        DtcCodeInfo(
            code = "P2098",
            title = "Post Catalyst Fuel Trim System Too Lean (Bank 2)",
            subsystem = JkSubsystem.POWERTRAIN,
            severity = DtcSeverity.WARNING,
            affectedEngines = "JK 3.8L V6 & 3.6L Pentastar",
            description = "Driver-side post-cat lean trim from cracked exhaust manifold, leaking Y-pipe flange gasket, or aging downstream O2 sensor.",
            trailGuidance = "Inspect driver-side Y-pipe flange bolts and O2 harness clearance.",
            models = jkOnly
        ),
        DtcCodeInfo(
            code = "P0325",
            title = "Knock Sensor 1 Circuit (Bank 1)",
            subsystem = JkSubsystem.POWERTRAIN,
            severity = DtcSeverity.WARNING,
            affectedEngines = "JK 3.6L Pentastar & 3.8L V6",
            description = "Knock sensor open/shorted or rodent/heat damage to valley harness under lower intake manifold.",
            trailGuidance = "ECU retards ignition timing slightly as safeguard; check valley wiring when servicing oil cooler.",
            models = jkOnly
        ),
        DtcCodeInfo(
            code = "P0715",
            title = "Input / Turbine Speed Sensor A Circuit (W5A580)",
            subsystem = JkSubsystem.TRANSMISSION,
            severity = DtcSeverity.CRITICAL,
            affectedEngines = "JK 2012–2018 W5A580 5-Speed Auto",
            description = "NAG1/W5A580 conductor plate speed sensor dropout locking transmission in 2nd-gear limp mode.",
            trailGuidance = "Inspect 13-pin transmission bulkhead connector for ATF wicking; cycle key to reset limp temporarily.",
            models = jkOnly
        ),
        DtcCodeInfo(
            code = "C1014",
            title = "Left Front Wheel Speed Comparative Performance",
            subsystem = JkSubsystem.ABS_CHASSIS,
            severity = DtcSeverity.WARNING,
            affectedEngines = "Wrangler JK Dana 30 / Dana 44 Front Axle",
            description = "Front left ABS wheel speed signal erratic due to worn unit bearing hub, rusted tone ring, or mud-jammed sensor tip.",
            trailGuidance = "Wash mud out of front knuckle/hub area and check unit bearing play.",
            models = jkOnly
        ),
        DtcCodeInfo(
            code = "U110C",
            title = "Lost Fuel Level Message (TIPM to PCM)",
            subsystem = JkSubsystem.TIPM_NETWORK,
            severity = DtcSeverity.INFO,
            affectedEngines = "All Wrangler JK (2007–2018)",
            description = "PCM stopped receiving fuel tank level percentage over CAN from the JK Totally Integrated Power Module (TIPM).",
            trailGuidance = "Check fuel pump sender harness above rear tank and inspect TIPM M25/M30 fuses.",
            models = jkOnly
        ),
        DtcCodeInfo(
            code = "B1630",
            title = "Right Low Beam Control Circuit High (TIPM)",
            subsystem = JkSubsystem.TIPM_NETWORK,
            severity = DtcSeverity.INFO,
            affectedEngines = "JK with LED Headlight Conversion",
            description = "TIPM open-load fault on passenger headlight driver caused by aftermarket LED lights without PWM anti-flicker load.",
            trailGuidance = "Install anti-flicker CANbus decoder harness or flash TIPM LED mode.",
            models = jkOnly
        )
    )
}
