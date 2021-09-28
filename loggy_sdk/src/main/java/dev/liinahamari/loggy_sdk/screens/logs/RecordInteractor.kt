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

import dev.liinahamari.loggy_sdk.db.Log
import dev.liinahamari.loggy_sdk.db.LogToLogUiMapper
import dev.liinahamari.loggy_sdk.db.Log_
import dev.liinahamari.loggy_sdk.helper.BaseComposers
import dev.liinahamari.loggy_sdk.helper.FlightRecorder
import dev.liinahamari.loggy_sdk.screens.logs.log_list.PAGE_CAPACITY
import io.objectbox.Box
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject

class RecordInteractor @Inject constructor(private val logMapper: LogToLogUiMapper, private val baseComposers: BaseComposers, private val logBox: Box<Log>) {
    fun getPagedRecord(page: Int): Single<GetRecordResult> = Single.fromCallable {
        logBox.query()
            .order(Log_.timestamp)
            .build()
            .find(if (page == 0) 0 else page * PAGE_CAPACITY, PAGE_CAPACITY)
    }
        .map { it.map(logMapper::transform) }
        .map { if (it.isNotEmpty()) GetRecordResult.Success(it) else GetRecordResult.EmptyList }
        .onErrorReturn { GetRecordResult.Error.IOError }
        .compose(baseComposers.applySingleSchedulers())

    fun getFilteredRecord(page: Int = 0, filterStates: List<FilterState>): Single<GetRecordResult> = Single.fromCallable {
        logBox.query()
            .apply {
                if (filterStates.any { it == FilterState.NON_MAIN_THREAD }) notEqual(Log_.thread, "main")
                if (filterStates.any { it == FilterState.NOT_LIFECYCLE_EVENT }) notEqual(Log_.priority, FlightRecorder.Priority.L.ordinal.toLong())
                if (filterStates.any { it == FilterState.SHOW_ONLY_ERRORS }) equal(Log_.priority, FlightRecorder.Priority.E.ordinal.toLong())
            }
            .order(Log_.timestamp)
            .build()
            .find(if (page == 1) 0 else page * PAGE_CAPACITY, PAGE_CAPACITY)

    }
        .map { it.map(logMapper::transform) }
        .map { if (it.isNotEmpty()) GetRecordResult.Success(it) else GetRecordResult.EmptyList }
        .onErrorReturn {
            it.printStackTrace()
            GetRecordResult.Error.IOError
        }
        .compose(baseComposers.applySingleSchedulers())

    fun clearEntireRecord(): Observable<ClearRecordResult> = Observable.fromCallable {
        logBox.removeAll()
    }
        .map<ClearRecordResult> { ClearRecordResult.Success }
        .onErrorReturn { ClearRecordResult.Error.IOError }
        .startWithItem(ClearRecordResult.InProgress)
        .compose(baseComposers.applyObservableSchedulers())
}

sealed class GetRecordResult {
    data class Success(val logs: List<LogUi>) : GetRecordResult()
    object EmptyList : GetRecordResult()
    sealed class Error : GetRecordResult() {
        object IOError : Error()
    }
}

sealed class ClearRecordResult {
    object Success : ClearRecordResult()
    object InProgress : ClearRecordResult()
    sealed class Error : ClearRecordResult() {
        object IOError : Error()
    }
}