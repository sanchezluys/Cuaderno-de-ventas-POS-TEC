package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.model.Venta
import com.example.data.model.VentaConItems
import kotlinx.coroutines.flow.Flow

@Dao
interface VentaDao {
    @Transaction
    @Query("SELECT * FROM ventas WHERE fechaHora >= :startOfDay AND fechaHora <= :endOfDay ORDER BY fechaHora DESC")
    fun getVentasDelDiaConItems(startOfDay: Long, endOfDay: Long): Flow<List<VentaConItems>>

    @Transaction
    @Query("SELECT * FROM ventas ORDER BY fechaHora DESC")
    fun getAllVentasConItems(): Flow<List<VentaConItems>>

    @Transaction
    @Query("SELECT * FROM ventas WHERE fechaHora >= :startOfDay AND fechaHora <= :endOfDay ORDER BY fechaHora DESC")
    suspend fun getVentasDelDiaConItemsSync(startOfDay: Long, endOfDay: Long): List<VentaConItems>

    @Query("SELECT * FROM ventas WHERE fechaHora >= :startOfDay AND fechaHora <= :endOfDay AND estado = 'ACTIVA' ORDER BY fechaHora DESC, id DESC LIMIT 1")
    suspend fun getLastActiveVentaOfTheDay(startOfDay: Long, endOfDay: Long): Venta?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertVenta(venta: Venta): Long

    @Update
    suspend fun updateVenta(venta: Venta)

    @Query("UPDATE ventas SET estado = 'ANULADA', anuladaEn = :anuladaEn WHERE id = :ventaId")
    suspend fun anularVenta(ventaId: Int, anuladaEn: Long): Int

    @Transaction
    @Query("SELECT * FROM ventas WHERE id = :ventaId LIMIT 1")
    suspend fun getVentaConItemsById(ventaId: Int): VentaConItems?
}
