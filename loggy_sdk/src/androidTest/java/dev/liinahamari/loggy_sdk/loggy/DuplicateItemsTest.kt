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

package dev.liinahamari.loggy_sdk.loggy

import androidx.fragment.app.commitNow
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import dev.liinahamari.loggy_sdk.Loggy
import dev.liinahamari.loggy_sdk.R
import dev.liinahamari.loggy_sdk.helper.FlightRecorder
import dev.liinahamari.loggy_sdk.loggy.rules.ImmediateSchedulersRule
import dev.liinahamari.loggy_sdk.screens.logs.LogsFragment
import io.github.kakaocup.kakao.screen.Screen.Companion.onScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.random.Random

@RunWith(AndroidJUnit4ClassRunner::class)
class DuplicateItemsTest {
    @Rule
    @JvmField
    val rule = ActivityScenarioRule(MainActivity::class.java)

    @get:Rule
    val immediateSchedulersRule = ImmediateSchedulersRule()

    @Test
    fun dbHas10_000InfoLogItemsWithUniqueTitles_andNoDuplicationShownInUI() {
        val logsAmount = 10000

        rule.scenario.onActivity {
            Loggy.initForTest(it.application)
            FlightRecorder.logBox.removeAll()

            for (i in 0 until logsAmount) { /*LONG operation*/
                FlightRecorder.i { "title $i" }
            }

            it.supportFragmentManager.commitNow {
                replace(R.id.container, LogsFragment.newInstance())
            }
        }

        onScreen<LogsRecyclerScreen> {
            logsRecycler {
                isVisible()
                for (i in 0 until logsAmount) {
                    childAt<LogsRecyclerScreen.InfoLogItem>(i) {
                        Thread.sleep(50)
                        logDescription.hasText("title $i")
                    }
                }
                hasSize(logsAmount)
            }
        }
    }

    @Test
    fun dbHas500MixedInfoAndErrorLogItemsWithUniqueTitles_andNoDuplicationShownInUI() {
        val logsAmount = 500
        val infoLogIndices = mutableListOf<Int>()
        val errorLogIndices = mutableListOf<Int>()

        rule.scenario.onActivity {
            Loggy.initForTest(it.application)
            FlightRecorder.logBox.removeAll()

            for (i in 0 until logsAmount) {
                if (Random.nextBoolean()) {
                    FlightRecorder.i { "title $i" }
                    infoLogIndices.add(i)
                } else {
                    FlightRecorder.e("error $i", IllegalArgumentException())
                    errorLogIndices.add(i)
                }
            }

            it.supportFragmentManager.commitNow {
                replace(R.id.container, LogsFragment.newInstance())
            }
        }

        onScreen<LogsRecyclerScreen> {
            logsRecycler {
                isVisible()
                for (i in infoLogIndices) {
                    childAt<LogsRecyclerScreen.InfoLogItem>(i) {
                        Thread.sleep(50)
                        logDescription.hasText("title $i")
                    }
                }
                for (i in errorLogIndices) {
                    childAt<LogsRecyclerScreen.ErrorLogItem>(i) {
                        Thread.sleep(50)
                        errorLogDescription.containsText("error $i")
                    }
                }
                hasSize(logsAmount)
            }
        }
    }
}