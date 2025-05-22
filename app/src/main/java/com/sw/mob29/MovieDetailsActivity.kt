package com.sw.mob29

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MovieDetailsActivity : AppCompatActivity() {

    private lateinit var titleTextView: TextView
    private lateinit var ratingTextView: TextView
    private lateinit var directorTextView: TextView
    private lateinit var releaseDateTextView: TextView
    private lateinit var lengthTextView: TextView
    private lateinit var overviewTextView: TextView
    private lateinit var loadingView: View

    companion object {
        private const val MOVIE_ID_EXTRA = "movie_id_extra"

        fun createIntent(context: Context, movieId: Int): Intent {
            return Intent(context, MovieDetailsActivity::class.java).apply {
                putExtra(MOVIE_ID_EXTRA, movieId)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_movie_details)
//        setContentView()


        titleTextView = findViewById(R.id.movieTitle)
        ratingTextView = findViewById(R.id.movieRating)
        directorTextView = findViewById(R.id.movieDirector)
        releaseDateTextView = findViewById(R.id.movieReleaseDate)
        lengthTextView = findViewById(R.id.movieLength)
        overviewTextView = findViewById(R.id.movieOverview)
        loadingView = findViewById(R.id.loadingView)



        val movieId = intent.getIntExtra(MOVIE_ID_EXTRA, -1)

        if (movieId != -1) {
            loadMovieDetails(movieId)
        } else {
            Toast.makeText(this, "Invalid movie ID", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun loadMovieDetails(movieId: Int) {

        loadingView.visibility = View.VISIBLE

        ApiClient.apiService.getMovieDetails(movieId).enqueue(object : Callback<Movie> {
            override fun onResponse(call: Call<Movie>, response: Response<Movie>) {
                loadingView.visibility = View.GONE

                if (response.isSuccessful) {
                    val movie = response.body()
                    if (movie != null) {
                        displayMovieDetails(movie)
                    } else {
                        showError("Movie details not available")
                    }
                } else {
                    showError("Failed to load movie details")
                }
            }

            override fun onFailure(call: Call<Movie>, t: Throwable) {
                loadingView.visibility = View.GONE
                showError("Network error: ${t.message}")
            }
        })
    }

    private fun displayMovieDetails(movie: Movie) {
        titleTextView.text = movie.title
        ratingTextView.text = "Rating: ${movie.rating}"
        directorTextView.text = "Director: ${movie.director}"
        releaseDateTextView.text = "Release Date: ${movie.release_date}"
        lengthTextView.text = "Length: ${movie.length} Minutes"
        overviewTextView.text = movie.overview
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        finish()
    }
}