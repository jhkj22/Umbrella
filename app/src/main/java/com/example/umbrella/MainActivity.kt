package com.example.umbrella

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.NetworkInfo
import android.net.wifi.WifiManager
import android.net.wifi.WpsInfo
import android.net.wifi.p2p.*
import android.os.*
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import java.net.InetAddress
import java.util.*
import kotlin.system.measureTimeMillis


fun Log(msg: String) {
    Log.d("hayato", msg)
}

private const val STATE_DISCONNECTED = 0
private const val STATE_CONNECTING = 1
private const val STATE_CONNECTED = 2

const val ACTION_GATT_CONNECTED = "com.example.bluetooth.le.ACTION_GATT_CONNECTED"
const val ACTION_GATT_DISCONNECTED = "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED"
const val ACTION_GATT_SERVICES_DISCOVERED =
    "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED"
const val ACTION_DATA_AVAILABLE = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE"
const val EXTRA_DATA = "com.example.bluetooth.le.EXTRA_DATA"

val UUID_SERVICE: UUID = UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb")
val UUID_HEART_RATE_MEASUREMENT: UUID = UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb")

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class MainActivity : AppCompatActivity(), WifiP2pManager.ChannelListener {

    private fun scanner(): BluetoothLeScanner {
        val manager: BluetoothManager =
            getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        return manager.adapter.bluetoothLeScanner
    }

    private fun log(text: String) {
        /*runOnUiThread {
            var t = textView.text.toString()
            if (t.isNotEmpty()) {
                t += System.lineSeparator()
            }
            t += text

            textView.text = t
        }*/
    }

    private fun updateImage() {
        /*val address = InetSocketAddress(targetAddress.hostAddress, 8080)

        val socket = Socket()

        try {
            socket.connect(address, 1000)

            val reader = socket.getInputStream()
            val data = reader.readBytes()
            socket.close()

            val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)

            runOnUiThread {
                imageView2.setImageBitmap(bitmap)
            }
        } catch (e: Exception) {
            log(e.toString())
        } finally {
            socket.close()
        }*/
    }

    private val runnable = Runnable {
        val times = arrayListOf<Double>()
        for (i in 0..100) {
            val time = measureTimeMillis {
                updateImage()
            }
            times.add(time.toDouble())
        }

        log("${times.average()}")
    }

    private val handler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(inputMessage: Message) {
            /*val bitmap = inputMessage.obj as Bitmap
            imageView2.setImageBitmap(bitmap)*/
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /*requestPermissions(arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION), 1)

        val scanFilter: ScanFilter = ScanFilter.Builder()
            .setDeviceName("Test Device")
            .setServiceUuid(ParcelUuid(UUID_SERVICE))
            .build()
        val scanFilterList = listOf(scanFilter)

        val scanSettings: ScanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
            .build()
        Log("startScan set")
        scanner().startScan(scanFilterList, scanSettings, scanCallback)

        registerReceiver(gattUpdateReceiver, IntentFilter())*/

        if (!initP2p()) {
            finish()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION
                ), 1
            )
        }

        /*button.setOnClickListener {
            manager.discoverPeers(channel, object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    log("discoverPeers onSuccess")
                }

                override fun onFailure(reasonCode: Int) {
                    log("discoverPeers onFailure")
                }
            })
        }

        button2.setOnClickListener {
            if (!::targetDevice.isInitialized) return@setOnClickListener

            log("connect to ${targetDevice.deviceName}")

            connect(targetDevice)
        }

        button3.setOnClickListener {
            disconnect()
        }*/

        /*button2.setOnClickListener {
            startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
        }*/

        fun toAlignmentType(): AlignmentTypeFragment {
            val fragment = AlignmentTypeFragment()

            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.container, fragment)
            transaction.commit()

            return fragment
        }

        fun toAlignmentAlgorithm(): AlignmentAlgorithmFragment {
            val fragment = AlignmentAlgorithmFragment()

            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.container, fragment)
            transaction.commit()

            return fragment
        }

        fun toAlignmentOrigin(): AlignmentOriginFragment {
            val fragment = AlignmentOriginFragment()

            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.container, fragment)
            transaction.commit()

            return fragment
        }

        fun toAlignmentModelSize(): AlignmentModelSizeFragment {
            val fragment = AlignmentModelSizeFragment()

            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.container, fragment)
            transaction.commit()

            return fragment
        }

        /*val fragAlignmentType = toAlignmentType()
        fragAlignmentType.onNextClicked = {
            toAlignmentAlgorithm()
        }*/

        toAlignmentModelSize()

        //val thread = Thread(runnable)
        //thread.start()
    }

    public override fun onResume() {
        super.onResume()
        //registerReceiver(receiver, intentFilter)
    }

    public override fun onPause() {
        super.onPause()
        //unregisterReceiver(receiver)
    }

    private val intentFilter = object : IntentFilter() {
        init {
            addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
        }
    }

    private lateinit var manager: WifiP2pManager
    private lateinit var channel: WifiP2pManager.Channel

    private fun initP2p(): Boolean {
        // Device capability definition check
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_WIFI_DIRECT)) {
            log("Wi-Fi Direct is not supported by this device.")
            return false
        }
        // Hardware capability check
        val wifiManager: WifiManager =
            applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
        if (!wifiManager.isP2pSupported) {
            log("Wi-Fi Direct is not supported by the hardware or Wi-Fi is off.")
            return false
        }
        manager = getSystemService(WIFI_P2P_SERVICE) as WifiP2pManager
        channel = manager.initialize(this, mainLooper, null)
        return true
    }

    private lateinit var targetDevice: WifiP2pDevice
    private lateinit var targetAddress: InetAddress

    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context?, intent: Intent) {
            val action = intent.action.toString()

            var extra = ""
            when (action) {
                WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> {
                    val state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1)
                    if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                        extra = "enabled"
                    } else if (state == WifiP2pManager.WIFI_P2P_STATE_DISABLED) {
                        extra = "disabled"
                    }
                }
                WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                    val deviceList =
                        intent.getParcelableExtra<WifiP2pDeviceList>(WifiP2pManager.EXTRA_P2P_DEVICE_LIST)
                    if (deviceList != null) {
                        val devices = deviceList.deviceList

                        extra = devices.size.toString() + ": "
                        for (device in devices) {
                            if (device.deviceName == "raspberry") {
                                extra += device.status
                                targetDevice = device
                            }
                        }
                    }
                }
                WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> run {
                    val info =
                        intent.getParcelableExtra<WifiP2pInfo>(WifiP2pManager.EXTRA_WIFI_P2P_INFO)
                    val network =
                        intent.getParcelableExtra<NetworkInfo>(WifiP2pManager.EXTRA_NETWORK_INFO)
                    //val group = intent.getParcelableExtra<WifiP2pGroup>(WifiP2pManager.EXTRA_WIFI_P2P_GROUP)

                    info ?: return@run
                    network ?: return@run

                    val isConnected = network.isConnected
                    extra = if (isConnected) {
                        info.groupOwnerAddress.toString()
                    } else {
                        "false"
                    }

                    if (isConnected) {
                        targetAddress = info.groupOwnerAddress
                        //Thread(runnable).start()
                    }
                }
                WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION -> run {
                    val device =
                        intent.getParcelableExtra<WifiP2pDevice>(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE)
                            ?: return@run

                    val map = mapOf(
                        WifiP2pDevice.AVAILABLE to "AVAILABLE",
                        WifiP2pDevice.CONNECTED to "CONNECTED",
                        WifiP2pDevice.FAILED to "FAILED",
                        WifiP2pDevice.INVITED to "INVITED",
                        WifiP2pDevice.UNAVAILABLE to "UNAVAILABLE"
                    )

                    extra = map[device.status].toString()
                }
            }

            val label = action.replace("android.net.wifi.p2p.", "")

            log("$label: $extra")
        }
    }

    @SuppressLint("MissingPermission")
    fun connect(device: WifiP2pDevice) {

        val config = WifiP2pConfig().apply {
            deviceAddress = device.deviceAddress
            wps.setup = WpsInfo.PBC
        }

        manager.connect(channel, config, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                log("Connect succeeded.")
            }

            override fun onFailure(reason: Int) {
                log("Connect failed. Retry.")
            }
        })
    }

    private fun disconnect() {
        manager.removeGroup(channel, object : WifiP2pManager.ActionListener {
            override fun onFailure(reasonCode: Int) {
                log("Disconnect failed. Reason :$reasonCode")
            }

            override fun onSuccess() {
                log("Disconnect succeeded")
            }
        })
    }

    override fun onChannelDisconnected() {
    }

    /*
    private val scanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)

            val device = result.device
            Log("scanResult: ${device.name}")

            scanner().stopScan(this)

            device.connectGatt(applicationContext, false, gattCallback)
        }
    }

    private var connectionState = STATE_DISCONNECTED

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(
            gatt: BluetoothGatt,
            status: Int,
            newState: Int
        ) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    Log("Connected")
                    connectionState = STATE_CONNECTED
                    broadcastUpdate(ACTION_GATT_CONNECTED)

                    gatt.discoverServices()
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    Log("Disconnected")
                    connectionState = STATE_DISCONNECTED
                    broadcastUpdate(ACTION_GATT_DISCONNECTED)
                }
            }
        }

        fun getChar(services: List<BluetoothGattService>): BluetoothGattCharacteristic? {
            for (service in services) {
                if (service.uuid != UUID_SERVICE) {
                    continue
                }

                for (char in service.characteristics) {
                    if (char.uuid != UUID_HEART_RATE_MEASUREMENT)
                    {
                        continue
                    }

                    //Log("service uuid ${service.uuid} ${service.instanceId}")
                    //Log("  char ${char.uuid} ${char.value}")
                    return char
                }
            }

            return null
        }

        // New services discovered
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {

            when (status) {
                BluetoothGatt.GATT_SUCCESS -> {
                    broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED)

                    //Log("Service found")
                    gatt.readCharacteristic(getChar(gatt.services))
                }
            }
        }

        // Result of a characteristic read operation
        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> {
                    val value = characteristic.value

                    val str = value.map { it.toString() }.joinToString {it}
                    Log("ReadChar $str")
                    broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic)
                }
            }
        }
    }

    private fun broadcastUpdate(action: String) {
        val intent = Intent(action)
        sendBroadcast(intent)
    }

    private fun broadcastUpdate(action: String, characteristic: BluetoothGattCharacteristic) {
        val intent = Intent(action)

        // This is special handling for the Heart Rate Measurement profile. Data
        // parsing is carried out as per profile specifications.
        when (characteristic.uuid) {
            UUID_HEART_RATE_MEASUREMENT -> {
                val flag = characteristic.properties
                val format = when (flag and 0x01) {
                    0x01 -> {
                        BluetoothGattCharacteristic.FORMAT_UINT16
                    }
                    else -> {
                        BluetoothGattCharacteristic.FORMAT_UINT8
                    }
                }
                val heartRate = characteristic.getIntValue(format, 1)

                intent.putExtra(EXTRA_DATA, (heartRate).toString())
            }
            else -> {
                // For all other profiles, writes the data formatted in HEX.
                val data: ByteArray? = characteristic.value
                if (data?.isNotEmpty() == true) {
                    val hexString: String = data.joinToString(separator = " ") {
                        String.format("%02X", it)
                    }
                    intent.putExtra(EXTRA_DATA, "$data\n$hexString")
                }
            }

        }
        sendBroadcast(intent)
    }

    private val gattUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            Log("$action ${intent.extras}")
        }
    }*/
}
