package ru.molinov.mapsmarker

import android.Manifest
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.yandex.mapkit.MapKitFactory
import ru.molinov.mapsmarker.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapKitFactory.setApiKey("6985ce83-c00f-405e-b211-d6db564e0918") // Need to set before setContentView
        setContentView(ActivityMainBinding.inflate(layoutInflater).root)
        supportFragmentManager.beginTransaction()
            .add(R.id.container, MapFragment())
            .commit()
        val locationPermissionRequest = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) {
            if (!it) {
                Toast.makeText(applicationContext, "Need permission", Toast.LENGTH_SHORT).show()
            }
        }
        locationPermissionRequest.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }
}
