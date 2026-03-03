package myplaces.utils

import android.content.Context
import android.location.Address
import android.location.Geocoder
import java.util.Locale

/**
 * Reverse geocoding helper class to resolve Lat/Lng into human-readable addresses.
 */
class AddressLatLong(
    context: Context,
    private val latitude: Double,
    private val longitude: Double
) {
    private var addressListener: AddressListener? = null
    private val geocoder: Geocoder = Geocoder(context, Locale.getDefault())

    fun getAddress(): String {
        var result = ""
        try {
            @Suppress("DEPRECATION")
            val addressList: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)

            if (!addressList.isNullOrEmpty()) {
                val address: Address = addressList[0]
                val sb = StringBuilder()
                for (i in 0..address.maxAddressLineIndex) {
                    sb.append(address.getAddressLine(i)).append(" ")
                }
                if (sb.isNotEmpty()) {
                    sb.deleteCharAt(sb.length - 1)
                }
                result = sb.toString()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (result.isEmpty()) {
            addressListener?.onError()
        } else {
            addressListener?.onAddressFound(result)
        }

        return result
    }

    fun setAddressListener(listener: AddressListener) {
        this.addressListener = listener
    }

    interface AddressListener {
        fun onAddressFound(address: String)
        fun onError()
    }
}