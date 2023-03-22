package com.example.weaterapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val gson : Gson = GsonBuilder()
            .setLenient()
            .create()

        val retrofit = Retrofit.Builder()
            .baseUrl("http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        val service = retrofit.create(WeatherService::class.java)
        service.getVillageForecast(
            base_date = BaseDateTime.getBaseDateTime().baseData.toInt(),//20230321,
            data_type = "json",
            page_no = 1,
            num_of_rows = 400,
            base_time =  BaseDateTime.getBaseDateTime().baseTime,
            nx = "55",
            ny = "127"
        ).enqueue(object : Callback<WeatherEntity>{
            override fun onFailure(call: Call<WeatherEntity>, t: Throwable) {
                t.printStackTrace()
            }

            override fun onResponse(call: Call<WeatherEntity>, response: Response<WeatherEntity>) {
                val forecastDateTimeMap = mutableMapOf<String, Forecast>()
                val forecastList = response.body()?.response?.body?.items?.forecastEntities.orEmpty()
                Log.e("ForecastSize", forecastList.size.toString())
                for(index in forecastList){
                    //Log.e("Forecast1", index.toString())

                    if(forecastDateTimeMap["${index.forecastDate}/${index.forecastTime}"] == null){
                        forecastDateTimeMap["${index.forecastDate}/${index.forecastTime}"] =
                            Forecast(forecastDate = index.forecastDate, forecastTime = index.forecastTime)
                    }
                    forecastDateTimeMap["${index.forecastDate}/${index.forecastTime}"]?.apply {
                        when(index.category){
                            Category.POP -> precipitation = index.forecastValue.toInt()
                            Category.PTY -> precipitationType = transformRainType(index)
                            Category.SKY -> sky = transformSky(index)
                            Category.TMP -> temperature = index.forecastValue.toDouble()
                            else -> {}
                        }
                    }
                }

                Log.e("Forecast", forecastDateTimeMap.toString())
            }
        })

    }

    private fun transformRainType(forecast: ForecastEntity) : String = when(forecast.forecastValue.toInt()) {
        0 -> "없음"
        1 -> "비"
        2 -> "비/눈"
        3 -> "눈"
        4 -> "소나기"
        else -> ""
    }

    private fun transformSky(forecast: ForecastEntity) : String = when(forecast.forecastValue.toInt()) {
        1 -> "맑음"
        3 -> "구름많음"
        4 -> "흐림"
        else -> ""
    }
}