package com.example.healt4u.Service

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object FdaClient {

    private const val BASE_URL =
        "https://api.fda.gov/"

    val api: OpenFdaApi by lazy {

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(
                GsonConverterFactory.create()
            )
            .build()
            .create(OpenFdaApi::class.java)
    }
}
