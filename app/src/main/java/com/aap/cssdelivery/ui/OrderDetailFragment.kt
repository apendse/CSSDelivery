package com.aap.cssdelivery.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.VisibleForTesting
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.aap.cssdelivery.R
import com.aap.cssdelivery.dao.Order
import com.aap.cssdelivery.dao.OrderHistoryRow
import com.aap.cssdelivery.databinding.FragmentOrderDetailBinding
import com.aap.cssdelivery.databinding.ViewChangeLogBinding
import com.aap.cssdelivery.ui.viewmodel.OrderDetailViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.sqrt

const val EXTRA_ORDER_ID = "com.aap.cssdelivery.ui.order_id"

class OrderDetailFragment : Fragment() {

    @VisibleForTesting
    lateinit var binding: FragmentOrderDetailBinding
    private lateinit var viewModel: OrderDetailViewModel
    private lateinit var adapter: OrderHistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOrderDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    lateinit var id: String
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        id = arguments?.getString(EXTRA_ORDER_ID) ?: return
        viewModel = ViewModelProvider(this).get(OrderDetailViewModel::class.java)
        viewModel.getOrderFor(id).observe(this.viewLifecycleOwner, {
            populateView(it)
        })
        adapter = OrderHistoryAdapter()
        with(binding.activityHistory) {
            layoutManager = LinearLayoutManager(binding.activityHistory.context)
            adapter = this@OrderDetailFragment.adapter
        }
        viewModel.getOrderHistory(id).observe(this.viewLifecycleOwner, {
            populateHistory(it)
        })

        binding.cancelOrder.setOnClickListener {

            viewModel.cancelOrder(getAddress(), id)
        }



    }

    private fun getAddress(): String {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.activity)
        val address = sharedPreferences.getString("address", DEFAULT_BASE_URL) ?: DEFAULT_BASE_URL
        return "http://$address"
    }

    private fun populateHistory(rows: List<OrderHistoryRow>) {
        adapter.submitList(rows)
    }

    @VisibleForTesting
    fun populateView(order: Order) {
        with(binding) {
            item.text = order.item
            price.text = getPrice(price.context, order.price)
            shelf.text = order.shelf
            state.text = order.state
            customerName.text = order.customer
            customerAddress.text = order.destination
            timestamp.text = createTimeStamp(order.timestamp)
            cancelOrder.isEnabled = !order.isTerminated()
         }
    }
}

fun getPrice(context: Context, price: Int): String {
    return context.getString(R.string.price_template, price / 100.0)
}

fun createTimeStamp(timeStamp: Long): String {
    val formatter = SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss")
    val date = Date(timeStamp)
    return formatter.format(date)
}


class OrderHistoryAdapter(callback: DiffUtil.ItemCallback<OrderHistoryRow> = orderDiffCallbackHistory) :
    ListAdapter<OrderHistoryRow, OrderHistoryAdapter.OrderHistoryViewHolder>(callback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderHistoryViewHolder {
        val binding =
            ViewChangeLogBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderHistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderHistoryViewHolder, position: Int) {
        val item = getItem(position)
        holder.binding.changeMessage.text = getMessage(holder.itemView.context, item)
        holder.binding.timestamp.text = createTimeStamp(item.timestamp)
    }

    private fun getMessage(context: Context, row: OrderHistoryRow): String {
        val change: String
        val old: String
        val newVal: String
        when {
            row.prevState == null -> {
                return context.getString(R.string.order_created_history)
            }
            row.prevState != row.state -> {
                change = context.getString(R.string.state)
                old = row.prevState
                newVal = row.state
            }
            row.prevShelf != row.shelf -> {
                change = context.getString(R.string.shelf)
                old = row.prevShelf ?: ""
                newVal = row.shelf
            }
            else -> {
                return context.getString(R.string.timestamp_changed)
            }
        }
        return context.getString(R.string.changelog_template, change, old, newVal)
    }

    inner class OrderHistoryViewHolder(val binding: ViewChangeLogBinding) :
        RecyclerView.ViewHolder(binding.root)

}

val orderDiffCallbackHistory = object : DiffUtil.ItemCallback<OrderHistoryRow>() {
    override fun areItemsTheSame(oldItem: OrderHistoryRow, newItem: OrderHistoryRow) =
        oldItem.primary == newItem.primary

    override fun areContentsTheSame(oldItem: OrderHistoryRow, newItem: OrderHistoryRow) =
        oldItem == newItem

}