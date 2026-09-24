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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.kharjyar.app.data.db.CategoryEntity
import ir.kharjyar.app.data.db.CategoryRuleEntity
import ir.kharjyar.app.ui.AppViewModel
import ir.kharjyar.app.ui.components.SkinCard
import ir.kharjyar.app.core.text.Digits
import androidx.compose.runtime.LaunchedEffect
import ir.kharjyar.app.ui.components.SwipeActionRow
import ir.kharjyar.app.ui.components.EmptyState
import kotlinx.coroutines.launch

/** مدیریت دسته‌بندی‌ها و قوانین خودکار. */
@Composable
fun CategoriesScreen(viewModel: AppViewModel) {
    val scope = rememberCoroutineScope()
    val categories by viewModel.categories.collectAsState()
    val rules by remember { viewModel.repo.categoryDao.observeRules() }.collectAsStateWithLifecycle(initialValue = emptyList())
    var tab by remember { mutableStateOf(0) }
    var editCategory by remember { mutableStateOf<CategoryEntity?>(null) }
    var showNewCategory by remember { mutableStateOf(false) }
    var showNewRule by remember { mutableStateOf(false) }
    // دسته در انتظار تأیید حذف (کشیدن انگشت روی کارت)
    var pendingDelete by remember { mutableStateOf<CategoryEntity?>(null) }
    var pendingDeleteUsage by remember { mutableStateOf(0) }

    LaunchedEffect(pendingDelete?.id) {
        pendingDelete?.let { pendingDeleteUsage = viewModel.repo.categoryDao.usageCount(it.id) }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Spacer(Modifier.padding(4.dp))
        TabRow(selectedTabIndex = tab) {
            Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("دسته‌ها") })
            Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("قوانین خودکار") })
        }
        Spacer(Modifier.padding(6.dp))

        if (tab == 0) {
            Button(onClick = { showNewCategory = true }) { Text("دسته جدید") }
            Spacer(Modifier.padding(4.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories.size) { i ->
                    val c = categories[i]
                    SwipeActionRow(
                        onDelete = { pendingDelete = c },
                        onEdit = { editCategory = c },
                        // حذف دسته تأییدیه دارد، پس ردیف بلافاصله برداشته نمی‌شود
                        removeOnDelete = false
                    ) {
                        SkinCard(modifier = Modifier.fillMaxWidth().clickable { editCategory = c }) {
                            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(14.dp).background(Color(c.colorArgb), CircleShape))
                                Spacer(Modifier.width(12.dp))
                                Text(c.name, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                                if (c.archived) Text("بایگانی", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        } else {
            Button(onClick = { showNewRule = true }) { Text("قانون جدید") }
            Spacer(Modifier.padding(4.dp))
            if (rules.isEmpty()) {
                EmptyState("قانونی تعریف نشده", "مثال: هر تراکنشی که «اسنپ» دارد در دسته حمل‌ونقل پیشنهاد شود")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(rules.size) { i ->
                        val r = rules[i]
                        val catName = categories.firstOrNull { it.id == r.categoryId }?.name ?: "؟"
                        SkinCard(modifier = Modifier.fillMaxWidth()) {
                            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("«${r.keyword}» ← $catName", style = MaterialTheme.typography.bodyLarge)
                                }
                                Switch(checked = r.enabled, onCheckedChange = { on ->
                                    scope.launch { viewModel.repo.categoryDao.updateRule(r.copy(enabled = on)) }
                                })
                                TextButton(onClick = { scope.launch { viewModel.repo.categoryDao.deleteRule(r.id) } }) {
                                    Text("حذف", color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // ---------- تأیید حذف دسته ----------
    pendingDelete?.let { cat ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("حذف دسته") },
            text = {
                Text(
                    if (pendingDeleteUsage > 0)
                        "«${cat.name}» در ${Digits.toPersian(pendingDeleteUsage.toString())} تراکنش استفاده شده است. با حذف، آن تراکنش‌ها بدون دسته می‌شوند (خود تراکنش‌ها پاک نمی‌شوند)."
                    else "«${cat.name}» حذف شود؟"
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        viewModel.repo.categoryDao.detachTransactions(cat.id)
                        viewModel.repo.categoryDao.deleteRulesOfCategory(cat.id)
                        viewModel.repo.categoryDao.delete(cat.id)
                        pendingDelete = null
                    }
                }) { Text("حذف", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = {
                        scope.launch {
                            viewModel.repo.categoryDao.update(cat.copy(archived = true))
                            pendingDelete = null
                        }
                    }) { Text("بایگانی") }
                    TextButton(onClick = { pendingDelete = null }) { Text("انصراف") }
                }
            }
        )
    }

    if (showNewCategory || editCategory != null) {
        CategoryDialog(
            existing = editCategory,
            onDismiss = { showNewCategory = false; editCategory = null },
            onSave = { name, color, archived ->
                scope.launch {
                    val e = editCategory
                    if (e == null) {
                        viewModel.repo.categoryDao.insert(CategoryEntity(name = name, colorArgb = color, archived = archived))
                    } else {
                        viewModel.repo.categoryDao.update(e.copy(name = name, colorArgb = color, archived = archived))
                    }
                    showNewCategory = false; editCategory = null
                }
            }
        )
    }

    if (showNewRule) {
        RuleDialog(
            categories = categories.filter { !it.archived },
            onDismiss = { showNewRule = false },
            onSave = { keyword, categoryId ->
                scope.launch {
                    viewModel.repo.categoryDao.insertRule(
                        CategoryRuleEntity(keyword = keyword, categoryId = categoryId, priority = 10, createdByUser = true, createdAt = System.currentTimeMillis())
                    )
                    showNewRule = false
                }
            }
        )
    }
}

private val categoryColors = listOf(
    0xFF4CAF50, 0xFFFF7043, 0xFF29B6F6, 0xFF8D6E63, 0xFFFFA726, 0xFF7E57C2,
    0xFFEF5350, 0xFFEC407A, 0xFF26A69A, 0xFFFFCA28, 0xFF5C6BC0, 0xFFAB47BC
)

@Composable
private fun CategoryDialog(
    existing: CategoryEntity?,
    onDismiss: () -> Unit,
    onSave: (String, Long, Boolean) -> Unit
) {
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var color by remember { mutableStateOf(existing?.colorArgb ?: categoryColors[0]) }
    var archived by remember { mutableStateOf(existing?.archived ?: false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "دسته جدید" else "ویرایش دسته") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("نام") }, singleLine = true)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    categoryColors.take(6).forEach { c ->
                        Box(Modifier.size(32.dp).background(Color(c), CircleShape).clickable { color = c })
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    categoryColors.drop(6).forEach { c ->
                        Box(Modifier.size(32.dp).background(Color(c), CircleShape).clickable { color = c })
                    }
                }
                if (existing != null) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("بایگانی")
                        Switch(checked = archived, onCheckedChange = { archived = it })
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { if (name.isNotBlank()) onSave(name.trim(), color, archived) }) { Text("ذخیره") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("انصراف") } }
    )
}

@Composable
private fun RuleDialog(
    categories: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onSave: (String, Long) -> Unit
) {
    var keyword by remember { mutableStateOf("") }
    var categoryId by remember { mutableStateOf<Long?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("قانون خودکار جدید") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = keyword, onValueChange = { keyword = it }, label = { Text("کلیدواژه (مثل «اسنپ»)") }, singleLine = true)
                Text("دسته:", style = MaterialTheme.typography.labelLarge)
                androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(categories.size) { i ->
                        androidx.compose.material3.FilterChip(
                            selected = categoryId == categories[i].id,
                            onClick = { categoryId = categories[i].id },
                            label = { Text(categories[i].name) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val cid = categoryId
                if (keyword.isNotBlank() && cid != null) onSave(keyword.trim(), cid)
            }) { Text("ذخیره") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("انصراف") } }
    )
}
