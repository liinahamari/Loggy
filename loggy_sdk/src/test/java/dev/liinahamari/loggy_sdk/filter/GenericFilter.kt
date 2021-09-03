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

import android.app.Application
import android.content.Context
import android.os.Build
import androidx.test.platform.app.InstrumentationRegistry
import dev.liinahamari.loggy_sdk.db.Log
import dev.liinahamari.loggy_sdk.db.LogToLogUiMapper
import dev.liinahamari.loggy_sdk.db.MyObjectBox
import dev.liinahamari.loggy_sdk.helper.BaseComposers
import dev.liinahamari.loggy_sdk.helper.FlightRecorder
import dev.liinahamari.loggy_sdk.rules.ImmediateSchedulersRule
import dev.liinahamari.loggy_sdk.screens.logs.FilterState
import dev.liinahamari.loggy_sdk.screens.logs.GetRecordResult
import dev.liinahamari.loggy_sdk.screens.logs.LogUi
import dev.liinahamari.loggy_sdk.screens.logs.RecordInteractor
import io.mockk.every
import io.mockk.spyk
import io.objectbox.Box
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Config(sdk = [Build.VERSION_CODES.O_MR1])
@RunWith(RobolectricTestRunner::class)
class GenericFilter {
    @get:Rule
    val immediateSchedulersRule = ImmediateSchedulersRule()

    private val context: Context = InstrumentationRegistry.getInstrumentation().context
    private val application = InstrumentationRegistry.getInstrumentation().context.applicationContext as Application
    private val composers = BaseComposers()

    private val logBox: Box<Log> = MyObjectBox.builder()
        .androidContext(context)
        .build()
        .boxFor(Log::class.java)

    @After
    fun `tear down`() {
        logBox.removeAll()
    }

    @Test
    fun `when DB has the only Error log written in non-main thread and filters passed are NON_MAIN_THREAD and SHOW_ONLY_ERRORS then result is Success with 1 entity attached`() {
        assert(logBox.isEmpty)
        val threadLabel = "new_thread"
        logBox.put(Log(timestamp = 1L, priority = FlightRecorder.Priority.E.ordinal, body = "", thread = threadLabel))
        RecordInteractor(LogToLogUiMapper(), composers, logBox).getFilteredRecord(0, listOf(FilterState.SHOW_ONLY_ERRORS, FilterState.NON_MAIN_THREAD))
            .test()
            .assertNoErrors()
            .assertComplete()
            .assertValueAt(0) { it is GetRecordResult.Success &&
                    it.logs.size == 1 &&
                    it.logs.first() is LogUi.ErrorLog &&
                    it.logs.first().thread == threadLabel
            }
    }

    @Test
    fun `when DB has the only Error log written in non-main thread and filters passed are NON_MAIN_THREAD and NOT_LIFECYCLE_EVENT then result is Success with 1 entity attached`() {
        assert(logBox.isEmpty)
        val threadLabel = "new_thread"
        logBox.put(Log(timestamp = 1L, priority = FlightRecorder.Priority.E.ordinal, body = "", thread = threadLabel))
        RecordInteractor(LogToLogUiMapper(), composers, logBox).getFilteredRecord(0, listOf(FilterState.NOT_LIFECYCLE_EVENT, FilterState.NON_MAIN_THREAD))
            .test()
            .assertNoErrors()
            .assertComplete()
            .assertValueAt(0) { it is GetRecordResult.Success &&
                    it.logs.size == 1 &&
                    it.logs.first() is LogUi.ErrorLog &&
                    it.logs.first().thread == threadLabel
            }
    }

    @Test
    fun `when DB has the only Error log written in non-main thread and filters passed are SHOW_ONLY_ERRORS, NON_MAIN_THREAD and NOT_LIFECYCLE_EVENT then result is Success with 1 entity attached`() {
        assert(logBox.isEmpty)
        val threadLabel = "new_thread"
        logBox.put(Log(timestamp = 1L, priority = FlightRecorder.Priority.E.ordinal, body = "", thread = threadLabel))
        RecordInteractor(LogToLogUiMapper(), composers, logBox).getFilteredRecord(0, listOf(FilterState.NOT_LIFECYCLE_EVENT, FilterState.NON_MAIN_THREAD, FilterState.SHOW_ONLY_ERRORS))
            .test()
            .assertNoErrors()
            .assertComplete()
            .assertValueAt(0) { it is GetRecordResult.Success &&
                    it.logs.size == 1 &&
                    it.logs.first() is LogUi.ErrorLog &&
                    it.logs.first().thread == threadLabel
            }
    }

    @Test
    fun `when DB fetch causes error, then GetRecordResult#IOError is the result`() {
        val brokenBox = spyk(logBox) {
            every { query() } throws IllegalArgumentException()
        }
        val recordInteractor = RecordInteractor(LogToLogUiMapper(), composers, brokenBox)

        recordInteractor.getFilteredRecord(0, emptyList())
            .test()
            .assertNoErrors()
            .assertComplete()
            .assertValueAt(0) { it is GetRecordResult.Error.IOError }

        recordInteractor.getFilteredRecord(0, listOf(FilterState.SHOW_ONLY_ERRORS))
            .test()
            .assertNoErrors()
            .assertComplete()
            .assertValueAt(0) { it is GetRecordResult.Error.IOError }

        recordInteractor.getFilteredRecord(0, listOf(FilterState.NON_MAIN_THREAD))
            .test()
            .assertNoErrors()
            .assertComplete()
            .assertValueAt(0) { it is GetRecordResult.Error.IOError }

        recordInteractor.getFilteredRecord(0, listOf(FilterState.NOT_LIFECYCLE_EVENT))
            .test()
            .assertNoErrors()
            .assertComplete()
            .assertValueAt(0) { it is GetRecordResult.Error.IOError }

        recordInteractor.getFilteredRecord(0, listOf(FilterState.NOT_LIFECYCLE_EVENT, FilterState.NON_MAIN_THREAD, FilterState.SHOW_ONLY_ERRORS))
            .test()
            .assertNoErrors()
            .assertComplete()
            .assertValueAt(0) { it is GetRecordResult.Error.IOError }
    }

}