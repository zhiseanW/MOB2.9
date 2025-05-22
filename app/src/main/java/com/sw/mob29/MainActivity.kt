package com.sw.mob29

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SearchView
import android.widget.Spinner
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var movieAdapter: MovieAdapter
    private lateinit var movieList: MutableList<Movie>
    private lateinit var searchView: SearchView
    private lateinit var sortBySpinner: Spinner
    private lateinit var sortOrderSpinner: Spinner
    private lateinit var loadingView: View
    private lateinit var emptyView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        recyclerView = findViewById(R.id.recyclerView)
        searchView = findViewById(R.id.searchView)
        sortBySpinner = findViewById(R.id.sortBySpinner)
        sortOrderSpinner = findViewById(R.id.sortOrderSpinner)
        loadingView = findViewById(R.id.loadingView)
        emptyView = findViewById(R.id.emptyView)


        recyclerView.layoutManager = LinearLayoutManager(this)
        movieList = mutableListOf()
        movieAdapter = MovieAdapter(movieList) { movie ->

            val intent = MovieDetailsActivity.createIntent(this, movie.id)
            startActivity(intent)
        }
        recyclerView.adapter = movieAdapter

        setupSortSpinners()


        setupSearchView()

        loadMovies()
    }

    private fun setupSortSpinners() {

        val sortByOptions = arrayOf("Title", "Rating")
        val sortByAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, sortByOptions)
        sortByAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        sortBySpinner.adapter = sortByAdapter


        val sortOrderOptions = arrayOf("Asc", "Desc")
        val sortOrderAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, sortOrderOptions)
        sortOrderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        sortOrderSpinner.adapter = sortOrderAdapter

        sortBySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                sortMovies()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        sortOrderSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                sortMovies()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupSearchView() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterMovies(newText)
                return true
            }
        })
    }

    private fun loadMovies() {
        // Show loading view
        loadingView.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        emptyView.visibility = View.GONE

        ApiClient.apiService.getMovies().enqueue(object : Callback<List<Movie>> {
            override fun onResponse(call: Call<List<Movie>>, response: Response<List<Movie>>) {
                loadingView.visibility = View.GONE

                if (response.isSuccessful) {
                    val movies = response.body()
                    if (movies != null && movies.isNotEmpty()) {
                        movieList.clear()
                        movieList.addAll(movies)
                        sortMovies()
                        recyclerView.visibility = View.VISIBLE
                    } else {
                        showEmptyView()
                    }
                } else {
                    showEmptyView()
                    Toast.makeText(this@MainActivity, "Failed to load movies", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Movie>>, t: Throwable) {
                loadingView.visibility = View.GONE
                showEmptyView()
                Toast.makeText(this@MainActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showEmptyView() {
        recyclerView.visibility = View.GONE
        emptyView.visibility = View.VISIBLE
    }

    private fun sortMovies() {
        val sortBy = sortBySpinner.selectedItem.toString()
        val sortOrder = sortOrderSpinner.selectedItem.toString()

        val sortedList = when (sortBy) {
            "Title" -> {
                if (sortOrder == "Asc") {
                    movieList.sortedBy { it.title }
                } else {
                    movieList.sortedByDescending { it.title }
                }
            }
            "Rating" -> {
                if (sortOrder == "Asc") {
                    movieList.sortedBy { it.rating }
                } else {
                    movieList.sortedByDescending { it.rating }
                }
            }
            else -> movieList
        }

        movieList.clear()
        movieList.addAll(sortedList)
        movieAdapter.notifyDataSetChanged()
    }

    private fun filterMovies(query: String?) {
        if (query.isNullOrEmpty()) {
            loadMovies()
            return
        }

        ApiClient.apiService.getMovies().enqueue(object : Callback<List<Movie>> {
            override fun onResponse(call: Call<List<Movie>>, response: Response<List<Movie>>) {
                if (response.isSuccessful) {
                    val allMovies = response.body()
                    if (allMovies != null) {
                        val filteredMovies = allMovies.filter {
                            it.title.contains(query, ignoreCase = true)
                        }

                        movieList.clear()

                        if (filteredMovies.isEmpty()) {
                            showEmptyView()
                        } else {
                            movieList.addAll(filteredMovies)
                            sortMovies()
                            recyclerView.visibility = View.VISIBLE
                            emptyView.visibility = View.GONE
                        }
                    } else {
                        showEmptyView()
                    }
                } else {
                    showEmptyView()
                }
            }

            override fun onFailure(call: Call<List<Movie>>, t: Throwable) {
                showEmptyView()
            }
        })
    }
}