package com.gradu.presentation.community.moim.diary.adapter

import android.annotation.SuppressLint
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gradu.core.utils.converter.DiaryDateConverter
import com.gradu.domain.model.DiaryDetail
import com.gradu.domain.model.DiaryImage
import com.gradu.presentation.R
import com.gradu.presentation.databinding.ItemMoimDiaryActivityBinding
import com.gradu.presentation.databinding.ItemMoimDiaryDiaryBinding
import java.util.Calendar
import java.util.Date

class MoimDiaryVPAdapter(
    private val diaryEventListener: OnDiaryEventListener,
    private val activityEventListener: OnActivityEventListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val activities = mutableListOf<com.gradu.domain.model.Activity>()
    private var diary = DiaryDetail("", 0L, emptyList(), 3)
    private var isEditMode: Boolean = false  // 편집 모드 상태 저장
    private var hasDiary: Boolean = false
    private var scheduleStartDate: String = ""
    private var scheduleEndDate: String = ""

    fun updateDiary(diary: DiaryDetail) {
        this.diary = diary
        notifyItemChanged(0)
    }

    fun setScheduleDate(startDate: String, endDate: String) {
        this.scheduleStartDate = startDate
        this.scheduleEndDate = endDate
    }

    @SuppressLint("NotifyDataSetChanged")
    fun submitActivities(newActivities: List<com.gradu.domain.model.Activity>) {
        activities.clear()
        activities.addAll(newActivities)
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setEditMode(isEditMode: Boolean) {
        this.isEditMode = isEditMode
        notifyDataSetChanged()
    }

    fun setHasDiary(hasDiary: Boolean) {
        this.hasDiary = hasDiary
        notifyItemChanged(0) // 일기장
    }

    override fun getItemCount(): Int = 1 + activities.size // diary 하나 + activities

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) VIEW_TYPE_DIARY else VIEW_TYPE_ACTIVITY
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_DIARY -> {
                DiaryViewHolder(
                    ItemMoimDiaryDiaryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                )
            }
            VIEW_TYPE_ACTIVITY -> {
                ActivityViewHolder(
                    ItemMoimDiaryActivityBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                )
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ActivityViewHolder) {
            val activity = activities[position - 1] // 첫 번째는 diary이므로 인덱스 조정
            holder.bind(activity)
        } else if (holder is DiaryViewHolder) {
            holder.bind(diary)
        }
    }

    inner class DiaryViewHolder(private val binding: ItemMoimDiaryDiaryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(diary: DiaryDetail) {
            binding.diary = diary
            binding.isEdit = isEditMode
            binding.hasDiary = hasDiary

            binding.diaryPrivateGuideBtn.setOnClickListener{ diaryEventListener.onGuideClicked() }
            binding.diaryEditBtn.setOnClickListener { diaryEventListener.onEditModeClicked() }
            binding.diaryViewBtn.setOnClickListener { diaryEventListener.onViewModeClicked() }
            binding.diaryDeleteBtn.setOnClickListener { diaryEventListener.onDeleteDiary() }
            // Content 변경 이벤트 처리
            binding.diaryContentEt.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    diaryEventListener.onContentChanged(s.toString())
                }
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })

            // Enjoy 클릭 이벤트 처리
            binding.diaryEnjoy1Iv.setOnClickListener { diaryEventListener.onEnjoyClicked(1) }
            binding.diaryEnjoy2Iv.setOnClickListener { diaryEventListener.onEnjoyClicked(2) }
            binding.diaryEnjoy3Iv.setOnClickListener { diaryEventListener.onEnjoyClicked(3) }

            binding.diaryAddImageIv.setOnClickListener { diaryEventListener.onAddImageClicked() }

            // 이미지 리스트 어댑터 설정
            val adapter = MoimDiaryImagesRVAdapter(
                itemClickListener = { diaryEventListener.onImageClicked(diary.diaryImages) },
                deleteClickListener = { diaryImage -> diaryEventListener.onDeleteImage(diaryImage) },
                isEditMode
            )
            binding.diaryImagesRv.apply {
                this.adapter = adapter
                layoutManager = LinearLayoutManager(binding.root.context, LinearLayoutManager.HORIZONTAL, false)
                itemAnimator = null
            }
            adapter.addItem(diary.diaryImages)
        }
    }

    inner class ActivityViewHolder(
        private val binding: ItemMoimDiaryActivityBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(activity: com.gradu.domain.model.Activity) {
            binding.activity = activity
            binding.isEdit = isEditMode
            binding.hasDiary = hasDiary

            if (!isEditMode) {
                closeAllDateViews()
            }

            binding.activityEditBtn.setOnClickListener { activityEventListener.onEditModeClicked() }
            binding.activityViewBtn.setOnClickListener { activityEventListener.onViewModeClicked() }

            binding.activityDeleteBtn.setOnClickListener {
                activityEventListener.onDeleteActivity(bindingAdapterPosition - 1)
            }

            binding.activityTitleTv.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    activityEventListener.onActivityNameChanged(s.toString(), bindingAdapterPosition - 1)
                }
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })

            // 시작 날짜 클릭 시
            binding.activityStartDateTv.setOnClickListener {
                handleDateViews(binding.activityStartDateLayout, binding.activityStartDateTv)
            }
            // 시작 시간 클릭 시
            binding.activityStartTimeTv.setOnClickListener {
                handleDateViews(binding.activityStartTimeLayout, binding.activityStartTimeTv)
            }
            // 종료 날짜 클릭 시
            binding.activityEndDateTv.setOnClickListener {
                handleDateViews(binding.activityEndDateLayout, binding.activityEndDateTv)
            }
            // 종료 시간 클릭 시
            binding.activityEndTimeTv.setOnClickListener {
                handleDateViews(binding.activityEndTimeLayout, binding.activityEndTimeTv)
            }

            initPickerListeners(activity)

            binding.activityPlaceTv.setOnClickListener {
                activityEventListener.onLocationClicked(bindingAdapterPosition - 1)
            }

            binding.activityAddImageIv.setOnClickListener {
                activityEventListener.onAddImageClicked(bindingAdapterPosition - 1)
            }

            // 이미지 리스트 어댑터 설정
            val adapter = MoimDiaryImagesRVAdapter(
                itemClickListener = { activityEventListener.onImageClicked(activity.images) },
                deleteClickListener = { activityImage -> activityEventListener.onDeleteImage(bindingAdapterPosition - 1, activityImage) },
                isEditMode
            )
            binding.activityImagesRv.apply {
                this.adapter = adapter
                layoutManager = LinearLayoutManager(binding.root.context, LinearLayoutManager.HORIZONTAL, false)
                itemAnimator = null
            }
            adapter.addItem(activity.images)

            binding.activityParticipantsTv.setOnClickListener {
                activityEventListener.onParticipantsClicked(bindingAdapterPosition - 1)
            }

            binding.activityPaymentTv.setOnClickListener {
                activityEventListener.onPayClicked(bindingAdapterPosition - 1)
            }
        }

        private fun initPickerListeners(activity: com.gradu.domain.model.Activity) {
            var selectedStartDate = activity.startDate
            var selectedEndDate = activity.endDate

            // 초기 표시값 설정
            binding.activityStartDateTv.text = DiaryDateConverter.toDate(selectedStartDate)
            binding.activityStartTimeTv.text = DiaryDateConverter.to12HourTime(selectedStartDate)
            binding.activityEndDateTv.text = DiaryDateConverter.toDate(selectedEndDate)
            binding.activityEndTimeTv.text = DiaryDateConverter.to12HourTime(selectedEndDate)

            // 시작 날짜와 시간 파싱
            val startDateTime = selectedStartDate.split("T")
            val startDateParts = startDateTime[0].split("-")
            val startTimeParts = startDateTime[1].split(":")

            // 종료 날짜와 시간 파싱
            val endDateTime = selectedEndDate.split("T")
            val endDateParts = endDateTime[0].split("-")
            val endTimeParts = endDateTime[1].split(":")

            // 일정의 시작 및 종료 날짜와 시간을 파싱하여 Date 객체로 변환
            val scheduleStartDateTime = DiaryDateConverter.parseDate(scheduleStartDate) ?: Date()
            val scheduleEndDateTime = DiaryDateConverter.parseDate(scheduleEndDate) ?: Date()
            val scheduleStartMillis = scheduleStartDateTime.time
            val scheduleEndMillis = scheduleEndDateTime.time

            // 시작 날짜 설정
            binding.activityStartDateDp.init(
                startDateParts[0].toInt(),
                startDateParts[1].toInt() - 1,
                startDateParts[2].toInt()
            ) { _, year, monthOfYear, dayOfMonth ->
                val datePart = String.format("%04d-%02d-%02d", year, monthOfYear + 1, dayOfMonth)
                val timePart = selectedStartDate.substring(11)  // "HH:mm:ss"
                val updatedStartDate = "${datePart}T${timePart}"

                selectedStartDate = updatedStartDate

                // 텍스트뷰 업데이트
                binding.activityStartDateTv.text = DiaryDateConverter.toDate(selectedStartDate)

                // 시작 날짜가 종료 날짜보다 이후인 경우 종료 날짜를 시작 날짜로 맞춤
                if (selectedStartDate > selectedEndDate) {
                    selectedEndDate = selectedStartDate
                    binding.activityEndDateDp.updateDate(year, monthOfYear, dayOfMonth)
                    binding.activityEndDateTv.text = DiaryDateConverter.toDate(selectedEndDate)
                    activityEventListener.onEndDateSelected(bindingAdapterPosition - 1, selectedEndDate)
                }
                activityEventListener.onStartDateSelected(bindingAdapterPosition - 1, selectedStartDate)
            }

            // 시작 날짜 선택기에 최소 및 최대 날짜 설정
            binding.activityStartDateDp.minDate = scheduleStartDateTime.time
            binding.activityStartDateDp.maxDate = scheduleEndDateTime.time

            // 종료 날짜 설정
            binding.activityEndDateDp.init(
                endDateParts[0].toInt(),
                endDateParts[1].toInt() - 1,
                endDateParts[2].toInt()
            ) { _, year, monthOfYear, dayOfMonth ->
                val datePart = String.format("%04d-%02d-%02d", year, monthOfYear + 1, dayOfMonth)
                val timePart = selectedEndDate.substring(11)
                val updatedEndDate = "${datePart}T${timePart}"

                selectedEndDate = updatedEndDate

                // 텍스트뷰 업데이트
                binding.activityEndDateTv.text = DiaryDateConverter.toDate(selectedEndDate)

                // 종료 날짜가 시작 날짜보다 이전인 경우 시작 날짜를 종료 날짜로 맞춤
                if (selectedEndDate < selectedStartDate) {
                    selectedStartDate = selectedEndDate
                    binding.activityStartDateDp.updateDate(year, monthOfYear, dayOfMonth)
                    binding.activityStartDateTv.text = DiaryDateConverter.toDate(selectedStartDate)
                    activityEventListener.onStartDateSelected(bindingAdapterPosition - 1, selectedStartDate)
                }
                activityEventListener.onEndDateSelected(bindingAdapterPosition - 1, selectedEndDate)
            }

            // 종료 날짜 선택기에 최소 및 최대 날짜 설정
            binding.activityEndDateDp.minDate = scheduleStartDateTime.time
            binding.activityEndDateDp.maxDate = scheduleEndDateTime.time

            // 시작 시간 설정
            with(binding.activityStartTimeTp) {
                this.hour = startTimeParts[0].toInt()
                this.minute = startTimeParts[1].toInt()

                this.setOnTimeChangedListener { _, hourOfDay, minute ->
                    val timePart = String.format("%02d:%02d:%02d", hourOfDay, minute, 0)
                    val datePart = selectedStartDate.substring(0, 10)
                    val updatedStartDateTimeString = "${datePart}T${timePart}"
                    val updatedStartDateTime = DiaryDateConverter.parseDate(updatedStartDateTimeString) ?: Date()

                    val calendar = Calendar.getInstance()

                    // 선택된 시작 시간이 기록의 시작 시간보다 이전인 경우
                    if (updatedStartDateTime.time < scheduleStartMillis) {
                        // 기록의 시작 시간으로 설정
                        selectedStartDate = DiaryDateConverter.formatDateTime(scheduleStartDateTime)
                        calendar.time = scheduleStartDateTime
                        this.hour = calendar.get(Calendar.HOUR_OF_DAY)
                        this.minute = calendar.get(Calendar.MINUTE)
                        binding.activityStartTimeTv.text = DiaryDateConverter.to12HourTime(selectedStartDate)
                    }
                    // 선택된 시작 시간이 기록의 종료 시간보다 이후인 경우
                    else if (updatedStartDateTime.time > scheduleEndMillis) {
                        // 기록의 종료 시간으로 설정
                        selectedStartDate = DiaryDateConverter.formatDateTime(scheduleEndDateTime)
                        calendar.time = scheduleEndDateTime
                        this.hour = calendar.get(Calendar.HOUR_OF_DAY)
                        this.minute = calendar.get(Calendar.MINUTE)
                        binding.activityStartTimeTv.text = DiaryDateConverter.to12HourTime(selectedStartDate)
                    } else {
                        selectedStartDate = updatedStartDateTimeString
                        binding.activityStartTimeTv.text = DiaryDateConverter.to12HourTime(selectedStartDate)
                    }

                    // 시작 시간이 종료 시간보다 이후인 경우 종료 시간을 시작 시간으로 맞춤
                    if (selectedStartDate >= selectedEndDate) {
                        selectedEndDate = selectedStartDate
                        binding.activityEndTimeTp.hour = this.hour
                        binding.activityEndTimeTp.minute = this.minute
                        binding.activityEndTimeTv.text = binding.activityStartTimeTv.text
                        activityEventListener.onEndDateSelected(bindingAdapterPosition - 1, selectedEndDate)
                    }

                    activityEventListener.onStartDateSelected(bindingAdapterPosition - 1, selectedStartDate)
                }
            }

            // 종료 시간 설정
            with(binding.activityEndTimeTp) {
                this.hour = endTimeParts[0].toInt()
                this.minute = endTimeParts[1].toInt()

                this.setOnTimeChangedListener { _, hourOfDay, minute ->
                    val timePart = String.format("%02d:%02d:%02d", hourOfDay, minute, 0)
                    val datePart = selectedEndDate.substring(0, 10)
                    val updatedEndDateTimeString = "${datePart}T${timePart}"
                    val updatedEndDateTime = DiaryDateConverter.parseDate(updatedEndDateTimeString) ?: Date()

                    val calendar = Calendar.getInstance()

                    // 선택된 종료 시간이 기록의 시작 시간보다 이전인 경우
                    if (updatedEndDateTime.time < scheduleStartMillis) {
                        // 기록의 시작 시간으로 설정
                        selectedEndDate = DiaryDateConverter.formatDateTime(scheduleStartDateTime)
                        calendar.time = scheduleStartDateTime
                        this.hour = calendar.get(Calendar.HOUR_OF_DAY)
                        this.minute = calendar.get(Calendar.MINUTE)
                        binding.activityEndTimeTv.text = DiaryDateConverter.to12HourTime(selectedEndDate)
                    }
                    // 선택된 종료 시간이 기록의 종료 시간보다 이후인 경우
                    else if (updatedEndDateTime.time > scheduleEndMillis) {
                        // 기록의 종료 시간으로 설정
                        selectedEndDate = DiaryDateConverter.formatDateTime(scheduleEndDateTime)
                        calendar.time = scheduleEndDateTime
                        this.hour = calendar.get(Calendar.HOUR_OF_DAY)
                        this.minute = calendar.get(Calendar.MINUTE)
                        binding.activityEndTimeTv.text = DiaryDateConverter.to12HourTime(selectedEndDate)
                    } else {
                        selectedEndDate = updatedEndDateTimeString
                        binding.activityEndTimeTv.text = DiaryDateConverter.to12HourTime(selectedEndDate)
                    }

                    // 종료 시간이 시작 시간보다 이전인 경우 시작 시간을 종료 시간으로 맞춤
                    if (selectedEndDate <= selectedStartDate) {
                        selectedStartDate = selectedEndDate
                        binding.activityStartTimeTp.hour = this.hour
                        binding.activityStartTimeTp.minute = this.minute
                        binding.activityStartTimeTv.text = binding.activityEndTimeTv.text
                        activityEventListener.onStartDateSelected(bindingAdapterPosition - 1, selectedStartDate)
                    }

                    activityEventListener.onEndDateSelected(bindingAdapterPosition - 1, selectedEndDate)
                }
            }
        }

        private fun handleDateViews(targetMotionLayout: MotionLayout, selectedTextView: TextView) {
            // 모든 MotionLayout 리스트
            val allMotionLayouts = listOf(
                binding.activityStartDateLayout,
                binding.activityStartTimeLayout,
                binding.activityEndDateLayout,
                binding.activityEndTimeLayout
            )

            val allTextViews = listOf(
                binding.activityStartDateTv,
                binding.activityStartTimeTv,
                binding.activityEndDateTv,
                binding.activityEndTimeTv
            )

            // 다른 MotionLayout들은 닫기 및 텍스트뷰 색상 복원
            allMotionLayouts.forEach { layout ->
                if (layout != targetMotionLayout && layout.progress != 0f) {
                    layout.transitionToStart() // 닫기 애니메이션
                }
            }

            allTextViews.forEach { textView ->
                if (textView != selectedTextView) {
                    textView.setTextColor(textView.context.getColor(R.color.main_text)) // 다른 텍스트뷰는 기본 색상으로
                }
            }

            // 현재 MotionLayout이 열려있으면 닫고 텍스트 색상도 원래대로 복원
            if (targetMotionLayout.progress == 0f) {
                targetMotionLayout.transitionToEnd() // 열기 애니메이션
                selectedTextView.setTextColor(selectedTextView.context.getColor(R.color.main)) // 선택된 텍스트뷰 색상 변경
            } else {
                targetMotionLayout.transitionToStart() // 닫기 애니메이션
                selectedTextView.setTextColor(selectedTextView.context.getColor(R.color.main_text)) // 다시 클릭 시 색상 복원
            }
        }

        private fun closeAllDateViews() {
            val allMotionLayouts = listOf(
                binding.activityStartDateLayout,
                binding.activityStartTimeLayout,
                binding.activityEndDateLayout,
                binding.activityEndTimeLayout
            )

            val allTextViews = listOf(
                binding.activityStartDateTv,
                binding.activityStartTimeTv,
                binding.activityEndDateTv,
                binding.activityEndTimeTv
            )

            // 모든 MotionLayout을 닫기
            allMotionLayouts.forEach { layout ->
                if (layout.progress != 0f) {
                    layout.transitionToStart()
                }
            }

            // 텍스트 색상을 기본 색상으로 복원
            allTextViews.forEach { textView ->
                textView.setTextColor(textView.context.getColor(R.color.main_text))
            }
        }
    }

    companion object {
        private const val VIEW_TYPE_DIARY = 0
        private const val VIEW_TYPE_ACTIVITY = 1
    }

    interface OnActivityEventListener {
        fun onAddImageClicked(position: Int)
        fun onDeleteActivity(position: Int)
        fun onActivityNameChanged(name: String, position: Int)
        fun onStartDateSelected(position: Int, date: String)
        fun onEndDateSelected(position: Int, date: String)
        fun onLocationClicked(position: Int)
        fun onParticipantsClicked(position: Int)
        fun onPayClicked(position: Int)
        fun onDeleteImage(position: Int, image: DiaryImage)
        fun onEditModeClicked()
        fun onViewModeClicked()
        fun onImageClicked(images: List<DiaryImage>)
    }

    interface OnDiaryEventListener {
        fun onGuideClicked()
        fun onAddImageClicked()
        fun onImageClicked(images: List<DiaryImage>)
        fun onContentChanged(content: String)
        fun onEnjoyClicked(enjoyRating: Int)
        fun onDeleteImage(image: DiaryImage)
        fun onEditModeClicked()
        fun onViewModeClicked()
        fun onDeleteDiary()
    }
}