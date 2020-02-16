package com.docproductions.hackernewsreader

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.docproductions.hackernewsreader.data.HNItemModel
import java.util.*

class ItemListController(context: Context,
                         private val items: List<HNItemModel>): BaseAdapter() {

    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getView(position: Int, convertView: View?, container: ViewGroup?): View {
        var itemView = convertView
        if (itemView == null) {
            itemView = inflater.inflate(R.layout.item_view, container, false)
        }

        val itemModel = items[position]
        (itemView?.findViewById(R.id.itemTitleTextView) as? TextView)?.text = itemModel.title
        (itemView?.findViewById(R.id.itemScoreTextView) as? TextView)?.text = itemModel.score.toString()
        (itemView?.findViewById(R.id.itemCommentsTextView) as? TextView)?.text = itemModel.commentCount.toString()
        (itemView?.findViewById(R.id.authorAndTimeTextView) as? TextView)?.text = String.format("%s, %s", itemModel.author, itemModel.getTimeSincePosted())
        (itemView?.findViewById(R.id.linkTextView) as? TextView)?.text = itemModel.url.toString()

        return itemView!!
    }

    override fun getItem(position: Int): Any {
        return items[position]
    }

    override fun getItemId(position: Int): Long {
        return items[position].id
    }

    override fun getCount(): Int {
        return items.count()
    }
}