package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import com.example.data.DelegateAccountEntity
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.*
import com.example.ui.theme.SuccessGreenSoft
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.IconButton
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.example.ui.theme.WarningAmber
import java.util.Locale

@Composable
fun AdminScreen(
    viewModel: SalesViewModel,
    modifier: Modifier = Modifier
) {
    var isAuthenticated by remember { mutableStateOf(false) }
    var usernameInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var loginError by remember { mutableStateOf<String?>(null) }
    var isPasswordVisible by remember { mutableStateOf(false) }

    val keyboardController = LocalSoftwareKeyboardController.current

    val allDelegateTargets by viewModel.allDelegateTargets.collectAsStateWithLifecycle()
    val allDelegateAccounts by viewModel.allDelegateAccounts.collectAsStateWithLifecycle()
    val rawSavedEntries by viewModel.rawSavedEntries.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val delegatesList = viewModel.delegatesList

    var accountDelNameInput by remember { mutableStateOf("مندوب 1") }
    var accountUsernameInput by remember { mutableStateOf("del1") }
    var accountPasswordInput by remember { mutableStateOf("1234") }
    var accountMonthlyTargetInput by remember { mutableStateOf("24000") }

    var selectedAdminDelegate by remember { mutableStateOf(delegatesList.firstOrNull() ?: "مندوب 1") }

    val defaultCategories = remember {
        listOf(
            "قشطة", "جبن", "جبن بيتزا", "عجين بيتزا", "عصير", "دوغ", "صلصات", "حلويات",
            "جبن ويلي", "خاثر", "خضراوات وفنكر", "صوصج", "بركر ومقرمش", "دانيت", "حليب بطل", "حليب كارتون"
        )
    }

    // State map to store modified target inputs in memory for the currently selected delegate
    val targetInputs = remember { mutableStateMapOf<String, String>() }

    // Fetch active targets for selected delegate
    val activeTargets = allDelegateTargets.filter { it.delegateName == selectedAdminDelegate }

    // Overall metrics across all delegates
    val totalCompanySalesWeight = rawSavedEntries.sumOf { it.totalWeightKg }
    val totalCompanyTargetWeight = allDelegateTargets.sumOf { it.dailyTargetWeightKg }.let { if (it > 0) it else 350.0 * 7 }

    var saveFeedbackMessage by remember { mutableStateOf<String?>(null) }

    if (!isAuthenticated) {
        // ADMIN LOGIN SCREEN
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF142C21)),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, SoftPrimaryLight)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Surface(
                        color = SoftPrimary,
                        shape = RoundedCornerShape(50)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier
                                .padding(16.dp)
                                .size(36.dp)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "تسجيل دخول لوحة الأدمن",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "يرجى إدخال اسم المستخدم والرمز السري للوصول للصفحة",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f),
                            textAlign = TextAlign.Center
                        )
                    }

                    OutlinedTextField(
                        value = usernameInput,
                        onValueChange = {
                            usernameInput = it
                            loginError = null
                        },
                        label = { Text("اسم المستخدم", color = Color.White) },
                        placeholder = { Text("admin", color = Color.White.copy(alpha = 0.6f)) },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = Color.White)
                        },
                        singleLine = true,
                        textStyle = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedLabelColor = Color.White,
                            unfocusedLabelColor = Color.White.copy(alpha = 0.8f),
                            focusedLeadingIconColor = Color.White,
                            unfocusedLeadingIconColor = Color.White,
                            focusedContainerColor = Color(0xFF1F3E30),
                            unfocusedContainerColor = Color(0xFF1F3E30),
                            focusedBorderColor = SoftPrimaryLight,
                            unfocusedBorderColor = SoftDarkOutline
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = {
                            passwordInput = it
                            loginError = null
                        },
                        label = { Text("الرمز السري (كلمة المرور)", color = Color.White) },
                        placeholder = { Text("1234", color = Color.White.copy(alpha = 0.6f)) },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Key, contentDescription = null, tint = Color.White)
                        },
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                            }
                        },
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        textStyle = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedLabelColor = Color.White,
                            unfocusedLabelColor = Color.White.copy(alpha = 0.8f),
                            focusedLeadingIconColor = Color.White,
                            unfocusedLeadingIconColor = Color.White,
                            focusedTrailingIconColor = Color.White,
                            unfocusedTrailingIconColor = Color.White,
                            focusedContainerColor = Color(0xFF1F3E30),
                            unfocusedContainerColor = Color(0xFF1F3E30),
                            focusedBorderColor = SoftPrimaryLight,
                            unfocusedBorderColor = SoftDarkOutline
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    loginError?.let { err ->
                        Surface(
                            color = Color(0xFF991B1B),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = err,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }

                    Button(
                        onClick = {
                            keyboardController?.hide()
                            val u = usernameInput.trim()
                            val p = passwordInput.trim()

                            val matchedAccount = viewModel.allDelegateAccounts.value.find {
                                (it.username.equals(u, ignoreCase = true) || it.delegateName.equals(u, ignoreCase = true)) &&
                                        it.password == p
                            } ?: if ((u.equals("admin", ignoreCase = true) || u == "1234") && (p == "1234" || p == "admin")) {
                                DelegateAccountEntity("admin", "1234", "الأدمن", 0.0, true)
                            } else null

                            if (matchedAccount != null) {
                                if (matchedAccount.isAdmin) {
                                    isAuthenticated = true
                                    loginError = null
                                    viewModel.loginAccount(UserAccount("الأدمن", "مدير النظام", true, matchedAccount.username, 0.0))
                                } else {
                                    // Logged in as delegate
                                    viewModel.loginAccount(UserAccount(matchedAccount.delegateName, "مندوب مبيعات", false, matchedAccount.username, matchedAccount.monthlyTargetKg))
                                    saveFeedbackMessage = "تم تسجيل الدخول بنجاح كـ (${matchedAccount.delegateName})"
                                    isAuthenticated = true
                                }
                            } else {
                                loginError = "اسم المستخدم أو الرمز السري غير صحيح"
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SoftPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.LockOpen, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("تسجيل الدخول", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // TOP ADMIN BANNER WITH LOCK BUTTON
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF142C21)),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, SoftPrimaryLight)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "صفحة الأدمن | إدارة التاركت والأهداف",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Button(
                            onClick = {
                                isAuthenticated = false
                                passwordInput = ""
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SoftPrimary),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("قفل الصفحة", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.2f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "إجمالي مبيعات المندوبين اليوم",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                            Text(
                                text = String.format(Locale.US, "%.1f كجم", totalCompanySalesWeight),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "إجمالي التاركت المطلوب",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                            Text(
                                text = String.format(Locale.US, "%.1f كجم", totalCompanyTargetWeight),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // DELEGATE PICKER BAR
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF142C21)),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SoftDarkOutline)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "1. اختر المندوب لتحديد أهدافه وتاركت الأصناف:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        delegatesList.take(4).forEach { delName ->
                            val isSelected = selectedAdminDelegate == delName
                            Surface(
                                onClick = {
                                    selectedAdminDelegate = delName
                                    saveFeedbackMessage = null
                                },
                                color = if (isSelected) SoftPrimary else Color(0xFF1F3E30),
                                shape = RoundedCornerShape(8.dp),
                                border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, SoftDarkOutline)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = delName,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        delegatesList.drop(4).forEach { delName ->
                            val isSelected = selectedAdminDelegate == delName
                            Surface(
                                onClick = {
                                    selectedAdminDelegate = delName
                                    saveFeedbackMessage = null
                                },
                                color = if (isSelected) SoftPrimary else Color(0xFF1F3E30),
                                shape = RoundedCornerShape(8.dp),
                                border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, SoftDarkOutline)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = delName,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Feedback Banner
        saveFeedbackMessage?.let { msg ->
            item {
                Surface(
                    color = Color(0xFF1B4332),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SoftPrimaryLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = msg,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        }

        // CATEGORIES & TARGETS TABLE FOR SELECTED DELEGATE
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF142C21)),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SoftDarkOutline)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "2. تحديد التاركت اليومي لأصناف ($selectedAdminDelegate):",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Button(
                            onClick = {
                                var count = 0
                                defaultCategories.forEach { catName ->
                                    val key = "${selectedAdminDelegate}_$catName"
                                    val inputVal = targetInputs[key]
                                    val targetKg = inputVal?.toDoubleOrNull()
                                    if (targetKg != null && targetKg >= 0) {
                                        viewModel.updateDelegateTarget(selectedAdminDelegate, catName, targetKg)
                                        count++
                                    }
                                }
                                saveFeedbackMessage = "✔️ تم حفظ أهداف التاركت لـ $selectedAdminDelegate بنجاح وسوف تظهر للمندوب فوراً!"
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SoftPrimary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Save, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("حفظ التاركت", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.2f))

                    // Header row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1F3E30), RoundedCornerShape(6.dp))
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("#", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(28.dp), color = Color.White)
                        Text("اسم الصنف", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.3f), color = Color.White)
                        Text("التاركت اليومي (كجم)", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.2f), textAlign = TextAlign.Center, color = Color.White)
                        Text("مبيعات اليوم", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.End, color = Color.White)
                    }

                    // 16 Categories ordered
                    defaultCategories.forEachIndexed { index, catName ->
                        val currentTargetObj = activeTargets.find { it.categoryName.equals(catName, ignoreCase = true) }
                        val dbTargetKg = currentTargetObj?.dailyTargetWeightKg ?: 50.0

                        val key = "${selectedAdminDelegate}_$catName"
                        var textValue by remember(selectedAdminDelegate, catName, dbTargetKg) {
                            mutableStateOf(targetInputs[key] ?: String.format(Locale.US, "%.1f", dbTargetKg))
                        }

                        // Sales today for this delegate and category
                        val delSalesToday = rawSavedEntries
                            .filter { it.delegateName == selectedAdminDelegate && it.categoryName.equals(catName, ignoreCase = true) }
                            .sumOf { it.totalWeightKg }

                        val isAchieved = delSalesToday >= dbTargetKg && dbTargetKg > 0

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (index % 2 == 0) Color(0xFF142C21) else Color(0xFF18382B), RoundedCornerShape(6.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${index + 1}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.width(28.dp)
                            )

                            Column(modifier = Modifier.weight(1.3f)) {
                                Text(
                                    text = catName,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                if (isAchieved) {
                                    Text(
                                        text = "✔️ مُحقق 100%",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF86EFAC)
                                    )
                                }
                            }

                            // OutlinedTextField for Target Weight
                            OutlinedTextField(
                                value = textValue,
                                onValueChange = { newStr ->
                                    textValue = newStr
                                    targetInputs[key] = newStr
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                textStyle = TextStyle(
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    textAlign = TextAlign.Center
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = SoftPrimaryLight,
                                    unfocusedBorderColor = SoftDarkOutline,
                                    focusedContainerColor = Color(0xFF0B1E16),
                                    unfocusedContainerColor = Color(0xFF0B1E16)
                                ),
                                modifier = Modifier
                                    .weight(1.2f)
                                    .height(50.dp)
                                    .padding(horizontal = 4.dp)
                            )

                            Text(
                                text = String.format(Locale.US, "%.1f كجم", delSalesToday),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                textAlign = TextAlign.End,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = {
                            var count = 0
                            defaultCategories.forEach { catName ->
                                val key = "${selectedAdminDelegate}_$catName"
                                val inputVal = targetInputs[key]
                                val targetKg = inputVal?.toDoubleOrNull()
                                if (targetKg != null && targetKg >= 0) {
                                    viewModel.updateDelegateTarget(selectedAdminDelegate, catName, targetKg)
                                    count++
                                }
                            }
                            saveFeedbackMessage = "✔️ تم حفظ جميع أهداف $selectedAdminDelegate بنجاح وتحديثها للمندوبين!"
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = SoftPrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Save, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("حفظ أهداف التاركت لـ ($selectedAdminDelegate)", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }

        // DELEGATE CREDENTIALS AND USERNAME / PASSWORD MANAGEMENT CARD
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF142C21)),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, SoftPrimaryLight)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Key, contentDescription = null, tint = Color.White)
                        Text(
                            text = "3. تحديد اسم المستخدم والرمز السري وتاركت كل مندوب:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Text(
                        text = "يقوم الأدمن بتحديد بيانات الدخول والتاركت لكل مندوب، ويستخدم المندوب هذه البيانات للتسجيل والعمل.",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.85f)
                    )

                    HorizontalDivider(color = Color.White.copy(alpha = 0.2f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = accountDelNameInput,
                            onValueChange = { accountDelNameInput = it },
                            label = { Text("اسم المندوب", color = Color.White) },
                            placeholder = { Text("مثال: مندوب 1", color = Color.White.copy(alpha = 0.6f)) },
                            singleLine = true,
                            textStyle = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedLabelColor = Color.White,
                                unfocusedLabelColor = Color.White.copy(alpha = 0.8f),
                                focusedContainerColor = Color(0xFF1F3E30),
                                unfocusedContainerColor = Color(0xFF1F3E30),
                                focusedBorderColor = SoftPrimaryLight,
                                unfocusedBorderColor = SoftDarkOutline
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = accountUsernameInput,
                            onValueChange = { accountUsernameInput = it },
                            label = { Text("اسم المستخدم", color = Color.White) },
                            placeholder = { Text("del1", color = Color.White.copy(alpha = 0.6f)) },
                            singleLine = true,
                            textStyle = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedLabelColor = Color.White,
                                unfocusedLabelColor = Color.White.copy(alpha = 0.8f),
                                focusedContainerColor = Color(0xFF1F3E30),
                                unfocusedContainerColor = Color(0xFF1F3E30),
                                focusedBorderColor = SoftPrimaryLight,
                                unfocusedBorderColor = SoftDarkOutline
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = accountPasswordInput,
                            onValueChange = { accountPasswordInput = it },
                            label = { Text("الرمز السري", color = Color.White) },
                            placeholder = { Text("1234", color = Color.White.copy(alpha = 0.6f)) },
                            singleLine = true,
                            textStyle = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedLabelColor = Color.White,
                                unfocusedLabelColor = Color.White.copy(alpha = 0.8f),
                                focusedContainerColor = Color(0xFF1F3E30),
                                unfocusedContainerColor = Color(0xFF1F3E30),
                                focusedBorderColor = SoftPrimaryLight,
                                unfocusedBorderColor = SoftDarkOutline
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = accountMonthlyTargetInput,
                            onValueChange = { accountMonthlyTargetInput = it },
                            label = { Text("التاركت الشهري (كجم)", color = Color.White) },
                            placeholder = { Text("24000", color = Color.White.copy(alpha = 0.6f)) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            textStyle = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedLabelColor = Color.White,
                                unfocusedLabelColor = Color.White.copy(alpha = 0.8f),
                                focusedContainerColor = Color(0xFF1F3E30),
                                unfocusedContainerColor = Color(0xFF1F3E30),
                                focusedBorderColor = SoftPrimaryLight,
                                unfocusedBorderColor = SoftDarkOutline
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Button(
                        onClick = {
                            val delName = accountDelNameInput.trim()
                            val u = accountUsernameInput.trim()
                            val p = accountPasswordInput.trim()
                            val targetVal = accountMonthlyTargetInput.toDoubleOrNull() ?: 1500.0

                            if (delName.isNotEmpty() && u.isNotEmpty() && p.isNotEmpty()) {
                                viewModel.saveDelegateAccount(
                                    DelegateAccountEntity(
                                        username = u,
                                        password = p,
                                        delegateName = delName,
                                        monthlyTargetKg = targetVal,
                                        isAdmin = false
                                    )
                                )
                                saveFeedbackMessage = "✔️ تم حفظ بيانات الدخول والتاركت الشهري لـ ($delName) بنجاح!"
                            } else {
                                saveFeedbackMessage = "❌ يرجى تعبئة جميع الحقول المطلوبة"
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SoftPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Save, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("حفظ بيانات الدخول والتاركت للمندوب", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "قائمة حسابات المندوبين المسجلة حالياً:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color.White
                    )

                    allDelegateAccounts.filter { !it.isAdmin }.forEach { acc ->
                        Surface(
                            color = Color(0xFF1F3E30),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        text = "${acc.delegateName} (اسم المستخدم: ${acc.username})",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "الرمز السري: ${acc.password} | التاركت الشهري: ${acc.monthlyTargetKg} كجم",
                                        fontSize = 11.sp,
                                        color = Color.White.copy(alpha = 0.85f)
                                    )
                                }

                                Button(
                                    onClick = {
                                        accountDelNameInput = acc.delegateName
                                        accountUsernameInput = acc.username
                                        accountPasswordInput = acc.password
                                        accountMonthlyTargetInput = acc.monthlyTargetKg.toInt().toString()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = SoftPrimary),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("تعديل", fontSize = 11.sp, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }

        // OVERVIEW CARDS FOR ALL 7 DELEGATES
        item {
            Text(
                text = "4. ملخص أداء ومبيعات الـ 7 مندوبين اليوم:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        items(delegatesList) { delName ->
            val delSales = rawSavedEntries.filter { it.delegateName == delName }.sumOf { it.totalWeightKg }
            val delTargets = allDelegateTargets.filter { it.delegateName == delName }
            val totalTargetKg = delTargets.sumOf { it.dailyTargetWeightKg }.let { if (it > 0) it else 800.0 }
            val pct = if (totalTargetKg > 0) (delSales / totalTargetKg) * 100.0 else 0.0

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF142C21)
                ),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SoftDarkOutline)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = delName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                        }

                        Surface(
                            onClick = {
                                selectedAdminDelegate = delName
                                saveFeedbackMessage = null
                            },
                            color = SoftPrimary,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "تعديل التاركت",
                                fontSize = 11.sp,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = String.format(Locale.US, "المبيعات: %.1f كجم", delSales),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )

                        Text(
                            text = String.format(Locale.US, "التاركت: %.1f كجم", totalTargetKg),
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )

                        Text(
                            text = String.format(Locale.US, "%.0f%%", pct),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    LinearProgressIndicator(
                        progress = { (pct / 100.0).toFloat().coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp),
                        color = if (pct >= 100) Color(0xFF86EFAC) else SoftPrimaryLight,
                        trackColor = Color(0xFF1F3E30)
                    )
                }
            }
        }
    }
}

