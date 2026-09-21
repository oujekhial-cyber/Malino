package ir.kharjyar.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import ir.kharjyar.app.core.money.Money
import ir.kharjyar.app.core.text.Digits
import ir.kharjyar.app.core.date.PersianDate
import ir.kharjyar.app.ui.AppViewModel
import ir.kharjyar.app.ui.components.EmptyState

@Composable
fun AccountsScreen(viewModel: AppViewModel, nav: NavHostController) {
    val accounts by viewModel.accounts.collectAsState()
    val settings by viewModel.settings.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { nav.navigate("accountEdit/0") }) {
                Icon(Icons.Filled.Add, contentDescription = "افزودن حساب")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text("مدیریت حساب‌ها", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.padding(4.dp))
            if (accounts.isEmpty()) {
                EmptyState("حسابی معرفی نشده", "برای اتصال پیامک‌های بانکی به حساب، از دکمه + استفاده کنید")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(accounts.size) { i ->
                        val a = accounts[i]
                        Card(modifier = Modifier.fillMaxWidth().clickable { nav.navigate("accountEdit/${a.id}") }) {
                            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(14.dp).background(Color(a.colorArgb), CircleShape))
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(a.title, style = MaterialTheme.typography.titleSmall)
                                        if (a.archived) {
                                            Text("بایگانی", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                    Text(
                                        listOfNotNull(a.bankName.ifBlank { null }, a.maskedNumber.ifBlank { null }?.let { Digits.toPersian(it) })
                                            .joinToString(" — "),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (a.initialBalanceRial != null && a.initialBalanceAt != null) {
                                        Text(
                                            "موجودی اولیه: ${Money.format(a.initialBalanceRial!!, settings.moneyUnit)} (ثبت کاربر، ${PersianDate.formatDateTime(a.initialBalanceAt!!)})",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
