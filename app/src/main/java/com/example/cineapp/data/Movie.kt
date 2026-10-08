package com.example.cineapp.data

data class Movie(
    val id: Int,
    val title: String,
    val overview: String,
    val poster_path: String?,
    val vote_average: Double,
    val release_date: String?
) {
    val posterUrl: String?
        get() = poster_path?.let {
            "https://image.tmdb.org/t/p/w500$it"
        }
}