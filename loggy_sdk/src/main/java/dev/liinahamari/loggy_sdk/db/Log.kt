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

package dev.liinahamari.loggy_sdk.db

import dev.liinahamari.loggy_sdk.helper.FlightRecorder
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

@Entity
data class Log(
    @Id var id: Long = 0L, //todo how it works?
    var timestamp: Long,
    var title: String? = null,
    var priority: Int,
    var body: String,
    var thread: String
) {
    companion object {
        fun testILog(): Log = Log(timestamp = 1L, priority = FlightRecorder.Priority.I.ordinal, body = "", thread = "")
        fun testLLog(): Log = Log(timestamp = 1L, priority = FlightRecorder.Priority.L.ordinal, body = "", thread = "")
        fun testELog(): Log = Log(timestamp = 1L, priority = FlightRecorder.Priority.E.ordinal, body = "", thread = "")
        fun testWLog(): Log = Log(timestamp = 1L, priority = FlightRecorder.Priority.W.ordinal, body = "", thread = "")
        fun testDLog(): Log = Log(timestamp = 1L, priority = FlightRecorder.Priority.D.ordinal, body = "", thread = "")
    }
}
