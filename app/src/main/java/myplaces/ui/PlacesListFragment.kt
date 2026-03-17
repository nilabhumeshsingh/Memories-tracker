package myplaces.ui

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import myplaces.R
import myplaces.adapters.PlaceAdapter
import myplaces.data.model.PlaceModel
import myplaces.data.viewmodel.PlaceViewModel
import myplaces.databinding.FragmentPlacesListBinding
import myplaces.utils.Constants.SWIPE_DELETE
import myplaces.utils.Constants.SWIPE_EDIT
import myplaces.utils.SwipeItem
import myplaces.utils.snackBarMsg
import jp.wasabeef.recyclerview.animators.LandingAnimator

class PlacesListFragment : Fragment() {

    private var _binding: FragmentPlacesListBinding? = null
    private val binding get() = _binding!!

    private val placeViewModel: PlaceViewModel by activityViewModels()

    private val adapter: PlaceAdapter by lazy { PlaceAdapter() }
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlacesListBinding.inflate(inflater, container, false)
        binding.placeViewModel = placeViewModel
        binding.lifecycleOwner = viewLifecycleOwner

        setupRecyclerView()

        placeViewModel.getDatabase.observe(viewLifecycleOwner) { data ->
            placeViewModel.checkIfPlacesIsEmpty(data)
            adapter.setData(data)
        }

        return binding.root
    }

    private fun setupRecyclerView() {
        recyclerView = binding.placeRecyclerView
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        recyclerView.itemAnimator = LandingAnimator().apply {
            addDuration = 300
        }

        setupSwipeItem(SWIPE_DELETE, recyclerView)
        setupSwipeItem(SWIPE_EDIT, recyclerView)
    }

    private fun setupSwipeItem(action: Int, recyclerView: RecyclerView) {
        lateinit var background: Drawable
        lateinit var icon: Drawable

        when (action) {
            SWIPE_DELETE -> {
                background =
                    ResourcesCompat.getDrawable(resources, R.drawable.bg_swipe_delete, null)!!
                icon = ResourcesCompat.getDrawable(resources, R.drawable.ic_trash, null)!!
            }
            SWIPE_EDIT -> {
                background =
                    ResourcesCompat.getDrawable(resources, R.drawable.bg_swipe_archive, null)!!
                icon = ResourcesCompat.getDrawable(resources, R.drawable.ic_edit_location, null)!!
            }
        }

        val swipeCallBack = object : SwipeItem(action, background, icon, requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, action: Int) {
                if (viewHolder.adapterPosition in adapter.dataList.indices) {
                    val placeItem = adapter.dataList[viewHolder.adapterPosition]
                    when (action) {
                        SWIPE_EDIT -> swipeEdit(placeItem)
                        SWIPE_DELETE -> swipeDelete(placeItem)
                    }
                }
                adapter.notifyDataSetChanged()
            }
        }

        val itemTouchHelper = ItemTouchHelper(swipeCallBack)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun swipeEdit(placeItem: PlaceModel) {
        findNavController().navigate(
            PlacesFragmentDirections.actionPlacesFragmentToAddPlacesFragment(placeItem)
        )
    }

    private fun swipeDelete(placeItem: PlaceModel) {
        MaterialDialog(requireContext(), BottomSheet(LayoutMode.WRAP_CONTENT)).show {
            icon(R.drawable.ic_delete_forever)
            title(R.string.dialog_delete_forever)
            message(R.string.dialog_delete_confirmation)
            positiveButton(R.string.dialog_confirmation) {
                placeViewModel.deleteItem(placeItem)
                snackBarMsg(requireView(), getString(R.string.snackbar_deleted_forever))
            }
            negativeButton(R.string.dialog_negative)
        }
    }

    fun filterData(query: String) {
        if (query.isEmpty()) {
            placeViewModel.getDatabase.value?.let { adapter.setData(it) }
        } else {
            placeViewModel.searchDatabase("%$query%").observe(viewLifecycleOwner) { list ->
                list?.let { adapter.setData(it) }
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
