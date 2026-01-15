package com.sakethh.linkora

import com.sakethh.linkora.domain.Result
import com.sakethh.linkora.ui.PageKey
import com.sakethh.linkora.ui.Paginator
import com.sakethh.linkora.utils.Constants
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PaginatorTest {

    private val activeConnections = mutableSetOf<Int>()

    suspend fun onRetrieve(startIndex: PageKey): Pair<Int, Flow<Result<List<String>>>> {
        return startIndex to flow {
            println("Connection started for Page $startIndex")
            activeConnections.add(startIndex)
            emit(Result.Success(List(Constants.PAGE_SIZE) {
                "item $it"
            }))

            try {
                awaitCancellation()
            } finally {
                println("Connection cancelled for Page $startIndex")
                activeConnections.remove(startIndex)
            }
        }
    }

    @Test
    fun `test sliding window cancels old pages and restarts new ones`() = runTest(
        UnconfinedTestDispatcher() // runs the stuff as instructed, no waiting in the queue,
        // if we are using the StandardTestDispatcher, then we gotta wait until
        // the thing execute from that queue that we don't directly control,
        // advanceUntilIdle should help i guess but even then the result depends upon which got put into the queue first,
        // the paginator will have two running tasks, one from the init and the next being
        // updating stuff based on the current visible index,
        // so it might not result in the same output everytime, i.,e no consistency in the behavior,
        // but we aint calling that
        // for every operation, this should and will work just fine
    ) {
        val paginator = Paginator(
            coroutineScope = this.backgroundScope,
            onRetrieve = ::onRetrieve,
            onRetrieved = { _, _ -> },
            onError = {},
            onRetrieving = {},
            onPagesFinished = {}
        )


        // 💯 100% 💯 real behavior
        paginator.retrieveNextBatch() // 20 items; key = 0
        paginator.updateFirstVisibleItemIndex(0)
        assertTrue("Page 0 should be active", { activeConnections.contains(0) })
        assertFalse("Page 20 should not be active yet", { activeConnections.contains(20) })
        assertFalse("Page 40 should not be active", { activeConnections.contains(40) })
        assertTrue("Only 1 connection should be active", {
            activeConnections.size == 1
        })

        paginator.retrieveNextBatch() // 20*2 items; key = 20
        paginator.retrieveNextBatch() // 20*3  items; key = 40
        paginator.updateFirstVisibleItemIndex(21)
        assertTrue("Page 0 should be active (-1)", { activeConnections.contains(0) })
        assertTrue("Page 20 should be active", { activeConnections.contains(20) })
        assertTrue("Page 40 should be active (+1)", { activeConnections.contains(40) })
        assertFalse("Page 60 should not be active", { activeConnections.contains(60) })

        assertTrue("Only 3 connections should be active", {
            activeConnections.size == 3
        })

        paginator.updateFirstVisibleItemIndex(46)
        paginator.retrieveNextBatch() // 20*4  items; key = 60
        assertFalse("Page 0 should not be active", { activeConnections.contains(0) })
        assertTrue("Page 20 should be active (-1)", { activeConnections.contains(20) })
        assertTrue("Page 40 should be active", { activeConnections.contains(40) })
        assertTrue("Page 60 should be active (+1)", { activeConnections.contains(60) })
        assertFalse("Page 80 should not be active", { activeConnections.contains(80) })

        assertTrue("Only 3 connections should be active", {
            activeConnections.size == 3
        })

        // no need to retrieve next batch
        paginator.updateFirstVisibleItemIndex(0)
        assertTrue("Page 0 should be active", { activeConnections.contains(0) })
        assertTrue("Page 20 should be active (+1)", { activeConnections.contains(20) })
        assertFalse("Page 40 should not be active", { activeConnections.contains(40) })
        assertTrue("Only 2 connections should be active", {
            activeConnections.size == 2
        })
    }

}