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
import dev.liinahamari.loggy_sdk.loggy.sample.rules.ImmediateSchedulersRule
import dev.liinahamari.loggy_sdk.loggy.sample.screens.LogsRecyclerScreen
import dev.liinahamari.loggy_sdk.loggy.sample.screens.PresetScreen
import dev.liinahamari.loggy_sdk.loggy.sample.screens.main.MainActivity
import io.github.kakaocup.kakao.screen.Screen.Companion.onScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4ClassRunner::class)
class EraseLogsTest {
    @Rule
    @JvmField
    val rule = ActivityScenarioRule(MainActivity::class.java)

    @Rule
    @JvmField
    val immediateSchedulersRule = ImmediateSchedulersRule()

    @Test
    fun recyclerHas0LogsAtAll_afterClearButtonClicked_EmptyPlaceholderIsShown() {
        onScreen<PresetScreen> {
            openDashboardBtn.click()

            onScreen<LogsRecyclerScreen> {
                logsRecycler {
                    isVisible()
                    hasSize(0)
                }
                emptyLogsPlaceholder {
                    isGone()
                }
                mainFab {
                    isVisible()
                    click()
                }
                clearLogsFab {
                    isVisible()
                    isFocusable()
                    click()
                }
                logsRecycler {
                    isGone()
                }
                emptyLogsPlaceholder {
                    isVisible()
                }
            }
        }
    }

    @Test
    fun recyclerHas21ErrorLog_afterClearButtonClicked_RecyclerHasGoneAndEmptyPlaceholderIsShown() {
        onScreen<PresetScreen> {
            create21errorLogBtn.click()
            openDashboardBtn.click()

            onScreen<LogsRecyclerScreen> {
                logsRecycler {
                    isVisible()
                    hasSize(21)
                }
                emptyLogsPlaceholder {
                    isGone()
                }
                mainFab {
                    isVisible()
                    click()
                }
                clearLogsFab {
                    isVisible()
                    isFocusable()
                    click()
                }
                logsRecycler {
                    isGone()
                }
                emptyLogsPlaceholder {
                    isVisible()
                }
            }
        }
    }

    @Test
    fun recyclerHas21MixedLogs_afterClearButtonClicked_RecyclerHasGoneAndEmptyPlaceholderIsShown() {
        onScreen<PresetScreen> {
            create21mixedLogBtn.click()
            openDashboardBtn.click()

            onScreen<LogsRecyclerScreen> {
                logsRecycler {
                    isVisible()
                    hasSize(21)
                }
                emptyLogsPlaceholder {
                    isGone()
                }
                mainFab {
                    isVisible()
                    click()
                }
                clearLogsFab {
                    isVisible()
                    isFocusable()
                    click()
                }
                logsRecycler {
                    isGone()
                }
                emptyLogsPlaceholder {
                    isVisible()
                }
            }
        }
    }
}