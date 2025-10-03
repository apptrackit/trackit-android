package com.ballabotond.trackit.data.api

import com.ballabotond.trackit.data.model.CreateMetricRequest
import com.ballabotond.trackit.data.model.GetMetricTypesResponse
import com.ballabotond.trackit.data.model.GetMetricsResponse
import com.ballabotond.trackit.data.model.MetricResponse
import com.ballabotond.trackit.data.model.UpdateMetricRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface MetricsApiService {
    @GET("api/metrics")
    suspend fun getMetricEntries(
        @Header("Authorization") authToken: String,
        @Query("metric_type_id") metricTypeId: Int? = null,
        @Query("limit") limit: Int = 100,
        @Query("offset") offset: Int = 0
    ): Response<GetMetricsResponse>
    
    @GET("api/metrics/types")
    suspend fun getMetricTypes(
        @Header("Authorization") authToken: String
    ): Response<GetMetricTypesResponse>
    
    @POST("api/metrics")
    suspend fun createMetricEntry(
        @Header("Authorization") authToken: String,
        @Body request: CreateMetricRequest
    ): Response<MetricResponse>
    
    @PUT("api/metrics/{entryId}")
    suspend fun updateMetricEntry(
        @Header("Authorization") authToken: String,
        @Path("entryId") entryId: String,
        @Body request: UpdateMetricRequest
    ): Response<MetricResponse>
    
    @DELETE("api/metrics/{entryId}")
    suspend fun deleteMetricEntry(
        @Header("Authorization") authToken: String,
        @Path("entryId") entryId: String
    ): Response<MetricResponse>
}
