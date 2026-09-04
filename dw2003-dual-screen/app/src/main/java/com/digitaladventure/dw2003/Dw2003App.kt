package com.digitaladventure.dw2003

import android.app.Application
import com.digitaladventure.dw2003.emulation.CrashLogStore

class Dw2003App : Application() {
    lateinit var crashLog: CrashLogStore
        private set

    override fun onCreate() {
        super.onCreate()
        crashLog = CrashLogStore(this).also { it.install() }
    }
}
