package com.aap.cssdelivery.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.aap.cssdelivery.dao.Order
import com.aap.cssdelivery.repository.OrderRepository

class OrderListViewModel: ViewModel() {

    val orders: LiveData<List<Order>>
        get() =  OrderRepository.getInstance().getOrders()

}