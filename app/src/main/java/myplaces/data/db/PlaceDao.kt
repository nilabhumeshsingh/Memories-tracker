package myplaces.data.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import myplaces.data.model.PlaceModel

/**
 * Data Access Object (DAO) for place_table SQLite database operations.
 */
@Dao
interface PlaceDao {

    @Query("SELECT * FROM place_table ORDER BY id DESC")
    fun getDatabase(): LiveData<List<PlaceModel>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertItem(placeModel: PlaceModel)

    @Update
    suspend fun updateItem(placeModel: PlaceModel)

    @Delete
    suspend fun deleteItem(placeModel: PlaceModel)

    @Query("DELETE FROM place_table")
    suspend fun deleteDatabase()

    @Query("SELECT * FROM place_table WHERE title LIKE :searchQuery OR description LIKE :searchQuery ORDER BY id DESC")
    fun searchDatabase(searchQuery: String): LiveData<List<PlaceModel>>
}