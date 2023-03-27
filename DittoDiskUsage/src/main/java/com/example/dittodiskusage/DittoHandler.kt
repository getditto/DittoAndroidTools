package com.example.dittodiskusage

import android.app.Application
import live.ditto.Ditto

public class DittoHandler : Application() {

    companion object {
        lateinit var ditto: Ditto
    }
}