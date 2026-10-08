package ir.kharjyar.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.kharjyar.app.core.text.Digits
import ir.kharjyar.app.data.db.AccountEntity
import ir.kharjyar.app.data.db.AccountType

/**
 * نسخه کوتاه برای پیش‌نمایش در فرم معرفی حساب: مستقیماً موجودیت حساب را می‌گیرد.
 *
 * @param balanceText اگر null باشد، بخش مانده نمایش داده نمی‌شود.
 * @param masked اگر true باشد شماره کارت و CVV2 پوشانده می‌شوند.
 */
@Composable
fun BankCard(
    account: AccountEntity,
    balanceText: String?,
    balanceCaption: String?,
    selected: Boolean,
    masked: Boolean,
    showDetailsWhenSelected: Boolean = true,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onCopy: (label: String, value: String) -> Unit = { _, _ -> }
) {
    BankCard(
        title = account.title,
        bankName = account.bankName,
        colorArgb = account.colorArgb,
        accountType = account.accountType,
        ownerName = account.ownerName,
        cashLocation = account.cashLocation,
        balanceText = balanceText ?: "",
        balanceHint = balanceCaption ?: "",
        selected = selected,
        cardNumber = account.cardNumber,
        accountNumber = account.accountNumber,
        iban = account.iban,
        expiry = account.cardExpiry,
        cvv2 = account.cardCvv2,
        showSecrets = !masked,
        balanceColor = null,
        showDetailsWhenSelected = showDetailsWhenSelected,
        modifier = modifier,
        onClick = onClick,
        onCopy = onCopy
    )
}

/**
 * کارت حساب به شکل کارت عابربانک.
 *
 * رنگ کارت از رنگ انتخابی خود حساب گرفته می‌شود تا هر بانک ظاهر خودش را داشته باشد.
 * فیلدهایی که کاربر وارد نکرده باشد اصلاً نمایش داده نمی‌شوند.
 *
 * @param selected کارت فعال؛ حاشیه روشن و کمی بزرگ‌تر می‌شود.
 * @param balanceText مانده برآوردی، از قبل قالب‌بندی‌شده.
 */
