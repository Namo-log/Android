package com.example.namo.bottom.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.namo.bottom.home.calendar.CalendarAdapter
import com.example.namo.MainActivity
import com.example.namo.R
import com.example.namo.databinding.FragmentHomeBinding
import com.google.android.material.bottomnavigation.BottomNavigationView


class HomeFragment : Fragment() {

    private lateinit var calendarAdapter : CalendarAdapter
    private lateinit var binding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate<FragmentHomeBinding>(inflater, R.layout.fragment_home, container,false)
        calendarAdapter = CalendarAdapter(context as MainActivity)

        hideBottomNavigation(false)

        binding.homeCalendarVp.adapter = calendarAdapter
        binding.homeCalendarVp.setCurrentItem(CalendarAdapter.START_POSITION, false)

        return binding.root
    }

    private fun hideBottomNavigation( bool : Boolean){
        val bottomNavigationView : BottomNavigationView = requireActivity().findViewById(R.id.nav_bar)
        if(bool == true) {
            bottomNavigationView.visibility = View.GONE
        } else {
            bottomNavigationView.visibility = View.VISIBLE
        }
    }
}