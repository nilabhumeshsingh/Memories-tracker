package myplaces.adapters

import android.net.Uri
import android.view.View
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.databinding.BindingAdapter
import androidx.lifecycle.MutableLiveData
import androidx.navigation.findNavController
import myplaces.R
import myplaces.data.model.PlaceModel
import myplaces.ui.PlaceDetailFragmentDirections
import myplaces.ui.PlacesFragmentDirections
import com.google.android.material.button.MaterialButton
import java.io.File

/**
 * DataBinding adapters for custom UI view bindings.
 */
class BindingAdapters {

    companion object {

        @JvmStatic
        @BindingAdapter("android:emptyDatabase")
        fun emptyDatabase(view: View, emptyDatabase: MutableLiveData<Boolean>?) {
            view.visibility = if (emptyDatabase?.value == true) View.VISIBLE else View.GONE
        }

        @JvmStatic
        @BindingAdapter("imageUri")
        fun loadImageFromUri(imageView: ImageView, imagePath: String?) {
            if (!imagePath.isNullOrEmpty()) {
                try {
                    val uri = Uri.parse(imagePath)
                    if (uri.scheme != null) {
                        imageView.setImageURI(uri)
                    } else {
                        imageView.setImageURI(Uri.fromFile(File(imagePath)))
                    }
                } catch (e: Exception) {
                    imageView.setImageResource(R.drawable.ic_image)
                }
            } else {
                imageView.setImageResource(R.drawable.ic_image)
            }
        }

        @JvmStatic
        @BindingAdapter("android:sendDataToPlaceDetailFragment")
        fun sendDataToPlaceDetailFragment(view: CardView, currentItem: PlaceModel) {
            view.setOnClickListener {
                val action = PlacesFragmentDirections.actionPlacesFragmentToPlaceDetailFragment(currentItem)
                view.findNavController().navigate(action)
            }
        }

        @JvmStatic
        @BindingAdapter("android:sendDataToMapsFragment")
        fun sendDataToMapsFragment(view: MaterialButton, currentItem: PlaceModel) {
            view.setOnClickListener {
                val action = PlaceDetailFragmentDirections.actionPlaceDetailFragmentToMapFragment(currentItem)
                view.findNavController().navigate(action)
            }
        }
    }
}