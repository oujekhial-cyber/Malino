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
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import ir.kharjyar.app.ui.components.GlassSnackbarHost
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
    var editRule by remember { mutableStateOf<CategoryRuleEntity?>(null) }
    val snackbar = remember { SnackbarHostState() }

    /** حذف قانون با امکان بازگرداندن. */
    fun deleteRuleWithUndo(rule: CategoryRuleEntity) {
        scope.launch {
            viewModel.repo.categoryDao.deleteRule(rule.id)
            val res = snackbar.showSnackbar(
                message = "قانون «${rule.keyword}» حذف شد",
                actionLabel = "بازگرداندن",
                duration = SnackbarDuration.Short
            )
            // شناسه حفظ می‌شود تا قانون دقیقاً همان‌طور برگردد
            if (res == SnackbarResult.ActionPerformed) viewModel.repo.categoryDao.insertRule(rule)
        }
    }
    // دسته در انتظار تأیید حذف (کشیدن انگشت روی کارت)
    var pendingDelete by remember { mutableStateOf<CategoryEntity?>(null) }
    var pendingDeleteUsage by remember { mutableStateOf(0) }

    LaunchedEffect(pendingDelete?.id) {
        pendingDelete?.let { pendingDeleteUsage = viewModel.repo.categoryDao.usageCount(it.id) }
    }

    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = { GlassSnackbarHost(snackbar) }
    ) { padding ->
    Column(
        modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SkinCard(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("دسته‌بندی هوشمند", style = MaterialTheme.typography.titleLarge)
                Text(
                    "خرج‌ها و درآمدها را مرتب کنید؛ قوانین خودکار دفعه بعد دسته را پیشنهاد می‌دهند.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.padding(3.dp))
                Text(
                    "${Digits.toPersian(categories.count { !it.archived }.toString())} دسته فعال  •  ${Digits.toPersian(rules.count { it.enabled }.toString())} قانون فعال",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        TabRow(selectedTabIndex = tab, containerColor = Color.Transparent, divider = {}) {
            Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("دسته‌ها") })
            Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("قوانین خودکار") })
        }

        if (tab == 0) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { showNewCategory = true }, modifier = Modifier.weight(1f)) { Text("+ دسته جدید") }
                Button(
                    onClick = {
                        scope.launch {
                            val existing = categories.map { it.name }.toSet()
                            ir.kharjyar.app.data.db.KharjYarDatabase.DEFAULT_CATEGORIES
                                .filter { it.first !in existing }
                                .forEach { (name, color, kind) ->
                                    viewModel.repo.categoryDao.insert(CategoryEntity(name = name, colorArgb = color, kind = kind, builtin = true))
                                }
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) { Text("دسته‌های پیشنهادی") }
            }
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
                            Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier.size(38.dp).background(Color(c.colorArgb).copy(alpha = 0.18f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(modifier = Modifier.size(12.dp).background(Color(c.colorArgb), CircleShape))
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(c.name, style = MaterialTheme.typography.bodyLarge)
                                    Text(if (c.builtin) "پیشنهادی خرج‌یار" else "ساخته‌شده توسط شما", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
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
                        SwipeActionRow(
                            onDelete = { deleteRuleWithUndo(r) },
                            onEdit = { editRule = r }
                        ) {
                        SkinCard(modifier = Modifier.fillMaxWidth()) {
                            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("«${r.keyword}» ← $catName", style = MaterialTheme.typography.bodyLarge)
                                }
                                Switch(checked = r.enabled, onCheckedChange = { on ->
                                    scope.launch { viewModel.repo.categoryDao.updateRule(r.copy(enabled = on)) }
                                })
                                TextButton(onClick = { deleteRuleWithUndo(r) }) {
                                    Text("حذف", color = MaterialTheme.colorScheme.error)
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

    if (showNewRule || editRule != null) {
        val existing = editRule
        RuleDialog(
            existing = existing,
            categories = categories.filter { !it.archived },
            onDismiss = { showNewRule = false; editRule = null },
            onSave = { keyword, categoryId ->
                scope.launch {
                    if (existing == null) {
                        viewModel.repo.categoryDao.insertRule(
                            CategoryRuleEntity(keyword = keyword, categoryId = categoryId, priority = 10, createdByUser = true, createdAt = System.currentTimeMillis())
                        )
                    } else {
                        viewModel.repo.categoryDao.updateRule(existing.copy(keyword = keyword, categoryId = categoryId))
                    }
                    showNewRule = false; editRule = null
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
    existing: CategoryRuleEntity?,
    categories: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onSave: (String, Long) -> Unit
) {
    var keyword by remember(existing?.id) { mutableStateOf(existing?.keyword ?: "") }
    var categoryId by remember(existing?.id) { mutableStateOf(existing?.categoryId) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "قانون خودکار جدید" else "ویرایش قانون") },
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
