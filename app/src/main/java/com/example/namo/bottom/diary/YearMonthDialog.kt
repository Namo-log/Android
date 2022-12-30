import android.app.AlertDialog
import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.example.namo.R
import com.example.namo.databinding.DialogSetMonthBinding

import java.util.*


class YearMonthDialog(v: View): DialogFragment() {

    private val MAX_YEAR = 2099
    private val MIN_YEAR = 2000
    lateinit var binding: DialogSetMonthBinding

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder =
            AlertDialog.Builder(activity)
        val inflater = requireActivity().layoutInflater

        binding= DataBindingUtil.inflate(inflater, R.layout.dialog_set_month,null,false)

        var cal = Calendar.getInstance()

        binding.monthPicker.value=cal.get(Calendar.MONTH)
        binding.yearPicker.value=cal.get(Calendar.YEAR)


        binding.acceptBtn.setOnClickListener {
            // onClickedListener.onClicked(binding.monthPicker.value,binding.yearPicker.value)
            dismiss()
        }
        binding.cancelBtn.setOnClickListener{
            dismiss()
        }

        binding.monthPicker.minValue = 1
        binding.monthPicker.maxValue = 12
        binding. monthPicker.value = cal[Calendar.MONTH] + 1
        val year = cal[Calendar.YEAR]
        binding.yearPicker.minValue = MIN_YEAR
        binding.yearPicker.maxValue = MAX_YEAR
        binding.yearPicker.value = year

        builder.setView(binding.root)

        return builder.create()
    }


    interface ButtonClickListener {
        fun onClicked(year:Int,month:Int)
    }

    private lateinit var onClickedListener: ButtonClickListener

    fun setOnClickedListener(listener: ButtonClickListener) {
        onClickedListener = listener
    }

}