package com.maverick.adminapp.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.firebase.firestore.FirebaseFirestore
import com.maverick.adminapp.R
import com.maverick.adminapp.model.Pago

class PaymentHistoryAdapter(private var payments: List<Pago>) : RecyclerView.Adapter<PaymentHistoryAdapter.PaymentViewHolder>() {

    var onAllPaymentsCompleted: (() -> Unit)? = null  // Función de callback

    private fun allPaymentsCompleted(): Boolean {
        return payments.all { it.estado == "Pagado" }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_payment, parent, false)
        return PaymentViewHolder(view)
    }

    override fun onBindViewHolder(holder: PaymentViewHolder, position: Int) {
        val payment = payments[position]  // Obtener el objeto Pago en lugar de un String

        Log.d(
            "PaymentHistoryAdapter",
            "Mostrando pago: ${payment.fechaPago}"
        )  // Mostrar la fecha de pago

        holder.bind(payment) {
            if (allPaymentsCompleted()) {
                onAllPaymentsCompleted?.invoke()
            }
        }
    }

    override fun getItemCount(): Int {
        Log.d("PaymentHistoryAdapter", "Cantidad de pagos: ${payments.size}")
        return payments.size
    }

    fun setPayments(payments: List<Pago>) {
        Log.d(
            "PaymentHistoryAdapter",
            "Actualizando pagos en el adaptador: ${payments.size} pagos."
        )
        this.payments = payments
        notifyDataSetChanged()  // Notificamos al adaptador que los datos han cambiado
    }

    class PaymentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtFecha: TextView = itemView.findViewById(R.id.txtFecha)
        private val txtMonto: TextView = itemView.findViewById(R.id.txtMonto)
        private val txtEstado: TextView = itemView.findViewById(R.id.txtEstado)
        private val switchEstado: Switch = itemView.findViewById(R.id.switchEstadoPago)


        fun bind(payment: Pago, onEstadoCambiado: () -> Unit) {
            Log.d("PaymentHistoryAdapter", "Vinculando fecha de pago: ${payment.fechaPago}")
            txtFecha.text = "Fecha de pago: ${payment.fechaPago}"  // Mostrar la fecha del pago
            txtMonto.text = "Monto a pagar: $${String.format("%.2f", payment.montoAPagar)}"
            txtEstado.text = "Estado del pago: ${payment.estado}"

            // Establecer el estado del switch
            switchEstado.setOnCheckedChangeListener(null) // ← evitar que se dispare al hacer bind
            switchEstado.isChecked = payment.estado == "Pagado"

            // Al cambiar el switch, actualizar el estado
            switchEstado.setOnCheckedChangeListener { _, isChecked ->
                // Cambiar el estado en la UI
                val newEstado = if (isChecked) "Pagado" else "Pendiente"

                // Actualizar el estado del pago en la base de datos
                payment.estado = newEstado
                updatePaymentStatus(payment)  // Llamar a la función para actualizar Firestore
                onEstadoCambiado() // ← avisar al adapter para validar todos
            }
        }

        // Función para actualizar el estado en Firestore
        private fun updatePaymentStatus(payment: Pago) {
            val firestore = FirebaseFirestore.getInstance()
            val deviceId = payment.deviceId  // Usar el deviceId que has pasado en el objeto Pago

            // Buscar el documento en la colección 'dispositivos' usando el deviceId
            val dispositivoRef = firestore.collection("dispositivos").document(deviceId)

            dispositivoRef.get().addOnSuccessListener { document ->
                if (document.exists()) {
                    val pagos = mutableListOf<Map<String, Any>>()

                    // Obtener los pagos del dispositivo
                    val existingPagos = document.get("pagos") as? List<Map<String, Any>> ?: emptyList()

                    // Recorrer los pagos y actualizar el estado del pago correspondiente
                    for (pago in existingPagos) {
                        if (pago["fechaPago"] == payment.fechaPago) {
                            val updatedPago = pago.toMutableMap()
                            updatedPago["estado"] = payment.estado  // Cambiar el estado

                            // Agregar el pago actualizado a la lista de pagos
                            pagos.add(updatedPago)
                        } else {
                            pagos.add(pago)  // Dejar el pago tal cual si no coincide
                        }
                    }

                    // Actualizar la lista de pagos con el estado modificado en Firestore
                    dispositivoRef.update("pagos", pagos)
                        .addOnSuccessListener {
                            Log.d("PaymentHistoryAdapter", "Estado del pago actualizado correctamente")
                        }
                        .addOnFailureListener { e ->
                            Log.e("PaymentHistoryAdapter", "Error al actualizar estado del pago: ${e.message}", e)
                        }
                }
            }
        }
    }
}