@Composable
fun BankCard(
    title: String,
    bankName: String,
    colorArgb: Long,
    balanceText: String,
    balanceHint: String,
    selected: Boolean,
    accountType: String = AccountType.BANK,
    ownerName: String = "",
    cashLocation: String = "",
    cardNumber: String = "",
    accountNumber: String = "",
    iban: String = "",
    expiry: String = "",
    cvv2: String = "",
    showSecrets: Boolean = false,
    balanceColor: Color? = null,
    showDetailsWhenSelected: Boolean = true,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onCopy: (label: String, value: String) -> Unit = { _, _ -> },
    /** محتوای دلخواه زیر مانده؛ برای نمایش خلاصه واریز/برداشت همین حساب. */
    content: @Composable ColumnScope.() -> Unit = {}
) {
    if (accountType == AccountType.CASH) {
        CashFundCard(
            title = title,
            ownerName = ownerName,
            location = cashLocation,
            colorArgb = colorArgb,
            balanceText = balanceText,
            balanceHint = balanceHint,
            selected = selected,
            modifier = modifier,
            onClick = onClick,
            content = content
        )
        return
    }
    // رنگ کارت از روی لوگوی بانک؛ برای بانک ناشناس، رنگ ذخیره‌شده خود حساب
    val base = bankCardColor(bankName, colorArgb)
    // گرادیان از رنگ حساب: روشن‌تر در بالا-راست، تیره‌تر در پایین-چپ
    val top = base.lighten(0.18f)
    val bottom = base.darken(0.32f)
    // متن روی کارت بر اساس روشنایی رنگ انتخاب می‌شود تا همیشه خوانا بماند
    val onCard = if (base.luminance() > 0.55f) Color(0xFF14121A) else Color.White
    val shape = RoundedCornerShape(20.dp)

    var targetRotation by remember(title, cardNumber) { mutableFloatStateOf(0f) }
    var verticalDrag by remember { mutableFloatStateOf(0f) }
    // زاویه تجمعی است: حتی اگر کاربر ده بار پیاپی از بالا به پایین بکشد، هر بار
    // یک نیم‌دور دیگر در همان جهت افزوده می‌شود و جهت به‌صورت یکی‌درمیان برنمی‌گردد.
    val flipRotation by animateFloatAsState(
        targetValue = targetRotation,
        animationSpec = tween(
            durationMillis = 880,
            easing = CubicBezierEasing(.22f, 0f, .18f, 1f)
        ),
        label = "bankCardDirectionalFlip"
    )
    val radians = Math.toRadians(flipRotation.toDouble())
    val flipWave = kotlin.math.abs(kotlin.math.sin(radians).toFloat())
    val faceStrength = ((kotlin.math.abs(kotlin.math.cos(radians)).toFloat() - .10f) / .35f).coerceIn(0f,1f)
    val showingFront = kotlin.math.cos(radians) >= 0.0
    val frontVisible = if(showingFront) faceStrength else 0f
    val backVisible = if(showingFront) 0f else faceStrength

    Box(
        modifier = modifier
            .graphicsLayer {
                rotationX = flipRotation
                cameraDistance = 28f * density
                scaleX = 1f - (.025f * flipWave)
                scaleY = 1f - (.045f * flipWave)
                translationY = -5.dp.toPx() * flipWave
                // سایه برجسته قدیمی حذف شده؛ در میانه چرخش یک برق نرم داخل خود
                // کارت دیده می‌شود و لبه بیرونی تمیز باقی می‌ماند.
                shadowElevation = 0f
                clip = false
            }
            .pointerInput(title,cardNumber) {
                detectVerticalDragGestures(
                    onVerticalDrag = { change, amount -> change.consume();verticalDrag+=amount },
                    onDragEnd = {
                        if(kotlin.math.abs(verticalDrag)>42f){
                            // در مختصات Compose، rotationX مثبت لبه بالایی را به سمت
                            // پایین می‌آورد؛ پس علامت باید مستقیماً هم‌جهت drag باشد.
                            val direction=if(verticalDrag>0f)1f else -1f
                            targetRotation+=direction*180f
                        }
                        verticalDrag=0f
                    },
                    onDragCancel={verticalDrag=0f}
                )
            }
            .clip(shape)
            .background(Brush.linearGradient(listOf(top, bottom)))
            // انتخاب با نشان تیک مشخص است؛ قاب خطی هنگام چرخش حذف شده تا لبه‌ها نرم بمانند.
            .clickable(onClick = onClick)
    ) {
        // هاله شیشه‌ای داخلی جای سایه سنگین قدیمی را گرفته است. شدت آن فقط نزدیک
        // لبه چرخش بیشتر می‌شود و در ابتدا/انتهای حرکت کاملاً محو است.
        Box(
            Modifier.matchParentSize().background(
                Brush.linearGradient(
                    listOf(
                        Color.Transparent,
                        onCard.copy(alpha=.16f*flipWave),
                        base.lighten(.28f).copy(alpha=.10f*flipWave),
                        Color.Transparent
                    )
                )
            )
        )
        // نشان بانک به‌صورت واترمارک: تک‌رنگ و بسیار کم‌رنگ، در گوشه کارت.
        // آن‌قدر محو است که خواندن مبلغ و شماره کارت را سخت نمی‌کند.
        bankLogoRes(bankName)?.let { logo ->
            Box(modifier = Modifier.matchParentSize(), contentAlignment = Alignment.CenterEnd) {
                Image(
                    painter = painterResource(logo),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    colorFilter = ColorFilter.tint(onCard),
                    modifier = Modifier
                        .fillMaxHeight(0.92f)
                        .aspectRatio(1f)
                        .offset(x = 16.dp)
                        .alpha(0.10f * frontVisible)
                )
            }
        }

    Column(
        modifier = Modifier
            .matchParentSize()
            .padding(16.dp)
            .graphicsLayer {
                alpha = frontVisible
                // متن هنگام دورشدن کارت اندکی عقب می‌رود تا عمق واقعی‌تری حس شود.
                translationY = 3.dp.toPx() * flipWave
            }
    ) {
        // ---------- ردیف بالا: نام بانک و نشان ----------
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = balanceColor ?: onCard,
                    maxLines = 1
                )
                if (bankName.isNotBlank()) {
                    Text(
                        bankName,
                        style = MaterialTheme.typography.labelSmall,
                        color = onCard.copy(alpha = 0.75f),
                        maxLines = 1
                    )
                }
            }
            // نشان بانک: دایره‌ای با رنگ و کوته‌نوشت همان بانک. اگر بانک ناشناس
            // باشد، نشان خنثی با آیکون بانک نشان داده می‌شود.
            Box(contentAlignment = Alignment.Center) {
                BankLogo(bankName = bankName, size = 34.dp, ringColor = onCard)
                if (selected) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(15.dp)
                            .clip(CircleShape)
                            .background(onCard),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Check,
                            contentDescription = null,
                            tint = bottom,
                            modifier = Modifier.size(11.dp)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // ---------- شماره کارت ----------
        if (cardNumber.isNotBlank()) {
            Text(
                formatCardNumber(cardNumber, showSecrets),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = onCard,
                letterSpacing = 1.5.sp,
                maxLines = 1,
                modifier = Modifier.clickable { onCopy("شماره کارت", cardNumber) }
            )
            Spacer(Modifier.height(10.dp))
        }

        // ---------- مانده (در حالت پیش‌نمایش فرم نمایش داده نمی‌شود) ----------
        if (balanceText.isNotBlank() || balanceHint.isNotBlank()) {
            if (balanceHint.isNotBlank()) {
                Text(
                    balanceHint,
                    style = MaterialTheme.typography.labelSmall,
                    color = onCard.copy(alpha = 0.72f)
                )
            }
            if (balanceText.isNotBlank()) {
                Text(
                    balanceText,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = onCard,
                    maxLines = 1
                )
            }
        }

        // خلاصه واریز/برداشت همیشه به لبه پایین کارت تکیه می‌کند؛ محتوای
        // بالایی با تغییر طول شماره یا مانده، جای این نوارها را عوض نمی‌کند.
        Spacer(Modifier.weight(1f))
        content()

        // جزئیات کامل روی پشت کارت نمایش داده می‌شوند.
    }

    Column(
            Modifier
                .matchParentSize()
                .graphicsLayer {
                    rotationX = 180f
                    alpha = backVisible
                    translationY = -3.dp.toPx() * flipWave
                }
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column { Text(title, color=onCard, fontWeight=FontWeight.Black);Text("اطلاعات کامل حساب",color=onCard.copy(.72f),style=MaterialTheme.typography.labelSmall) }
                BankLogo(bankName=bankName,size=34.dp,ringColor=onCard)
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(onCard.copy(.24f)))
            if(accountNumber.isNotBlank()) CardField("شماره حساب",Digits.ltr(Digits.toPersian(accountNumber)),onCard){onCopy("شماره حساب",accountNumber)}
            if(iban.isNotBlank()) CardField("شبا",Digits.ltr("IR"+Digits.toPersian(iban)),onCard){onCopy("شماره شبا","IR$iban")}
            Row(horizontalArrangement=Arrangement.spacedBy(28.dp)) {
                if(expiry.isNotBlank()) CardField("انقضا",formatCardExpiry(expiry),onCard){}
                if(cvv2.isNotBlank()) CardField("CVV2",if(showSecrets)Digits.ltr(Digits.toPersian(cvv2)) else "•••",onCard){}
            }
            if(ownerName.isNotBlank()) Text("دارنده: $ownerName",color=onCard,style=MaterialTheme.typography.bodyMedium)
            Text("برای بازگشت، کارت را عمودی بکشید",color=onCard.copy(.68f),style=MaterialTheme.typography.labelSmall,modifier=Modifier.align(Alignment.CenterHorizontally))
        }

    }
}


