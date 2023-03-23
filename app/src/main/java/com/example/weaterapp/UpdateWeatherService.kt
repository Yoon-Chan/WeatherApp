package com.example.weaterapp

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.IBinder
import android.widget.RemoteViews
import android.widget.RemoteViews.RemoteView
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.LocationServices

class UpdateWeatherService : Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        createChannel()
        startForeground(1, createNotification())

        val appWidgetManager: AppWidgetManager = AppWidgetManager.getInstance(this)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            val pendingIntent: PendingIntent = Intent(this, SettingActivity::class.java).let {
                PendingIntent.getActivity(this, 2, it, PendingIntent.FLAG_MUTABLE)
            }

            RemoteViews(packageName, R.layout.widget_weather).apply {
                setTextViewText(R.id.temperatureTextView, "권한없음")
                setTextViewText(R.id.weatherTextView, "")
                setOnClickPendingIntent(R.id.temperatureTextView, pendingIntent)
            }.also { remoteViews ->
                val appWidgetName = ComponentName(this, WeatherAppWidgetProvider::class.java)
                appWidgetManager.updateAppWidget(appWidgetName, remoteViews)
            }

            stopSelf()

            return super.onStartCommand(intent, flags, startId)
        }
        LocationServices.getFusedLocationProviderClient(this).lastLocation.addOnSuccessListener {

            WeatherRepository.getVillageForecast(
                longitude = it.longitude,
                latitude = it.latitude,
                successCallback = { forecastList ->

                    val pendingServiceIntent: PendingIntent =
                        Intent(this, UpdateWeatherService::class.java)
                            .let { intent ->
                                PendingIntent.getService(
                                    this,
                                    1,
                                    intent,
                                    PendingIntent.FLAG_IMMUTABLE
                                )
                            }

                    val currentForecast = forecastList.first()

                    RemoteViews(packageName, R.layout.widget_weather).apply {
                        setTextViewText(
                            R.id.temperatureTextView,
                            getString(R.string.temperature_text, currentForecast.temperature)
                        )
                        setTextViewText(
                            R.id.weatherTextView,
                            currentForecast.weather
                        )
                        setOnClickPendingIntent(R.id.temperatureTextView, pendingServiceIntent)
                    }.also { remoteViews ->
                        val appWidgetName =
                            ComponentName(this, WeatherAppWidgetProvider::class.java)
                        appWidgetManager.updateAppWidget(appWidgetName, remoteViews)
                    }

                    stopSelf()

                },
                failureCallback = {
                    val pendingServiceIntent: PendingIntent =
                        Intent(this, UpdateWeatherService::class.java)
                            .let { intent ->
                                PendingIntent.getService(
                                    this,
                                    1,
                                    intent,
                                    PendingIntent.FLAG_IMMUTABLE
                                )
                            }

                    RemoteViews(packageName, R.layout.widget_weather).apply {
                        setTextViewText(
                            R.id.temperatureTextView,
                            "에러"
                        )
                        setTextViewText(
                            R.id.weatherTextView,
                            ""
                        )
                        setOnClickPendingIntent(R.id.temperatureTextView, pendingServiceIntent)
                    }.also { remoteViews ->
                        val appWidgetName =
                            ComponentName(this, WeatherAppWidgetProvider::class.java)
                        appWidgetManager.updateAppWidget(appWidgetName, remoteViews)
                    }

                    stopSelf()
                }
            )
        }

        return super.onStartCommand(intent, flags, startId)
    }

    private fun createChannel() {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL,
            "날씨앱",
            NotificationManager.IMPORTANCE_LOW
        )

        channel.description = "위젯을 업데이트하는 채널"

        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle("날씨앱")
            .setContentText("날씨 업데이트")
            .build()

    }

    override fun onDestroy() {
        super.onDestroy()

        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    companion object {
        const val NOTIFICATION_CHANNEL = "widget_refresh_channel"
    }
    /*override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        //notification channel
        createChannel()
        startForeground(1, createNotification())
        //foregroundService

        val appWidgetManager: AppWidgetManager = AppWidgetManager.getInstance(this)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            val pendingIntent : PendingIntent = Intent(this, SettingActivity::class.java).let {
                PendingIntent.getActivity(this, 2, it, PendingIntent.FLAG_IMMUTABLE)
            }
            RemoteViews(packageName, R.layout.widget_weather).apply{
                setTextViewText(R.id.temperaturTextView, "권한없음")
                setTextViewText(R.id.weatherTextView, "")
                setOnClickPendingIntent(R.id.temperaturTextView, pendingIntent)
            }.also {remoteViews ->
                val appWidgetName = ComponentName(this, WeatherAppWidgetProvider::class.java)
                appWidgetManager.updateAppWidget(appWidgetName, remoteViews)
            }
            stopSelf()

            //todo 위젯을 권한없음 상태로 표시하고, 클릭했을 때 권한 팝업을 얻을 수 있도록 조정
            return super.onStartCommand(intent, flags, startId)
        }
        LocationServices.getFusedLocationProviderClient(this).lastLocation.addOnSuccessListener {
            WeatherRepository.getVillageForecast(
                longitude = it.longitude, latitude = it.latitude,
                successCallback = { forecastList ->
                    val pendingServiceIntent: PendingIntent =
                        Intent(this, UpdateWeatherService::class.java)
                            .let { intent ->
                                PendingIntent.getService(
                                    this,
                                    1,
                                    intent,
                                    PendingIntent.FLAG_IMMUTABLE
                                )
                            }


                    val currentForecast = forecastList.first()
                    RemoteViews(packageName, R.layout.widget_weather).apply {
                        setTextViewText(
                            R.id.temperaturTextView,
                            getString(R.string.temperature_text, currentForecast.temperature)
                        )
                        setTextViewText(R.id.weatherTextView, currentForecast.weather)

                        setOnClickPendingIntent(R.id.temperaturTextView, pendingServiceIntent)
                    }.also { remoteViews ->
                        val appWidgetName =
                            ComponentName(this, WeatherAppWidgetProvider::class.java)
                        appWidgetManager.updateAppWidget(appWidgetName, remoteViews)
                    }

                    stopSelf()
                },
                failureCallback = {
                    //위젯을 에러 상태로 표시
                    val pendingServiceIntent: PendingIntent =
                        Intent(this, UpdateWeatherService::class.java)
                            .let { intent ->
                                PendingIntent.getService(
                                    this,
                                    1,
                                    intent,
                                    PendingIntent.FLAG_IMMUTABLE
                                )
                            }

                    RemoteViews(packageName, R.layout.widget_weather).apply{
                        setTextViewText(R.id.temperaturTextView, "에러")
                        setTextViewText(R.id.weatherTextView, "")
                        setOnClickPendingIntent(R.id.temperaturTextView, pendingServiceIntent)
                    }.also {remoteViews ->
                        val appWidgetName = ComponentName(this, WeatherAppWidgetProvider::class.java)
                        appWidgetManager.updateAppWidget(appWidgetName, remoteViews)
                    }

                    stopSelf()
                },
            )
        }

        return super.onStartCommand(intent, flags, startId)
    }

    private fun createChannel() {
        val channel =
            NotificationChannel(NOTIFICATION_CHANNEL, "날씨앱", NotificationManager.IMPORTANCE_LOW)

        channel.description = "위젯을 업데이트하는 채널"

        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    private fun createNotification() : Notification{
        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle("날씨앱")
            .setContentText("날씨 업데이트")
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    companion object{
        const val NOTIFICATION_CHANNEL = "widget_refresh_channel"
    }*/
}