package myplaces.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import myplaces.R

/**
 * ItemTouchHelper.SimpleCallback for drawing custom swipe background and icons.
 */
abstract class SwipeItem(
    private val action: Int,
    private val background: Drawable,
    private val icon: Drawable,
    private val context: Context
) : ItemTouchHelper.SimpleCallback(0, action) {

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean = false

    override fun onChildDraw(
        canvas: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        val itemView = viewHolder.itemView

        // Draw Background
        when (action) {
            Constants.SWIPE_DELETE -> background.setBounds(
                itemView.left + dX.toInt(), itemView.top, itemView.right, itemView.bottom
            )
            Constants.SWIPE_EDIT -> background.setBounds(
                itemView.left, itemView.top, itemView.right + dX.toInt(), itemView.bottom
            )
        }
        background.draw(canvas)

        // Draw Icon
        val itemHeight = itemView.bottom - itemView.top
        val iconMargin = 55
        val iconTop = itemView.top + (itemHeight - icon.intrinsicHeight) / 2
        val iconBottom = iconTop + icon.intrinsicHeight

        when (action) {
            Constants.SWIPE_DELETE -> {
                val iconLeft = itemView.right - iconMargin - icon.intrinsicWidth
                val iconRight = itemView.right - iconMargin
                icon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
            }
            Constants.SWIPE_EDIT -> {
                val iconLeft = itemView.left + iconMargin
                val iconRight = itemView.left + iconMargin + icon.intrinsicWidth
                icon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
            }
        }

        icon.setTint(ContextCompat.getColor(context, R.color.swipeIconColor))
        icon.draw(canvas)
        icon.setTint(ContextCompat.getColor(context, R.color.iconColor))

        super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }
}