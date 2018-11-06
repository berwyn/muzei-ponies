package so.codeweaver.muzei.ponies.util

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class DenseGridLayoutManager(
        private val context: Context,
        private val spanCount: Int,
        private val itemSize: Int
) : RecyclerView.LayoutManager() {
    override fun canScrollHorizontally(): Boolean {
        return false
    }

    override fun canScrollVertically(): Boolean {
        return false
    }

    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
        return RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    override fun onLayoutChildren(recycler: RecyclerView.Recycler?, state: RecyclerView.State?) {
        removeAllViews()

        if (itemCount == 0 || recycler == null) return

        recycler.setViewCacheSize(itemCount)

        for (i in 0 until itemCount) {
            val view = recycler.getViewForPosition(i)
            val top = itemSize * (i / spanCount)
            val left = itemSize * (i % spanCount)

            view.left = left
            view.top = top
            view.bottom = top + itemSize
            view.right = left + itemSize

            addView(view)
            layoutDecorated(view, view.left, view.top, view.right, view.bottom)
        }
    }
}