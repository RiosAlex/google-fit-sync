package health.brook.googlefitsync.utils

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.*

class DateUtils {
    companion object {
        @SuppressLint("SimpleDateFormat")
        fun getNow (): String {
            val formatter = SimpleDateFormat("dd/MM/yyyy hh:mm a")
            val calendar: Calendar = Calendar.getInstance()
            return formatter.format(calendar.time)
        }

        @SuppressLint("SimpleDateFormat")
        fun readingDate(date: Long): String {
            val formatter = SimpleDateFormat("dd/MM/yyyy")
            val calendar: Calendar = Calendar.getInstance()
            calendar.setTimeInMillis(date)
            return formatter.format(calendar.getTime())
        }
    }
}