package ir.kharjyar.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import ir.kharjyar.app.R
import androidx.compose.ui.unit.dp

enum class VehicleGraphicKind { PEUGEOT_PARS, SHAHIN, PEUGEOT_206, SAMAND, DENA, PRIDE, SEDAN, MOTORCYCLE, PICKUP, TRUCK, BUS, MACHINERY }

/** غلط‌های املایی رایج نام سازنده را برای نمایش، تشخیص لوگو و ذخیره یکسان می‌کند. */
fun normalizeVehicleTitle(value:String):String = value.trim()
 .replace(Regex("(?<![آ-ی])هندا(?![آ-ی])"),"هوندا")
 .replace(Regex("(?<![آ-ی])کاواساکی(?![آ-ی])"),"کاوازاکی")

fun vehicleGraphicKind(title:String,type:String):VehicleGraphicKind{
 val value=(normalizeVehicleTitle(title)+" "+type).lowercase().replace('ي','ی').replace('ك','ک')
 return when{
  "موتور" in value -> VehicleGraphicKind.MOTORCYCLE
  "پژو پارس" in value||"پارس سال" in value||"پرشیا" in value -> VehicleGraphicKind.PEUGEOT_PARS
  "شاهین" in value||"shahin" in value -> VehicleGraphicKind.SHAHIN
  "۲۰۶" in value||"206" in value -> VehicleGraphicKind.PEUGEOT_206
  "دنا" in value -> VehicleGraphicKind.DENA
  "سمند" in value -> VehicleGraphicKind.SAMAND
  "پراید" in value||"ساینا" in value||"تیبا" in value -> VehicleGraphicKind.PRIDE
  "وانت" in value -> VehicleGraphicKind.PICKUP
  "اتوبوس" in value||"مینی‌بوس" in value -> VehicleGraphicKind.BUS
  "کامیون" in value||"تریلر" in value -> VehicleGraphicKind.TRUCK
  "ماشین‌آلات" in value||"تراکتور" in value||"لودر" in value -> VehicleGraphicKind.MACHINERY
  else -> VehicleGraphicKind.SEDAN
 }
}

