/*
 * Copyright (c) 2021. liinahamari
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files
 * (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge,
 * publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR
 * ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.liinahamari.loggy_sdk.screens.logs

import android.content.Context
import android.net.Uri
import androidx.annotation.VisibleForTesting
import androidx.core.content.FileProvider
import dev.liinahamari.loggy_sdk.BuildConfig
import dev.liinahamari.loggy_sdk.db.Log
import dev.liinahamari.loggy_sdk.helper.FlightRecorder
import dev.liinahamari.loggy_sdk.helper.createFileIfNotExist
import dev.liinahamari.loggy_sdk.helper.toReadableDate
import io.objectbox.Box
import io.reactivex.rxjava3.core.Observable
import java.io.BufferedOutputStream
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.inject.Inject

/** Must be corresponding <provider>'s authority in AndroidManifest */
@VisibleForTesting const val FILE_PROVIDER_META = ".fileprovider"

/** Must be corresponding dir name in file_paths.xml */
@VisibleForTesting const val SHARED_LOGS_DIR_NAME = "SharedLogs"

@VisibleForTesting const val SHARED_LOGS_ZIP_FILE_NAME = "logs.zip"
@VisibleForTesting const val SHARED_LOGS_TEXT_FILE_NAME = "logs.txt"

class CreateZippedLogFileUseCase @Inject constructor(private val logBox: Box<Log>) {
    fun execute(applicationContext: Context): Observable<CreateZipLogsFileResult> = Observable.just(BuildConfig.LIBRARY_PACKAGE_NAME + FILE_PROVIDER_META)
        .map { authority ->
            val zippedLogs = applicationContext.createFileIfNotExist(SHARED_LOGS_ZIP_FILE_NAME, SHARED_LOGS_DIR_NAME)
            ZipOutputStream(BufferedOutputStream(FileOutputStream(zippedLogs))).use { output ->
                output.putNextEntry(ZipEntry(SHARED_LOGS_TEXT_FILE_NAME))
                output.write(
                    logBox
                        .all
                        .joinToString {
                            "${it.timestamp.toReadableDate()} /${FlightRecorder.Priority.values()[it.priority]}/ (Thread: ${it.thread}): ${it.title ?: it.body}\n ${if (it.title != null) it.body else ""}"
                        }
                        .toByteArray() // todo catch lack of space on disk
                )
            }
            FileProvider.getUriForFile(applicationContext, authority, zippedLogs)
        }
        .doOnError { it.printStackTrace() }
        .delaySubscription(750, TimeUnit.MILLISECONDS)
        .map<CreateZipLogsFileResult> { CreateZipLogsFileResult.Success(it) }
        .onErrorReturn { CreateZipLogsFileResult.IOError }
        .startWithItem(CreateZipLogsFileResult.InProgress)
}

sealed class CreateZipLogsFileResult {
    data class Success(val path: Uri) : CreateZipLogsFileResult()
    object IOError : CreateZipLogsFileResult()
    object NotEnoughSpaceError : CreateZipLogsFileResult()
    object InProgress : CreateZipLogsFileResult()
}