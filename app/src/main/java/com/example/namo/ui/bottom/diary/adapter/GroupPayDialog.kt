package com.example.namo.ui.bottom.diary.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.namo.databinding.DialogGroupPayBinding


class GroupPayDialog : DialogFragment(), View.OnClickListener{

    lateinit var binding: DialogGroupPayBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = DialogGroupPayBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onClick(p0: View?) {
        dismiss()
    }
}
