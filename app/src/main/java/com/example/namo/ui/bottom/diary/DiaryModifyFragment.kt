package com.example.namo.ui.bottom.diary


import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.example.namo.R
import com.example.namo.data.NamoDatabase
import com.example.namo.data.entity.diary.Diary
import com.example.namo.databinding.FragmentDiaryModifyBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.text.SimpleDateFormat



class DiaryModifyFragment : Fragment() {

    private var _binding: FragmentDiaryModifyBinding? = null
    private val binding get() = _binding!!

    private lateinit var db:NamoDatabase

    private lateinit var diary:Diary
    private var diaryIdx:Int=0

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentDiaryModifyBinding.inflate(inflater, container, false)

        hideBottomNavigation(true)

        db=NamoDatabase.getInstance(requireContext())

        diaryIdx= arguments?.getInt("diaryIdx")!!

        Thread {
            diary = db.diaryDao.getDiaryDaily(diaryIdx)
            requireActivity().runOnUiThread {
                bind()
            }
        }.start()

        charCnt()

        return binding.root
    }

    private fun bind(){

        val formatDate=SimpleDateFormat("yyyy.MM.dd (EE)").format(diary.date)
        binding.diaryInputDateTv.text=formatDate
        binding.diaryInputPlaceTv.text=diary.place
        binding.diaryTitleTv.text=diary.title
        binding.diaryContentsEt.setText(diary.content)
        context?.resources?.let { binding.itemDiaryCategoryColorIv.background.setTint(it.getColor(diary.categoryColor)) }

        binding.diaryTodayDayTv.text=SimpleDateFormat("EE").format(diary.date)
        binding.diaryTodayNumTv.text=SimpleDateFormat("dd").format(diary.date)

        binding.diaryEditTv.setOnClickListener {
            updateDiary()
            view?.findNavController()?.navigate(R.id.diaryFragment)
            hideBottomNavigation(false)
        }

        binding.diaryDeleteIv.setOnClickListener {
            deleteDiary()
            view?.findNavController()?.navigate(R.id.diaryFragment)
            hideBottomNavigation(false)
        }
    }

    private fun updateDiary(){
        Thread{
            diary.content= binding.diaryContentsEt.text.toString()
            db.diaryDao.updateDiary(diary)
        }.start()
    }

    private fun deleteDiary(){
        Thread{
            db.diaryDao.deleteDiary(diary)
        }.start()
    }

    private fun charCnt(){
        with(binding) {
            diaryContentsEt.addTextChangedListener(object : TextWatcher {
                var maxText=""
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                    maxText=s.toString()
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if(diaryContentsEt.length() > 200){
                        Toast.makeText(requireContext(),"최대 200자까지 입력 가능합니다",
                            Toast.LENGTH_SHORT).show()

                        diaryContentsEt.setText(maxText)
                        diaryContentsEt.setSelection(diaryContentsEt.length())
                        textNumTv.text="${diaryEditTv.length()} / 200"
                    } else { textNumTv.text="${diaryEditTv.length()} / 200"}
                }
                override fun afterTextChanged(s: Editable?) {
                }

            })
        }
    }

    private fun hideBottomNavigation( bool : Boolean){
        val bottomNavigationView : BottomNavigationView = requireActivity().findViewById(R.id.nav_bar)
        if(bool) {
            bottomNavigationView.visibility = View.GONE
        } else {
            bottomNavigationView.visibility = View.VISIBLE
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        _binding = null
        hideBottomNavigation(false)
    }
}