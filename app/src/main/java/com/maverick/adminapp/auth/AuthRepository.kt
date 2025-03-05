package com.maverick.adminapp.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance() //Obtengo una Instancia de FirebaseAuth
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    suspend fun registerUser(username: String, fullName: String, email: String, password: String): FirebaseUser? {
        return try {
            // 1. Crear usuario en Firebase Authentication
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user

            user?.let {// Esta función se ejecuta solo si user NO es nulo
                // Si el usuario se creó con éxito, lo guardamos en Firestore
                val userMap = hashMapOf(
                    "username" to username,
                    "fullName" to fullName,
                    "email" to email
                )

                db.collection("users").document(it.uid).set(userMap).await()
            }
            // 3. Devolver el usuario creado
            user
        } catch (e: Exception) {
            // 4. Manejar errores
            e.printStackTrace()
            null
        }
    }


    suspend fun loginUser(email: String, password: String): FirebaseUser? {
        return try {
            //inicia sesión con el correo electrónico y la contraseña en Firebase Authentication
            val result = auth.signInWithEmailAndPassword(email, password).await()
            result.user // Devuelve el usuario autenticado si el inicio de sesión fue exitoso
        } catch (e: Exception) {
            e.printStackTrace()
            null //devuelve null si ocurrió un problema con el inicio de sesión
        }

    }
}