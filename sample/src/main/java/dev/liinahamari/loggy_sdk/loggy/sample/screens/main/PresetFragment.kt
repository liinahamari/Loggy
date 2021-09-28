package dev.liinahamari.loggy_sdk.loggy.sample.screens.main

import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import com.jakewharton.rxbinding4.view.clicks
import dev.liinahamari.loggy_sdk.base.BaseFragment
import dev.liinahamari.loggy_sdk.helper.FlightRecorder
import dev.liinahamari.loggy_sdk.helper.throttleFirst
import dev.liinahamari.loggy_sdk.loggy.R
import dev.liinahamari.loggy_sdk.screens.logs.LogsFragment
import io.reactivex.rxjava3.kotlin.plusAssign
import kotlinx.android.synthetic.main.fragment_preset.*
import kotlin.random.Random.Default.nextBoolean

class PresetFragment : BaseFragment(R.layout.fragment_preset) {
    companion object {
        fun newInstance() = PresetFragment()
    }

    private val viewModel: MainViewModel by viewModels()

    override fun setupClicks() {
        subscriptions += createMixedLogs.clicks()
            .subscribe {
                val amount = amountOfLogsToGenerateEt.text.toString().toInt()
                for (i in 1..amount) {
                    if (nextBoolean())
                        FlightRecorder.e("errorLog $i", IllegalArgumentException())
                    else
                        FlightRecorder.i { "infoLog $i" }
                }
            }
        subscriptions += createErrorLogs.clicks()
            .subscribe {
                val amount = amountOfLogsToGenerateEt.text.toString().toInt()
                for (i in 1..amount) {
                    FlightRecorder.e("errorLog $i", IllegalArgumentException(), false)
                }
            }

        subscriptions += createInfoLogs.clicks()
            .subscribe {
                val amount = amountOfLogsToGenerateEt.text.toString().toInt()
                for (i in 1..amount) {
                    FlightRecorder.i { "infoLog $i" }
                }
            }

        subscriptions += openDashboardBtn.clicks()
            .throttleFirst()
            .subscribe {
                requireActivity().supportFragmentManager.commit {
                    addToBackStack(this.javaClass.simpleName.toString())
                    add(R.id.container, LogsFragment.newInstance())
                }
            }
    }
}