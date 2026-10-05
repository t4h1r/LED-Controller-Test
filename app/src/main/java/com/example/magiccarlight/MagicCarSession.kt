package com.example.magiccarlight

import androidx.car.app.Screen
import androidx.car.app.Session

class MagicCarSession : Session() {
    override fun onCreateScreen(intent: android.content.Intent): Screen = MagicCarScreen(carContext)
}
