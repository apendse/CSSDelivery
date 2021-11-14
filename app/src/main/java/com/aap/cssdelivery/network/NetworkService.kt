package com.aap.cssdelivery.network

import com.aap.cssdelivery.dao.Order
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST


class NetworkService(retrofit: Retrofit) {

    private val orderService = retrofit.create(OrderService::class.java)


    fun readOrders(): NetworkResult<List<Order>> {
        try {
            val response = orderService.getOrders().execute()
            when(response.code()) {
                200 ->{
                    response.body()?.let {
                        return OK(it)
                    } ?: NetworkError(RuntimeException("Null response"))

                }
                409 -> {
                   return RecoverableError(409, "")
                }
                else -> {return NetworkError(RuntimeException("Null response"))}
            }
            /*
            if (response.isSuccessful) {
                response.body()?.let {
                    return OK(it)
                } ?: NetworkError(RuntimeException("Null response"))
            }*/
            //return NetworkError(RuntimeException("Network error ${response.code()}"))
        } catch (ex: Exception) {
            return NetworkError(ex)
        }
        return NetworkError(java.lang.RuntimeException())
    }

    fun cancelOrder(id: String): NetworkResult<String> {
        try {
            val response = orderService.cancelOrder(id).execute()
            when(response.code()) {
                200 -> {
                    return OK("")
                }
                else -> {
                    return NetworkError(java.lang.RuntimeException("unknown"))
                }
            }
        } catch(ex: Exception) {
            return NetworkError(java.lang.RuntimeException("unknown"))
        }
        return NetworkError(java.lang.RuntimeException("unknown"))
    }


}

object NetworkServiceFactory {
    fun getNetworkService(retrofit: Retrofit) = NetworkService(retrofit)
}

object RetrofitFactory {
    fun getRetrofit(address: String): Retrofit = Retrofit.Builder()
        .baseUrl(address)
        .client(OkHttpClient())
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}

interface OrderService {
    @GET("order_events")
    fun getOrders(): Call<List<Order>>

    @POST("cancel")
    fun cancelOrder(@Body orderId: String): Call<Any>
}