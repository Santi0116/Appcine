package com.example.cineapp.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.cineapp.BuildConfig
import com.example.cineapp.data.Movie
import com.example.cineapp.data.RetrofitClient
import kotlinx.coroutines.launch

@Composable
fun MovieDetailScreen(
    movieId: Int,
    onBack: () -> Unit
) {
    var movie by remember(movieId) {
        mutableStateOf<Movie?>(null)
    }
    var loading by remember(movieId) {
        mutableStateOf(true)
    }
    var error by remember(movieId) {
        mutableStateOf<String?>(null)
    }

    val scope = rememberCoroutineScope()

    LaunchedEffect(movieId) {
        loading = true
        error = null

        try {
            movie = RetrofitClient.api.getMovieDetails(
                movieId = movieId,
                token = "Bearer ${BuildConfig.TMDB_BEARER_TOKEN}"
            )
        } catch (e: Exception) {
            error = "No se pudo cargar el detalle de la película."
        } finally {
            loading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = onBack) {
            Text("Volver")
        }

        Spacer(modifier = Modifier.height(16.dp))

        when {
            loading -> CircularProgressIndicator()

            error != null -> {
                Text(error!!)
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        scope.launch {
                            loading = true
                            error = null
                            try {
                                movie = RetrofitClient.api.getMovieDetails(
                                    movieId = movieId,
                                    token = "Bearer ${BuildConfig.TMDB_BEARER_TOKEN}"
                                )
                            } catch (e: Exception) {
                                error = "Error al consultar TMDB."
                            } finally {
                                loading = false
                            }
                        }
                    }
                ) {
                    Text("Reintentar")
                }
            }

            movie != null -> {
                val selectedMovie = movie!!

                AsyncImage(
                    model = selectedMovie.posterUrl,
                    contentDescription = "Póster de ${selectedMovie.title}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp),
                    contentScale = ContentScale.Fit
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = selectedMovie.title,
                    style = MaterialTheme.typography.headlineMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "⭐ %.1f/10".format(
                        selectedMovie.vote_average
                    ),
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Fecha de estreno: " +
                            (selectedMovie.release_date
                                ?.takeIf { it.isNotBlank() }
                                ?: "No disponible")
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Sinopsis",
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = selectedMovie.overview
                        .ifBlank { "No hay sinopsis disponible." },
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}