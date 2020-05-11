package com.docproductions.hackernewsreader.data

import android.util.Log
import kotlinx.coroutines.*
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import org.json.JSONArray
import java.lang.Exception
import java.net.URL
import java.util.*
import kotlin.collections.ArrayList

@OptIn(UnstableDefault::class)
class HNDataManager {

    private val hnBaseUrl = "https://hacker-news.firebaseio.com/v0"
    private val json: Json

    // A cached list of the story IDs for each sort type (top, new, etc.)
    private var storyListIds = HashMap<HNStorySortType, ArrayList<Long>>()

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

    fun clearCachedStories(sortType: HNStorySortType) {
        storyListIds[sortType]?.clear()
    }

    fun fetchStoriesAsync(sortType: HNStorySortType, startIndex: Int, count: Int, onComplete: (Boolean, List<HNItemModel>?) -> Unit) {
        GlobalScope.launch {
            // Fetch the list of stories for the desired sort type if we don't have the data cached already
            if (!storyListIds.containsKey(sortType) || storyListIds[sortType]?.count() == 0) {
                if (!fetchStoryIds(sortType)) {
                    onComplete(false, null)
                    return@launch
                }
            }

            // Should never happen, but just do some sanity checking to make sure we have populated the story list
            if (!storyListIds.containsKey(sortType)) {
                Log.e("HNDataManager", "Empty story list for sort type $sortType")
                onComplete(false, null)
                return@launch
            }

            val storyIds = storyListIds[sortType]!!
            val endIndex = (startIndex + count).coerceAtMost(storyIds.count())
            val validatedCount = endIndex - startIndex
            if (validatedCount <= 0) {
                onComplete(true, ArrayList())
                return@launch
            }

            val storiesToFetchList = ArrayList<Long>()
            for (i in 0 until validatedCount) {
                storiesToFetchList.add(storyIds[i + startIndex])
            }

            fetchItemsConcurrently(storiesToFetchList) { stories ->
                onComplete(true, stories)
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
    private fun fetchStoryIds(sortType: HNStorySortType): Boolean {
        val url = String.format("%s/%s.json", hnBaseUrl, sortType.relativeUrl)
        val json = fetchJson(URL(url)) ?: return false

        val result = JSONArray(json)
        val storyIds = ArrayList<Long>()
        for (i in 0 until result.length()) {
            storyIds.add(result.getLong(i))
        }

        storyListIds[sortType] = storyIds
        return true
    }

    private fun fetchItemDetails(itemId: Long): HNItemModel? {
        val url = String.format("%s/item/%d.json", hnBaseUrl, itemId)
        val itemJson = fetchJson(URL(url)) ?: return null

        return try {
            json.parse(HNItemModel.serializer(), itemJson)
        } catch (e: Exception) {
            Log.e("HNDataManager", "JSON parser for item id: $itemId", e)
            null
        }
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