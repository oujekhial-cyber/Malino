package ir.kharjyar.app.weather

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URLEncoder
import java.net.URL

/** هواشناسی شهر انتخابی کاربر؛ بدون دسترسی به موقعیت گوشی. */
object WeatherService {
    private const val PREFS = "weather_cache"
    data class WeatherInfo(val temperature: String, val city: String, val icon: String) {
        val displayText get() = "$icon $city $temperature"
    }

    fun city(context: Context): String = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString("city", "تهران") ?: "تهران"
    fun setCity(context: Context, city: String) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
        .putString("city", city.trim().ifBlank { "تهران" }).putLong("updated", 0).apply()

    fun cached(context: Context): WeatherInfo? {
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val temp = p.getString("temperature", null) ?: return null
        return WeatherInfo(temp, p.getString("resolved_city", city(context)) ?: city(context), p.getString("icon", "☀️") ?: "☀️")
    }

    suspend fun refresh(context: Context): WeatherInfo? = withContext(Dispatchers.IO) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (System.currentTimeMillis() - prefs.getLong("updated", 0L) < 6 * 60 * 60 * 1000L) return@withContext cached(context)
        runCatching {
            val requested = city(context)
            val q = URLEncoder.encode(requested, "UTF-8")
            val geo = get("https://geocoding-api.open-meteo.com/v1/search?name=$q&count=1&language=fa&format=json")
            val lat = Regex("\"latitude\"\s*:\s*(-?\d+(?:\.\d+)?)").find(geo)?.groupValues?.get(1) ?: error("city")
            val lon = Regex("\"longitude\"\s*:\s*(-?\d+(?:\.\d+)?)").find(geo)?.groupValues?.get(1) ?: error("city")
            val resolved = Regex("\"name\"\s*:\s*\"([^\"]+)\"").find(geo)?.groupValues?.get(1) ?: requested
            val json = get("https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon&current=temperature_2m,weather_code&timezone=auto")
            val value = Regex("\"temperature_2m\"\s*:\s*(-?\d+(?:\.\d+)?)").find(json)?.groupValues?.get(1)?.toDoubleOrNull() ?: error("temperature")
            val code = Regex("\"weather_code\"\s*:\s*(\d+)").find(json)?.groupValues?.get(1)?.toIntOrNull() ?: 0
            val info = WeatherInfo("${value.toInt()}°", resolved, iconFor(code))
            prefs.edit().putString("temperature", info.temperature).putString("resolved_city", info.city)
                .putString("icon", info.icon).putLong("updated", System.currentTimeMillis()).apply()
            info
        }.getOrElse { cached(context) }
    }

    private fun get(address: String): String {
        val connection = (URL(address).openConnection() as HttpURLConnection).apply { connectTimeout = 8_000; readTimeout = 8_000 }
        return connection.inputStream.bufferedReader().use { it.readText() }.also { connection.disconnect() }
    }

    internal fun iconFor(code: Int): String = when (code) {
        0 -> "☀️"; 1, 2 -> "🌤️"; 3 -> "☁️"; in 45..48 -> "🌫️"
        in 51..67, in 80..82 -> "🌧️"; in 71..77, in 85..86 -> "🌨️"; in 95..99 -> "⛈️"; else -> "🌡️"
    }
}
