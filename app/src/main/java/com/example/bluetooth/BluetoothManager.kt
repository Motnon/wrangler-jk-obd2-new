package com.example.bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager as AndroidSystemBluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.BluetoothSocket
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.example.obd.BluetoothDeviceItem
import com.example.obd.ConnectionStatus
import com.example.obd.DiagnosticReading
import com.example.obd.DiagnosticScanEngine
import com.example.obd.DiagnosticTestId
import com.example.obd.Elm327Protocol
import com.example.obd.LiveTelemetry
import com.example.obd.TerminalDirection
import com.example.obd.TerminalEntry
import com.example.obd.TransportType
import com.example.obd.VinInfo
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.ArrayDeque
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.concurrent.atomic.AtomicLong

/**
 * Manages Bluetooth runtime permissions (BLUETOOTH_CONNECT / BLUETOOTH_SCAN),
 * Classic SPP & BLE device discovery, and ELM327 OBD-II ECU communication.
 */
class BluetoothManager(private val context: Context) {

    companion object {
        private val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")
        private val CCCD_UUID: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

        private val PREFERRED_WRITE_FFF2: UUID = UUID.fromString("0000fff2-0000-1000-8000-00805f9b34fb")
        private val FALLBACK_WRITE_FFE1: UUID = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb")

        private const val INIT_COMMAND_TIMEOUT_MS = 8_000L
        private const val POLL_COMMAND_TIMEOUT_MS = 3_000L

        private val ELM327_INIT_SEQUENCE = listOf(
            "ATZ",    // Reset ELM327
            "ATE0",   // Echo OFF
            "ATL0",   // Linefeeds OFF
            "ATS0",   // Spaces OFF
            "ATH0",   // Headers OFF
            "ATSP6",  // ISO 15765-4 CAN 11-bit 500kbps (Wrangler JK & JL)
            "ATCAF1", // CAN Auto Formatting ON
            "0100"    // Supported PIDs probe
        )

        private val ELM327_NAME_HINTS = listOf(
            "OBD", "ELM", "VEEPEAK", "VGATE", "ICAR", "JSCAN", "OBDLINK", "SCANTOOL", "KONNWEI", "CARISTA"
        )
    }

