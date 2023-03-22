package com.example.weaterapp

data class Forecast(
    val forecastDate : String,
    val forecastTime : String,

    var temperature : Double = 0.0,
    var sky : String ="",
    var precipitationType : String = "", //강수량
    var precipitation : Int =0
)