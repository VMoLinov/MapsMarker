package ru.molinov.mapsmarker

import android.Manifest
import android.content.Context.LOCATION_SERVICE
import android.content.pm.PackageManager
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.*
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.user_location.UserLocationLayer
import com.yandex.runtime.image.ImageProvider
import kotlinx.parcelize.Parcelize
import ru.molinov.mapsmarker.databinding.FragmentMapBinding
import kotlin.random.Random

class MapFragment : Fragment() {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!
    private lateinit var map: Map
    private lateinit var userLocation: UserLocationLayer
    private lateinit var locationManager: com.yandex.mapkit.location.LocationManager
    private lateinit var mapObjectCollection: MutableList<MapObjectCollection>
    private val inputListener = object : InputListener {
        override fun onMapTap(map: Map, point: Point) {
            mapObjectCollection.add(map.mapObjects.addCollection())
            val index = mapObjectCollection.lastIndex
            val mark: PlacemarkMapObject = mapObjectCollection[index].addPlacemark(point)
            mark.apply {
                opacity = 0.5f
                setIcon(ImageProvider.fromResource(requireContext(), R.drawable.mark))
                isDraggable = true
                addTapListener(mapObjectTapListener)
            }
            showBottomDialog(mapObjectCollection[index])
        }

        override fun onMapLongTap(map: Map, point: Point) {
            map.apply {
                mapObjects.addPlacemark(point)
                move(CameraPosition(point, ZOOM, AZIMUTH, TILT), ANIMATION, null)
            }
        }
    }
    private val mapObjectTapListener = MapObjectTapListener { mapObject, point ->
        mapObject.parent.remove(mapObject)
        return@MapObjectTapListener true
    }

    private fun showBottomDialog(mapObject: MapObject) {
        mapObject.userData = UserData(Random.nextInt(), "description")
        val data = mapObject.userData as UserData
        BottomSheet.newInstance(data).show(parentFragmentManager, "TAG")
    }

    @Parcelize
    class UserData constructor(val id: Int, var description: String) : Parcelable

    private val locationListener = LocationListener {
        map.move(
            CameraPosition(Point(it.latitude, it.longitude), ZOOM, AZIMUTH, TILT),
            ANIMATION,
            null
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        MapKitFactory.initialize(requireContext())
        userLocation =
            MapKitFactory.getInstance().createUserLocationLayer(binding.mapView.mapWindow)
        locationManager = MapKitFactory.getInstance().createLocationManager()
        map = binding.mapView.map
        map.addInputListener(inputListener)
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
        mapObjectCollection = mutableListOf(map.mapObjects.addCollection())
        parentFragmentManager.setFragmentResultListener("DELETE", this) { _, bundle ->
            val data = bundle.getParcelable<UserData>("KEY")
            for (mapObject in mapObjectCollection) {
                if (mapObject.userData == data) {
                    mapObject.clear()
                }
            }
        }
        parentFragmentManager.setFragmentResultListener("KEY", this) { _, bundle ->
            val data = bundle.getParcelable<UserData>("KEY")
            for (mapObject in mapObjectCollection) {
                mapObject.userData.apply {
                    if (this is UserData && this == data) {
                        mapObject.userData = data
                    }
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
        MapKitFactory.getInstance().onStart()
    }

    override fun onStop() {
        super.onStop()
        binding.mapView.onStop()
        MapKitFactory.getInstance().onStop()
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
