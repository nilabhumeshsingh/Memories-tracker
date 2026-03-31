package myplaces.utils

import androidx.recyclerview.widget.ItemTouchHelper

/**
 * Application constants.
 */
object Constants {

    const val IMAGE_DIRECTORY = "MyPlacesImages"

    // Permission Request Codes
    const val GALLERY_REQUEST_CODE = 1
    const val CAMERA_REQUEST_CODE = 2
    const val PLACE_REQUEST_CODE = 3

    // Swipe Directions
    const val SWIPE_DELETE = ItemTouchHelper.LEFT
    const val SWIPE_EDIT = ItemTouchHelper.RIGHT
}