package com.aap.cssdelivery.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.launchActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.aap.cssdelivery.R
import com.aap.cssdelivery.dao.Order
import junit.framework.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OrderDetailFragmentTest {

    @Test
    fun test1() {
        val scenario = launchActivity<TestActivity>()
        scenario.moveToState(Lifecycle.State.CREATED)
        scenario.moveToState(Lifecycle.State.RESUMED)
        scenario.onActivity { activity ->
            val container = activity.findViewById<ViewGroup>(R.id.container)
            val orderDetailFragment = OrderDetailFragment()
            val trans = activity.supportFragmentManager.beginTransaction()
            trans.add(orderDetailFragment, "test").commit()
            val layoutInflater = LayoutInflater.from(InstrumentationRegistry.getInstrumentation().targetContext)
            val view = layoutInflater.inflate(R.layout.fragment_order_detail, null, false)
            orderDetailFragment.onCreateView(layoutInflater, container, null)
            orderDetailFragment.onViewCreated(view, null)

            val order = Order("123", 123, "Item", "John", "555",
                                "CREATED", "FROZEN", "", 123456)
            orderDetailFragment.populateView(order)
            assertEquals(orderDetailFragment.binding.item.text.toString(), order.item)
        }
    }
}