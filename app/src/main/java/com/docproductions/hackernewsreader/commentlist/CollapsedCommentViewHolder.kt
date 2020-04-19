package com.docproductions.hackernewsreader.commentlist

import android.content.Context
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.docproductions.hackernewsreader.R
import com.docproductions.hackernewsreader.data.HNItemModel
import kotlinx.android.synthetic.main.collapsed_comment_item_view.view.*
import java.lang.ref.WeakReference

class CollapsedCommentViewHolder(private val context: Context, commentView: View) : RecyclerView.ViewHolder(commentView) {

    private var comment: HNItemModel? = null
    private var commentActionDelegate: WeakReference<CommentActionDelegate>? = null

    fun setComment(comment: HNItemModel, depth: Int, numberOfChildComments: Int, commentActionDelegate: CommentActionDelegate) {
        this.comment = comment
        this.commentActionDelegate = WeakReference(commentActionDelegate)
        setContent(comment, depth, numberOfChildComments)
        setOnClickHandlers()
    }

    private fun setContent(comment: HNItemModel, depth: Int, numberOfChildComments: Int) {
        var collapsedText = String.format("%s, %s", comment.author, comment.getTimeSincePosted())
        if (numberOfChildComments > 0) collapsedText = String.format("%s (%d)", collapsedText, numberOfChildComments)
        itemView.collapsedCommentTextView.text = collapsedText

        val commentDepthIndent = context.resources.getDimension(R.dimen.comment_margin).toInt()

        val layoutParams = itemView.collapsedCommentTextView.layoutParams as ConstraintLayout.LayoutParams
        layoutParams.marginStart = commentDepthIndent * (depth + 1)
        itemView.collapsedCommentTextView.layoutParams = layoutParams
    }

    private fun setOnClickHandlers() {
        itemView.setOnClickListener {
            commentActionDelegate?.get()?.expandComment(comment?.id ?: -1)
        }
    }
}