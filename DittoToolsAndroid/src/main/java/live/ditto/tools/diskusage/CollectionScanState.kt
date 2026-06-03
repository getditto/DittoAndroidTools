package live.ditto.tools.diskusage

/**
 * Per-collection scan outcome. One sealed class keeps state consistent
 * rather than splitting it across parallel "counts" / "failures" maps.
 */
sealed class CollectionScanState {
    object Pending : CollectionScanState()
    data class Counted(val count: Int) : CollectionScanState()
    object Failed : CollectionScanState()
}