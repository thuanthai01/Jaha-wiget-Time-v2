package com.hyperos.weather.widget

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import android.os.Bundle
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.concurrent.Executors

class MainActivity : Activity() {
    private val executor = Executors.newSingleThreadExecutor()
    private val requestCode = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(TextView(this).apply {
            text = "HyperOS Weather\n\nĐang lấy vị trí và thời tiết…"
            textSize = 18f
            setPadding(40, 40, 40, 40)
        })

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                requestCode
            )
        } else {
            initialize()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, results: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, results)
        if (requestCode == this.requestCode && results.any { it == PackageManager.PERMISSION_GRANTED }) initialize()
    }

    private fun initialize() {
        executor.execute {
            val lm = getSystemService(LOCATION_SERVICE) as LocationManager
            val provider = when {
                lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
                lm.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
                else -> null
            }
            if (provider == null) return@execute

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) return@execute

            val location = lm.getLastKnownLocation(provider) ?: return@execute
            val city = try {
                Geocoder(this, Locale("vi", "VN")).getFromLocation(location.latitude, location.longitude, 1)
                    ?.firstOrNull()?.locality
                    ?: Geocoder(this, Locale("vi", "VN")).getFromLocation(location.latitude, location.longitude, 1)
                        ?.firstOrNull()?.subAdminArea
                    ?: "Vị trí hiện tại"
            } catch (_: Exception) {
                "Vị trí hiện tại"
            }

            WeatherRepository.saveLocation(this, location.latitude, location.longitude, city)
            try { WeatherRepository.fetch(this) } catch (_: Exception) {}
            lifecycleScope.launch { HyperOSWeatherWidget().updateAll(this@MainActivity) }
        }
    }
}
