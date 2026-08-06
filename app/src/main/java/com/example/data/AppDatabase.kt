package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [CategoryEntity::class, SalesEntryEntity::class, DelegateTargetEntity::class, DelegateAccountEntity::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun salesDao(): SalesDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val REQUIRED_CATEGORIES = listOf(
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

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sales_target_db"
                )
                .fallbackToDestructiveMigration()
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        INSTANCE?.let { database ->
                            CoroutineScope(Dispatchers.IO).launch {
                                database.salesDao().insertCategories(
                                    REQUIRED_CATEGORIES.map { catName ->
                                        CategoryEntity(name = catName, dailyTargetWeightKg = 50.0)
                                    }
                                )
                            }
                        }
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
