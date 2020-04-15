package com.docproductions.hackernewsreader.commentlist

import android.content.Context
import android.view.View
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.marginLeft
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.docproductions.hackernewsreader.R
import com.docproductions.hackernewsreader.data.HNItemModel
import kotlinx.android.synthetic.main.comment_item_view.view.*

class CommentViewHolder(private val context: Context, commentView: View) : RecyclerView.ViewHolder(commentView) {

    fun setComment(comment: HNItemModel, depth: Int) {
        itemView.commentTextView.text = comment.text
        itemView.authorAndTimeTextView.text = String.format("%s, %s", comment.author, comment.getTimeSincePosted())

        val commentDepthIndent = context.resources.getDimension(R.dimen.comment_margin).toInt()

        val commentTextViewLayoutParams = itemView.commentTextView.layoutParams as ConstraintLayout.LayoutParams
        commentTextViewLayoutParams.marginStart = commentDepthIndent * (depth + 1)
        itemView.commentTextView.layoutParams = commentTextViewLayoutParams

        val authorTextViewLayoutParams = itemView.authorAndTimeTextView.layoutParams as ConstraintLayout.LayoutParams
        authorTextViewLayoutParams.marginStart = commentDepthIndent * (depth + 1)
        itemView.authorAndTimeTextView.layoutParams = authorTextViewLayoutParams
    }
}