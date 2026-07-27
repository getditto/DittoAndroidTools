package com.ditto.tools.databrowser

internal data class Document(
    var id: String,
    var properties: MutableMap<String, Any?>
)
