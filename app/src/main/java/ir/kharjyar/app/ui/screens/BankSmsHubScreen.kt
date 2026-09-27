package ir.kharjyar.app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.weight
import androidx.compose.material3.TabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import ir.kharjyar.app.ui.AppViewModel

/** مرکز واحد پیامک‌های بانکی؛ پیش‌فرض همیشه صف بررسی‌نشده‌ها است. */
@Composable
fun BankSmsHubScreen(
    viewModel: AppViewModel,
    nav: NavHostController,
    initialTab: Int = 0
) {
    var selectedTab by rememberSaveable(initialTab) { mutableIntStateOf(initialTab.coerceIn(0, 1)) }
    Column(Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("پیامک‌های بررسی‌نشده") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("ورود پیامک‌های بانکی") }
            )
        }
        androidx.compose.foundation.layout.Box(Modifier.weight(1f)) {
            if (selectedTab == 0) ReviewScreen(viewModel, nav)
            else SmsHistoryImportScreen(viewModel, nav)
        }
    }
}
