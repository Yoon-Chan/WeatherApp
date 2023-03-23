package com.example.weaterapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import com.example.weaterapp.databinding.ActivityMainBinding
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory


class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                // Only approximate location access granted.
                updateLocation()
            }
            else -> {
                // No location access granted.
                Toast.makeText(this, "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", packageName, null)
                }
                startActivity(intent)
                finish()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )


    }

    private fun updateLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionRequest.launch(
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
            return
        }

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient.lastLocation.addOnSuccessListener {
            Log.e("lastLocation", it.toString())

            val gson: Gson = GsonBuilder()
                .setLenient()
                .create()

            val retrofit = Retrofit.Builder()
                .baseUrl("http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()

            val converter = GeoPointConverter()
            Log.e("lastLocation", "${it.latitude},${it.longitude}")
            val point = converter.convert(lon = it.longitude, lat = it.latitude)
            Log.e("converter", "${point.nx}, ${point.ny}")
            val service = retrofit.create(WeatherService::class.java)
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
                    t.printStackTrace()
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

                    val currentForecast = list.first()
                    binding.temperaturTextView.text = getString(R.string.temperature_text, currentForecast.temperature)
                    binding.skyTextView.text = currentForecast.weather
                    binding.precipitationTextView.text = getString(R.string.precipitation_text, currentForecast.precipitation)
                    //Log.e("Forecast", forecastDateTimeMap.toString())
                }
            })
        }


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