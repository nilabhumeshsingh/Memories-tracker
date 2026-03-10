package myplaces.data.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import myplaces.data.db.PlaceDatabase
import myplaces.data.model.PlaceModel
import myplaces.data.repository.PlaceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * ViewModel managing UI-related place data in a lifecycle-conscious way.
 */
class PlaceViewModel(application: Application) : AndroidViewModel(application) {

    private val placeDao = PlaceDatabase.getDatabase(application).placeDao()
    private val repository: PlaceRepository = PlaceRepository(placeDao)

    val getDatabase: LiveData<List<PlaceModel>> = repository.getDatabase
    val emptyDatabase: MutableLiveData<Boolean> = MutableLiveData(false)

    fun insertItem(placeModel: PlaceModel) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertItem(placeModel)
        }
    }

    fun updateItem(placeModel: PlaceModel) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateItem(placeModel)
        }
    }

    fun deleteItem(placeModel: PlaceModel) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteItem(placeModel)
        }
    }

    fun deleteDatabase() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteDatabase()
        }
    }

    fun searchDatabase(searchQuery: String): LiveData<List<PlaceModel>> {
        return repository.searchDatabase(searchQuery)
    }

    fun checkIfPlacesIsEmpty(placeData: List<PlaceModel>) {
        emptyDatabase.value = placeData.isEmpty()
    }
}