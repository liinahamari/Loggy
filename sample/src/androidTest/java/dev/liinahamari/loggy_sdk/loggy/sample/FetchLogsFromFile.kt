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

import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import dev.liinahamari.loggy_sdk.loggy.sample.Setup.dummyErrorLog
import dev.liinahamari.loggy_sdk.loggy.sample.Setup.dummyInfoLog
import dev.liinahamari.loggy_sdk.loggy.sample.Setup.setupLoggerInteractorInjection
import dev.liinahamari.loggy_sdk.loggy.sample.screens.main.MainActivity
import dev.liinahamari.loggy_sdk.screens.logs.GetRecordResult
import dev.liinahamari.loggy_sdk.screens.logs.LogUi
import io.github.kakaocup.kakao.screen.Screen.Companion.onScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.random.Random

@RunWith(AndroidJUnit4ClassRunner::class)
class FetchLogsFromFile {
    @Rule
    @JvmField
    val rule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun whenNoLogsInFileThenLogsRecyclerIsGoneAndPlaceholderIsVisible() {
        setupLoggerInteractorInjection(GetRecordResult.EmptyList, rule)
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
        setupLoggerInteractorInjection(
            GetRecordResult.Success(listOf(dummyErrorLog)),
            rule
        )
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
                        hasText(DUMMY_DESCRIPTION)
                    }
                    stackTrace {
                        isVisible()
                        hasText(DUMMY_STACKTRACE)
                    }
                    expandButton { isVisible() }
                }
            }
        }
    }

    @Test
    fun whenOneInfoLogInFileThenLogsRecyclerIsVisibleAndHasOneItemWithTypeOfInfoLog() {
        val logs = listOf(dummyInfoLog)
        setupLoggerInteractorInjection(GetRecordResult.Success(logs), rule)
        onScreen<LogsRecyclerScreen> {
            emptyLogsPlaceholder {
                isGone()
            }
            logsRecycler {
                isVisible()
                hasSize(logs.size)
                firstChild<LogsRecyclerScreen.InfoLogItem> {
                    isVisible()
                    logDescription {
                        isVisible()
                        containsText(DUMMY_DESCRIPTION)
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
            errorLogs.add(dummyErrorLog)
        }
        setupLoggerInteractorInjection(GetRecordResult.Success(errorLogs), rule)

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
                            hasText(DUMMY_DESCRIPTION)
                        }
                    }
                }
            }
        }
    }

    @Test
    fun checkParticularValuesAppliedOnErrorLogDisplaying() {
        val logs = listOf(dummyErrorLog)
        setupLoggerInteractorInjection(GetRecordResult.Success(logs), rule)

        onScreen<LogsRecyclerScreen> {
            emptyLogsPlaceholder {
                isGone()
            }
            logsRecycler {
                isVisible()
                hasSize(logs.size)
                childAt<LogsRecyclerScreen.ErrorLogItem>(0) {
                    isVisible()
                    errorLogDescription {
                        isVisible()
                        hasText(DUMMY_DESCRIPTION)
                    }
                    stackTrace {
                        isVisible()
                        hasText(DUMMY_STACKTRACE)
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
        val logs = listOf(dummyInfoLog)
        setupLoggerInteractorInjection(GetRecordResult.Success(logs), rule)

        onScreen<LogsRecyclerScreen> {
            emptyLogsPlaceholder {
                isGone()
            }
            logsRecycler {
                isVisible()
                hasSize(logs.size)
                childAt<LogsRecyclerScreen.InfoLogItem>(0) {
                    isVisible()
                    logDescription {
                        isVisible()
                        hasText(DUMMY_DESCRIPTION)
                    }
                }
            }
        }
    }
}