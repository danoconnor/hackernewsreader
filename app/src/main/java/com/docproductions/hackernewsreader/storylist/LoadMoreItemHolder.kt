package com.docproductions.hackernewsreader.storylist

import android.content.Context
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.docproductions.hackernewsreader.R
import com.docproductions.hackernewsreader.data.HNItemModel
import kotlinx.android.synthetic.main.load_more_item_view.view.*
import java.lang.ref.WeakReference

class LoadMoreItemHolder(private val context: Context, loadMoreView: View) : RecyclerView.ViewHolder(loadMoreView) {

    private var loadMoreActionDelegate: WeakReference<LoadMoreActionDelegate>? = null

    fun bind(loadMoreActionDelegate: LoadMoreActionDelegate) {
        this.loadMoreActionDelegate = WeakReference(loadMoreActionDelegate)
        setOnClickHandlers()
    }

    private fun setOnClickHandlers() {
        itemView.loadMoreButton.setOnClickListener {
            loadMoreActionDelegate?.get()?.loadMore()
        }
    }
}