/** کارت مدرن مخصوص پول فیزیکی؛ از کارت بانکی متمایز اما هم‌خانواده داشبورد است. */
@Composable
private fun CashFundCard(
    title:String,
    ownerName:String,
    location:String,
    colorArgb:Long,
    balanceText:String,
    balanceHint:String,
    selected:Boolean,
    modifier:Modifier=Modifier,
    onClick:()->Unit={},
    content:@Composable ColumnScope.()->Unit={}
) {
    val accent=Color(colorArgb)
    val deep=Color(0xFF102820)
    val shape=RoundedCornerShape(24.dp)
    Box(
        modifier.clip(shape)
            .background(Brush.linearGradient(listOf(deep,accent.darken(.48f),Color(0xFF071B1B))))
            .border(if(selected)2.dp else 1.dp,if(selected)Color(0xFFFFD978) else Color.White.copy(.14f),shape)
            .clickable(onClick=onClick)
    ) {
        // حلقه‌ها و سکه محو، حس پول فیزیکی می‌دهند بدون اینکه متن شلوغ شود.
        Box(Modifier.size(190.dp).offset(x=(-58).dp,y=(-82).dp).border(28.dp,accent.copy(.10f),CircleShape))
        Box(Modifier.size(130.dp).align(Alignment.BottomEnd).offset(x=42.dp,y=48.dp).background(Color(0xFFFFD66B).copy(.07f),CircleShape))
        Text("﷼",fontSize=92.sp,fontWeight=FontWeight.Black,color=Color.White.copy(.055f),modifier=Modifier.align(Alignment.CenterEnd).offset(x=(-20).dp))
        Column(Modifier.matchParentSize().padding(17.dp)) {
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(title,fontWeight=FontWeight.Black,style=MaterialTheme.typography.titleMedium,color=Color.White,maxLines=1)
                    Text("صندوق نقدی",style=MaterialTheme.typography.labelMedium,color=Color(0xFFFFD978))
                }
                Box(Modifier.size(48.dp).background(Brush.radialGradient(listOf(Color(0xFFFFE69B),Color(0xFFDCA93B))),RoundedCornerShape(15.dp)).border(1.dp,Color.White.copy(.45f),RoundedCornerShape(15.dp)),contentAlignment=Alignment.Center) {
                    Icon(Icons.Filled.Payments,"صندوق نقدی",tint=Color(0xFF49320A),modifier=Modifier.size(27.dp))
                    if(selected) Box(Modifier.align(Alignment.BottomEnd).size(16.dp).background(Color(0xFF35D39A),CircleShape),contentAlignment=Alignment.Center){Icon(Icons.Filled.Check,null,tint=Color(0xFF06251C),modifier=Modifier.size(11.dp))}
                }
            }
            Spacer(Modifier.height(16.dp))
            if(balanceText.isNotBlank()) {
                Text(balanceHint.ifBlank{"وجه نقد موجود"},style=MaterialTheme.typography.labelSmall,color=Color.White.copy(.65f))
                Text(balanceText,style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Black,color=Color.White,maxLines=1)
            }
            if(ownerName.isNotBlank()||location.isNotBlank()) {
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                    if(ownerName.isNotBlank()) CashMetaPill(Icons.Filled.Person,ownerName,Modifier.weight(1f))
                    if(location.isNotBlank()) CashMetaPill(Icons.Filled.Place,location,Modifier.weight(1f))
                }
            }
            Spacer(Modifier.weight(1f))
            content()
        }
    }
}

