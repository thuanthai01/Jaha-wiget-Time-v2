package com.hyperos.weather.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

class HyperOSWeatherWidget : GlanceAppWidget() {

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId
    ) {
        provideContent {
            WidgetContent(context)
        }
    }

    @Composable
    private fun WidgetContent(context: Context) {

        val now = Date()

        val currentTime =
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(now)

        val currentDate =
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(now)

        val dayName =
            SimpleDateFormat("EEEE", Locale("vi", "VN")).format(now)

        val location = WeatherRepository.loadLocation(context)
        val weather = WeatherRepository.loadWeather(context)

        val city =
            weather?.city
                ?: location?.third
                ?: "Chưa có vị trí"

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(12.dp)
                .background(Color(0x33FFFFFF))
        ) {

            // =========================
            // HÀNG THÔNG TIN HIỆN TẠI
            // =========================

            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                // Thời gian + ngày
                Column(
                    modifier = GlanceModifier.defaultWeight(),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Text(
                            text = currentTime,
                            style = TextStyle(
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Normal,
                                color = ColorProvider(Color.White)
                            )
                        )

                        Spacer(
                            modifier = GlanceModifier.width(8.dp)
                        )

                        Column {

                            Text(
                                text = "$dayName $currentDate",
                                style = TextStyle(
                                    fontSize = 11.sp,
                                    color = ColorProvider(Color.White)
                                )
                            )

                            Text(
                                text = "Thời tiết thực tế",
                                style = TextStyle(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ColorProvider(
                                        Color(0xFFFDE047)
                                    )
                                )
                            )
                        }
                    }
                }

                // Đường ngăn cách
                Spacer(
                    modifier = GlanceModifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(Color(0x33FFFFFF))
                )

                Spacer(
                    modifier = GlanceModifier.width(8.dp)
                )

                // Nhiệt độ + vị trí
                Column(
                    modifier = GlanceModifier.defaultWeight()
                ) {

                    Text(
                        text = weather
                            ?.let {
                                "%.0f°C".format(
                                    Locale.US,
                                    it.temperature
                                )
                            }
                            ?: "--°C",
                        style = TextStyle(
                            fontSize = 24.sp,
                            color = ColorProvider(Color.White)
                        )
                    )

                    Text(
                        text = "📍 $city",
                        style = TextStyle(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorProvider(Color.White)
                        )
                    )

                    Text(
                        text = weather?.description ?: "Đang cập nhật",
                        style = TextStyle(
                            fontSize = 10.sp,
                            color = ColorProvider(
                                Color(0xCCFFFFFF)
                            )
                        )
                    )
                }
            }

            Spacer(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color(0x33FFFFFF))
            )

            Spacer(
                modifier = GlanceModifier.height(8.dp)
            )

            // =========================
            // DỰ BÁO 3 NGÀY
            // =========================

            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                val forecasts =
                    weather?.forecast
                        ?.drop(1)
                        ?.take(3)
                        ?: emptyList()

                forecasts.forEachIndexed { index, forecast ->

                    if (index > 0) {
                        Spacer(
                            modifier = GlanceModifier
                                .defaultWeight()
                        )
                    }

                    ForecastColumn(
                        day = dayLabel(forecast.day),
                        temp = "${forecast.min.roundToInt()}° / ${forecast.max.roundToInt()}°",
                        desc = forecast.description
                    )

                    if (index < forecasts.lastIndex) {
                        Spacer(
                            modifier = GlanceModifier
                                .defaultWeight()
                        )
                    }
                }
            }
        }
    }

    private fun dayLabel(iso: String): String {

        return try {

            val date = SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.US
            ).parse(iso)

            if (date == null) {
                iso
            } else {
                SimpleDateFormat(
                    "EEE",
                    Locale("vi", "VN")
                ).format(date)
            }

        } catch (_: Exception) {
            iso
        }
    }

    @Composable
    private fun ForecastColumn(
        day: String,
        temp: String,
        desc: String
    ) {

        Column(
            horizontalAlignment = Alignment.Start
        ) {

            Text(
                text = day,
                style = TextStyle(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorProvider(Color.White)
                )
            )

            Text(
                text = temp,
                style = TextStyle(
                    fontSize = 10.sp,
                    color = ColorProvider(Color.White)
                )
            )

            Text(
                text = desc,
                style = TextStyle(
                    fontSize = 9.sp,
                    color = ColorProvider(
                        Color(0xAAFFFFFF)
                    )
                )
            )
        }
    }
}
