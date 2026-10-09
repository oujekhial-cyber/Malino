package ir.kharjyar.app.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.kharjyar.app.data.db.CategoryEntity
import ir.kharjyar.app.data.db.CategoryRuleEntity
import ir.kharjyar.app.ui.AppViewModel
import ir.kharjyar.app.ui.components.SkinCard
import ir.kharjyar.app.ui.components.CategoryGraphic
import ir.kharjyar.app.core.text.Digits
import androidx.compose.runtime.LaunchedEffect
import ir.kharjyar.app.ui.components.SwipeActionRow
import ir.kharjyar.app.ui.components.EmptyState
import kotlinx.coroutines.launch

private fun normalizedCategoryName(value:String):String = Digits.normalizeForMatch(value)
    .replace('\u200c',' ').trim().replace(Regex("\\s+")," ")

/** مدیریت دسته‌بندی‌ها و قوانین خودکار. */
@OptIn(ExperimentalFoundationApi::class)
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
    var pendingRuleDelete by remember { mutableStateOf<CategoryRuleEntity?>(null) }
    val snackbar = remember { SnackbarHostState() }
    var categoryOrder by remember { mutableStateOf<List<Long>>(emptyList()) }
    var draggingCategoryId by remember { mutableStateOf<Long?>(null) }
    var categoryDragY by remember { mutableFloatStateOf(0f) }
    var categoryDragStart by remember { mutableIntStateOf(-1) }
    var categoryDragTarget by remember { mutableIntStateOf(-1) }
    val categoryHeights = remember { mutableStateMapOf<Long, Float>() }
    val categoryWindowY = remember { mutableStateMapOf<Long, Float>() }
    val categoryListState = rememberLazyListState()
    val categoryEdgePx = with(LocalDensity.current) { 56.dp.toPx() }
    val categoryScrollStepPx = with(LocalDensity.current) { 22.dp.toPx() }
    val categoryWindowHeightPx = with(LocalDensity.current) { LocalConfiguration.current.screenHeightDp.dp.toPx() }
    LaunchedEffect(categories.map { it.id }) {
        if (draggingCategoryId == null) {
            val valid = categoryOrder.filter { id -> categories.any { it.id == id } }
            categoryOrder = valid + categories.map { it.id }.filterNot { it in valid }
        }
    }
    val orderedCategories = categoryOrder.mapNotNull { id -> categories.firstOrNull { it.id == id } } +
        categories.filterNot { it.id in categoryOrder }

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
                            val existing = categories.map { normalizedCategoryName(it.name) }.toSet()
                            var nextOrder=viewModel.repo.categoryDao.nextSortOrder()
                            ir.kharjyar.app.data.db.KharjYarDatabase.DEFAULT_CATEGORIES
                                .filter { normalizedCategoryName(it.first) !in existing }
                                .forEach { (name, color, kind) ->
                                    viewModel.repo.categoryDao.insert(CategoryEntity(name = name, colorArgb = color, kind = kind, builtin = true,sortOrder=nextOrder++))
                                }
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) { Text("دسته‌های پیشنهادی") }
            }
            Text("برای جابه‌جایی، کارت را لمس و نگه دارید و بالا یا پایین ببرید.",style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
            val fallbackCategoryHeightPx = with(LocalDensity.current) { 68.dp.toPx() }
            val categoryGapPx = with(LocalDensity.current) { 8.dp.toPx() }
            LazyColumn(state = categoryListState, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(orderedCategories.size, key = { orderedCategories[it].id }) { index ->
                    val category = orderedCategories[index]
                    val dragging = draggingCategoryId == category.id
                    val averageHeight = categoryHeights.values.average()
                        .takeIf { !it.isNaN() && it > 0 }
                        ?.toFloat() ?: fallbackCategoryHeightPx
                    val step = averageHeight + categoryGapPx
                    val neighborDisplacement = when {
                        draggingCategoryId == null || dragging -> 0f
                        categoryDragStart < categoryDragTarget &&
                            index in (categoryDragStart + 1)..categoryDragTarget -> -step
                        categoryDragTarget < categoryDragStart &&
                            index in categoryDragTarget until categoryDragStart -> step
                        else -> 0f
                    }
                    val neighborY by animateFloatAsState(
                        targetValue = neighborDisplacement,
                        animationSpec = tween(620),
                        label = "categoryNeighborSettleLikeVehicle"
                    )
                    SwipeActionRow(
                        onDelete = { pendingDelete = category },
                        onEdit = { editCategory = category },
                        removeOnDelete = false,
                        enabled = draggingCategoryId == null,
                        modifier = Modifier
                            .animateItemPlacement(animationSpec = tween(760))
                            .onGloballyPositioned { coordinates -> categoryHeights[category.id] = coordinates.size.height.toFloat(); categoryWindowY[category.id] = coordinates.positionInWindow().y }
                            .zIndex(if (dragging) 2f else 0f)
                            .graphicsLayer {
                                translationY = if (dragging) categoryDragY else neighborY
                                scaleX = if (dragging) 1.018f else 1f
                                scaleY = if (dragging) 1.018f else 1f
                                shadowElevation = if (dragging) 18f else 0f
                            }
                            .pointerInput(category.id, orderedCategories.map { it.id }) {
                                detectDragGesturesAfterLongPress(
                                    onDragStart = {
                                        draggingCategoryId = category.id
                                        categoryDragY = 0f
                                        categoryDragStart = orderedCategories.indexOfFirst { it.id == category.id }
                                        categoryDragTarget = categoryDragStart
                                    },
                                    onDrag = { change, amount ->
                                        change.consume()
                                        categoryDragY += amount.y
                                        val fingerWindowY=(categoryWindowY[category.id]?:0f)+change.position.y
                                        val edge=when{fingerWindowY<categoryEdgePx->-1;fingerWindowY>categoryWindowHeightPx-categoryEdgePx->1;else->0}
                                        if(edge!=0)scope.launch{val consumed=categoryListState.scrollBy(edge*categoryScrollStepPx);categoryDragY+=consumed}
                                        val liveStep = (categoryHeights.values.average()
                                            .takeIf { !it.isNaN() && it > 0 }
                                            ?.toFloat() ?: size.height.toFloat()) + categoryGapPx
                                        categoryDragTarget = (categoryDragStart +
                                            kotlin.math.round(categoryDragY / liveStep).toInt())
                                            .coerceIn(0, orderedCategories.lastIndex.coerceAtLeast(0))
                                    },
                                    onDragEnd = {
                                        val ids = orderedCategories.map { it.id }.toMutableList()
                                        val startIndex = categoryDragStart
                                        val targetIndex = categoryDragTarget
                                        if (startIndex in ids.indices && targetIndex in ids.indices && startIndex != targetIndex) {
                                            ids.removeAt(startIndex)
                                            ids.add(targetIndex, category.id)
                                            categoryOrder = ids
                                            scope.launch {
                                                ids.forEachIndexed { position, id ->
                                                    viewModel.repo.categoryDao.setSortOrder(id, position)
                                                }
                                            }
                                        }
                                        draggingCategoryId = null
                                        categoryDragY = 0f
                                        categoryDragStart = -1
                                        categoryDragTarget = -1
                                    },
                                    onDragCancel = {
                                        draggingCategoryId = null
                                        categoryDragY = 0f
                                        categoryDragStart = -1
                                        categoryDragTarget = -1
                                    }
                                )
                            }
                    ) {
                        SkinCard(
                            modifier = Modifier.fillMaxWidth().clickable { editCategory = category }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CategoryGraphic(name = category.name, color = Color(category.colorArgb), size = 42.dp)
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(category.name, style = MaterialTheme.typography.bodyLarge)
                                    Text(
                                        if (category.builtin) "پیشنهادی خرج‌یار" else "ساخته‌شده توسط شما",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (category.archived) Text(
                                    "بایگانی",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Icon(
                                    Icons.Filled.DragHandle,
                                    "جابه‌جایی دسته",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(start = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

        } else {
            Button(onClick = { showNewRule = true }) { Text("قانون جدید") }
            Spacer(Modifier.padding(4.dp))
            if (rules.isEmpty()) {
                EmptyState("قانونی تعریف نشده", "مثال: هر تراکنشی که «کافه» دارد در دسته رستوران و کافه پیشنهاد شود")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(rules.size) { i ->
                        val r = rules[i]
                        val catName = categories.firstOrNull { it.id == r.categoryId }?.name ?: "؟"
                        SwipeActionRow(
                            onDelete = { pendingRuleDelete = r },
                            onEdit = { editRule = r },
                            removeOnDelete = false
                        ) {
                        SkinCard(modifier = Modifier.fillMaxWidth()) {
                            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("«${r.keyword}» ← $catName", style = MaterialTheme.typography.bodyLarge)
                                    Text(
                                        if (r.createdByUser) "قانون ساخته‌شده توسط شما" else "نمونه پیشنهادی متداول در ایران",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(checked = r.enabled, onCheckedChange = { on ->
                                    scope.launch { viewModel.repo.categoryDao.updateRule(r.copy(enabled = on)) }
                                })
                            }
                        }
                        }
                    }
                }
            }
        }
    }
    }

    // ---------- تأیید حذف قانون خودکار ----------
    pendingRuleDelete?.let { rule ->
        val categoryName = categories.firstOrNull { it.id == rule.categoryId }?.name ?: "دسته نامشخص"
        AlertDialog(
            onDismissRequest = { pendingRuleDelete = null },
            title = { Text("حذف قانون خودکار") },
            text = { Text("قانون «${rule.keyword} ← $categoryName» حذف شود؟ پس از حذف، تراکنش‌های جدید دیگر با این قانون دسته‌بندی نمی‌شوند.") },
            confirmButton = {
                TextButton(onClick = {
                    pendingRuleDelete = null
                    deleteRuleWithUndo(rule)
                }) { Text("حذف قانون", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { pendingRuleDelete = null }) { Text("انصراف") } }
        )
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
                val e = editCategory
                val duplicate = categories.any { it.id != e?.id && normalizedCategoryName(it.name) == normalizedCategoryName(name) }
                if (duplicate) {
                    scope.launch { snackbar.showSnackbar("دسته‌ای با این نام از قبل وجود دارد") }
                } else scope.launch {
                    if (e == null) {
                        val inserted = viewModel.repo.categoryDao.insert(CategoryEntity(name = name, colorArgb = color, archived = archived,sortOrder=viewModel.repo.categoryDao.nextSortOrder()))
                        if (inserted == -1L) snackbar.showSnackbar("دسته‌ای با این نام از قبل وجود دارد")
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
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    CategoryGraphic(name = name, color = Color(color), size = 44.dp)
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("نام") }, singleLine = true, modifier = Modifier.weight(1f))
                }
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
