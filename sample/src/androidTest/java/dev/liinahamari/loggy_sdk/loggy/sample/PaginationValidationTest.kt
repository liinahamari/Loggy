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
import dev.liinahamari.loggy_sdk.helper.FlightRecorder
import dev.liinahamari.loggy_sdk.loggy.sample.rules.ImmediateSchedulersRule
import dev.liinahamari.loggy_sdk.loggy.sample.screens.LogsRecyclerScreen
import dev.liinahamari.loggy_sdk.loggy.sample.screens.PresetScreen
import dev.liinahamari.loggy_sdk.loggy.sample.screens.main.MainActivity
import dev.liinahamari.loggy_sdk.loggy.sample.screens.paginateToEnd
import dev.liinahamari.loggy_sdk.screens.logs.log_list.PAGE_CAPACITY
import io.github.kakaocup.kakao.screen.Screen.Companion.onScreen
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val ITERATIONS_AMOUNT = 1000

@RunWith(AndroidJUnit4ClassRunner::class)
class PaginationValidationTest {
    @Rule
    @JvmField
    val rule = ActivityScenarioRule(MainActivity::class.java)

    @Rule
    @JvmField
    val immediateSchedulersRule = ImmediateSchedulersRule()

    @Before
    fun clearDb() = FlightRecorder.clearDb()

    /** some == ITERATIONS_AMOUNT*/
    @Test
    fun whenSomeInfoLogsAdded_thenInDashboardAllTheLogsPresented() {
        onScreen<PresetScreen> {
            amountOfLogsToGenerate.clearText()
            amountOfLogsToGenerate.typeText(ITERATIONS_AMOUNT.toString())
            closeSoftKeyboard()
            createInfoLogBtn.click()
            openDashboardBtn.click()

            onScreen<LogsRecyclerScreen> {
                logsRecycler {
                    isVisible()

                    paginateToEnd(ITERATIONS_AMOUNT / PAGE_CAPACITY.toInt())

                    hasSize(ITERATIONS_AMOUNT)
                }
            }
        }
    }

    /** some == ITERATIONS_AMOUNT*/
    @Test
    fun whenSomeErrorLogsAdded_thenInDashboardAllTheLogsPresented() {
        onScreen<PresetScreen> {
            amountOfLogsToGenerate.clearText()
            amountOfLogsToGenerate.typeText(ITERATIONS_AMOUNT.toString())
            closeSoftKeyboard()
            createErrorLogBtn.click()
            openDashboardBtn.click()

            onScreen<LogsRecyclerScreen> {
                logsRecycler {
                    isVisible()

                    paginateToEnd(ITERATIONS_AMOUNT / PAGE_CAPACITY.toInt())

                    hasSize(ITERATIONS_AMOUNT)
                }
            }
        }
    }

    /** some == ITERATIONS_AMOUNT*/
    @Test
    fun whenSomeMixedLogsAdded_thenInDashboardAllTheLogsPresented() {
        onScreen<PresetScreen> {
            amountOfLogsToGenerate.clearText()
            amountOfLogsToGenerate.typeText(ITERATIONS_AMOUNT.toString())
            closeSoftKeyboard()
            createMixedLogBtn.click()
            openDashboardBtn.click()

            onScreen<LogsRecyclerScreen> {
                logsRecycler {
                    isVisible()

                    paginateToEnd(ITERATIONS_AMOUNT / PAGE_CAPACITY.toInt())

                    hasSize(ITERATIONS_AMOUNT)
                }
            }
        }
    }
}