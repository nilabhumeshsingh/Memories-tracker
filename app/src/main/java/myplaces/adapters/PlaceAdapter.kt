package myplaces.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import myplaces.data.model.PlaceModel
import myplaces.databinding.RecyclerviewPlaceBinding

/**
 * RecyclerView Adapter for displaying places list items with DiffUtil.
 */
class PlaceAdapter : RecyclerView.Adapter<PlaceAdapter.MyViewHolder>() {

    var dataList: List<PlaceModel> = emptyList()
        private set

    class MyViewHolder(private val binding: RecyclerviewPlaceBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(placeModel: PlaceModel) {
            binding.placeModel = placeModel
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): MyViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RecyclerviewPlaceBinding.inflate(layoutInflater, parent, false)
                return MyViewHolder(binding)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(dataList[position])
    }

    override fun getItemCount(): Int = dataList.size

    fun setData(newPlaceList: List<PlaceModel>) {
        val diffUtil = PlaceDiffUtil(dataList, newPlaceList)
        val diffResult = DiffUtil.calculateDiff(diffUtil)
        this.dataList = newPlaceList
        diffResult.dispatchUpdatesTo(this)
    }
}

private class PlaceDiffUtil(
    private val oldList: List<PlaceModel>,
    private val newList: List<PlaceModel>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].id == newList[newItemPosition].id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldList[oldItemPosition]
        val newItem = newList[newItemPosition]
        return oldItem.id == newItem.id &&
                oldItem.title == newItem.title &&
                oldItem.description == newItem.description &&
                oldItem.date == newItem.date &&
                oldItem.image == newItem.image &&
                oldItem.category == newItem.category &&
                oldItem.latitude == newItem.latitude &&
                oldItem.longitude == newItem.longitude
    }
}