package live.ditto.tools.databrowser

import androidx.lifecycle.ViewModel
import com.ditto.kotlin.DittoStoreObserver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CollectionsViewModel : ViewModel() {

    private val _collections = MutableStateFlow<List<String>?>(null)
    val collections: StateFlow<List<String>?> = _collections.asStateFlow()

    private val observer: DittoStoreObserver? = try {
        DittoHandler.ditto.store.registerObserver(
            "SELECT name FROM __collections"
        ) { result ->
            val names = result.items
                .mapNotNull { it.value["name"].stringOrNull }
                .sorted()
            _collections.value = names
        }
    } catch (t: Throwable) {
        _collections.value = emptyList()
        null
    }

    override fun onCleared() {
        super.onCleared()
        observer?.close()
    }
}
