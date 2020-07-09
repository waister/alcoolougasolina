package br.com.gazoza.alcoolougasolina.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import br.com.gazoza.alcoolougasolina.R
import br.com.gazoza.alcoolougasolina.activity.NotificationDetailsActivity
import br.com.gazoza.alcoolougasolina.util.*
import org.jetbrains.anko.find
import org.jetbrains.anko.intentFor
import org.json.JSONArray
import org.json.JSONObject

class NotificationsAdapter(
        private val activity: Activity
) : RecyclerView.Adapter<NotificationsAdapter.MyViewHolder>() {

    private var itemsArr: JSONArray? = null

    fun setData(data: JSONArray?) {
        if (data != null) {
            itemsArr = data
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notification, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        if (itemsArr != null) {
            holder.setData(itemsArr!!.get(position) as JSONObject)
        }
    }

    override fun getItemCount(): Int {
        try {
            if (itemsArr != null) {
                return itemsArr!!.length()
            }
        } catch (e: IllegalStateException) {
        }
        return 0
    }

    inner class MyViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var tvTitle: TextView = itemView.find(R.id.tv_title)
        private var tvMessage: TextView = itemView.find(R.id.tv_message)
        private var tvDate: TextView = itemView.find(R.id.tv_date)

        fun setData(itemObj: JSONObject) {
            val title = itemObj.getStringVal(API_TITLE)
            val body = itemObj.getStringVal(API_BODY)
            val date = itemObj.getStringVal(API_DATE)

            tvTitle.text = title
            tvMessage.text = body
            tvDate.text = date.formatDatetime()

            itemView.setOnClickListener {
                activity.startActivity(activity.intentFor<NotificationDetailsActivity>(
                        PARAM_ITEM_ID to itemObj.getStringVal(API_ID)
                ))
            }
        }

    }

}