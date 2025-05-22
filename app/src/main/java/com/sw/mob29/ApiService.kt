package com.sw.mob29

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {
    @GET("movies")
    fun getMovies(): Call<List<Movie>>

    @GET("movies/{id}")
    fun getMovieDetails(@Path("id") id: Int): Call<Movie>
}