    private data class PendingCccdWrite(
        val descriptor: BluetoothGattDescriptor,
        val enableValue: ByteArray,
        val characteristicUuid: UUID
    )

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val commandMutex = Mutex()
    private val rxBufferLock = Any()
    private val terminalIdCounter = AtomicLong(1L)
    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.US)

    private val bluetoothAdapter: BluetoothAdapter?
        get() = context.getSystemService(AndroidSystemBluetoothManager::class.java)?.adapter

    private val _permissionsGranted = MutableStateFlow(hasAllPermissions())
    val permissionsGranted: StateFlow<Boolean> = _permissionsGranted.asStateFlow()

    private val _connectionStatus = MutableStateFlow(ConnectionStatus.DISCONNECTED)
    val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus.asStateFlow()

    private val _connectedDevice = MutableStateFlow<BluetoothDeviceItem?>(null)
    val connectedDevice: StateFlow<BluetoothDeviceItem?> = _connectedDevice.asStateFlow()

    private val _statusMessage = MutableStateFlow("Ready to pair ELM327 adapter")
    val statusMessage: StateFlow<String> = _statusMessage.asStateFlow()

    private val _availableDevices = MutableStateFlow<List<BluetoothDeviceItem>>(emptyList())
    val availableDevices: StateFlow<List<BluetoothDeviceItem>> = _availableDevices.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _telemetry = MutableStateFlow(LiveTelemetry())
    val telemetry: StateFlow<LiveTelemetry> = _telemetry.asStateFlow()

    private val _activeDtcs = MutableStateFlow<List<String>>(emptyList())
    val activeDtcs: StateFlow<List<String>> = _activeDtcs.asStateFlow()

    private val _pendingDtcs = MutableStateFlow<List<String>>(emptyList())
    val pendingDtcs: StateFlow<List<String>> = _pendingDtcs.asStateFlow()

    private val _vinInfo = MutableStateFlow<VinInfo?>(null)
    val vinInfo: StateFlow<VinInfo?> = _vinInfo.asStateFlow()

    private val _sgwBlockedEvent = MutableStateFlow(false)
    val sgwBlockedEvent: StateFlow<Boolean> = _sgwBlockedEvent.asStateFlow()

    private val unsupportedStandardCommands = mutableSetOf<String>()

    private val _terminalLogs = MutableStateFlow<List<TerminalEntry>>(
        listOf(
            TerminalEntry(
                id = 0L,
                timestamp = timeFormat.format(Date()),
                direction = TerminalDirection.INFO,
                message = "ELM327 console ready. Pair Bluetooth OBD-II dongle to begin Wrangler JK/JL CAN-Bus session."
            )
        )
    )
    val terminalLogs: StateFlow<List<TerminalEntry>> = _terminalLogs.asStateFlow()

    private val diagnosticScanEngine = DiagnosticScanEngine()

    private val _appliedDiagnosticTests = MutableStateFlow<Set<DiagnosticTestId>>(emptySet())
    val appliedDiagnosticTests: StateFlow<Set<DiagnosticTestId>> = _appliedDiagnosticTests.asStateFlow()

    private val _diagnosticReadings = MutableStateFlow<Map<DiagnosticTestId, DiagnosticReading>>(emptyMap())
    val diagnosticReadings: StateFlow<Map<DiagnosticTestId, DiagnosticReading>> = _diagnosticReadings.asStateFlow()

    private val _isDiagnosticScreenActive = MutableStateFlow(false)
    private val _isDiagnosticScanPaused = MutableStateFlow(false)
    val isDiagnosticScanPaused: StateFlow<Boolean> = _isDiagnosticScanPaused.asStateFlow()

    private val _diagnosticCycleCount = MutableStateFlow(0L)
    val diagnosticCycleCount: StateFlow<Long> = _diagnosticCycleCount.asStateFlow()

    @Volatile
    private var speedCalibrationFactor: Float = 1.0f

    @Volatile
    private var coolantWarnCelsius: Int = 108

    private var classicSocket: BluetoothSocket? = null
    private var classicIn: InputStream? = null
    private var classicOut: OutputStream? = null

    private var bluetoothGatt: BluetoothGatt? = null
    private var bleWriteChar: BluetoothGattCharacteristic? = null
    private val rxBuffer = StringBuilder()
    private var bleResponseDeferred: CompletableDeferred<String>? = null
    private val pendingCccdQueue = ArrayDeque<PendingCccdWrite>()
    private var bleSetupDeferred: CompletableDeferred<Boolean>? = null

    private var pollingJob: Job? = null
    private var scanTimeoutJob: Job? = null
    private var classicReceiverRegistered = false

    private val classicDiscoveryReceiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(ctx: Context?, intent: Intent?) {
            when (intent?.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    }
                    if (device != null && hasConnectPermission()) {
                        val name = runCatching { device.name }.getOrNull()?.takeIf { it.isNotBlank() }
                            ?: "Unknown BT Device"
                        val transport = if (device.type == BluetoothDevice.DEVICE_TYPE_LE) {
                            TransportType.BLE_GATT
                        } else {
                            TransportType.CLASSIC_SPP
                        }
                        upsertDiscoveredDevice(
                            BluetoothDeviceItem(
                                name = name,
                                address = device.address,
                                isPaired = device.bondState == BluetoothDevice.BOND_BONDED,
                                transportType = transport
                            )
                        )
                    }
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    _isScanning.value = false
                }
            }
        }
    }

    private val bleScanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            if (!hasConnectPermission()) return
            val device = result.device ?: return
            val name = runCatching { device.name }.getOrNull()
                ?: result.scanRecord?.deviceName
                ?: return
            upsertDiscoveredDevice(
                BluetoothDeviceItem(
                    name = name,
                    address = device.address,
                    isPaired = device.bondState == BluetoothDevice.BOND_BONDED,
                    transportType = TransportType.BLE_GATT,
                    rssi = result.rssi
                )
            )
        }
    }

    fun requiredPermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN
            )
        } else {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        }
    }

    fun hasConnectPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) ==
                PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH) ==
                PackageManager.PERMISSION_GRANTED
        }
    }

    fun hasScanPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) ==
                PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
        }
    }

    fun hasAllPermissions(): Boolean = hasConnectPermission() && hasScanPermission()

    fun refreshPermissionState(): Boolean {
        val granted = hasAllPermissions()
        _permissionsGranted.value = granted
        if (granted) {
            refreshPairedDevices()
        }
        return granted
    }

    fun isBluetoothEnabled(): Boolean = bluetoothAdapter?.isEnabled == true

    @SuppressLint("MissingPermission")
    fun refreshPairedDevices() {
        if (!hasConnectPermission()) {
            _permissionsGranted.value = false
            _statusMessage.value = "Grant BLUETOOTH_CONNECT & BLUETOOTH_SCAN permissions"
            return
        }
        _permissionsGranted.value = hasAllPermissions()
        val adapter = bluetoothAdapter ?: run {
            _statusMessage.value = "Bluetooth hardware unavailable on this device"
            return
        }

        val paired = runCatching {
            adapter.bondedDevices.orEmpty().map { dev ->
                val transport = if (dev.type == BluetoothDevice.DEVICE_TYPE_LE) {
                    TransportType.BLE_GATT
                } else {
                    TransportType.CLASSIC_SPP
                }
                BluetoothDeviceItem(
                    name = dev.name ?: "Paired Adapter",
                    address = dev.address,
                    isPaired = true,
                    transportType = transport
                )
            }
        }.getOrDefault(emptyList())

        _availableDevices.value = paired.sortedWith(
            compareByDescending<BluetoothDeviceItem> { isLikelyElm327(it.name) }
                .thenBy { it.name }
        )
        if (!isBluetoothEnabled()) {
            _statusMessage.value = "Turn on Bluetooth to connect your Wrangler JK ELM327 dongle"
        }
    }

    @SuppressLint("MissingPermission")
    fun startDiscovery() {
        if (!hasScanPermission() || !hasConnectPermission()) {
            _permissionsGranted.value = false
            _statusMessage.value = "Grant BLUETOOTH_SCAN & BLUETOOTH_CONNECT to discover ELM327 adapters"
            return
        }
        val adapter = bluetoothAdapter ?: run {
            _statusMessage.value = "Bluetooth radio not available"
            return
        }
        if (!adapter.isEnabled) {
            _statusMessage.value = "Bluetooth is turned off"
            return
        }

        refreshPairedDevices()
        _isScanning.value = true
        _statusMessage.value = "Scanning for ELM327 OBD-II adapters…"

        if (!classicReceiverRegistered) {
            val filter = IntentFilter().apply {
                addAction(BluetoothDevice.ACTION_FOUND)
                addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
            }
            ContextCompat.registerReceiver(
                context,
                classicDiscoveryReceiver,
                filter,
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
            classicReceiverRegistered = true
        }

        runCatching {
            if (adapter.isDiscovering) adapter.cancelDiscovery()
            adapter.startDiscovery()
        }
        runCatching {
            adapter.bluetoothLeScanner?.startScan(bleScanCallback)
        }

        scanTimeoutJob?.cancel()
        scanTimeoutJob = scope.launch {
            delay(10_000L)
            stopDiscovery()
        }
    }

    @SuppressLint("MissingPermission")
    fun stopDiscovery() {
        scanTimeoutJob?.cancel()
        _isScanning.value = false
        val adapter = bluetoothAdapter ?: return
        if (hasScanPermission()) {
            runCatching { if (adapter.isDiscovering) adapter.cancelDiscovery() }
            runCatching { adapter.bluetoothLeScanner?.stopScan(bleScanCallback) }
        }
    }

    @SuppressLint("MissingPermission")
    fun connect(deviceItem: BluetoothDeviceItem) {
        if (!hasConnectPermission()) {
            _permissionsGranted.value = false
            _connectionStatus.value = ConnectionStatus.ERROR
            _statusMessage.value = "BLUETOOTH_CONNECT permission required"
            return
        }
        val adapter = bluetoothAdapter ?: run {
            _connectionStatus.value = ConnectionStatus.ERROR
            _statusMessage.value = "No Bluetooth hardware available"
            return
        }
        stopDiscovery()
        scope.launch {
            disconnectInternal(updateStatus = false)
            _connectionStatus.value = ConnectionStatus.CONNECTING
            _connectedDevice.value = deviceItem
            _statusMessage.value = "Connecting to ${deviceItem.name}…"
            appendTerminal(
                TerminalDirection.INFO,
                "Opening ${deviceItem.transportType.badge} link to ${deviceItem.name} (${deviceItem.address})"
            )

            val remoteDevice = runCatching { adapter.getRemoteDevice(deviceItem.address) }.getOrNull()
            if (remoteDevice == null) {
                failConnection("Invalid Bluetooth address ${deviceItem.address}")
                return@launch
            }

            val linked = if (deviceItem.transportType == TransportType.BLE_GATT) {
                connectBle(remoteDevice)
            } else {
                connectClassic(remoteDevice)
            }

            if (!linked) {
                failConnection("Could not open RFCOMM/GATT channel to ${deviceItem.name}")
                return@launch
            }

            _connectionStatus.value = ConnectionStatus.INITIALIZING
            _statusMessage.value = "Initializing ELM327 & Wrangler CAN-Bus (ATSP6)…"
            val initOk = initializeElm327()
            if (!initOk) {
                failConnection("Adapter did not respond to ATZ/ATSP6 initialization")
                return@launch
            }

            _connectionStatus.value = ConnectionStatus.CONNECTED
            _statusMessage.value = "ECU Link Active — ${deviceItem.name}"
            unsupportedStandardCommands.clear()
            _sgwBlockedEvent.value = false
            publishDiagnosticReadings()
            appendTerminal(
                TerminalDirection.INFO,
                "ELM327 initialized (ISO 15765-4 CAN 11-bit 500kbps). Streaming Wrangler PIDs."
            )

            queryVinInternal()
            readDtcsInternal()
            startPollingLoop()
        }
    }

    fun disconnect() {
        scope.launch {
            disconnectInternal(updateStatus = true)
        }
    }

    private suspend fun disconnectInternal(updateStatus: Boolean) {
        pollingJob?.cancel()
        pollingJob = null
        synchronized(rxBufferLock) {
            rxBuffer.clear()
            bleResponseDeferred?.cancel()
            bleResponseDeferred = null
        }
        synchronized(pendingCccdQueue) {
            pendingCccdQueue.clear()
        }
        runCatching { classicIn?.close() }
        runCatching { classicOut?.close() }
        runCatching { classicSocket?.close() }
        classicIn = null
        classicOut = null
        classicSocket = null
        runCatching {
            if (hasConnectPermission()) {
                bluetoothGatt?.disconnect()
                bluetoothGatt?.close()
            }
        }
        bluetoothGatt = null
        bleWriteChar = null
        if (updateStatus) {
            _connectionStatus.value = ConnectionStatus.DISCONNECTED
            _connectedDevice.value = null
            _statusMessage.value = "Disconnected from OBD-II adapter"
            _telemetry.value = LiveTelemetry()
            _diagnosticCycleCount.value = 0L
            unsupportedStandardCommands.clear()
            _sgwBlockedEvent.value = false
            diagnosticScanEngine.resetSession()
            publishDiagnosticReadings()
            appendTerminal(TerminalDirection.INFO, "OBD-II session closed.")
        }
    }

    private fun failConnection(reason: String) {
        _connectionStatus.value = ConnectionStatus.ERROR
        _statusMessage.value = reason
        appendTerminal(TerminalDirection.ERROR, reason)
    }

    @SuppressLint("MissingPermission")
    private suspend fun connectClassic(device: BluetoothDevice): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val socket = device.createRfcommSocketToServiceRecord(SPP_UUID)
            socket.connect()
            classicSocket = socket
            classicIn = socket.inputStream
            classicOut = socket.outputStream
            true
        } catch (_: IOException) {
            try {
                @Suppress("DiscouragedPrivateApi")
                val fallback = device.javaClass
                    .getMethod("createRfcommSocket", Int::class.javaPrimitiveType)
                    .invoke(device, 1) as BluetoothSocket
                fallback.connect()
                classicSocket = fallback
                classicIn = fallback.inputStream
                classicOut = fallback.outputStream
                true
            } catch (_: Exception) {
                false
            }
        } catch (_: SecurityException) {
            false
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun connectBle(device: BluetoothDevice): Boolean {
        val connectedSignal = CompletableDeferred<Boolean>()
        bleSetupDeferred = connectedSignal
        synchronized(pendingCccdQueue) {
            pendingCccdQueue.clear()
        }
        synchronized(rxBufferLock) {
            rxBuffer.clear()
        }

        val gattCallback = object : BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                if (newState == BluetoothProfile.STATE_CONNECTED && status == BluetoothGatt.GATT_SUCCESS) {
                    gatt.discoverServices()
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    if (!connectedSignal.isCompleted) connectedSignal.complete(false)
                    if (_connectionStatus.value == ConnectionStatus.CONNECTED ||
                        _connectionStatus.value == ConnectionStatus.INITIALIZING
                    ) {
                        scope.launch { disconnectInternal(updateStatus = true) }
                    }
                }
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                if (status != BluetoothGatt.GATT_SUCCESS) {
                    if (!connectedSignal.isCompleted) connectedSignal.complete(false)
                    return
                }

                val allCharacteristics = gatt.services.orEmpty().flatMap { it.characteristics.orEmpty() }

                // 1. Select write characteristic: prefer FFF2 -> FFE1 -> any writeable characteristic
                val selectedWrite =
                    allCharacteristics.firstOrNull {
                        it.uuid == PREFERRED_WRITE_FFF2 && isWritable(it)
                    } ?: allCharacteristics.firstOrNull {
                        it.uuid == FALLBACK_WRITE_FFE1 && isWritable(it)
                    } ?: allCharacteristics.firstOrNull {
                        isWritable(it)
                    }

                if (selectedWrite == null) {
                    appendTerminal(TerminalDirection.ERROR, "No writable GATT characteristic found on BLE adapter")
                    if (!connectedSignal.isCompleted) connectedSignal.complete(false)
                    return
                }
                bleWriteChar = selectedWrite
                appendTerminal(TerminalDirection.INFO, "BLE TX characteristic selected: ${selectedWrite.uuid}")

                // 2. Subscribe to EVERY notify or indicate characteristic across ALL discovered services (Vgate fix)
                synchronized(pendingCccdQueue) {
                    pendingCccdQueue.clear()
                    for (char in allCharacteristics) {
                        val hasNotify = (char.properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0
                        val hasIndicate = (char.properties and BluetoothGattCharacteristic.PROPERTY_INDICATE) != 0
                        if (!hasNotify && !hasIndicate) continue

                        gatt.setCharacteristicNotification(char, true)
                        val cccd = char.getDescriptor(CCCD_UUID)
                        if (cccd != null) {
                            val enableVal = if (hasNotify) {
                                BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                            } else {
                                BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
                            }
                            pendingCccdQueue.addLast(
                                PendingCccdWrite(
                                    descriptor = cccd,
                                    enableValue = enableVal,
                                    characteristicUuid = char.uuid
                                )
                            )
                        } else {
                            appendTerminal(TerminalDirection.INFO, "Notify enabled (no CCCD): ${char.uuid}")
                        }
                    }
                }

                if (!writeNextCccd(gatt)) {
                    if (!connectedSignal.isCompleted) connectedSignal.complete(true)
                }
            }

            override fun onDescriptorWrite(
                gatt: BluetoothGatt,
                descriptor: BluetoothGattDescriptor,
                status: Int
            ) {
                val charUuid = descriptor.characteristic?.uuid
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    appendTerminal(TerminalDirection.INFO, "Subscribed notify/indicate on $charUuid")
                } else {
                    appendTerminal(TerminalDirection.ERROR, "CCCD write status=$status on $charUuid")
                }
                if (!writeNextCccd(gatt)) {
                    if (!connectedSignal.isCompleted) connectedSignal.complete(true)
                }
            }

            @Deprecated("Deprecated in API 33")
            override fun onCharacteristicChanged(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic
            ) {
                @Suppress("DEPRECATION")
                handleBleChunk(characteristic.value)
            }

            override fun onCharacteristicChanged(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic,
                value: ByteArray
            ) {
                handleBleChunk(value)
            }
        }

        bluetoothGatt = device.connectGatt(context, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
        return withTimeoutOrNull(10_000L) { connectedSignal.await() } == true
    }

    private fun isWritable(characteristic: BluetoothGattCharacteristic): Boolean {
        val props = characteristic.properties
        return (props and BluetoothGattCharacteristic.PROPERTY_WRITE) != 0 ||
            (props and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) != 0
    }

    @SuppressLint("MissingPermission")
    private fun writeNextCccd(gatt: BluetoothGatt): Boolean {
        val next = synchronized(pendingCccdQueue) {
            if (pendingCccdQueue.isEmpty()) null else pendingCccdQueue.removeFirst()
        } ?: return false

        return try {
            val started = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                gatt.writeDescriptor(next.descriptor, next.enableValue) ==
                    android.bluetooth.BluetoothStatusCodes.SUCCESS
            } else {
                @Suppress("DEPRECATION")
                next.descriptor.value = next.enableValue
                @Suppress("DEPRECATION")
                gatt.writeDescriptor(next.descriptor)
            }
            if (!started) {
                appendTerminal(TerminalDirection.ERROR, "CCCD write failed immediately for ${next.characteristicUuid}")
                writeNextCccd(gatt)
            } else {
                true
            }
        } catch (_: Exception) {
            writeNextCccd(gatt)
        }
    }

    private fun handleBleChunk(bytes: ByteArray?) {
        if (bytes == null || bytes.isEmpty()) return
        val chunk = String(bytes, Charsets.UTF_8)
        val escapedChunk = chunk.replace("\r", "\\r").replace("\n", "\\n")
        appendTerminal(TerminalDirection.RX, "[RAW CHUNK]: $escapedChunk")

        var completedResponse: String? = null
        synchronized(rxBufferLock) {
            rxBuffer.append(chunk)
            if (rxBuffer.contains(">")) {
                completedResponse = rxBuffer
                    .toString()
                    .replace("\r", " ")
                    .replace("\n", " ")
                    .replace(">", "")
                    .replace(Regex("""\s+"""), " ")
                    .trim()
                rxBuffer.clear()
            }
        }
        completedResponse?.let { resolved ->
            bleResponseDeferred?.complete(resolved)
        }
    }

    private suspend fun initializeElm327(): Boolean {
        var anyOk = false
        var supportedPidsOk = false
        for (cmd in ELM327_INIT_SEQUENCE) {
            val timeout = INIT_COMMAND_TIMEOUT_MS
            val resp = sendCommandRaw(cmd, timeoutMs = timeout, logTerminal = true)
            if (cmd == "ATZ" && resp != null &&
                (resp.contains("ELM", ignoreCase = true) || resp.contains("OK", ignoreCase = true))
            ) {
                anyOk = true
            } else if (resp != null && !Elm327Protocol.isErrorResponse(resp) && resp.isNotBlank()) {
                anyOk = true
                if (cmd == "0100" && !Elm327Protocol.isNegativeResponse(resp)) {
                    supportedPidsOk = true
                }
            }
            delay(150L)
        }
        if (!supportedPidsOk) {
            // Fallback to Auto Protocol if fixed 11-bit 500kbps CAN probe failed
            sendCommandRaw("ATSP0", timeoutMs = INIT_COMMAND_TIMEOUT_MS, logTerminal = true)
            delay(150L)
            val fallbackResp = sendCommandRaw("0100", timeoutMs = INIT_COMMAND_TIMEOUT_MS, logTerminal = true)
            if (fallbackResp != null &&
                !Elm327Protocol.isErrorResponse(fallbackResp) &&
                !Elm327Protocol.isNegativeResponse(fallbackResp) &&
                fallbackResp.isNotBlank()
            ) {
                anyOk = true
            }
        }
        return anyOk
    }

    fun updateDiagnosticSettings(speedFactor: Float, coolantWarnC: Int) {
        speedCalibrationFactor = speedFactor
        coolantWarnCelsius = coolantWarnC
        publishDiagnosticReadings()
    }

    fun setAppliedDiagnosticTests(tests: Set<DiagnosticTestId>, forceReprobe: Boolean = false) {
        _appliedDiagnosticTests.value = tests
        if (forceReprobe) {
            diagnosticScanEngine.clearUnsupportedCache()
            _isDiagnosticScanPaused.value = false
        }
        publishDiagnosticReadings()
    }

    fun setDiagnosticScreenActive(active: Boolean) {
        _isDiagnosticScreenActive.value = active
        publishDiagnosticReadings()
    }

    fun toggleDiagnosticScanPaused() {
        _isDiagnosticScanPaused.value = !_isDiagnosticScanPaused.value
    }

    fun reprobeUnsupportedDiagnostics() {
        diagnosticScanEngine.clearUnsupportedCache()
        _isDiagnosticScanPaused.value = false
        publishDiagnosticReadings()
    }

    private fun publishDiagnosticReadings() {
        val connected = _connectionStatus.value == ConnectionStatus.CONNECTED
        _diagnosticReadings.value = diagnosticScanEngine.buildReadingsMap(
            selectedTests = _appliedDiagnosticTests.value,
            isConnected = connected,
            speedCalibrationFactor = speedCalibrationFactor,
            coolantWarnCelsius = coolantWarnCelsius
        )
    }

    private fun startPollingLoop() {
        pollingJob?.cancel()
        pollingJob = scope.launch {
            var tick = 0
            while (_connectionStatus.value == ConnectionStatus.CONNECTED) {
                val chosenTests = _appliedDiagnosticTests.value
                val customScanActive = _isDiagnosticScreenActive.value &&
                    !_isDiagnosticScanPaused.value &&
                    chosenTests.isNotEmpty()

                if (customScanActive) {
                    val plan = diagnosticScanEngine.buildCommandPlan(chosenTests)
                    if (plan.isNotEmpty()) {
                        for (cmd in plan) {
                            if (_connectionStatus.value != ConnectionStatus.CONNECTED ||
                                !_isDiagnosticScreenActive.value ||
                                _isDiagnosticScanPaused.value
                            ) {
                                break
                            }
                            val resp = sendCommandRaw(cmd, POLL_COMMAND_TIMEOUT_MS, logTerminal = false)
                            val decoded = diagnosticScanEngine.processCommandResponse(cmd, resp)
                            if (decoded != null) {
                                applyDecodedToTelemetry(decoded)
                            }
                            publishDiagnosticReadings()
                            delay(25L)
                        }
                        _diagnosticCycleCount.update { it + 1 }
                    }
                    delay(120L)
                } else {
                    applyPid(sendCommandRaw("010C", POLL_COMMAND_TIMEOUT_MS, logTerminal = false), expectedPid = 0x0C)
                    applyPid(sendCommandRaw("0105", POLL_COMMAND_TIMEOUT_MS, logTerminal = false), expectedPid = 0x05)
                    applyPid(sendCommandRaw("010D", POLL_COMMAND_TIMEOUT_MS, logTerminal = false), expectedPid = 0x0D)

                    if (tick % 2 == 0) {
                        applyPid(sendCommandRaw("012F", POLL_COMMAND_TIMEOUT_MS, logTerminal = (tick == 0)), expectedPid = 0x2F)
                        val voltResp = sendCommandRaw("ATRV", POLL_COMMAND_TIMEOUT_MS, logTerminal = false)
                        voltResp?.let { Elm327Protocol.parseVoltageResponse(it) }?.let { v ->
                            _telemetry.update { it.copy(batteryVolts = v, lastUpdatedMs = System.currentTimeMillis()) }
                            diagnosticScanEngine.applyDecodedPid(Elm327Protocol.DecodedPid.ControlModuleVoltage(v))
                        }
                        applyPid(sendCommandRaw("0111", POLL_COMMAND_TIMEOUT_MS, logTerminal = false), expectedPid = 0x11)
                        applyPid(sendCommandRaw("0104", POLL_COMMAND_TIMEOUT_MS, logTerminal = false), expectedPid = 0x04)
                    }
                    if (tick % 4 == 0) {
                        applyPid(sendCommandRaw("010F", POLL_COMMAND_TIMEOUT_MS, logTerminal = false), expectedPid = 0x0F)
                        applyPid(sendCommandRaw("010B", POLL_COMMAND_TIMEOUT_MS, logTerminal = false), expectedPid = 0x0B)
                        applyPid(sendCommandRaw("0106", POLL_COMMAND_TIMEOUT_MS, logTerminal = false), expectedPid = 0x06)
                        applyPid(sendCommandRaw("0107", POLL_COMMAND_TIMEOUT_MS, logTerminal = false), expectedPid = 0x07)
                        applyPid(sendCommandRaw("0146", POLL_COMMAND_TIMEOUT_MS, logTerminal = false), expectedPid = 0x46)
                        applyPid(sendCommandRaw("0101", POLL_COMMAND_TIMEOUT_MS, logTerminal = false), expectedPid = 0x01)
                        pollOptionalStandardPid("0133", expectedPid = 0x33)
                        pollOptionalStandardPid("015C", expectedPid = 0x5C)
                        pollOptionalStandardPid("0123", expectedPid = 0x23)
                        pollOptionalStandardPid("0170", expectedPid = 0x70)
                        pollOptionalStandardPid("015B", expectedPid = 0x5B)
                        if (tick == 0) {
                            pollOptionalStandardPid("0151", expectedPid = 0x51)
                        }
                    }
                    if (tick == 4 && _vinInfo.value == null) {
                        queryVinInternal()
                    }
                    publishDiagnosticReadings()
                    tick++
                    delay(250L)
                }
            }
        }
    }

    private suspend fun pollOptionalStandardPid(cmd: String, expectedPid: Int) {
        if (cmd in unsupportedStandardCommands) return
        val raw = sendCommandRaw(cmd, POLL_COMMAND_TIMEOUT_MS, logTerminal = false) ?: return
        if (Elm327Protocol.isNegativeResponse(raw)) {
            unsupportedStandardCommands.add(cmd)
            return
        }
        applyPid(raw, expectedPid = expectedPid)
    }

    private fun applyPid(raw: String?, expectedPid: Int? = null) {
        if (raw == null) return
        val decoded = Elm327Protocol.decodeMode01(raw, expectedPid = expectedPid) ?: return
        diagnosticScanEngine.applyDecodedPid(decoded)
        applyDecodedToTelemetry(decoded)
    }

    private fun applyDecodedToTelemetry(decoded: Elm327Protocol.DecodedPid) {
        val now = System.currentTimeMillis()
        _telemetry.update { state ->
            when (decoded) {
                is Elm327Protocol.DecodedPid.Rpm -> state.copy(rpm = decoded.rpm, lastUpdatedMs = now)
                is Elm327Protocol.DecodedPid.Speed -> state.copy(speedKmh = decoded.kmh, lastUpdatedMs = now)
                is Elm327Protocol.DecodedPid.CoolantTemp -> state.copy(coolantCelsius = decoded.celsius, lastUpdatedMs = now)
                is Elm327Protocol.DecodedPid.EngineOilTemp -> state.copy(engineOilTempCelsius = decoded.celsius, lastUpdatedMs = now)
                is Elm327Protocol.DecodedPid.IntakeAirTemp -> state.copy(intakeAirCelsius = decoded.celsius, lastUpdatedMs = now)
                is Elm327Protocol.DecodedPid.AmbientAirTemp -> state.copy(ambientAirCelsius = decoded.celsius, lastUpdatedMs = now)
                is Elm327Protocol.DecodedPid.ThrottlePosition -> state.copy(throttlePercent = decoded.percent, lastUpdatedMs = now)
                is Elm327Protocol.DecodedPid.EngineLoad -> state.copy(engineLoadPercent = decoded.percent, lastUpdatedMs = now)
                is Elm327Protocol.DecodedPid.ManifoldPressure -> state.copy(manifoldKpa = decoded.kpa, lastUpdatedMs = now)
                is Elm327Protocol.DecodedPid.BarometricPressure -> state.copy(barometricKpa = decoded.kpa, lastUpdatedMs = now)
                is Elm327Protocol.DecodedPid.FuelPressure -> state.copy(fuelRailPressureKpa = decoded.kpa, lastUpdatedMs = now)
                is Elm327Protocol.DecodedPid.FuelRailPressureRel -> state.copy(fuelRailPressureKpa = decoded.kpa.toInt(), lastUpdatedMs = now)
                is Elm327Protocol.DecodedPid.FuelRailPressureGauge -> state.copy(fuelRailPressureKpa = decoded.kpa, lastUpdatedMs = now)
                is Elm327Protocol.DecodedPid.BoostPressureControl -> state.copy(boostPressureKpa = decoded.kpa.toInt(), lastUpdatedMs = now)
                is Elm327Protocol.DecodedPid.HybridBatteryRemaining -> state.copy(hybridBatteryRemainingPct = decoded.percent, lastUpdatedMs = now)
                is Elm327Protocol.DecodedPid.FuelType -> state.copy(fuelTypeName = decoded.typeName, lastUpdatedMs = now)
                is Elm327Protocol.DecodedPid.ShortTermFuelTrim -> state.copy(shortTermFuelTrimB1 = decoded.percent, lastUpdatedMs = now)
                is Elm327Protocol.DecodedPid.LongTermFuelTrim -> state.copy(longTermFuelTrimB1 = decoded.percent, lastUpdatedMs = now)
                is Elm327Protocol.DecodedPid.FuelLevel -> state.copy(fuelLevelPercent = decoded.percent, lastUpdatedMs = now)
                is Elm327Protocol.DecodedPid.ControlModuleVoltage -> state.copy(batteryVolts = decoded.volts, lastUpdatedMs = now)
                is Elm327Protocol.DecodedPid.MonitorStatus -> state.copy(
                    milOn = decoded.milOn,
                    ecuDtcCount = decoded.dtcCount,
                    lastUpdatedMs = now
                )
                else -> state.copy(lastUpdatedMs = now)
            }
        }
    }

    fun sendManualCommand(command: String) {
        val trimmed = command.trim().uppercase()
        if (trimmed.isEmpty()) return
        scope.launch {
            if (_connectionStatus.value != ConnectionStatus.CONNECTED) {
                appendTerminal(TerminalDirection.TX, trimmed)
                appendTerminal(TerminalDirection.ERROR, "NO ECU LINK — Connect an ELM327 adapter first.")
                return@launch
            }
            val timeout = if (trimmed == "ATZ" || trimmed == "0100" || trimmed.startsWith("ATSP")) {
                INIT_COMMAND_TIMEOUT_MS
            } else {
                POLL_COMMAND_TIMEOUT_MS
            }
            val resp = sendCommandRaw(trimmed, timeoutMs = timeout, logTerminal = true)
            if (resp != null) {
                if (trimmed == "0902") {
                    Elm327Protocol.decodeVinResponse(resp)?.let { _vinInfo.value = it }
                } else {
                    applyPid(resp)
                }
            }
        }
    }

    fun readDtcs() {
        scope.launch {
            if (_connectionStatus.value != ConnectionStatus.CONNECTED) {
                appendTerminal(TerminalDirection.ERROR, "Cannot scan ECU: no Bluetooth OBD-II adapter connected.")
                return@launch
            }
            readDtcsInternal()
        }
    }

    private suspend fun readDtcsInternal() {
        applyPid(sendCommandRaw("0101", POLL_COMMAND_TIMEOUT_MS, logTerminal = true))
        val mode03 = sendCommandRaw("03", POLL_COMMAND_TIMEOUT_MS, logTerminal = true)
        _activeDtcs.value = mode03?.let { Elm327Protocol.decodeDtcResponse(it, 0x43) }.orEmpty()

        val mode07 = sendCommandRaw("07", POLL_COMMAND_TIMEOUT_MS, logTerminal = true)
        _pendingDtcs.value = mode07?.let { Elm327Protocol.decodeDtcResponse(it, 0x47) }.orEmpty()
    }

    fun clearDtcs() {
        scope.launch {
            if (_connectionStatus.value != ConnectionStatus.CONNECTED) return@launch
            val clearResp = sendCommandRaw("04", POLL_COMMAND_TIMEOUT_MS, logTerminal = true)
            if (clearResp != null && Elm327Protocol.isSecurityAccessDenied(clearResp)) {
                _sgwBlockedEvent.value = true
                appendTerminal(
                    TerminalDirection.ERROR,
                    "JL Security Gateway (SGW) blocked Mode 04 clear (NRC 0x33). Use 12+8 SGW bypass cable or AutoAuth."
                )
            }
            delay(300L)
            readDtcsInternal()
        }
    }

    fun consumeSgwBlockedEvent() {
        _sgwBlockedEvent.value = false
    }

    private suspend fun queryVinInternal() {
        val resp = sendCommandRaw("0902", INIT_COMMAND_TIMEOUT_MS, logTerminal = true) ?: return
        Elm327Protocol.decodeVinResponse(resp)?.let {
            _vinInfo.value = it
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun sendCommandRaw(
        command: String,
        timeoutMs: Long,
        logTerminal: Boolean
    ): String? = commandMutex.withLock {
        if (logTerminal) appendTerminal(TerminalDirection.TX, command)
        val cleanCmd = command.trim()
        val payload = ("$cleanCmd\r").toByteArray(Charsets.UTF_8)

        // Clear stale rxBuffer before sending every new command
        synchronized(rxBufferLock) {
            rxBuffer.clear()
            bleResponseDeferred = null
        }

        val response: String? = try {
            val out = classicOut
            val inp = classicIn
            if (out != null && inp != null) {
                withTimeoutOrNull(timeoutMs) {
                    while (inp.available() > 0) inp.read()
                    out.write(payload)
                    out.flush()
                    readUntilPrompt(inp, timeoutMs)
                }
            } else {
                val gatt = bluetoothGatt
                val char = bleWriteChar
                if (gatt != null && char != null && hasConnectPermission()) {
                    val deferred = CompletableDeferred<String>()
                    synchronized(rxBufferLock) {
                        rxBuffer.clear()
                        bleResponseDeferred = deferred
                    }
                    val writeType = if ((char.properties and BluetoothGattCharacteristic.PROPERTY_WRITE) != 0) {
                        BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                    } else {
                        BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        gatt.writeCharacteristic(char, payload, writeType)
                    } else {
                        @Suppress("DEPRECATION")
                        char.writeType = writeType
                        @Suppress("DEPRECATION")
                        char.value = payload
                        @Suppress("DEPRECATION")
                        gatt.writeCharacteristic(char)
                    }
                    withTimeoutOrNull(timeoutMs) { deferred.await() }
                } else {
                    null
                }
            }
        } catch (e: IOException) {
            appendTerminal(TerminalDirection.ERROR, "I/O link dropped: ${e.message}")
            disconnectInternal(updateStatus = true)
            null
        } catch (_: SecurityException) {
            null
        }

        if (logTerminal) {
            val clean = response?.let { Elm327Protocol.sanitizeResponse(it) }?.ifBlank { "<empty>" }
            if (clean != null) {
                appendTerminal(TerminalDirection.RX, clean)
            } else {
                appendTerminal(TerminalDirection.ERROR, "Timeout (${timeoutMs}ms) waiting for ELM327 prompt '>'")
            }
        }
        return@withLock response
    }

    private fun readUntilPrompt(input: InputStream, timeoutMs: Long): String {
        val sb = StringBuilder()
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            if (input.available() > 0) {
                val b = input.read()
                if (b == -1) break
                val ch = b.toChar()
                if (ch == '>') break
                sb.append(ch)
            } else {
                Thread.sleep(15L)
            }
        }
        return sb.toString()
            .replace("\r", " ")
            .replace("\n", " ")
            .replace(">", "")
            .replace(Regex("""\s+"""), " ")
            .trim()
    }

    private fun isLikelyElm327(deviceName: String): Boolean {
        val upper = deviceName.uppercase()
        return ELM327_NAME_HINTS.any { upper.contains(it) }
    }

    private fun upsertDiscoveredDevice(item: BluetoothDeviceItem) {
        _availableDevices.update { current ->
            val filtered = current.filterNot { it.address == item.address }
            (filtered + item).sortedWith(
                compareByDescending<BluetoothDeviceItem> { isLikelyElm327(it.name) }
                    .thenByDescending { it.isPaired }
                    .thenBy { it.name }
            )
        }
    }

    private fun appendTerminal(direction: TerminalDirection, message: String) {
        val entry = TerminalEntry(
            id = terminalIdCounter.getAndIncrement(),
            timestamp = timeFormat.format(Date()),
            direction = direction,
            message = message
        )
        _terminalLogs.update { logs -> (logs + entry).takeLast(150) }
    }

    fun release() {
        stopDiscovery()
        if (classicReceiverRegistered) {
            runCatching { context.unregisterReceiver(classicDiscoveryReceiver) }
            classicReceiverRegistered = false
        }
        scope.launch { disconnectInternal(updateStatus = false) }
    }
}
