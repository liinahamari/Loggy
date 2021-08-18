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

package dev.liinahamari.loggy_sdk.loggy.sample

import android.content.Context
import android.view.View
import androidx.fragment.app.commitNow
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.rules.ActivityScenarioRule
import dagger.Module
import dagger.Provides
import dev.liinahamari.loggy_sdk.Loggy
import dev.liinahamari.loggy_sdk.di.APPLICATION_CONTEXT
import dev.liinahamari.loggy_sdk.di.DaggerLoggyComponent
import dev.liinahamari.loggy_sdk.di.MainModule
import dev.liinahamari.loggy_sdk.helper.BaseComposers
import dev.liinahamari.loggy_sdk.helper.FlightRecorder
import dev.liinahamari.loggy_sdk.loggy.R
import dev.liinahamari.loggy_sdk.loggy.sample.screens.main.MainActivity
import dev.liinahamari.loggy_sdk.screens.logs.*
import io.github.kakaocup.kakao.common.views.KView
import io.github.kakaocup.kakao.image.KImageView
import io.github.kakaocup.kakao.recycler.KRecyclerItem
import io.github.kakaocup.kakao.recycler.KRecyclerView
import io.github.kakaocup.kakao.screen.Screen
import io.github.kakaocup.kakao.text.KTextView
import io.mockk.every
import io.mockk.mockk
import io.reactivex.rxjava3.core.Observable
import org.hamcrest.Matcher
import java.io.File
import javax.inject.Named
import javax.inject.Singleton

const val DUMMY_DESCRIPTION = "dummy_desc"
const val DUMMY_THREAD_LABEL = "dummy_thread"
const val DUMMY_STACKTRACE = "dummy_stacktrace\n\tdummy_line"

object Setup {
    val dummyErrorLog = LogUi.ErrorLog(DUMMY_DESCRIPTION, DUMMY_STACKTRACE, 1L, DUMMY_THREAD_LABEL)
    val dummyInfoLog = LogUi.InfoLog(DUMMY_DESCRIPTION, 1L, DUMMY_THREAD_LABEL, FlightRecorder.Priority.D)

    fun setupLoggerInteractorInjection(result: GetRecordResult, rule: ActivityScenarioRule<MainActivity>) {
        mockk<Loggy> {
            every { loggyComponent } returns DaggerLoggyComponent.builder()
                .application(ApplicationProvider.getApplicationContext<Application>())
                .email(DUMMY_DESCRIPTION)
                .userId(DUMMY_DESCRIPTION)
                .logFile(File.createTempFile(DUMMY_DESCRIPTION, ".tmp"))
                .loggyModule(MockLoggyModule(ApplicationProvider.getApplicationContext<Application>(), result))
                .build()
        }
        rule.scenario.onActivity {
            it.supportFragmentManager.commitNow {
                replace(R.id.container, LogsFragment.newInstance())
            }
        }
    }
}

@Module
class MockLoggyModule(private val context: Context, private val result: GetRecordResult) : MainModule() {
    @Provides
    @Singleton
    override fun provideLoggerInteractor(@Named(APPLICATION_CONTEXT) appContext: Context, composers: BaseComposers, logFile: File): LoggerInteractor =
        mockk {
            every { getEntireRecord() } returns Observable.just(result)
            every { clearEntireRecord() } returns Observable.just(ClearRecordResult.Success)
        }
}

open class LogsRecyclerScreen : Screen<LogsRecyclerScreen>() {
    val mainFab: KView = KView{ withId(R.id.mainFab)}
    val clearLogsFab: KView = KView{ withId(R.id.clearLogsFab)}

    val logsRecycler: KRecyclerView = KRecyclerView({
        withId(R.id.logsRv)
    }, itemTypeBuilder = {
        itemType(LogsRecyclerScreen::InfoLogItem)
        itemType(LogsRecyclerScreen::ErrorLogItem)
    })

    val emptyLogsPlaceholder: KTextView = KTextView { withId(R.id.emptyLogsTv) }

    class InfoLogItem(parent: Matcher<View>) : KRecyclerItem<InfoLogItem>(parent) {
        val logDescription: KTextView = KTextView(parent) { withId(R.id.logInfoTv) }
    }

    class ErrorLogItem(parent: Matcher<View>) : KRecyclerItem<ErrorLogItem>(parent) {
        val errorLogDescription: KTextView = KTextView(parent) { withId(R.id.logErrorTv) }
        val expandButton: KImageView = KImageView(parent) { withId(R.id.arrowBtn) }
        val stackTrace: KTextView = KTextView(parent) { withId(R.id.stacktraceTv) }
    }
}
