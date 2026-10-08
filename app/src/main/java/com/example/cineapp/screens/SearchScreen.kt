package com.example.cineapp.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import coil.compose.AsyncImage
import com.example.cineapp.BuildConfig
import com.example.cineapp.data.Movie
import com.example.cineapp.data.RetrofitClient
import kotlinx.coroutines.launch

@Composable
fun SearchScreen(
    onMovieClick: (Int) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var movies by remember { mutableStateOf<List<Movie>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var searched by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val keyboard = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    fun searchMovies() {
        val searchText = query.trim()
        if (searchText.isBlank()) return

        keyboard?.hide()
        focusManager.clearFocus()

        scope.launch {
            loading = true
            error = null
            searched = true
            movies = emptyList()

            try {
                movies = RetrofitClient.api.searchMovies(
                    query = searchText,
                    token = "Bearer ${BuildConfig.TMDB_BEARER_TOKEN}"
                ).results
            } catch (e: Exception) {
                error = if (
                    BuildConfig.TMDB_BEARER_TOKEN.isBlank()
                ) {
                    "Falta configurar el token de TMDB."
                } else {
                    "No se pudo realizar la búsqueda. " +
                            "Comprueba tu conexión e inténtalo de nuevo."
                }
            } finally {
                loading = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Buscar películas",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Nombre de la película") },
            placeholder = { Text("Ej. Interestelar") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(
                onSearch = { searchMovies() }
            ),
            trailingIcon = {
                if (query.isNotEmpty()) {
                    TextButton(
                        onClick = {
                            query = ""
                            movies = emptyList()
                            error = null
                            searched = false
                        }
                    ) {
                        Text("Borrar")
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { searchMovies() },
            modifier = Modifier.fillMaxWidth(),
            enabled = query.isNotBlank() && !loading
        ) {
            Text("Buscar")
        }

        Spacer(modifier = Modifier.height(12.dp))

        when {
            loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            error != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = error!!,
                        color = MaterialTheme.colorScheme.error
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(onClick = { searchMovies() }) {
                        Text("Reintentar")
                    }
                }
            }

            searched && movies.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No se encontraron películas.")
                }
            }

            movies.isNotEmpty() -> {
                Text(
                    text = "Resultados: ${movies.size}",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    horizontalArrangement =
                        Arrangement.spacedBy(12.dp),
                    verticalArrangement =
                        Arrangement.spacedBy(16.dp)
                ) {
                    items(
                        items = movies,
                        key = { it.id }
                    ) { movie ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onMovieClick(movie.id)
                                }
                        ) {
                            Column {
                                AsyncImage(
                                    model = movie.posterUrl,
                                    contentDescription =
                                        "Póster de ${movie.title}",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(230.dp),
                                    contentScale = ContentScale.Crop
                                )

                                Column(
                                    modifier = Modifier.padding(10.dp)
                                ) {
                                    Text(
                                        text = movie.title,
                                        style = MaterialTheme
                                            .typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Spacer(
                                        modifier = Modifier.height(6.dp)
                                    )

                                    Text(
                                        text = "⭐ %.1f/10".format(
                                            movie.vote_average
                                        ),
                                        style = MaterialTheme
                                            .typography.bodyMedium
                                    )

                                    Text(
                                        text = movie.release_date
                                            ?.takeIf { it.isNotBlank() }
                                            ?: "Fecha no disponible",
                                        style = MaterialTheme
                                            .typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }
            }

            else -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Escribe el nombre de una película " +
                                "para comenzar a buscar.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}