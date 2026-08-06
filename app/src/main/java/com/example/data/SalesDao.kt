package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SalesDao {
    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)

    @Update
    suspend fun updateCategory(category: CategoryEntity)

    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun deleteCategoryById(id: Int)

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun getCategoryCount(): Int

    @Query("SELECT * FROM sales_entries WHERE dateString = :date ORDER BY timestamp DESC")
    fun getSalesEntriesByDate(date: String): Flow<List<SalesEntryEntity>>

    @Query("SELECT * FROM sales_entries ORDER BY timestamp DESC")
    fun getAllSalesEntries(): Flow<List<SalesEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSalesEntry(entry: SalesEntryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSalesEntries(entries: List<SalesEntryEntity>)

    @Query("DELETE FROM sales_entries WHERE id = :id")
    suspend fun deleteSalesEntryById(id: Int)

    @Query("DELETE FROM sales_entries WHERE dateString = :date")
    suspend fun clearSalesEntriesByDate(date: String)

    @Query("DELETE FROM sales_entries WHERE timestamp < :cutoffTimestamp")
    suspend fun clearSalesEntriesOlderThan(cutoffTimestamp: Long)

    @Query("DELETE FROM sales_entries WHERE delegateName = :delegateName")
    suspend fun clearSalesEntriesForDelegate(delegateName: String)

    @Query("DELETE FROM sales_entries")
    suspend fun clearAllSalesEntries()

    @Query("SELECT * FROM delegate_targets ORDER BY delegateName ASC, categoryName ASC")
    fun getAllDelegateTargets(): Flow<List<DelegateTargetEntity>>

    @Query("SELECT * FROM delegate_targets WHERE delegateName = :delegateName ORDER BY categoryName ASC")
    fun getDelegateTargetsForDelegate(delegateName: String): Flow<List<DelegateTargetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDelegateTargets(targets: List<DelegateTargetEntity>)

    @Query("SELECT COUNT(*) FROM delegate_targets")
    suspend fun getDelegateTargetsCount(): Int

    @Query("UPDATE delegate_targets SET dailyTargetWeightKg = :targetKg WHERE delegateName = :delegateName AND categoryName = :categoryName")
    suspend fun updateDelegateCategoryTarget(delegateName: String, categoryName: String, targetKg: Double)

    @Query("SELECT * FROM delegate_accounts ORDER BY delegateName ASC")
    fun getAllDelegateAccounts(): Flow<List<DelegateAccountEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDelegateAccount(account: DelegateAccountEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDelegateAccounts(accounts: List<DelegateAccountEntity>)

    @Query("DELETE FROM delegate_accounts WHERE username = :username")
    suspend fun deleteDelegateAccountByUsername(username: String)

    @Query("SELECT COUNT(*) FROM delegate_accounts")
    suspend fun getDelegateAccountsCount(): Int
}
