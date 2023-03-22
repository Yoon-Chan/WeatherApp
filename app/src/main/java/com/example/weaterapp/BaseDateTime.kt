package com.example.weaterapp

import java.time.LocalDateTime
import java.time.LocalTime

data class BaseDateTime(
    val baseData : String, val baseTime : String
){
    companion object{
        fun getBaseDateTime() : BaseDateTime{

            //시간 가져오기
            var datetime = LocalDateTime.now()
            val baseTime = when(datetime.toLocalTime()){
                in LocalTime.of(0,0) .. LocalTime.of(2, 30) -> {
                    datetime = datetime.minusDays(1); "2300"
                }
                in LocalTime.of(2, 30) .. LocalTime.of(5, 30) -> "0200"
                in LocalTime.of(5, 30) .. LocalTime.of(8, 30) -> "0500"
                in LocalTime.of(8, 30) .. LocalTime.of(11, 30) -> "0800"
                in LocalTime.of(11, 30) .. LocalTime.of(14, 30) -> "1100"
                in LocalTime.of(14, 30) .. LocalTime.of(17, 30) -> "1400"
                in LocalTime.of(17, 30) .. LocalTime.of(20, 30) -> "1700"
                in LocalTime.of(20, 30) .. LocalTime.of(23, 30) -> "2000"
                else -> "2300"
            }
            val baseDate = String.format("%04d%02d%02d", datetime.year, datetime.monthValue, datetime.dayOfMonth)

            return  BaseDateTime(baseDate, baseTime)
        }
    }
}
