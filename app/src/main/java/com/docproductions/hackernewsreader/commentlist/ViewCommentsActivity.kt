package com.docproductions.hackernewsreader.commentlist

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.docproductions.hackernewsreader.Constants
import com.docproductions.hackernewsreader.R
import com.docproductions.hackernewsreader.data.HNItemModel
import com.docproductions.hackernewsreader.shared.VerticalSpaceItemDecoration
import kotlinx.android.synthetic.main.activity_story_list.*
import kotlinx.android.synthetic.main.list_view.*
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json

class ViewCommentsActivity : AppCompatActivity() {

    @OptIn(UnstableDefault::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_comments)
        setSupportActionBar(toolbar)

        val serializedStoryModel = intent.getStringExtra(Constants.ActivityParameters.StoryModelParameterName)
        val story = Json.parse(HNItemModel.serializer(), serializedStoryModel)

        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        listView.layoutManager = layoutManager
        listView.addItemDecoration(VerticalSpaceItemDecoration(resources.getDimension(R.dimen.large_margin).toInt()))
        listView.adapter =
            ViewCommentsListAdapter(
                this,
                story
            )
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}
