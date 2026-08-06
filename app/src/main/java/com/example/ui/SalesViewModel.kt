package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.CategoryEntity
import com.example.data.DelegateAccountEntity
import com.example.data.DelegateTargetEntity
import com.example.data.SalesEntryEntity
import com.example.data.SalesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class UserAccount(
    val name: String,
    val roleName: String,
    val isAdmin: Boolean,
    val username: String = "",
    val monthlyTargetKg: Double = 1500.0
)

data class DraftProductItem(
    val id: String = UUID.randomUUID().toString(),
    val productName: String,
    val categoryName: String,
    val quantity: Int,
    val pieceWeightKg: Double
) {
    val totalWeightKg: Double get() = quantity * pieceWeightKg
}

data class CategoryReportItem(
    val categoryId: Int,
    val categoryName: String,
    val dailySalesWeightKg: Double,
    val dailyTargetWeightKg: Double
) {
    val percentage: Double
        get() = if (dailyTargetWeightKg > 0) (dailySalesWeightKg / dailyTargetWeightKg) * 100.0 else 0.0

    val isAchieved: Boolean
        get() = percentage >= 100.0

    val remainingWeightKg: Double
        get() = if (dailyTargetWeightKg > dailySalesWeightKg) dailyTargetWeightKg - dailySalesWeightKg else 0.0
}

class SalesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SalesRepository

    val todayDateString: String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    private val _selectedDate = MutableStateFlow(todayDateString)
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    private val _loginTimestamp = MutableStateFlow(System.currentTimeMillis())
    val loginTimestamp: StateFlow<Long> = _loginTimestamp.asStateFlow()

    val allDelegateAccounts: StateFlow<List<DelegateAccountEntity>>

    val availableAccounts = listOf(
        UserAccount("الأدمن", "مدير النظام", true, "admin", 0.0),
        UserAccount("مندوب 1", "مندوب مبيعات", false, "del1", 1500.0),
        UserAccount("مندوب 2", "مندوب مبيعات", false, "del2", 1500.0),
        UserAccount("مندوب 3", "مندوب مبيعات", false, "del3", 1500.0),
        UserAccount("مندوب 4", "مندوب مبيعات", false, "del4", 1500.0),
        UserAccount("مندوب 5", "مندوب مبيعات", false, "del5", 1500.0),
        UserAccount("مندوب 6", "مندوب مبيعات", false, "del6", 1500.0),
        UserAccount("مندوب 7", "مندوب مبيعات", false, "del7", 1500.0)
    )

    val delegatesList = availableAccounts.filter { !it.isAdmin }.map { it.name }

    private val _currentUser = MutableStateFlow<UserAccount>(availableAccounts[1]) // Default to "مندوب 1"
    val currentUser: StateFlow<UserAccount> = _currentUser.asStateFlow()

    private val _selectedDelegate = MutableStateFlow("مندوب 1")
    val selectedDelegate: StateFlow<String> = _selectedDelegate.asStateFlow()

    private val _draftItems = MutableStateFlow<List<DraftProductItem>>(emptyList())
    val draftItems: StateFlow<List<DraftProductItem>> = _draftItems.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    val categories: StateFlow<List<CategoryEntity>>
    val allDelegateTargets: StateFlow<List<DelegateTargetEntity>>
    val rawSavedEntries: StateFlow<List<SalesEntryEntity>>
    val savedEntries: StateFlow<List<SalesEntryEntity>>
    val categoryReports: StateFlow<List<CategoryReportItem>>

    companion object {
        const val TWELVE_HOURS_MS = 12 * 60 * 60 * 1000L
    }

    init {
        val salesDao = AppDatabase.getDatabase(application).salesDao()
        repository = SalesRepository(salesDao)

        viewModelScope.launch {
            repository.ensureDefaultCategories()
            clean12HourExpiredEntries()
        }

        allDelegateAccounts = repository.allDelegateAccounts.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        categories = repository.allCategories.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allDelegateTargets = repository.allDelegateTargets.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
        rawSavedEntries = _selectedDate.flatMapLatest { date ->
            repository.getSalesEntriesForDate(date)
        }.combine(_loginTimestamp) { entries, loginTime ->
            // Filter entries created within the last 12 hours from current time or login timestamp
            val now = System.currentTimeMillis()
            val cutoff = now - TWELVE_HOURS_MS
            entries.filter { it.timestamp >= cutoff }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Filter saved entries for current logged in user (or selected delegate for admin)
        savedEntries = combine(rawSavedEntries, currentUser, selectedDelegate) { entries, user, activeDel ->
            if (user.isAdmin) {
                if (activeDel == "الكل") entries else entries.filter { it.delegateName == activeDel }
            } else {
                entries.filter { it.delegateName == user.name }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        categoryReports = combine(categories, rawSavedEntries, allDelegateTargets, currentUser, selectedDelegate) { catList, entries, delTargets, user, activeDelegate ->
            val effectiveDelegate = if (!user.isAdmin) user.name else activeDelegate

            val filteredEntries = if (effectiveDelegate == "الكل") {
                entries
            } else {
                entries.filter { it.delegateName == effectiveDelegate }
            }

            val defaultCategories = listOf(
                "قشطة", "جبن", "جبن بيتزا", "عجين بيتزا", "عصير", "دوغ", "صلصات", "حلويات",
                "جبن ويلي", "خاثر", "خضراوات وفنكر", "صوصج", "بركر ومقرمش", "دانيت", "حليب بطل", "حليب كارتون"
            )

            val allCatNames = (defaultCategories + catList.map { it.name.trim() }).distinctBy { it.lowercase() }

            allCatNames.mapIndexed { index, displayName ->
                val salesWeightForCategory = filteredEntries
                    .filter { it.categoryName.trim().equals(displayName, ignoreCase = true) }
                    .sumOf { it.totalWeightKg }

                val targetKg = if (effectiveDelegate != "الكل") {
                    delTargets.find {
                        it.delegateName == effectiveDelegate && it.categoryName.equals(displayName, ignoreCase = true)
                    }?.dailyTargetWeightKg ?: 50.0
                } else {
                    delTargets.filter {
                        it.categoryName.equals(displayName, ignoreCase = true)
                    }.sumOf { it.dailyTargetWeightKg }.let { if (it > 0) it else 350.0 }
                }

                CategoryReportItem(
                    categoryId = index + 1,
                    categoryName = displayName,
                    dailySalesWeightKg = salesWeightForCategory,
                    dailyTargetWeightKg = targetKg
                )
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    private fun clean12HourExpiredEntries() {
        viewModelScope.launch {
            val cutoff = System.currentTimeMillis() - TWELVE_HOURS_MS
            repository.clearSalesEntriesOlderThan(cutoff)
        }
    }

    fun loginAccount(account: UserAccount) {
        _currentUser.value = account
        _loginTimestamp.value = System.currentTimeMillis()
        if (!account.isAdmin) {
            _selectedDelegate.value = account.name
        }
        clean12HourExpiredEntries()
        _userMessage.value = "تم تسجيل الدخول كـ: ${account.name} (بدأت دورة التقرير الـ 12 ساعة)"
    }

    fun manualReset12HourReport() {
        viewModelScope.launch {
            val user = _currentUser.value
            val activeDel = if (user.isAdmin) _selectedDelegate.value else user.name
            if (activeDel == "الكل" || user.isAdmin) {
                repository.clearAllSalesEntries()
                _userMessage.value = "تم تصفير جميع تقارير المبيعات مع الإبقاء على التاركت اليومي"
            } else {
                repository.clearSalesEntriesForDelegate(activeDel)
                _userMessage.value = "تم تصفير تقرير المبيعات لـ ($activeDel) مع الإبقاء على التاركت اليومي"
            }
            _loginTimestamp.value = System.currentTimeMillis()
        }
    }

    fun setSelectedDelegate(delegateName: String) {
        _selectedDelegate.value = delegateName
    }

    fun addDraftItem(productName: String, categoryName: String, quantity: Int, pieceWeightKg: Double) {
        val item = DraftProductItem(
            productName = productName.trim(),
            categoryName = categoryName.trim(),
            quantity = quantity,
            pieceWeightKg = pieceWeightKg
        )
        _draftItems.value = _draftItems.value + item
    }

    fun removeDraftItem(id: String) {
        _draftItems.value = _draftItems.value.filter { it.id != id }
    }

    fun clearDraftItems() {
        _draftItems.value = emptyList()
    }

    fun saveDraftEntries() {
        val currentDrafts = _draftItems.value
        if (currentDrafts.isEmpty()) return

        viewModelScope.launch {
            val dateStr = _selectedDate.value
            val user = _currentUser.value
            val activeDel = if (user.isAdmin) _selectedDelegate.value else user.name
            val entries = currentDrafts.map { item ->
                SalesEntryEntity(
                    productName = item.productName,
                    categoryName = item.categoryName,
                    quantity = item.quantity,
                    pieceWeightKg = item.pieceWeightKg,
                    totalWeightKg = item.totalWeightKg,
                    delegateName = activeDel,
                    dateString = dateStr
                )
            }
            repository.saveSalesEntries(entries)
            _draftItems.value = emptyList()
            _userMessage.value = "تم حفظ الإدخال للمندوب ($activeDel) بنجاح (${entries.size} منتجات)"
        }
    }

    fun updateDelegateTarget(delegateName: String, categoryName: String, targetKg: Double) {
        viewModelScope.launch {
            repository.updateDelegateCategoryTarget(delegateName, categoryName, targetKg)
            _userMessage.value = "تم تحديث هدف صنف $categoryName للمندوب $delegateName"
        }
    }

    fun deleteSavedEntry(id: Int) {
        viewModelScope.launch {
            repository.deleteSalesEntry(id)
            _userMessage.value = "تم حذف المنتج المسجل"
        }
    }

    fun addCategory(name: String, targetWeightKg: Double) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.saveCategory(
                CategoryEntity(name = name.trim(), dailyTargetWeightKg = targetWeightKg)
            )
            _userMessage.value = "تم إضافة الصنف $name بنجاح"
        }
    }

    fun updateCategoryTarget(category: CategoryEntity, newTargetWeightKg: Double) {
        viewModelScope.launch {
            repository.updateCategory(
                category.copy(dailyTargetWeightKg = newTargetWeightKg)
            )
            _userMessage.value = "تم تحديث التاركت اليومي لصنف ${category.name}"
        }
    }

    fun deleteCategory(id: Int) {
        viewModelScope.launch {
            repository.deleteCategory(id)
            _userMessage.value = "تم حذف الصنف"
        }
    }

    fun saveDelegateAccount(account: DelegateAccountEntity) {
        viewModelScope.launch {
            repository.saveDelegateAccount(account)
            _userMessage.value = "تم حفظ بيانات المندوب (${account.delegateName}) بنجاح"
        }
    }

    fun deleteDelegateAccount(username: String) {
        viewModelScope.launch {
            repository.deleteDelegateAccount(username)
            _userMessage.value = "تم حذف حساب المندوب"
        }
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    fun setSelectedDate(dateStr: String) {
        _selectedDate.value = dateStr
    }
}
