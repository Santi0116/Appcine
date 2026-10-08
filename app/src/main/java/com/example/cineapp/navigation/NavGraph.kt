package com.example.cineapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.cineapp.screens.AboutScreen
import com.example.cineapp.screens.FavoritesScreen
import com.example.cineapp.screens.MovieDetailScreen
import com.example.cineapp.screens.SearchScreen

object Routes {
    const val HOME = "home"
    const val DETAILS = "details/{movieId}"
    const val FAVORITES = "favorites"
    const val ABOUT = "about"
    const val SEARCH = "search"
}

@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(

        navController = navController,
        startDestination = Routes.HOME,
        modifier = modifier

    ) {
        composable(Routes.SEARCH) {
            SearchScreen(
                onMovieClick = { movieId ->
                    navController.navigate("details/$movieId")
                }
            )
        }

        composable(Routes.HOME) {
            MovieTabs(
                onMovieClick = { movieId ->
                    navController.navigate("details/$movieId")
                }
            )
        }

        composable(
            route = Routes.DETAILS,
            arguments = listOf(
                navArgument("movieId") {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->
            val movieId =
                backStackEntry.arguments?.getInt("movieId") ?: 0

            MovieDetailScreen(
                movieId = movieId,
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.FAVORITES) {
            FavoritesScreen()
        }

        composable(Routes.ABOUT) {
            AboutScreen()
        }
    }
}