package com.docproductions.hackernewsreader.storylist

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.docproductions.hackernewsreader.ObjectGraph
import com.docproductions.hackernewsreader.R
import com.docproductions.hackernewsreader.data.HNItemModel
import com.google.firebase.crashlytics.FirebaseCrashlytics

import kotlinx.android.synthetic.main.activity_story_list.*
import kotlinx.android.synthetic.main.list_view.*

class StoryListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_story_list)
        setSupportActionBar(toolbar)

        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)

        ObjectGraph.hnDataManager.fetchStoriesAsync(0, 50) { success: Boolean, stories: List<HNItemModel>? ->
            this.runOnUiThread {
                if (!success) {
                    Toast.makeText(
                        this,
                        this.getString(R.string.story_fetch_error),
                        Toast.LENGTH_LONG).show()
                }
            }
        }

        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        listView.layoutManager = layoutManager
        listView.adapter =
            StoryListAdapter(this)
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
            R.id.action_refresh -> {
                (listView.adapter as? StoryListAdapter)?.refreshList()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
