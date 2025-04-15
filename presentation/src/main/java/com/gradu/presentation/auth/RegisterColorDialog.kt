package com.gradu.presentation.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.gradu.core.enums.CategoryColor
import com.gradu.presentation.databinding.DialogRegisterColorBinding
import com.gradu.presentation.ui.home.category.adapter.CategoryPaletteRVAdapter


class RegisterColorDialog() : BottomSheetDialogFragment() {
    private val viewModel: RegisterViewModel by activityViewModels()
    private var _binding: DialogRegisterColorBinding? = null
    private val binding get() = _binding!!

    private lateinit var paletteAdapter: CategoryPaletteRVAdapter
    private var selectedColor: CategoryColor? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogRegisterColorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupButtons()
    }

    private fun setupRecyclerView() {
        val colorList = ArrayList(CategoryColor.getAllColors())
        val initialPosition = colorList.indexOf(viewModel.color.value).takeIf { it >= 0 } ?: -1  // 선택된 색이 없으면 -1

        paletteAdapter = CategoryPaletteRVAdapter(
            requireContext(),
            colorList,
            initialPosition
        )
        binding.categoryPaletteRv.apply {
            adapter = paletteAdapter
            layoutManager = GridLayoutManager(requireContext(), 5)
        }

        paletteAdapter.setColorClickListener(object : CategoryPaletteRVAdapter.MyItemClickListener {
            override fun onItemClick(position: Int, selectedColor: CategoryColor) {
                this@RegisterColorDialog.selectedColor = selectedColor
            }
        })
    }


    private fun setupButtons() {
        binding.registerColorCancelBtn.setOnClickListener {
            dismiss()
        }
        binding.registerColorSaveBtn.setOnClickListener {
            viewModel.clearHighlight("color")
            viewModel.setColor(selectedColor)
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
