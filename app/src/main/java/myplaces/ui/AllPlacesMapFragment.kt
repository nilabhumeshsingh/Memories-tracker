package myplaces.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import myplaces.R
import myplaces.data.model.PlaceModel
import myplaces.data.viewmodel.PlaceViewModel
import myplaces.databinding.FragmentAllPlacesMapBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions

class AllPlacesMapFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentAllPlacesMapBinding? = null
    private val binding get() = _binding!!

    private val placeViewModel: PlaceViewModel by activityViewModels()
    private var googleMap: GoogleMap? = null
    private var placesList: List<PlaceModel> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAllPlacesMapBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner

        val mapFragment = childFragmentManager.findFragmentById(R.id.allPlacesMap) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        return binding.root
    }

    override fun onMapReady(map: GoogleMap?) {
        googleMap = map
        googleMap?.uiSettings?.isZoomControlsEnabled = true

        googleMap?.setOnInfoWindowClickListener { marker ->
            val place = marker.tag as? PlaceModel
            place?.let {
                val action = PlacesFragmentDirections.actionPlacesFragmentToPlaceDetailFragment(it)
                findNavController().navigate(action)
            }
        }

        placeViewModel.getDatabase.observe(viewLifecycleOwner) { places ->
            placesList = places
            updateMapMarkers()
        }
    }

    private fun updateMapMarkers() {
        val map = googleMap ?: return
        map.clear()

        if (placesList.isEmpty()) return

        val builder = LatLngBounds.Builder()
        var hasValidBounds = false

        for (place in placesList) {
            val position = LatLng(place.latitude, place.longitude)
            val marker = map.addMarker(
                MarkerOptions()
                    .position(position)
                    .title(place.title)
                    .snippet(if (place.description.isNotEmpty()) place.description else place.location)
            )
            marker?.tag = place
            builder.include(position)
            hasValidBounds = true
        }

        if (hasValidBounds) {
            try {
                if (placesList.size == 1) {
                    val singlePosition = LatLng(placesList[0].latitude, placesList[0].longitude)
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(singlePosition, 14f))
                } else {
                    val bounds = builder.build()
                    val padding = 120 // offset from edges of the map in pixels
                    map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding))
                }
            } catch (e: Exception) {
                // In case map view layout has zero dimensions during animation
                if (placesList.isNotEmpty()) {
                    val pos = LatLng(placesList[0].latitude, placesList[0].longitude)
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 10f))
                }
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
