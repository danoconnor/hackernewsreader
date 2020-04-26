package com.docproductions.hackernewsreader.commentlist

import android.app.Activity
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
import org.w3c.dom.Comment

interface CommentActionDelegate {
    fun collapseComment(commentId: Long)
    fun expandComment(commentId: Long)
}

class ViewCommentsListAdapter(private val context: Context,
                       private val story: HNItemModel): RecyclerView.Adapter<RecyclerView.ViewHolder>(), CommentActionDelegate {

   inner class CommentItem(val item: HNItemModel, val depth: Int, var isHidden: Boolean = false, var isCollapsed: Boolean = false) { }

    private val storyViewType = 0
    private val commentViewType = 1
    private val collapsedCommentViewType = 2

    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    private var comments: MutableList<CommentItem> = ArrayList()
    private val visibleComments get() = comments.filter { !it.isHidden && !it.item.deleted }

    private val commentChangeMutex = Mutex()

    init {
        fetchAllChildComments(story, 0, 3)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == storyViewType) {
            val view = LayoutInflater.from(context).inflate(R.layout.story_item_view, parent, false)
            StoryViewHolder(context, view)
        } else if (viewType == commentViewType) {
            val view = LayoutInflater.from(context).inflate(R.layout.comment_item_view, parent, false)
            CommentViewHolder(context, view)
        } else {
            val view = LayoutInflater.from(context).inflate(R.layout.collapsed_comment_item_view, parent, false)
            CollapsedCommentViewHolder(context, view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (position == 0) {
            (holder as? StoryViewHolder)?.setItem(story, true)
        } else {
            // Subtract one since the story itself takes up the first position in the RecyclerView
            val comment = visibleComments[position - 1]
            if (comment.isCollapsed) {
                (holder as? CollapsedCommentViewHolder)?.setComment(comment.item, comment.depth, getChildComments(comments, comment.item.id).count(), this)
            } else {
                (holder as? CommentViewHolder)?.setComment(comment.item, comment.depth, this)
            }
        }
    }

    // Need to include the story item itself in the count
    override fun getItemCount(): Int = visibleComments.size + 1

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            storyViewType
        } else if (visibleComments[position - 1].isCollapsed) {
            collapsedCommentViewType
        } else {
            commentViewType
        }
    }

    override fun collapseComment(commentId: Long) {
        modifyChildComments(visibleComments, commentId, true)
    }

    override fun expandComment(commentId: Long) {
        modifyChildComments(comments, commentId, false)
    }

    private fun fetchAllChildComments(item: HNItemModel, currentDepth: Int, maxDepth: Int) {
        if (currentDepth > maxDepth) {
            return
        }

        HNDataManager().fetchChildItemsAsync(item) {

            // Add the children immediately after their parent
            val commentItems = it.map { CommentItem(it, currentDepth) }
            (context as? Activity)?.runOnUiThread {
                // We update the comments list on the UI thread since all other interactions with the
                // comments list run on the UI thread. This avoids concurrent modifications
                val parentIndex = comments.indexOfFirst { it.item.id == item.id }
                comments.addAll(parentIndex + 1, commentItems)
                notifyDataSetChanged()
            }

            for (child in it) {
                fetchAllChildComments(child, currentDepth + 1, maxDepth)
            }
        }
    }

    private fun getChildComments(commentList: List<CommentItem>, parentId: Long): List<CommentItem> {
        var foundParentComment = false
        var parentCommentDepth = -1
        var childComments = ArrayList<CommentItem>()
        for (comment in commentList) {
            if (comment.item.id == parentId) {
                foundParentComment = true
                parentCommentDepth = comment.depth
            } else if (foundParentComment) {
                if (comment.depth <= parentCommentDepth) {
                    break
                } else {
                    childComments.add(comment)
                }
            }
        }

        return childComments
    }

    private fun modifyChildComments(commentList: List<CommentItem>, targetCommentId: Long, shouldCollapse: Boolean) {
        var isChildComment = false
        var targetCommentDepth = -1
        for (comment in commentList) {
            if (comment.item.id == targetCommentId) {
                isChildComment = true
                targetCommentDepth = comment.depth
                comment.isCollapsed = shouldCollapse
            } else if (isChildComment) {
                // Mark child items of the target comment as hidden, until we find the next comment that is a peer to the target
                if (comment.depth > targetCommentDepth) {
                    comment.isHidden = shouldCollapse
                } else {
                    isChildComment = false
                }
            }
        }

        Handler(Looper.getMainLooper()).post {
            notifyDataSetChanged()
        }
    }
}