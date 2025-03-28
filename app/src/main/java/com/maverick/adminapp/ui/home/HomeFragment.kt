package com.maverick.adminapp.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.maverick.adminapp.R
import com.maverick.adminapp.adapters.DeviceAdapter
import com.maverick.adminapp.model.Device

class HomeFragment : Fragment() {

    private lateinit var fabAddDevice: FloatingActionButton
    private lateinit var recyclerDevices: RecyclerView
    private lateinit var deviceAdapter: DeviceAdapter
    private val firestore = FirebaseFirestore.getInstance()
    private val deviceList = mutableListOf<Device>() // Ahora sí es mutable

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Ocultar la barra superior
        (activity as AppCompatActivity).supportActionBar?.hide()

        // Inicializar vistas
        val drawerLayout = view.findViewById<DrawerLayout>(R.id.drawer_layout)
        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar)
        val navView = view.findViewById<NavigationView>(R.id.navigation_view)
        val header = navView.getHeaderView(0)
        val tvUserName = header.findViewById<TextView>(R.id.tvUserName)
        val tvUserEmail = header.findViewById<TextView>(R.id.tvUserEmail)

        val btnClose = header.findViewById<ImageButton>(R.id.btnCloseDrawer)

        btnClose.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        toolbar.title = "Dispositivos Registrados"

        val user = FirebaseAuth.getInstance().currentUser

        tvUserName.text = user?.displayName ?: "Nombre no disponible"
        tvUserEmail.text = user?.email ?: "Correo no disponible"
        recyclerDevices = view.findViewById(R.id.recyclerViewDevices)
        fabAddDevice = view.findViewById(R.id.fab_add_device)

        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_logout -> {
                    drawerLayout.closeDrawer(GravityCompat.START)
                    cerrarSesion()
                    true
                }
                else -> false
            }
        }


        // Este método muestra el ícono hamburguesa a la izquierda del título
        val toggle = ActionBarDrawerToggle(
            activity,
            drawerLayout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        setupRecyclerView()
        loadDevicesFromFirestore()

        fabAddDevice.setOnClickListener {
            // Navegar a la pantalla de agregar dispositivo
            findNavController().navigate(R.id.action_homeFragment_to_addDeviceFragment)
        }
    }

    override fun onResume() {
        super.onResume()
        loadDevicesFromFirestore()
    }

    private fun setupRecyclerView() {
        deviceAdapter = DeviceAdapter(deviceList) { selectedDevice ->
            val bundle = Bundle().apply {
                putString("deviceId", selectedDevice.id)
            }
            findNavController().navigate(R.id.action_homeFragment_to_viewDeviceFragment, bundle)
        }

        recyclerDevices.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = deviceAdapter
        }
    }

    private fun loadDevicesFromFirestore() {
        firestore.collection("dispositivos")
            .get()
            .addOnSuccessListener { result ->
                val devices = result.documents.mapNotNull { doc ->
                    try {
                        val data = doc.data ?: return@mapNotNull null

                        Device(
                            id = doc.id,
                            imei = data["imei"] as? String ?: "",
                            marca = data["marca"] as? String ?: "",
                            modelo = data["modelo"] as? String ?: "",
                            cliente = data["cliente"] as? String ?: "",
                            ciudad = data["ciudad"] as? String ?: "",
                            telefono = data["telefono"] as? String ?: "",
                            precio = (data["precio"] as? Number)?.toDouble() ?: 0.0,
                            frecuenciaPago = data["frecuenciaPago"] as? String ?: "",
                            periodoPago = data["periodoPago"] as? String ?: "",
                            fechaInicio = data["fechaInicio"] as? String ?: "",
                            fechaFin = data["fechaFin"] as? String ?: "",
                            montoAPagar = (data["montoAPagar"] as? Number)?.toDouble() ?: 0.0, // 🔹 Conversión segura
                            estado = (data["estado"] as? String)?.equals("bloqueado", ignoreCase = true) ?: false
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                        null
                    }
                }
                deviceList.clear()
                deviceList.addAll(devices)
                deviceAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
            }
    }
    private fun cerrarSesion() {
        // --- OPCIÓN A: Si usas Firebase ---
        FirebaseAuth.getInstance().signOut()

        // Mostrar confirmación
        Toast.makeText(requireContext(), "Sesión cerrada", Toast.LENGTH_SHORT).show()

        // Redirigir al LoginFragment (si usas Navigation Component)
        findNavController().navigate(R.id.loginFragment)
    }

}
