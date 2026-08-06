package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SwitchAccount
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.AdminScreen
import com.example.ui.EntryScreen
import com.example.ui.ReportsScreen
import com.example.ui.SalesViewModel
import com.example.ui.UserAccount
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val salesViewModel: SalesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                // Ensure RTL layout direction for Arabic language interface
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    SalesAppContent(viewModel = salesViewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesAppContent(viewModel: SalesViewModel) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showAccountDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val userMessage by viewModel.userMessage.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val availableAccounts = viewModel.availableAccounts

    LaunchedEffect(userMessage) {
        userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearUserMessage()
        }
    }

    val primaryColor = MaterialTheme.colorScheme.primary

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "نظام المندوب | $selectedDate",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.Black
                        )
                        Text(
                            text = "الحساب الحالي: ${currentUser.name}",
                            fontSize = 12.sp,
                            color = Color.Black.copy(alpha = 0.85f)
                        )
                    }
                },
                actions = {
                    Surface(
                        onClick = { showAccountDialog = true },
                        color = Color.White,
                        shape = RoundedCornerShape(20.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SoftPrimary),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwitchAccount,
                                contentDescription = "تغيير الحساب",
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "تغيير الحساب",
                                color = Color.Black,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = Color.Black
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = Color.Black
            ) {
                // Tab 0: Entry Screen
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.EditNote,
                            contentDescription = "صفحة الإدخال"
                        )
                    },
                    label = { Text("صفحة الإدخال", fontWeight = FontWeight.Bold, color = Color.Black) },
                    modifier = Modifier.testTag("nav_entry_tab"),
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = primaryColor,
                        unselectedIconColor = Color.Black,
                        selectedTextColor = Color.Black,
                        unselectedTextColor = Color.Black,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )

                // Tab 1: Admin Screen (adjacent to Entry Screen)
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = "صفحة الأدمن"
                        )
                    },
                    label = { Text("صفحة الأدمن", fontWeight = FontWeight.Bold, color = Color.Black) },
                    modifier = Modifier.testTag("nav_admin_tab"),
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = primaryColor,
                        unselectedIconColor = Color.Black,
                        selectedTextColor = Color.Black,
                        unselectedTextColor = Color.Black,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )

                // Tab 2: Reports Screen
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Assessment,
                            contentDescription = "صفحة التقارير"
                        )
                    },
                    label = { Text("صفحة التقارير", fontWeight = FontWeight.Bold, color = Color.Black) },
                    modifier = Modifier.testTag("nav_reports_tab"),
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = primaryColor,
                        unselectedIconColor = Color.Black,
                        selectedTextColor = Color.Black,
                        unselectedTextColor = Color.Black,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> EntryScreen(viewModel = viewModel)
                1 -> AdminScreen(viewModel = viewModel)
                2 -> ReportsScreen(viewModel = viewModel)
            }
        }
    }

    // Account Switcher Dialog
    if (showAccountDialog) {
        AlertDialog(
            onDismissRequest = { showAccountDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.SwitchAccount, contentDescription = null, tint = primaryColor)
                    Text("اختر حساب تسجيل الدخول", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "اختر حسابك للبدء بتسجيل المبيعات أو إدارة التاركت:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    availableAccounts.forEach { acc ->
                        val isCurrent = currentUser.name == acc.name
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.loginAccount(acc)
                                    showAccountDialog = false
                                    // Reset tab if non-admin
                                    if (!acc.isAdmin && selectedTab == 2) {
                                        selectedTab = 0
                                    }
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isCurrent) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = if (acc.isAdmin) Icons.Default.AdminPanelSettings else Icons.Default.Person,
                                        contentDescription = null,
                                        tint = if (isCurrent) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Column {
                                        Text(
                                            text = acc.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = acc.roleName,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                if (isCurrent) {
                                    Surface(
                                        color = primaryColor,
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text(
                                            text = "نشط",
                                            fontSize = 10.sp,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAccountDialog = false }) {
                    Text("إلغاء", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

