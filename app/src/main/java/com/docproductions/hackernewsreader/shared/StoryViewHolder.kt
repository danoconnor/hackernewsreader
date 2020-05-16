package com.docproductions.hackernewsreader.shared

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.text.Html
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.docproductions.hackernewsreader.Constants
import com.docproductions.hackernewsreader.commentlist.ViewCommentsActivity
import com.docproductions.hackernewsreader.data.HNItemModel
import kotlinx.android.synthetic.main.story_item_view.view.*
import kotlinx.serialization.json.Json

class StoryViewHolder(private val context: Context, storyView: View) : RecyclerView.ViewHolder(storyView) {

    private var story: HNItemModel? = null

    fun setItem(item: HNItemModel, isStoryDetailsMode: Boolean) {
        itemView.itemTitleTextView.text = item.title
        itemView.itemScoreTextView.text = item.score.toString()
        itemView.itemCommentsTextView.text = item.commentCount.toString()
        itemView.collapsedCommentTextView.text = String.format("%s, %s", item.author, item.getTimeSincePosted())
        itemView.linkTextView.text = item.url?.toString() ?: ""

        // Some stories are text based, not URL based. If we are in the comment view, show the text. We should not show the text in the stories list.
        if (item.url == null && !item.text.isNullOrEmpty() && isStoryDetailsMode) {
            val storyText = item.text
            val storyTextParsed = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Html.fromHtml(storyText, Html.FROM_HTML_MODE_LEGACY)
            } else {
                Html.fromHtml(storyText)
            }

            itemView.linkTextView.text = storyTextParsed.trim()
        }

        itemView.linkTextView.visibility = if (itemView.linkTextView.text.isNullOrEmpty()) {
            View.GONE
        } else {
            View.VISIBLE
        }

        story = item
        setOnClickListeners(isStoryDetailsMode)
    }

    private fun setOnClickListeners(isStoryDetailsMode: Boolean) {
        itemView.linkTextView.setOnClickListener {
            story?.url?.let {
                val webpage = Uri.parse(it)
                val intent = Intent(Intent.ACTION_VIEW, webpage)
                if (intent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(intent)
                }
            }
        }

        // If we're on the stories list, then add a click listener to take the user to the story details
        if (!isStoryDetailsMode) {
            itemView.setOnClickListener {
                if (story == null) {
                    return@setOnClickListener
                }

                val intent = Intent(context, ViewCommentsActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                val serializedStory = Json.stringify(HNItemModel.serializer(), story!!)
                intent.putExtra(
                    Constants.ActivityParameters.StoryModelParameterName,
                    serializedStory
                )
                context.startActivity(intent)
            }
        }
    }
}