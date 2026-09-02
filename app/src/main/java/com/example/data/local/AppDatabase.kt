package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.data.model.CategoriaCustom
import com.example.data.model.PerfilLocal
import com.example.data.model.Producto
import com.example.data.model.Venta
import com.example.data.model.VentaEstado
import com.example.data.model.VentaItem

class Converters {
    @TypeConverter
    fun fromVentaEstado(value: VentaEstado?): String? = value?.name

    @TypeConverter
    fun toVentaEstado(value: String?): VentaEstado? =
        value?.let { runCatching { VentaEstado.valueOf(it) }.getOrDefault(VentaEstado.ACTIVA) }
}

@Database(
    entities = [
        Producto::class,
        Venta::class,
        VentaItem::class,
        PerfilLocal::class,
        CategoriaCustom::class
    ],
    version = 6,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productoDao(): ProductoDao
    abstract fun ventaDao(): VentaDao
    abstract fun ventaItemDao(): VentaItemDao
    abstract fun perfilLocalDao(): PerfilLocalDao
    abstract fun categoriaDao(): CategoriaDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cuaderno_ventas_database"
                )
                    .fallbackToDestructiveMigrationOnDowngrade(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
