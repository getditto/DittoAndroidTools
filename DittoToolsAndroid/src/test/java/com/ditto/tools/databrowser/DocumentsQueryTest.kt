package com.ditto.tools.databrowser

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DocumentsQueryTest {

    @Test
    fun emptyExpressionBuildsBoundedCollectionQuery() {
        assertEquals(
            "SELECT * FROM `cars` LIMIT 1000",
            documentsQuery("cars", "")
        )
    }

    @Test
    fun dqlWhereExpressionIsUsedWithoutSyntaxGuessing() {
        assertEquals(
            "SELECT * FROM `cars` WHERE color = 'blue' LIMIT 1000",
            documentsQuery("cars", "color = 'blue'")
        )
        assertEquals(
            "SELECT * FROM `cars` WHERE deleted IS NULL LIMIT 1000",
            documentsQuery("cars", "deleted IS NULL")
        )
    }

    @Test
    fun selectionKeepsCurrentDocumentWhenResultsRefresh() {
        val documents = mutableListOf(
            Document("first", mutableMapOf()),
            Document("selected", mutableMapOf())
        )

        assertEquals(1, selectedDocumentIndex(documents, "selected"))
    }

    @Test
    fun selectionFallsBackToFirstDocumentOrNothing() {
        val documents = mutableListOf(Document("first", mutableMapOf()))

        assertEquals(0, selectedDocumentIndex(documents, "missing"))
        assertNull(selectedDocumentIndex(emptyList(), "missing"))
    }
}
