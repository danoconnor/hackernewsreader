package com.docproductions.hackernewsreader.data

import org.json.JSONObject
import java.net.URL
import java.util.*
import java.util.concurrent.TimeUnit

enum class ItemType {
    job,
    story,
    comment,
    poll,
    pollopt,
    unknown
}

class HNItemModel(json: String) : JSONObject(json) {

    val id = this.getLong("id")
    val type = ItemType.valueOf(this.optString("type", "unknown"))
    val author = this.optString("by")
    val timePosted = Date(this.optLong("time", 0))
    val parent = this.optInt("parent")
    val kids: List<Int>
    val url = URL(this.optString("url"))
    val score = this.optInt("score")
    val title = this.optString("title")
    val commentCount = this.optInt("descendants")

    init {
        val kidsJsonArray = this.optJSONArray("kids")
        kids = arrayListOf()
        if (kidsJsonArray != null) {
            for (i in 0 until kidsJsonArray.length()) {
                kids.add(kidsJsonArray.getInt(i))
            }
        }
    }

    // A string describing how long it has been since this item was posted
    // Ex. "3 hrs ago"
    fun getTimeSincePosted() : String {
        val timeDiffMs = Date().time - this.timePosted.time

        var unitDescription = "ms"
        var value = timeDiffMs

        if (value > 1000) {
            // Convert to seconds
            unitDescription = "seconds"
            value = TimeUnit.SECONDS.convert(value, TimeUnit.MILLISECONDS)
        }

        if (value > 60) {
            // Convert to minutes
            unitDescription = "minutes"
            value = TimeUnit.MINUTES.convert(value, TimeUnit.SECONDS)
        }

        if (value > 60) {
            // Convert to hours
            unitDescription = "hours"
            value = TimeUnit.HOURS.convert(value, TimeUnit.MINUTES)
        }

        if (value > 24) {
            // Convert to days
            unitDescription = "days"
            value = TimeUnit.DAYS.convert(value, TimeUnit.HOURS)
        }

        // TODO: localize
        if (value < 2) {
            // Chop off the 's' at the end
            // Ex. "Days" becomes "Day"
            unitDescription = unitDescription.substring(0, unitDescription.count() - 1)
        }

        return String.format("%d %s ago", value, unitDescription)
    }
}