package com.maverick.adminapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.maverick.adminapp.R
import com.maverick.adminapp.model.Device
import com.google.android.material.textview.MaterialTextView
import com.google.android.material.imageview.ShapeableImageView

class DeviceAdapter(private var deviceList: MutableList<Device>, private val onItemClick: (Device) -> Unit)
    : RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder>() {

    class DeviceViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtClient: MaterialTextView = view.findViewById(R.id.txtClientName)
        val txtBrandModel: MaterialTextView = view.findViewById(R.id.txtBrandModel)
        val txtPrice: MaterialTextView = view.findViewById(R.id.txtPrice)
        val imgStatus: ShapeableImageView = view.findViewById(R.id.imgStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_device, parent, false)
        return DeviceViewHolder(view)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        val device = deviceList[position]

        holder.txtClient.text = device.cliente
        holder.txtBrandModel.text = "${device.marca} - ${device.modelo}"
        holder.txtPrice.text = "Precio: $${device.precio}"

        // Cambiar icono según el estado
        holder.imgStatus.setImageResource(
            if (device.estado) R.drawable.ic_phone_unlock else R.drawable.ic_phone_lock
        )

        holder.itemView.setOnClickListener { onItemClick(device) }
    }

    fun updateList(newDevices: List<Device>) {
        deviceList.clear() // 🔹 Ahora sí funcionará
        deviceList.addAll(newDevices)
        notifyDataSetChanged()
    }
    override fun getItemCount() = deviceList.size
}
