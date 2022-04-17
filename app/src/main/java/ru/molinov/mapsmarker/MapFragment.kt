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
import com.yandex.mapkit.user_location.UserLocationLayer
import ru.molinov.mapsmarker.databinding.FragmentMapBinding

class MapFragment : Fragment() {

    /**
     * Вместо findViewById мы используем view binding. _binding будет инициализирована в callback'е
     * жизненного цикла фрагмента #onCreateView(). Результатом выполнения этой функции будет привязка
     * fragment_map.xml к MapFragment
     * Обращаемся посредством константы binding через форсколл (!!) в тех местах, где мы уверены, что
     * _binding != null Такой подход реализован для упрощения читабельности кода, нет нужды
     * каждый раз обращаться к binding с сейфколлом (binding?). В callback'е жизненного цикла
     * фрагмента #onDestroy() удаляем привязку binding к текущему представлению
     */
    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    /**
     * Объекты, необходимые для работы нашего функционала.
     * map - объект карты
     * userLocation - UI слой с иконкой месторасположения устройства
     * locationManager - возьмём менеджер ОС для более быстрой инициализации и дальнейшей работы
     */
    private lateinit var map: Map
    private lateinit var userLocation: UserLocationLayer
    private lateinit var locationManager: LocationManager

    /**
     * Слушатель изменения местоположения. При каждом обновлении возвращает широту и долготу.
     * Привязываем каждое обновление к сдвигу map на текущую локацию
     */
    private var locationListener = LocationListener {
        map.move(
            CameraPosition(Point(it.latitude, it.longitude), ZOOM, AZIMUTH, TILT),
            ANIMATION,
            null
        )
    }

    /**
     * Этот и последующие функции жизненного цикла вызваны в порядке их вызова ОС
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Инициализируем Kit от Яндекса, затем обращаемся с экземпляру инициализированного объекта
     * и создаём слой местонахождения устройства. Привязываем слой к окну карты через binding и
     * помещаем результат в объект userLocation
     * Так же инициализируем объект map из созданной карты
     * Обращаемся к системным сервисам через активити и забираем LOCATION_SERVICE
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        MapKitFactory.initialize(requireContext())
        userLocation =
            MapKitFactory.getInstance().createUserLocationLayer(binding.mapView.mapWindow)
        map = binding.mapView.map
        locationManager = (requireActivity().getSystemService(LOCATION_SERVICE)) as LocationManager
    }

    /**
     * Последний callback жизненного цикла перед отображением на экране. Запускаем UI карты и
     * MapKit. Перед отображением и подпиской на обновления местоположения нужно проверить
     * наличие разрешений на использование местоположения. Получаем в условии true - отображаем,
     * подписываемся
     */
    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
        MapKitFactory.getInstance().onStart()
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

    /**
     * Если разрешение получено только на время работы приложения и мы свернём его - приложение
     * закроется с ошибкой. Что бы этого не случалось и для экономии заряда батареи устройства
     * удаляем подписку locationManager
     * Если она нам понадобится снова - при возврате в приложение вызовется callback #onStart()
     * где мы снова подпишем locationManager на обновления
     */
    override fun onStop() {
        super.onStop()
        locationManager.removeUpdates(locationListener)
    }

    /**
     * Статичный объект класса. Может существовать в единственном экземпляре. В данной
     * работе в него вынесены константы для удобства изменения свойств
     */
    companion object {
        val ANIMATION = Animation(Animation.Type.SMOOTH, 0.4f) // Тип анимации и продолжительность в секундах
        const val ZOOM = 14.0f // Размер приближения
        const val AZIMUTH = 0.0f // Угол между севером и интересующим направлением на плоскости карты, в градусах в диапазоне от 0 до 360)
        const val TILT = 0.0f // Наклон камеры в градусах
        const val MIN_TIME_MS = 1000L // Интервал опроса изменения местоположения в миллисекундах
        const val MIN_DISTANCE_M = 500f // Минимальная точность определения местоположения в метрах
    }
}
