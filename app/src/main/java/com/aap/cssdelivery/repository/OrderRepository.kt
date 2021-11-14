package com.aap.cssdelivery.repository

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.aap.cssdelivery.dao.Order
import com.aap.cssdelivery.dao.OrderDao
import com.aap.cssdelivery.dao.OrderHistoryRow
import com.aap.cssdelivery.utils.DatabaseFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * The data repository for the app. The database access is done through the repository
 */
class OrderRepository private constructor(private val orderDao: OrderDao) {

    fun getOrders() = orderDao.getOrders()

    fun getOrderForIdAsLiveData(id: String) = orderDao.getOrder(id)

    fun getOrderHistoryForIdAsLiveData(id: String) = orderDao.getOrderHistoryAsLiveData(id)

    // fun getOrderCreationTimeFromHistory(order: Order) = orderDao.getOrderCreationTime(order.id)

    //fun getAverageOrderLifetimeAsLiveData() = orderDao.getAverageOrderLifetime()

    fun getOrderForId(id: String) = orderDao.getOrderRaw(id)

    suspend fun insertOrder(order: Order) {
        withContext(Dispatchers.IO) {
            orderDao.insert(order)
        }
    }

    suspend fun updateOrder(order: Order) {
        withContext(Dispatchers.IO) {
            orderDao.update(order)
        }
    }

    suspend fun insertIntoOrderHistory(orderHistoryRow: OrderHistoryRow) {
        withContext(Dispatchers.IO) {
            orderDao.insertOrderHistory(orderHistoryRow)
        }
    }


    suspend fun clearDb() {
        withContext(Dispatchers.IO) {
            DatabaseFactory.getDatabase().clearAllTables()
        }
    }

    val orderStatLiveData: LiveData<OrderStat>
        get() = orderStatMutable

    private val orderStatMutable = MutableLiveData<OrderStat>()

    private val orderStat = OrderStat()

    /**
     *  Increment the trashed order count and add the price amount to the accumulated loss
     */
    fun addTrashedOrderData(lossAmount: Int) {
        synchronized(orderStat) {
            orderStat.trashed++
            orderStat.loss += lossAmount
        }
        orderStatMutable.postValue(orderStat)
    }

    /**
     * Increment the delivered order count and add the price amount to the accumulated profit
     */
    fun addDeliveredOrderData(profitAmount: Int) {
        synchronized(orderStat) {
            orderStat.delivered++
            orderStat.profit += profitAmount
        }
        orderStatMutable.postValue(orderStat)
    }



    companion object {
        @Volatile
        private var instance: OrderRepository? = null

        fun getInstance() =
            instance ?: synchronized(this) {
                instance ?: OrderRepository(DatabaseFactory.getDatabase().orderDao()).also { instance = it }
            }

        @VisibleForTesting
        fun getInstance(mockOrderDao: OrderDao) =
            instance ?: synchronized(this) {
                instance ?: OrderRepository(mockOrderDao).also { instance = it }
            }
    }

}

data class OrderStat(var trashed: Int = 0, var delivered: Int = 0, var profit: Int = 0, var loss: Int = 0)