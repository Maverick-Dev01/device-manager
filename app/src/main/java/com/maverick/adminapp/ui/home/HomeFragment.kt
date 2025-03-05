package com.maverick.adminapp.ui.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.maverick.adminapp.R

class HomeFragment : Fragment() {

    private lateinit var fabAddDevice: FloatingActionButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        //quita la barra de arriba, el toolbar y el boton de retroceso
        //(activity as AppCompatActivity).supportActionBar?.show() //este lo muestra
        (activity as AppCompatActivity).supportActionBar?.hide()

        //requireView() → Asegura que la vista del fragmento no sea nula, si es nula, lanzará una excepción.
        fabAddDevice = requireView().findViewById(R.id.fab_add_device)

        fabAddDevice.setOnClickListener {
            // Navegar a la pantalla de agregar dispositivo
            findNavController().navigate(R.id.action_homeFragment_to_addDeviceFragment)

        }

    }
}