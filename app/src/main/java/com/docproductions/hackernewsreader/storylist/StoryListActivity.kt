package com.docproductions.hackernewsreader.storylist

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.core.view.forEachIndexed
import androidx.recyclerview.widget.LinearLayoutManager
import com.docproductions.hackernewsreader.ObjectGraph
import com.docproductions.hackernewsreader.R
import com.docproductions.hackernewsreader.data.HNItemModel
import com.docproductions.hackernewsreader.data.HNStorySortType
import com.google.firebase.crashlytics.FirebaseCrashlytics

import kotlinx.android.synthetic.main.activity_story_list.*
import kotlinx.android.synthetic.main.list_view.*

class StoryListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_story_list)
        setSupportActionBar(toolbar)

        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)

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

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.let { menu ->
            val sortType = (listView.adapter as? StoryListAdapter)?.sortType ?: HNStorySortType.Top

            // Hide the menu item that matches the current sort type
            menu.findItem(R.id.action_top_stories).isVisible = sortType != HNStorySortType.Top
            menu.findItem(R.id.action_best_stories).isVisible = sortType != HNStorySortType.Best
            menu.findItem(R.id.action_new_stories).isVisible = sortType != HNStorySortType.New
            menu.findItem(R.id.action_ask_stories).isVisible = sortType != HNStorySortType.Ask
            menu.findItem(R.id.action_show_stories).isVisible = sortType != HNStorySortType.Show
            menu.findItem(R.id.action_jobs_stories).isVisible = sortType != HNStorySortType.Jobs

            toolbar.title = when (sortType) {
                HNStorySortType.Top -> getString(R.string.top_stories_label)
                HNStorySortType.Best -> getString(R.string.best_stories_label)
                HNStorySortType.New -> getString(R.string.new_stories_label)
                HNStorySortType.Ask -> getString(R.string.ask_stories_label)
                HNStorySortType.Show -> getString(R.string.show_stories_label)
                HNStorySortType.Jobs -> getString(R.string.jobs_stories_label)
            }

            return true
        }

        return false
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
            R.id.action_top_stories -> {
                changeStorySortType(HNStorySortType.Top)
                return true
            }
            R.id.action_best_stories -> {
                changeStorySortType(HNStorySortType.Best)
                return true
            }
            R.id.action_new_stories -> {
                changeStorySortType(HNStorySortType.New)
                return true
            }
            R.id.action_ask_stories -> {
                changeStorySortType(HNStorySortType.Ask)
                return true
            }
            R.id.action_show_stories -> {
                changeStorySortType(HNStorySortType.Show)
                return true
            }
            R.id.action_jobs_stories -> {
                changeStorySortType(HNStorySortType.Jobs)
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun changeStorySortType(newSortType: HNStorySortType) {
        (listView.adapter as? StoryListAdapter)?.changeSortType(newSortType)

        // Invalidate the menu list so onPrepareOptionsMenu is called and we can re-calculate
        // which items to show
        invalidateOptionsMenu()
    }
}
