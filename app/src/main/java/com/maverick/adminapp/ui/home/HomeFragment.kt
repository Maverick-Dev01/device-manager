package com.maverick.adminapp.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import com.maverick.adminapp.R
import com.maverick.adminapp.adapters.DeviceAdapter
import com.maverick.adminapp.model.Device

class HomeFragment : Fragment() {

    private lateinit var fabAddDevice: FloatingActionButton
    private lateinit var recyclerDevices: RecyclerView
    private lateinit var deviceAdapter: DeviceAdapter
    private val firestore = FirebaseFirestore.getInstance()
    private val deviceList = mutableListOf<Device>() // Ahora sí es mutable

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Ocultar la barra superior
        (activity as AppCompatActivity).supportActionBar?.hide()

        // Inicializar vistas
        recyclerDevices = view.findViewById(R.id.recyclerViewDevices)
        fabAddDevice = view.findViewById(R.id.fab_add_device)

        setupRecyclerView()
        loadDevicesFromFirestore()

        fabAddDevice.setOnClickListener {
            // Navegar a la pantalla de agregar dispositivo
            findNavController().navigate(R.id.action_homeFragment_to_addDeviceFragment)
        }
    }

    override fun onResume() {
        super.onResume()
        loadDevicesFromFirestore()
    }

    private fun setupRecyclerView() {
        deviceAdapter = DeviceAdapter(deviceList) { selectedDevice ->
            val bundle = Bundle().apply {
                putString("deviceId", selectedDevice.id)
            }
            findNavController().navigate(R.id.action_homeFragment_to_viewDeviceFragment, bundle)
        }

        recyclerDevices.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = deviceAdapter
        }
    }

    private fun loadDevicesFromFirestore() {
        firestore.collection("dispositivos")
            .get()
            .addOnSuccessListener { result ->
                val devices = result.documents.mapNotNull { doc ->
                    try {
                        val data = doc.data ?: return@mapNotNull null

                        Device(
                            id = doc.id,
                            imei = data["imei"] as? String ?: "",
                            marca = data["marca"] as? String ?: "",
                            modelo = data["modelo"] as? String ?: "",
                            cliente = data["cliente"] as? String ?: "",
                            ciudad = data["ciudad"] as? String ?: "",
                            telefono = data["telefono"] as? String ?: "",
                            precio = (data["precio"] as? Number)?.toDouble() ?: 0.0,
                            frecuenciaPago = data["frecuenciaPago"] as? String ?: "",
                            periodoPago = data["periodoPago"] as? String ?: "",
                            fechaInicio = data["fechaInicio"] as? String ?: "",
                            fechaFin = data["fechaFin"] as? String ?: "",
                            montoAPagar = (data["montoAPagar"] as? Number)?.toDouble() ?: 0.0, // 🔹 Conversión segura
                            estado = (data["estado"] as? String)?.equals("bloqueado", ignoreCase = true) ?: false
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                        null
                    }
                }
                deviceList.clear()
                deviceList.addAll(devices)
                deviceAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
            }
    }

}
