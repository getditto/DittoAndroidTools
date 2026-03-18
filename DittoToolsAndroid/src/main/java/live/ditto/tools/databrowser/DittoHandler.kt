package live.ditto.tools.databrowser

import android.app.Application
import com.ditto.kotlin.Ditto

class DittoHandler : Application() {

    companion object {
        lateinit var ditto: Ditto
    }
}