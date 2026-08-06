package com.example.data

import kotlinx.coroutines.flow.Flow

class SalesRepository(private val salesDao: SalesDao) {

    val allCategories: Flow<List<CategoryEntity>> = salesDao.getAllCategories()

    val allDelegateTargets: Flow<List<DelegateTargetEntity>> = salesDao.getAllDelegateTargets()

    fun getDelegateTargetsForDelegate(delegateName: String): Flow<List<DelegateTargetEntity>> =
        salesDao.getDelegateTargetsForDelegate(delegateName)

    fun getSalesEntriesForDate(date: String): Flow<List<SalesEntryEntity>> =
        salesDao.getSalesEntriesByDate(date)

    suspend fun saveSalesEntries(entries: List<SalesEntryEntity>) {
        salesDao.insertSalesEntries(entries)
    }

    suspend fun saveCategory(category: CategoryEntity) {
        salesDao.insertCategory(category)
    }

    suspend fun updateCategory(category: CategoryEntity) {
        salesDao.updateCategory(category)
    }

    suspend fun updateDelegateCategoryTarget(delegateName: String, categoryName: String, targetKg: Double) {
        salesDao.updateDelegateCategoryTarget(delegateName, categoryName, targetKg)
    }

    suspend fun deleteCategory(id: Int) {
        salesDao.deleteCategoryById(id)
    }

    suspend fun deleteSalesEntry(id: Int) {
        salesDao.deleteSalesEntryById(id)
    }

    suspend fun clearEntriesForDate(date: String) {
        salesDao.clearSalesEntriesByDate(date)
    }

    suspend fun clearSalesEntriesOlderThan(cutoffTimestamp: Long) {
        salesDao.clearSalesEntriesOlderThan(cutoffTimestamp)
    }

    suspend fun clearSalesEntriesForDelegate(delegateName: String) {
        salesDao.clearSalesEntriesForDelegate(delegateName)
    }

    suspend fun clearAllSalesEntries() {
        salesDao.clearAllSalesEntries()
    }

    val allDelegateAccounts: Flow<List<DelegateAccountEntity>> = salesDao.getAllDelegateAccounts()

    suspend fun saveDelegateAccount(account: DelegateAccountEntity) {
        salesDao.insertDelegateAccount(account)
    }

    suspend fun deleteDelegateAccount(username: String) {
        salesDao.deleteDelegateAccountByUsername(username)
    }

    suspend fun ensureDefaultCategories() {
        val requiredCategories = listOf(
            "قشطة",
            "جبن",
            "جبن بيتزا",
            "عجين بيتزا",
            "عصير",
            "دوغ",
            "صلصات",
            "حلويات",
            "جبن ويلي",
            "خاثر",
            "خضراوات وفنكر",
            "صوصج",
            "بركر ومقرمش",
            "دانيت",
            "حليب بطل",
            "حليب كارتون"
        )

        val currentCategories = salesDao.getCategoryCount()
        if (currentCategories == 0) {
            salesDao.insertCategories(
                requiredCategories.map { catName ->
                    CategoryEntity(name = catName, dailyTargetWeightKg = 50.0)
                }
            )
        }

        // Initialize default delegate accounts if empty
        if (salesDao.getDelegateAccountsCount() == 0) {
            val defaultAccounts = listOf(
                DelegateAccountEntity("admin", "1234", "الأدمن", 0.0, isAdmin = true),
                DelegateAccountEntity("del1", "1234", "مندوب 1", 1500.0, isAdmin = false),
                DelegateAccountEntity("del2", "1234", "مندوب 2", 1500.0, isAdmin = false),
                DelegateAccountEntity("del3", "1234", "مندوب 3", 1500.0, isAdmin = false),
                DelegateAccountEntity("del4", "1234", "مندوب 4", 1500.0, isAdmin = false),
                DelegateAccountEntity("del5", "1234", "مندوب 5", 1500.0, isAdmin = false),
                DelegateAccountEntity("del6", "1234", "مندوب 6", 1500.0, isAdmin = false),
                DelegateAccountEntity("del7", "1234", "مندوب 7", 1500.0, isAdmin = false)
            )
            salesDao.insertDelegateAccounts(defaultAccounts)
        }

        // Initialize 7 delegates with targets for each category if targets table is empty
        val defaultDelegates = listOf("مندوب 1", "مندوب 2", "مندوب 3", "مندوب 4", "مندوب 5", "مندوب 6", "مندوب 7")
        if (salesDao.getDelegateTargetsCount() == 0) {
            val targetsToInsert = mutableListOf<DelegateTargetEntity>()
            for (delegate in defaultDelegates) {
                for (catName in requiredCategories) {
                    targetsToInsert.add(
                        DelegateTargetEntity(
                            delegateName = delegate,
                            categoryName = catName,
                            dailyTargetWeightKg = 50.0
                        )
                    )
                }
            }
            salesDao.insertDelegateTargets(targetsToInsert)
        }
    }
}
