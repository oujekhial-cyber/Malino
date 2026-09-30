package ir.kharjyar.app.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import ir.kharjyar.app.weather.WeatherService
import ir.kharjyar.app.widget.WidgetUpdater

/** تازه‌سازی دوره‌ای هوا و بازطراحی ویجت، مستقل از بازشدن برنامه. */
class WeatherWidgetWorker(context:Context,params:WorkerParameters):CoroutineWorker(context,params){
 override suspend fun doWork():Result = try {
  WeatherService.refresh(applicationContext,force=true)
  WidgetUpdater.requestUpdate(applicationContext)
  Result.success()
 }catch(_:Exception){Result.retry()}
}
