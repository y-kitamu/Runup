package com.runapp.runup

import android.arch.persistence.room.*
import android.location.Location
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.Date
import java.util.concurrent.CountDownLatch


@Entity
class Record {
    // 一秒ごとの Location データを保持するクラス
    // DB に保存するデータ
    //     startDate, endDate, distance, duration
    // Json に保存するデータ (recordList 内)
    //     mLocations, lapDistances, lapDurations
    @Ignore val TAG = "Record"

    @PrimaryKey @ColumnInfo(name = "start_date")
    lateinit var startDate: Date
    @ColumnInfo(name = "end_date")
    lateinit var endDate: Date

    // 各ラップの Location情報、距離、時間 のリスト
    @Ignore var recordList: MutableList<Triple<MutableList<Location>, Float, Int>> = mutableListOf()
    @Ignore private var mLapLocation: MutableList<Location> = mutableListOf()
    @Ignore private lateinit var mPrevLocation: Location

    @ColumnInfo(name = "distance") var distance = 0.0f
    @ColumnInfo(name = "duration") var duration = 0
    @Ignore var lapDistance = 0.0f
    @Ignore var lapDuration = 0

    @Ignore private var mBackgroundThread = HandlerThread("background").also { it.start() }
    @Ignore private var mBackgroundHandler = Handler(mBackgroundThread.looper)

    fun start(location: Location) {
        startDate = Date()
        mPrevLocation = location
        mLapLocation.add(location)
    }

    fun add(location: Location) {
        val delta = mPrevLocation.distanceTo(location)
        distance += delta
        lapDistance += delta
        lapDuration++
        mPrevLocation = location
        mLapLocation.add(location)
    }

    fun lap() {
        if (mLapLocation.size > 1) {
            recordList.add(Triple(mLapLocation, lapDistance, lapDuration))

            lapDistance = 0.0f
            lapDuration = 0
            mLapLocation = mutableListOf(mPrevLocation)
        }
    }

    fun pause() {}

    fun stop() {
        endDate = Date()

        if (recordList.isEmpty() || mLapLocation.size > 1) {
            recordList.add(Triple(mLapLocation, lapDistance, lapDuration))
        }

        val latch = CountDownLatch(1)
        mBackgroundHandler.post {
            Constant.RECORD_DB.recordDao().insertAll(this)
            saveDataToJson()
            latch.countDown()
        }
        latch.await()
    }

    fun loadDataFromJson() {
        // TODO: 他の Utility Library を使ったほうがいいのか？　https://developers.google.com/maps/documentation/android-sdk/utility/
        val type =
            object : TypeToken<MutableList<Triple<MutableList<Location>, Float, Int>>>(){}.type
        val file = File(
            Constant.RECORD_DIR, String.format("$1%d_$2%d.json", startDate.time, endDate.time))
        if (file.exists()) {
            val jsonStr = FileInputStream(file).let {
                val data = ByteArray(file.length().toInt())
                it.read(data)
                it.close()
                String(data)
            }
            recordList = Gson().fromJson(jsonStr, type)
        }
    }

    fun saveDataToJson() {
        val type =
            object : TypeToken<MutableList<Triple<MutableList<Location>, Float, Int>>>(){}.type
        val jsonStr = Gson().toJson(recordList, type)
        try {
            val file = File(Constant.RECORD_DIR, String.format("$1%d_$2%d.json", startDate.time, endDate.time))
            FileOutputStream(file).write(jsonStr.toByteArray())
        } catch (e: Error) {
            Log.e(TAG, "saveDatasFromJson error", e)
        }
    }
}

class Converter {
    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromTimeStamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }
}

@Dao
interface RecordDao {
    @Query("SELECT * FROM record")
    fun getAll(): List<Record>

    @Query("SELECT * FROM record WHERE start_date BETWEEN :from AND :to")
    fun getMonthRecord(from: Date, to: Date): List<Record>

    @Query("SELECT * FROM record WHERE start_date == :start AND end_date == :end")
    fun getRecordOf(start: Date, end: Date): List<Record>

    @Insert
    fun insertAll(vararg records: Record)

    @Delete
    fun delete(record: Record)
}


@Database(entities = [Record::class], version = 1)
@TypeConverters(Converter::class)
abstract class RecordDatabase : RoomDatabase() {
    abstract fun recordDao(): RecordDao
}
