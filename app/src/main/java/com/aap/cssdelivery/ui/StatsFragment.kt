package com.aap.cssdelivery.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.aap.cssdelivery.R
import com.aap.cssdelivery.databinding.FragmentStatsBinding
import com.aap.cssdelivery.repository.OrderStat
import com.aap.cssdelivery.ui.viewmodel.OrderStatsViewModel


/**
 * Fragment that shows the order statistics in real time.
 */
class StatsFragment : Fragment() {
    lateinit var viewModel: OrderStatsViewModel
    private lateinit var binding: FragmentStatsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(OrderStatsViewModel::class.java)
        initializeText()
        viewModel.getOrderStat().observe(this.viewLifecycleOwner, {
            Log.d("CSS", "Received order stat update")
            handleOrderStatUpdate(it)
        })
//        viewModel.getAverageDeliveryTime().observe(this.viewLifecycleOwner, {
//            handleAverageDeliveryTime(it)
//        })

    }

//    private fun handleAverageDeliveryTime(avg: Long) {
//        val totalSeconds = avg / 1000
//        val minutes = totalSeconds / 60
//        val seconds = totalSeconds % 60
//        binding.average.text = binding.average.context.getString(R.string.average_template, minutes, seconds)
//    }

    private fun initializeText() {
        val placeHolder = binding.delivered.context.getString(R.string.not_available)
        with(binding) {
            delivered.text = placeHolder
            trashed.text = placeHolder
            salesValue.text = placeHolder
            trashedOrderLoss.text = placeHolder
            totalRevenue.text = placeHolder
        }
    }

    private fun handleOrderStatUpdate(orderStat: OrderStat) {
        with(binding) {
            delivered.text = "${orderStat.delivered}"
            trashed.text = "${orderStat.trashed}"
            val context = salesLabel.context
            salesValue.text = getPrice(context, orderStat.profit)
            trashedOrderLoss.text = getPrice(context, orderStat.loss)
            totalRevenue.text = getPrice(context, orderStat.profit - orderStat.loss)
        }
    }
}