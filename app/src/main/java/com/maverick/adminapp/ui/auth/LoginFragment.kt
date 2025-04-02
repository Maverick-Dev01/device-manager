package com.maverick.adminapp.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
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
    //Login mediante Google
    private lateinit var googleSignInButton: SignInButton
    private lateinit var mAuth: FirebaseAuth
    private val RC_SIGN_IN = 9001


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
            googleSignInButton = view.findViewById(R.id.sign_in_google_button) // Usar SignInButton de Google

        // Configuramos el botón de Google Sign-In
        googleSignInButton.setSize(SignInButton.SIZE_STANDARD) // Tamaño estándar o el que prefieras
        googleSignInButton.setOnClickListener {
            signInWithGoogle()  // Llamamos a la función para iniciar sesión con Google
        }

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

    // Función para iniciar sesión con Google
    private fun signInWithGoogle() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) // Asegúrate de usar tu Web Client ID
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(requireContext(), gso)
        val signInIntent: Intent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    // Manejar el resultado de la solicitud de inicio de sesión con Google
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Verificar que la solicitud fue para el inicio de sesión con Google
        if (requestCode == RC_SIGN_IN) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Intento de obtener la cuenta de Google
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account) // Autenticamos con Firebase
            } catch (e: ApiException) {
                Toast.makeText(requireContext(), "Error al iniciar sesión con Google: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Función para autenticar con Firebase usando la cuenta de Google
    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount?) {
        val credential = GoogleAuthProvider.getCredential(account?.idToken, null)
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    val user = FirebaseAuth.getInstance().currentUser
                    Toast.makeText(requireContext(), "Autenticación exitosa con Google", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_loginFragment_to_homeFragment) // Navegar al Home
                } else {
                    Toast.makeText(requireContext(), "Error de autenticación con Google", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
