package dev.liinahamari.loggy_sdk.loggy.sample.screens.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import dev.liinahamari.loggy_sdk.loggy.R
import dev.liinahamari.loggy_sdk.screens.logs.LogsFragment
import kotlinx.android.synthetic.main.main_fragment.*

class MainFragment : Fragment() {
    companion object {
        fun newInstance() = MainFragment()
    }

    private val viewModel: MainViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.main_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        message.setOnClickListener {
            requireActivity().supportFragmentManager.commit {
                replace(R.id.container, LogsFragment.newInstance())
            }
        }
    }
}