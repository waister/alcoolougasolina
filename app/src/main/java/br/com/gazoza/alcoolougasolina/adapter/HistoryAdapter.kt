package br.com.gazoza.alcoolougasolina.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import br.com.gazoza.alcoolougasolina.R
import br.com.gazoza.alcoolougasolina.domain.Comparison
import br.com.gazoza.alcoolougasolina.util.formatDatetime
import io.realm.RealmResults
import org.jetbrains.anko.find

class HistoryAdapter(private val context: Context) :
    RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    private var comparisons: RealmResults<Comparison>? = null

    @SuppressLint("NotifyDataSetChanged")
    fun setData(data: RealmResults<Comparison>?) {
        comparisons = data
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater
            .from(context)
            .inflate(R.layout.item_comparation, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setData(comparisons!![position])
    }

    override fun getItemCount(): Int {
        if (comparisons == null) {
            return 0
        }
        return comparisons!!.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var ivIcon = itemView.find<ImageView>(R.id.iv_icon)
        private var tvDate = itemView.find<TextView>(R.id.tv_date)
        private var tvEthanol = itemView.find<TextView>(R.id.tv_ethanol)
        private var tvGasoline = itemView.find<TextView>(R.id.tv_gasoline)
        private var tvResult = itemView.find<TextView>(R.id.tv_result)

        fun setData(comparison: Comparison?) {
            if (comparison != null) {
                val icon: Int
                val text: Int

                if (comparison.proportion < 0.7) {
                    icon = R.drawable.ic_ethanol_36dp
                    text = R.string.msg_use_ethanol
                } else {
                    icon = R.drawable.ic_gasoline_36dp
                    text = R.string.msg_use_gasoline
                }

                ivIcon.setImageResource(icon)

                tvDate.text = comparison.timestamp.formatDatetime()
                tvEthanol.text = context.getString(R.string.label_ethanol, comparison.priceEthanol)
                tvGasoline.text =
                    context.getString(R.string.label_gasoline, comparison.priceGasoline)

                val result = "${context.getString(text)} (${comparison.percentage}%)"

                tvResult.text = result
            }
        }
    }
}
