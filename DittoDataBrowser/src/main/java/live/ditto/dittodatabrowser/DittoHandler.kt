package live.ditto.dittodatabrowser

import android.app.Application
import live.ditto.Ditto

class DittoHandler : Application() {

    companion object {
        lateinit var ditto: Ditto
    }
}