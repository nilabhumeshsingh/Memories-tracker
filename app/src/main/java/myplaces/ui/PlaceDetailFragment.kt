package myplaces.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import myplaces.R
import myplaces.data.model.PlaceModel
import myplaces.data.viewmodel.PlaceViewModel
import myplaces.databinding.FragmentPlaceDetailBinding
import myplaces.utils.snackBarMsg
import java.util.Locale

class PlaceDetailFragment : Fragment() {

    private var _binding: FragmentPlaceDetailBinding? = null
    private val binding get() = _binding!!

    private val args by navArgs<PlaceDetailFragmentArgs>()
    private val placeViewModel: PlaceViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaceDetailBinding.inflate(inflater, container, false)
        binding.args = args
        binding.lifecycleOwner = viewLifecycleOwner

        val currentItem = args.currentItem

        setupCategoryVisibility(currentItem)
        setupClickListeners(currentItem)

        return binding.root
    }

    private fun setupCategoryVisibility(currentItem: PlaceModel) {
        if (currentItem.category.isNullOrEmpty() || currentItem.category == getString(R.string.category_none)) {
            binding.tvDetailCategory.visibility = View.GONE
        } else {
            binding.tvDetailCategory.visibility = View.VISIBLE
            binding.tvDetailCategory.text = currentItem.category
        }
    }

    private fun setupClickListeners(currentItem: PlaceModel) {
        // Open in Google Maps
        binding.btnOpenGoogleMaps.setOnClickListener {
            openInGoogleMaps(currentItem)
        }

        // Share text snippet
        binding.btnShare.setOnClickListener {
            sharePlace(currentItem)
        }

        // View on In-App Map
        binding.btnViewOnMap.setOnClickListener {
            val action = PlaceDetailFragmentDirections.actionPlaceDetailFragmentToMapFragment(currentItem)
            findNavController().navigate(action)
        }

        // Edit Place
        binding.btnEdit.setOnClickListener {
            val action = PlaceDetailFragmentDirections.actionPlaceDetailFragmentToAddPlaceFragment(currentItem)
            findNavController().navigate(action)
        }

        // Delete Place
        binding.btnDelete.setOnClickListener {
            confirmDelete(currentItem)
        }
    }

    private fun openInGoogleMaps(place: PlaceModel) {
        val gmmIntentUri = Uri.parse(
            "https://www.google.com/maps/search/?api=1&query=${place.latitude},${place.longitude}"
        )
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
            setPackage("com.google.android.apps.maps")
        }

        if (mapIntent.resolveActivity(requireContext().packageManager) != null) {
            startActivity(mapIntent)
        } else {
            // Fallback to web browser
            val webIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            startActivity(webIntent)
        }
    }

    private fun sharePlace(place: PlaceModel) {
        val shareText = getString(
            R.string.share_place_text,
            place.title,
            place.latitude,
            place.longitude
        )
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, getString(R.string.share_place))
        startActivity(shareIntent)
    }

    private fun confirmDelete(place: PlaceModel) {
        MaterialDialog(requireContext(), BottomSheet(LayoutMode.WRAP_CONTENT)).show {
            icon(R.drawable.ic_delete_forever)
            title(R.string.dialog_delete_forever)
            message(R.string.dialog_delete_confirmation)
            positiveButton(R.string.dialog_confirmation) {
                placeViewModel.deleteItem(place)
                snackBarMsg(requireView(), getString(R.string.snackbar_deleted_forever))
                findNavController().popBackStack()
            }
            negativeButton(R.string.dialog_negative)
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}