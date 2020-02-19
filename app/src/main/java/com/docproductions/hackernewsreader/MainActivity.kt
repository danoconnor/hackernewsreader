package com.docproductions.hackernewsreader

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.ListView
import com.docproductions.hackernewsreader.data.HNItemModel

import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        // Test data
//        val json = "{\n" +
//                "  \"by\" : \"dhouston\",\n" +
//                "  \"descendants\" : 71,\n" +
//                "  \"id\" : 8863,\n" +
//                "  \"kids\" : [ 8952, 9224, 8917, 8884, 8887, 8943, 8869, 8958, 9005, 9671, 8940, 9067, 8908, 9055, 8865, 8881, 8872, 8873, 8955, 10403, 8903, 8928, 9125, 8998, 8901, 8902, 8907, 8894, 8878, 8870, 8980, 8934, 8876 ],\n" +
//                "  \"score\" : 111,\n" +
//                "  \"time\" : 1175714200,\n" +
//                "  \"title\" : \"My YC app: Dropbox - Throw away your USB drive\",\n" +
//                "  \"type\" : \"story\",\n" +
//                "  \"url\" : \"http://www.getdropbox.com/u/2/screencast.html\"\n" +
//                "}"
//
//        val list = ArrayList<HNItemModel>()
//        for (i in 0 until 10) {
//            list.add(HNItemModel(json))
//        }

        findViewById<ListView>(R.id.itemList).adapter = ItemListController(this.applicationContext, ArrayList())
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
