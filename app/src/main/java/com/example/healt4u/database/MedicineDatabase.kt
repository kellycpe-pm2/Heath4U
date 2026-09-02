package com.example.healt4u.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.healt4u.model.NPRAMedicine

@Database(
    entities = [NPRAMedicine::class],
    version = 1,
    exportSchema = false
)
abstract class MedicineDatabase : RoomDatabase() {

    abstract fun medicineDao(): MedicineDao
}
