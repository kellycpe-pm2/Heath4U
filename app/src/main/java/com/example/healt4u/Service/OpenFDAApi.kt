package com.example.healt4u.Service

import com.example.healt4u.model.OpenFdaResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenFdaApi {

    @GET("drug/ndc.json")
    suspend fun searchDrug(
        @Query("search") search: String,
        @Query("limit") limit: Int = 5
    ): OpenFdaResponse
}
