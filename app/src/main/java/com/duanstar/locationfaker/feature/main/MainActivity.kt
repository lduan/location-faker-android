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
import com.duanstar.locationfaker.ui.Screens
import com.duanstar.locationfaker.ui.theme.AppTheme
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
                    startDestination = Screens.Main.route,
                ) {
                    composable(Screens.Main.route) {
                        MainLayout(
                            onSearchClick = { bounds ->
                                navController.navigate(Screens.Search.makeRoute(bounds))
                            }
                        )
                    }

                    composable(Screens.Search.route, arguments = Screens.Search.arguments) {
                        val cameraBounds = Screens.Search.getBoundsArgument(it)
                        SearchLayout(
                            cameraBounds = cameraBounds,
                            onBack = navController::popBackStack
                        )
                    }
                }
            }
        }
    }
}