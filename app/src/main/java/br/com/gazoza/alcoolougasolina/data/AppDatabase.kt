package br.com.gazoza.alcoolougasolina.data

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import br.com.gazoza.alcoolougasolina.domain.Comparison

@Database(
    entities = [Comparison::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun comparisonDao(): ComparisonDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "alcohol_gasoline_database"
                ).allowMainThreadQueries() // Para manter compatibilidade com o código atual
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
