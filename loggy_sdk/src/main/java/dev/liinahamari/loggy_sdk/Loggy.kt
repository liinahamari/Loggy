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
import dev.liinahamari.loggy_sdk.di.DaggerLoggyComponent
import dev.liinahamari.loggy_sdk.di.LoggyComponent
import dev.liinahamari.loggy_sdk.helper.FlightRecorder
import java.io.File

object Loggy {
    internal lateinit var loggyComponent: LoggyComponent

    fun init(application: Application, logFile: File, integratorEmail: String, userId: String) {
        FlightRecorder.logFileIs(logFile)
        loggyComponent = DaggerLoggyComponent.builder()
            .application(application)
            .logFile(logFile)
            .email(integratorEmail)
            .userId(userId)
            .build()
    }
}