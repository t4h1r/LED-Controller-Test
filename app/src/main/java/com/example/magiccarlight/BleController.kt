package com.example.magiccarlight

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import java.util.UUID

class BleController(private val context: Context) {
    companion object {
        val SERVICE: UUID = UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb")
        val WRITE: UUID = UUID.fromString("0000fff3-0000-1000-8000-00805f9b34fb")
    }
    private var gatt: BluetoothGatt? = null
    private var writeChar: BluetoothGattCharacteristic? = null
    private var connected = false
    private var scanCallback: ScanCallback? = null

    fun hasPermission(): Boolean = android.os.Build.VERSION.SDK_INT < 31 ||
        (context.checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
         context.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED)

    @SuppressLint("MissingPermission")
    fun scanAndConnect(onStatus: (String) -> Unit) {
        if (!hasPermission()) { onStatus("Allow Bluetooth permission on phone"); return }
        val adapter = (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
        val scanner = adapter.bluetoothLeScanner ?: run { onStatus("Bluetooth scanner unavailable"); return }
        scanCallback?.let { scanner.stopScan(it) }
        val cb = object : ScanCallback() {
            override fun onScanResult(type: Int, result: ScanResult) {
                val device = result.device
                val likely = result.scanRecord?.serviceUuids?.any { it.uuid == SERVICE } == true ||
                    (device.name?.contains("LED", true) == true) ||
                    (device.name?.contains("MELK", true) == true) ||
                    (device.name?.contains("ELK", true) == true)
                if (likely) {
                    scanner.stopScan(this); scanCallback = null
                    connect(device, onStatus)
                }
            }
            override fun onScanFailed(errorCode: Int) { scanCallback = null; onStatus("Scan failed: $errorCode") }
        }
        scanCallback = cb
        onStatus("Scanning for LED controller…")
        scanner.startScan(cb)
        Handler(Looper.getMainLooper()).postDelayed({
            scanCallback?.let { scanner.stopScan(it); scanCallback = null; onStatus("No likely LED controller found") }
        }, 9000)
    }

    @SuppressLint("MissingPermission")
    private fun connect(device: BluetoothDevice, onStatus: (String) -> Unit) {
        onStatus("Connecting…")
        gatt?.close(); writeChar = null
        gatt = device.connectGatt(context, false, object : BluetoothGattCallback() {
            override fun onConnectionStateChange(g: BluetoothGatt, status: Int, newState: Int) {
                connected = newState == BluetoothProfile.STATE_CONNECTED
                if (connected) { onStatus("Connected"); g.discoverServices() } else { writeChar=null; onStatus("Disconnected ($status)") }
            }
            override fun onServicesDiscovered(g: BluetoothGatt, status: Int) {
                writeChar = g.getService(SERVICE)?.getCharacteristic(WRITE)
                onStatus(if (writeChar != null) "Ready" else "FFF3 write characteristic not found")
            }
        })
    }

    @SuppressLint("MissingPermission")
    fun send(bytes: ByteArray): Boolean {
        val g = gatt ?: return false
        val c = writeChar ?: return false
        c.writeType = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
        c.value = bytes
        return g.writeCharacteristic(c)
    }
}
