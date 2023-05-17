package com.example.buy_ingredients.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.graphics.vector.ImageVector


sealed class NavigationItem(val route: String, val label: String, val icon: ImageVector) {
    object VendorDashboard : NavigationItem("VendorDashboard", "Dashboard", Icons.Filled.List)
    object VendorInventory : NavigationItem("Viewinventory", "Inventory", Icons.Filled.ShoppingCart)
}

