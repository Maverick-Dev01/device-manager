package com.maverick.adminapp.ui.device

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.firestore.FirebaseFirestore
import com.maverick.adminapp.R
import com.maverick.adminapp.model.Device
import com.google.android.material.textview.MaterialTextView
import com.maverick.adminapp.utils.NavigationHelper

class ViewDeviceFragment : Fragment() {

    private lateinit var deviceId: String
    private val firestore = FirebaseFirestore.getInstance()

    private lateinit var txtNombreCliente: MaterialTextView
    private lateinit var txtMarcaModelo: MaterialTextView
    private lateinit var txtPrecio: MaterialTextView
    private lateinit var txtMontoPagar: MaterialTextView
    private lateinit var txtFrecuenciaPago: MaterialTextView
    private lateinit var txtPeriodoPago: MaterialTextView


    private lateinit var imgBack : ImageView
    private lateinit var btnEditar: Button
    private lateinit var btnEliminar: Button
    private lateinit var btnBloquear: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_view_device, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 🔹 Recuperar ID del dispositivo
        deviceId = arguments?.getString("deviceId") ?: ""

        if (deviceId.isEmpty()) {
            Toast.makeText(requireContext(), "Error: ID de dispositivo no encontrado", Toast.LENGTH_LONG).show()
            findNavController().navigate(R.id.action_viewDeviceFragment_to_homeFragment)
            return
        }

        initViews(view)
        setupListener()
        loadDeviceData(deviceId)
    }

    private fun initViews(view: View) {
        txtNombreCliente = view.findViewById(R.id.txtNombreCliente)
        txtMarcaModelo = view.findViewById(R.id.txtMarcaModelo)
        txtPrecio = view.findViewById(R.id.txtPrecio)
        txtMontoPagar = view.findViewById(R.id.txtMontoPagar)
        txtFrecuenciaPago = view.findViewById(R.id.txtFrecuenciaPago)
        txtPeriodoPago = view.findViewById(R.id.txtPeriodoPago)

        btnEditar = view.findViewById(R.id.btnEditar)
        btnBloquear = view.findViewById(R.id.btnBloquear)
        btnEliminar = view.findViewById(R.id.btnEliminar)

        imgBack = view.findViewById(R.id.btnBack)

    }

    private fun loadDeviceData(deviceId: String) {
        firestore.collection("dispositivos").document(deviceId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val data = document.data ?: return@addOnSuccessListener

                    // Convertir manualmente `estado`
                    val estadoStr = data["estado"] as? String ?: "activo"
                    val estadoBoolean = estadoStr == "bloqueado" // Si es "bloqueado", será `true`

                    val device = Device(
                        id = document.id,
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
                        montoAPagar = (data["montoAPagar"] as? Number)?.toDouble() ?: 0.0,
                        estado = estadoBoolean  // 🔹 Convertido manualmente
                    )

                    // Asignar datos a las vistas
                    txtNombreCliente.text = device.cliente
                    txtMarcaModelo.text = "${device.marca} - ${device.modelo}"
                    txtPrecio.text = "Precio: $${device.precio}"
                    txtMontoPagar.text = "Monto a Pagar: $${device.montoAPagar}"
                    txtFrecuenciaPago.text = "Frecuencia de Pago: ${device.frecuenciaPago}"
                    txtPeriodoPago.text = "Periodo: ${device.fechaInicio} - ${device.fechaFin}"

                    actualizarInterfazBloqueo(estadoBoolean)
                } else {
                    Toast.makeText(requireContext(), "Dispositivo no encontrado", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_viewDeviceFragment_to_homeFragment)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error al obtener datos: ${e.message}", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_viewDeviceFragment_to_homeFragment)
            }
    }


    private fun setupListener() {

        imgBack.setOnClickListener {
            NavigationHelper.navigateToHomeTwo(findNavController())
        }
        btnEditar.setOnClickListener {
            val bundle = Bundle().apply {
                putString("deviceId", deviceId)
            }
            findNavController().navigate(R.id.action_viewDeviceFragment_to_addDeviceFragment, bundle)
        }

        btnBloquear.setOnClickListener {
            mostrarConfirmacionBloqueo()
        }

        btnEliminar.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Confirmar eliminación")
                .setMessage("¿Estás seguro de que deseas eliminar este dispositivo?")
                .setPositiveButton("Eliminar") { _, _ ->
                    eliminarDispositivo(deviceId)
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    private fun mostrarConfirmacionBloqueo() {
        AlertDialog.Builder(requireContext())
            .setTitle("Confirmar bloqueo")
            .setMessage("¿Estás seguro de que deseas bloquear este dispositivo?")
            .setPositiveButton("Sí, bloquear") { _, _ -> bloquearDispositivo() }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun bloquearDispositivo() {
        firestore.collection("dispositivos").document(deviceId)
            .update("estado", "bloqueado")
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Dispositivo bloqueado exitosamente", Toast.LENGTH_SHORT).show()
                actualizarInterfazBloqueo(true)
                findNavController().navigate(R.id.action_viewDeviceFragment_to_homeFragment)
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error al bloquear el dispositivo: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun actualizarInterfazBloqueo(estaBloqueado: Boolean) {
        view?.let { safeView ->
            val imgEstadoTelefono: ImageView? = safeView.findViewById(R.id.imgEstadoTelefono)

            imgEstadoTelefono?.apply {
                setImageResource(if (estaBloqueado) R.drawable.ic_phone_lock else R.drawable.ic_phone_unlock)
                setColorFilter(context.getColor(if (estaBloqueado) R.color.red else R.color.green))
            }

            btnBloquear?.apply {
                text = if (estaBloqueado) "Desbloquear" else "Bloquear"
                setOnClickListener {
                    if (estaBloqueado) {
                        mostrarConfirmacionDesbloqueo()
                    } else {
                        mostrarConfirmacionBloqueo()
                    }
                }
            }
        }
    }


    private fun desbloquearDispositivo() {
        firestore.collection("dispositivos").document(deviceId)
            .update("estado", "activo")
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Dispositivo desbloqueado exitosamente", Toast.LENGTH_SHORT).show()
                actualizarInterfazBloqueo(false)
                findNavController().navigate(R.id.action_viewDeviceFragment_to_homeFragment)
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error al desbloquear el dispositivo: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun mostrarConfirmacionDesbloqueo() {
        AlertDialog.Builder(requireContext())
            .setTitle("Confirmar desbloqueo")
            .setMessage("¿Estás seguro de que deseas desbloquear este dispositivo?")
            .setPositiveButton("Sí, desbloquear") { _, _ -> desbloquearDispositivo() }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun eliminarDispositivo(deviceId: String) {
        firestore.collection("dispositivos").document(deviceId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Dispositivo eliminado correctamente", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_viewDeviceFragment_to_homeFragment)
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error al eliminar: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
