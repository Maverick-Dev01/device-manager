package com.maverick.adminapp.utils

import androidx.navigation.NavController
import com.maverick.adminapp.R

object NavigationHelper {
    fun navigateToHome(navController: NavController) {
        navController.navigate(R.id.action_addDeviceFragment_to_homeFragment)
    }

    fun navigateToHomeTwo(navController: NavController) {
        navController.navigate(R.id.action_viewDeviceFragment_to_homeFragment)
    }

}
