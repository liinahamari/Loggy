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

import android.os.Build
import android.os.Looper.getMainLooper
import dev.liinahamari.loggy_sdk.helper.FlightRecorder
import dev.liinahamari.loggy_sdk.helper.FlightRecorder.Companion.d
import dev.liinahamari.loggy_sdk.helper.FlightRecorder.Companion.e
import dev.liinahamari.loggy_sdk.helper.FlightRecorder.Companion.i
import dev.liinahamari.loggy_sdk.helper.FlightRecorder.Companion.lifecycle
import dev.liinahamari.loggy_sdk.helper.FlightRecorder.Companion.w
import dev.liinahamari.loggy_sdk.helper.FlightRecorder.Companion.wtf
import dev.liinahamari.loggy_sdk.helper.toErrorLogMessage
import dev.liinahamari.loggy_sdk.helper.toLogMessage
import dev.liinahamari.loggy_sdk.helper.yellow
import dev.liinahamari.loggy_sdk.rules.ImmediateSchedulersRule
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import java.io.File

@Config(sdk = [Build.VERSION_CODES.O_MR1])
@RunWith(RobolectricTestRunner::class)
class FlightRecorderTest {
    @get:Rule
    val immediateSchedulersRule = ImmediateSchedulersRule()

    private val logsFile = File.createTempFile("logs", ".tmp")

    @Before
    fun setup() {
        shadowOf(getMainLooper()).idle()
        FlightRecorder.logFileIs(logsFile)
    }

    @After
    fun tearDown() {
        logsFile.writeText("")
        assertEquals(0, logsFile.length())
    }

    @Test
    fun `file's size does not change if empty string added while logging`() {
        assertEquals(0, logsFile.length())

        i { "" }

        assertEquals(0L, logsFile.length())
    }

    @Test
    fun `FlightRecorder is writing to file while i logging`() {
        assertEquals(0, logsFile.length())
        val text = "some_text"

        i { text }

        assert(logsFile.length() > 0)
        assertEquals(text.toLogMessage(FlightRecorder.Priority.I).toByteArray().size.toLong(), logsFile.length())
    }

    @Test
    fun `FlightRecorder is writing to file while d logging`() {
        assertEquals(0, logsFile.length())
        val text = "some_text"

        d { text }

        assert(logsFile.length() > 0)
        assertEquals(text.toLogMessage(FlightRecorder.Priority.D).toByteArray().size.toLong(), logsFile.length())
    }

    @Test
    fun `FlightRecorder is writing to file while lifecycle logging`() {
        assertEquals(0, logsFile.length())
        val text = "some_text"

        lifecycle { text }

        assert(logsFile.length() > 0)
        assertEquals(text.toLogMessage(FlightRecorder.Priority.LIFECYCLE).toByteArray().size.toLong(), logsFile.length())
    }

    @Test
    fun `FlightRecorder is writing to file while w logging`() {
        assertEquals(0, logsFile.length())
        val text = "some_text"

        w { text }

        assert(logsFile.length() > 0)
        assertEquals(text.toLogMessage(FlightRecorder.Priority.W).toByteArray().size.toLong(), logsFile.length())
    }

    @Test
    fun `FlightRecorder is writing to file while wtf logging`() {
        assertEquals(0, logsFile.length())
        val text = "some_text"

        wtf { text }

        assert(logsFile.length() > 0)
        assertEquals(text.toLogMessage(FlightRecorder.Priority.WTF).toByteArray().size.toLong(), logsFile.length())
    }

    @Test
    fun `FlightRecorder is writing to file while error logging`() {
        assertEquals(0, logsFile.length())
        val errorLabel = "some_text"
        val error = IllegalArgumentException()

        e(errorLabel, error)

        assert(logsFile.length() > 0)
        assertEquals(error.toErrorLogMessage(errorLabel).toByteArray().size.toLong(), logsFile.length())
    }

    @Test
    fun overwriting() {
        val stringToLog = """
            log_string_01
            log_string_02
            log_string_03
            log_string_04
            log_string_05
            log_string_06
            log_string_07
            log_string_08
            log_string_09
            log_string_10
            log_string_11
            log_string_12
            log_string_13
            log_string_14
            log_string_15
            log_string_16
        """.trimIndent()
        val initialLoadSize = stringToLog
            .toLogMessage(FlightRecorder.Priority.I)
            .toByteArray()
            .size

        println("Initial string consist of $initialLoadSize bytes".yellow())
        println()
        FlightRecorder.tapeVolume = initialLoadSize

        i { stringToLog }
        assertEquals(initialLoadSize.toLong(), logsFile.length())
        println("Initial text in file:".yellow())
        println(logsFile.readText())
        println()

        val newLine = "______________________________________________SOME_LARGE_AMOUNT_OF_TEXT____________________________________________"
        i { newLine }
        assertEquals(initialLoadSize.toLong(), logsFile.length())
        println("Text in file after overwriting:".yellow())
        println(logsFile.readText())
    }
}