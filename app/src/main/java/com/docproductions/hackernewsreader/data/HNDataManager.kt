package com.docproductions.hackernewsreader.data

import kotlinx.coroutines.*
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import org.json.JSONArray
import java.lang.Exception
import java.net.URL
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

interface IHNDataFetchCallback {
    fun fetchCompleted(success: Boolean, data: List<HNItemModel>)
}

@OptIn(UnstableDefault::class)
class HNDataManager {

    private val hnBaseUrl = "https://hacker-news.firebaseio.com/v0"
    private val json: Json

    // A list of the IDs of the top stories
    private var topStoryIds = ArrayList<Long>()

    init {
        val jsonConfiguration = JsonConfiguration(
            encodeDefaults = true,
            ignoreUnknownKeys = true,
            isLenient = false,
            serializeSpecialFloatingPointValues = false,
            allowStructuredMapKeys = true,
            prettyPrint = false,
            unquotedPrint = false,
            indent = "    ",
            useArrayPolymorphism = false,
            classDiscriminator = "type"
        )

        json = Json(jsonConfiguration)
    }

    fun fetchStoriesAsync(startIndex: Int, count: Int, callback: IHNDataFetchCallback) {
        GlobalScope.launch {
            if (topStoryIds.count() == 0) {
                if (!fetchTopStoryIds()) {
                    callback.fetchCompleted(false, ArrayList())
                }
            }

            val endIndex = (startIndex + count).coerceAtMost(topStoryIds.count())
            val validatedCount = endIndex - startIndex
            if (validatedCount <= 0) {
                callback.fetchCompleted(true, ArrayList())
                return@launch
            }

            var storiesToFetchList = ArrayList<Long>()
            for (i in 0 until validatedCount) {
                storiesToFetchList.add(topStoryIds[i + startIndex])
            }

            fetchItemsConcurrently(storiesToFetchList) { stories ->
                callback.fetchCompleted(true, stories)
            }
        }
    }

    fun fetchChildItemsAsync(item: HNItemModel, onComplete: (List<HNItemModel>) -> Unit) {
        fetchItemsConcurrently(item.children, onComplete)
    }

    private fun fetchItemsConcurrently(itemIdList: List<Long>, onComplete: (List<HNItemModel>) -> Unit) {
        GlobalScope.launch {
            // Kick off all story detail tasks async in parallel
            val deferredItems = ArrayList<Deferred<HNItemModel?>>()
            for (i in 0 until itemIdList.count()) {
                deferredItems.add(async { fetchItemDetails(itemIdList[i]) })
            }

            // Aggregate the results of all the async fetches
            val items = ArrayList<HNItemModel>()
            for (i in 0 until deferredItems.count()) {
                val item = deferredItems[i].await()
                if (item != null) {
                    items.add(item)
                }
            }

            onComplete(items)
        }
    }

    // Returns whether the fetch succeeded or not
    private fun fetchTopStoryIds(): Boolean {
        val url = String.format("%s/topstories.json", hnBaseUrl)
        val json = fetchJson(URL(url)) ?: return false

        val result = JSONArray(json)
        topStoryIds = ArrayList()
        for (i in 0 until result.length()) {
            topStoryIds.add(result.getLong(i))
        }

        return true
    }

    private fun fetchItemDetails(itemId: Long): HNItemModel? {
        val url = String.format("%s/item/%d.json", hnBaseUrl, itemId)
        val itemJson = fetchJson(URL(url)) ?: return null

        return json.parse(HNItemModel.serializer(), itemJson)
    }

    // Returns null if json fetch failed
    private fun fetchJson(url: URL): String? {
        var json: String? = null
        try {
            json = url.readText()
        }
        catch (e: Exception) {
            print(String.format("JSON fetch failed: %s", e))
        }

        return json
    }
}