package com.aap.cssdelivery.repository

import androidx.lifecycle.LiveData
import com.aap.cssdelivery.dao.Order
import com.aap.cssdelivery.dao.OrderDao
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

class OrderRepositoryTest {

    @MockK
    lateinit var orderDao: OrderDao

    private lateinit var orderRepository: OrderRepository

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        orderRepository = OrderRepository.getInstance(orderDao)
    }

    @After
    fun tearDown() {
    }

    // Only passes if run individually
    @Test
    fun getOrders() {
        val liveData = FakeLiveData<List<Order>>()
        every {orderDao.getOrders()} returns liveData
        orderRepository.getOrders()
        verify(exactly = 1) { orderDao.getOrders() }
    }

    class FakeLiveData<T>: LiveData<T>()

    // Only passes if run individually
    @Test
    fun getOrder() {
        val id = "5555"
        every{orderDao.getOrderRaw(id)} returns Order(id, 0, "", "", "", "", "", "", 0L, 0L)
        orderRepository.getOrderForId(id)
        verify(exactly = 1) { orderDao.getOrderRaw(id) }
    }

    @Test
    fun addDeliveredOrderData() {
        val orderStatBefore = orderRepository.orderStatLiveData.value
        val add = 1122
        orderRepository.addDeliveredOrderData(add)
        val orderStatAfter = orderRepository.orderStatLiveData.value
        assert(orderStatAfter?.delivered == orderStatBefore?.delivered?.plus(1))
        assert(orderStatAfter?.profit == orderStatBefore?.profit?.plus(add))
    }

    @Test
    fun addTrashedOrderData() {
        val orderStatBefore = orderRepository.orderStatLiveData.value
        val lost = 1122
        orderRepository.addTrashedOrderData(lost)
        val orderStatAfter = orderRepository.orderStatLiveData.value
        assert(orderStatAfter?.trashed == orderStatBefore?.trashed?.plus(1))
        assert(orderStatAfter?.loss == orderStatBefore?.loss?.plus(lost))
    }

}