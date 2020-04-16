package com.docproductions.hackernewsreader.shared

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.docproductions.hackernewsreader.Constants
import com.docproductions.hackernewsreader.commentlist.ViewCommentsActivity
import com.docproductions.hackernewsreader.data.HNItemModel
import kotlinx.android.synthetic.main.story_item_view.view.*
import kotlinx.serialization.json.Json

class StoryViewHolder(private val context: Context, storyView: View) : RecyclerView.ViewHolder(storyView) {

    private var story: HNItemModel? = null

    init {
        itemView.setOnClickListener {
            if (story == null) {
                return@setOnClickListener
            }

            val intent = Intent(context, ViewCommentsActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            val serializedStory = Json.stringify(HNItemModel.serializer(), story!!)
            intent.putExtra(Constants.ActivityParameters.StoryModelParameterName, serializedStory)
            context.startActivity(intent)
        }

        itemView.linkTextView.setOnClickListener {
            story?.url?.let {
                val webpage = Uri.parse(it)
                val intent = Intent(Intent.ACTION_VIEW, webpage)
                if (intent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(intent)
                }
            }
        }
    }

    fun setItem(item: HNItemModel) {
        itemView.itemTitleTextView.text = item.title
        itemView.itemScoreTextView.text = item.score.toString()
        itemView.itemCommentsTextView.text = item.commentCount.toString()
        itemView.authorAndTimeTextView.text = String.format("%s, %s", item.author, item.getTimeSincePosted())
        itemView.linkTextView.text = item.url.toString()

        story = item
    }
}