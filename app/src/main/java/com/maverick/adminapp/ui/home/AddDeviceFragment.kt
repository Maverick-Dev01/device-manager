package com.maverick.adminapp.ui.home

import android.app.DatePickerDialog
import android.content.Context
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
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore
import com.maverick.adminapp.R
import com.maverick.adminapp.utils.DeviceFormHelper
import com.maverick.adminapp.utils.NavigationHelper
import java.util.Calendar

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
    }

    private fun setupDropdowns() {
        DeviceFormHelper.setupBrandDropdown(requireContext(), brandAC)
        DeviceFormHelper.setupModelDropdown(requireContext(), brandAC, modelAC)
        DeviceFormHelper.setupFrequencyDropdown(requireContext(), frequencyAC)
        DeviceFormHelper.setupPeriodDropdown(requireContext(), periodAC)
    }

    private fun setupDatePicker() {
        DeviceFormHelper.setupDatePicker(requireContext(), startDate, endDate, frequencyAC, periodAC)
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
        calculatePayment()

        val dispositivo = hashMapOf(
            "imei" to imeiInput.text.toString(),
            "marca" to brandAC.text.toString(),
            "modelo" to modelAC.text.toString(),
            "cliente" to clientInput.text.toString(),
            "ciudad" to cityInput.text.toString(),
            "telefono" to phoneInput.text.toString(),
            "precio" to priceInput.text.toString().toDoubleOrNull(),
            "frecuenciaPago" to frequencyAC.text.toString(),
            "periodoPago" to periodAC.text.toString(),
            "fechaInicio" to startDate.text.toString(),
            "fechaFin" to endDate.text.toString(),
            "montoAPagar" to amountToPay.text.toString()
        )

        db.collection("dispositivos")
            .add(dispositivo)
            .addOnSuccessListener { documentReference ->
                Log.d("Firebase", "Dispositivo registrado con ID: ${documentReference.id}")
                Toast.makeText(requireContext(), "Registro exitoso", Toast.LENGTH_SHORT).show()

                // 🔥 Redirigir al Home
                findNavController().navigate(R.id.action_addDeviceFragment_to_homeFragment)
            }
            .addOnFailureListener { e ->
                Log.e("Firebase", "Error al registrar", e)
                Toast.makeText(requireContext(), "Error al registrar", Toast.LENGTH_SHORT).show()
            }
    }
}
