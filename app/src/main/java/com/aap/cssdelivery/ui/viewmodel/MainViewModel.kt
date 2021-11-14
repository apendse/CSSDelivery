package com.aap.cssdelivery.ui.viewmodel

import android.util.Log
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aap.cssdelivery.dao.Order
import com.aap.cssdelivery.dao.OrderHistoryRow
import com.aap.cssdelivery.network.*
import com.aap.cssdelivery.repository.OrderRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val DEFAULT_DELAY = 2 * 1000L

/**
 * This is the main workhorse that brings the data from the server into the app and calls the appropriate repo methods
 */
class MainViewModel: ViewModel() {

    private val isKitchenClosedPrivate = MutableLiveData<Boolean>()

    private val isServerDown = MutableLiveData<Boolean>()

    val isKitchenClosed: LiveData<Boolean>
        get() = isKitchenClosedPrivate

    val isNetworkError: LiveData<Boolean>
        get() = isServerDown

    /**
     * Launch a coroutine to fetch orders from the servers and update the repository with the data
     */
    fun fetchOrders(address: String) {
        viewModelScope.launch(Dispatchers.IO) {
            clearDb()
            val retrofit = RetrofitFactory.getRetrofit(address)
            val networkService =NetworkServiceFactory.getNetworkService(retrofit)
            periodicReadOrders(networkService, isServerDown, OrderRepository.getInstance())
        }
    }

    private suspend fun clearDb() {
        OrderRepository.getInstance().clearDb()
    }

    /**
     * Read the server response periodically (every 2 seconds) until the kitchen is closed
     */
    @VisibleForTesting
    suspend fun periodicReadOrders(networkService: NetworkService, isServerDownParam: MutableLiveData<Boolean>, orderRepo: OrderRepository) {
        var done = false
        while(!done) {
            when (val result = networkService.readOrders()) {
                is OK -> {
                    val orders = result.data
                    isServerDownParam.postValue(false)
                    if (orders.isEmpty()) {
                        done = true

                    }
                    handleOrders(orders, isKitchenClosedPrivate, orderRepo)
                }
                is NetworkError -> {
                    done = true
                    isServerDownParam.postValue(true)
                }
            }
        }
    }

    /**
     * handle orders received from the server
     */
    suspend fun handleOrders(orders: List<Order>, isKitchenClosed: MutableLiveData<Boolean>, orderRepo: OrderRepository) {

        if (orders.isEmpty()) {
            isKitchenClosed.postValue(true)
        } else {
            isKitchenClosed.postValue(false)
            orders.forEach {
                addOrUpdate(it, orderRepo)
            }
        }
        delay(DEFAULT_DELAY)
    }

    /**
     * Add a new order or update an exiting order into the repository
     */
    suspend fun addOrUpdate(newOrder: Order, orderRepo: OrderRepository) {
        val existingOrder = orderRepo.getOrderForId(newOrder.id)
        if (existingOrder == null) {
            orderRepo.insertOrder(newOrder)
            val orderHistoryRow = OrderHistoryRow(primary = null, newOrder.id, newOrder.state, null, newOrder.shelf, null, newOrder.timestamp)
            orderRepo.insertIntoOrderHistory(orderHistoryRow)
        } else {
            if (newOrder.timestamp >= existingOrder.timestamp ) {
                val orderHistoryRow = OrderHistoryRow(primary = null, newOrder.id, newOrder.state, existingOrder.state, newOrder.shelf, existingOrder.shelf, newOrder.timestamp)
                orderRepo.insertIntoOrderHistory(orderHistoryRow)
//                if (newOrder.isDelivered()) {
//                    // TODO : add order lifetime
//                }
                orderRepo.updateOrder(newOrder)
            } else {
                val orderHistoryRow = OrderHistoryRow(primary = null, newOrder.id, newOrder.state, existingOrder.state, newOrder.shelf, existingOrder.shelf, newOrder.timestamp)
                orderRepo.insertIntoOrderHistory(orderHistoryRow)
            }
        }
        updateRevenue(newOrder, orderRepo)
    }

//    fun updateOrderLifetime(orderRepo: OrderRepository, order: Order) {
//        val creationTime = orderRepo.getOrderCreationTimeFromHistory(order)
//        order.lifeTime = order.timestamp - creationTime
//    }

    @VisibleForTesting
    fun updateRevenue(order: Order, orderRepo: OrderRepository) {
        if (order.isTrashed()) {
            Log.d("CSS", "Adding trashed order")
            orderRepo.addTrashedOrderData(order.price)
        } else if (order.isDelivered()) {
            Log.d("CSS", "Adding delivered order")
            orderRepo.addDeliveredOrderData(order.price)
        }
    }
}

