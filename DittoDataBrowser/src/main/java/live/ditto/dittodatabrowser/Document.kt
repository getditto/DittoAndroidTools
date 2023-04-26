package live.ditto.dittodatabrowser

data class Document(
    var id: String,
    var properties: MutableMap<String, Any?>
)
