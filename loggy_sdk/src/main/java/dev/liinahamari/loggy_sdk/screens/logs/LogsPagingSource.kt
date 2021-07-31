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

package dev.liinahamari.loggy_sdk.screens.logs

import androidx.paging.PagingState
import androidx.paging.rxjava3.RxPagingSource
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject

class LogsPagingSource @Inject constructor(private val loggerInteractor: LoggerInteractor) : RxPagingSource<Int, LogUi>() {
    override fun loadSingle(params: LoadParams<Int>): Single<LoadResult<Int, LogUi>> = loggerInteractor.getEntireRecord(params.key ?: 0)
        .doOnError { it.printStackTrace() }
        .map {
            if (it is GetRecordResult.Success) {
                toLoadResult(it.logs, params.key ?: 0)
            } else {
                LoadResult.Error(IllegalStateException())
            }
        }
        .onErrorReturn { LoadResult.Error(it) }

    private fun toLoadResult(data: List<LogUi>, lastIndex: Int): LoadResult<Int, LogUi> = LoadResult.Page(
        data = data,
        prevKey = if (lastIndex == 0) null else lastIndex - 1,
        nextKey = if (data.size < PAGE_CAPACITY) null else lastIndex + 1
    )

    override fun getRefreshKey(state: PagingState<Int, LogUi>): Int? {
        val anchorPosition = state.anchorPosition ?: return null
        val page = state.closestPageToPosition(anchorPosition) ?: return null
        return page.prevKey?.plus(1) ?: page.nextKey?.minus(1)
    }
}