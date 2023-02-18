package com.example.namo.ui.bottom.diary

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.namo.R
import com.example.namo.data.NamoDatabase
import com.example.namo.data.entity.diary.Diary
import com.example.namo.data.entity.home.calendar.Event
import com.example.namo.databinding.FragmentDiaryBinding
import com.example.namo.ui.bottom.diary.adapter.DiaryListRVAdapter
import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime
import java.util.*


class DiaryFragment: Fragment() {

    private var _binding: FragmentDiaryBinding? = null
    private val binding get() = _binding!!

    private var dateTime = DateTime().withDayOfMonth(1).withTimeAtStartOfDay().millis
    private var diaryList=listOf<Diary>()
    lateinit var diaryAdapter:DiaryListRVAdapter

    private lateinit var db: NamoDatabase

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentDiaryBinding.inflate(inflater, container, false)

        binding.diaryMonth.setOnClickListener {
            dialogCreate()
        }

        db=NamoDatabase.getInstance(requireContext())
        diaryAdapter= DiaryListRVAdapter(requireContext(),diaryList)

        val r = Runnable {
            try {
                diaryList = db.diaryDao.getDiaryList(DateTime(dateTime).toString("yyyy-MM"))
                diaryAdapter= DiaryListRVAdapter(requireContext(),diaryList)
                requireActivity().runOnUiThread {
                    binding.diaryListRv.adapter =  diaryAdapter
                    binding.diaryListRv.layoutManager = LinearLayoutManager(requireContext())
                    binding.diaryListRv.setHasFixedSize(true)
                }

            } catch (e: Exception) {
                Log.d("tag", "Error - $e")
            }
        }

        val thread = Thread(r)
        thread.start()

        Log.d("date","$diaryList")
        return binding.root
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStart() {
        super.onStart()
        binding.diaryMonth.text=DateTime(dateTime).toString("yyyy.MM")

        Log.d("date","${DateTime(dateTime).toString("yyyy-MM")}")

    }

    private fun dialogCreate() {

        YearMonthDialog(dateTime){
            binding.diaryMonth.text= DateTime(it).toString("yyyy.MM")

            val r = Runnable {
                try {
                    diaryList = db.diaryDao.getDiaryList(DateTime(it).toString("yyyy-MM"))
                    diaryAdapter= DiaryListRVAdapter(requireContext(),diaryList)
                    requireActivity().runOnUiThread {
                        binding.diaryListRv.adapter =  diaryAdapter
                        binding.diaryListRv.layoutManager = LinearLayoutManager(requireContext())
                        binding.diaryListRv.setHasFixedSize(true)
                    }

                } catch (e: Exception) {
                    Log.d("tag", "Error - $e")
                }
            }

            val thread = Thread(r)
            thread.start()


        }.show(parentFragmentManager,"test")
    }


    private fun initRecyclerview() {


    }

    private fun onItemClick(position:Int){
        val act=DiaryFragmentDirections.actionDiaryFragmentToDiaryDetailFragment(position)
        findNavController().navigate(act)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}




