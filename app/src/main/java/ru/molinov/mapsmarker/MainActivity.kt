package ru.molinov.mapsmarker

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ru.molinov.mapsmarker.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(ActivityMainBinding.inflate(layoutInflater).root)
        supportFragmentManager.beginTransaction()
            .add(R.id.container, MapFragment())
            .commit()
        super.onCreate(savedInstanceState)
    }
}
