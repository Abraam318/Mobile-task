package com.example.mobiletask2

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {

    private lateinit var titleInput: EditText
    private lateinit var ingredientsInput: EditText
    private lateinit var categoryInput: EditText
    private lateinit var addButton: Button
    private lateinit var categorySpinner: Spinner
    private lateinit var filterButton: Button
    private lateinit var showAllButton: Button
    private lateinit var recipeListView: ListView

    private lateinit var database: AppDatabase
    private lateinit var recipeDao: RecipeDao

    private var allRecipes = mutableListOf<Recipe>()
    private lateinit var adapter: ArrayAdapter<String>

    private val scope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize database
        database = AppDatabase.getDatabase(this)
        recipeDao = database.recipeDao()

        // Initialize views
        titleInput = findViewById(R.id.titleInput)
        ingredientsInput = findViewById(R.id.ingredientsInput)
        categoryInput = findViewById(R.id.categoryInput)
        addButton = findViewById(R.id.addButton)
        categorySpinner = findViewById(R.id.categorySpinner)
        filterButton = findViewById(R.id.filterButton)
        showAllButton = findViewById(R.id.showAllButton)
        recipeListView = findViewById(R.id.recipeListView)

        // Set up ListView adapter
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_2, android.R.id.text1, mutableListOf())
        recipeListView.adapter = adapter

        // Load recipes
        loadAllRecipes()
        loadCategories()

        // Add recipe button click
        addButton.setOnClickListener {
            addRecipe()
        }

        // Filter button click
        filterButton.setOnClickListener {
            filterByCategory()
        }

        // Show All button click
        showAllButton.setOnClickListener {
            loadAllRecipes()
        }

        // ListView item click - open details
        recipeListView.setOnItemClickListener { _, _, position, _ ->
            val recipe = allRecipes[position]
            openRecipeDetails(recipe)
        }
    }

    private fun addRecipe() {
        val title = titleInput.text.toString().trim()
        val ingredients = ingredientsInput.text.toString().trim()
        val category = categoryInput.text.toString().trim()

        if (title.isEmpty() || ingredients.isEmpty() || category.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val recipe = Recipe(title = title, ingredients = ingredients, category = category)

        scope.launch {
            withContext(Dispatchers.IO) {
                recipeDao.insertRecipe(recipe)
            }

            // Clear inputs
            titleInput.text.clear()
            ingredientsInput.text.clear()
            categoryInput.text.clear()

            Toast.makeText(this@MainActivity, "Recipe added!", Toast.LENGTH_SHORT).show()

            loadAllRecipes()
            loadCategories()
        }
    }

    private fun loadAllRecipes() {
        scope.launch {
            allRecipes = withContext(Dispatchers.IO) {
                recipeDao.getAllRecipes().toMutableList()
            }

            displayRecipes(allRecipes)
        }
    }

    private fun loadCategories() {
        scope.launch {
            val categories = withContext(Dispatchers.IO) {
                recipeDao.getAllCategories()
            }

            val spinnerAdapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_item, categories)
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            categorySpinner.adapter = spinnerAdapter
        }
    }

    private fun filterByCategory() {
        val selectedCategory = categorySpinner.selectedItem?.toString()

        if (selectedCategory == null) {
            Toast.makeText(this, "No category selected", Toast.LENGTH_SHORT).show()
            return
        }

        scope.launch {
            val filteredRecipes = withContext(Dispatchers.IO) {
                recipeDao.getRecipesByCategory(selectedCategory)
            }

            allRecipes = filteredRecipes.toMutableList()
            displayRecipes(allRecipes)
        }
    }

    private fun displayRecipes(recipes: List<Recipe>) {
        val displayList = recipes.map { "${it.title}\n${it.category}" }
        adapter.clear()
        adapter.addAll(displayList)
        adapter.notifyDataSetChanged()
    }

    private fun openRecipeDetails(recipe: Recipe) {
        val intent = Intent(this, RecipeDetailsActivity::class.java)
        intent.putExtra("RECIPE_ID", recipe.id)
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}
