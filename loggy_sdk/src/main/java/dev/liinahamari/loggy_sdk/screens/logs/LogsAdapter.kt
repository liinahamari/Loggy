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

import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.doOnLayout
import androidx.databinding.DataBindingUtil
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxbinding4.view.clicks
import dev.liinahamari.loggy_sdk.R
import dev.liinahamari.loggy_sdk.databinding.ItemErrorLogBinding
import dev.liinahamari.loggy_sdk.databinding.ItemInfoLogBinding
import dev.liinahamari.loggy_sdk.databinding.ItemPlaceholderBinding
import dev.liinahamari.loggy_sdk.helper.throttleFirst
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.plusAssign
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_error_log.*
import java.lang.IllegalStateException

class LogsAdapter : PagingDataAdapter<LogUi, RecyclerView.ViewHolder>(COMPARATOR) {
    private val clicks = CompositeDisposable()
    private lateinit var expandedMarkers: SparseBooleanArray
    private var errorItemHeight = -1

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) = clicks.clear()

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        is LogUi.InfoLog -> R.layout.item_info_log
        is LogUi.ErrorLog -> R.layout.item_error_log
        else -> R.layout.item_placeholder //paging library returns nulls when no data ready at the moment
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder = when (viewType) {
        R.layout.item_info_log -> InfoLogViewHolder(ItemInfoLogBinding.inflate(LayoutInflater.from(parent.context), parent, false).root)
        R.layout.item_error_log -> ErrorLogViewHolder(ItemErrorLogBinding.inflate(LayoutInflater.from(parent.context), parent, false).root)
        R.layout.item_placeholder -> PlaceholderViewHolder(ItemPlaceholderBinding.inflate(LayoutInflater.from(parent.context), parent, false).root)
        else -> throw IllegalStateException()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ErrorLogViewHolder -> holder.bind(position)
            is InfoLogViewHolder -> holder.binding?.infoLog = getItem(position) as LogUi.InfoLog
            else -> Unit
        }
    }

    private inner class ErrorLogViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer {
        private val binding: ItemErrorLogBinding? = DataBindingUtil.bind(containerView)

        fun bind(position: Int) {
            binding?.errorLog = getItem(position) as LogUi.ErrorLog

            itemView.doOnLayout {
                if (errorItemHeight == -1) {
                    errorItemHeight = (itemView.height - itemView.context.resources.getDimensionPixelSize(
                        R.dimen.arrow_button_height)) / 2
                    arrowBtn.layoutParams = (arrowBtn.layoutParams as ConstraintLayout.LayoutParams).apply {
                        setMargins(0, errorItemHeight, 0, 0)
                    }
                }
            }

            if (errorItemHeight != -1) {
                /** expedient duplicated code */
                arrowBtn.layoutParams = (arrowBtn.layoutParams as ConstraintLayout.LayoutParams).apply {
                    setMargins(0, errorItemHeight, 0, 0)
                }
            }

            with(expandedMarkers[position]) {
                arrowBtn.rotation = if (this) 180f else 0f
                expandableLayout.setExpanded(this, false)
            }

            clicks += itemView.clicks()
                .throttleFirst()
                .map { expandableLayout.isExpanded.not() }
                .subscribe {
                    expandedMarkers.put(position, it)
                    notifyItemChanged(position)
                }
        }
    }

    private inner class InfoLogViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer {
        val binding: ItemInfoLogBinding? = DataBindingUtil.bind(containerView)
    }

    private inner class PlaceholderViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer {
        val binding: ItemInfoLogBinding? = DataBindingUtil.bind(containerView)
    }

     companion object {
         private val COMPARATOR = object : DiffUtil.ItemCallback<LogUi>() {
             override fun areItemsTheSame(oldItem: LogUi, newItem: LogUi) = oldItem.time == newItem.time
             override fun areContentsTheSame(oldItem: LogUi, newItem: LogUi) = oldItem == newItem
         }
     }
}