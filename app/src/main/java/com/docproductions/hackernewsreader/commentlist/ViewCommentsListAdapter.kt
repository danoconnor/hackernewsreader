package com.docproductions.hackernewsreader.commentlist

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.docproductions.hackernewsreader.ObjectGraph
import com.docproductions.hackernewsreader.R
import com.docproductions.hackernewsreader.data.HNDataManager
import com.docproductions.hackernewsreader.data.HNItemModel
import com.docproductions.hackernewsreader.shared.StoryViewHolder
import kotlinx.android.synthetic.main.activity_story_list.*
import kotlinx.android.synthetic.main.activity_view_comments.*
import kotlinx.coroutines.sync.Mutex

interface CommentActionDelegate {
    fun collapseComment(commentId: Long)
    fun expandComment(commentId: Long)
}

class ViewCommentsListAdapter(private val context: Activity,
                               private val story: HNItemModel): RecyclerView.Adapter<RecyclerView.ViewHolder>(), CommentActionDelegate {

    inner class CommentItem(val itemId: Long, var item: HNItemModel?, val depth: Int, var isLoading: Boolean = false, var isHidden: Boolean = false, var isCollapsed: Boolean = false) { }

    private val storyViewType = 0
    private val commentViewType = 1
    private val collapsedCommentViewType = 2

    private var comments: MutableList<CommentItem> = ArrayList()
    private val visibleComments get() = comments.filter { !it.isHidden && !(it.item?.deleted ?: false) }

    init {
        if (story.commentCount != null && story.commentCount > 0) {
            context.commentsLoadingProgressBar.visibility = View.VISIBLE
            comments.addAll(story.children.map { childItemId -> CommentItem(childItemId, null, 0) })
        } else {
            context.commentsLoadingProgressBar.visibility = View.GONE
        }
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

            // We don't start loading comments until they are ready to be shown
            // Start the loading process if we don't have the comment data yet and haven't started the fetch
            if (comment.item == null && !comment.isLoading) {
                comment.isLoading = true
                ObjectGraph.hnDataManager.fetchItemDetailsAsync(comment.itemId) { success, loadedComment ->
                    if (!success || loadedComment == null) {
                        Log.e("ViewCommentsList", "Failed to fetch comment data for id ${comment.itemId}")
                        return@fetchItemDetailsAsync
                    }

                    context.runOnUiThread {
                        context.commentsLoadingProgressBar.visibility = View.GONE

                        var loadedCommentIndex = comments.indexOfFirst { it.itemId == comment.itemId }
                        comments[loadedCommentIndex].item = loadedComment
                        comments[loadedCommentIndex].isLoading = false

                        // TODO: Testing, remove
                        Log.i("ViewCommentsList", "Loaded comment count: ${comments.count { it.item != null }}")

                        // Add any children of the loaded comment right after it in the list
                        comments.addAll(loadedCommentIndex + 1, loadedComment.children.map { childItemId -> CommentItem(childItemId, null, comment.depth + 1) })
                        notifyDataSetChanged()
                    }
                }
            } else if (comment.item != null) {
                if (comment.isCollapsed) {
                    (holder as? CollapsedCommentViewHolder)?.setComment(comment.item!!, comment.depth, getChildComments(comments, comment.itemId).count(), this)
                } else {
                    (holder as? CommentViewHolder)?.setComment(comment.item!!, comment.depth, this)
                }
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

    private fun getChildComments(commentList: List<CommentItem>, parentId: Long): List<CommentItem> {
        var foundParentComment = false
        var parentCommentDepth = -1
        val childComments = ArrayList<CommentItem>()
        for (comment in commentList) {
            if (comment.itemId == parentId) {
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
            if (comment.itemId == targetCommentId) {
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