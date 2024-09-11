package com.example.sampleapp.adapters

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sampleapp.R
import com.example.sampleapp.data.Country
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

class CountryAdapter(private var countries: List<Country>) : RecyclerView.Adapter<CountryAdapter.CountryViewHolder>(), Filterable {

    private var countriesAll: List<Country> = ArrayList(countries)
    var filteredCountries: List<Country> = ArrayList(countries)
        private set

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CountryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_list, parent, false)
        return CountryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CountryViewHolder, position: Int) {
        val country = filteredCountries[position]

        // Set country name only
        holder.nameTextView.text = country.name?.common ?: "Unknown Country"

        // Set country capital
        holder.capitalTextView.text = country.capital?.firstOrNull() ?: "No Capital"

        // Load flag image
        loadImage(holder.flagImageView, country.flags?.png)


    }

    override fun getItemCount(): Int = filteredCountries.size

    override fun getFilter(): Filter {
        return countryFilter
    }

    private val countryFilter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val filteredList: MutableList<Country> = ArrayList()
            if (constraint == null || constraint.isEmpty()) {
                filteredList.addAll(countriesAll)
            } else {
                val filterPattern = constraint.toString().uppercase().trim()
                filteredList.addAll(countriesAll.filter { country ->
                    country.name?.common?.uppercase()?.contains(filterPattern) == true
                })
            }
            val results = FilterResults()
            results.values = filteredList
            return results
        }

        @Suppress("UNCHECKED_CAST")
        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            filteredCountries = results?.values as? List<Country> ?: emptyList()
            notifyDataSetChanged()
        }
    }

    inner class CountryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.countryName)
        val flagImageView: ImageView = itemView.findViewById(R.id.countryFlag)
        val capitalTextView: TextView = itemView.findViewById(R.id.countryCapital)
    }

    // Inside your CountryAdapter class
    private fun loadImage(imageView: ImageView, url: String?) {
        if (url == null) {
            imageView.setImageResource(R.drawable.error_image) // Placeholder image if URL is null
            return
        }

        // Use a coroutine to handle background work
        imageView.post {
            // Launch a coroutine to fetch the image
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val bitmap = fetchBitmapFromUrl(url)
                    withContext(Dispatchers.Main) {
                        imageView.setImageBitmap(bitmap)
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        imageView.setImageResource(R.drawable.error_image) // Error image if loading fails
                    }
                }
            }
        }
    }

    private fun fetchBitmapFromUrl(urlString: String): Bitmap? {
        var bitmap: Bitmap? = null
        val url = URL(urlString)
        var connection: HttpURLConnection? = null
        try {
            connection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val inputStream = connection.inputStream
            bitmap = BitmapFactory.decodeStream(inputStream)
        } finally {
            connection?.disconnect()
        }
        return bitmap
    }


}