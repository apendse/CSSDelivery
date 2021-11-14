package com.aap.cssdelivery.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aap.cssdelivery.R
import com.aap.cssdelivery.dao.Order
import com.aap.cssdelivery.databinding.FragmentOrderListBinding
import com.aap.cssdelivery.ui.viewmodel.OrderListViewModel

/**
 * A fragment representing a list of Items.
 */
class OrderListFragment : Fragment(), OrderClickListener {

    private lateinit var binding: FragmentOrderListBinding
    lateinit var viewModel: OrderListViewModel
    private lateinit var orderListAdapter: OrderListItemAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOrderListBinding.inflate(layoutInflater, container, false)
        val view = binding.root

        orderListAdapter = OrderListItemAdapter(this)
        with(view) {
            layoutManager = LinearLayoutManager(this.context, RecyclerView.VERTICAL, false)
            adapter = orderListAdapter
            addItemDecoration(DividerItemDecoration(context, RecyclerView.VERTICAL))
        }
        startObserving()
        return view
    }

    private fun startObserving() {
        viewModel = ViewModelProvider(this).get(OrderListViewModel::class.java)
        viewModel.orders.observe(this.viewLifecycleOwner, {
            handleOrderUpdate(it)
        })
    }

    /**
     * Called wen
     */
    private fun handleOrderUpdate(orders: List<Order>) {
        orderListAdapter.submitList(orders)
    }

    override fun onClick(order: Order) {
        val bundle = bundleOf()
        bundle.putString(EXTRA_ORDER_ID, order.id)
        findNavController().navigate(R.id.orderDetailFragment, bundle)
    }
}