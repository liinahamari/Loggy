package dev.liinahamari.loggy_sdk.helper

import android.content.Context
import android.os.Build
import android.os.storage.StorageManager
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/** Represents date and in such format: "year-month-day_of_month 24_format_hours:minutes:seconds.milliseconds"
 *  For example:
 *  2020-12-23 00:12:11:101
 *  */
const val DATE_PATTERN_FOR_LOGGING = "yyyy-MM-dd HH:mm:ss:SSS"
fun now(): String = SimpleDateFormat(DATE_PATTERN_FOR_LOGGING, Locale.getDefault()).format(Date())

fun Context.isStorageSpaceAvailable(fileToStoreSize: Int): Boolean = getFreeSpaceInBytes() > fileToStoreSize

fun Context.getFreeSpaceInBytes(): Long = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
    with(getSystemService(StorageManager::class.java)) {
        getAllocatableBytes(getUuidForPath(filesDir))
    }
} else {
    filesDir.usableSpace
}

fun Context.createDirIfNotExist(dirName: String) = File(filesDir, dirName).apply {
    if (exists().not()) {
        mkdir()
    }
}

fun Context.createFileIfNotExist(fileName: String, dirName: String) = File(createDirIfNotExist(dirName), fileName).apply {
    if (exists().not()) {
        createNewFile()
    }
}

/** Only for RxView elements!*/
 fun Observable<Unit>.throttleFirst(skipDurationMillis: Long = 500L): Observable<Unit> = compose { it.throttleFirst(skipDurationMillis, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread()) }

 fun String.yellow() = 27.toChar() + "[33m$this" + 27.toChar() + "[0m"
 fun String.red() = 27.toChar() + "[31m$this" + 27.toChar() + "[0m"
