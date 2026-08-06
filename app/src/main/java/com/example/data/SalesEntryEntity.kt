package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sales_entries")
data class SalesEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val productName: String,
    val categoryName: String,
    val quantity: Int,
    val pieceWeightKg: Double,
    val totalWeightKg: Double,
    val delegateName: String = "مندوب 1",
    val timestamp: Long = System.currentTimeMillis(),
    val dateString: String
)
