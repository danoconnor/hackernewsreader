package com.docproductions.hackernewsreader

import com.docproductions.hackernewsreader.data.HNDataManager

object ObjectGraph {
    val hnDataManager: HNDataManager

    init {
        hnDataManager = HNDataManager()
    }
}