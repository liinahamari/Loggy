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
