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
import dev.liinahamari.loggy_sdk.screens.logs.RecordInteractor
import dev.liinahamari.loggy_sdk.screens.logs.log_list.PAGE_CAPACITY
import io.objectbox.Box
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.random.Random

private const val THREAD = "thread"

@Config(sdk = [Build.VERSION_CODES.O_MR1])
@RunWith(RobolectricTestRunner::class)
class FilterLogsMainThread {
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
    fun `when DB is empty and filter mode should hide non-main thread logs, then GetRecordResult#EmptyList is the result`() {
        val recordInteractor = RecordInteractor(LogToLogUiMapper(), composers, logBox)
        assert(logBox.isEmpty)

        recordInteractor.getFilteredRecord(filterStates = listOf(FilterState.NON_MAIN_THREAD))
            .test()
            .assertNoErrors()
            .assertComplete()
            .assertValueAt(0) { it is GetRecordResult.EmptyList }
    }

    @Test
    fun `when DB contains only logs written in main thread and filter mode should hide non-main thread logs, then GetRecordResult#EmptyList is the result`() {
        val recordInteractor = RecordInteractor(LogToLogUiMapper(), composers, logBox)
        assert(logBox.isEmpty)

        for (i in 0 until Random.nextInt(1, 1000)) {
            val logPriority = FlightRecorder.Priority.values().map { it.ordinal }.random() /*any priority should be suitable*/
            logBox.put(Log(thread = "main", body = "", timestamp = 1L, priority = logPriority))
        }
        assert(logBox.isEmpty.not())

        recordInteractor.getFilteredRecord(filterStates = listOf(FilterState.NON_MAIN_THREAD))
            .test()
            .assertNoErrors()
            .assertComplete()
            .assertValueAt(0) { it is GetRecordResult.EmptyList }
    }

    @Test
    fun `when DB contains only n log written in non-main thread and filter mode should hide non-main thread logs, then GetRecordResult#Success is the result with n entity attached (sliced to pages)`() {
        val recordInteractor = RecordInteractor(LogToLogUiMapper(), composers, logBox)
        assert(logBox.isEmpty)

        val labelString = "new_thread"
        val randomAmount = Random.nextInt(1, 1000)
        for (i in 0 until randomAmount) {
            val logPriority = FlightRecorder.Priority.values().map { it.ordinal }.random() /*any priority should be suitable*/
            val newThreadName = "$labelString$i"
            logBox.put(Log(thread = newThreadName, body = "", timestamp = 1L, priority = logPriority))
        }
        assert(logBox.isEmpty.not())

        //first page
        val firstPageSize: Int = if (randomAmount >= PAGE_CAPACITY.toInt()) PAGE_CAPACITY.toInt() else randomAmount % PAGE_CAPACITY.toInt()
        recordInteractor.getFilteredRecord(filterStates = listOf(FilterState.NON_MAIN_THREAD))
            .test()
            .assertNoErrors()
            .assertComplete()
            .assertValueAt(0) {
                it is GetRecordResult.Success &&
                        it.logs.size == firstPageSize &&
                        it.logs.all { log -> log.thread.matches("$labelString\\d+".toRegex()) }
            }
        if (randomAmount >= PAGE_CAPACITY.toInt()) return
        //last page
        recordInteractor.getFilteredRecord(filterStates = listOf(FilterState.NON_MAIN_THREAD))
            .test()
            .assertNoErrors()
            .assertComplete()
            .assertValueAt(0) {
                it is GetRecordResult.Success &&
                        it.logs.size == (randomAmount % PAGE_CAPACITY).toInt() &&
                        it.logs.all { log -> log.thread.matches("$labelString\\d+".toRegex()) }
            }
    }

    @Test
    fun `when DB contains n log written in non-main thread some logs in main thread and filter mode should hide non-main thread logs, then GetRecordResult#Success is the result with n entity attached (sliced to pages)`() {
        val recordInteractor = RecordInteractor(LogToLogUiMapper(), composers, logBox)
        assert(logBox.isEmpty)

        val labelString = "new_thread"
        val randomAmount = Random.nextInt(1, 1000)
        for (i in 0 until randomAmount) {
            val logPriority = FlightRecorder.Priority.values().map { it.ordinal }.random() /*any priority should be suitable*/
            val newThreadName = "$labelString$i"
            logBox.put(Log(thread = newThreadName, body = "", timestamp = 1L, priority = logPriority))
        }

        for (i in 0 until randomAmount) {
            val logPriority = FlightRecorder.Priority.values().map { it.ordinal }.random() /*any priority should be suitable*/
            logBox.put(Log(thread = "main", body = "", timestamp = 1L, priority = logPriority))
        }
        assert(logBox.isEmpty.not())

        //first page
        val firstPageSize: Int = if (randomAmount >= PAGE_CAPACITY.toInt()) PAGE_CAPACITY.toInt() else randomAmount % PAGE_CAPACITY.toInt()
        recordInteractor.getFilteredRecord(filterStates = listOf(FilterState.NON_MAIN_THREAD))
            .test()
            .assertNoErrors()
            .assertComplete()
            .assertValueAt(0) {
                it is GetRecordResult.Success &&
                        it.logs.size == firstPageSize &&
                        it.logs.all { log -> log.thread.matches("$labelString\\d+".toRegex()) }
            }
        if (randomAmount >= PAGE_CAPACITY.toInt()) return
        //last page
        recordInteractor.getFilteredRecord(filterStates = listOf(FilterState.NON_MAIN_THREAD))
            .test()
            .assertNoErrors()
            .assertComplete()
            .assertValueAt(0) {
                it is GetRecordResult.Success &&
                        it.logs.size == (randomAmount % PAGE_CAPACITY).toInt() &&
                        it.logs.all { log -> log.thread.matches("$labelString\\d+".toRegex()) }
            }
    }
}
