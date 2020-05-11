package com.docproductions.hackernewsreader.data

import android.net.Uri
import kotlinx.serialization.*
import org.json.JSONObject
import java.lang.Exception
import java.util.*
import java.util.concurrent.TimeUnit

@Serializable
data class HNItemModel(
    val id: Long,
    val type: HNItemType,
    @SerialName("by")
    val author: String? = null,
    @SerialName("time")
    val timeUnix: Long,
    val parent: Long? = null,
    @SerialName("kids")
    val children: List<Long> = ArrayList<Long>(),
    val url: String? = null,
    val score: Int? = null,
    val title: String? = null,
    val text: String? = null,
    @SerialName("descendants")
    val commentCount: Int? = null,
    val deleted: Boolean = false) {

    @Transient
    val timePosted = Date(this.timeUnix * 1000)

    // A string describing how long it has been since this item was posted
    // Ex. "3 hours ago"
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