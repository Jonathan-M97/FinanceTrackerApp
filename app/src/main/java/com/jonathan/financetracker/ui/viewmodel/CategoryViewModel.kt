package com.jonathan.financetracker.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.jonathan.financetracker.data.Category
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class CategoryViewModel : ViewModel() {

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories

    fun fetchCategories() {
        viewModelScope.launch {
            val db = Firebase.firestore
            try {
                val result = db.collection("categories").get().await()
                val categoryList = result.map { document ->
                    document.toObject(Category::class.java)
                }
                _categories.value = categoryList
            } catch (e: Exception) {
                Log.e("CategoryViewModel", "Error fetching categories", e)
            }
        }
    }
}
