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
@file:Suppress("CAST_NEVER_SUCCEEDS")

package dev.liinahamari.loggy_sdk.screens.logs

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.isItemChecked
import com.afollestad.materialdialogs.list.listItemsMultiChoice
import com.jakewharton.rxbinding4.view.clicks
import dev.liinahamari.loggy_sdk.R
import dev.liinahamari.loggy_sdk.base.BaseFragment
import dev.liinahamari.loggy_sdk.helper.CustomToast.errorToast
import dev.liinahamari.loggy_sdk.helper.FlightRecorder
import dev.liinahamari.loggy_sdk.helper.throttleFirst
import dev.liinahamari.loggy_sdk.screens.logs.log_list.LogsAdapter
import io.reactivex.rxjava3.kotlin.addTo
import jp.wasabeef.recyclerview.animators.FadeInAnimator
import kotlinx.android.synthetic.main.fragment_logs.*
import kotlinx.coroutines.ExperimentalCoroutinesApi

private const val FILE_SENDING_REQUEST_CODE = 1011010
private const val MIME_TYPE_ANY = "*/*"

class LogsFragment : BaseFragment(R.layout.fragment_logs) {
    private val logsFilters = mutableListOf<Int>()
    private var isFabMenuOpened = false

    companion object {
        fun newInstance() = LogsFragment()
    }

    private val loadingDialog by lazy {
        Dialog(requireActivity(), R.style.DialogNoPaddingNoTitle).apply {
            setContentView(R.layout.dialog_saving)
            setCancelable(false)
            setCanceledOnTouchOutside(false)
        }
    }

    private val viewModel by viewModels<LogsViewModel> { viewModelFactory }
    private val logsAdapter: LogsAdapter by lazy { LogsAdapter() }

    @ExperimentalCoroutinesApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        logsRv.apply {
            layoutManager = LinearLayoutManager(requireActivity())
            itemAnimator = FadeInAnimator()
            adapter = logsAdapter
        }

        viewModel.logs.subscribe {
            logsAdapter.submitData(lifecycle, it)
        }?.addTo(subscriptions)
    }

    override fun setupViewModelSubscriptions() {
        super.setupViewModelSubscriptions()

        viewModel.errorEvent.observe(this, { errorToast(it) })

        viewModel.emptyLogListEvent.observe(this, {
            emptyLogsTv.isVisible = true
            logsRv.isVisible = false
        })

        viewModel.loadingEvent.observe(this, { toShow ->
            when {
                toShow && loadingDialog.isShowing.not() -> loadingDialog.show()
                toShow.not() && loadingDialog.isShowing -> loadingDialog.cancel()
            }
        })

        viewModel.displayLogsEvent.observe(this, {
            emptyLogsTv.isVisible = false
            logsRv.isVisible = true
        })

        viewModel.logFilePathEvent.observe(this, {
            Intent(Intent.ACTION_SEND).apply {
                putExtra(Intent.EXTRA_EMAIL, arrayOf(viewModel.sharingCredentialsDatasetRepository.integratorsEmail))
                putExtra(
                    Intent.EXTRA_SUBJECT, String.format(
                        getString(R.string.subject),
                        viewModel.sharingCredentialsDatasetRepository.userId,
                        requireActivity().applicationInfo.name
                    )
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                putExtra(Intent.EXTRA_STREAM, it)
                type = MIME_TYPE_ANY
            }.also {
                startActivity(it)
            }
        })
    }

    override fun onStop() {
        super.onStop()
        viewModel.deleteZippedLogs() //todo: causes blocking await. move to service
    }

    override fun setupClicks() {
        clearLogsFab
            ?.clicks()
            ?.throttleFirst()
            ?.subscribe {
                fabMenu.isVisible = false
                viewModel.clearLogs()
            }?.addTo(subscriptions)

        filterLogsFab
            ?.clicks()
            ?.throttleFirst()
            ?.subscribe {
                fabMenu.isVisible = false

                MaterialDialog(requireContext()).show {
                    listItemsMultiChoice(
                        res = R.array.filter_mode,
                        initialSelection = logsFilters.toIntArray()
                    )

                    positiveButton(android.R.string.ok) {
                        mutableListOf<FlightRecorder.FilterMode>().apply {
                            0.also {
                                if (isItemChecked(it)) {
                                    add(FlightRecorder.FilterMode.SHOW_ERRORS)
                                    logsFilters.add(it)
                                } else {
                                    logsFilters.remove(it)
                                }
                            }

                            1.also {
                                if (isItemChecked(it)) {
                                    add(FlightRecorder.FilterMode.HIDE_LIFECYCLE)
                                    logsFilters.add(it)
                                } else {
                                    logsFilters.remove(it)
                                }
                            }

                            2.also {
                                if (isItemChecked(it)) {
                                    add(FlightRecorder.FilterMode.SHOW_NON_MAIN_THREAD)
                                    logsFilters.add(it)
                                } else {
                                    logsFilters.add(it)
                                }
                            }
                        }.also {
//                            viewModel.sortLogs(it)
                        }
                    }
                    negativeButton(android.R.string.cancel) {}
                }
            }?.addTo(subscriptions)

        sendLogsToDeveloperFab
            ?.clicks()
            ?.throttleFirst()
            ?.subscribe {
                fabMenu.isVisible = false
                viewModel.createZippedLogsFile()
            }?.addTo(subscriptions)

        mainFab
            ?.clicks()
            ?.throttleFirst()
            ?.subscribe {
                fabMenu.isVisible = false
                if (isFabMenuOpened.not()) {
                    showFabMenu()
                } else {
                    closeFabMenu()
                }
            }?.addTo(subscriptions)
    }

    private fun showFabMenu() {
        isFabMenuOpened = true
        fabMenu.isVisible = true
    }

    private fun closeFabMenu() {
        isFabMenuOpened = false
        fabMenu.isVisible = false
    }
}