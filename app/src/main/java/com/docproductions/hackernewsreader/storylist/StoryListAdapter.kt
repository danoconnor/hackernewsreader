package com.docproductions.hackernewsreader.storylist

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.docproductions.hackernewsreader.Constants
import com.docproductions.hackernewsreader.MainActivity
import com.docproductions.hackernewsreader.R
import com.docproductions.hackernewsreader.commentlist.ViewCommentsActivity
import com.docproductions.hackernewsreader.data.HNDataManager
import com.docproductions.hackernewsreader.data.HNItemModel
import com.docproductions.hackernewsreader.data.HNItemType
import com.docproductions.hackernewsreader.data.IHNDataFetchCallback
import com.docproductions.hackernewsreader.shared.StoryViewHolder
import kotlinx.android.synthetic.main.story_item_view.view.*
import kotlinx.serialization.json.Json

class StoryListAdapter(private val context: Context,
                       private var stories: List<HNItemModel>): RecyclerView.Adapter<StoryViewHolder>(), IHNDataFetchCallback {

    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    init {
        HNDataManager().fetchStoriesAsync(0, 20, this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.story_item_view, parent, false)
        return StoryViewHolder(context, view)
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
}