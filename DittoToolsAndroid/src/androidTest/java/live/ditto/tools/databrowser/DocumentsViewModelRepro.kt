package live.ditto.tools.databrowser

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.ditto.kotlin.Ditto
import com.ditto.kotlin.DittoConfig
import com.ditto.kotlin.DittoFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.UUID

/**
 * Verifies the rewritten Data Browser fixes the three known bugs:
 *  1. Silent 1000-doc cap on count and page access
 *  2. Heavy callback lag (mitigated by snapshot pages + cheap live count)
 *  3. Zero-flash on screen re-entry (mitigated by nullable initial StateFlow values)
 *
 * Runs offline via `SmallPeersOnly(null)` so no credentials are required.
 */
@RunWith(AndroidJUnit4::class)
class DocumentsViewModelRepro {

    private lateinit var ditto: Ditto
    private val collectionName = "test_collection"

    @Before
    fun setUp() {
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        val persistenceDir = File(
            ctx.cacheDir,
            "docs-vm-test-${System.currentTimeMillis()}-${UUID.randomUUID()}"
        ).apply { mkdirs() }

        val config = DittoConfig(
            databaseId = UUID.randomUUID().toString(),
            connect = DittoConfig.Connect.SmallPeersOnly(null),
            persistenceDirectory = persistenceDir.absolutePath,
        )
        ditto = runBlocking { DittoFactory.create(config) }
        DittoHandler.ditto = ditto
    }

    @After
    fun tearDown() {
        runBlocking { ditto.close() }
    }

    private suspend fun insert(docCount: Int) = withContext(Dispatchers.IO) {
        repeat(docCount) { i ->
            ditto.store.execute(
                "INSERT INTO `$collectionName` DOCUMENTS (:doc)",
                mapOf("doc" to mapOf("_id" to UUID.randomUUID().toString(), "n" to i.toLong()))
            )
        }
    }

    private suspend fun storeCount(): Int {
        var c = -1
        ditto.store.execute("SELECT COUNT(*) AS c FROM `$collectionName`") { r ->
            c = r.items.firstOrNull()?.value?.get("c")?.longOrNull?.toInt() ?: 0
        }
        return c
    }

    private suspend fun awaitCount(vm: DocumentsViewModel, target: Int, timeoutMs: Long = 10_000): Int {
        val start = System.currentTimeMillis()
        while (System.currentTimeMillis() - start < timeoutMs) {
            val v = vm.totalCount.value
            if (v == target) return v
            delay(50)
        }
        return vm.totalCount.value ?: -1
    }

    private suspend fun awaitPage(vm: DocumentsViewModel, timeoutMs: Long = 5_000): DocumentsViewModel.Page? {
        val start = System.currentTimeMillis()
        while (System.currentTimeMillis() - start < timeoutMs) {
            val p = vm.pageState.value.page
            if (p != null && !vm.pageState.value.isLoading) return p
            delay(50)
        }
        return vm.pageState.value.page
    }

    @Test
    fun count_aboveOldLimit_reportsAccurately() {
        runBlocking {
            insert(1200)
            val vm = DocumentsViewModel(collectionName)
            val final = awaitCount(vm, target = 1200)
            Log.i(TAG, "Final totalCount=$final actualStore=${storeCount()}")
            assertEquals(1200, final)
        }
    }

    @Test
    fun page_canBrowsePastOldLimit() {
        runBlocking {
            insert(1200)
            val vm = DocumentsViewModel(collectionName)
            awaitCount(vm, target = 1200)
            awaitPage(vm)

            // Page forward 40 pages (offset 1000) — past the old LIMIT 1000 ceiling.
            repeat(40) {
                withContext(Dispatchers.Main) { vm.nextPage() }
                awaitPage(vm)
            }
            val page = vm.pageState.value.page
            assertNotNull("Expected a page past offset 1000", page)
            assertEquals(1000, page!!.offset)
            assertTrue("Expected docs on the page", page.docs.isNotEmpty())
            Log.i(TAG, "Reached offset=${page.offset} docs=${page.docs.size}")
        }
    }

    @Test
    fun page_isStableUnderConcurrentInserts() {
        runBlocking {
        insert(50)
        val vm = DocumentsViewModel(collectionName)
        awaitCount(vm, target = 50)
        val initial = awaitPage(vm)!!.docs.map { it.id }
        assertEquals(25, initial.size)

        // Start writing in the background; page should NOT shuffle until refresh.
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val job: Job = scope.launch {
            var i = 0
            while (isActive && i < 200) {
                ditto.store.execute(
                    "INSERT INTO `$collectionName` DOCUMENTS (:doc)",
                    mapOf("doc" to mapOf("_id" to UUID.randomUUID().toString(), "n" to i.toLong()))
                )
                i++
                delay(15)
            }
        }
        delay(1500)
        val afterWrites = vm.pageState.value.page!!.docs.map { it.id }
        val countDuring = vm.totalCount.value
        job.cancelAndJoin()
        scope.coroutineContext[Job]?.cancelAndJoin()

            Log.i(TAG, "Page stable test — count went from 50 to $countDuring; page same? ${initial == afterWrites}")
            assertEquals("Page content must be a stable snapshot until refresh", initial, afterWrites)
            // Count, on the other hand, MUST have updated live.
            assertTrue("totalCount should have grown live: was $countDuring", (countDuring ?: 0) > 50)
        }
    }

    @Test
    fun refresh_resnapshotsAndStaleIndicatorClears() {
        runBlocking {
            insert(30)
            val vm = DocumentsViewModel(collectionName)
            awaitCount(vm, target = 30)
            val before = awaitPage(vm)!!
            assertEquals(30, before.snapshotAtCount)

            insert(100)
            awaitCount(vm, target = 130)
            // Page snapshot is still at 30; count is 130 — stale state.
            assertEquals(30, vm.pageState.value.page!!.snapshotAtCount)

            withContext(Dispatchers.Main) { vm.refresh() }
            awaitPage(vm)
            val after = vm.pageState.value.page!!
            assertEquals("Refresh should re-snapshot at current count", 130, after.snapshotAtCount)
        }
    }

    @Test
    fun noZeroFlash_initialStateIsNullNotZero() {
        runBlocking {
            insert(30)

            // Construct a fresh VM five times under continuous writes — verify no
            // VM ever emits a "0" count or empty Page as its first observable value.
            val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
            val writer: Job = scope.launch {
                var i = 0
                while (isActive && i < 200) {
                    ditto.store.execute(
                        "INSERT INTO `$collectionName` DOCUMENTS (:doc)",
                        mapOf("doc" to mapOf("_id" to UUID.randomUUID().toString(), "n" to i.toLong()))
                    )
                    i++
                    delay(20)
                }
            }

            repeat(5) { cycle ->
                val vm = DocumentsViewModel(collectionName)
                // Sample initial state — must be the loading sentinel, not zero/empty.
                val firstCount = vm.totalCount.value
                val firstPage = vm.pageState.value.page
                val firstLoading = vm.pageState.value.isLoading
                Log.i(TAG, "Cycle $cycle initial — count=$firstCount page=$firstPage loading=$firstLoading")
                assertNull("Initial totalCount must be null (loading), not 0", firstCount)
                assertNull("Initial pageState.page must be null (loading), not empty list", firstPage)
                assertTrue("Initial isLoading must be true", firstLoading)
                awaitCount(vm, target = (vm.totalCount.value ?: 30).coerceAtLeast(30), timeoutMs = 1500)
            }
            writer.cancelAndJoin()
        }
    }

    companion object { private const val TAG = "DocsVMRepro" }
}
