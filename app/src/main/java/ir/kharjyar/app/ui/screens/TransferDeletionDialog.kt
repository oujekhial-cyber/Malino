package ir.kharjyar.app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import ir.kharjyar.app.data.db.TransactionEntity

/** Include only active counterparts; the selected rows always remain in the result. */
internal fun withTransferCounterparts(selected: List<TransactionEntity>, all: List<TransactionEntity>): List<TransactionEntity> {
    val groups = selected.mapNotNull { it.transferGroupId }.toSet()
    return (selected + all.filter { it.deletedAt == null && it.transferGroupId != null && it.transferGroupId in groups })
        .distinctBy { it.id }
}

@Composable
internal fun TransferDeletionDialog(
    selected: List<TransactionEntity>, all: List<TransactionEntity>,
    onDismiss: () -> Unit, onDelete: (List<TransactionEntity>) -> Unit
) {
    val both = withTransferCounterparts(selected, all)
    val hasAdditionalSides = both.size > selected.distinctBy { it.id }.size
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("انتقال به سطل زباله") },
        text = { Text(if (hasAdditionalSides)
            "انتقال بین حساب‌ها دو طرف دارد. حذف فقط یک طرف می‌تواند مانده حساب‌ها را نامتوازن کند. کدام را حذف کنیم؟"
            else "تراکنش‌های انتخاب‌شده به سطل زباله منتقل می‌شوند و تا ۳۰ روز قابل بازیابی هستند.") },
        confirmButton = { TextButton(onClick = { onDelete(if (hasAdditionalSides) both else selected) }) {
            Text(if (hasAdditionalSides) "حذف هر دو طرف" else "انتقال به سطل")
        } },
        dismissButton = { Column {
            if (hasAdditionalSides) TextButton(onClick = { onDelete(selected) }) { Text("فقط موارد انتخاب‌شده") }
            TextButton(onClick = onDismiss) { Text("انصراف") }
        } }
    )
}
