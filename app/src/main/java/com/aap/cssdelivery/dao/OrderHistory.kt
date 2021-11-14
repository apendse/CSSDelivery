package com.aap.cssdelivery.dao

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName="order_history")
data class OrderHistoryRow(
    @PrimaryKey(autoGenerate = true) val primary: Int? = null,
    val id: String,
    val state: String, val prevState: String?, val shelf: String, val prevShelf: String?, val timestamp: Long)