package ir.kharjyar.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ir.kharjyar.app.core.text.Digits

/** آیکون مرتبط دسته، بدون نیاز به ذخیره داده اضافی در دیتابیس. */
fun categoryIconFor(name: String): ImageVector {
    val n = Digits.normalizeForMatch(name)
    return when {
        listOf("مواد غذایی", "سوپرمارکت", "خوراک").any(n::contains) -> Icons.Filled.ShoppingCart
        listOf("رستوران", "غذا").any(n::contains) -> Icons.Filled.Restaurant
        listOf("کافه", "قهوه").any(n::contains) -> Icons.Filled.LocalCafe
        listOf("حمل و نقل", "خودرو", "بنزین", "تاکسی").any(n::contains) -> Icons.Filled.DirectionsCar
        listOf("مسکن", "اجاره", "خانه").any(n::contains) -> Icons.Filled.Home
        listOf("لوازم خانه", "خرید روزانه").any(n::contains) -> Icons.Filled.ShoppingBag
        listOf("قبض", "آب", "برق", "گاز").any(n::contains) -> Icons.Filled.ReceiptLong
        listOf("اینترنت", "تلفن", "موبایل").any(n::contains) -> Icons.Filled.PhoneAndroid
        listOf("درمان", "پزشک", "دارو").any(n::contains) -> Icons.Filled.LocalHospital
        n.contains("بیمه") -> Icons.Filled.HealthAndSafety
        listOf("آرایشی", "بهداشتی").any(n::contains) -> Icons.Filled.Fastfood
        listOf("پوشاک", "لباس").any(n::contains) -> Icons.Filled.Checkroom
        listOf("آموزش", "مدرسه", "دانشگاه").any(n::contains) -> Icons.Filled.School
        listOf("تفریح", "هدیه").any(n::contains) -> Icons.Filled.CardGiftcard
        listOf("سفر", "اقامت").any(n::contains) -> Icons.Filled.Flight
        listOf("اشتراک", "سرویس").any(n::contains) -> Icons.Filled.Subscriptions
        listOf("حقوق", "درآمد", "کار").any(n::contains) -> Icons.Filled.Work
        listOf("کارمزد", "بانک", "انتقال").any(n::contains) -> Icons.Filled.AccountBalanceWallet
        else -> Icons.Filled.Category
    }
}

@Composable
fun CategoryGraphic(name: String, color: Color, size: Dp = 42.dp) {
    val shape = RoundedCornerShape(size * .32f)
    Box(
        modifier = Modifier
            .size(size)
            .background(
                Brush.linearGradient(listOf(color.copy(alpha=.28f), color.copy(alpha=.08f))),
                shape
            )
            .border(1.dp, color.copy(alpha=.42f), shape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = categoryIconFor(name),
            contentDescription = "آیکون $name",
            tint = color,
            modifier = Modifier.size(size * .56f)
        )
    }
}
