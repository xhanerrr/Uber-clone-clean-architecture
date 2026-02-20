package com.example.signinregister.ui.map.vehiclelist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.signinregister.R
import com.example.signinregister.data.remote.Vehicle
import com.example.signinregister.databinding.ItemCarBinding

interface OnVehicleClickListener {
    fun onVehicleClicked(latitude: Double, longitude: Double)
}

class VehicleAdapter(
    private val clickListener: OnVehicleClickListener
) : ListAdapter<Vehicle, VehicleAdapter.VehicleViewHolder>(VehicleDiffCallback()) {
    class VehicleViewHolder(val binding: ItemCarBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(vehicle: Vehicle, clickListener: OnVehicleClickListener) {

            binding.vehicleNameTextView.text = "${vehicle.brand} ${vehicle.model}"

            binding.vehicleDetailsTextView.text = "Placa: ${vehicle.plate}"

            binding.vehicleImageView.load(vehicle.imageUrl) {
                crossfade(true)
                placeholder(R.drawable.ic_car)
                error(R.drawable.ic_car)
            }

            binding.root.setOnClickListener {
                clickListener.onVehicleClicked(vehicle.currentLat, vehicle.currentLng)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VehicleViewHolder {
        val binding = ItemCarBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VehicleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VehicleViewHolder, position: Int) {
        val vehicle = getItem(position)
        holder.bind(vehicle, clickListener)
    }
}

class VehicleDiffCallback : DiffUtil.ItemCallback<Vehicle>() {
    override fun areItemsTheSame(oldItem: Vehicle, newItem: Vehicle): Boolean {
        return oldItem.plate == newItem.plate
    }

    override fun areContentsTheSame(oldItem: Vehicle, newItem: Vehicle): Boolean {
        return oldItem == newItem
    }
}