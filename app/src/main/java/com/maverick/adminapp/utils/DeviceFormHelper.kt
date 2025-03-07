package com.maverick.adminapp.utils

import android.app.DatePickerDialog
import android.content.Context
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.maverick.adminapp.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object DeviceFormHelper {

    //lista de marcas de celulares
    fun setupBrandDropdown(context: Context, dropdown: AutoCompleteTextView) {
        val brands = listOf("Samsung", "Motorola", "Xiaomi", "OnePlus")
        val adapter = ArrayAdapter(
            context,
            android.R.layout.simple_dropdown_item_1line,
            brands
        )

        dropdown.setAdapter(adapter)

    }

    //lista de modelos de celulares
    fun setupModelDropdown(
        context: Context,
        brandDropdown: AutoCompleteTextView,
        modelDropdown: AutoCompleteTextView
    ) {
        // Diccionario de modelos según la marca
        val modelsByBrand = mapOf(
            "Samsung" to listOf("Galaxy A14", "Galaxy A24", "Galaxy A34", "Galaxy M13", "Galaxy M23", "Galaxy M33", "Galaxy S20 FE", "Galaxy S21 FE", "Galaxy A53", "Galaxy A72"),
            "Motorola" to listOf("Moto G23", "Moto G53", "Moto G72", "Moto Edge 20", "Moto Edge 30", "Moto G82", "Moto G100", "Moto G Stylus", "Moto G Power", "Moto G Play"),
            "OnePlus" to listOf("OnePlus Nord CE 3 Lite", "OnePlus Nord N200", "OnePlus Nord N20", "OnePlus Nord 2T", "OnePlus 8T", "OnePlus 9R", "OnePlus 10R", "OnePlus Nord CE 2", "OnePlus 7T", "OnePlus 6T"),
            "Xiaomi" to listOf("Redmi Note 12", "Redmi Note 11S", "Redmi Note 10 Pro", "Redmi Note 9T", "POCO X4 Pro", "POCO M4 Pro", "Xiaomi 11 Lite", "Xiaomi 12X", "Xiaomi 12 Lite", "Xiaomi Mi 10T")
        )

        // Evento cuando se selecciona una marca
        brandDropdown.setOnItemClickListener { _, _, position, _ ->
            val selectedBrand = brandDropdown.adapter.getItem(position) as String
            val models = modelsByBrand[selectedBrand] ?: emptyList()

            // Si la marca tiene modelos, se actualiza el dropdown de modelos
            modelDropdown.setAdapter(ArrayAdapter(context, android.R.layout.simple_dropdown_item_1line, models))
            modelDropdown.setText("") // Limpiar selección anterior
        }

        // Permitir que el usuario escriba su propio modelo
        modelDropdown.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                modelDropdown.showDropDown()
            }
        }
    }


    fun setupFrequencyDropdown(context: Context, dropdown: AutoCompleteTextView) {
        val frequencies = listOf("Semanal", "Quincenal", "Mensual", )
        val adapter = ArrayAdapter(context, android.R.layout.simple_dropdown_item_1line, frequencies)
        dropdown.setAdapter(adapter)
    }

    fun setupPeriodDropdown(context: Context, dropdown: AutoCompleteTextView) {
        val periods = listOf("Bimestral", "Trimestral", "Semestral", "Anual")
        val adapter = ArrayAdapter(context, android.R.layout.simple_dropdown_item_1line, periods)
        dropdown.setAdapter(adapter)

    }
    //seleccionar fecha
    fun setupDatePicker(
        context: Context,
        startDate: TextInputEditText,
        endDate: TextInputEditText,
        frequencyDropdown: AutoCompleteTextView,
        periodDropdown: AutoCompleteTextView
    ) {
        startDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePicker = DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    val selectedDate = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year)
                    startDate.setText(selectedDate)

                    // 🔥 Verificar si ya hay frecuencia y período seleccionados para calcular Fecha Fin
                    updateEndDate(startDate, endDate, frequencyDropdown, periodDropdown)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePicker.show()
        }

    // Cuando cambia el Período de Pago, recalcula la fecha de fin automáticamente
    periodDropdown.setOnItemClickListener { _, _, _, _ ->
        updateEndDate(startDate, endDate, frequencyDropdown, periodDropdown)
    }

    // Cuando cambia la Frecuencia de Pago, recalcula la fecha de fin automáticamente
    frequencyDropdown.setOnItemClickListener { _, _, _, _ ->
        updateEndDate(startDate, endDate, frequencyDropdown, periodDropdown)
    }
}

    // 🔥 Nueva función para actualizar la Fecha de Fin sin depender de hacer clic en Fecha de Inicio
    private fun updateEndDate(
        startDate: TextInputEditText,
        endDate: TextInputEditText,
        frequencyDropdown: AutoCompleteTextView,
        periodDropdown: AutoCompleteTextView
    ) {
        val selectedDate = startDate.text.toString()
        val frequency = frequencyDropdown.text.toString()
        val period = periodDropdown.text.toString()

        if (selectedDate.isNotEmpty() && frequency.isNotEmpty() && period.isNotEmpty()) {
            val calculatedEndDate = calculateEndDate(selectedDate, frequency, period)
            endDate.setText(calculatedEndDate)
        }
    }



    fun calculateEndDate(startDate: String, frequency: String, period: String): String {
        // Definir el formato de fecha que estamos usando (dd/MM/yyyy)
        val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        try {
            // Convertir la cadena de la fecha de inicio a un objeto Date
            val date = format.parse(startDate) ?: return "Error"
            val calendar = Calendar.getInstance()
            calendar.time = date

            // Definir cuántos meses sumar según el período de pago
            val monthsToAdd = when (period) {
                "Bimestral" -> 2
                "Trimestral" -> 3
                "Semestral" -> 6
                "Anual" -> 12
                else -> 0 // En caso de error
            }

            // Sumar los meses al calendario
            calendar.add(Calendar.MONTH, monthsToAdd)

            // Devolver la fecha formateada
            return format.format(calendar.time)
        } catch (e: Exception) {
            e.printStackTrace()
            return "Error"
        }
    }

    fun calculatePaymentAmount(
        price: Double?,
        startDate: String,
        endDate: String,
        frequency: String,
        amountTextView: TextInputEditText
    ) {
        if (price == null || price <= 0 || startDate.isEmpty() || endDate.isEmpty() || frequency.isEmpty()) {
            amountTextView.setText("") // No mostramos nada hasta que todo esté correcto
            return
        }

        val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        try {
            val start = format.parse(startDate)
            val end = format.parse(endDate)

            if (start != null && end != null && end.after(start)) {
                // Calcular la cantidad total de días entre la fecha inicio y fin
                val totalDays = ((end.time - start.time) / (24 * 60 * 60 * 1000)).toInt()

                // Definir la cantidad de pagos según la frecuencia
                val numberOfPayments = when (frequency) {
                    "Semanal" -> totalDays / 7  // Semanas completas
                    "Quincenal" -> totalDays / 15 // Quincenas completas
                    "Mensual" -> totalDays / 30  // Meses completos
                    else -> 0
                }

                if (numberOfPayments > 0) {
                    // Calcular el pago por período
                    val basePayment = price / numberOfPayments

                    // Ajustar el último pago si hay diferencia
                    val totalCalculated = basePayment * numberOfPayments
                    val difference = price - totalCalculated

                    val finalPayment = if (difference > 0) basePayment + difference else basePayment

                    // Mostrar el monto ajustado
                    amountTextView.setText("$%.2f".format(finalPayment))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            amountTextView.setText("")
        }
    }




}
