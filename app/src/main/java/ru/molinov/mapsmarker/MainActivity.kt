package ru.molinov.mapsmarker

import android.Manifest
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.yandex.mapkit.MapKitFactory
import ru.molinov.mapsmarker.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    /**
     * Объект для запуска запроса разрешения. Инициализация в #onCreate()
     * #ActivityResultContracts.RequestPermission() запрашивает разрешения,
     * #registerForActivityResult ловит результат. Результат - isGranted может быть true или false
     * */
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    /**
     * Переопределим функцию жизненного цикла Activity для указания ключа Yandex MapKitFactory.
     * Используем ключ, полученный здесь - https://yandex.ru/dev/maps/mapkit
     * В #setContentView() задаём xml ресурс из папки layout посредством view binding
     * Далее инициализируем объект запроса разрешения и запускаем запрос разрешения
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapKitFactory.setApiKey("6985ce83-c00f-405e-b211-d6db564e0918") // Need to set before setContentView
        setContentView(ActivityMainBinding.inflate(layoutInflater).root)
        requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
                if (isGranted) {
                    // Разрешение получено, запускаем основной фрагмент приложения
                    // И помещаем его в container activity_main.xml
                    supportFragmentManager.beginTransaction()
                        .add(R.id.container, MapFragment())
                        .commit()

                } else {
                    // Разрешение не получено, выводим диалоговое окно. AlertDialog имеет паттерн
                    // builder - создаём экземпляр класса AlertDialog.Builder, задаём параметры,
                    // затем создаём экземпляр диалога и показываем его пользователю
                    AlertDialog.Builder(this)
                        .setMessage(getString(R.string.need_permission))
                        .setPositiveButton(android.R.string.ok) { _, _ ->
                            // При нажатии positive button - повторно запрашиваем разрешение
                            requestPermission()
                        }
                        .create()
                        .show()
                }
            }
        requestPermission()
    }

    /**
     * Запускает запрос разрешения.
     * */
    private fun requestPermission() =
        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
}
