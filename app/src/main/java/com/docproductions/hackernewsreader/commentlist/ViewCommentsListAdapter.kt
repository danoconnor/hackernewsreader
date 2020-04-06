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
import org.w3c.dom.Comment

class ViewCommentsListAdapter(private val context: Context,
                       private val story: HNItemModel): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val storyViewType = 0
    private val commentViewType = 1

    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private var comments: MutableList<HNItemModel> = ArrayList()

    init {
        HNDataManager().fetchChildItemsAsync(story) {
            comments.addAll(0, it)

            Handler(Looper.getMainLooper()).post {
                notifyDataSetChanged()
            }
        }
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
            (holder as? CommentViewHolder)?.setComment(comment)
        }
    }

    override fun getItemCount(): Int = comments.size + 1

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            storyViewType
        } else {
            commentViewType
        }
    }
}