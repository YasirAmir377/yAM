package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "delegate_accounts")
data class DelegateAccountEntity(
    @PrimaryKey
    val username: String,
    val password: String,
    val delegateName: String,
    val monthlyTargetKg: Double = 1500.0,
    val isAdmin: Boolean = false
)
