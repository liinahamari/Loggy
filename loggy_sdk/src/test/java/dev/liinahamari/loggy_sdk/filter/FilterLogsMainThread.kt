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

package dev.liinahamari.loggy_sdk.filter

import android.content.Context
import android.os.Build
import androidx.test.platform.app.InstrumentationRegistry
import dev.liinahamari.loggy_sdk.helper.BaseComposers
import dev.liinahamari.loggy_sdk.helper.FlightRecorder
import dev.liinahamari.loggy_sdk.helper.yellow
import dev.liinahamari.loggy_sdk.rules.ImmediateSchedulersRule
import dev.liinahamari.loggy_sdk.screens.logs.FilterMode
import dev.liinahamari.loggy_sdk.screens.logs.GetRecordResult
import dev.liinahamari.loggy_sdk.screens.logs.LoggerInteractor
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@Config(sdk = [Build.VERSION_CODES.O_MR1])
@RunWith(RobolectricTestRunner::class)
class FilterLogsMainThread {
    @get:Rule
    val immediateSchedulersRule = ImmediateSchedulersRule()

    private val logsFile = File.createTempFile("logs", ".tmp")
    private val context: Context = InstrumentationRegistry.getInstrumentation().context
    private val composers = BaseComposers()
    private val logsInteractor = LoggerInteractor(context, composers, logsFile)

    @Before
    fun setUp() {
        FlightRecorder.logFileIs(logsFile)
    }

    @After
    fun tearDown() {
        logsFile.delete()
    }

    @Test
    fun `log added from non-main thread is filtered well - FlightRecorder#Priority#I`() = writeLogsAndCheckNonMainThreadFilter(FlightRecorder.Priority.I)

    @Test
    fun `log added from non-main thread is filtered well - FlightRecorder#Priority#D`() = writeLogsAndCheckNonMainThreadFilter(FlightRecorder.Priority.D)

    @Test
    fun `log added from non-main thread is filtered well - FlightRecorder#Priority#W`() = writeLogsAndCheckNonMainThreadFilter(FlightRecorder.Priority.W)

    @Test
    fun `log added from non-main thread is filtered well - FlightRecorder#Priority#L`() = writeLogsAndCheckNonMainThreadFilter(FlightRecorder.Priority.L)

    @Test
    fun `log added from non-main thread is filtered well - FlightRecorder#Priority#E`() = writeLogsAndCheckNonMainThreadFilter(FlightRecorder.Priority.E)

    /** Logs one message from main thread and one from new thread. Filtering should keep non-main thread log and get rid of other*/
    private fun writeLogsAndCheckNonMainThreadFilter(priority: FlightRecorder.Priority) {
        assert(logsFile.length() == 0L)

        when (priority) {
            FlightRecorder.Priority.D -> FlightRecorder.d { "Message from main thread" }
            FlightRecorder.Priority.I -> FlightRecorder.i { "Message from main thread" }
            FlightRecorder.Priority.W -> FlightRecorder.w { "Message from main thread" }
            FlightRecorder.Priority.E -> FlightRecorder.e("Error from main thread", IllegalArgumentException(), toPrintInLogcat = false)
            FlightRecorder.Priority.L -> FlightRecorder.lifecycle { "Message from main thread" }
        }

        val newThreadName = "new_thread"
        Thread ({
            with(Thread.currentThread().name) {
                when (priority) {
                    FlightRecorder.Priority.D -> FlightRecorder.d { "Message from non-main thread" }
                    FlightRecorder.Priority.I -> FlightRecorder.i { "Message from non-main thread" }
                    FlightRecorder.Priority.W -> FlightRecorder.w { "Message from non-main thread" }
                    FlightRecorder.Priority.E -> FlightRecorder.e("Error from non-main thread", IllegalArgumentException(), toPrintInLogcat = false)
                    FlightRecorder.Priority.L -> FlightRecorder.lifecycle { "Message from non-main thread" }
                }

                println(this.yellow())
            }
        }, newThreadName).start()
        Thread.sleep(500)

        assert(logsFile.length() != 0L)
        logsInteractor.sortLogs(listOf(FilterMode.SHOW_NON_MAIN_THREAD))
            .test()
            .assertNoErrors()
            .assertComplete()
            .assertValueAt(0, GetRecordResult.InProgress)
            .assertValueAt(1, GetRecordResult.InProgress) // because getEntireRecord() also has "InProgress" emission
            .assertValueAt(2) { it is GetRecordResult.Success && it.logs.size == 1 && it.logs[0].thread == newThreadName }
    }
}