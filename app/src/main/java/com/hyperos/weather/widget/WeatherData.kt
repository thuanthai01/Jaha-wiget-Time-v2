package com.hyperos.weather.widget

data class WeatherData(
    val temperature: Double,
    val description: String,
    val city: String,
    val forecast: List<Forecast>
)
data class Forecast(val day: String, val min: Double, val max: Double, val description: String)
