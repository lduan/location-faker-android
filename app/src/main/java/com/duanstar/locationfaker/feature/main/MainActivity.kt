package com.duanstar.locationfaker.feature.main

import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
import android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.duanstar.locationfaker.feature.search.SearchLayout
import com.duanstar.locationfaker.ui.theme.AppTheme
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    companion object {

        fun newIntent(context: Context) = Intent(context, MainActivity::class.java)
            .apply {
                flags = FLAG_ACTIVITY_CLEAR_TOP or FLAG_ACTIVITY_SINGLE_TOP
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            AppTheme {
                NavHost(
                    navController = navController,
                    startDestination = "main",
                ) {
                    composable("main") {
                        MainLayout(
                            onSearchClick = { position ->
                                navController.navigate("search/${position.latitude}/${position.longitude}")
                            }
                        )
                    }
                    composable("search/{lat}/{lng}") {
                        val lat = it.arguments?.getDouble("lat")!!
                        val lng = it.arguments?.getDouble("lng")!!
                        SearchLayout(
                            position = LatLng(lat, lng),
                            onBack = navController::popBackStack
                        )
                    }
                }
            }
        }
    }
}