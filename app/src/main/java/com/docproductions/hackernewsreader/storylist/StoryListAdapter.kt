package com.docproductions.hackernewsreader.storylist

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.docproductions.hackernewsreader.ObjectGraph
import com.docproductions.hackernewsreader.R
import com.docproductions.hackernewsreader.data.HNItemModel
import com.docproductions.hackernewsreader.data.HNItemType
import com.docproductions.hackernewsreader.shared.StoryViewHolder
import kotlinx.android.synthetic.main.activity_story_list.*

interface LoadMoreActionDelegate {
    fun loadMore()
}

class StoryListAdapter
    (private val context: Activity): RecyclerView.Adapter<RecyclerView.ViewHolder>(), LoadMoreActionDelegate {

    private val storyItemType = 0
    private val loadMoreButtonType = 1

    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private val storiesPerPage = 50

    private var currentLoadedPageIndex = 0
    private var stories = ArrayList<HNItemModel>()

    private var refreshInProgress = false

    init {
        refreshList()
    }

    override fun getItemViewType(position: Int): Int {
        // We'll add a load more button at the end of the loaded stories list
        return if (position < stories.size) {
            storyItemType
        } else {
            loadMoreButtonType
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == storyItemType) {
            val view = LayoutInflater.from(context).inflate(R.layout.story_item_view, parent, false)
            StoryViewHolder(context, view)
        } else {
            val view = LayoutInflater.from(context).inflate(R.layout.load_more_item_view, parent, false)
            LoadMoreItemHolder(context, view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder.itemViewType == storyItemType) {
            val storyViewHolder = holder as StoryViewHolder
            val story = stories[position]
            storyViewHolder.setItem(story, false)
        } else {
            val loadMoreItemHolder = holder as LoadMoreItemHolder
            loadMoreItemHolder.bind(this)
        }
    }

    override fun getItemCount(): Int {
        return if (refreshInProgress) {
            // Don't show anything while we are refreshing
            0
        } else {
            // Add one so we can add the load more button at the end
            stories.size + 1
        }
    }

    override fun loadMore() {
        currentLoadedPageIndex += storiesPerPage
        ObjectGraph.hnDataManager.fetchStoriesAsync(currentLoadedPageIndex, storiesPerPage) { success: Boolean, newStories: List<HNItemModel>? ->
            this.fetchCompleted(success, newStories)
        }
    }

    fun refreshList() {
        context.runOnUiThread {
            if (refreshInProgress) {
                Log.i("StoryListAdapter", "Refresh already in progress, not starting a new one")
                return@runOnUiThread
            }

            refreshInProgress = true
            notifyDataSetChanged()

            context.storiesLoadingProgressBar.visibility = View.VISIBLE

            stories.clear()
            currentLoadedPageIndex = 0
            ObjectGraph.hnDataManager.clearCachedStories()

            ObjectGraph.hnDataManager.fetchStoriesAsync(currentLoadedPageIndex, storiesPerPage) { success: Boolean, newStories: List<HNItemModel>? ->
                this.refreshInProgress = false
                this.fetchCompleted(success, newStories)

                context.runOnUiThread {
                    context.storiesLoadingProgressBar.visibility = View.GONE
                }
            }
        }


    }

    private fun fetchCompleted(success: Boolean, data: List<HNItemModel>?) {
        if (!success) {
            Log.println(Log.ERROR, "StoryListAdapter", "Fetch did not succeed")

            context.runOnUiThread {
                Toast.makeText(
                    context,
                    R.string.story_fetch_error,
                    Toast.LENGTH_LONG
                ).show()
            }

            return
        }

        if (data != null && data.isNotEmpty()) {
            this.stories.addAll(data.filter { !it.deleted })

            Handler(Looper.getMainLooper()).post {
                notifyDataSetChanged()
            }
        }
    }
}