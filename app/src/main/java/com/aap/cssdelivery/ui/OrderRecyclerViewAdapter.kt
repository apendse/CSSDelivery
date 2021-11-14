package com.aap.cssdelivery.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.aap.cssdelivery.R
import com.aap.cssdelivery.dao.Order
import com.aap.cssdelivery.databinding.ViewOrderRowBinding
import java.text.SimpleDateFormat
import java.util.*

interface OrderClickListener {
    fun onClick(order: Order)
}
interface ListItemClickListener {
    fun onClick(pos: Int)
}

/**
 * The [RecyclerView.Adapter] for showing the orders
 */
class OrderListItemAdapter(private val orderClickListener: OrderClickListener, callback: DiffUtil.ItemCallback<Order> = orderDiffCallback) :
    ListAdapter<Order, OrderListItemAdapter.OrderViewHolder>(callback), ListItemClickListener {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        return OrderViewHolder(this,
            ViewOrderRowBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = getItem(position) ?: return
        with(holder.binding) {
            customer.text = customer.context.getString(R.string.customer_prefix, order.customer)
            item.text = order.item
            shelf.text = order.shelf
            state.text = order.state
            timestamp.text = getTimestampString(holder.itemView.context, order.timestamp)
        }
    }

    private fun getTimestampString(context: Context, timeStamp: Long): String {
            val formatter = SimpleDateFormat("HH:mm:ss")
            val date = Date(timeStamp)
            return context.getString(R.string.last_updated_prefix) + formatter.format(date)
    }

    override fun onClick(pos: Int) {
        val item = getItem(pos) ?: return
        orderClickListener.onClick(item)
    }

    inner class OrderViewHolder(listItemClickListener: ListItemClickListener, val binding: ViewOrderRowBinding): RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                listItemClickListener.onClick(bindingAdapterPosition)
            }
        }
    }
}

val orderDiffCallback = object: DiffUtil.ItemCallback<Order>() {
    override fun areItemsTheSame(oldItem: Order, newItem: Order) = oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: Order, newItem: Order) = oldItem == newItem
}

