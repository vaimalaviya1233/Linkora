package com.sakethh.linkora.ui

import com.sakethh.linkora.domain.Result
import com.sakethh.linkora.domain.onFailure
import com.sakethh.linkora.domain.onLoading
import com.sakethh.linkora.domain.onSuccess
import com.sakethh.linkora.ui.utils.linkoraLog
import com.sakethh.linkora.utils.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

typealias PageKey = Int

class Paginator<T>(
    private val coroutineScope: CoroutineScope,
    private val onRetrieve: suspend (nextPageStartIndex: Int) -> Pair<PageKey, Flow<Result<List<T>>>>,
    private val onRetrieved: suspend (currentPageKey: PageKey, data: List<Pair<PageKey, T>>) -> Unit,
    private val onError: suspend (String) -> Unit,
    private val onRetrieving: suspend () -> Unit,
    private val onPagesFinished: suspend () -> Unit
) {

    var isRetrieving = false
        private set

    var isPagesFinished = false
        private set

    var errorOccurred = false
        private set

    private var indexToRetrieveFrom = 0

    private val seenPageKeys = mutableSetOf<PageKey>()

    private val firstVisibleItemIndex = MutableStateFlow(0)

    suspend fun updateFirstVisibleItemIndex(newIndex: Int) = firstVisibleItemIndex.emit(newIndex)

    private val collectionJobs = ConcurrentHashMap<PageKey, Job>()

    init {
        coroutineScope.launch {
            firstVisibleItemIndex.collect { firstVisibleIndex ->

                val visibleElementBatch = firstVisibleIndex / Constants.PAGE_SIZE
                val pageKeyOfTheVisibleElement = visibleElementBatch * Constants.PAGE_SIZE


                val qualifiedForRestarting =
                    ((pageKeyOfTheVisibleElement downTo (pageKeyOfTheVisibleElement - (Constants.PAGE_SIZE * (Constants.ACTIVE_PAGE_COLLECTION_SIZE - 1)))).filterValidKeys()
                            + (pageKeyOfTheVisibleElement until (pageKeyOfTheVisibleElement + (Constants.PAGE_SIZE * (Constants.ACTIVE_PAGE_COLLECTION_SIZE - 1))) + 1).filterValidKeys()).distinct()

                linkoraLog("qualifiedForRestarting = $qualifiedForRestarting")

                // cancel the active collections of pages that are out of $activePageCollectionSize
                collectionJobs.forEach { (pageKey, job) ->
                    if (!job.isCancelled && pageKey !in qualifiedForRestarting) {
                        linkoraLog("cancelling the $pageKey job")
                        job.cancel()
                    }
                }
                linkoraLog("After cancellation::\n")
                logCoroutineScopeChildrenCount()

                // restart collecting the collections that were canceled
                collectionJobs.forEach { (pageKey, job) ->
                    if (job.isCancelled && pageKey in qualifiedForRestarting) {
                        linkoraLog("restarting the $pageKey job")
                        collectionJobs[pageKey] = coroutineScope.launch {
                            onRetrieve(pageKey).second.collectData(pageKey)
                        }
                    }
                }
                linkoraLog("After restarting::\n")
                logCoroutineScopeChildrenCount()
            }
        }
    }

    private fun IntRange.filterValidKeys(): List<Int> {
        return filter {
            seenPageKeys.contains(it)
        }
    }

    private fun IntProgression.filterValidKeys(): List<Int> {
        return filter {
            seenPageKeys.contains(it)
        }
    }

    /*   private fun IntRange.multipleOf(number: Int): List<Int> {
           return this.filter {
               it % number == 0
           }
       }

       private fun IntProgression.multipleOf(number: Int): List<Int> {
           return this.filter {
               it % number == 0
           }
       }*/

    private fun logCoroutineScopeChildrenCount() {
        linkoraLog(
            "active children count:${
                try {
                    coroutineScope.coroutineContext.job.children.count { it.isActive } - 1 // -1 because the collection of visible index in init block of this class
                    // is one of those children which we don't care about and is irrelevant
                } catch (_: Exception) {
                    null
                }
            }"
        )
        linkoraLog(
            "cancelled children count:${
                try {
                    coroutineScope.coroutineContext.job.children.count { it.isCancelled }
                } catch (_: Exception) {
                    null
                }
            }"
        )
    }

    suspend fun retrieveNextBatch() {

        if ((isRetrieving || isPagesFinished || errorOccurred).also {
                linkoraLog("isRetrieving = $isRetrieving, isPagesFinished  = $isPagesFinished, errorOccurred = $errorOccurred")
            }) return

        isRetrieving = true

        val (pageKey, dataFlow) = onRetrieve(indexToRetrieveFrom)
        seenPageKeys.add(pageKey)

        var didLaunch = false

        collectionJobs.computeIfAbsent(pageKey) {
            didLaunch = true
            coroutineScope.launch {
                try {
                    dataFlow.collectData(pageKey)
                } finally {
                    isRetrieving = false
                }
            }.also {
                indexToRetrieveFrom += Constants.PAGE_SIZE
                linkoraLog("Seen pages:${seenPageKeys.count()}")
            }
        }

        if (!didLaunch) {
            linkoraLog("Collections for the key $pageKey is already happening")
            isRetrieving = false
        }
    }

    private suspend fun Flow<Result<List<T>>>.collectData(pageKey: PageKey) {
        cancellable().distinctUntilChanged().collect { result ->
            result.onSuccess {
                onRetrieved(pageKey, it.data.map {
                    pageKey to it
                })

                linkoraLog("Retrieved from page key: $pageKey of size = ${it.data.size}")

                isRetrieving = false
                isPagesFinished = it.data.isEmpty() || it.data.size < Constants.PAGE_SIZE
                if (isPagesFinished) {
                    onPagesFinished()
                }
            }.onFailure {
                linkoraLog(it)
                isRetrieving = false
                errorOccurred = true
                onError(it)
            }.onLoading {
                onRetrieving()
            }
        }
    }

}