package com.aap.cssdelivery.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aap.cssdelivery.network.NetworkServiceFactory
import com.aap.cssdelivery.network.RetrofitFactory
import com.aap.cssdelivery.repository.OrderRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class OrderDetailViewModel : ViewModel() {
    fun getOrderFor(id: String) = OrderRepository.getInstance().getOrderForIdAsLiveData(id)

    fun getOrderHistory(id: String) = OrderRepository.getInstance().getOrderHistoryForIdAsLiveData(id)

    fun cancelOrder(address: String, id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val retrofit = RetrofitFactory.getRetrofit(address)
            val networkService = NetworkServiceFactory.getNetworkService(retrofit)
            networkService.cancelOrder(id)
        }

    }
}