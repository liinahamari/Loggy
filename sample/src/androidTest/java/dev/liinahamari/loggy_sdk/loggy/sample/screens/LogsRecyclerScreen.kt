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

package dev.liinahamari.loggy_sdk.loggy.sample.screens

import android.view.View
import dev.liinahamari.loggy_sdk.R
import io.github.kakaocup.kakao.common.views.KView
import io.github.kakaocup.kakao.image.KImageView
import io.github.kakaocup.kakao.recycler.KRecyclerItem
import io.github.kakaocup.kakao.recycler.KRecyclerView
import io.github.kakaocup.kakao.screen.Screen
import io.github.kakaocup.kakao.text.KTextView
import org.hamcrest.Matcher

open class LogsRecyclerScreen : Screen<LogsRecyclerScreen>() {
    val mainFab: KView = KView { withId(R.id.mainFab) }
    val clearLogsFab: KView = KView { withId(R.id.clearLogsFab) }

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

fun KRecyclerView.paginateToEnd(pagesAmount: Int) {
    for (i in 0 until pagesAmount) {
        scrollToEnd()
        Thread.sleep(500)
    }
}