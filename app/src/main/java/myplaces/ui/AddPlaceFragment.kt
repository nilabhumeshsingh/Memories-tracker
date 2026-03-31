package myplaces.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.list.listItems
import myplaces.R
import myplaces.data.model.PlaceModel
import myplaces.data.viewmodel.PlaceViewModel
import myplaces.databinding.FragmentAddPlaceBinding
import myplaces.utils.AddressLatLong
import myplaces.utils.Constants.CAMERA_REQUEST_CODE
import myplaces.utils.Constants.GALLERY_REQUEST_CODE
import myplaces.utils.saveImageToInternalStorage
import myplaces.utils.snackBarMsg
import com.google.android.gms.location.*
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class AddPlaceFragment : Fragment() {

    private var _binding: FragmentAddPlaceBinding? = null
    private val binding get() = _binding!!

    private val placeViewModel: PlaceViewModel by viewModels()
    private val args by navArgs<AddPlaceFragmentArgs>()

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var imageUri: Uri? = null

    private var isEditMode: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddPlaceBinding.inflate(inflater, container, false)
        binding.addPlaceFragment = this
        binding.lifecycleOwner = viewLifecycleOwner

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        setupCategoryDropdown()

        isEditMode = arguments != null && args.currentItem != null

        if (isEditMode) {
            setEditPlace()
        } else {
            setAddPlace()
        }

        onBackPressed()
        return binding.root
    }

    private fun setupCategoryDropdown() {
        val categories = arrayOf(
            getString(R.string.category_none),
            getString(R.string.category_food),
            getString(R.string.category_nature),
            getString(R.string.category_photo_spot),
            getString(R.string.category_landmark),
            getString(R.string.category_nightlife),
            getString(R.string.category_other)
        )
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categories)
        binding.actCategory.setAdapter(adapter)
    }

    private fun setAddPlace() {
        (activity as MainActivity).supportActionBar?.title = getString(R.string.add_place)

        // Auto-fill current Date & Time
        val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        binding.etDate.setText(sdf.format(Date()))

        binding.btnSave.setOnClickListener { savePlace() }

        // Trigger one-time location fetch automatically on open
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            getCurrentLocation()
        } else {
            getLocation()
        }
    }

    private fun setEditPlace() {
        val currentItem = args.currentItem ?: return
        (activity as MainActivity).supportActionBar?.title = getString(R.string.edit_place)

        binding.etTitle.setText(currentItem.title)
        binding.etDescription.setText(currentItem.description)
        binding.etDate.setText(currentItem.date)
        binding.etLocation.setText(currentItem.location)
        binding.etLatitude.setText(currentItem.latitude.toString())
        binding.etLongitude.setText(currentItem.longitude.toString())
        latitude = currentItem.latitude
        longitude = currentItem.longitude

        if (currentItem.category.isNotEmpty()) {
            binding.actCategory.setText(currentItem.category, false)
        }

        if (currentItem.image.isNotEmpty()) {
            imageUri = Uri.parse(currentItem.image)
            try {
                binding.ivPlaceImage.setImageURI(imageUri)
            } catch (e: Exception) {
                binding.ivPlaceImage.setImageResource(R.drawable.ic_image)
            }
        }

        binding.btnSave.text = getString(R.string.edit_place_btn_update)
        binding.btnSave.setOnClickListener { updatePlace() }
    }

    /** ===================================== Permissions & Location ===================================== **/

    fun getCurrentLocation() {
        if (!isLocationEnabled()) {
            snackBarMsg(requireView(), getString(R.string.snackbar_location_off))
            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        } else {
            getLocation()
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = activity?.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return false
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun getLocation() {
        binding.pbGpsLoading.visibility = View.VISIBLE

        Dexter.withContext(requireContext()).withPermissions(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ).withListener(object : MultiplePermissionsListener {

            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                if (report?.areAllPermissionsGranted() == true) {
                    requestLocationData()
                } else {
                    binding.pbGpsLoading.visibility = View.GONE
                }
            }

            override fun onPermissionRationaleShouldBeShown(
                permissions: MutableList<PermissionRequest>?, token: PermissionToken?
            ) {
                binding.pbGpsLoading.visibility = View.GONE
                permissionDeniedDialog()
            }

        }).onSameThread().check()
    }

    @SuppressLint("MissingPermission")
    private fun requestLocationData() {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 1000
            fastestInterval = 500
            numUpdates = 1
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            binding.pbGpsLoading.visibility = View.GONE
            val lastLocation: Location? = locationResult.lastLocation
            if (lastLocation != null) {
                latitude = lastLocation.latitude
                longitude = lastLocation.longitude

                binding.etLatitude.setText(String.format(Locale.US, "%.6f", latitude))
                binding.etLongitude.setText(String.format(Locale.US, "%.6f", longitude))

                // Optional address lookup
                if (binding.etLocation.text.isNullOrEmpty()) {
                    val getAddress = AddressLatLong(requireContext(), latitude, longitude)
                    getAddress.setAddressListener(object : AddressLatLong.AddressListener {
                        override fun onAddressFound(address: String) {
                            binding.etLocation.setText(address)
                        }

                        override fun onError() {
                            // If address reverse lookup fails, standard coordinate string is fine
                        }
                    })
                    getAddress.getAddress()
                }
            } else {
                snackBarMsg(requireView(), getString(R.string.snackbar_gps_failed))
            }
        }
    }

    private fun permissionDeniedDialog() {
        MaterialDialog(requireContext()).show {
            title(R.string.dialog_permission_denied)
            message(R.string.dialog_permission_denied_msg)
            negativeButton(R.string.dialog_cancel)
            positiveButton(R.string.dialog_go_to_settings) {
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    intent.data = Uri.fromParts("package", requireContext().packageName, null)
                    startActivity(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    /** ===================================== Image Picker ===================================== **/

    fun addImage() {
        val actionsItems = listOf(
            getString(R.string.image_from_gallery),
            getString(R.string.image_from_camera)
        )

        MaterialDialog(requireContext(), BottomSheet(LayoutMode.WRAP_CONTENT)).show {
            title(R.string.place_add_image)
            listItems(items = actionsItems) { _, index, _ ->
                when (index) {
                    0 -> pickImageFromGallery()
                    1 -> takePhotoFromCamera()
                }
            }
        }
    }

    private fun pickImageFromGallery() {
        val permissions = mutableListOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        Dexter.withContext(requireContext())
            .withPermissions(permissions)
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    if (report?.areAllPermissionsGranted() == true) {
                        val pickImageIntent = Intent(
                            Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        )
                        startActivityForResult(pickImageIntent, GALLERY_REQUEST_CODE)
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?, token: PermissionToken?
                ) = permissionDeniedDialog()
            }).onSameThread().check()
    }

    private fun takePhotoFromCamera() {
        val permissions = mutableListOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE)
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        Dexter.withContext(requireContext())
            .withPermissions(permissions)
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    if (report?.areAllPermissionsGranted() == true) {
                        val takePhotoIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        startActivityForResult(takePhotoIntent, CAMERA_REQUEST_CODE)
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?, token: PermissionToken?
                ) = permissionDeniedDialog()
            }).onSameThread().check()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && data != null) {
            when (requestCode) {
                GALLERY_REQUEST_CODE -> {
                    data.data?.let { dataUri ->
                        try {
                            @Suppress("DEPRECATION")
                            val pickedBitmap = MediaStore.Images.Media.getBitmap(
                                requireContext().contentResolver, dataUri
                            )
                            imageUri = saveImageToInternalStorage(requireContext(), pickedBitmap)
                            binding.ivPlaceImage.setImageURI(dataUri)
                        } catch (e: IOException) {
                            snackBarMsg(requireView(), e.localizedMessage ?: "Error picking image")
                        }
                    }
                }
                CAMERA_REQUEST_CODE -> {
                    val bitmap = data.extras?.get("data") as? Bitmap
                    if (bitmap != null) {
                        imageUri = saveImageToInternalStorage(requireContext(), bitmap)
                        binding.ivPlaceImage.setImageBitmap(bitmap)
                    }
                }
            }
        }
    }

    /** ===================================== Save / Update ===================================== **/

    private fun parseCoordinates(): Boolean {
        val latStr = binding.etLatitude.text.toString().trim()
        val lngStr = binding.etLongitude.text.toString().trim()

        if (latStr.isNotEmpty() && lngStr.isNotEmpty()) {
            val parsedLat = latStr.toDoubleOrNull()
            val parsedLng = lngStr.toDoubleOrNull()
            if (parsedLat != null && parsedLng != null) {
                latitude = parsedLat
                longitude = parsedLng
                return true
            }
        }
        return false
    }

    private fun savePlace() {
        val title = binding.etTitle.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()
        val date = binding.etDate.text.toString().trim()
        val location = binding.etLocation.text.toString().trim()
        val category = binding.actCategory.text.toString().trim()

        if (title.isEmpty()) {
            snackBarMsg(requireView(), getString(R.string.snackbar_empty_title))
            return
        }

        if (!parseCoordinates()) {
            snackBarMsg(requireView(), getString(R.string.snackbar_invalid_coordinates))
            return
        }

        val item = PlaceModel(
            id = 0,
            title = title,
            description = description,
            date = if (date.isNotEmpty()) date else SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date()),
            location = if (location.isNotEmpty()) location else String.format(Locale.US, "%.6f, %.6f", latitude, longitude),
            latitude = latitude,
            longitude = longitude,
            image = imageUri?.toString() ?: "",
            category = category
        )

        placeViewModel.insertItem(item)
        snackBarMsg(requireView(), getString(R.string.snackbar_add_place))
        findNavController().popBackStack()
    }

    private fun updatePlace() {
        val currentItem = args.currentItem ?: return
        val title = binding.etTitle.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()
        val date = binding.etDate.text.toString().trim()
        val location = binding.etLocation.text.toString().trim()
        val category = binding.actCategory.text.toString().trim()

        if (title.isEmpty()) {
            snackBarMsg(requireView(), getString(R.string.snackbar_empty_title))
            return
        }

        if (!parseCoordinates()) {
            snackBarMsg(requireView(), getString(R.string.snackbar_invalid_coordinates))
            return
        }

        val updatedItem = PlaceModel(
            id = currentItem.id,
            title = title,
            description = description,
            date = date,
            location = location,
            latitude = latitude,
            longitude = longitude,
            image = imageUri?.toString() ?: currentItem.image,
            category = category
        )

        placeViewModel.updateItem(updatedItem)
        snackBarMsg(requireView(), getString(R.string.snackbar_place_update))
        findNavController().popBackStack()
    }

    private fun onBackPressed() {
        activity?.onBackPressedDispatcher?.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    MaterialDialog(requireContext(), BottomSheet(LayoutMode.WRAP_CONTENT)).show {
                        title(R.string.dialog_discard)
                        message(R.string.dialog_discard_confirmation)
                        positiveButton(R.string.dialog_confirmation) {
                            snackBarMsg(requireView(), getString(R.string.snackbar_place_not_saved))
                            findNavController().popBackStack()
                        }
                        negativeButton(R.string.dialog_negative)
                    }
                }
            })
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}