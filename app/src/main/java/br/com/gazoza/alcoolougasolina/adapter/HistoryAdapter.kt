package br.com.gazoza.alcoolougasolina.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import br.com.gazoza.alcoolougasolina.R
import br.com.gazoza.alcoolougasolina.domain.Comparation
import br.com.gazoza.alcoolougasolina.util.formatDatetime
import io.realm.RealmResults
import org.jetbrains.anko.find

class HistoryAdapter(private val context: Context) :
        RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    private var comparations: RealmResults<Comparation>? = null

    fun setData(data: RealmResults<Comparation>?) {
        comparations = data
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater
                .from(context)
                .inflate(R.layout.item_comparation, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setData(comparations!![position])
    }

    override fun getItemCount(): Int {
        if (comparations == null) {
            return 0
        }
        return comparations!!.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var ivIcon = itemView.find<ImageView>(R.id.iv_icon)
        private var tvDate = itemView.find<TextView>(R.id.tv_date)
        private var tvEthanol = itemView.find<TextView>(R.id.tv_ethanol)
        private var tvGasoline = itemView.find<TextView>(R.id.tv_gasoline)
        private var tvResult = itemView.find<TextView>(R.id.tv_result)

        fun setData(comparation: Comparation?) {
            if (comparation != null) {
                val icon: Int
                val text: Int

                if (comparation.proportion < 0.7) {
                    icon = R.drawable.ic_ethanol_36dp
                    text = R.string.msg_use_ethanol
                } else {
                    icon = R.drawable.ic_gasoline_36dp
                    text = R.string.msg_use_gasoline
                }

                ivIcon.setImageResource(icon)

                tvDate.text = comparation.millis.formatDatetime()
                tvEthanol.text = context.getString(R.string.label_ethanol, comparation.priceEthanol)
                tvGasoline.text = context.getString(R.string.label_gasoline, comparation.priceGasoline)

                val result = "${context.getString(text)} (${comparation.percentage}%)"

                tvResult.text = result
            }
        }
    }
}
