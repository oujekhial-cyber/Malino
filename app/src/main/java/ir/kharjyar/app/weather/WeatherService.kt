package ir.kharjyar.app.weather

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import kotlin.coroutines.resume

/** دمای موقعیت گوشی با کش شش‌ساعته؛ در نبود موقعیت/شبکه آخرین مقدار نمایش داده می‌شود. */
object WeatherService {
    private const val PREFS = "weather_cache"

    fun cached(context: Context): String? = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        .getString("temperature", null)

    suspend fun refresh(context: Context): String? = withContext(Dispatchers.IO) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val age = System.currentTimeMillis() - prefs.getLong("updated", 0L)
        if (age < 6 * 60 * 60 * 1000L) return@withContext prefs.getString("temperature", null)
        val location = currentLocation(context) ?: return@withContext cached(context)
        runCatching {
            val url = URL("https://api.open-meteo.com/v1/forecast?latitude=${location.latitude}&longitude=${location.longitude}&current=temperature_2m&timezone=auto")
            val connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 8_000; readTimeout = 8_000; requestMethod = "GET"
            }
            val json = connection.inputStream.bufferedReader().use { it.readText() }
            connection.disconnect()
            val value = Regex(""""temperature_2m"\s*:\s*(-?\d+(?:\.\d+)?)""")
                .find(json)?.groupValues?.get(1)?.toDoubleOrNull() ?: error("temperature missing")
            val text = "${value.toInt()}°"
            prefs.edit().putString("temperature", text).putLong("updated", System.currentTimeMillis()).apply()
            text
        }.getOrElse { cached(context) }
    }

    @Suppress("MissingPermission")
    private suspend fun currentLocation(context: Context): Location? {
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (!granted) return null
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val providers = manager.getProviders(true)
        val recent = providers.mapNotNull { runCatching { manager.getLastKnownLocation(it) }.getOrNull() }
            .maxByOrNull { it.time }
        if (recent != null && System.currentTimeMillis() - recent.time < 24 * 60 * 60 * 1000L) return recent
        val provider = when {
            manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
            manager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
            else -> return recent
        }
        return suspendCancellableCoroutine { continuation ->
            manager.getCurrentLocation(provider, null, context.mainExecutor) { continuation.resume(it) }
        }
    }
}
