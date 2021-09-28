/*
Copyright 2021 liinahamari

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files
(the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge,
publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so,
subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR
ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

package dev.liinahamari.loggy_sdk.helper

import androidx.annotation.VisibleForTesting
import dev.liinahamari.loggy_sdk.BuildConfig
import dev.liinahamari.loggy_sdk.Loggy
import dev.liinahamari.loggy_sdk.db.Log
import io.objectbox.Box
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import java.lang.System.currentTimeMillis
import javax.inject.Inject

typealias LogTitle = String
typealias LogBody = String

const val MESSAGE_LENGTH_THRESHOLD = 50

/** Workaround for injection into Kotlin's object*/
open class LogBoxInjector {
    @Inject
    internal lateinit var logBox: Box<Log>
}

object FlightRecorder : LogBoxInjector() {
    init {
        Loggy.loggyComponent.inject(this)
    }

    /**
     * Logs a lifecycle event and writes it to the database.
     * Must not be called before {@link Loggy#init}
     * */
    fun lifecycle(toPrintInLogcat: Boolean = true, what: () -> String) = printLogAndWriteToFile(what.invoke(), Priority.L, toPrintInLogcat)

    /**
     * Logs an info message and writes it to the database.
     * Must not be called before {@link Loggy#init}
     * */
    fun i(toPrintInLogcat: Boolean = true, what: () -> String) = printLogAndWriteToFile(what.invoke(), Priority.I, toPrintInLogcat)

    /**
     * Logs a debug message and writes it to the database.
     * Must not be called before {@link Loggy#init}
     * */
    fun d(toPrintInLogcat: Boolean = true, what: () -> String) = printLogAndWriteToFile(what.invoke(), Priority.D, toPrintInLogcat)

    /**
     * Logs a warn message and writes it to the database.
     * Must not be called before {@link Loggy#init}
     * */
    fun w(toPrintInLogcat: Boolean = true, what: () -> String) = printLogAndWriteToFile(what.invoke(), Priority.W, toPrintInLogcat)

    /**
     * Logs an error and writes it to the database.
     * Must not be called before {@link Loggy#init}
     * */
    fun e(label: String, error: Throwable, toPrintInLogcat: Boolean = true) {
        val errorMessage = error.stackTrace.joinToString(
            separator = "\n\t",
            prefix = "label: $label\n${error.message}\n\t"
        )
        printLogAndWriteToFile(errorMessage, Priority.E, toPrintInLogcat)
        if (toPrintInLogcat) {
            error.printStackTrace()
        }
    }

    private fun printLogAndWriteToFile(logMessage: String, priority: Priority, toPrintInLogcat: Boolean) {
        if (logMessage.isBlank()) return
        val thread = Thread.currentThread().name
        Single.fromCallable { splitLogTitleAndLogBody(logMessage) }
            .doOnSuccess {
                logBox.put(
                    Log(
                        timestamp = currentTimeMillis(),
                        title = it.first,
                        priority = priority.ordinal,
                        body = it.second,
                        thread = thread
                    )
                )
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe()

        if (toPrintInLogcat && BuildConfig.DEBUG) {
            android.util.Log.i(this::class.java.simpleName, logMessage)
        }
    }

    /**
     * @param logMessage must not be blank.
     *
     * @return Log's title and log's body.
     * If log message is lesser than 200 symbols, then title will be null. In that case body includes all the message from original logMessage.
     * In case logMessage has 200 symbols or more, first 100 symbols plus three dots (...) returned as LogTitle and the rest as LogBody
     * */
    @VisibleForTesting
    fun splitLogTitleAndLogBody(logMessage: String): Pair<LogTitle?, LogBody> =
        if (logMessage.isBlank()) {
            throw IllegalArgumentException()
        } else {
            logMessage.takeIf {
                it.length < (MESSAGE_LENGTH_THRESHOLD * 2)
            }?.let {
                null to it
            } ?: logMessage.split("\n")
                .takeIf { it.size > 1 }
                ?.let {
                    it[0] to it
                        .drop(1)
                        .joinToString("\n\t")
                } ?: logMessage.take(MESSAGE_LENGTH_THRESHOLD).plus("...") to logMessage.drop(MESSAGE_LENGTH_THRESHOLD)
        }

    @VisibleForTesting
    fun clearDb() = logBox.removeAll()

    enum class Priority {
        I,
        D,
        W,
        E,
        L
    }
}