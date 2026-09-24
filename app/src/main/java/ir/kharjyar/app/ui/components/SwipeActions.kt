package ir.kharjyar.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import ir.kharjyar.app.ui.theme.LocalAppSkin

/**
 * ردیفی که با کشیدن انگشت، حذف یا ویرایش می‌شود.
 *
 * - کشیدن به یک سمت (شروع → پایان): حذف، با آیکون سطل زباله در پس‌زمینه.
 * - کشیدن به سمت مخالف: ویرایش، با آیکون مداد در پس‌زمینه. ردیف پس از باز شدن
 *   صفحه ویرایش به جای خودش برمی‌گردد.
 *
 * پس‌زمینه دقیقاً هم‌اندازه خود ردیف است (`matchParentSize` داخل
 * `SwipeToDismissBox`) تا کادر کوچک‌تر/بزرگ‌تر از کارت دیده نشود.
 */
@Composable
fun SwipeActionRow(
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    deleteLabel: String = "حذف",
    editLabel: String = "ویرایش",
    /** آیا حذف باید ردیف را از فهرست بردارد؟ اگر حذف تأییدیه دارد، false بدهید. */
    removeOnDelete: Boolean = true,
    content: @Composable () -> Unit
) {
    val skin = LocalAppSkin.current
    val shape = RoundedCornerShape(skin.cardCorner)

    val state = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    onDelete()
                    removeOnDelete
                }
                SwipeToDismissBoxValue.EndToStart -> {
                    onEdit()
                    false
                }
                SwipeToDismissBoxValue.Settled -> false
            }
        },
        // کمی بیش از یک‌سوم عرض تا تصادفی فعال نشود
        positionalThreshold = { total -> total * 0.38f }
    )

    SwipeToDismissBox(
        state = state,
        modifier = modifier,
        gesturesEnabled = enabled,
        backgroundContent = {
            val deleting = state.dismissDirection != SwipeToDismissBoxValue.EndToStart
            val tint = if (deleting) skin.expenseColor else skin.accent
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(shape)
                    .background(tint.copy(alpha = 0.20f))
                    .padding(horizontal = 20.dp),
                contentAlignment = if (deleting) Alignment.CenterStart else Alignment.CenterEnd
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        if (deleting) Icons.Filled.Delete else Icons.Filled.Edit,
                        contentDescription = null,
                        tint = tint,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (deleting) deleteLabel else editLabel,
                        color = tint,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    ) {
        content()
    }
}
