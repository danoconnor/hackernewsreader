package com.docproductions.hackernewsreader.commentlist

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.docproductions.hackernewsreader.R
import com.docproductions.hackernewsreader.data.HNDataManager
import com.docproductions.hackernewsreader.data.HNItemModel
import com.docproductions.hackernewsreader.shared.StoryViewHolder
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex

class ViewCommentsListAdapter(private val context: Context,
                       private val story: HNItemModel): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

   inner class CommentItem(val item: HNItemModel, val depth: Int) { }

    private val storyViewType = 0
    private val commentViewType = 1

    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private var comments: MutableList<CommentItem> = ArrayList()

    private val commentChangeMutex = Mutex()

    init {
        fetchAllChildComments(story, 0, 2)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == storyViewType) {
            val view = LayoutInflater.from(context).inflate(R.layout.story_item_view, parent, false)
            StoryViewHolder(context, view)
        } else {
            val view = LayoutInflater.from(context).inflate(R.layout.comment_item_view, parent, false)
            CommentViewHolder(context, view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (position == 0) {
            (holder as? StoryViewHolder)?.setItem(story)
        } else {
            // Subtract one since the story itself takes up the first position in the RecyclerView
            val comment = comments[position - 1]
            (holder as? CommentViewHolder)?.setComment(comment.item, comment.depth)
        }
    }

    // Need to include the story item itself in the count
    override fun getItemCount(): Int = comments.size + 1

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            storyViewType
        } else {
            commentViewType
        }
    }

    private fun fetchAllChildComments(item: HNItemModel, currentDepth: Int, maxDepth: Int) {
        if (currentDepth > maxDepth) {
            return
        }

        HNDataManager().fetchChildItemsAsync(item) {
            GlobalScope.launch {
                commentChangeMutex.lock()

                // Add the children immediately after their parent
                val commentItems = it.map { CommentItem(it, currentDepth) }
                val parentIndex = comments.indexOfFirst { it.item.id == item.id }
                comments.addAll(parentIndex + 1, commentItems)

                // Update UI, then start fetching the children of the children
                Handler(Looper.getMainLooper()).post {
                    notifyDataSetChanged()
                }

                commentChangeMutex.unlock()

                for (child in it) {
                    fetchAllChildComments(child, currentDepth + 1, maxDepth)
                }
            }
        }
    }
}