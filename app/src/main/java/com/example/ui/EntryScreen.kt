package com.example.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.example.data.DelegateAccountEntity
import com.example.ui.theme.SoftOnPrimaryContainer
import com.example.ui.theme.SoftPrimary
import com.example.ui.theme.SoftPrimaryContainer
import com.example.ui.theme.SuccessGreenSoft

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.CategoryEntity
import com.example.data.SalesEntryEntity
import com.example.ui.theme.HighDensityOutline
import java.util.Locale

// Bright Light Green Color matching the requested user screenshot (#66FF66 / #7CFF70)
val GridHeaderGreen = Color(0xFF7EFF74)

data class GridEntryRow(
    var category: String = "",
    var pieceWeight: String = "",
    var quantity: String = "",
    var productName: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryScreen(
    viewModel: SalesViewModel,
    modifier: Modifier = Modifier
) {
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val savedEntries by viewModel.savedEntries.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val selectedDelegate by viewModel.selectedDelegate.collectAsStateWithLifecycle()
    val delegatesList = viewModel.delegatesList

    // Default 16 categories requested by user
    val requiredCategoryNames = remember(categories) {
        val defaultList = listOf(
            "قشطة", "جبن", "جبن بيتزا", "عجين بيتزا", "عصير", "دوغ", "صلصات", "حلويات",
            "جبن ويلي", "خاثر", "خضراوات وفنكر", "صوصج", "بركر ومقرمش", "دانيت", "حليب بطل", "حليب كارتون"
        )
        (defaultList + categories.map { it.name }).distinctBy { it.trim().lowercase() }
    }

    // Default 10 rows matching the exact image grid layout
    val gridRows = remember {
        mutableStateListOf(
            GridEntryRow(),
            GridEntryRow(),
            GridEntryRow(),
            GridEntryRow(),
            GridEntryRow(),
            GridEntryRow(),
            GridEntryRow(),
            GridEntryRow(),
            GridEntryRow(),
            GridEntryRow()
        )
    }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    val defaultCategory = requiredCategoryNames.firstOrNull() ?: "قشطة"

    val totalSavedWeight = savedEntries.sumOf { it.totalWeightKg }
    val totalSavedQuantity = savedEntries.sumOf { it.quantity }

    val allDelegateAccounts by viewModel.allDelegateAccounts.collectAsStateWithLifecycle()
    val allDelegateTargets by viewModel.allDelegateTargets.collectAsStateWithLifecycle()

    var showDelegateLoginDialog by remember { mutableStateOf(false) }
    var loginUsernameInput by remember { mutableStateOf("") }
    var loginPasswordInput by remember { mutableStateOf("") }
    var loginDialogError by remember { mutableStateOf<String?>(null) }

    // Active delegate account & targets
    val activeDelegateName = if (currentUser.isAdmin) selectedDelegate else currentUser.name
    val activeAccountObj = allDelegateAccounts.find { it.delegateName == activeDelegateName }
    val monthlyTargetKg = activeAccountObj?.monthlyTargetKg ?: 24000.0

    val delCategoryTargets = allDelegateTargets.filter { it.delegateName == activeDelegateName }
    val dailyTargetKg = delCategoryTargets.sumOf { it.dailyTargetWeightKg }.let { if (it > 0) it else 800.0 }
    val dailyPct = if (dailyTargetKg > 0) (totalSavedWeight / dailyTargetKg) * 100.0 else 0.0

    // DELEGATE LOGIN DIALOG
    if (showDelegateLoginDialog) {
        AlertDialog(
            onDismissRequest = { showDelegateLoginDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = SoftPrimary)
                    Text("تسجيل دخول المندوب", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "يرجى إدخال اسم المستخدم والرمز السري المحدد لك من قبل الأدمن:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = loginUsernameInput,
                        onValueChange = {
                            loginUsernameInput = it
                            loginDialogError = null
                        },
                        label = { Text("اسم المستخدم") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = SoftPrimary) },
                        singleLine = true,
                        textStyle = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = loginPasswordInput,
                        onValueChange = {
                            loginPasswordInput = it
                            loginDialogError = null
                        },
                        label = { Text("الرمز السري") },
                        leadingIcon = { Icon(Icons.Default.Key, contentDescription = null, tint = SoftPrimary) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        visualTransformation = PasswordVisualTransformation(),
                        textStyle = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold),
                        modifier = Modifier.fillMaxWidth()
                    )

                    loginDialogError?.let { err ->
                        Text(
                            text = err,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD32F2F)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val u = loginUsernameInput.trim()
                        val p = loginPasswordInput.trim()
                        val matched = allDelegateAccounts.find {
                            (it.username.equals(u, ignoreCase = true) || it.delegateName.equals(u, ignoreCase = true)) &&
                                    it.password == p
                        }
                        if (matched != null) {
                            if (matched.isAdmin) {
                                viewModel.loginAccount(UserAccount("الأدمن", "مدير النظام", true, matched.username, 0.0))
                            } else {
                                viewModel.loginAccount(UserAccount(matched.delegateName, "مندوب مبيعات", false, matched.username, matched.monthlyTargetKg))
                            }
                            showDelegateLoginDialog = false
                            loginUsernameInput = ""
                            loginPasswordInput = ""
                            loginDialogError = null
                        } else {
                            loginDialogError = "اسم المستخدم أو الرمز السري غير صحيح"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SoftPrimary)
                ) {
                    Text("دخول", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDelegateLoginDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ACCOUNT BADGE & LOGIN BUTTON
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "المندوب الحالي:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        Surface(
                            color = SoftPrimary,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = activeDelegateName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Button(
                        onClick = { showDelegateLoginDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = SoftPrimary),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("تسجيل دخول المندوب", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // DELEGATE MONTHLY & DAILY TARGET BANNER
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, SoftPrimary)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = SoftPrimary, modifier = Modifier.size(20.dp))
                            Text(
                                text = "الهدف والـ Target للمندوب ($activeDelegateName)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color.Black
                            )
                        }

                        Surface(
                            color = Color(0xFFD8F3DC),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = String.format(Locale.US, "الشهري: %.0f كجم", monthlyTargetKg),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = Color.Black,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    HorizontalDivider(color = Color.Black.copy(alpha = 0.15f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("التاركت اليومي المطلوب:", fontSize = 11.sp, color = Color.Black.copy(alpha = 0.85f), fontWeight = FontWeight.Bold)
                            Text(
                                text = String.format(Locale.US, "%.1f كجم", dailyTargetKg),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("المبيعات المحققة اليوم:", fontSize = 11.sp, color = Color.Black.copy(alpha = 0.85f), fontWeight = FontWeight.Bold)
                            Text(
                                text = String.format(Locale.US, "%.1f كجم", totalSavedWeight),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.Black
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = String.format(Locale.US, "نسبة الإنجاز: %.1f%%", dailyPct),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )

                        Text(
                            text = if (totalSavedWeight >= dailyTargetKg) "✔️ تم تحقيق الهدف اليومي!" else String.format(Locale.US, "متبقي: %.1f كجم", dailyTargetKg - totalSavedWeight),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }

                    LinearProgressIndicator(
                        progress = { (dailyPct / 100.0).toFloat().coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = SoftPrimary,
                        trackColor = Color(0xFFD8F3DC)
                    )
                }
            }
        }

        // TOP CARD: "مجموع الوزن الكلي لكل الإدخالات"
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, SoftPrimary)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "مجموع وزن إدخالات ($activeDelegateName)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = String.format(Locale.US, "%.2f كجم", totalSavedWeight),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = Color(0xFFE8F5E9),
                            shape = RoundedCornerShape(20.dp),
                            border = androidx.compose.foundation.BorderStroke(0.8.dp, SoftPrimary)
                        ) {
                            Text(
                                text = "إجمالي القطع: $totalSavedQuantity قطعة",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        Surface(
                            color = Color(0xFFE8F5E9),
                            shape = RoundedCornerShape(20.dp),
                            border = androidx.compose.foundation.BorderStroke(0.8.dp, SoftPrimary)
                        ) {
                            Text(
                                text = "عدد السجلات: ${savedEntries.size} منتج",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // Feedback Message Toast/Banner
        item {
            errorMessage?.let { err ->
                Surface(
                    color = Color(0xFF991B1B),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = err,
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(10.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            successMessage?.let { msg ->
                Surface(
                    color = Color(0xFF1B4332),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SoftPrimaryLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = msg,
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(10.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Exact Grid Table matching the User's Image
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, Color.Black)
                    .background(Color.White)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Header Row with Bright Green Background & Black Text
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .background(GridHeaderGreen)
                            .border(0.8.dp, Color.Black),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Col 1 (Right): الصنف
                        Box(
                            modifier = Modifier
                                .weight(1.2f)
                                .fillMaxHeight()
                                .border(0.8.dp, Color.Black),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "الصنف",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                textAlign = TextAlign.Center
                            )
                        }

                        // Col 2: وزن القطعة
                        Box(
                            modifier = Modifier
                                .weight(1.1f)
                                .fillMaxHeight()
                                .border(0.8.dp, Color.Black),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "وزن القطعة",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                textAlign = TextAlign.Center
                            )
                        }

                        // Col 3: عدد القطع
                        Box(
                            modifier = Modifier
                                .weight(1.1f)
                                .fillMaxHeight()
                                .border(0.8.dp, Color.Black),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "عدد القطع",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                textAlign = TextAlign.Center
                            )
                        }

                        // Col 4 (Left): اسم المنتج
                        Box(
                            modifier = Modifier
                                .weight(1.5f)
                                .fillMaxHeight()
                                .border(0.8.dp, Color.Black),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "اسم المنتج",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // Table Data Rows
                    gridRows.forEachIndexed { index, row ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Col 1: Category Picker Cell
                            GridCategoryCell(
                                modifier = Modifier
                                    .weight(1.2f)
                                    .fillMaxHeight(),
                                value = row.category,
                                defaultCategory = defaultCategory,
                                categoryNames = requiredCategoryNames,
                                onValueChange = { selectedCat ->
                                    gridRows[index] = row.copy(category = selectedCat)
                                    errorMessage = null
                                    successMessage = null
                                }
                            )

                            // Col 2: Piece Weight Input Cell
                            GridTextCell(
                                modifier = Modifier
                                    .weight(1.1f)
                                    .fillMaxHeight(),
                                value = row.pieceWeight,
                                onValueChange = { valStr ->
                                    if (valStr.isEmpty() || valStr.matches(Regex("^\\d*\\.?\\d*$"))) {
                                        gridRows[index] = row.copy(pieceWeight = valStr)
                                        errorMessage = null
                                        successMessage = null
                                    }
                                },
                                keyboardType = KeyboardType.Decimal,
                                textAlign = TextAlign.Center,
                                testTag = "row_weight_$index"
                            )

                            // Col 3: Quantity Input Cell
                            GridTextCell(
                                modifier = Modifier
                                    .weight(1.1f)
                                    .fillMaxHeight(),
                                value = row.quantity,
                                onValueChange = { valStr ->
                                    if (valStr.isEmpty() || valStr.all { c -> c.isDigit() }) {
                                        gridRows[index] = row.copy(quantity = valStr)
                                        errorMessage = null
                                        successMessage = null
                                    }
                                },
                                keyboardType = KeyboardType.Number,
                                textAlign = TextAlign.Center,
                                testTag = "row_qty_$index"
                            )

                            // Col 4: Product Name Input Cell
                            GridTextCell(
                                modifier = Modifier
                                    .weight(1.5f)
                                    .fillMaxHeight(),
                                value = row.productName,
                                onValueChange = { valStr ->
                                    gridRows[index] = row.copy(productName = valStr)
                                    errorMessage = null
                                    successMessage = null
                                },
                                keyboardType = KeyboardType.Text,
                                textAlign = TextAlign.End,
                                testTag = "row_name_$index"
                            )
                        }
                    }

                    // Bottom Banner / Save Button ("حفظ الادخال") matching exact image
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .background(GridHeaderGreen)
                            .border(1.dp, Color.Black)
                            .clickable {
                                // Collect all valid filled rows
                                val itemsToSave = mutableListOf<DraftProductItem>()
                                var invalidRowFound = false

                                gridRows.forEachIndexed { i, r ->
                                    val name = r.productName.trim()
                                    val qStr = r.quantity.trim()
                                    val wStr = r.pieceWeight.trim()

                                    if (name.isNotEmpty() || qStr.isNotEmpty() || wStr.isNotEmpty()) {
                                        val q = qStr.toIntOrNull()
                                        val w = wStr.toDoubleOrNull()
                                        val cat = r.category.ifBlank { defaultCategory }

                                        if (name.isEmpty()) {
                                            errorMessage = "يرجى كتابة اسم المنتج في الصف رقم ${i + 1}"
                                            invalidRowFound = true
                                            return@clickable
                                        }
                                        if (q == null || q <= 0) {
                                            errorMessage = "يرجى كتابة عدد قطع صحيح في الصف رقم ${i + 1}"
                                            invalidRowFound = true
                                            return@clickable
                                        }
                                        if (w == null || w <= 0) {
                                            errorMessage = "يرجى كتابة وزن قطعة صحيح في الصف رقم ${i + 1}"
                                            invalidRowFound = true
                                            return@clickable
                                        }

                                        itemsToSave.add(
                                            DraftProductItem(
                                                productName = name,
                                                categoryName = cat,
                                                quantity = q,
                                                pieceWeightKg = w
                                            )
                                        )
                                    }
                                }

                                if (invalidRowFound) return@clickable

                                if (itemsToSave.isEmpty()) {
                                    errorMessage = "يرجى إدخال بيانات منتج واحد على الأقل قبل الحفظ"
                                    return@clickable
                                }

                                // Clear drafts and add new ones to save
                                viewModel.clearDraftItems()
                                itemsToSave.forEach { item ->
                                    viewModel.addDraftItem(
                                        productName = item.productName,
                                        categoryName = item.categoryName,
                                        quantity = item.quantity,
                                        pieceWeightKg = item.pieceWeightKg
                                    )
                                }
                                viewModel.saveDraftEntries()

                                // Reset table back to empty 10 rows
                                for (i in gridRows.indices) {
                                    gridRows[i] = GridEntryRow()
                                }

                                successMessage = "تم حفظ الإدخال بنجاح (${itemsToSave.size} منتجات)"
                                errorMessage = null
                            }
                            .testTag("save_grid_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = "حفظ الادخال",
                                tint = Color.Black,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "حفظ الادخال",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        // Action to add 5 more rows if 10 is not enough
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = {
                        repeat(5) {
                            gridRows.add(GridEntryRow())
                        }
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("إضافة 5 صفوف إضافية", fontSize = 13.sp)
                }

                OutlinedButton(
                    onClick = {
                        for (i in gridRows.indices) {
                            gridRows[i] = GridEntryRow()
                        }
                        errorMessage = null
                        successMessage = null
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("محي الجدول", fontSize = 13.sp, color = MaterialTheme.colorScheme.error)
                }
            }
        }

        // Summary Header for Today's Saved Sales Entries
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "سجل المبيعات المحفوظة اليوم (${savedEntries.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (savedEntries.isNotEmpty()) {
                    Text(
                        text = String.format(Locale.US, "الإجمالي: %.2f كجم", totalSavedWeight),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B5E20)
                    )
                }
            }
        }

        // Saved Sales Table Rows
        if (savedEntries.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, HighDensityOutline)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "لا توجد مبيعات محفوظة اليوم بعد. أدخل المنتجات في الجدول أعلاه واضغط 'حفظ الادخال'.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(savedEntries, key = { it.id }) { entry ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(0.8.dp, Color.Black)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1.5f)) {
                            Text(
                                text = entry.productName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            Text(
                                text = "الصنف: ${entry.categoryName}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.DarkGray
                            )
                        }

                        Text(
                            text = "${entry.quantity} قطعة",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )

                        Text(
                            text = "${entry.pieceWeightKg} كجم",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )

                        Text(
                            text = String.format(Locale.US, "%.2f كجم", entry.totalWeightKg),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1B5E20),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1.2f)
                        )

                        IconButton(
                            onClick = { viewModel.deleteSavedEntry(entry.id) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "حذف السجل",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun GridTextCell(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
    textAlign: TextAlign = TextAlign.Start,
    testTag: String = ""
) {
    Box(
        modifier = modifier
            .border(0.8.dp, Color.Black)
            .background(Color.White)
            .padding(horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(testTag),
            textStyle = MaterialTheme.typography.bodySmall.copy(
                fontSize = 13.sp,
                color = Color.Black,
                textAlign = textAlign,
                fontWeight = FontWeight.Medium
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
        )
    }
}

@Composable
fun GridCategoryCell(
    modifier: Modifier = Modifier,
    value: String,
    defaultCategory: String,
    categoryNames: List<String>,
    onValueChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val displayCategory = value.ifBlank { defaultCategory }

    Box(
        modifier = modifier
            .border(0.8.dp, Color.Black)
            .background(Color.White)
            .clickable { expanded = true }
            .padding(horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = displayCategory,
            fontSize = 13.sp,
            color = Color.Black,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(Color.White)
                .border(1.5.dp, Color(0xFF2D6A4F), RoundedCornerShape(8.dp))
        ) {
            categoryNames.forEach { catName ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = catName,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF082C1D)
                        )
                    },
                    onClick = {
                        onValueChange(catName)
                        expanded = false
                    },
                    colors = MenuDefaults.itemColors(
                        textColor = Color(0xFF082C1D)
                    ),
                    modifier = Modifier.background(Color.White)
                )
            }
        }
    }
}
