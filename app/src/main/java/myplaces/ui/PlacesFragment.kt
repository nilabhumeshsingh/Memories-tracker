package myplaces.ui

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import myplaces.R
import myplaces.data.viewmodel.PlaceViewModel
import myplaces.databinding.FragmentPlacesBinding
import myplaces.utils.snackBarMsg
import com.google.android.material.tabs.TabLayoutMediator

class PlacesFragment : Fragment(), SearchView.OnQueryTextListener {

    private var _binding: FragmentPlacesBinding? = null
    private val binding get() = _binding!!

    private val placeViewModel: PlaceViewModel by activityViewModels()
    private var placesListFragment: PlacesListFragment? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlacesBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner

        setupViewPager()

        binding.fabPlaces.setOnClickListener {
            findNavController().navigate(R.id.action_placesFragment_to_addPlacesFragment)
        }

        setHasOptionsMenu(true)
        return binding.root
    }

    private fun setupViewPager() {
        val adapter = PlacesPagerAdapter(this)
        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.tab_list)
                1 -> getString(R.string.tab_map)
                else -> ""
            }
        }.attach()
    }

    private inner class PlacesPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> {
                    val listFragment = PlacesListFragment()
                    placesListFragment = listFragment
                    listFragment
                }
                1 -> AllPlacesMapFragment()
                else -> PlacesListFragment()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_places, menu)

        val searchItem = menu.findItem(R.id.menu_main_search)
        val searchView = searchItem.actionView as? SearchView
        searchView?.isSubmitButtonEnabled = true
        searchView?.setOnQueryTextListener(this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_main_delete_all -> deleteAllPlaces()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun deleteAllPlaces() {
        MaterialDialog(requireContext(), BottomSheet(LayoutMode.WRAP_CONTENT)).show {
            icon(R.drawable.ic_delete_forever)
            title(R.string.dialog_delete_all)
            message(R.string.dialog_delete_confirmation)
            positiveButton(R.string.dialog_confirmation) {
                placeViewModel.deleteDatabase()
                snackBarMsg(requireView(), getString(R.string.snackbar_deleted_all))
            }
            negativeButton(R.string.dialog_negative)
        }
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        query?.let {
            placesListFragment?.filterData(it)
        }
        return true
    }

    override fun onQueryTextChange(query: String?): Boolean {
        query?.let {
            placesListFragment?.filterData(it)
        }
        return true
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}