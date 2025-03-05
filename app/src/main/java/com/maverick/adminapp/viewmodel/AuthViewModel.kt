package com.maverick.adminapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.maverick.adminapp.auth.AuthRepository
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val authRepository = AuthRepository()
    private val _user = MutableLiveData<FirebaseUser?>()
    val user: LiveData<FirebaseUser?> = _user

    //se cambió Boolean por Exception
    fun registerUser(username: String, fullName: String, email: String, password: String, onResult: (Exception?) -> Unit) {
        viewModelScope.launch {
            try {
                val user = authRepository.registerUser(username, fullName, email, password)
                _user.postValue(user)
                onResult(null) // Registro exitoso
            } catch (e: Exception) {
                onResult(e) // Devuelve la excepción
            }
        }
    }

    // Función para iniciar sesión
    fun loginUser(email: String, password: String) {
        viewModelScope.launch {
            val result = authRepository.loginUser(email, password)
            _user.postValue(result) // Actualiza la UI con el resultado del login
        }
    }
}
