package com.aap.cssdelivery.dao

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders")
    fun getOrders(): LiveData<List<Order>>

    @Query("SELECT * FROM orders WHERE id == (:id)")
    fun getOrder(id: String): LiveData<Order>

    @Query("SELECT * FROM orders WHERE id == (:id)")
    fun getOrderRaw(id: String): Order?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(order: Order): Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(order: Order)

    @Query("SELECT * FROM order_history WHERE id == (:id) ORDER BY timestamp")
    fun getOrderHistoryAsLiveData(id: String): LiveData<List<OrderHistoryRow>>

    @Query("SELECT * FROM order_history WHERE id == (:id) ORDER BY timestamp")
    fun getOrderHistory(id: String): List<OrderHistoryRow>

    @Query("SELECT timestamp FROM order_history WHERE id == (:id) AND state == 'CREATED'")
    fun getOrderCreationTime(id: String): Long

    @Query("SELECT avg(lifeTime) from orders WHERE lifeTime IS NOT NULL")
    fun getAverageOrderLifetime(): LiveData<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrderHistory(orderHistoryRow: OrderHistoryRow): Long
}