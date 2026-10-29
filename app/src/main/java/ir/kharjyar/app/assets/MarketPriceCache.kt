package ir.kharjyar.app.assets

import android.content.Context

data class CachedMarketPrice(val valueRial:Long,val updatedAt:Long)

/** Last successful public market values. No user data is stored or transmitted. */
object MarketPriceCache {
 private const val PREFS="market_price_cache"
 fun read(context:Context,code:String):CachedMarketPrice?{
  val p=context.getSharedPreferences(PREFS,Context.MODE_PRIVATE);val value=p.getLong("value_$code",-1L);val at=p.getLong("at_$code",0L)
  return if(value>0&&at>0)CachedMarketPrice(value,at)else null
 }
 fun write(context:Context,code:String,value:Long,updatedAt:Long=System.currentTimeMillis()){
  if(value<=0)return;context.getSharedPreferences(PREFS,Context.MODE_PRIVATE).edit().putLong("value_$code",value).putLong("at_$code",updatedAt).apply()
 }
}
