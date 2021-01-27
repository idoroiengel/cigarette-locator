package org.idoroiengel.cigarettelocator

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.geometry.LatLng
import org.idoroiengel.cigarettelocator.mapbox.MapboxActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token))

        setContentView(R.layout.activity_main)

        val intent = Intent(applicationContext, MapboxActivity::class.java)
        intent.putParcelableArrayListExtra(
            getString(R.string.INTENT_EXTRA_FEATURE_LIST_FOR_MAPBOX),
            arrayListOf(
                LatLng(32.086, 34.789),
                LatLng(32.086, 34.77),
                LatLng(32.086, 34.77),
                LatLng(32.086, 34.765),
            )
        )
        startActivity(intent)
    }
}