package ru.molinov.mapsmarker

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import ru.molinov.mapsmarker.databinding.BottomSheetBinding

class BottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetBinding? = null
    private val binding get() = _binding!!
    private var userData: MapFragment.UserData? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.getParcelable<MapFragment.UserData>("KEY").let { userData = it }
        binding.description.setText(userData?.description.toString())
        binding.save.setOnClickListener {
            parentFragmentManager.setFragmentResult("KEY", Bundle().apply {
                userData?.description = binding.description.toString()
                putParcelable("KEY", userData)
            })
            dialog?.dismiss()
        }
        binding.cancel.setOnClickListener {
            parentFragmentManager.setFragmentResult("DELETE", Bundle().apply {
                putParcelable("KEY", userData)
            })
            dialog?.dismiss()
        }
    }

    override fun getTheme(): Int = R.style.AppBottomSheetDialogTheme

    override fun onStart() {
        super.onStart()
        binding.cancel.setBackgroundColor(Color.RED)
        binding.save.setBackgroundColor(Color.GREEN)

        // Плотность понадобится нам в дальнейшем
        val density = requireContext().resources.displayMetrics.density

        dialog?.let {
            // Находим сам bottomSheet и достаём из него Behaviour
            val bottomSheet =
                it.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout
            val behavior = BottomSheetBehavior.from(bottomSheet)
            // Выставляем высоту для состояния collapsed и выставляем состояние collapsed
            behavior.apply {
                peekHeight = (COLLAPSED_HEIGHT * density).toInt()
                state = BottomSheetBehavior.STATE_COLLAPSED
            }
            behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {

                override fun onStateChanged(bottomSheet: View, newState: Int) {
//                    when (newState) {
//                        BottomSheetBehavior.STATE_DRAGGING -> TODO("not implemented")
//                        BottomSheetBehavior.STATE_COLLAPSED -> TODO("not implemented")
//                        BottomSheetBehavior.STATE_EXPANDED -> TODO("not implemented")
//                        BottomSheetBehavior.STATE_HALF_EXPANDED -> TODO("not implemented")
//                        BottomSheetBehavior.STATE_HIDDEN -> TODO("not implemented")
//                        BottomSheetBehavior.STATE_SETTLING -> TODO("not implemented")
//                    }
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    with(binding) {
                        // Нас интересует только положительный оффсет, тк при отрицательном нас устроит стандартное поведение - скрытие фрагмента
                        if (slideOffset > 0) {
                            // Делаем "свёрнутый" layout более прозрачным
//                            layoutCollapsed.alpha = 1 - 2 * slideOffset
                            // И в то же время делаем "расширенный layout" менее прозрачным
//                            layoutExpanded.alpha = slideOffset * slideOffset

                            // Когда оффсет превышает половину, мы скрываем collapsed layout и делаем видимым expanded
                            if (slideOffset > 0.5) {
//                                layoutCollapsed.visibility = View.GONE
//                                layoutExpanded.visibility = View.VISIBLE
                            }

                            // Если же оффсет меньше половины, а expanded layout всё ещё виден, то нужно скрывать его и показывать collapsed
//                            if (slideOffset < 0.5 && binding.layoutExpanded.visibility == View.VISIBLE) {
//                                layoutCollapsed.visibility = View.VISIBLE
//                                layoutExpanded.visibility = View.INVISIBLE
//                            }
                        }
                    }
                }
            })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    companion object {
        fun newInstance(userData: MapFragment.UserData) = BottomSheet().apply {
            arguments = Bundle().apply { putParcelable("KEY", userData) }
        }

        const val COLLAPSED_HEIGHT = 500
    }
}
