package com.maverick.adminapp.utils

import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.Spinner
import com.google.android.material.textfield.TextInputLayout

object FormValidator {

    // Validar IMEI
    fun validateIMEI(inputLayout: TextInputLayout): Boolean {
        val imei = inputLayout.editText?.text.toString().trim()
        return if (imei.length != 16) {
            inputLayout.error = "El IMEI debe tener 16 dígitos"
            false
        } else {
            inputLayout.error = null
            true
        }
    }

    // Validar que un Spinner tenga una selección válida
    fun validateSpinner(dropdown: AutoCompleteTextView): Boolean {
        return if (dropdown.validator == null || dropdown.validator.toString().isEmpty()) {
            false
        } else {
            true
        }
    }

    // Validar un campo obligatorio (como nombre, precio, etc.)
    fun validateRequiredField(inputLayout: TextInputLayout): Boolean {
        val text = inputLayout.editText?.text.toString().trim()
        return if (text.isEmpty()) {
            inputLayout.error = "Este campo es obligatorio"
            false
        } else {
            inputLayout.error = null
            true
        }
    }

    // Validar precio del teléfono (debe ser un número válido)
    fun validatePrice(inputLayout: TextInputLayout): Boolean {
        val text = inputLayout.editText?.text.toString().trim()
        return if (text.isEmpty()) {
            inputLayout.error = "El precio es obligatorio"
            false
        } else if (text.toDoubleOrNull() == null) {
            inputLayout.error = "Ingrese un precio válido"
            false
        } else {
            inputLayout.error = null
            true
        }
    }
}
