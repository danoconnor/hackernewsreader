package com.docproductions.hackernewsreader.commentlist

import android.content.Context
import android.os.Build
import android.text.Html
import android.text.Spanned
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.docproductions.hackernewsreader.R
import com.docproductions.hackernewsreader.data.HNItemModel
import kotlinx.android.synthetic.main.comment_item_view.view.*

class CommentViewHolder(private val context: Context, commentView: View) : RecyclerView.ViewHolder(commentView) {

    fun setComment(comment: HNItemModel, depth: Int) {

        // The comment text is actually HTML that needs to be parsed
        val commentText = comment.text ?: ""
        val commentTextAsString = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(commentText, Html.FROM_HTML_MODE_LEGACY);
        } else {
            Html.fromHtml(commentText);
        }

        // Need to trim the parsed HTML since the parser will occasionally add extra line breaks to the end
        itemView.commentTextView.text = commentTextAsString.trim()
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