package com.example.currencyconverter.data

import com.example.currencyconverter.models.ConvertCurrency
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.QueryMap

interface CurrencyRatesApi {

    @GET("latest")
    suspend fun getExchangeRates(@QueryMap queries: Map<String, String>): Response<ConvertCurrency>
}