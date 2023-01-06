import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.example.namo.R
import com.example.namo.databinding.DialogSetMonthBinding
import java.util.*

class YearMonthDialog : DialogFragment(), View.OnClickListener{

    private val maxYear = 2099
    private val minYear = 2000
    lateinit var binding: DialogSetMonthBinding
    private val cal: Calendar = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding= DataBindingUtil.inflate(inflater, R.layout.dialog_set_month,null,false)

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))  //배경 투명하게
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)  //dialog 모서리 둥글게

        binding.apply {

            monthPicker.setOnValueChangedListener { picker, oldVal, newVal ->
                month =  newVal
            }
           yearPicker.setOnValueChangedListener { picker, oldVal, newVal ->
               year =newVal
           }

            acceptBtn.setOnClickListener {
                acceptClick(true)
                dismiss()
            }

            cancelBtn.setOnClickListener {
                dismiss()
            }

            monthPicker.minValue = 1
            monthPicker.maxValue = 12

            yearPicker.minValue = minYear
            yearPicker.maxValue = maxYear
            monthPicker.value = cal.get(Calendar.MONTH)
            yearPicker.value = cal.get(Calendar.YEAR)
        }

            return binding.root
    }


    companion object {
        lateinit var acceptClick: (Boolean) -> Unit
        var month:Int=0
        var year:Int=0

        fun getInstance(acceptClick: (Boolean) -> Unit, month: Int, year: Int): YearMonthDialog {
            this.acceptClick = acceptClick
            this.month = month
            this.year=year

            return YearMonthDialog()
        }
    }

    /** 다이얼로그 화면 외 클릭시 사라짐 **/
    override fun onClick(p0: View?) {
        dismiss()
    }

}
