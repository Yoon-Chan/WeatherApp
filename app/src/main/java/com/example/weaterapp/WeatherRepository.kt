package com.example.weaterapp

import com.example.weaterapp.databinding.ItemForecastBinding
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object WeatherRepository {

    val gson: Gson = GsonBuilder()
        .setLenient()
        .create()

    val retrofit = Retrofit.Builder()
        .baseUrl("http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/")
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    val service = retrofit.create(WeatherService::class.java)

    fun getVillageForecast(
        longitude : Double,
        latitude : Double,
        successCallback : (List<Forecast>) -> Unit,
        failureCallback : (Throwable) -> Unit,
    ){
        val converter = GeoPointConverter()
        //Log.e("lastLocation", "${it.latitude},${it.longitude}")
        val point = converter.convert(lon = longitude, lat = latitude)
        //Log.e("converter", "${point.nx}, ${point.ny}")
        service.getVillageForecast(
            base_date = BaseDateTime.getBaseDateTime().baseData.toInt(),//20230321,
            data_type = "json",
            page_no = 1,
            num_of_rows = 400,
            base_time = BaseDateTime.getBaseDateTime().baseTime,
            nx = point.nx.toString(),
            ny = point.ny.toString()
        ).enqueue(object : Callback<WeatherEntity> {
            override fun onFailure(call: Call<WeatherEntity>, t: Throwable) {
                failureCallback(t)
            }

            override fun onResponse(
                call: Call<WeatherEntity>,
                response: Response<WeatherEntity>
            ) {
                val forecastDateTimeMap = mutableMapOf<String, Forecast>()
                val forecastList =
                    response.body()?.response?.body?.items?.forecastEntities.orEmpty()
                //Log.e("ForecastSize", forecastList.size.toString())
                for (index in forecastList) {
                    //Log.e("Forecast1", index.toString())

                    if (forecastDateTimeMap["${index.forecastDate}/${index.forecastTime}"] == null) {
                        forecastDateTimeMap["${index.forecastDate}/${index.forecastTime}"] =
                            Forecast(
                                forecastDate = index.forecastDate,
                                forecastTime = index.forecastTime
                            )
                    }
                    forecastDateTimeMap["${index.forecastDate}/${index.forecastTime}"]?.apply {
                        when (index.category) {
                            Category.POP -> precipitation = index.forecastValue.toInt()
                            Category.PTY -> precipitationType = transformRainType(index)
                            Category.SKY -> sky = transformSky(index)
                            Category.TMP -> temperature = index.forecastValue.toDouble()
                            else -> {}
                        }
                    }
                }

                val list = forecastDateTimeMap.values.toMutableList()
                list.sortWith(Comparator { t, t2 ->
                    val f1DateTime = "${t.forecastDate}${t.forecastTime}"
                    val f21DateTime = "${t2.forecastDate}${t2.forecastTime}"

                    return@Comparator f1DateTime.compareTo(f21DateTime)
                })
                if(list.isEmpty()){
                    failureCallback(NullPointerException())
                }else {
                    successCallback(list)
                }

                val currentForecast = list.first()
                //Log.e("Forecast", forecastDateTimeMap.toString())
            }
        })
    }

    private fun transformRainType(forecast: ForecastEntity): String =
        when (forecast.forecastValue.toInt()) {
            0 -> "없음"
            1 -> "비"
            2 -> "비/눈"
            3 -> "눈"
            4 -> "소나기"
            else -> ""
        }

    private fun transformSky(forecast: ForecastEntity): String =
        when (forecast.forecastValue.toInt()) {
            1 -> "맑음"
            3 -> "구름많음"
            4 -> "흐림"
            else -> ""
        }
}