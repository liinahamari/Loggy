package dev.liinahamari.loggy_sdk.helper

import android.content.Context
import androidx.annotation.VisibleForTesting
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
internal fun Observable<Unit>.throttleFirst(skipDurationMillis: Long = 500L): Observable<Unit> = compose { it.throttleFirst(skipDurationMillis, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread()) }


internal fun String.toLogMessage(priority: FlightRecorder.Priority) = "${FlightRecorder.getPriorityPattern(priority)}  ${now()} $SEPARATOR${Thread.currentThread().name}$SEPARATOR: $this\n\n"

internal fun Throwable.toErrorLogMessage(label: String) = stackTrace.joinToString(
    separator = "\n\t",
    prefix = "label: $label\n${message}\n\t"
).toLogMessage(FlightRecorder.Priority.E)

internal fun String.yellow() = 27.toChar() + "[33m$this" + 27.toChar() + "[0m"
internal fun String.red() = 27.toChar() + "[31m$this" + 27.toChar() + "[0m"
