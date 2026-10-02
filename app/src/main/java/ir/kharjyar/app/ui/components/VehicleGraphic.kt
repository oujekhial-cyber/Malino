package ir.kharjyar.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.*
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class VehicleGraphicKind { PEUGEOT_PARS, PEUGEOT_206, SAMAND, DENA, PRIDE, SEDAN, MOTORCYCLE, PICKUP, TRUCK, BUS, MACHINERY }

fun vehicleGraphicKind(title:String,type:String):VehicleGraphicKind{
 val value=(title+" "+type).lowercase().replace('ي','ی').replace('ك','ک')
 return when{
  "موتور" in value -> VehicleGraphicKind.MOTORCYCLE
  "پژو پارس" in value||"پرشیا" in value -> VehicleGraphicKind.PEUGEOT_PARS
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

/** تصویر برداری اختصاصی؛ بدون استفاده از آیکون سیستم و متناسب با مدل/نوع ثبت‌شده. */
@Composable fun VehicleGraphic(title:String,type:String,accent:Color,modifier:Modifier=Modifier,size:Dp=58.dp){
 val kind=vehicleGraphicKind(title,type)
 Box(modifier.size(size).background(accent.copy(.11f),RoundedCornerShape(17.dp)).border(1.dp,accent.copy(.30f),RoundedCornerShape(17.dp)),contentAlignment=Alignment.Center){
  Canvas(Modifier.size(size*.86f)){val w=this.size.width;val h=this.size.height;val body=accent;val glass=Color.White.copy(.72f);val tire=Color(0xFF20242A);val rim=Color(0xFFCAD2DC)
   fun wheel(x:Float,y:Float,r:Float){drawCircle(tire,r,Offset(x,y));drawCircle(rim,r*.48f,Offset(x,y));drawCircle(body.copy(.75f),r*.18f,Offset(x,y))}
   when(kind){
    VehicleGraphicKind.MOTORCYCLE->{drawCircle(tire,h*.15f,Offset(w*.25f,h*.69f));drawCircle(tire,h*.15f,Offset(w*.75f,h*.69f));drawLine(body,Offset(w*.25f,h*.67f),Offset(w*.48f,h*.45f),h*.065f,StrokeCap.Round);drawLine(body,Offset(w*.48f,h*.45f),Offset(w*.68f,h*.66f),h*.065f,StrokeCap.Round);drawLine(body,Offset(w*.38f,h*.67f),Offset(w*.57f,h*.67f),h*.07f,StrokeCap.Round);drawRoundRect(body,Offset(w*.39f,h*.39f),Size(w*.25f,h*.11f),CornerRadius(h*.05f,h*.05f));drawLine(body,Offset(w*.61f,h*.42f),Offset(w*.70f,h*.28f),h*.045f,StrokeCap.Round);drawLine(body,Offset(w*.68f,h*.29f),Offset(w*.80f,h*.29f),h*.035f,StrokeCap.Round)}
    VehicleGraphicKind.PICKUP->{drawRoundRect(body,Offset(w*.08f,h*.42f),Size(w*.84f,h*.31f),CornerRadius(h*.07f,h*.07f));drawPath(Path().apply{moveTo(w*.12f,h*.43f);lineTo(w*.30f,h*.24f);lineTo(w*.51f,h*.24f);lineTo(w*.61f,h*.43f);close()},body);drawRect(glass,Offset(w*.31f,h*.28f),Size(w*.17f,h*.14f));drawLine(Color.White.copy(.55f),Offset(w*.64f,h*.46f),Offset(w*.89f,h*.46f),h*.025f);wheel(w*.26f,h*.72f,h*.13f);wheel(w*.75f,h*.72f,h*.13f)}
    VehicleGraphicKind.TRUCK,VehicleGraphicKind.BUS,VehicleGraphicKind.MACHINERY->{val bus=kind==VehicleGraphicKind.BUS;drawRoundRect(body,Offset(w*.07f,h*(if(bus).27f else .36f)),Size(w*.86f,h*(if(bus).45f else .37f)),CornerRadius(h*.08f,h*.08f));if(bus){repeat(4){i->drawRoundRect(glass,Offset(w*(.14f+i*.18f),h*.34f),Size(w*.13f,h*.17f),CornerRadius(h*.025f,h*.025f))}}else{drawRoundRect(glass,Offset(w*.12f,h*.42f),Size(w*.25f,h*.18f),CornerRadius(h*.03f,h*.03f));drawLine(Color.White.copy(.5f),Offset(w*.47f,h*.43f),Offset(w*.47f,h*.68f),h*.025f)};wheel(w*.25f,h*.72f,h*.12f);wheel(w*.76f,h*.72f,h*.12f)}
    else->{val hatch=kind==VehicleGraphicKind.PEUGEOT_206;val roofStart=when(kind){VehicleGraphicKind.PEUGEOT_PARS->.26f;VehicleGraphicKind.SAMAND->.25f;VehicleGraphicKind.DENA->.28f;VehicleGraphicKind.PRIDE->.30f;else->.29f};val roofEnd=if(hatch).67f else .72f;val path=Path().apply{moveTo(w*.07f,h*.61f);quadraticBezierTo(w*.09f,h*.48f,w*.22f,h*.45f);lineTo(w*roofStart,h*.29f);quadraticBezierTo(w*.36f,h*.18f,w*.55f,h*.22f);lineTo(w*roofEnd,h*(if(hatch).31f else .40f));lineTo(w*.90f,h*.49f);quadraticBezierTo(w*.96f,h*.53f,w*.92f,h*.68f);lineTo(w*.08f,h*.68f);close()};drawPath(path,body);val windows=Path().apply{moveTo(w*(roofStart+.035f),h*.42f);lineTo(w*(roofStart+.09f),h*.29f);quadraticBezierTo(w*.40f,h*.24f,w*.53f,h*.27f);lineTo(w*(roofEnd-.04f),h*(if(hatch).34f else .42f));close()};drawPath(windows,glass);drawLine(body.copy(.7f),Offset(w*.51f,h*.27f),Offset(w*.52f,h*.48f),h*.022f);drawRoundRect(Color(0xFFFFE082),Offset(w*.84f,h*.49f),Size(w*.09f,h*.055f),CornerRadius(h*.02f,h*.02f));drawRoundRect(Color(0xFFE94D5C),Offset(w*.07f,h*.50f),Size(w*.06f,h*.07f),CornerRadius(h*.02f,h*.02f));if(kind==VehicleGraphicKind.PEUGEOT_PARS){drawLine(Color.White.copy(.65f),Offset(w*.69f,h*.55f),Offset(w*.89f,h*.55f),h*.018f);drawCircle(Color.White.copy(.8f),h*.026f,Offset(w*.77f,h*.58f))};wheel(w*.25f,h*.68f,h*.13f);wheel(w*.76f,h*.68f,h*.13f)}
   }
  }
 }
}
