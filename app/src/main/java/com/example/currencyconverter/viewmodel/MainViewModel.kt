package com.example.currencyconverter.viewmodel

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.currencyconverter.data.Repository
import com.example.currencyconverter.models.ConvertCurrency
import com.example.currencyconverter.utils.NetworkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: Repository,
    application: Application
) : AndroidViewModel(application) {

    var exchangeRatesResponse: MutableLiveData<NetworkResult<ConvertCurrency>> = MutableLiveData()

    fun getExchangeRates(queries: Map<String, String>) {
        viewModelScope.launch {
            getExchangeRatesSafeCall(queries)
        }
    }

    private suspend fun getExchangeRatesSafeCall(queries: Map<String, String>) {
        if (hasInternetConnection()) {
            try {
                val response = repository.remote.getExchangeRates(queries)
                exchangeRatesResponse.value = handleExchangeRatesResponse(response)
            } catch (e: Exception) {
                exchangeRatesResponse.value =
                    NetworkResult.Error(message = "No Response. Try again!!")
            }
        } else {
            exchangeRatesResponse.value = NetworkResult.Error(message = "No Internet Connection")
        }
    }

    private fun handleExchangeRatesResponse(response: Response<ConvertCurrency>): NetworkResult<ConvertCurrency>? {
        return when {
            response.message().toString().contains("timeout") -> {
                NetworkResult.Error(message = "Time Out")
            }
            response.isSuccessful -> {
                val exchangeResponse = response.body()
                NetworkResult.Success(data = exchangeResponse!!)
            }
            else -> {
                NetworkResult.Error(message = "Could not Fetch Results")
            }
        }
    }

    private fun hasInternetConnection(): Boolean {
        val connectivityManager =
            getApplication<Application>().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false

        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }
}