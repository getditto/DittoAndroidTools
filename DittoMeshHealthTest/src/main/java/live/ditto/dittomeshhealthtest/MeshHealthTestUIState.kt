package live.ditto.dittomeshhealthtest

import live.ditto.DittoPeer

data class MeshHealthTestUIState(
    val testState: TestState = TestState.OBSERVING,
    val remotePeers: List<DittoPeer> = emptyList()
)

enum class TestState { OBSERVING, CONFIGURING, TEST_RUNNING, TEST_COMPLETE }

