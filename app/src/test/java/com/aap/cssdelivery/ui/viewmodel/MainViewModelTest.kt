package com.aap.cssdelivery.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import com.aap.cssdelivery.dao.Order
import com.aap.cssdelivery.network.NetworkError
import com.aap.cssdelivery.network.NetworkService
import com.aap.cssdelivery.network.OK
import com.aap.cssdelivery.repository.OrderRepository
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test


class MainViewModelTest {

    @MockK
    lateinit var mockOrderRepository: OrderRepository

    lateinit var mainViewModel: MainViewModel
    @Before
    fun setup() {
        MockKAnnotations.init(this)
        mainViewModel = MainViewModel()
    }

    @Test
    fun test_addOrUpdate_insertHappens() = runBlocking {
        every {mockOrderRepository.getOrderForId(any())} returns null
        coEvery {mockOrderRepository.insertOrder(any())} returns Unit
        mainViewModel.addOrUpdate(testOrder, mockOrderRepository)
        coVerify {mockOrderRepository.insertOrder(testOrder) }
    }

    @Test
    fun test_addOrUpdate_updateHappens() = runBlocking {
        every {mockOrderRepository.getOrderForId(any())} returns existingOrderOlder
        coEvery {mockOrderRepository.updateOrder(any())} returns Unit
        coEvery {mockOrderRepository.insertIntoOrderHistory(any())} returns Unit
        mainViewModel.addOrUpdate(testOrder, mockOrderRepository)
        coVerify { mockOrderRepository.updateOrder(testOrder) }
        coVerify { mockOrderRepository.insertIntoOrderHistory(any()) }
    }

    @Test
    fun test_addOrUpdate_updateDoesNotHappenForOlderOrder() = runBlocking {
        every {mockOrderRepository.getOrderForId(any())} returns existingOrderNewer
        coEvery {mockOrderRepository.updateOrder(any())} returns Unit
        coEvery {mockOrderRepository.insertIntoOrderHistory(any())} returns Unit
        mainViewModel.addOrUpdate(testOrder, mockOrderRepository)
        coVerify(exactly = 1) { mockOrderRepository.getOrderForId(testOrder.id) }
        coVerify(exactly = 0) { mockOrderRepository.updateOrder(testOrder) }
        coVerify(exactly = 0) { mockOrderRepository.insertIntoOrderHistory(any()) }

    }


    private val existingOrderNewer: Order
        get() = Order("123", 123, "item1", "John", "1234 some place", "", "", "", 1240)

    private val existingOrderOlder: Order
        get() = Order("123", 123, "item1", "John", "1234 some place", "", "", "", 1230)
    private val testOrder: Order
        get() = Order("123", 123, "item1", "John", "1234 some place", "", "", "", 1234)

    @Test
    fun handleOrders_isKitchenClosedTrue() = runBlocking {
        val mockKitchenClosed = mockk<MutableLiveData<Boolean>>()
        every {mockKitchenClosed.postValue(any())} returns Unit
        mainViewModel.handleOrders(emptyList(), mockKitchenClosed, mockOrderRepository)
        verify { mockKitchenClosed.postValue(true) }
    }

    @Test
    fun handleOrders_isKitchenClosedFalse() = runBlocking {
        val mockKitchenClosed = mockk<MutableLiveData<Boolean>>()
        every {mockKitchenClosed.postValue(any())} returns Unit
        every {mockOrderRepository.getOrderForId(any())} returns null
        coEvery {mockOrderRepository.insertOrder(any())} returns Unit
        mainViewModel.handleOrders(listOf(testOrder), mockKitchenClosed, mockOrderRepository)
        verify { mockKitchenClosed.postValue(false) }
    }

    @Test
    fun periodicReadOrders_testNetworkDown() = runBlocking {
        val mockNetworkService = mockk<NetworkService>()
        val mockIsServerDown = mockk<MutableLiveData<Boolean>>()
        every { mockIsServerDown.postValue(any())} returns Unit
        coEvery { mockNetworkService.readOrders() } returns NetworkError(Exception("Test exception"))
        mainViewModel.periodicReadOrders(mockNetworkService, mockIsServerDown, mockOrderRepository)
        verify { mockIsServerDown.postValue(true) }
    }

    @Test
    fun periodicReadOrders_testNetworkNotDown() = runBlocking {
        val mockNetworkService = mockk<NetworkService>()
        val mockIsServerDown = mockk<MutableLiveData<Boolean>>()
        every { mockIsServerDown.postValue(any())} returns Unit
        coEvery { mockNetworkService.readOrders() } returns OK(emptyList())
        every {mockOrderRepository.getOrderForId(any())} returns null
        coEvery {mockOrderRepository.insertOrder(any())} returns Unit
        mainViewModel.periodicReadOrders(mockNetworkService, mockIsServerDown, mockOrderRepository)
        verify { mockIsServerDown.postValue(false) }
    }

    private val deliveredOrder: Order
        get() = Order("123", 1231, "item1", "John", "1234 some place", "DELIVERED", "", "", 1234)

    private val trashedOrder: Order
        get() = Order("123", 1999, "item1", "John", "1234 some place", "TRASHED", "", "", 1234)
    @Test
    fun updateRevenue_profitUpdated() = runBlocking {
        coEvery {mockOrderRepository.addTrashedOrderData(any())} returns Unit
        coEvery {mockOrderRepository.addDeliveredOrderData(any())} returns Unit
        mainViewModel.updateRevenue(deliveredOrder, mockOrderRepository)
        coVerify { mockOrderRepository.addDeliveredOrderData(deliveredOrder.price) }
    }

    @Test
    fun updateRevenue_lossUpdated() {
        coEvery {mockOrderRepository.addTrashedOrderData(any())} returns Unit
        coEvery {mockOrderRepository.addDeliveredOrderData(any())} returns Unit
        mainViewModel.updateRevenue(trashedOrder, mockOrderRepository)
        coVerify { mockOrderRepository.addTrashedOrderData(trashedOrder.price) }

    }

}