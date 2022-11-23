package com.example.namo.Bottom.GroupList

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.namo.databinding.FragmentGroupListBinding


class GroupListFragment: Fragment() {

    lateinit var binding: FragmentGroupListBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentGroupListBinding.inflate(inflater, container, false)

        return binding.root
    }
}