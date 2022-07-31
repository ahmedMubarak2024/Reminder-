package com.udacity.project4.locationreminders.savereminder.mapFragment

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.databinding.FragmentMapsBinding
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.CustomInfoWindowAdapter
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.get
import org.koin.android.ext.android.inject
import org.koin.core.qualifier.named

class MapsFragment : BaseFragment() {
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentMapsBinding
    private lateinit var map: GoogleMap
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var marker: Marker? = null
    private val callback = OnMapReadyCallback { googleMap ->
        this.map = googleMap
        if (isPermissionGranted()) {
            initMap()
        } else requestPermission()

    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate<FragmentMapsBinding>(
            inflater,
            R.layout.fragment_maps,
            container,
            false
        )
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = _viewModel
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)

    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)

    }

    // Called whenever an item in your options menu is selected.
    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        // Change the map type based on the user's selection.
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    // Checks that users have given permission
    private fun isPermissionGranted(): Boolean {
        return (activity as? RemindersActivity)?.checkOnForegroundLocation() ?: false
    }

    @SuppressLint("MissingPermission")
    private fun initMap() {
        map.setInfoWindowAdapter(CustomInfoWindowAdapter(requireActivity()))
        enableMyLocation()
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())
        fusedLocationProviderClient.lastLocation.addOnSuccessListener {
            if (it != null) {
                val latLng = LatLng(it.getLatitude(), it.getLongitude())
                map.animateCamera(CameraUpdateFactory.newLatLng(latLng))
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
            }
        }
        setMapClick(map)
        setMapStyle(map)
    }

    // Checks if users have given their location and sets location enabled if so.
    private fun enableMyLocation() {
        if (isPermissionGranted()) {
            map.setMyLocationEnabled(true)
        } else {
            requestPermission()
        }
    }

    private fun requestPermission() {
        (activity as? RemindersActivity)?.requestForegroundAndBackgroundLocationPermissions(
            foregroundOnly = true
        ) {
            if (isPermissionGranted())
                initMap()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (isPermissionGranted()) {
            initMap()
        } else requestPermission()

    }

    private fun setMapClick(map: GoogleMap) {
        map.setOnPoiClickListener { poiData ->
            // A Snippet is Additional text that's displayed below the title.
            setLocationInfo(poiData)
        }
    }

    fun setLocationInfo(poi: PointOfInterest) {
        marker?.remove()
        marker = map.addMarker(
            MarkerOptions()
                .position(poi.latLng)
                .title(getString(R.string.dropped_pin))
                .snippet(poi.name)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
        )
        marker?.showInfoWindow()
        _viewModel.selectedPOI.value = poi
        _viewModel.latitude.value = poi.latLng.latitude
        _viewModel.longitude.value = poi.latLng.longitude
        _viewModel.reminderSelectedLocationStr.value = poi.name

//        lifecycleScope.launch {
//            val address = loadLocationInfoInBackground(context, latLng)
//            val sb = StringBuilder()
//            for (i in 0 until address!!.maxAddressLineIndex) {
//                if(!address.getAddressLine(i).isNullOrBlank())
//                sb.append(address.getAddressLine(i)) //.append("\n");
//            }
//            if (!address.locality.isNullOrBlank())
//            sb.append(address.locality).append("\n")
//            if (!address.postalCode.isNullOrBlank())
//            sb.append(address.postalCode).append("\n")
//            if (!address.countryName.isNullOrBlank())
//            sb.append(address.countryName)
//
//            val snippet = sb.toString()
//            marker?.snippet = snippet
//
//            marker?.showInfoWindow()
//
//        }

    }

    // Allows map styling and theming to be customized.
    private fun setMapStyle(map: GoogleMap) {
        try {
            // Customize the styling of the base map using a JSON object defined
            // in a raw resource file.
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireContext(),
                    R.raw.map_style
                )
            )

            if (!success) {
                Log.e(TAG, "Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e(TAG, "Can't find style. Error: ", e)
        }
    }

    suspend fun loadLocationInfoInBackground(context: Context, latLng: LatLng): Address? {
        return withContext(get(named("IO"))) {
            Geocoder(context).getFromLocation(latLng.latitude, latLng.longitude, 1).firstOrNull()

        }

    }
}