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

import android.util.Log
import androidx.annotation.VisibleForTesting
import dev.liinahamari.loggy_sdk.BuildConfig
import io.reactivex.rxjava3.core.Completable
import java.io.File
import java.io.FileNotFoundException
import java.util.concurrent.TimeUnit

const val SEPARATOR = "/"
const val DEBUG_LOGS_DIR = "TempLogs"
const val TAPE_VOLUME = 10485760 /*10 MB*/

class FlightRecorder private constructor() {
    companion object {
        private lateinit var logStorage: File

        fun logFileIs(file: File) {
            logStorage = file
        }

        fun getPriorityPattern(priority: Priority) = "$SEPARATOR${priority.name}$SEPARATOR"

        fun lifecycle(toPrintInLogcat: Boolean = true, what: () -> String) = printLogAndWriteToFile(what.invoke(), Priority.LIFECYCLE, toPrintInLogcat)
        fun i(toPrintInLogcat: Boolean = true, what: () -> String) = printLogAndWriteToFile(what.invoke(), Priority.I, toPrintInLogcat)
        fun d(toPrintInLogcat: Boolean = true, what: () -> String) = printLogAndWriteToFile(what.invoke(), Priority.D, toPrintInLogcat)
        fun w(toPrintInLogcat: Boolean = true, what: () -> String) = printLogAndWriteToFile(what.invoke(), Priority.W, toPrintInLogcat)
        fun wtf(toPrintInLogcat: Boolean = true, what: () -> String) = printLogAndWriteToFile(what.invoke(), Priority.WTF, toPrintInLogcat)

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

        @VisibleForTesting
        fun printLogAndWriteToFile(
            logMessage: String,
            priority: Priority,
            toPrintInLogcat: Boolean
        ) {
            with(logMessage.toLogMessage(priority)) {
                clearBeginningOfLogFileIfNeeded(this)

                Completable.fromCallable { logStorage.appendText(this) }
                    .timeout(5, TimeUnit.SECONDS)
                    .subscribe()

                if (toPrintInLogcat && BuildConfig.DEBUG) {
                    Log.i(this::class.java.simpleName, this)
                }
            }
        }


        fun getEntireRecord() = try {
            logStorage.readText()
        } catch (e: FileNotFoundException) {
            logStorage.createNewFile()
            logStorage.readText()
        }

        private fun clearBeginningOfLogFileIfNeeded(what: String) {
            val newDataSize = what.toByteArray().size
            if ((logStorage.length() + newDataSize.toLong()) > TAPE_VOLUME) {
                val dataToRemain = logStorage.readBytes().drop(newDataSize).toByteArray()
                logStorage.writeBytes(dataToRemain)
            }
        }

    }

    enum class Priority {
        I,
        D,
        W,
        E,
        WTF,
        LIFECYCLE
    }
}