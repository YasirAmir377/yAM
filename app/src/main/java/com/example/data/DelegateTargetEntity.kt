package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "delegate_targets")
data class DelegateTargetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val delegateName: String,
    val categoryName: String,
    val dailyTargetWeightKg: Double
)