@Composable
private fun CashMetaPill(icon:androidx.compose.ui.graphics.vector.ImageVector,text:String,modifier:Modifier=Modifier){
    Row(modifier.background(Color.White.copy(.08f),RoundedCornerShape(50)).border(1.dp,Color.White.copy(.11f),RoundedCornerShape(50)).padding(horizontal=9.dp,vertical=6.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(5.dp)){
        Icon(icon,null,tint=Color(0xFFFFD978),modifier=Modifier.size(14.dp));Text(text,color=Color.White.copy(.84f),style=MaterialTheme.typography.labelSmall,maxLines=1)
    }
}

@Composable
private fun CardField(
    label: String,
    value: String,
    onCard: Color,
    onCopy: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onCopy)
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = onCard.copy(alpha = 0.7f)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = onCard,
            maxLines = 1,
            modifier = Modifier.weight(1f)
        )
        Icon(
            Icons.Filled.ContentCopy,
            contentDescription = "کپی",
            tint = onCard.copy(alpha = 0.6f),
            modifier = Modifier.size(14.dp)
        )
    }
}

/**
 * گروه‌بندی چهارتایی شماره کارت؛ در حالت مخفی فقط چهار رقم آخر دیده می‌شود.
 *
 * خروجی داخل ایزوله چپ‌به‌راست پیچیده می‌شود تا در چیدمان راست‌به‌چپ، ترتیب
 * گروه‌ها برعکس دیده نشود (۵۰۲۹ باید سمت چپ‌ترین نباشد بلکه اولین گروه بماند).
 */
/**
 * تاریخ در دیتابیس به صورت ماه/سال نگهداری می‌شود، اما روی کارت از چپ به راست
 * «سال/ماه» نمایش داده می‌شود تا سال سمت چپ و ماه سمت راست قرار بگیرد.
 */
internal fun formatCardExpiry(raw: String): String {
    val normalized = Digits.normalize(raw.trim())
    val parts = normalized.split(Regex("[/\\-.\\s]+"), limit = 2)
    val visual = if (parts.size == 2 && parts.all { it.isNotBlank() }) {
        "${parts[1]}/${parts[0]}"
    } else normalized
    return Digits.ltr(Digits.toPersian(visual))
}

private fun formatCardNumber(raw: String, reveal: Boolean): String {
    val digits = Digits.normalize(raw).filter { it.isDigit() }
    if (digits.isEmpty()) return ""
    val shown = if (reveal || digits.length <= 4) digits
    else "•".repeat(digits.length - 4) + digits.takeLast(4)
    return Digits.ltr(Digits.toPersian(shown.chunked(4).joinToString("  ")))
}

private fun Color.lighten(f: Float) = Color(
    red + (1f - red) * f,
    green + (1f - green) * f,
    blue + (1f - blue) * f,
    alpha
)

private fun Color.darken(f: Float) = Color(red * (1f - f), green * (1f - f), blue * (1f - f), alpha)
