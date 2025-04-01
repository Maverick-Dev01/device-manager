package com.maverick.adminapp.ui.auth

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.maverick.adminapp.R
import com.maverick.adminapp.viewmodel.AuthViewModel

class LoginFragment : Fragment() {

    //declaramos las variables para los elementos del layout
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton : Button
    private lateinit var registerTV : TextView
    //Creamos una instancia de AuthViewModel
    private lateinit var authViewModel: AuthViewModel


    //solo infla la vista, no es recomendable acceder a los elementos aquí
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // infla el layout y lo devuelve
        // que es inflar? es convertir un archivo xml en un objeto java
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    //es el lugar correcto para acceder a los elementos del layout, asegura que los elementos existan
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //inicializamos el ViewModel
        authViewModel = ViewModelProvider(this).get(AuthViewModel::class.java)


        //buscamos los elementos del layout por su id y los guardamos en variables
            emailEditText = view.findViewById(R.id.emailEditText)
            passwordEditText = view.findViewById(R.id.passwordEditText)
            loginButton = view.findViewById(R.id.btnLogin)
            registerTV = view.findViewById(R.id.registerTV)

        //Vista de recuperacion de contraseña
        val forgotPasswordTextView = view.findViewById<TextView>(R.id.tvForgotPassword)
        forgotPasswordTextView.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_forgotPasswordFragment)
        }

        //observamos el LiveData del ViewModel
            authViewModel.user.observe(viewLifecycleOwner) { firebaseUser  ->
                if (firebaseUser  != null) {
                    //si el usuario no es nulo, mostramos un mensaje de éxito
                    Toast.makeText(requireContext(), "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()

                    //Navegar a la siguiente pantalla usando Navigation Component
                    findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
                } else {
                    //si el usuario es nulo, mostramos un mensaje de error
                    Toast.makeText(requireContext(), "Usuario y/o contraseña incorrectos", Toast.LENGTH_SHORT).show()
                }
            }

            //configuramos el listener del botón para inicio de sesión
            loginButton.setOnClickListener {
                //obtenemos el texto ingresado por el usuario
                val email = emailEditText.text.toString().trim() //trim elimina espacios en blanco
                val password = passwordEditText.text.toString().trim()

                //validamos que los campos no estén vacíos
                if (email.isNotEmpty() && password.isNotEmpty()){
                    //llamamos a la función para iniciar sesión
                    authViewModel.loginUser(email, password)
                }else {
                    //si los campos están vacíos mostramos un mensaje
                    Toast.makeText(requireContext(), "Por favor ingrese correo y contraseña", Toast.LENGTH_SHORT).show()
            }
        }

        registerTV.setOnClickListener {
            findNavController().navigate(R.id.registerFragment)
        }

    }
}