fun vehicleManufacturerLogo(title:String,type:String):Int?{
 val value=(normalizeVehicleTitle(title)+" "+type).lowercase().replace('ي','ی').replace('ك','ک')
 return when{
  listOf("رنو تراکس","رنو کامیون","renault trucks","renault truck","premium truck","range t").any{it in value}->R.drawable.brand_renault_trucks
  listOf("وولوو","volvo","fh12","fh13","fh16","fm9","fm12").any{it in value}->R.drawable.brand_volvo
  listOf("اسکانیا","scania","r420","r440","r450","g410","s500").any{it in value}->R.drawable.brand_scania
  listOf("فاو","faw","j6p","j7 truck").any{it in value}->R.drawable.brand_faw
  listOf("مان کامیون","مان کشنده","man truck","tgx","tgs","tga").any{it in value}->R.drawable.brand_man
  listOf("ایویکو","iveco","stralis","s-way","eurocargo").any{it in value}->R.drawable.brand_iveco
  listOf("داف","daf","xf105","xf 105","xf480","cf85").any{it in value}->R.drawable.brand_daf
  listOf("کاماز","kamaz").any{it in value}->R.drawable.brand_kamaz
  listOf("شاکمان","شکمن","shacman","x5000","x3000").any{it in value}->R.drawable.brand_shacman
  listOf("آمیکو","امیکو","amico","m2631","m1929").any{it in value}->R.drawable.brand_amico
  listOf("ماک","mack","بولداگ","bulldog","anthem","granite").any{it in value}->R.drawable.brand_mack
  listOf("ایسوزو","isuzu","دی مکس","d-max","dmax","npr","nqr","fvr").any{it in value}->R.drawable.brand_isuzu
  listOf("پژو","peugeot").any{it in value}->R.drawable.brand_peugeot
  listOf("شاهین","پراید","تیبا","ساینا","کوییک","اطلس","سهند","سایپا","saipa").any{it in value}->R.drawable.brand_saipa
  listOf("سمند","دنا","تارا","رانا","سورن","ایران خودرو","ikco").any{it in value}->R.drawable.brand_ikco
  listOf("رنو","تندر","ال ۹۰","ال90","ساندرو","لوگان","کپچر","کولیوس","renault").any{it in value}->R.drawable.brand_renault
  listOf("تویوتا","کمری","کرولا","پرادو","لندکروزر","راوفور","یاریس","هایلوکس","toyota").any{it in value}->R.drawable.brand_toyota
  listOf("هیوندای","النترا","سوناتا","آزرا","توسان","سانتافه","اکسنت","ورنا","hyundai").any{it in value}->R.drawable.brand_hyundai
  listOf("کیا","سراتو","اپتیما","اسپورتیج","سورنتو","ریو","پیکانتو","kia").any{it in value}->R.drawable.brand_kia
  listOf("نیسان","ماکسیما","مورانو","جوک","ایکس تریل","قشقایی","تیانا","nissan").any{it in value}->R.drawable.brand_nissan
  listOf("مزدا","mazda").any{it in value}->R.drawable.brand_mazda
  listOf("هوندا","هندا","سیویک","آکورد","cr-v","honda").any{it in value}->R.drawable.brand_honda
  listOf("یاماها","yamaha","nmax","xmax","mt-","yzf").any{it in value}->R.drawable.brand_yamaha
  listOf("کاوازاکی","کاواساکی","kawasaki","ninja","z1000").any{it in value}->R.drawable.brand_kawasaki
  listOf("باجاج","bajaj","پالس","پالسار","pulsar","discover").any{it in value}->R.drawable.brand_bajaj
  listOf("تی وی اس","تی‌وی‌اس","tvs","apache","wego").any{it in value}->R.drawable.brand_tvs
  listOf("بنلی","benelli","tnt","trk").any{it in value}->R.drawable.brand_benelli
  listOf("فیات","fiat","ducato","doblo","500x").any{it in value}->R.drawable.brand_fiat
  listOf("فراری","ferrari").any{it in value}->R.drawable.brand_ferrari
  listOf("لامبورگینی","لامبورجینی","lamborghini").any{it in value}->R.drawable.brand_lamborghini
  listOf("مرسدس","بنز","mercedes","benz").any{it in value}->R.drawable.brand_mercedes
  listOf("بی ام و","بی‌ام‌و","بی‌ ام‌ و","bmw").any{it in value}->R.drawable.brand_bmw
  listOf("آئودی","آودی","audi").any{it in value}->R.drawable.brand_audi
  listOf("فولکس","گلف","پاسات","تیگوان","volkswagen","vw").any{it in value}->R.drawable.brand_volkswagen
  listOf("فورد","موستانگ","تاروس","اکسپلورر","ford").any{it in value}->R.drawable.brand_ford
  listOf("میتسوبیشی","لنسر","اوتلندر","پاجرو","asx","mitsubishi").any{it in value}->R.drawable.brand_mitsubishi
  listOf("سوزوکی","ویتارا","کیزاشی","گرند ویتارا","suzuki").any{it in value}->R.drawable.brand_suzuki
  listOf("چری","ام وی ام","mvm","فونیکس","تیگو","آریزو","chery","fownix").any{it in value}->R.drawable.brand_chery
  listOf("بی وای دی","بی‌وای‌دی","byd","سانگ پلاس","دلفین","هان","اتو ۳","atto 3").any{it in value}->R.drawable.brand_byd
  listOf("لوکانو","lucano","جیکو","jaecoo","l7","l8").any{it in value}->R.drawable.brand_lucano
  listOf("کی ام سی","کی‌ام‌سی","kmc","j7","x5","t8","t9","eagle").any{it in value}->R.drawable.brand_kmc
  listOf("لاماری","ایما","lamari","eama").any{it in value}->R.drawable.brand_lamari
  listOf("بهمن","فیدلیتی","دیگنیتی","ریسپکت","کاپرا","bahman","fidelity","dignity").any{it in value}->R.drawable.brand_bahman
  listOf("جی ای سی","جی‌ای‌سی","gac","امپو","امکو","empow","emkoo","gs3").any{it in value}->R.drawable.brand_gac
  listOf("جیلی","آزکارا","geely","azkarra","coolray").any{it in value}->R.drawable.brand_geely
  listOf("دانگ فنگ","دانگ‌فنگ","dongfeng","شاین مکس","shine max","aeolus").any{it in value}->R.drawable.brand_dongfeng
  listOf("هاوال","haval","h2","h6","jolion").any{it in value}->R.drawable.brand_haval
  listOf("جک","jac","s3","s5","j4").any{it in value}->R.drawable.brand_jac
  else->null
 }
}

