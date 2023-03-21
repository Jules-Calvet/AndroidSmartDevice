package fr.isen.calvet.androidsmartdevice

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AdapterDevicesList(/*var devices: ArrayList<String>*/ var devices: ScanActivity.Devices) : RecyclerView.Adapter<AdapterDevicesList.DevicesListViewHolder>() {
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
        if(devices.device_name.isNotEmpty() && devices.MAC.isNotEmpty()) {
            if(devices.device_name[position].isNullOrBlank()) {
                holder.deviceName.text = "Inconnu"
                val MAC = devices.MAC[position]
                holder.MACAddress.text = MAC
                //holder.deviceName.text = ""
                //holder.MACAddress.text = ""
            }
            else {
                val name = devices.device_name[position]
                holder.deviceName.text = name
                val MAC = devices.MAC[position]
                holder.MACAddress.text = MAC
            }

            //holder.deviceName.text = device.name
        }
    }

    override fun getItemCount(): Int = devices.size
}
