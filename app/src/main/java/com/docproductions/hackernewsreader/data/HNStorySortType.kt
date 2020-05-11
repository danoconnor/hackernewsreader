package com.docproductions.hackernewsreader.data

// The different ways to request the stories list
// More info here: https://github.com/HackerNews/API#new-top-and-best-stories
public enum class HNStorySortType(public val relativeUrl: String) {
    Top("topstories"),
    Best("beststories"),
    New("newstories"),
    Ask("askstories"),
    Show("showstories"),
    Jobs("jobstories")
}