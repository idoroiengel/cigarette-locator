package org.idoroiengel.cigarettelocator.mapbox

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.res.ResourcesCompat
import com.mapbox.android.core.location.*
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.expressions.Expression
import com.mapbox.mapboxsdk.style.layers.Property
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import org.idoroiengel.cigarettelocator.R
import java.lang.ref.WeakReference
import java.util.*

class MapboxActivity : AppCompatActivity(), OnMapReadyCallback, PermissionsListener {
    private var mapboxMap: MapboxMap? = null
    private var mapView: MapView? = null
    private var routeCoordinates: MutableList<Point>? = null
    private var locationEngine: LocationEngine? = null
    private var permissionsManager: PermissionsManager? = null

    private var callback: MapboxActivityLocationCallback = MapboxActivityLocationCallback(this)

    private val GEOJSON_SOURCE_ID: String = "cigarettes_source_id"
    private val CIGARETTES_LAYER_ID: String = "cigarettes_layer_id"
    private val ICON_PROPERTY: String = "icon_property"
    private val IC_SMOKING_GREEN_CIRCLE: String = "ic-launcher-background"
    private val IC_SMOKING_RED_CIRCLE: String = "ic-launcher-foreground"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token))

        setContentView(R.layout.activity_main)
        mapView = findViewById(R.id.map_view)
        mapView?.onCreate(savedInstanceState)
        mapView?.getMapAsync(this)
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
            locationComponent.zoomWhileTracking(10.0)
            initLocationEngine()
        } else {
            permissionsManager = PermissionsManager(this)
            permissionsManager?.requestLocationPermissions(this)
        }
    }

    private fun initSymbolRouteCoordinates(): List<Feature> {
        val feature1: Feature = Feature.fromGeometry(
            Point.fromLngLat(34.804, 32.077)
        )
        feature1.addStringProperty(ICON_PROPERTY, IC_SMOKING_RED_CIRCLE)
        val feature2: Feature = Feature.fromGeometry(
            Point.fromLngLat(34.810, 32.08)
        )
        feature2.addStringProperty(ICON_PROPERTY, IC_SMOKING_GREEN_CIRCLE)
        val feature3: Feature = Feature.fromGeometry(
            Point.fromLngLat(34.817, 32.085)
        )
        feature3.addStringProperty(ICON_PROPERTY, IC_SMOKING_RED_CIRCLE)
        val feature4: Feature = Feature.fromGeometry(
            Point.fromLngLat(34.823, 32.077)
        )
        feature4.addStringProperty(ICON_PROPERTY, IC_SMOKING_GREEN_CIRCLE)
        val list: List<Feature> = arrayListOf(
            feature1,
            feature2,
            feature3,
            feature4
        )
        return list
    }

    private fun initRouteCoordinates() {
        routeCoordinates = ArrayList()
        (routeCoordinates as ArrayList<Point>).add(
            Point.fromLngLat(34.804, 32.077)
        )
        (routeCoordinates as ArrayList<Point>).add(
            Point.fromLngLat(34.810, 32.08)
        )
        (routeCoordinates as ArrayList<Point>).add(
            Point.fromLngLat(34.817, 32.085)
        )
        (routeCoordinates as ArrayList<Point>).add(
            Point.fromLngLat(34.823, 32.077)
        )
    }

    override fun onResume() {
        super.onResume()
        mapView!!.onResume()
    }

    override fun onStart() {
        super.onStart()
        mapView!!.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView!!.onStop()
    }

    override fun onPause() {
        super.onPause()
        mapView!!.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView!!.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView!!.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView!!.onSaveInstanceState(outState)
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        this.mapboxMap = mapboxMap


        mapboxMap.setStyle(
            Style.Builder().fromUri(Style.MAPBOX_STREETS)
                .withImage(
                    IC_SMOKING_GREEN_CIRCLE,
                    ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.smoking_green_circle,
                        null
                    )!!
                )
                .withImage(
                    IC_SMOKING_RED_CIRCLE,
                    ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.smoking_red_circle,
                        null
                    )!!
                )
        )
//        (
//            Style.OUTDOORS
//        )
        { style ->

            style.addSource(
                GeoJsonSource(
                    GEOJSON_SOURCE_ID,
                    FeatureCollection.fromFeatures(
                        initSymbolRouteCoordinates()
//                        arrayOf(
//                            Feature.fromGeometry(
//                                LineString.fromLngLats(routeCoordinates!!)
//                            )
//                        )
                    )
                )
            )
//            style.addLayer(
//                LineLayer(CIGARETTES_LAYER_ID, GEOJSON_SOURCE_ID).withProperties(
//                    PropertyFactory.lineDasharray(
//                        arrayOf(
//                            0.01f,
//                            2f
//                        )
//                    ),
//                    PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
//                    PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
//                    PropertyFactory.lineWidth(5f),
//                    PropertyFactory.lineColor(getColor(R.color.purple_200))
//                )
//            )

//            style.addLayer(
//                CircleLayer(
//                    CIGARETTES_LAYER_ID, GEOJSON_SOURCE_ID
//                ).withProperties(
//                    PropertyFactory.circleColor(Color.parseColor("#0000FF")),
//                    PropertyFactory.circleRadius(20f),
//                    PropertyFactory.circleStrokeColor(getColor(R.color.purple_200)),
//                    PropertyFactory.circleStrokeWidth(4f)
//                )
//            )
            style.addLayer(
                SymbolLayer(
                    CIGARETTES_LAYER_ID, GEOJSON_SOURCE_ID
                ).withProperties(
                    PropertyFactory.iconImage(
                        Expression.match(
                            Expression.get(ICON_PROPERTY),
                            Expression.literal(IC_SMOKING_GREEN_CIRCLE),
                            Expression.stop(IC_SMOKING_RED_CIRCLE, IC_SMOKING_RED_CIRCLE),
                            Expression.stop(IC_SMOKING_GREEN_CIRCLE, IC_SMOKING_GREEN_CIRCLE)
                        )
                    ),
                    PropertyFactory.iconAllowOverlap(true),
                    PropertyFactory.iconAnchor(Property.ICON_ANCHOR_BOTTOM)
                )
            )
            enableLocationComponent(style)
        }

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
        locationEngine?.getLastLocation(callback)
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