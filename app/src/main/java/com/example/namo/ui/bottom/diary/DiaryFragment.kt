package com.example.namo.ui.bottom.diary

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.namo.R
import com.example.namo.data.NamoDatabase
import com.example.namo.data.entity.diary.Diary
import com.example.namo.databinding.FragmentDiaryBinding
import com.example.namo.ui.bottom.diary.adapter.DiaryListRVAdapter
import org.joda.time.DateTime


class DiaryFragment: Fragment() {

    private var _binding: FragmentDiaryBinding? = null
    private val binding get() = _binding!!

    private var dateTime = DateTime().withDayOfMonth(1).withTimeAtStartOfDay().millis
    private var diaryList=listOf<Diary>()
    lateinit var diaryAdapter:DiaryListRVAdapter
    private lateinit var yearMonth:String

    private lateinit var db: NamoDatabase

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentDiaryBinding.inflate(inflater, container, false)

        db=NamoDatabase.getInstance(requireContext())
        diaryAdapter= DiaryListRVAdapter(requireContext(),diaryList)

        binding.diaryMonth.text=DateTime(dateTime).toString("yyyy.MM")
        yearMonth=binding.diaryMonth.text.toString()

        getList()

        binding.diaryMonth.setOnClickListener {
            dialogCreate()
        }

        return binding.root
    }

    private fun getList(){
        val r = Runnable {
            try {
                diaryList = db.diaryDao.getDiaryList(yearMonth)
                diaryAdapter= DiaryListRVAdapter(requireContext(),diaryList)
                requireActivity().runOnUiThread {
                    binding.diaryListRv.adapter =  diaryAdapter
                    diaryAdapter.notifyDataSetChanged()
                    binding.diaryListRv.layoutManager = LinearLayoutManager(requireContext())
                    binding.diaryListRv.setHasFixedSize(true)

                    diaryAdapter.setRecordClickListener(object : DiaryListRVAdapter.DiaryEditInterface{
                        override fun onEditClicked(diary: Diary) {
                            val bundle=Bundle()
                            bundle.putInt("diaryIdx",diary.diaryIdx)

                            val diaryFrag=DiaryModifyFragment()
                            diaryFrag.arguments=bundle

                            view?.findNavController()?.navigate(R.id.action_diaryFragment_to_diaryModifyFragment, bundle)
                        }
                    })
                }

            } catch (e: Exception) {
                Log.d("tag", "Error - $e")
            }
        }

        val thread = Thread(r)
        thread.start()

    }

    private fun dialogCreate() {

        YearMonthDialog(dateTime){
            yearMonth= DateTime(it).toString("yyyy.MM")
            binding.diaryMonth.text=yearMonth
            getList()
            Log.d("date","$yearMonth")
        }.show(parentFragmentManager,"test")

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}




