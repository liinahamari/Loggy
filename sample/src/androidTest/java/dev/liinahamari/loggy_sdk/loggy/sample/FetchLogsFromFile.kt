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
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import dagger.Module
import dagger.Provides
import dev.liinahamari.loggy_sdk.Loggy
import dev.liinahamari.loggy_sdk.di.APPLICATION_CONTEXT
import dev.liinahamari.loggy_sdk.di.DaggerLoggyComponent
import dev.liinahamari.loggy_sdk.di.LoggyModule
import dev.liinahamari.loggy_sdk.helper.BaseComposers
import dev.liinahamari.loggy_sdk.helper.FlightRecorder
import dev.liinahamari.loggy_sdk.loggy.R
import dev.liinahamari.loggy_sdk.loggy.sample.screens.main.MainActivity
import dev.liinahamari.loggy_sdk.screens.logs.GetRecordResult
import dev.liinahamari.loggy_sdk.screens.logs.LogUi
import dev.liinahamari.loggy_sdk.screens.logs.LoggerInteractor
import dev.liinahamari.loggy_sdk.screens.logs.LogsFragment
import io.github.kakaocup.kakao.image.KImageView
import io.github.kakaocup.kakao.recycler.KRecyclerItem
import io.github.kakaocup.kakao.recycler.KRecyclerView
import io.github.kakaocup.kakao.screen.Screen
import io.github.kakaocup.kakao.screen.Screen.Companion.onScreen
import io.github.kakaocup.kakao.text.KTextView
import io.mockk.every
import io.mockk.mockk
import io.reactivex.rxjava3.core.Observable
import org.hamcrest.Matcher
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import javax.inject.Named
import javax.inject.Singleton
import kotlin.random.Random

private const val DUMMY = "dummy"

@RunWith(AndroidJUnit4ClassRunner::class)
class FetchLogsFromFile {
    @Rule
    @JvmField
    val rule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun whenNoLogsInFileThenLogsRecyclerIsGoneAndPlaceholderIsVisible() {
        setupLoggerInteractorInjection(GetRecordResult.EmptyList)
        onScreen<LogsRecyclerScreen> {
            logsRecycler {
                isGone()
            }
            emptyLogsPlaceholder {
                isVisible()
            }
        }
    }

    @Test
    fun whenOneErrorLogInFileThenLogsRecyclerIsVisibleAndHasOneItemWithTypeOfErrorLog() {
        val dummyStackTrace = "dummy_stacktrace"
        setupLoggerInteractorInjection(GetRecordResult.Success(listOf(LogUi.ErrorLog(DUMMY, dummyStackTrace, 1L, DUMMY))))
        onScreen<LogsRecyclerScreen> {
            emptyLogsPlaceholder {
                isGone()
            }
            logsRecycler {
                isVisible()
                hasSize(1)
                firstChild<LogsRecyclerScreen.ErrorLogItem> {
                    isVisible()
                    errorLogDescription {
                        isVisible()
                        hasText(DUMMY)
                    }
                    stackTrace {
                        isVisible()
                        hasText(dummyStackTrace)
                    }
                    expandButton { isVisible() }
                }
            }
        }
    }

    @Test
    fun whenOneInfoLogInFileThenLogsRecyclerIsVisibleAndHasOneItemWithTypeOfInfoLog() {
        val logPriority = FlightRecorder.Priority.D
        setupLoggerInteractorInjection(GetRecordResult.Success(listOf(LogUi.InfoLog(DUMMY, 1L, DUMMY, logPriority))))
        onScreen<LogsRecyclerScreen> {
            emptyLogsPlaceholder {
                isGone()
            }
            logsRecycler {
                isVisible()
                hasSize(1)
                firstChild<LogsRecyclerScreen.InfoLogItem> {
                    isVisible()
                    logDescription {
                        isVisible()
                        containsText(DUMMY)
                    }
                }
            }
        }
    }

    @Test
    fun when_n_ErrorLogsInFileThenRecyclerShouldShow_n_Entries() {
        val errorLogs = mutableListOf<LogUi.ErrorLog>()
        val randomAmount = Random.nextInt(50)
        for (i in 0 until randomAmount) {
            errorLogs.add(LogUi.ErrorLog(DUMMY, DUMMY, 1L, DUMMY))
        }
        setupLoggerInteractorInjection(GetRecordResult.Success(errorLogs))

        onScreen<LogsRecyclerScreen> {
            emptyLogsPlaceholder {
                isGone()
            }
            logsRecycler {
                isVisible()
                hasSize(randomAmount)
                for (i in 0 until randomAmount) {
                    scrollTo(i)
                    childAt<LogsRecyclerScreen.ErrorLogItem>(i) {
                        isVisible()
                        errorLogDescription {
                            isVisible()
                            hasText(DUMMY)
                        }
                    }
                }
            }
        }
    }

    @Test
    fun checkParticularValuesAppliedOnErrorLogDisplaying() {
        val stackTrace = "dummy_stacktrace"
        val label = "dummy_label"
        val threadLabel = "dummy_thread_label"

        val errorLogs = listOf(
            LogUi.ErrorLog(
                label = label,
                stacktrace = stackTrace,
                time = 1L,
                thread = threadLabel
            )
        )

        setupLoggerInteractorInjection(GetRecordResult.Success(errorLogs))

        onScreen<LogsRecyclerScreen> {
            emptyLogsPlaceholder {
                isGone()
            }
            logsRecycler {
                isVisible()
                hasSize(errorLogs.size)
                childAt<LogsRecyclerScreen.ErrorLogItem>(0) {
                    isVisible()
                    errorLogDescription {
                        isVisible()
                        hasText(label)
                    }
                    stackTrace {
                        isVisible()
                        hasText(stackTrace)
                    }
                    expandButton {
                        isVisible()
                    }
                }
            }
        }
    }

    @Test
    fun checkParticularValuesAppliedOnInfoLogDisplaying() {
        val logBody = "dummy_log"
        val threadLabel = "dummy_thread_label"

        val infoLogs = listOf(
            LogUi.InfoLog(
                message = logBody,
                time = 1L,
                thread = threadLabel,
                priority = FlightRecorder.Priority.D
            )
        )

        setupLoggerInteractorInjection(GetRecordResult.Success(infoLogs))

        onScreen<LogsRecyclerScreen> {
            emptyLogsPlaceholder {
                isGone()
            }
            logsRecycler {
                isVisible()
                hasSize(infoLogs.size)
                childAt<LogsRecyclerScreen.InfoLogItem>(0) {
                    isVisible()
                    logDescription {
                        isVisible()
                        hasText(logBody)
                    }
                }
            }
        }
    }

    private fun setupLoggerInteractorInjection(result: GetRecordResult) {
        mockk<Loggy> {
            every { loggyComponent } returns DaggerLoggyComponent.builder()
                .application(getApplicationContext<Application>())
                .email(DUMMY)
                .userId(DUMMY)
                .logFile(File.createTempFile(DUMMY, ".tmp"))
                .loggyModule(MockLoggyModule(getApplicationContext<Application>(), result))
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
class MockLoggyModule(private val context: Context, private val result: GetRecordResult) : LoggyModule() {
    @Provides
    @Singleton
    override fun provideLoggerInteractor(@Named(APPLICATION_CONTEXT) appContext: Context, composers: BaseComposers, logFile: File): LoggerInteractor =
        mockk {
            every { getEntireRecord() } returns Observable.just(result)
        }
}

open class LogsRecyclerScreen : Screen<LogsRecyclerScreen>() {
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
