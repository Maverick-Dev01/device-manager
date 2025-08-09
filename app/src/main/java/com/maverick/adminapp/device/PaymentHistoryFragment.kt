package com.maverick.adminapp.device

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.maverick.adminapp.R
import com.maverick.adminapp.adapters.PaymentHistoryAdapter
import com.maverick.adminapp.model.Pago
import com.maverick.adminapp.utils.NavigationHelper

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [PaymentHistoryFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PaymentHistoryFragment : Fragment() {
    private lateinit var deviceId: String
    private lateinit var paymentHistoryAdapter: PaymentHistoryAdapter
    private lateinit var paymentsList: MutableList<Pago>  // Lista de fechas de pagos
    private lateinit var recyclerView: RecyclerView
    private lateinit var imgBack : ImageView

    //Detalles de pagos
    private lateinit var txtPagoProgreso: TextView
    private lateinit var txtMontoResumen: TextView


    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_payment_history, container, false)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment PaymentHistoryFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            PaymentHistoryFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("PaymentHistoryFragment", "PaymentHistoryFragment ha sido creado.")

        deviceId = arguments?.getString("deviceId") ?: ""
        Log.d("PaymentHistoryFragment", "deviceId recibido: $deviceId")
        paymentsList = mutableListOf()  // Inicializamos la lista de pagos

        Log.d("PaymentHistoryFragment", "deviceId recibido: $deviceId")

        if (deviceId.isEmpty()) {
            Log.d("PaymentHistoryFragment", "ID de dispositivo no encontrado")
            Toast.makeText(requireContext(), "Error: ID de dispositivo no encontrado", Toast.LENGTH_LONG).show()
            findNavController().navigate(R.id.action_viewDeviceFragment_to_homeFragment)
            return
        }

        initViews(view)
        setupRecyclerView()
        loadPaymentHistory(deviceId)  // Cargar historial de pagos
    }

    private fun initViews(view: View) {
        recyclerView = view.findViewById(R.id.recyclerViewPayments)

        //Vistas de detalles de pagos encabezado
        txtPagoProgreso = view.findViewById(R.id.txtPagoProgreso)
        txtMontoResumen = view.findViewById(R.id.txtMontoResumen)

        imgBack = view.findViewById(R.id.btnBack)

        imgBack.setOnClickListener {
            val bundle = Bundle().apply {
                putString("deviceId", deviceId)
            }
            findNavController().navigate(R.id.action_paymentHistoryFragment_to_viewDeviceFragment, bundle)
        }
    }
    private fun actualizarResumenDePagos() {
        val totalPagos = paymentsList.size
        val pagosCompletados = paymentsList.count { it.estado == "Pagado" }
        val pagosPendientes = totalPagos - pagosCompletados

        val montoTotal = paymentsList.sumOf { it.montoAPagar }
        val montoPagado = paymentsList.filter { it.estado == "Pagado" }.sumOf { it.montoAPagar }
        val montoPendiente = montoTotal - montoPagado

        txtPagoProgreso.text = "Completados: $pagosCompletados / $totalPagos pagos"
        txtMontoResumen.text = "Pagado: $${String.format("%.2f", montoPagado)} | Pendiente: $${String.format("%.2f", montoPendiente)}"
    }


    private fun setupRecyclerView() {
        paymentHistoryAdapter = PaymentHistoryAdapter(paymentsList)

        paymentHistoryAdapter.onPagoCambiado = {
            actualizarResumenDePagos()
        }






        paymentHistoryAdapter.onAllPaymentsCompleted = {
            AlertDialog.Builder(requireContext())
                .setTitle("¡Pagos completados!")
                .setMessage("Todos los pagos han sido cubiertos. ¡Buen trabajo!")
                .setPositiveButton("Aceptar") { dialog, _ ->
                    dialog.dismiss()
                }
                .setCancelable(false)  // Opcional: evitar que se cierre fuera del botón
                .show()
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = paymentHistoryAdapter
    }

    private fun loadPaymentHistory(deviceId: String) {
        val firestore = FirebaseFirestore.getInstance()
        Log.d("PaymentHistoryFragment", "Cargando historial de pagos para el deviceId: $deviceId")

        Log.d("PaymentHistoryFragment", "Consultando pagos con deviceId: $deviceId")

        firestore.collection("dispositivos")
            .document(deviceId)
            .get()
            .addOnSuccessListener { document ->
                // Obtener el campo 'pagos' del documento, que ya es una lista de mapas
                val pagos = document.get("pagos") as? List<Map<String, Any>> ?: emptyList()

                // Obtener el monto a pagar del documento (fuera del campo pagos)
                val montoAPagar = document.getDouble("montoAPagar") ?: 0.0

                paymentsList.clear()  // Limpiar la lista antes de agregar nuevos pagos

                // Iterar a través de cada elemento en 'pagos' (que es un mapa con fechaPago)
                for (document in pagos) {
                    // Crear un objeto 'Pago' directamente desde el mapa
                    val fechaPago = document["fechaPago"] as? String ?: continue  // Asegúrate de que la fecha sea válida
                    val estado = document["estado"] as? String ?: "Pendiente"  // Obtener el estado del pago, por defecto "Pendiente"

                    // Crear el objeto Pago con la fecha de pago
                    val pago = Pago(fechaPago = fechaPago, montoAPagar = montoAPagar, estado = estado, deviceId = deviceId)

                    Log.d("PaymentHistoryFragment", "Pago recuperado: ${pago.fechaPago}")
                    paymentsList.add(pago)  // Agregar el pago a la lista de la clase
                }

                // Verificar que se está agregando algo a la lista
                if (paymentsList.isEmpty()) {
                    Log.d("PaymentHistoryFragment", "No hay pagos para mostrar")
                } else {
                    Log.d("PaymentHistoryFragment", "Pagos cargados: ${paymentsList.size}")
                }

                // Actualizar el adaptador con la lista de pagos
                paymentHistoryAdapter.setPayments(paymentsList)
                actualizarResumenDePagos()
            }
            .addOnFailureListener { e ->
                Log.e("PaymentHistoryFragment", "Error al cargar los pagos: ${e.message}", e)
            }
    }
}