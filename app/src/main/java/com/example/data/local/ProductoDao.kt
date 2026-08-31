package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Producto
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductoDao {
    @Query("SELECT * FROM productos ORDER BY nombre ASC")
    fun getAllProductos(): Flow<List<Producto>>

    @Query("SELECT * FROM productos WHERE activo = 1 ORDER BY nombre ASC")
    fun getActiveProductos(): Flow<List<Producto>>

    @Query("SELECT COUNT(*) FROM productos")
    fun getProductosCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM productos")
    suspend fun getProductosCountSync(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducto(producto: Producto): Long

    @Update
    suspend fun updateProducto(producto: Producto)

    @Query("UPDATE productos SET activo = :activo WHERE id = :id")
    suspend fun setActivo(id: Int, activo: Boolean)

    @Delete
    suspend fun deleteProducto(producto: Producto)

    @Query("SELECT * FROM productos WHERE id = :id LIMIT 1")
    suspend fun getProductoById(id: Int): Producto?
}
