package live.ditto.dittodiskusage

import android.app.Application
import live.ditto.Ditto

class DittoHandler : Application() {
    companion object {
        lateinit var ditto: Ditto
    }
}