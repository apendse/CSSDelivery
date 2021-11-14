package com.aap.cssdelivery.network

sealed class NetworkResult<out T>

//object Loading: NetworkResult<Nothing>()

data class OK<out T>(val data: T): NetworkResult<T>()

data class RecoverableError(val code: Int, val message: String = ""): NetworkResult<Nothing>()

data class NetworkError(val exception: Throwable): NetworkResult<Nothing>()
