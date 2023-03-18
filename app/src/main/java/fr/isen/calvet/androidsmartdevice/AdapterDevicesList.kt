package fr.isen.calvet.androidsmartdevice

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AdapterDevicesList(var devices: ArrayList<String>) : RecyclerView.Adapter<AdapterDevicesList.DevicesListViewHolder>() {
    class DevicesListViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val deviceName: TextView = view.findViewById(R.id.name)
        val MACAddress : TextView = view.findViewById(R.id.MAC)
        val deviceImg : ImageView = view.findViewById(R.id.imageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DevicesListViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_ble_devices, parent, false)

        return DevicesListViewHolder(view)
    }

    override fun onBindViewHolder(holder: DevicesListViewHolder, position: Int) {
        val device = devices[position]
        holder.deviceName.text = device
        //holder.deviceName.text = device.name

    }

    override fun getItemCount(): Int = devices.size
}
