package myplaces.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import myplaces.data.model.PlaceModel

/**
 * Main Room Database singleton instance for My Places application.
 */
@Database(entities = [PlaceModel::class], version = 2, exportSchema = false)
abstract class PlaceDatabase : RoomDatabase() {

    abstract fun placeDao(): PlaceDao

    companion object {
        @Volatile
        private var INSTANCE: PlaceDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE place_table ADD COLUMN category TEXT NOT NULL DEFAULT ''"
                )
            }
        }


        }
    }
}