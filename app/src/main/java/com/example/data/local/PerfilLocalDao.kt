package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.PerfilLocal
import kotlinx.coroutines.flow.Flow

@Dao
interface PerfilLocalDao {
    @Query("SELECT * FROM perfil_local WHERE id = 1 LIMIT 1")
    fun getPerfil(): Flow<PerfilLocal?>

    @Query("SELECT * FROM perfil_local WHERE id = 1 LIMIT 1")
    suspend fun getPerfilSync(): PerfilLocal?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePerfil(perfil: PerfilLocal)
}
