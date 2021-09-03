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

import android.app.Application
import dev.liinahamari.loggy_sdk.helper.FlightRecorder
import io.reactivex.rxjava3.core.Completable
import java.io.File
import javax.inject.Inject

class DeleteZippedLogsFileUseCase @Inject constructor(private val applicationContext: Application) {
    fun execute(): Completable = Completable.fromCallable {
        File(
            File(applicationContext.filesDir, SHARED_LOGS_ZIP_FILE_NAME),
            SHARED_LOGS_DIR_NAME
        )
            .delete()
            .also {
                if (it.not()) {
                    FlightRecorder.e("Log file deletion failed", RuntimeException())
                }
            }
    }.onErrorComplete()
}