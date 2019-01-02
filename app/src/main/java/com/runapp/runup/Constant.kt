package com.runapp.runup

import android.arch.persistence.room.Room
import android.content.Context
import java.io.File


class Constant {

    enum class Permissions(val idx: Int) {
        ACCESS_FINE_LOCATION(0),
        INTERNET(1)
    }

    enum class MainTabFragments(val idx: Int, val title: String) {
        RECORD(0, "record"),
        HISTORY(1, "history"),
        COMMUNITY(2, "community")
    }

    enum class StatisticTabFragments(val idx: Int, val title: String) {
        MAP(0, "map"),
        LAP(1, "lap"),
        GRAPH(2, "graph")
    }

    companion object {

        val ARG_PARAM1 = "arg_param1"
        val ARG_PARAM2 = "arg_param2"

        val MAX_COLORMAP_SPEED = 20.0f
        val MIN_COLORMAP_SPEED = 5.0f
        val MAP_LINE_WIDTH = 20.0f
        val MAPVIEW_BUNDLE_KEY = "MapViewBundleKey"

        val RECORD_DB_BASENAME = "record_database"
        lateinit var RECORD_DB: RecordDatabase
        lateinit var RECORD_DIR: File

        fun init(activity: Context) {
            RECORD_DB = Room.databaseBuilder(
                activity.applicationContext,
                RecordDatabase::class.java, "RECORD_DB_BASENAME").build()

            RECORD_DIR = activity.filesDir.also { it.mkdirs() }
        }
    }
}
