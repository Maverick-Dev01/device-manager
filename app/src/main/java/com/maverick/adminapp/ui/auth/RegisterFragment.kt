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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.UserProfileChangeRequest
import com.maverick.adminapp.R
import com.maverick.adminapp.viewmodel.AuthViewModel


class RegisterFragment : Fragment() {

    private lateinit var authViewModel: AuthViewModel
    private lateinit var usernameET : EditText
    private lateinit var fullNameET : EditText
    private lateinit var emailET : EditText
    private lateinit var passwordET : EditText
    private lateinit var confirmPassword : EditText
    private lateinit var btnRegister : Button
    private lateinit var signInTV : TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        authViewModel = ViewModelProvider(this).get(AuthViewModel::class.java)


        usernameET = view.findViewById(R.id.usernameET)
        fullNameET = view.findViewById(R.id.fullNameET)
        emailET = view.findViewById(R.id.emailET)
        passwordET = view.findViewById(R.id.passwordET)
        confirmPassword = view.findViewById(R.id.confirmPassword)
        btnRegister = view.findViewById(R.id.btnRegister)
        signInTV = view.findViewById(R.id.signInTV)

        btnRegister.setOnClickListener{
            val username = usernameET.text.toString().trim()
            val fullName = fullNameET.text.toString().trim()
            val email = emailET.text.toString().trim()
            val password = passwordET.text.toString().trim()
            val confirmPass = confirmPassword.text.toString().trim()

            if (username.isEmpty() || fullName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPass.isEmpty()){
                Toast.makeText(requireContext(), "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validar contraseña segura
            if (password.length < 8) {
                Toast.makeText(requireContext(), "La contraseña debe tener al menos 8 caracteres", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validar coincidencia de contraseñas
            if (password != confirmPass) {
                Toast.makeText(requireContext(), "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validar formato de correo electrónico
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(requireContext(), "El correo electrónico no es válido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Llamar a la función para registrar usuario
            authViewModel.registerUser(username, fullName, email, password) { exception ->
                if (exception == null) {
                    if (exception == null) {
                        // ACTUALIZAR EL NOMBRE DEL USUARIO EN FIREBASE
                        val user = FirebaseAuth.getInstance().currentUser
                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setDisplayName(username)  // o puedes usar fullName si prefieres
                            .build()
                        user?.updateProfile(profileUpdates)
                            ?.addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Toast.makeText(requireContext(), "Registro exitoso", Toast.LENGTH_SHORT).show()
                                    findNavController().navigate(R.id.action_registerFragment_to_homeFragment)
                                } else {
                                    Toast.makeText(requireContext(), "Registro exitoso pero no se guardó el nombre", Toast.LENGTH_SHORT).show()
                                    findNavController().navigate(R.id.action_registerFragment_to_homeFragment)
                                }
                            }
                    }

                } else {
                    val errorMessage = when (exception) {
                        is FirebaseAuthWeakPasswordException -> "La contraseña es demasiado débil."
                        is FirebaseAuthInvalidCredentialsException -> "El correo electrónico no es válido."
                        is FirebaseAuthUserCollisionException -> "El correo electrónico ya está en uso."
                        else -> "Error en el registro: ${exception.message}"
                    }
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
                }
            }
        }

        signInTV.setOnClickListener{
            findNavController().navigate(R.id.loginFragment)
        }

    }

}

