package com.hyperos.weather.widget

import android.content.Context
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Locale

object WeatherRepository {
    private const val PREF = "weather"
    private const val LAT = "lat"
    private const val LON = "lon"
    private const val CITY = "city"

    fun saveLocation(context: Context, lat: Double, lon: Double, city: String) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit()
            .putFloat(LAT, lat.toFloat())
            .putFloat(LON, lon.toFloat())
            .putString(CITY, city)
            .apply()
    }

    fun saveWeather(context: Context, data: WeatherData) {
        val p = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        val forecast = data.forecast.joinToString("|") { "${it.day},${it.min},${it.max},${it.description}" }
        p.edit().putFloat("temp", data.temperature.toFloat())
            .putString("desc", data.description)
            .putString("forecast", forecast)
            .apply()
    }

    fun loadWeather(context: Context): WeatherData? {
        val p = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        if (!p.contains("temp")) return null
        val forecast = p.getString("forecast", "")!!.split("|").filter { it.isNotBlank() }.mapNotNull {
            val x = it.split(",", limit = 4)
            if (x.size == 4) Forecast(x[0], x[1].toDoubleOrNull() ?: return@mapNotNull null, x[2].toDoubleOrNull() ?: return@mapNotNull null, x[3]) else null
        }
        return WeatherData(
            p.getFloat("temp", 0f).toDouble(),
            p.getString("desc", "Đang cập nhật") ?: "Đang cập nhật",
            p.getString(CITY, "Vị trí hiện tại") ?: "Vị trí hiện tại",
            forecast
        )
    }

    fun loadLocation(context: Context): Triple<Double, Double, String>? {
        val p = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        if (!p.contains(LAT) || !p.contains(LON)) return null
        return Triple(p.getFloat(LAT, 0f).toDouble(), p.getFloat(LON, 0f).toDouble(), p.getString(CITY, "Vị trí hiện tại") ?: "Vị trí hiện tại")
    }

    fun fetch(context: Context): WeatherData? {
        val loc = loadLocation(context) ?: return null
        val (lat, lon, city) = loc
        val url = "https://api.open-meteo.com/v1/forecast?latitude=${lat}&longitude=${lon}" +
                "&current=temperature_2m,weather_code&daily=weather_code,temperature_2m_max,temperature_2m_min" +
                "&timezone=auto&forecast_days=4"
        val conn = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 10000
            readTimeout = 10000
        }
        return try {
            val json = JSONObject(conn.inputStream.bufferedReader().use { it.readText() })
            val current = json.getJSONObject("current")
            val daily = json.getJSONObject("daily")
            val times = daily.getJSONArray("time")
            val codes = daily.getJSONArray("weather_code")
            val maxs = daily.getJSONArray("temperature_2m_max")
            val mins = daily.getJSONArray("temperature_2m_min")
            val forecasts = mutableListOf<Forecast>()
            for (i in 0 until minOf(4, times.length())) {
                forecasts += Forecast(
                    times.getString(i),
                    mins.getDouble(i),
                    maxs.getDouble(i),
                    description(codes.getInt(i))
                )
            }
            WeatherData(
                current.getDouble("temperature_2m"),
                description(current.getInt("weather_code")),
                city,
                forecasts
            ).also { saveWeather(context, it) }
        } finally {
            conn.disconnect()
        }
    }

    private fun description(code: Int): String = when (code) {
        0 -> "Trời quang"
        1, 2, 3 -> "Có mây"
        45, 48 -> "Sương mù"
        51, 53, 55, 56, 57 -> "Mưa phùn"
        61, 63, 65, 66, 67 -> "Mưa"
        71, 73, 75, 77 -> "Tuyết"
        80, 81, 82 -> "Mưa rào"
        85, 86 -> "Mưa tuyết"
        95, 96, 99 -> "Dông"
        else -> "Không rõ"
    }
}
