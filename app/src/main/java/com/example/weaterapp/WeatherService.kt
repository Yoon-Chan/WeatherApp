package com.example.weaterapp

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherService{

    @GET("getVilageFcst?serviceKey=$SERVICEKEY")
    fun getVillageForecast(
        @Query("dataType") data_type : String,
        @Query("numOfRows") num_of_rows : Int,
        @Query("pageNo") page_no : Int,
        @Query("base_date") base_date : Int,
        @Query("base_time") base_time : String,
        @Query("nx") nx : String,
        @Query("ny") ny : String
    )   : Call<WeatherEntity>
}