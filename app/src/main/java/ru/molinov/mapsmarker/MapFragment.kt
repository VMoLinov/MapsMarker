package ru.molinov.mapsmarker

import android.Manifest
import android.content.Context.LOCATION_SERVICE
import android.content.pm.PackageManager
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.map.MapObjectCollection
import com.yandex.mapkit.user_location.UserLocationLayer
import ru.molinov.mapsmarker.databinding.FragmentMapBinding

class MapFragment : Fragment() {

    /**
     * Вместо findViewById мы используем view binding. _binding будет инициализирована в callback'е
     * жизненного цикла фрагмента #onCreateView. Результатом выполнения этой функции будет привязка
     * fragment_map.xml к MapFragment
     * Обращаемся посредством константы binding через форсколл (!!) в тех местах, где мы уверены, что
     * _binding != null Такой подход реализован для упрощения читабельности кода, нет нужды
     * каждый раз обращаться к binding с сейфколлом (binding?). В callback'е жизненного цикла
     * фрагмента #onDestroy() удаляем привязку binding к текущему представлению
     * */
    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    /**
     * Объекты, необходимые для работы нашего функционала.
     * map - объект карты
     * userLocation - UI слой с иконкой месторасположения устройства
     * locationManager -
     * */
    private lateinit var map: Map
    private lateinit var userLocation: UserLocationLayer
    private lateinit var locationManager: com.yandex.mapkit.location.LocationManager

    private val locationListener = LocationListener {
        map.move(
            CameraPosition(Point(it.latitude, it.longitude), ZOOM, AZIMUTH, TILT),
            ANIMATION,
            null
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        MapKitFactory.initialize(requireContext())
        userLocation =
            MapKitFactory.getInstance().createUserLocationLayer(binding.mapView.mapWindow)
//        locationManager = MapKitFactory.getInstance().createLocationManager()
        map = binding.mapView.map
        val locationManager: LocationManager =
            (requireActivity().getSystemService(LOCATION_SERVICE)) as LocationManager
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            userLocation.isVisible = true
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, MIN_TIME_MS, MIN_DISTANCE_M, locationListener
            )
        }
    }

    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
        MapKitFactory.getInstance().onStart()
    }

    override fun onStop() {
        super.onStop()
//        binding.mapView.onStop()
//        MapKitFactory.getInstance().onStop()
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }

    companion object {
        val ANIMATION = Animation(Animation.Type.SMOOTH, 0.4f)
        const val ZOOM = 14.0f
        const val AZIMUTH = 0.0f
        const val TILT = 0.0f
        const val MIN_TIME_MS = 1000L
        const val MIN_DISTANCE_M = 500f
    }
}
