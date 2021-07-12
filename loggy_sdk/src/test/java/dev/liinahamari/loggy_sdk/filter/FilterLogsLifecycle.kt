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
import kotlin.random.Random

@Config(sdk = [Build.VERSION_CODES.O_MR1])
@RunWith(RobolectricTestRunner::class)
class FilterLogsLifecycle {
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
    fun `when file contains only one lifecycle log and filter mode should hide lifecycle, then GetRecordResult#EmptyList is the result`() {
        assert(logsFile.length() == 0L)
        FlightRecorder.lifecycle { DUMMY }
        assert(logsFile.length() != 0L)

        logsInteractor.sortLogs(listOf(FilterMode.HIDE_LIFECYCLE))
            .test()
            .assertNoErrors()
            .assertComplete()
            .assertValueAt(0, GetRecordResult.InProgress)
            .assertValueAt(1, GetRecordResult.InProgress) // because getEntireRecord() also has "InProgress" emission
            .assertValueAt(2) { it is GetRecordResult.EmptyList }
    }

    @Test
    fun `when file contains only lifecycle logs (n) and filter mode should hide lifecycle, then GetRecordResult#EmptyList is the result`() {
        assert(logsFile.length() == 0L)
        for (i in 0 until Random.nextInt(1000)) {
            FlightRecorder.lifecycle { DUMMY }
        }
        assert(logsFile.length() != 0L)

        logsInteractor.sortLogs(listOf(FilterMode.HIDE_LIFECYCLE))
            .test()
            .assertNoErrors()
            .assertComplete()
            .assertValueAt(0, GetRecordResult.InProgress)
            .assertValueAt(1, GetRecordResult.InProgress) // because getEntireRecord() also has "InProgress" emission
            .assertValueAt(2) { it is GetRecordResult.EmptyList }
    }

    @Test
    fun `when file contains only non-lifecycle log and filter mode should hide lifecycle, then GetRecordResult#EmptyList is the result and amount of logs is 1`() {
        assert(logsFile.length() == 0L)
        FlightRecorder.i { DUMMY }
        assert(logsFile.length() != 0L)

        logsInteractor.sortLogs(listOf(FilterMode.HIDE_LIFECYCLE))
            .test()
            .assertNoErrors()
            .assertComplete()
            .assertValueAt(0, GetRecordResult.InProgress)
            .assertValueAt(1, GetRecordResult.InProgress) // because getEntireRecord() also has "InProgress" emission
            .assertValueAt(2) { it is GetRecordResult.Success && it.logs.size == 1 }
    }

    @Test
    fun `when file contains only non-lifecycle logs (n) and filter mode should hide lifecycle, then GetRecordResult#Success is the result and amount of logs equal to n`() {
        val randomAmount = Random.nextInt(1000)
        for (i in 0 until randomAmount) {
            FlightRecorder.i { DUMMY }
        }
        assert(logsFile.length() != 0L)

        logsInteractor.sortLogs(listOf(FilterMode.HIDE_LIFECYCLE))
            .test()
            .assertNoErrors()
            .assertComplete()
            .assertValueAt(0, GetRecordResult.InProgress)
            .assertValueAt(1, GetRecordResult.InProgress) // because getEntireRecord() also has "InProgress" emission
            .assertValueAt(2) { it is GetRecordResult.Success && it.logs.size == randomAmount }
    }
}