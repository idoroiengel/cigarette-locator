package org.idoroiengel.cigarettelocator.mapbox

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.mapbox.android.core.location.*
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import org.idoroiengel.cigarettelocator.R
import java.lang.ref.WeakReference


class MapboxActivity : AppCompatActivity(), OnMapReadyCallback, PermissionsListener {

    private var locationEngine: LocationEngine? = null
    private var permissionsManager: PermissionsManager? = null
    private var mapView: MapView? = null
    private var mapboxMap: MapboxMap? = null
    private var callback: MapboxActivityLocationCallback = MapboxActivityLocationCallback(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token))

        setContentView(R.layout.activity_main)

        mapView = findViewById(R.id.map_view)
        mapView?.onCreate(savedInstanceState)
        mapView?.getMapAsync(this)
    }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView?.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView?.onDestroy()
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        Log.d("MapboxActivity", "onMapReady")
        this.mapboxMap = mapboxMap
        mapboxMap.setStyle(
            Style.MAPBOX_STREETS,
            Style.OnStyleLoaded { style -> enableLocationComponent(style) })

    }

    private fun enableLocationComponent(style: Style) {
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            val locationComponent = mapboxMap!!.locationComponent
            val locationComponentActivationOptions =
                LocationComponentActivationOptions.builder(this, style)
                    .useDefaultLocationEngine(false)
                    .build()
            locationComponent.activateLocationComponent(locationComponentActivationOptions)
            locationComponent.setLocationComponentEnabled(true)
            locationComponent.setCameraMode(CameraMode.TRACKING)
            locationComponent.setRenderMode(RenderMode.COMPASS)
            initLocationEngine()
        } else {
            permissionsManager = PermissionsManager(this)
            permissionsManager?.requestLocationPermissions(this)
        }

    }

    private fun initLocationEngine() {
        locationEngine = LocationEngineProvider.getBestLocationEngine(this)
        val request = LocationEngineRequest.Builder(1000)
            .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
            .setMaxWaitTime(3000).build()
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        locationEngine?.requestLocationUpdates(request, callback, Looper.getMainLooper())
        locationEngine?.getLastLocation(callback!!)
    }

    override fun onExplanationNeeded(permissionsToExplain: List<String?>?) {
        Toast.makeText(this, "we need your location permission", Toast.LENGTH_LONG)
            .show()
    }

    override fun onPermissionResult(granted: Boolean) {
        if (granted) {
            if (mapboxMap!!.style != null) {
                enableLocationComponent(mapboxMap!!.style!!)
            }
        } else {
            Toast.makeText(this, "We have your location permission", Toast.LENGTH_LONG)
                .show()
            finish()
        }
    }

    private class MapboxActivityLocationCallback internal constructor(activity: MapboxActivity?) :
        LocationEngineCallback<LocationEngineResult> {
        private val activityWeakReference: WeakReference<MapboxActivity>

        /**
         * The LocationEngineCallback interface's method which fires when the device's location has changed.
         *
         * @param result the LocationEngineResult object which has the last known location within it.
         */
        override fun onSuccess(result: LocationEngineResult) {
            val activity: MapboxActivity = activityWeakReference.get()!!
            if (activity != null) {
                val location = result.lastLocation ?: return

// Create a Toast which displays the new location's coordinates
                Toast.makeText(
                    activity, String.format(
                        "This is your new location", result.lastLocation!!
                            .latitude.toString(), result.lastLocation!!.longitude.toString()
                    ),
                    Toast.LENGTH_SHORT
                ).show()

// Pass the new location to the Maps SDK's LocationComponent
                if (activity.mapboxMap != null && result.lastLocation != null) {
                    activity.mapboxMap!!.getLocationComponent()
                        .forceLocationUpdate(result.lastLocation)
                }
            }
        }

        /**
         * The LocationEngineCallback interface's method which fires when the device's location can not be captured
         *
         * @param exception the exception message
         */
        override fun onFailure(exception: Exception) {
            Log.d("LocationChangeActivity", exception.localizedMessage)
            val activity: MapboxActivity? = activityWeakReference.get()
            if (activity != null) {
                Toast.makeText(
                    activity, exception.localizedMessage,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        init {
            activityWeakReference = WeakReference(activity)
        }
    }
}