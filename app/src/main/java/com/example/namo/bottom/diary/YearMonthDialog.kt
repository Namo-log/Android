import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import com.example.namo.databinding.DialogSetMonthBinding
import org.joda.time.DateTime


class YearMonthDialog(
    private var millis:Long,
    private val okCallback : (DateTime) -> Unit
) : DialogFragment(), View.OnClickListener{

    private val maxYear = 2099
    private val minYear = 2000

    lateinit var binding: DialogSetMonthBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = DialogSetMonthBinding.inflate(inflater, container, false)

        initView()

        return binding.root
    }

    private fun initView(){

        binding.apply {

            dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))  //배경 투명하게
            dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)  //dialog 모서리 둥글게

            monthPicker.minValue = 1
            monthPicker.maxValue = 12

            yearPicker.minValue = minYear
            yearPicker.maxValue = maxYear

            val date = DateTime(millis)
            yearPicker.value = date.year
            monthPicker.value = date.monthOfYear

            onClickListener()
        }
    }

    @SuppressLint("SuspiciousIndentation")
    private fun onClickListener(){

        binding.apply {

            acceptBtn.setOnClickListener {
                val date = DateTime(binding.yearPicker.value, binding.monthPicker.value, 1, 0, 0)
                okCallback(date)
                dismiss()
            }

            cancelBtn.setOnClickListener {
                okCallback(DateTime(millis))
                dismiss()
            }
        }
    }

    /** 다이얼로그 화면 외 클릭시 사라짐 **/
    override fun onClick(p0: View?) {
        dismiss()
    }
}