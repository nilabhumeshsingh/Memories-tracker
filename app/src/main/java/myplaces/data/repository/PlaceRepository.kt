package myplaces.data.repository

import androidx.lifecycle.LiveData
import myplaces.data.db.PlaceDao
import myplaces.data.model.PlaceModel

/**
 * Repository layer connecting Room DAO to ViewModels.
 */
class PlaceRepository(private val placeDao: PlaceDao) {

    val getDatabase: LiveData<List<PlaceModel>> = placeDao.getDatabase()

    suspend fun insertItem(placeModel: PlaceModel) {
        placeDao.insertItem(placeModel)
    }

    suspend fun updateItem(placeModel: PlaceModel) {
        placeDao.updateItem(placeModel)
    }

    suspend fun deleteItem(placeModel: PlaceModel) {
        placeDao.deleteItem(placeModel)
    }

    suspend fun deleteDatabase() {
        placeDao.deleteDatabase()
    }

    fun searchDatabase(searchQuery: String): LiveData<List<PlaceModel>> {
        return placeDao.searchDatabase(searchQuery)
    }
}