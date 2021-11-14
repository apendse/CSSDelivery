package com.aap.cssdelivery.utils

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.aap.cssdelivery.dao.Order
import com.aap.cssdelivery.dao.OrderDao
import com.aap.cssdelivery.dao.OrderHistoryRow

const val DATABASE_NAME = "CSS_Orders"
@Database (entities = [Order::class, OrderHistoryRow::class], version = 1, exportSchema = false)
abstract class AppDatabase: RoomDatabase() {
    abstract fun orderDao(): OrderDao

    companion object {

        // For Singleton instantiation
        @Volatile private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance
                    ?: buildDatabase(
                        context
                    ).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME).build()
        }
    }
}

object DatabaseFactory {
    private lateinit var appDatabase: AppDatabase
    fun initializeDatabase(context: Context) {
        appDatabase = AppDatabase.getInstance(context)
    }

    fun getDatabase(): AppDatabase {
        return appDatabase
    }
}