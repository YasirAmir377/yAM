package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.CategoryEntity
import com.example.ui.theme.HighDensityOutline
import com.example.ui.theme.HighDensitySurfaceVariant
import com.example.ui.theme.PurpleContainer
import com.example.ui.theme.PurpleOnContainer
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.SoftPrimary
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SuccessGreenContainer
import com.example.ui.theme.SuccessGreenLight
import com.example.ui.theme.SuccessGreenSoft
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.WarningAmberLight
import java.util.Locale

@Composable
fun ReportsScreen(
    viewModel: SalesViewModel,
    modifier: Modifier = Modifier
) {
    val categoryReports by viewModel.categoryReports.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val allDelegateTargets by viewModel.allDelegateTargets.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val selectedDelegate by viewModel.selectedDelegate.collectAsStateWithLifecycle()
    val delegatesList = viewModel.delegatesList

    var showAdminTargetDialog by remember { mutableStateOf(false) }
    var selectedTargetDelegate by remember { mutableStateOf("مندوب 1") }

    val targetInputs = remember { mutableStateMapOf<String, String>() }

    val activeDelegateName = if (currentUser.isAdmin) selectedDelegate else currentUser.name

    val totalSalesWeight = categoryReports.sumOf { it.dailySalesWeightKg }
    val totalTargetWeight = categoryReports.sumOf { it.dailyTargetWeightKg }
    val overallPercentage = if (totalTargetWeight > 0) (totalSalesWeight / totalTargetWeight) * 100.0 else 0.0

    val achievedCategories = remember(categoryReports) {
        categoryReports.filter { it.isAchieved }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // TOP 100% ACHIEVEMENT ALERT BANNER
        if (achievedCategories.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SuccessGreenContainer),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, SuccessGreen)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = SuccessGreenSoft,
                                modifier = Modifier.size(24.dp)
                            )
                            Surface(
                                color = SuccessGreen,
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text(
                                    text = "🏆 إشعار إنجاز الهدف (100%)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                            Text(
                                text = "تم تحقيق ${achievedCategories.size} صنف!",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = SuccessGreenSoft
                            )
                        }

                        Text(
                            text = "تهانينا للمندوب ($activeDelegateName)! لقد تم تحقيق التاركت اليومي بنسبة 100% أو أكثر في الأصناف التالية:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            achievedCategories.take(4).forEach { ach ->
                                Surface(
                                    color = SuccessGreenLight,
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, SuccessGreen)
                                ) {
                                    Text(
                                        text = "✔️ ${ach.categoryName} (${String.format(Locale.US, "%.0f%%", ach.percentage)})",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SuccessGreenSoft,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // High Density Summary Container (Purple Container)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = PurpleContainer),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "مبيعات اليوم ($activeDelegateName)",
                                style = MaterialTheme.typography.labelMedium,
                                color = PurpleOnContainer.copy(alpha = 0.8f),
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = String.format(Locale.US, "%.1f كجم", totalSalesWeight),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = PurpleOnContainer
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "نسبة تحقيق التاركت",
                                style = MaterialTheme.typography.labelMedium,
                                color = PurpleOnContainer.copy(alpha = 0.8f),
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = String.format(Locale.US, "%.0f%%", overallPercentage),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = PurpleOnContainer
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = String.format(Locale.US, "التاركت اليومي المطلوب: %.1f كجم", totalTargetWeight),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = PurpleOnContainer.copy(alpha = 0.9f)
                        )
                    }
                }
            }
        }

        // 12-HOUR AUTO RESET BANNER & MANUAL RESET BUTTON
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = SoftPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = "دورة التقرير الـ 12 ساعة",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "تُصفَّر المبيعات تلقائياً بعد 12 ساعة من الدخول ويظل التاركت اليومي محفوطاً.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = { viewModel.manualReset12HourReport() },
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SoftPrimary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = SoftPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("تصفير الآن", fontSize = 11.sp, color = SoftPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Delegate Switcher Bar (Only visible to Admin)
        if (currentUser.isAdmin) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, HighDensityOutline)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "عرض تقرير المندوب (الأدمن):",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                onClick = { viewModel.setSelectedDelegate("الكل") },
                                color = if (selectedDelegate == "الكل") PurplePrimary else HighDensitySurfaceVariant,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "جميع المندوبين",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedDelegate == "الكل") Color.White else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }

                            delegatesList.take(3).forEach { delName ->
                                Surface(
                                    onClick = { viewModel.setSelectedDelegate(delName) },
                                    color = if (selectedDelegate == delName) PurplePrimary else HighDensitySurfaceVariant,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = delName,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selectedDelegate == delName) Color.White else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            delegatesList.drop(3).forEach { delName ->
                                Surface(
                                    onClick = { viewModel.setSelectedDelegate(delName) },
                                    color = if (selectedDelegate == delName) PurplePrimary else HighDensitySurfaceVariant,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = delName,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selectedDelegate == delName) Color.White else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section Title & Manage Targets Action
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Analytics,
                        contentDescription = null,
                        tint = PurplePrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "جدول الأصناف والتاركت",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (currentUser.isAdmin) {
                    Button(
                        onClick = {
                            selectedTargetDelegate = if (selectedDelegate != "الكل") selectedDelegate else "مندوب 1"
                            showAdminTargetDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("إدارة تاركت الأدمن للمندوبين", fontSize = 12.sp)
                    }
                }
            }
        }

        // High Density Reports Table Container
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, HighDensityOutline)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Table Header
                    Surface(
                        color = HighDensitySurfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "اسم الصنف",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1.5f)
                            )
                            Text(
                                text = "المبيعات",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1.1f)
                            )
                            Text(
                                text = "التاركت",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1.1f)
                            )
                            Text(
                                text = "النسبة %",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.End,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    HorizontalDivider(color = HighDensityOutline)

                    // Table Rows (All 16 categories)
                    if (categoryReports.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "جاري تحميل الأصناف والتاركت...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        categoryReports.forEachIndexed { index, report ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("report_card_${report.categoryName}")
                                    .background(if (report.isAchieved) SuccessGreenContainer.copy(alpha = 0.4f) else Color.Unspecified)
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // Row Content Grid
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1.5f),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = report.categoryName,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        if (report.isAchieved) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = SuccessGreen,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }

                                    Text(
                                        text = String.format(Locale.US, "%.1f كجم", report.dailySalesWeightKg),
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.weight(1.1f)
                                    )

                                    Text(
                                        text = String.format(Locale.US, "%.1f كجم", report.dailyTargetWeightKg),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.weight(1.1f)
                                    )

                                    Text(
                                        text = String.format(Locale.US, "%.0f%%", report.percentage),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (report.isAchieved) SuccessGreenSoft else WarningAmber,
                                        textAlign = TextAlign.End,
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                // Progress Indicator Bar
                                val progressFraction = (report.percentage / 100.0).coerceIn(0.0, 1.0).toFloat()
                                LinearProgressIndicator(
                                    progress = { progressFraction },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = if (report.isAchieved) SuccessGreen else WarningAmber,
                                    trackColor = HighDensitySurfaceVariant
                                )

                                // Target Pill Status
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Start
                                ) {
                                    if (report.isAchieved) {
                                        Surface(
                                            color = SuccessGreenLight,
                                            shape = RoundedCornerShape(4.dp),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, SuccessGreen)
                                        ) {
                                            Text(
                                                text = "🏆 100% تم تحقيق هدف هذا الصنف بنجاح!",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = SuccessGreenSoft,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                            )
                                        }
                                    } else {
                                        Surface(
                                            color = WarningAmberLight,
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = String.format(Locale.US, "❗ المتبقي: %.1f كجم", report.remainingWeightKg),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = WarningAmber,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }

                                if (index < categoryReports.size - 1) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(top = 4.dp),
                                        color = HighDensitySurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // ADMIN TARGET MANAGEMENT DIALOG
    if (showAdminTargetDialog) {
        var dropdownExpanded by remember { mutableStateOf(false) }

        val activeTargets = allDelegateTargets.filter { it.delegateName == selectedTargetDelegate }

        val defaultCategories = listOf(
            "قشطة", "جبن", "جبن بيتزا", "عجين بيتزا", "عصير", "دوغ", "صلصات", "حلويات",
            "جبن ويلي", "خاثر", "خضراوات وفنكر", "صوصج", "بركر ومقرمش", "دانيت", "حليب بطل", "حليب كارتون"
        )

        AlertDialog(
            onDismissRequest = { showAdminTargetDialog = false },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "إدارة أهداف وتاركت المندوبين (الأدمن)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = PurplePrimary
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "حدد المندوب لتحديد وتعديل التاركت لكل صنف من الاصناف الـ 16:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Box(modifier = Modifier.fillMaxWidth()) {
                        Surface(
                            onClick = { dropdownExpanded = true },
                            color = PurpleContainer,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "المندوب المختار: $selectedTargetDelegate",
                                    fontWeight = FontWeight.Bold,
                                    color = PurpleOnContainer
                                )
                                Text("▼", color = PurpleOnContainer)
                            }
                        }

                        DropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false },
                            modifier = Modifier
                                .background(Color.White)
                                .border(1.5.dp, Color(0xFF2D6A4F), RoundedCornerShape(8.dp))
                        ) {
                            delegatesList.forEach { del ->
                                DropdownMenuItem(
                                    text = { Text(del, fontWeight = FontWeight.Bold, color = Color(0xFF082C1D)) },
                                    onClick = {
                                        selectedTargetDelegate = del
                                        dropdownExpanded = false
                                    },
                                    colors = MenuDefaults.itemColors(textColor = Color(0xFF082C1D)),
                                    modifier = Modifier.background(Color.White)
                                )
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    Text(
                        text = "جدول تاركت أصناف المندوب ($selectedTargetDelegate):",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = PurplePrimary
                    )

                    defaultCategories.forEach { catName ->
                        val currentTarget = activeTargets.find {
                            it.categoryName.equals(catName, ignoreCase = true)
                        }?.dailyTargetWeightKg ?: 50.0

                        var textValue by remember(selectedTargetDelegate, catName) {
                            mutableStateOf(currentTarget.toString())
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(HighDensitySurfaceVariant, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = catName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                modifier = Modifier.weight(1.2f)
                            )

                            OutlinedTextField(
                                value = textValue,
                                onValueChange = { valStr ->
                                    textValue = valStr
                                    targetInputs["${selectedTargetDelegate}_$catName"] = valStr
                                },
                                label = { Text("كجم", fontSize = 10.sp) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier
                                    .width(100.dp)
                                    .height(52.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        defaultCategories.forEach { catName ->
                            val inputKey = "${selectedTargetDelegate}_$catName"
                            val valStr = targetInputs[inputKey]
                            val newKg = valStr?.toDoubleOrNull()
                            if (newKg != null && newKg >= 0) {
                                viewModel.updateDelegateTarget(selectedTargetDelegate, catName, newKg)
                            }
                        }
                        showAdminTargetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                ) {
                    Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("حفظ اهداف المندوب")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAdminTargetDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}


