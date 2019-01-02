package com.runapp.runup

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import java.util.*

class HistoryRecyclerViewAdapter(private val dataset: List<Record>, private val listener: OnRecyclerViewListener, private val activity: Context) :
    RecyclerView.Adapter<HistoryRecyclerViewAdapter.HistoryViewHolder>() {

    class HistoryViewHolder(itemView: RelativeLayout) : RecyclerView.ViewHolder(itemView) {
        val titleView: TextView = itemView.findViewById(R.id.history_title_text)
        val timeView: TextView = itemView.findViewById(R.id.history_time_text)
        val distanceView: TextView = itemView.findViewById(R.id.history_distance_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.history_item_view, parent, false) as RelativeLayout
        return HistoryViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val record = dataset[position]

        val cal = Calendar.getInstance().also { it.time = record.startDate }
        holder.titleView.text = String.format(activity.getString(R.string.str_history_title),
            cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH),
            cal.get(Calendar.HOUR), cal.get(Calendar.MINUTE))
        holder.timeView.text = String.format(activity.getString(R.string.str_history_time), record.duration / 60, record.duration % 60)
        holder.distanceView.text = String.format(activity.getString(R.string.str_history_distance), record.distance)

        holder.itemView.setOnClickListener {
            listener.onRecyclerViewClick(it, position)
        }
    }

    override fun getItemCount(): Int = dataset.size
}

interface OnRecyclerViewListener {
    fun onRecyclerViewClick(v: View, position: Int)
}
