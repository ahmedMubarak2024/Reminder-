package com.udacity.project4.utils

import android.view.View
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.udacity.project4.R
import com.udacity.project4.base.BaseRecyclerViewAdapter
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem


object BindingAdapters {

    @BindingAdapter("showRefresh")
    @JvmStatic
    fun SwipeRefreshLayout.showRefresh(refreshing: Boolean) {
        isRefreshing = refreshing
    }

    @BindingAdapter("loadMap")
    @JvmStatic
    fun ImageView.loadMap(reminderDataItem: ReminderDataItem) {
        val url =
            "https://maps.googleapis.com/maps/api/staticmap?center=${reminderDataItem.latitude},${reminderDataItem.longitude}&zoom=18&size=800x640&key=${
                resources.getString(R.string.google_maps_key)
            }"
        Glide.with(this).load(url).into(this)
    }


    /**
     * Use binding adapter to set the recycler view data using livedata object
     */
    @Suppress("UNCHECKED_CAST")
    @BindingAdapter("android:liveData")
    @JvmStatic
    fun <T> setRecyclerViewData(recyclerView: RecyclerView, items: LiveData<List<T>>?) {
        items?.value?.let { itemList ->
            (recyclerView.adapter as? BaseRecyclerViewAdapter<T>)?.apply {
                clear()
                addData(itemList)
            }
        }
    }

    /**
     * Use this binding adapter to show and hide the views using boolean variables
     */
    @BindingAdapter("android:fadeVisible")
    @JvmStatic
    fun setFadeVisible(view: View, visible: Boolean? = true) {
        if (view.tag == null) {
            view.tag = true
            view.visibility = if (visible == true) View.VISIBLE else View.GONE
        } else {
            view.animate().cancel()
            if (visible == true) {
                if (view.visibility == View.GONE)
                    view.fadeIn()
            } else {
                if (view.visibility == View.VISIBLE)
                    view.fadeOut()
            }
        }
    }
}