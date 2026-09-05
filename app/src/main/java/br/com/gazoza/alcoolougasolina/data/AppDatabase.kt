package br.com.gazoza.alcoolougasolina.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import br.com.gazoza.alcoolougasolina.domain.Comparison

@Database(
    entities = [Comparison::class],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun comparisonDao(): ComparisonDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase = instance ?: synchronized(this) {
            val newInstance =
                Room
                    .databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "alcohol_gasoline_database",
                    ).build()
            instance = newInstance
            newInstance
        }
    }
}
