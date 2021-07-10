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

package dev.liinahamari.loggy_sdk

import android.app.Application
import android.content.Context
import dev.liinahamari.loggy_sdk.di.DaggerLoggyComponent
import dev.liinahamari.loggy_sdk.di.LoggyComponent
import dev.liinahamari.loggy_sdk.helper.FlightRecorder
import dev.liinahamari.loggy_sdk.helper.TAPE_VOLUME
import dev.liinahamari.loggy_sdk.helper.getFreeSpaceInBytes
import java.io.File
import java.lang.RuntimeException

object Loggy {
     lateinit var loggyComponent: LoggyComponent

    fun init(application: Application, logFile: File, integratorEmail: String, userId: String) {
        if (isStorageSpaceAvailable(application).not()) {
            throw LoggyInitializationException("Not enough space to instantiate log storage file! Free space: ${application.getFreeSpaceInBytes()} bytes")
        }

        FlightRecorder.logFileIs(logFile)
        loggyComponent = DaggerLoggyComponent.builder()
            .application(application)
            .logFile(logFile)
            .email(integratorEmail)
            .userId(userId)
            .build()
    }

    private fun isStorageSpaceAvailable(context: Context): Boolean = context.getFreeSpaceInBytes() > TAPE_VOLUME
}

class LoggyInitializationException(cause: String): RuntimeException(cause)