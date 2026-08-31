package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.CategoriaCustom
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoriaDao {
    @Query("SELECT * FROM categorias_custom ORDER BY nombre ASC")
    fun getAllCustomCategorias(): Flow<List<CategoriaCustom>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategoria(categoria: CategoriaCustom): Long

    @Delete
    suspend fun deleteCategoria(categoria: CategoriaCustom)

    @Query("SELECT COUNT(*) FROM categorias_custom WHERE LOWER(nombre) = LOWER(:nombre)")
    suspend fun existsCategoria(nombre: String): Int
}
