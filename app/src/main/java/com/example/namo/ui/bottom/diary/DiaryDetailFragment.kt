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
import androidx.navigation.fragment.navArgs
import com.example.namo.R
import com.example.namo.databinding.FragmentDiaryDetailBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.ArrayList


class DiaryDetailFragment : Fragment() {

    private var _binding: FragmentDiaryDetailBinding? = null
    private val binding get() = _binding!!
    private var diaryData = ArrayList<Diary>()
    private var galleryData=ArrayList<Gallery>()

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentDiaryDetailBinding.inflate(inflater, container, false)

       // hideBottomNavigation(true)

        val args by navArgs<DiaryDetailFragmentArgs>()
        val position=args.position

//        binding.diaryTitleTv.text=diaryData[position].title
//        binding.diaryContentsEt.text=diaryData[position].contents
//        binding.diaryInputDateTv.text= diaryData[position].date.toString()
//        binding.diaryGallerySavedRy.layoutManager=
//            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
//
//        val listAdapter = com.example.namo.ui.bottom.diary.adapter.DiaryGalleryRVAdapter(galleryData)
//        binding.diaryGallerySavedRy.adapter = listAdapter


        binding.diaryBackIv.setOnClickListener { view ->
            view.findNavController().navigate(R.id.diaryFragment)
            hideBottomNavigation(false)
        }

        charCnt()

        return binding.root
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