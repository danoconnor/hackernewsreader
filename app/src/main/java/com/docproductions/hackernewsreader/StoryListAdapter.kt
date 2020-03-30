package com.docproductions.hackernewsreader

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.docproductions.hackernewsreader.data.HNDataManager
import com.docproductions.hackernewsreader.data.HNItemModel
import com.docproductions.hackernewsreader.data.IHNDataFetchCallback
import kotlinx.android.synthetic.main.story_item_view.view.*
import java.net.URL

class StoryListAdapter(private val context: Context,
                       private var stories: List<HNItemModel>): RecyclerView.Adapter<StoryListAdapter.StoryViewHolder>(), IHNDataFetchCallback {

    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    init {
        HNDataManager().fetchStoriesAsync(0, 20, this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.story_item_view, parent, false)
        return StoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        val story = stories[position]
        holder.setItem(story)
    }

    override fun getItemCount(): Int = stories.size

    override fun fetchCompleted(success: Boolean, data: List<HNItemModel>) {
        this.stories = data

        Handler(Looper.getMainLooper()).post {
            notifyDataSetChanged()
        }
    }

    inner class StoryViewHolder(storyView: View) : RecyclerView.ViewHolder(storyView) {

        private var story: HNItemModel? = null

        init {
            itemView.setOnClickListener {
                // TODO: Go to comments
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
}