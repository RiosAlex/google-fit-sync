package health.brook.googlefitsync.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import health.brook.googlefitsync.R
import health.brook.googlefitsync.models.BloodReading
import health.brook.googlefitsync.utils.DateUtils

class BloodReadingAdapter(private val readingList: List<BloodReading>) : RecyclerView.Adapter<BloodReadingAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val systolic: TextView = itemView.findViewById(R.id.systolicTextView)
        val diastolic: TextView = itemView.findViewById(R.id.diastolicTextView)
        val readingDate: TextView = itemView.findViewById(R.id.readingDateTextView)
        val warningSign: ImageView = itemView.findViewById(R.id.warningImageView)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_reading, parent, false)
        return ViewHolder(view)

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val systolic = holder.itemView.context.getString(R.string.systolic, readingList[position].systolicAverage)
        val diastolic = holder.itemView.context.getString(R.string.diastolic, readingList[position].diastolicAverage)

        holder.systolic.text = systolic
        holder.diastolic.text = diastolic
        holder.readingDate.text = DateUtils.readingDate(readingList[position].date)

        if (readingList[position].systolicAverage >= 120) {
            holder.warningSign.visibility = View.VISIBLE
            holder.systolic.setTextColor(Color.RED)
        }

        if (readingList[position].diastolicAverage >= 80) {
            holder.warningSign.visibility = View.VISIBLE
            holder.diastolic.setTextColor(Color.RED)
        }

    }

    override fun getItemCount(): Int {
        return readingList.size
    }

}