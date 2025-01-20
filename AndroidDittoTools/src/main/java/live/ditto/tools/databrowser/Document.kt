package live.ditto.tools.databrowser

data class Document(
    var id: String,
    var properties: MutableMap<String, Any?>
)
