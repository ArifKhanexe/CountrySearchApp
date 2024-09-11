package com.example.sampleapp
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.sampleapp.adapters.CountryAdapter
import com.example.sampleapp.adapters.ImageCarouselAdapter
import com.example.sampleapp.data.CountryRepository
import com.example.sampleapp.data.CountryViewModelFactory
import com.example.sampleapp.viewmodel.CountryViewModel
import com.example.sampleapp.viewmodel.MainActivityViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {


    private lateinit var countryViewModel: CountryViewModel
    private lateinit var countryAdapter: CountryAdapter
    private lateinit var viewModel: MainActivityViewModel
    private lateinit var adapter: ImageCarouselAdapter
    private lateinit var dots: Array<ImageView>
    private lateinit var viewPager: ViewPager2
    private lateinit var dotsLayout: LinearLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var progressBar: ProgressBar
    // Hold reference to OnPageChangeCallback so we can remove it later
    private var pageChangeCallback: ViewPager2.OnPageChangeCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewPager = findViewById(R.id.viewPager)
        dotsLayout = findViewById(R.id.dotsLayout)
        viewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)
        searchView = findViewById(R.id.searchView)
        progressBar = findViewById(R.id.progressBar)
        recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)

        // Initialize the ViewModel and Repository
        val repository = CountryRepository()
        countryViewModel = ViewModelProvider(this, CountryViewModelFactory(repository))
            .get(CountryViewModel::class.java)

        // Observe LiveData
        countryViewModel.countries.observe(this) { countryList ->
            countryAdapter = CountryAdapter(countryList)
            recyclerView.adapter = countryAdapter
        }

        // Observe loading state
        countryViewModel.isLoading.observe(this) { isLoading ->
            if (isLoading) {
                progressBar.visibility = View.VISIBLE
            } else {
                progressBar.visibility = View.GONE
            }
        }

        // Fetch countries from API
        countryViewModel.fetchCountries()


        // Set up SearchView to filter ListView
        searchView.setQueryHint("India");

        // Handle SearchView click directly to trigger search
        searchView.setOnClickListener {
            searchView.setIconified(false)  // Expand the search view immediately when clicked
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                countryAdapter.getFilter().filter(newText)
                return false
            }
        })

        //setting the fab for opening bottom sheet
        setupFAB()

        // Observe the image list and set up the ViewPager2 adapter
        viewModel.images.observe(this, Observer { images ->
            adapter = ImageCarouselAdapter(images)
            viewPager.adapter = adapter
            createDots(images.size)
        })

        // Handle page swiping and update the position in ViewModel
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                viewModel.updatePosition(position)
            }
        })

        // Observe current position and update dots accordingly
        viewModel.currentPosition.observe(this, Observer { position ->
            viewPager.setCurrentItem(position, true)
            updateDots(position)
        })

    }

    private fun setupFAB() {
        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener {
            showStatisticsBottomSheet()
        }
    }

    private fun showStatisticsBottomSheet() {
        if (!::countryAdapter.isInitialized || countryAdapter.filteredCountries.isEmpty()) {
            // Show a message or return if adapter is not ready
            Log.d("MainActivity", "Adapter not initialized or no filtered countries available.")
            return
        }

        val layoutManager = recyclerView.layoutManager as LinearLayoutManager
        val firstVisiblePosition = layoutManager.findFirstCompletelyVisibleItemPosition()
        val lastVisiblePosition = layoutManager.findLastCompletelyVisibleItemPosition()

        Log.d("MainActivity", "First Visible: $firstVisiblePosition, Last Visible: $lastVisiblePosition")

        // Ensure valid positions are returned
        if (firstVisiblePosition == RecyclerView.NO_POSITION || lastVisiblePosition == RecyclerView.NO_POSITION) {
            Log.d("MainActivity", "Invalid positions detected. Exiting.")
            return
        }

        val visibleItems = countryAdapter.filteredCountries.subList(firstVisiblePosition, lastVisiblePosition + 1)
        Log.d("MainActivity", "Visible Items Count: ${visibleItems.size}")

        val bottomSheetDialog = BottomSheetDialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_statistics, null)
        bottomSheetDialog.setContentView(view)

        val itemCountTextView: TextView = view.findViewById(R.id.itemCountTextView)
        val topCharactersTextView: TextView = view.findViewById(R.id.topCharactersTextView)

        itemCountTextView.text = "Visible Item Count: ${visibleItems.size}"

        val charFrequenciesPerItem = StringBuilder()

        visibleItems.forEach { country ->
            val countryName = country.name?.common ?: ""
            val capitalName = country.capital?.get(0) ?: ""

            val combinedName = "$countryName $capitalName"
            val charFrequency = calculateCharacterFrequency(combinedName)
            val totalCharCount = combinedName.replace(" ", "").length // Calculate total characters excluding spaces

            charFrequenciesPerItem.append("Country: $countryName, Capital: $capitalName\n")
            charFrequenciesPerItem.append("Character Frequencies: $charFrequency\n")
            charFrequenciesPerItem.append("Total Character Count: $totalCharCount\n\n")
        }

        topCharactersTextView.text = charFrequenciesPerItem.toString()

        bottomSheetDialog.show()

    }

    private fun calculateCharacterFrequency(item: String): String {
        val charCount = mutableMapOf<Char, Int>()

        item.forEach { char ->
            if (char.isLetter()) {
                charCount[char.lowercaseChar()] = charCount.getOrDefault(char.lowercaseChar(), 0) + 1
            }
        }

        // Sort by frequency and get the top 3 characters
        return charCount.entries
            .sortedByDescending { it.value }
            .take(3)
            .joinToString(separator = ", ") { "${it.key} = ${it.value}" }
    }

    private fun createDots(count: Int) {
        dots = Array(count) { ImageView(this) }
        dotsLayout.removeAllViews()

        for (i in dots.indices) {
            dots[i] = ImageView(this).apply {
                setImageDrawable(ContextCompat.getDrawable(this@MainActivity, R.drawable.inactive_dot))
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(8, 0, 8, 0)
                dotsLayout.addView(this, params)
            }
        }
        dots[0].setImageDrawable(ContextCompat.getDrawable(this, R.drawable.active_dot))  // Set the first dot as active
    }

    private fun updateDots(position: Int) {
        for (i in dots.indices) {
            val drawableId = if (i == position) R.drawable.active_dot else R.drawable.inactive_dot
            dots[i].setImageDrawable(ContextCompat.getDrawable(this, drawableId))
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        // Unregister OnPageChangeCallback to prevent memory leaks
        pageChangeCallback?.let {
            viewPager.unregisterOnPageChangeCallback(it)
        }
    }

}