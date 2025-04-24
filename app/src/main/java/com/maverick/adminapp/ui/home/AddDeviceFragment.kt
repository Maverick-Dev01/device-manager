package com.maverick.adminapp.ui.home

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.maverick.adminapp.R
import com.maverick.adminapp.utils.DeviceFormHelper
import com.maverick.adminapp.utils.NavigationHelper
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class AddDeviceFragment : Fragment() {

    private lateinit var imeiInput: TextInputEditText
    private lateinit var clientInput: TextInputEditText
    private lateinit var cityInput: TextInputEditText
    private lateinit var phoneInput: TextInputEditText
    private lateinit var brandAC: AutoCompleteTextView
    private lateinit var modelAC: AutoCompleteTextView
    private lateinit var frequencyAC: AutoCompleteTextView
    private lateinit var periodAC: AutoCompleteTextView
    private lateinit var startDate: TextInputEditText
    private lateinit var endDate: TextInputEditText
    private lateinit var priceInput: TextInputEditText
    private lateinit var amountToPay: TextInputEditText

    private lateinit var btnCancel: Button
    private lateinit var btnRegister: Button
    private lateinit var btnEditar: Button


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_device, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.hide()

        // 🔹 Inicializar vistas
        initViews(view)

        // 🔹 Configurar dropdowns y fecha
        setupDropdowns()
        setupDatePicker()

        // 🔹 Configurar eventos
        setupListeners()

        val deviceId = arguments?.getString("deviceId")

        if (!deviceId.isNullOrEmpty()) {
            obtenerDatosDispositivo(deviceId)
            btnRegister.text = "Actualizar" // Cambiar texto del botón
            btnRegister.setOnClickListener { actualizarDispositivo(deviceId) }
        } else {
            btnRegister.text = "Registrar" // Si es un nuevo registro
            btnRegister.setOnClickListener { registerDevice() }
        }

    }

    private fun initViews(view: View) {
        imeiInput = view.findViewById(R.id.input_device_imei)
        clientInput = view.findViewById(R.id.input_client_name)
        cityInput = view.findViewById(R.id.input_client_city)
        phoneInput = view.findViewById(R.id.input_client_phone)
        brandAC = view.findViewById(R.id.input_device_brand)
        modelAC = view.findViewById(R.id.input_device_model)
        priceInput = view.findViewById(R.id.input_purchase_price)
        frequencyAC = view.findViewById(R.id.input_device_frequency)
        periodAC = view.findViewById(R.id.input_device_period)
        startDate = view.findViewById(R.id.input_start_date)
        endDate = view.findViewById(R.id.input_end_date)
        amountToPay = view.findViewById(R.id.input_amount_to_pay)

        btnCancel = view.findViewById(R.id.btn_cancel)
        btnRegister = view.findViewById(R.id.btn_register_device)

        btnEditar = view.findViewById(R.id.btnEditar)
    }

    private fun setupDropdowns() {
        DeviceFormHelper.setupBrandDropdown(requireContext(), brandAC)
        DeviceFormHelper.setupModelDropdown(requireContext(), brandAC, modelAC)
        DeviceFormHelper.setupFrequencyDropdown(requireContext(), frequencyAC)
        DeviceFormHelper.setupPeriodDropdown(requireContext(), periodAC)
    }

    private fun setupDatePicker() {
        DeviceFormHelper.setupDatePicker(
            requireContext(),
            startDate,
            endDate,
            frequencyAC,
            periodAC
        )
    }

    private fun setupListeners() {
        brandAC.setOnClickListener { brandAC.showDropDown() }
        modelAC.setOnClickListener { modelAC.showDropDown() }
        frequencyAC.setOnClickListener { frequencyAC.showDropDown() }
        periodAC.setOnClickListener { periodAC.showDropDown() }


        // 🔹 Mostrar el dropdown al enfocar el campo
        brandAC.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                brandAC.showDropDown()
            }
        }

        modelAC.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                modelAC.showDropDown()
            }
        }

        frequencyAC.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                frequencyAC.showDropDown()
            }
        }

        periodAC.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                periodAC.showDropDown()
            }
        }

        btnCancel.setOnClickListener {
            NavigationHelper.navigateToHome(findNavController())
        }

        btnRegister.setOnClickListener {
            registerDevice()
        }

        priceInput.addTextChangedListener(paymentWatcher)
        startDate.addTextChangedListener(paymentWatcher)
        endDate.addTextChangedListener(paymentWatcher)
        frequencyAC.setOnItemClickListener { _, _, _, _ -> calculatePayment() }
    }

    private fun calculatePayment() {
        val price = priceInput.text.toString().toDoubleOrNull()
        val startDateText = startDate.text.toString()
        val endDateText = endDate.text.toString()
        val frequencyText = frequencyAC.text.toString()

        DeviceFormHelper.calculatePaymentAmount(
            price,
            startDateText,
            endDateText,
            frequencyText,
            amountToPay
        )
        Log.d("PaymentCalc", "Monto a pagar calculado: ${amountToPay.text.toString()}")

    }

    private val paymentWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            calculatePayment()
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    }

    private fun registerDevice() {
        val db = FirebaseFirestore.getInstance()
        val currentUser = FirebaseAuth.getInstance().currentUser
        val uid = currentUser?.uid ?: return
        val imei = imeiInput.text?.toString()?.trim() ?: ""
        val cliente = clientInput.text?.toString()?.trim() ?: ""
        val ciudad = cityInput.text?.toString()?.trim() ?: ""
        val telefono = phoneInput.text?.toString()?.trim() ?: ""
        val marca = brandAC.text?.toString()?.trim() ?: ""
        val modelo = modelAC.text?.toString()?.trim() ?: ""
        val frecuencia = frequencyAC.text?.toString()?.trim() ?: ""
        val periodo = periodAC.text?.toString()?.trim() ?: ""
        val fechaInicio = startDate.text?.toString()?.trim() ?: ""
        val fechaFin = endDate.text?.toString()?.trim() ?: ""
        val precio = priceInput.text?.toString()?.toDoubleOrNull()
        val montoAPagar = amountToPay.text?.toString()?.replace(",", "")?.toDoubleOrNull()

        if (!esIMEIValido(imei)) {
            Toast.makeText(requireContext(), "El IMEI debe tener exactamente 15 dígitos numéricos", Toast.LENGTH_SHORT).show()
            return
        }
        if (!esTelefonoValido(telefono)) {
            phoneInput.error = "El número debe tener exactamente 10 dígitos"
            phoneInput.requestFocus()
            return
        }
        if (
            imei.isEmpty() || cliente.isEmpty() || ciudad.isEmpty() || telefono.isEmpty() ||
            marca.isEmpty() || modelo.isEmpty() || frecuencia.isEmpty() || periodo.isEmpty() ||
            fechaInicio.isEmpty() || fechaFin.isEmpty() || precio == null || montoAPagar == null
        ) {
            Toast.makeText(requireContext(), "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        calculatePayment()
        Log.d("PaymentHistoryFragment", "Frecuencia: $frecuencia, Periodo: $periodo, Fecha de Inicio: $fechaInicio")
        // Calculamos el historial de pagos (solo las fechas)
        val pagos = calculatePaymentDates(frecuencia, periodo, fechaInicio)
        Log.d("PaymentHistoryFragment", "Pagos calculados: $pagos")

        val dispositivo = hashMapOf(
            "uid" to uid,
            "imei" to imei,
            "marca" to marca,
            "modelo" to modelo,
            "cliente" to cliente,
            "ciudad" to ciudad,
            "telefono" to telefono,
            "precio" to precio,
            "frecuenciaPago" to frecuencia,
            "periodoPago" to periodo,
            "fechaInicio" to fechaInicio,
            "fechaFin" to fechaFin,
            "montoAPagar" to (montoAPagar ?: 0.0),
            "pagos" to pagos // Aquí agregamos el historial de pagos con las fechas
        )

        Log.d(
            "FirestoreDebug",
            "Registrando dispositivo con montoAPagar: ${amountToPay.text.toString()}"
        )

        // Antes de enviar el registro de dispositivo
        Log.d("PaymentHistoryFragment", "Registrando dispositivo con los siguientes datos: deviceId: pagos: $pagos")

        // Registrar dispositivo en Firestore
        db.collection("dispositivos")
            .add(dispositivo)
            .addOnSuccessListener { documentReference ->
                // Log detallado de éxito
                Log.d("PaymentHistoryFragment", "Dispositivo registrado con ID: ${documentReference.id}")
                Toast.makeText(requireContext(), "Registro exitoso", Toast.LENGTH_SHORT).show()
                // Redirigir después del registro
                findNavController().navigate(R.id.action_addDeviceFragment_to_homeFragment)
            }
            .addOnFailureListener { e ->
                Log.e("PaymentHistoryFragment", "Error al registrar dispositivo: $e")
                Toast.makeText(requireContext(), "Error al registrar", Toast.LENGTH_SHORT).show()
            }

    }

    // Función que calcula las fechas de los pagos en base a la frecuencia y el periodo
    private fun calculatePaymentDates(frecuencia: String, periodo: String, fechaInicio: String): List<Map<String, Any>> {
        val paymentDates = mutableListOf<Map<String, Any>>()

        // Convertir fechaInicio a Date
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val start = dateFormat.parse(fechaInicio)
        val calendar = Calendar.getInstance()
        calendar.time = start

        // Definir cuántos pagos generar según la frecuencia y el periodo
        val numPayments = when (frecuencia) {
            "Semanal" -> 4 * when (periodo) {
                "Anual" -> 12
                "Semestral" -> 2
                else -> 1
            }
            "Quincenal" -> when (periodo) {
                "Anual" -> 24  // 2 pagos por mes, 12 meses
                "Semestral" -> 12  // 2 pagos por mes, 6 meses
                else -> 1
            }
            "Mensual" -> when (periodo) {
                "Anual" -> 12
                "Semestral" -> 6
                "Trimestral" -> 3
                else -> 1
            }
            else -> 0
        }

        // Verificar que numPayments sea mayor que 0
        Log.d("PaymentHistoryFragment", "NumPayments calculados: $numPayments")

        // Generar las fechas de los pagos
        for (i in 0 until numPayments) {
            // Calcular la fecha de pago
            calendar.add(Calendar.MONTH, 1) // Incrementamos un mes

            val paymentDate = dateFormat.format(calendar.time)
            val estado = "Pendiente"

            // Crear el pago con la fecha y agregarlo al array de pagos
            val payment = mapOf(
                "fechaPago" to paymentDate,
                "estado" to estado
            )

            paymentDates.add(payment)

            // Agregar log para verificar el contenido de los pagos
            Log.d("PaymentHistoryFragment", "Pago calculado: $paymentDate")
        }

        return paymentDates
    }



    private fun obtenerDatosDispositivo(deviceId: String) {
        val db = FirebaseFirestore.getInstance()

        db.collection("dispositivos").document(deviceId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // Convertir los datos obtenidos en variables
                    val imei = document.getString("imei") ?: ""
                    val cliente = document.getString("cliente") ?: ""
                    val ciudad = document.getString("ciudad") ?: ""
                    val telefono = document.getString("telefono") ?: ""
                    val marca = document.getString("marca") ?: ""
                    val modelo = document.getString("modelo") ?: ""
                    val precio = document.getDouble("precio") ?: 0.0
                    val frecuenciaPago = document.getString("frecuenciaPago") ?: ""
                    val periodoPago = document.getString("periodoPago") ?: ""
                    val fechaInicio = document.getString("fechaInicio") ?: ""
                    val fechaFin = document.getString("fechaFin") ?: ""

                    // Llenar los campos del formulario
                    imeiInput.setText(imei)
                    clientInput.setText(cliente)
                    cityInput.setText(ciudad)
                    phoneInput.setText(telefono)
                    brandAC.setText(marca, false)
                    modelAC.setText(modelo, false)
                    priceInput.setText(precio.toString())
                    frequencyAC.setText(frecuenciaPago, false)
                    periodAC.setText(periodoPago, false)
                    startDate.setText(fechaInicio)
                    endDate.setText(fechaFin)
                } else {
                    Toast.makeText(
                        requireContext(),
                        "No se encontró el dispositivo",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    requireContext(),
                    "Error al obtener datos: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }


    private fun actualizarDispositivo(deviceId: String) {
        val db = FirebaseFirestore.getInstance()
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val imei = imeiInput.text?.toString()?.trim() ?: ""
        val cliente = clientInput.text?.toString()?.trim() ?: ""
        val ciudad = cityInput.text?.toString()?.trim() ?: ""
        val telefono = phoneInput.text?.toString()?.trim() ?: ""
        val marca = brandAC.text?.toString()?.trim() ?: ""
        val modelo = modelAC.text?.toString()?.trim() ?: ""
        val frecuencia = frequencyAC.text?.toString()?.trim() ?: ""
        val periodo = periodAC.text?.toString()?.trim() ?: ""
        val fechaInicio = startDate.text?.toString()?.trim() ?: ""
        val fechaFin = endDate.text?.toString()?.trim() ?: ""
        val precio = priceInput.text?.toString()?.toDoubleOrNull()
        val montoAPagar = amountToPay.text?.toString()?.replace(",", "")?.toDoubleOrNull()

// 🔒 Validación de campos
        if (
            imei.isEmpty() || cliente.isEmpty() || ciudad.isEmpty() || telefono.isEmpty() ||
            marca.isEmpty() || modelo.isEmpty() || frecuencia.isEmpty() || periodo.isEmpty() ||
            fechaInicio.isEmpty() || fechaFin.isEmpty() || precio == null || montoAPagar == null
        ) {
            Toast.makeText(requireContext(), "Por favor completa todos los campos correctamente", Toast.LENGTH_SHORT).show()
            return
        }

// 🔐 Validar IMEI
        if (!esIMEIValido(imei)) {
            imeiInput.error = "El IMEI debe tener exactamente 15 dígitos numéricos"
            imeiInput.requestFocus()
            return
        }
        if (!esTelefonoValido(telefono)) {
            phoneInput.error = "El número debe tener exactamente 10 dígitos"
            phoneInput.requestFocus()
            return
        }

        // Paso 1: Verificar si el dispositivo pertenece al usuario
        db.collection("dispositivos").document(deviceId)
            .get()
            .addOnSuccessListener { document ->
                val uidDelDocumento = document.getString("uid")

                if (uidDelDocumento != currentUserId) {
                    Toast.makeText(requireContext(), "No tienes permiso para editar este dispositivo", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                // Paso 2: Si es válido, actualizar
                calculatePayment()

                val datosActualizados = mapOf(
                    "imei" to imei,
                    "cliente" to cliente,
                    "ciudad" to ciudad,
                    "telefono" to telefono,
                    "marca" to marca,
                    "modelo" to modelo,
                    "precio" to precio,
                    "frecuenciaPago" to frecuencia,
                    "periodoPago" to periodo,
                    "fechaInicio" to fechaInicio,
                    "fechaFin" to fechaFin,
                    "montoAPagar" to montoAPagar
                    // No tocamos el campo "uid"
                )

                db.collection("dispositivos").document(deviceId)
                    .update(datosActualizados)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Dispositivo actualizado correctamente", Toast.LENGTH_SHORT).show()
                        findNavController().navigateUp()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(requireContext(), "Error al actualizar: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error al verificar el dispositivo: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    fun esTelefonoValido(telefono: String): Boolean {
        return telefono.length == 10 && telefono.all { it.isDigit() }
    }

    // validar que el campo no esté vacío y tenga 15 dígitos (que es el formato estándar del IMEI)
    fun esIMEIValido(imei: String): Boolean {
        return imei.length == 15 && imei.all { it.isDigit() }
    }


}
