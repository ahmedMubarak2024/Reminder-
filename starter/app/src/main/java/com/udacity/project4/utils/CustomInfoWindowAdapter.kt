package com.udacity.project4.utils

import android.app.Activity
import android.view.View
import android.widget.TextView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import com.udacity.project4.R

class CustomInfoWindowAdapter(context: Activity) : GoogleMap.InfoWindowAdapter {
    private val context: Activity
    override fun getInfoWindow(marker: Marker): View? {
        return null
    }

    override fun getInfoContents(marker: Marker): View {
        val view: View = context.getLayoutInflater().inflate(R.layout.custom_info_window, null)
        val tvTitle: TextView = view.findViewById(R.id.tv_title) as TextView
        val tvSubTitle: TextView = view.findViewById(R.id.tv_subtitle) as TextView
        tvTitle.setText(marker.getTitle())
        tvSubTitle.setText(marker.getSnippet())
        return view
    }

    init {
        this.context = context
    }
}