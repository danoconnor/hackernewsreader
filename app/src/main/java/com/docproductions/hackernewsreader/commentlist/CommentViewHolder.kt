package com.docproductions.hackernewsreader.commentlist

import android.content.Context
import android.os.Build
import android.text.Html
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.docproductions.hackernewsreader.R
import com.docproductions.hackernewsreader.data.HNItemModel
import kotlinx.android.synthetic.main.comment_item_view.view.*
import java.lang.ref.WeakReference

class CommentViewHolder(private val context: Context, commentView: View) : RecyclerView.ViewHolder(commentView) {

    private var comment: HNItemModel? = null
    private var commentActionDelegate: WeakReference<CommentActionDelegate>? = null

    init {
        itemView.commentTextView.text = ""
        itemView.authorTextView.text = ""
        hideOptionsButtons()
    }

    fun setComment(comment: HNItemModel, depth: Int, commentActionDelegate: CommentActionDelegate) {
        this.comment = comment
        this.commentActionDelegate = WeakReference(commentActionDelegate)
        setContent(comment, depth)
        setOnClickHandlers()
        hideOptionsButtons()
    }

    private fun setContent(comment: HNItemModel, depth: Int) {
        // The comment text is actually HTML that needs to be parsed
        val commentText = comment.text ?: ""
        val commentTextParsed = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(commentText, Html.FROM_HTML_MODE_LEGACY)
        } else {
            Html.fromHtml(commentText)
        }

        // Need to trim the parsed HTML since the parser will occasionally add extra line breaks to the end
        itemView.commentTextView.text = commentTextParsed.trim()
        itemView.authorTextView.text = String.format("%s, %s", comment.author, comment.getTimeSincePosted())

        val commentDepthIndent = context.resources.getDimension(R.dimen.standard_margin).toInt()

        val commentTextViewLayoutParams = itemView.commentTextView.layoutParams as ConstraintLayout.LayoutParams
        commentTextViewLayoutParams.marginStart = commentDepthIndent * (depth + 1)
        itemView.commentTextView.layoutParams = commentTextViewLayoutParams

        val authorTextViewLayoutParams = itemView.authorTextView.layoutParams as ConstraintLayout.LayoutParams
        authorTextViewLayoutParams.marginStart = commentDepthIndent * (depth + 1)
        itemView.authorTextView.layoutParams = authorTextViewLayoutParams

        val optionsButtonLayoutParams = itemView.optionsButtonContainer.layoutParams as ConstraintLayout.LayoutParams
        optionsButtonLayoutParams.marginStart = commentDepthIndent * (depth + 1)
        itemView.optionsButtonContainer.layoutParams = optionsButtonLayoutParams
    }

    private fun setOnClickHandlers() {
        itemView.setOnClickListener {
            showOptionsButtons()
        }

        itemView.collapseCommentButton.setOnClickListener {
            hideOptionsButtons()
            commentActionDelegate?.get()?.collapseComment(comment?.id ?: -1)
        }

        itemView.doneButton.setOnClickListener {
            hideOptionsButtons()
        }
    }

    private fun showOptionsButtons() {
        val authorTextViewLayoutParams = itemView.authorTextView.layoutParams as ConstraintLayout.LayoutParams
        authorTextViewLayoutParams.topMargin = 0
        itemView.authorTextView.layoutParams = authorTextViewLayoutParams

        itemView.optionsButtonContainer.visibility = View.VISIBLE

        itemView.commentItemLayout.setBackgroundColor(context.resources.getColor(R.color.colorSelectedHighlight))
    }

    private fun hideOptionsButtons() {
        val authorTextViewLayoutParams = itemView.authorTextView.layoutParams as ConstraintLayout.LayoutParams
        authorTextViewLayoutParams.topMargin = context.resources.getDimension(R.dimen.standard_margin).toInt()
        itemView.authorTextView.layoutParams = authorTextViewLayoutParams

        itemView.optionsButtonContainer.visibility = View.GONE
        itemView.commentItemLayout.setBackgroundColor(context.resources.getColor(R.color.colorPrimary))
    }
}