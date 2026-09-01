package com.example.healt4u.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.healt4u.model.NPRAMedicine
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicineDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllMedicines(medicines: List<NPRAMedicine>)

    @Query("SELECT * FROM npra_medicines WHERE regNo = :regNo")
    suspend fun getMedicineByRegNo(regNo: String): NPRAMedicine?

    @Query("SELECT * FROM npra_medicines WHERE product LIKE '%' || :query || '%' OR genericName LIKE '%' || :query || '%'")
    suspend fun searchMedicines(query: String): List<NPRAMedicine>

    @Query("SELECT * FROM npra_medicines ORDER BY product ASC")
    fun getAllMedicines(): Flow<List<NPRAMedicine>>

    @Query("SELECT COUNT(*) FROM npra_medicines")
    suspend fun getCount(): Int

    @Query("DELETE FROM npra_medicines")
    suspend fun clearAll()
}
