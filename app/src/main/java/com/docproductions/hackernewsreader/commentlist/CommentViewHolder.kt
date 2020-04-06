package com.docproductions.hackernewsreader.commentlist

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.docproductions.hackernewsreader.data.HNItemModel
import kotlinx.android.synthetic.main.comment_item_view.view.*

class CommentViewHolder(private val context: Context, commentView: View) : RecyclerView.ViewHolder(commentView) {
    fun setComment(comment: HNItemModel) {
        itemView.commentTextView.text = comment.text
        itemView.authorAndTimeTextView.text = String.format("%s, %s", comment.author, comment.getTimeSincePosted())
    }
}