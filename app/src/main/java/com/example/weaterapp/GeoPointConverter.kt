package com.example.weaterapp

import android.util.Log
import kotlin.math.*

class GeoPointConverter {
    private val RE = 6371.00877     //지도 반경
    private val GRID = 5.0  //격자 간격
    private val SLAT1 = 30.0;   //표준위도 1
    private val SLAT2 = 60.0    //표준 위도 2
    private val OLON = 126.0    //기준점 경도
    private val OLAT = 38.0     //기준점 위도
    private val XO = 210 / GRID
    private val YO  = 675 / GRID

    private val DEGRAD = PI / 180.0
    private val RADDEG = 180.0/ PI


    private val re = RE/GRID
    private val slat1 = SLAT1 * DEGRAD
    private val slat2 = SLAT2 * DEGRAD
    private val olon = OLON * DEGRAD
    private val olat = OLAT * DEGRAD

    data class Points(val nx : Int, val ny : Int)

    fun convert(lon : Double, lat : Double ): Points{
        var sn = tan(PI * 0.25 + slat2 * 0.5) / tan(PI*0.25 + slat1 * 0.5)

        sn = log2(cos(slat1) / cos(slat2)) / log2(sn)
        Log.e("Converter", "sn : $sn")

        var sf = tan(PI * 0.25 + slat1 * 0.5)
        sf = sf.pow(sn)* cos(slat1)/sn
        Log.e("Converter", "sf : $sf")

        Log.e("Converter", "PI * 0.25 + olat* 0.5 : ${PI * 0.25 + olat* 0.5}")
        var ro  = tan(PI * 0.25 + olat* 0.5)
        ro = re * sf / ro.pow(sn)
        Log.e("Converter", "ro : $ro")


        var ra = tan(PI * 0.25 + lat * DEGRAD * 0.5)
        ra = re * sf / ra.pow(sn)
        Log.e("Converter", "ra : $ra")

        var theta = lon * DEGRAD - olon
        if(theta > PI)
            theta -= 2.0 * PI
        if(theta < -PI) theta+= 2.0 * PI
        Log.e("Converter", "theta: $theta")
        theta *= sn

        val nx = ra * sin(theta) + XO + 1.5
        val ny = ro - ra* cos(theta) + YO + 1.5

        Log.e("Converter", "${nx.toString()}, ${ny.toString()}")
        return Points(nx.toInt(),ny.toInt())
    }
}