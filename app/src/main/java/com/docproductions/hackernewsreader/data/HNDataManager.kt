package com.docproductions.hackernewsreader.data

import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject
import java.lang.Exception
import java.net.URL
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

interface IHNDataFetchCallback {
    fun fetchCompleted(success: Boolean, data: List<HNItemModel>)
}

class HNDataManager {

    private val hnBaseUrl = "https://hacker-news.firebaseio.com/v0"

    // A list of the IDs of the top stories
    private var topStoryIds = ArrayList<Long>()

    fun fetchStoriesAsync(startIndex: Int, count: Int, callback: IHNDataFetchCallback) {
        GlobalScope.launch {
            if (topStoryIds.count() == 0) {
                if (!fetchTopStoryIds()) {
                    callback.fetchCompleted(false, ArrayList())
                }
            }

            val endIndex = Math.min(startIndex + count, topStoryIds.count())
            val validatedCount = endIndex - startIndex
            if (validatedCount <= 0) {
                callback.fetchCompleted(true, ArrayList())
                return@launch
            }

            // Kick off all story detail tasks async in parallel
            val deferredStories = ArrayList<Deferred<HNItemModel?>>()
            for (i in 0 until validatedCount) {
                deferredStories.add(async { fetchItemDetails(topStoryIds[i + startIndex]) })
            }

            // Aggregate the results of all the async fetches
            val stories = ArrayList<HNItemModel>()
            for (i in 0 until deferredStories.count()) {
                val story = deferredStories[i].await()
                if (story != null) {
                    stories.add(story)
                }
            }

            callback.fetchCompleted(true, stories)
        }
    }

    fun fetchCommentsAsync(storyItem: HNItemModel, callback: IHNDataFetchCallback) {

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
        val json = fetchJson(URL(url)) ?: return null

        return HNItemModel(json)
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