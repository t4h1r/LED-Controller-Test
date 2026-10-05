package com.example.magiccarlight

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.*

class MagicCarScreen(carContext: CarContext) : Screen(carContext) {
    private val ble = BleController(carContext)
    private var protocol: Protocols.Protocol = Protocols.E1
    private var status = "Not connected"

    override fun onGetTemplate(): Template {
        val list = ItemList.Builder()
            .addItem(GridItem.Builder().setTitle("SCAN / CONNECT").setOnClickListener { ble.scanAndConnect { status=it; invalidate() } }.build())
            .addItem(GridItem.Builder().setTitle("E1 / 7E").setOnClickListener { protocol=Protocols.E1; status="E1 selected"; invalidate() }.build())
            .addItem(GridItem.Builder().setTitle("E2 / 8E").setOnClickListener { protocol=Protocols.E2; status="E2 selected"; invalidate() }.build())
            .addItem(GridItem.Builder().setTitle("RS / 8E").setOnClickListener { protocol=Protocols.RS; status="RS selected"; invalidate() }.build())
            .addItem(GridItem.Builder().setTitle("RED").setOnClickListener { send(protocol.rgb(255,0,0)) }.build())
            .addItem(GridItem.Builder().setTitle("GREEN").setOnClickListener { send(protocol.rgb(0,255,0)) }.build())
            .addItem(GridItem.Builder().setTitle("BLUE").setOnClickListener { send(protocol.rgb(0,0,255)) }.build())
            .addItem(GridItem.Builder().setTitle("WHITE").setOnClickListener { send(protocol.rgb(255,255,255)) }.build())
            .addItem(GridItem.Builder().setTitle("OFF").setOnClickListener { send(protocol.power(false)) }.build())
            .addItem(GridItem.Builder().setTitle("ON").setOnClickListener { send(protocol.power(true)) }.build())
            .build()
        return GridTemplate.Builder().setTitle("LED ${protocol.name}").setSingleList(list).build()
    }
    private fun send(bytes: ByteArray) {
        status = if (ble.send(bytes)) "Sent ${bytes.joinToString(" ") { String.format("%02X", it.toInt() and 255) }}" else "Not connected / write failed"
        invalidate()
    }
}
