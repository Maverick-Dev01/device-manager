package com.maverick.adminapp.ui.device

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import com.maverick.adminapp.R
import com.maverick.adminapp.model.Device
import com.google.android.material.textview.MaterialTextView

class ViewDeviceFragment : Fragment() {

    private lateinit var deviceId: String
    private val firestore = FirebaseFirestore.getInstance()

    private lateinit var txtNombreCliente: MaterialTextView
    private lateinit var txtMarcaModelo: MaterialTextView
    private lateinit var txtPrecio: MaterialTextView
    private lateinit var txtMontoPagar: MaterialTextView
    private lateinit var txtFrecuenciaPago: MaterialTextView
    private lateinit var txtPeriodoPago: MaterialTextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_view_device, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Recuperar ID del dispositivo
        arguments?.let {
            deviceId = it.getString("deviceId") ?: ""
        }

        // Inicializar vistas
        txtNombreCliente = view.findViewById(R.id.txtNombreCliente)
        txtMarcaModelo = view.findViewById(R.id.txtMarcaModelo)
        txtPrecio = view.findViewById(R.id.txtPrecio)
        txtMontoPagar = view.findViewById(R.id.txtMontoPagar)
        txtFrecuenciaPago = view.findViewById(R.id.txtFrecuenciaPago)
        txtPeriodoPago = view.findViewById(R.id.txtPeriodoPago)



        // Cargar los datos
        if (deviceId.isNotEmpty()) {
            loadDeviceData(deviceId)
        } else {
            Toast.makeText(requireContext(), "Error al cargar el dispositivo", Toast.LENGTH_SHORT).show()
        }


    }

    private fun loadDeviceData(deviceId: String) {
        firestore.collection("dispositivos").document(deviceId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val device = document.toObject(Device::class.java)

                    // Asignar datos a las vistas
                    txtNombreCliente.text = device?.cliente ?: "Sin nombre"
                    txtMarcaModelo.text = "${device?.marca ?: "Marca"} - ${device?.modelo ?: "Modelo"}"
                    txtPrecio.text = "Precio: $${device?.precio ?: 0.0}"
                    txtMontoPagar.text = "Monto a Pagar: $${device?.montoAPagar ?: 0.0}"
                    txtFrecuenciaPago.text = "Frecuencia de Pago: ${device?.frecuenciaPago ?: "N/A"}"
                    txtPeriodoPago.text = "Periodo: ${device?.fechaInicio ?: "???"} - ${device?.fechaFin ?: "???"}"
                } else {
                    Toast.makeText(requireContext(), "Dispositivo no encontrado", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error al obtener los datos", Toast.LENGTH_SHORT).show()
            }
    }

}
