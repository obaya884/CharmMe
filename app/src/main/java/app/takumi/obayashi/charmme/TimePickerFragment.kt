package app.takumi.obayashi.charmme

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.activity_add.*
import java.text.SimpleDateFormat
import java.util.*

class TimePickerFragment : DialogFragment(), TimePickerDialog.OnTimeSetListener {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        // Create a new instance of TimePickerDialog and return it
        return TimePickerDialog(
            activity,
            this,
            0,
            0,
            true
        )
    }

    override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int) {
        // Do something with the time chosen by the user
        val calender = Calendar.getInstance()
        calender.set(0, 0, 0, hourOfDay, minute)
        val setTime = SimpleDateFormat("HH:mm").format(calender.time)
        timeEditText.setText(setTime)
    }
}