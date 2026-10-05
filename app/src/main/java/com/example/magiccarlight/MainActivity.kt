package com.example.magiccarlight

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothManager
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.*
import android.graphics.Color
import android.view.ViewGroup

class MainActivity : Activity() {
    private lateinit var ble: BleController
    private var protocol: Protocols.Protocol = Protocols.E1
    private lateinit var status: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ble = BleController(this)
        if (!ble.hasPermission() && android.os.Build.VERSION.SDK_INT >= 31) {
            requestPermissions(arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT), 100)
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(28, 24, 28, 24)
        }
        root.addView(TextView(this).apply { text = "Magic CarLight tester"; textSize = 24f })
        root.addView(TextView(this).apply { text = "Phone test mode — select E1, E2 or RS, then connect."; textSize = 14f })
        status = TextView(this).apply { text = "Not connected"; textSize = 16f; setPadding(0, 18, 0, 18) }
        root.addView(status)

        val connect = Button(this).apply { text = "SCAN / CONNECT"; setOnClickListener { ble.scanAndConnect { update(it) } } }
        root.addView(connect)

        val protocols = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        protocols.addView(protocolButton("E1 / 7E", Protocols.E1))
        protocols.addView(protocolButton("E2 / 8E", Protocols.E2))
        protocols.addView(protocolButton("RS / 8E", Protocols.RS))
        root.addView(protocols)

        val colors = listOf(
            "RED" to intArrayOf(255,0,0), "GREEN" to intArrayOf(0,255,0),
            "BLUE" to intArrayOf(0,0,255), "WHITE" to intArrayOf(255,255,255)
        )
        for ((name, rgb) in colors) {
            val b = Button(this).apply {
                text = name
                setOnClickListener { send(protocol.rgb(rgb[0], rgb[1], rgb[2])) }
            }
            root.addView(b, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        }
        val power = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        power.addView(Button(this).apply { text = "ON"; setOnClickListener { send(protocol.power(true)) } }, LinearLayout.LayoutParams(0, -2, 1f))
        power.addView(Button(this).apply { text = "OFF"; setOnClickListener { send(protocol.power(false)) } }, LinearLayout.LayoutParams(0, -2, 1f))
        root.addView(power)

        val seek = SeekBar(this).apply {
            max = 100; progress = 100
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(s: SeekBar?, p: Int, fromUser: Boolean) { if (fromUser) send(protocol.brightness(p)) }
                override fun onStartTrackingTouch(s: SeekBar?) {}
                override fun onStopTrackingTouch(s: SeekBar?) {}
            })
        }
        root.addView(TextView(this).apply { text = "Brightness" })
        root.addView(seek)
        setContentView(ScrollView(this).apply { addView(root) })
    }

    private fun protocolButton(label: String, p: Protocols.Protocol): Button = Button(this).apply {
        text = label
        setOnClickListener { protocol = p; update("Selected ${p.name}") }
    }

    private fun send(bytes: ByteArray) {
        update(if (ble.send(bytes)) "Sent ${bytes.joinToString(" ") { "%02X".format(it.toInt() and 255) }}" else "Not connected / write failed")
    }

    private fun update(s: String) { runOnUiThread { status.text = s } }
}
