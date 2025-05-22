package com.example.tokomurahinventory.adapters

import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.TextView

class SimilarWordAdapter(
    context: Context,
    private var originalList: List<String> = listOf()
) : ArrayAdapter<String>(context, android.R.layout.simple_dropdown_item_1line, ArrayList()) {
    private val suggestions = ArrayList<String>()
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val results = FilterResults()
                suggestions.clear()

                if (constraint.isNullOrEmpty()) {
                    suggestions.addAll(originalList)
                } else {
                    val filteredList = filterSuggestions(constraint.toString(), originalList)
                    suggestions.addAll(filteredList)
                }

                results.values = suggestions
                results.count = suggestions.size
                return results
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                clear()
                if (results?.values is List<*>) {
                    addAll(results.values as List<String>)
                }
                notifyDataSetChanged()
            }
        }
    }

    fun filterSuggestions(input: String, list: List<String>): List<String> {
        val normalizedInput = input.lowercase().trim()
        val inputWords = normalizedInput.split(" ")

        fun levenshtein(a: String, b: String): Int {
            val dp = Array(a.length + 1) { IntArray(b.length + 1) }
            for (i in 0..a.length) dp[i][0] = i
            for (j in 0..b.length) dp[0][j] = j
            for (i in 1..a.length) {
                for (j in 1..b.length) {
                    val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                    dp[i][j] = minOf(
                        dp[i - 1][j] + 1,
                        dp[i][j - 1] + 1,
                        dp[i - 1][j - 1] + cost
                    )
                }
            }
            return dp[a.length][b.length]
        }

        fun similarity(s1: String, s2: String): Double {
            val maxLen = maxOf(s1.length, s2.length)
            return if (maxLen == 0) 1.0 else (maxLen - levenshtein(s1, s2)) / maxLen.toDouble()
        }

        return list.filter { item ->
            val itemWords = item.lowercase().split(" ")

            // If input looks like multiple words, do fuzzy matching on each word
            if (inputWords.size > 1) {
                // Match all input words somewhere in item words (fuzzy)
                inputWords.all { inputWord ->
                    itemWords.any { itemWord ->
                        itemWord.contains(inputWord) ||
                                similarity(inputWord, itemWord) >= 0.6 ||
                                levenshtein(inputWord, itemWord) <= 2
                    }
                }
            } else {
                // Single word or number input
                // If input is numeric and item contains numbers, match substring anywhere
                if (normalizedInput.all { it.isDigit() }) {
                    item.contains(normalizedInput)
                } else {
                    // Text input, do fuzzy word matching on any word
                    itemWords.any { word ->
                        word.contains(normalizedInput) ||
                                similarity(normalizedInput, word) >= 0.6 ||
                                levenshtein(normalizedInput, word) <= 2
                    }
                }
            }
        }
    }

    override fun getItem(position: Int): String? {
        return super.getItem(position)
    }
    fun updateData(newList: List<String>) {
        originalList = newList
        notifyDataSetChanged()
    }
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getView(position, convertView, parent)
        val item = getItem(position)
        (view as TextView).text = item
        return view
    }


}
