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
import androidx.glance.unit.ColorProvider
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
import androidx.glance.layout.defaultWeight
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

class HyperOSWeatherWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent { WidgetContent(context) }
    }

    @Composable
    private fun WidgetContent(context: Context) {
        val now = Date()
        val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(now)
        val currentDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(now)
        val dayName = SimpleDateFormat("EEEE", Locale("vi", "VN")).format(now)
        val location = WeatherRepository.loadLocation(context)
        val weather = WeatherRepository.loadWeather(context)
        val city = weather?.city ?: location?.third ?: "Chưa có vị trí"

        Column(
            modifier = GlanceModifier.fillMaxSize().padding(12.dp)
                .background(Color(0x33FFFFFF))
        ) {
            Row(
                modifier = GlanceModifier.fillMaxWidth().defaultWeight(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(modifier = GlanceModifier.defaultWeight(), verticalAlignment = Alignment.CenterVertically) {
                    Text(currentTime, TextStyle(fontSize = 36.sp, fontWeight = FontWeight.Normal,
                        color = ColorProvider(Color.White)))
                    Spacer(GlanceModifier.width(8.dp))
                    Column {
                        Text("$dayName $currentDate", TextStyle(fontSize = 11.sp, color = ColorProvider(Color.White)))
                        Text("Thời tiết thực tế", TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold,
                            color = ColorProvider(Color(0xFFFDE047))))
                    }
                }
                Spacer(GlanceModifier.width(1.dp).fillMaxHeight().background(Color(0x33FFFFFF)))
                Column(modifier = GlanceModifier.padding(start = 8.dp)) {
                    Text(weather?.let { "%.0f°C".format(Locale.US, it.temperature) } ?: "--°C",
                        TextStyle(fontSize = 24.sp, color = ColorProvider(Color.White)))
                    Text("📍 $city", TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold,
                        color = ColorProvider(Color.White)))
                    Text(weather?.description ?: "Đang cập nhật",
                        TextStyle(fontSize = 10.sp, color = ColorProvider(Color(0xCCFFFFFF))))
                }
            }

            Spacer(GlanceModifier.fillMaxWidth().height(1.dp).background(Color(0x33FFFFFF)))

            Row(modifier = GlanceModifier.fillMaxWidth().defaultWeight(),
                verticalAlignment = Alignment.CenterVertically) {
                weather?.forecast?.drop(1)?.take(3)?.forEachIndexed { i, f ->
                    if (i > 0) Spacer(GlanceModifier.defaultWeight())
                    ForecastColumn(dayLabel(f.day), "${f.min.roundToInt()}° / ${f.max.roundToInt()}°", f.description)
                    if (i < 2) Spacer(GlanceModifier.defaultWeight())
                }
            }
        }
    }

    private fun dayLabel(iso: String): String = try {
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(iso) ?: return iso
        SimpleDateFormat("EEE", Locale("vi", "VN")).format(date)
    } catch (_: Exception) { iso }

    @Composable
    private fun ForecastColumn(day: String, temp: String, desc: String) {
        Column(horizontalAlignment = Alignment.Start) {
            Text(day, TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ColorProvider(Color.White)))
            Text(temp, TextStyle(fontSize = 10.sp, color = ColorProvider(Color.White)))
            Text(desc, TextStyle(fontSize = 9.sp, color = ColorProvider(Color(0xAAFFFFFF))))
        }
    }
}
