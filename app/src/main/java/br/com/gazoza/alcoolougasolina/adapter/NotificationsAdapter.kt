package br.com.gazoza.alcoolougasolina.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import br.com.gazoza.alcoolougasolina.activity.NotificationDetailsActivity
import br.com.gazoza.alcoolougasolina.databinding.ItemNotificationBinding
import br.com.gazoza.alcoolougasolina.util.*
import org.json.JSONArray
import org.json.JSONObject

class NotificationsAdapter(
    private val activity: Context
) : RecyclerView.Adapter<NotificationsAdapter.ViewHolder>() {

    private var itemsArr: JSONArray? = null

    @SuppressLint("NotifyDataSetChanged")
    fun setData(data: JSONArray?) {
        if (data != null) {
            itemsArr = data
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemNotificationBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
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
            if (isDebug()) e.printStackTrace()
        }
        return 0
    }

    inner class ViewHolder internal constructor(private val binding: ItemNotificationBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun setData(itemObj: JSONObject) = with(binding) {
            val title = itemObj.getStringVal(API_TITLE)
            val body = itemObj.getStringVal(API_BODY)
            val date = itemObj.getStringVal(API_DATE)

            tvTitle.text = title
            tvMessage.text = body
            tvDate.text = date.formatDate()

            itemView.setOnClickListener {
                val intent = Intent(activity, NotificationDetailsActivity::class.java).apply {
                    putExtra(PARAM_ITEM_ID, itemObj.getStringVal(API_ID))
                }
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                activity.startActivity(intent)
            }
        }
    }
}
