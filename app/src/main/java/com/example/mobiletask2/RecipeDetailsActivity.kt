package com.example.mobiletask2

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*

class RecipeDetailsActivity : AppCompatActivity() {

    private lateinit var titleTextView: TextView
    private lateinit var categoryTextView: TextView
    private lateinit var ingredientsTextView: TextView

    private lateinit var database: AppDatabase
    private lateinit var recipeDao: RecipeDao

    private val scope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_details)

        // Initialize database
        database = AppDatabase.getDatabase(this)
        recipeDao = database.recipeDao()

        // Initialize views
        titleTextView = findViewById(R.id.detailTitle)
        categoryTextView = findViewById(R.id.detailCategory)
        ingredientsTextView = findViewById(R.id.detailIngredients)

        // Get recipe ID from intent
        val recipeId = intent.getIntExtra("RECIPE_ID", -1)

        if (recipeId != -1) {
            loadRecipeDetails(recipeId)
        }
    }

    private fun loadRecipeDetails(recipeId: Int) {
        scope.launch {
            val recipe = withContext(Dispatchers.IO) {
                recipeDao.getRecipeById(recipeId)
            }

            // Display recipe details
            titleTextView.text = recipe.title
            categoryTextView.text = "Category: ${recipe.category}"
            ingredientsTextView.text = recipe.ingredients
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}