/** نشان سازنده برای مدل‌های شناخته‌شده و گرافیک نوع وسیله برای موارد ناشناخته. */
@Composable fun VehicleGraphic(title:String,type:String,accent:Color,modifier:Modifier=Modifier,size:Dp=58.dp){
 val kind=vehicleGraphicKind(title,type)
 val shape=RoundedCornerShape(17.dp)
 Box(
  modifier.size(size).clip(shape)
   .background(Brush.radialGradient(listOf(accent.copy(.18f),accent.copy(.06f))))
   .border(1.dp,accent.copy(.30f),shape),
  contentAlignment=Alignment.Center
 ){
  val manufacturerLogo=vehicleManufacturerLogo(title,type)
  if(manufacturerLogo!=null){
   // Fit و حاشیه امن مانع بیرون‌زدن قاب سفید/شفاف PNGهای ناهم‌اندازه می‌شود.
   Image(painterResource(manufacturerLogo),contentDescription="نشان سازنده ${title.ifBlank{type}}",modifier=Modifier.fillMaxSize().padding(7.dp).clip(RoundedCornerShape(11.dp)),contentScale=ContentScale.Fit)
  }else Canvas(Modifier.size(size*.88f)){val w=this.size.width;val h=this.size.height;val body=accent;val glass=Color(0xFFEAF8FF).copy(.88f);val tire=Color(0xFF161A20);val rim=Color(0xFFE4EAF1)
   // سایه نرم زیر وسیله، آیکون عمومی را از حالت تخت خارج می‌کند.
   drawOval(Color.Black.copy(.18f),Offset(w*.12f,h*.72f),Size(w*.76f,h*.13f))
   fun wheel(x:Float,y:Float,r:Float){drawCircle(tire,r,Offset(x,y));drawCircle(rim,r*.48f,Offset(x,y));drawCircle(body.copy(.75f),r*.18f,Offset(x,y))}
   when(kind){
    VehicleGraphicKind.MOTORCYCLE->{
     // موتورسیکلت عمومی با چرخ، پره، انجین، باک، زین، فرمان و چراغ واقعی‌تر.
     listOf(w*.24f,w*.77f).forEach{x->drawCircle(tire,h*.16f,Offset(x,h*.70f));drawCircle(rim,h*.105f,Offset(x,h*.70f));repeat(8){i->val a=i*Math.PI/4;drawLine(Color(0xFF6F7A86),Offset(x,h*.70f),Offset(x+kotlin.math.cos(a).toFloat()*h*.09f,h*.70f+kotlin.math.sin(a).toFloat()*h*.09f),h*.009f)};drawCircle(tire,h*.025f,Offset(x,h*.70f))}
     drawLine(body,Offset(w*.25f,h*.68f),Offset(w*.47f,h*.46f),h*.055f,StrokeCap.Round);drawLine(body,Offset(w*.47f,h*.46f),Offset(w*.68f,h*.67f),h*.055f,StrokeCap.Round);drawLine(body,Offset(w*.34f,h*.67f),Offset(w*.61f,h*.67f),h*.055f,StrokeCap.Round)
     drawCircle(Color(0xFF56616D),h*.10f,Offset(w*.49f,h*.61f));drawCircle(Color(0xFFB9C3CC),h*.045f,Offset(w*.49f,h*.61f))
     drawRoundRect(Brush.linearGradient(listOf(body.lightenVehicle(.34f),body)),Offset(w*.39f,h*.36f),Size(w*.28f,h*.15f),CornerRadius(h*.07f,h*.07f));drawRoundRect(Color(0xFF252A31),Offset(w*.34f,h*.32f),Size(w*.24f,h*.065f),CornerRadius(h*.03f,h*.03f))
     drawLine(body,Offset(w*.63f,h*.42f),Offset(w*.73f,h*.25f),h*.04f,StrokeCap.Round);drawLine(Color(0xFF252A31),Offset(w*.69f,h*.26f),Offset(w*.83f,h*.26f),h*.025f,StrokeCap.Round);drawCircle(Color(0xFFFFE28A),h*.045f,Offset(w*.74f,h*.37f));drawLine(Color(0xFFD7DEE5),Offset(w*.52f,h*.66f),Offset(w*.70f,h*.74f),h*.035f,StrokeCap.Round)
    }
    VehicleGraphicKind.PICKUP->{drawRoundRect(body,Offset(w*.08f,h*.42f),Size(w*.84f,h*.31f),CornerRadius(h*.07f,h*.07f));drawPath(Path().apply{moveTo(w*.12f,h*.43f);lineTo(w*.30f,h*.24f);lineTo(w*.51f,h*.24f);lineTo(w*.61f,h*.43f);close()},body);drawRect(glass,Offset(w*.31f,h*.28f),Size(w*.17f,h*.14f));drawLine(Color.White.copy(.55f),Offset(w*.64f,h*.46f),Offset(w*.89f,h*.46f),h*.025f);wheel(w*.26f,h*.72f,h*.13f);wheel(w*.75f,h*.72f,h*.13f)}
    VehicleGraphicKind.TRUCK,VehicleGraphicKind.BUS,VehicleGraphicKind.MACHINERY->{val bus=kind==VehicleGraphicKind.BUS;drawRoundRect(body,Offset(w*.07f,h*(if(bus).27f else .36f)),Size(w*.86f,h*(if(bus).45f else .37f)),CornerRadius(h*.08f,h*.08f));if(bus){repeat(4){i->drawRoundRect(glass,Offset(w*(.14f+i*.18f),h*.34f),Size(w*.13f,h*.17f),CornerRadius(h*.025f,h*.025f))}}else{drawRoundRect(glass,Offset(w*.12f,h*.42f),Size(w*.25f,h*.18f),CornerRadius(h*.03f,h*.03f));drawLine(Color.White.copy(.5f),Offset(w*.47f,h*.43f),Offset(w*.47f,h*.68f),h*.025f)};wheel(w*.25f,h*.72f,h*.12f);wheel(w*.76f,h*.72f,h*.12f)}
    else->{val hatch=kind==VehicleGraphicKind.PEUGEOT_206;val roofStart=when(kind){VehicleGraphicKind.PEUGEOT_PARS->.26f;VehicleGraphicKind.SAMAND->.25f;VehicleGraphicKind.DENA->.28f;VehicleGraphicKind.PRIDE->.30f;else->.29f};val roofEnd=if(hatch).67f else .72f;val path=Path().apply{moveTo(w*.07f,h*.61f);quadraticBezierTo(w*.09f,h*.48f,w*.22f,h*.45f);lineTo(w*roofStart,h*.29f);quadraticBezierTo(w*.36f,h*.18f,w*.55f,h*.22f);lineTo(w*roofEnd,h*(if(hatch).31f else .40f));lineTo(w*.90f,h*.49f);quadraticBezierTo(w*.96f,h*.53f,w*.92f,h*.68f);lineTo(w*.08f,h*.68f);close()};drawPath(path,Brush.verticalGradient(listOf(body.lightenVehicle(.38f),body,body.darkenVehicle(.24f))));drawLine(Color.White.copy(.48f),Offset(w*.15f,h*.48f),Offset(w*.82f,h*.48f),h*.012f,StrokeCap.Round);val windows=Path().apply{moveTo(w*(roofStart+.035f),h*.42f);lineTo(w*(roofStart+.09f),h*.29f);quadraticBezierTo(w*.40f,h*.24f,w*.53f,h*.27f);lineTo(w*(roofEnd-.04f),h*(if(hatch).34f else .42f));close()};drawPath(windows,glass);drawLine(body.copy(.7f),Offset(w*.51f,h*.27f),Offset(w*.52f,h*.48f),h*.022f);drawRoundRect(Color(0xFFFFE082),Offset(w*.84f,h*.49f),Size(w*.09f,h*.055f),CornerRadius(h*.02f,h*.02f));drawRoundRect(Color(0xFFE94D5C),Offset(w*.07f,h*.50f),Size(w*.06f,h*.07f),CornerRadius(h*.02f,h*.02f));if(kind==VehicleGraphicKind.PEUGEOT_PARS){drawLine(Color.White.copy(.65f),Offset(w*.69f,h*.55f),Offset(w*.89f,h*.55f),h*.018f);drawCircle(Color.White.copy(.8f),h*.026f,Offset(w*.77f,h*.58f))};wheel(w*.25f,h*.68f,h*.13f);wheel(w*.76f,h*.68f,h*.13f)}
   }
  }
 }
}

private fun Color.lightenVehicle(amount:Float)=Color(
 red+(1f-red)*amount,green+(1f-green)*amount,blue+(1f-blue)*amount,alpha
)
private fun Color.darkenVehicle(amount:Float)=Color(
 red*(1f-amount),green*(1f-amount),blue*(1f-amount),alpha
)
