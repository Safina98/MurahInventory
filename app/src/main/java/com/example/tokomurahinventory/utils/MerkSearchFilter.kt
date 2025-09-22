package com.example.tokomurahinventory.utils

import com.example.tokomurahinventory.models.MerkTable

object MerkSearchFilter {

    fun filter(query: String?, list: List<MerkTable>): List<MerkTable> {
        if (query.isNullOrBlank()) return list

        val normalizedInput = query.lowercase().trim()
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

        return list.filter { merk ->
            val text = merk.namaMerk.lowercase()
            val itemWords = text.split(" ")

            if (inputWords.size > 1) {
                inputWords.all { inputWord ->
                    itemWords.any { itemWord ->
                        itemWord.contains(inputWord) ||
                                similarity(inputWord, itemWord) >= 0.6 ||
                                levenshtein(inputWord, itemWord) <= 2
                    }
                }
            } else {
                if (normalizedInput.all { it.isDigit() }) {
                    text.contains(normalizedInput)
                } else {
                    itemWords.any { word ->
                        word.contains(normalizedInput) ||
                                similarity(normalizedInput, word) >= 0.6 ||
                                levenshtein(normalizedInput, word) <= 2
                    }
                }
            }
        }
    }
}
