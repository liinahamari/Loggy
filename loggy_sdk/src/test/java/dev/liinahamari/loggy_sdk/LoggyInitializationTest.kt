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
import dev.liinahamari.loggy_sdk.helper.TAPE_VOLUME
import dev.liinahamari.loggy_sdk.helper.getFreeSpaceInBytes
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import org.junit.Test
import org.robolectric.annotation.Config

@MockK
@Config(sdk = [26])
class LoggyInitializationTestApi26 {
    @Test(expected = LoggyInitializationException::class)
    fun `when device has is Oreo or higher and has less than expected space exception should be thrown`() {
        mockk<Application>().also { app ->
            every { app.getFreeSpaceInBytes() } returns TAPE_VOLUME - 1L
            Loggy.init(app, createTempFile(), "doesn't_matter", "doesn't_matter")
        }
    }

    @Test
    fun `when device has is lesser than Oreo and has more space than required, no errors should be thrown`() {
        mockk<Application>().also { app ->
            every { app.getFreeSpaceInBytes() } returns TAPE_VOLUME + 1L
            Loggy.init(app, createTempFile(), "doesn't_matter", "doesn't_matter")
        }
    }
}

@MockK
@Config(sdk = [25])
class LoggyInitializationTestApi25 {
    @Test(expected = LoggyInitializationException::class)
    fun `when device has is lesser than Oreo and has less than expected space exception should be thrown`() {
        mockk<Application>().also { app ->
            every { app.getFreeSpaceInBytes() } returns TAPE_VOLUME - 1L
            Loggy.init(app, createTempFile(), "doesn't_matter", "doesn't_matter")
        }
    }

    @Test
    fun `when device has is lesser than Oreo and has more space than required, no errors should be thrown`() {
        mockk<Application>().also { app ->
            every { app.getFreeSpaceInBytes() } returns TAPE_VOLUME + 1L
            Loggy.init(app, createTempFile(), "doesn't_matter", "doesn't_matter")
        }
    }
}
