package ir.kharjyar.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import ir.kharjyar.app.core.date.PersianDate
import ir.kharjyar.app.core.money.Money
import ir.kharjyar.app.core.money.MoneyUnit
import ir.kharjyar.app.core.text.Digits
import ir.kharjyar.app.data.db.TxDirection
import ir.kharjyar.app.data.db.TxNature
import ir.kharjyar.app.ui.AppViewModel
import ir.kharjyar.app.ui.components.AmountTextField
import ir.kharjyar.app.ui.components.keepAboveKeyboard
import kotlinx.coroutines.launch

/** ثبت دستی تراکنش — بدون نیاز به هیچ مجوزی کار می‌کند. */
@Composable
fun ManualEntryScreen(viewModel: AppViewModel, nav: NavHostController) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val settings by viewModel.settings.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val scope = rememberCoroutineScope()

    var accountId by remember { mutableStateOf<Long?>(null) }
    var amountText by remember { mutableStateOf("") }
    var direction by remember { mutableStateOf(TxDirection.WITHDRAW) }
    var nature by remember { mutableStateOf(TxNature.EXPENSE) }
    var categoryId by remember { mutableStateOf<Long?>(null) }
    var description by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(PersianDate.today()) }
    var hour by remember { mutableStateOf(12) }
    var minute by remember { mutableStateOf(0) }
    var error by remember { mutableStateOf<String?>(null) }

    val active = accounts.filter { !it.archived }

    Column(
        modifier = Modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        
        if (active.isEmpty()) {
            Text("ابتدا یک حساب معرفی کنید.", color = MaterialTheme.colorScheme.error)
            Button(onClick = { nav.navigate("accountEdit/0") }) { Text("افزودن حساب") }
        } else {
            AccountPicker(active, accountId) { accountId = it }

            AmountTextField(
                value = amountText,
                onValueChange = { amountText = it },
                label = "مبلغ (${if (settings.moneyUnit == MoneyUnit.TOMAN) "تومان" else "ریال"})",
                supportingText = Money.inputToRial(amountText, settings.moneyUnit)
                    ?.let { Money.format(it, settings.moneyUnit) },
                modifier = Modifier.fillMaxWidth()
            )

            NaturePicker(nature, direction, onNature = { nature = it }, onDirection = { direction = it })
            CategoryPicker(categories, categoryId, nature) { categoryId = it }
            DatePickerRow(date, hour, minute, onDate = { date = it }, onTime = { h, m -> hour = h; minute = m })

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("برای چه بود؟") },
                modifier = Modifier.fillMaxWidth().keepAboveKeyboard()
            )

            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            Button(
                onClick = {
                    val acc = accountId
                    val amount = Money.inputToRial(amountText, settings.moneyUnit)
                    when {
                        acc == null -> error = "حساب را انتخاب کنید"
                        amount == null || amount <= 0 -> error = "مبلغ معتبر وارد کنید"
                        else -> {
                            error = null
                            scope.launch {
                                viewModel.repo.addManualTransaction(
                                    accountId = acc,
                                    amountRial = amount,
                                    direction = direction,
                                    nature = nature,
                                    categoryId = categoryId,
                                    description = description.trim(),
                                    occurredAt = PersianDate.toMillis(date, hour, minute)
                                )
                                ir.kharjyar.app.widget.WidgetUpdater.requestUpdate(context)
                                nav.popBackStack()
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("ثبت") }
        }
    }
}
