package fr.isen.calvet.androidsmartdevice

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AdapterDevicesList(var devices: ScanActivity.Devices, val onItemClickListener: (ScanActivity.Devices, Int) -> Unit) : RecyclerView.Adapter<AdapterDevicesList.DevicesListViewHolder>() {
    class DevicesListViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val deviceName: TextView = view.findViewById(R.id.name)
        val MACAddress : TextView = view.findViewById(R.id.MAC)
        val distance : TextView = view.findViewById(R.id.distance)
        val deviceImg : ImageView = view.findViewById(R.id.imageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DevicesListViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_ble_devices, parent, false)

        return DevicesListViewHolder(view)
    }

    override fun onBindViewHolder(holder: DevicesListViewHolder, position: Int) {
        val name = devices.device_name[position]
        holder.deviceName.text = name
        val MAC = devices.MAC[position]
        holder.MACAddress.text = MAC
        if(devices.distance[position] >= -65) {
            holder.deviceImg.setImageResource(R.drawable.rond_bleu)
        } else {
            holder.deviceImg.setImageResource(R.drawable.rond_bleu_clair)
        }
        holder.distance.text = devices.distance[position].toString()

        holder.itemView.setOnClickListener {
            onItemClickListener(devices, position)
        }
    }

    override fun getItemCount(): Int = devices.size
}
