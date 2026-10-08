package com.example.cineapp.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import com.example.cineapp.BuildConfig
import com.example.cineapp.data.Movie
import com.example.cineapp.data.RetrofitClient
import kotlinx.coroutines.launch

@Composable
fun MovieTabs(
    onMovieClick: (Int) -> Unit
) {
    val tabs = listOf(
        "Populares",
        "Mejor valoradas",
        "Próximamente"
    )

    var selectedTab by remember { mutableIntStateOf(0) }
    var movies by remember { mutableStateOf<List<Movie>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val token = "Bearer ${BuildConfig.TMDB_BEARER_TOKEN}"

    fun loadMovies(tab: Int) {
        scope.launch {
            loading = true
            error = null
            movies = emptyList()

            try {
                val response = when (tab) {
                    0 -> RetrofitClient.api.getPopularMovies(token)
                    1 -> RetrofitClient.api.getTopRatedMovies(token)
                    else -> RetrofitClient.api.getUpcomingMovies(token)
                }

                movies = response.results
            } catch (e: Exception) {
                error = when {
                    BuildConfig.TMDB_BEARER_TOKEN.isBlank() ->
                        "Falta configurar el token de TMDB."
                    else ->
                        "No se pudieron cargar las películas. " +
                                "Comprueba tu conexión y el token de TMDB."
                }
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(selectedTab) {
        loadMovies(selectedTab)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        when {
            loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            error != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(error!!)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = { loadMovies(selectedTab) }) {
                        Text("Reintentar")
                    }
                }
            }

            movies.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No hay películas disponibles.")
                }
            }

            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    horizontalArrangement =
                        Arrangement.spacedBy(12.dp),
                    verticalArrangement =
                        Arrangement.spacedBy(16.dp)
                ) {
                    items(movies, key = { it.id }) { movie ->
                        MovieCard(
                            movie = movie,
                            onClick = { onMovieClick(movie.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MovieCard(
    movie: Movie,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column {
            AsyncImage(
                model = movie.posterUrl,
                contentDescription = "Póster de ${movie.title}",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier.padding(10.dp)
            ) {
                Text(
                    text = movie.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "⭐ %.1f/10".format(movie.vote_average),
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = movie.release_date
                        ?.takeIf { it.isNotBlank() }
                        ?: "Estreno sin fecha",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}