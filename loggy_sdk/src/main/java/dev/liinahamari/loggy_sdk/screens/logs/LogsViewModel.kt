/*
Copyright 2021 liinahamari

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files
(the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge,
publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so,
subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR
ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

package dev.liinahamari.loggy_sdk.screens.logs

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.rxjava3.flowable
import dev.liinahamari.loggy_sdk.R
import dev.liinahamari.loggy_sdk.base.BaseViewModel
import dev.liinahamari.loggy_sdk.di.APPLICATION_CONTEXT
import dev.liinahamari.loggy_sdk.helper.BaseComposers
import dev.liinahamari.loggy_sdk.helper.SingleLiveEvent
import dev.liinahamari.loggy_sdk.screens.logs.log_list.LogsPagingSource
import dev.liinahamari.loggy_sdk.screens.logs.log_list.PAGE_CAPACITY
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.kotlin.plusAssign
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject
import javax.inject.Named

class LogsViewModel @Inject constructor(
    @SuppressLint("StaticFieldLeak") @Named(APPLICATION_CONTEXT) val applicationContext: Context,
    private val baseComposers: BaseComposers,
    private val recordInteractor: RecordInteractor,
    private val pagingSource: LogsPagingSource,
    private val createZippedLogFileUseCase: CreateZippedLogFileUseCase,
    private val deleteZippedLogsFileUseCase: DeleteZippedLogsFileUseCase,
    val sharingCredentialsDatasetRepository: SharingCredentialsDatasetRepository
) : BaseViewModel() {
    private val _loadingEvent = SingleLiveEvent<Boolean>()
    val loadingEvent: LiveData<Boolean> get() = _loadingEvent

    private val _logFilePathEvent = SingleLiveEvent<Uri>()
    val logFilePathEvent: LiveData<Uri> get() = _logFilePathEvent

    private val _emptyLogListEvent = SingleLiveEvent<Any>()
    val emptyLogListEvent: LiveData<Any> get() = _emptyLogListEvent

    private val _displayLogsEvent = SingleLiveEvent<List<LogUi>>()
    val displayLogsEvent: LiveData<List<LogUi>> get() = _displayLogsEvent

    @ExperimentalCoroutinesApi
    val logs: Flowable<PagingData<LogUi>> = Pager(
        config = PagingConfig(
            pageSize = PAGE_CAPACITY.toInt(),
            enablePlaceholders = true,
            maxSize = (PAGE_CAPACITY * 5).toInt(),
            prefetchDistance = 1,
            initialLoadSize = PAGE_CAPACITY.toInt()
        ),
        pagingSourceFactory = { pagingSource }
    ).flowable

    fun clearLogs() {
        disposable += recordInteractor.clearEntireRecord().subscribe { it ->
            when (it) {
                is ClearRecordResult.Success -> {
                    _emptyLogListEvent.call()
                    _loadingEvent.value = false
                }
                is ClearRecordResult.Error.IOError -> {
                    _errorEvent.value = R.string.io_error //fixme error message
                    _loadingEvent.value = false
                }
                is ClearRecordResult.InProgress -> _loadingEvent.value = true
            }
        }
    }

    fun createZippedLogsFile() {
        disposable += createZippedLogFileUseCase.execute(applicationContext)
            .compose(baseComposers.applyObservableSchedulers())
            .subscribe {
                when (it) {
                    is CreateZipLogsFileResult.InProgress -> _loadingEvent.value = true
                    is CreateZipLogsFileResult.Success -> {
                        _loadingEvent.value = false
                        _logFilePathEvent.value = it.path
                    }
                    is CreateZipLogsFileResult.IOError -> {
                        _loadingEvent.value = false
                        _errorEvent.value = R.string.io_error
                    }
                }
            }
    }

    fun deleteZippedLogs() {
        disposable += deleteZippedLogsFileUseCase.execute(applicationContext)
            .compose(baseComposers.applyCompletableSchedulers())
            .subscribe()
    }
}