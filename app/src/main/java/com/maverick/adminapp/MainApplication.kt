package com.maverick.adminapp

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp

class MainApplication : Application() {
    override fun onCreate() { //SOBREESCRIBIMOS EL METODO ONCREATE PARA AGREGAR CODIGO PERSONALIZADO
        super.onCreate() // Ejecuta el código original de onCreate() de Application
        FirebaseApp.initializeApp(this) // Inicializa Firebase en toda la app
        Log.d("FirebaseInit", "Firebase inicializado correctamente")

    }
}
