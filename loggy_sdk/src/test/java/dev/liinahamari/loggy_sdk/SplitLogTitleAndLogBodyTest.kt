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

package dev.liinahamari.loggy_sdk

import android.os.Build
import dev.liinahamari.loggy_sdk.helper.FlightRecorder
import dev.liinahamari.loggy_sdk.helper.MESSAGE_LENGTH_THRESHOLD
import dev.liinahamari.loggy_sdk.helper.yellow
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Config(sdk = [Build.VERSION_CODES.O_MR1])
class SplitLogTitleAndLogBodyTest {
    @Test(expected = IllegalArgumentException::class)
    fun `when empty message transmitted to splitLogTitleAndLogBody func then IllegalArgumentException thrown`() {
        FlightRecorder.splitLogTitleAndLogBody("")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `when blank message transmitted to splitLogTitleAndLogBody func then IllegalArgumentException thrown`() {
        FlightRecorder.splitLogTitleAndLogBody("    ")
    }

    @Test
    fun `when SHORT message transmitted to splitLogTitleAndLogBody then func returns null title and body equal to original message`() {
        val logMessage = (0 until (MESSAGE_LENGTH_THRESHOLD * 2) - 1)
            .map { ('a'..'z').random() }
            .joinToString("")
            .also { println("Generated string with length ${it.length}:\n${it.yellow()}") }

        with(FlightRecorder.splitLogTitleAndLogBody(logMessage)) {
            assert(first == null)
            assert(second == logMessage)
        }
    }

    @Test
    fun `when LONG message transmitted to splitLogTitleAndLogBody then func returns non-null title and body equal to original message minus title`() {
        val logMessage = (0 until MESSAGE_LENGTH_THRESHOLD * 2)
            .map { ('a'..'z').random() }
            .joinToString("")
            .also { println("Generated string with length ${it.length}:\n${it.yellow()}") }

        with(FlightRecorder.splitLogTitleAndLogBody(logMessage)) {
            assert(first != null)
            assert(first!!.endsWith("..."))
            assert(first!!.length == MESSAGE_LENGTH_THRESHOLD + "...".length)
            assert(logMessage.contains(first!!.dropLast(3)))
            assert(logMessage.contains(second))
        }
    }
}