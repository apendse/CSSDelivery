package com.aap.cssdelivery.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.aap.cssdelivery.repository.OrderRepository

class OrderStatsViewModel: ViewModel() {
    fun getOrderStat() = OrderRepository.getInstance().orderStatLiveData
    // fun getAverageDeliveryTime() = OrderRepository.getInstance().getAverageOrderLifetimeAsLiveData()
}