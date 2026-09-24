package com.hyperos.weather.widget

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import android.os.Bundle
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.glance.appwidget.updateAll
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class MainActivity : ComponentActivity() {

    private val requestCode = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(
            TextView(this).apply {
                text =
                    "HyperOS Weather\n\nĐang lấy vị trí và thời tiết…"
                textSize = 18f
                setPadding(40, 40, 40, 40)
            }
        )

        if (
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                requestCode
            )

        } else {
            initialize()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        results: IntArray
    ) {

        super.onRequestPermissionsResult(
            requestCode,
            permissions,
            results
        )

        if (
            requestCode == this.requestCode &&
            results.any {
                it == PackageManager.PERMISSION_GRANTED
            }
        ) {
            initialize()
        }
    }

    private fun initialize() {

        lifecycleScope.launch {

            val success = withContext(Dispatchers.IO) {

                try {

                    val lm =
                        getSystemService(LOCATION_SERVICE)
                                as LocationManager

                    val provider = when {

                        lm.isProviderEnabled(
                            LocationManager.NETWORK_PROVIDER
                        ) ->
                            LocationManager.NETWORK_PROVIDER

                        lm.isProviderEnabled(
                            LocationManager.GPS_PROVIDER
                        ) ->
                            LocationManager.GPS_PROVIDER

                        else ->
                            null
                    }

                    if (provider == null) {
                        return@withContext false
                    }

                    if (
                        ActivityCompat.checkSelfPermission(
                            this@MainActivity,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED
                        &&
                        ActivityCompat.checkSelfPermission(
                            this@MainActivity,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        return@withContext false
                    }

                    val location =
                        lm.getLastKnownLocation(provider)
                            ?: return@withContext false

                    val city = try {

                        val address =
                            Geocoder(
                                this@MainActivity,
                                Locale("vi", "VN")
                            )
                                .getFromLocation(
                                    location.latitude,
                                    location.longitude,
                                    1
                                )
                                ?.firstOrNull()

                        address?.locality
                            ?: address?.subAdminArea
                            ?: address?.adminArea
                            ?: "Vị trí hiện tại"

                    } catch (_: Exception) {

                        "Vị trí hiện tại"
                    }

                    WeatherRepository.saveLocation(
                        this@MainActivity,
                        location.latitude,
                        location.longitude,
                        city
                    )

                    WeatherRepository.fetch(
                        this@MainActivity
                    )

                    true

                } catch (_: Exception) {

                    false
                }
            }

            if (success) {

                HyperOSWeatherWidget()
                    .updateAll(this@MainActivity)
            }
        }
    }
}
