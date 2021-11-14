package com.aap.cssdelivery.dao

import androidx.room.Entity
import androidx.room.PrimaryKey

private const val TRASHED = "TRASHED"
private const val DELIVERED = "DELIVERED"
@Entity(tableName="orders")
data class Order(
    @PrimaryKey val id: String,
    val price: Int,
    val item: String,
    val customer: String,
    val destination: String,
    val state: String,
    val shelf: String,
    val idealShelf: String,
    val timestamp: Long,
    var lifeTime: Long? = null
) {
    fun isTrashed() = state == TRASHED
    fun isDelivered() = state == DELIVERED
    fun isTerminated() = isTrashed() || isDelivered()
}

/*
enum class State {
   CREATED, COOKING, WAITING, DELIVERED, TRASHED, CANCELLED
}

enum class Shelf {
    HOT, COLD, FROZEN, OVERFLOW, NONE
}

 */
