package com.example.umbrella

import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_ble.*
import java.util.*

private const val STATE_DISCONNECTED = 0
private const val STATE_CONNECTING = 1
private const val STATE_CONNECTED = 2

val UUID_SERVICE: UUID = UUID.fromString("13A28130-8883-49A8-8BDB-42BC1A7107F4")
val UUID_HEART_RATE_MEASUREMENT: UUID = UUID.fromString("A2935077-201F-44EB-82E8-10CC02AD8CE1")

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class BleFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_ble, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        button_scan.setOnClickListener {
            startScan()
        }

        button_con.setOnClickListener {
            connect()
        }

        button_discon.setOnClickListener {
            close()
        }

        updateButton()
    }

    private fun updateButton() {
        //button_scan.isEnabled = !scanning
        //button_con.isEnabled = bluetoothDevice != null && bluetoothGatt == null
        //button_discon.isEnabled = connectionState == STATE_CONNECTED
    }

    override fun onDestroyView() {
        super.onDestroyView()

        close()
    }

    fun log(text: String) {
        activity?.runOnUiThread {
            var t = textView3.text.toString()
            if (t.isNotEmpty()) {
                t += System.lineSeparator()
            }
            t += text

            textView3.text = t
        }
    }

    private fun manager(): BluetoothManager {
        return context?.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    }

    private fun adapter(): BluetoothAdapter {
        return manager().adapter
    }

    private fun scanner(): BluetoothLeScanner {
        return adapter().bluetoothLeScanner
    }

    private var scanning = false
    private fun startScan() {
        val scanFilter = ScanFilter.Builder()
            .setDeviceAddress("B8:27:EB:2C:52:17")
            .build()
        val scanFilterList = listOf(scanFilter)

        val scanSettings: ScanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
            .build()

        log("startScan set")

        scanning = true
        scanner().startScan(scanFilterList, scanSettings, scanCallback)
        //scanner().startScan(scanCallback)
        Handler(Looper.getMainLooper()).postDelayed({
            if (!scanning) return@postDelayed

            scanner().stopScan(scanCallback)
            scanning = false

            updateButton()
            log("stopScan timeout")
        }, 10000)

        updateButton()
    }

    private var bluetoothDevice: BluetoothDevice? = null
    private var bluetoothGatt: BluetoothGatt? = null

    private val scanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)

            val device = result.device
            log("scanResult: ${device.name}")

            scanning = false
            scanner().stopScan(this)

            bluetoothDevice = device

            updateButton()
        }
    }

    private fun connect() {
        bluetoothGatt = bluetoothDevice?.connectGatt(
            context?.applicationContext, true, gattCallback
        )
        updateButton()
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
                    log("Connected")
                    connectionState = STATE_CONNECTED

                    gatt.discoverServices()
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    log("Disconnected")
                    connectionState = STATE_DISCONNECTED

                    gatt.discoverServices()

                    updateButton()
                }
            }
        }

        fun getChar(services: List<BluetoothGattService>): BluetoothGattCharacteristic? {
            log("getChar")
            for (service in services) {
                log("  service ${service.uuid} ${service.instanceId}")
                //if (service.uuid != UUID_SERVICE) {
                //    continue
                //}

                for (char in service.characteristics) {
                    log("    char ${char.uuid}")
                    //if (char.uuid != UUID_HEART_RATE_MEASUREMENT)
                    //{
                    //    continue
                    //}
                    return char
                }
            }

            return null
        }

        // New services discovered
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            log("onServicesDiscovered")

            when (status) {
                BluetoothGatt.GATT_SUCCESS -> {
                    log("Service found")
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
            log("onCharacteristicRead")

            when (status) {
                BluetoothGatt.GATT_SUCCESS -> {
                    val value = characteristic.value

                    val str = value.map { it.toString() }.joinToString { it }
                    log("ReadChar $str")
                }
            }
        }
    }

    private fun close() {
        bluetoothGatt?.close()
        bluetoothDevice = null
        bluetoothGatt = null

        updateButton()
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
                log(heartRate.toString())
            }
            else -> {
                // For all other profiles, writes the data formatted in HEX.
                val data: ByteArray? = characteristic.value
                if (data?.isNotEmpty() == true) {
                    val hexString: String = data.joinToString(separator = " ") {
                        String.format("%02X", it)
                    }
                    log("$data\n$hexString")
                }
            }
        }
        context?.sendBroadcast(intent)
    }
}