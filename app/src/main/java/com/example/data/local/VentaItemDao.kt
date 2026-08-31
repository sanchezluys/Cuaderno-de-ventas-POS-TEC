package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.VentaItem

@Dao
interface VentaItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<VentaItem>)

    @Query("SELECT * FROM venta_items WHERE ventaId = :ventaId")
    suspend fun getItemsForVenta(ventaId: Int): List<VentaItem>
}
