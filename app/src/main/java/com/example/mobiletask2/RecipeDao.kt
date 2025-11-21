package com.example.mobiletask2

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface RecipeDao {

    @Insert
    suspend fun insertRecipe(recipe: Recipe)

    @Query("SELECT * FROM recipes")
    suspend fun getAllRecipes(): List<Recipe>

    @Query("SELECT DISTINCT category FROM recipes")
    suspend fun getAllCategories(): List<String>

    @Query("SELECT * FROM recipes WHERE category = :selectedCategory")
    suspend fun getRecipesByCategory(selectedCategory: String): List<Recipe>

    @Query("SELECT * FROM recipes WHERE id = :id")
    suspend fun getRecipeById(id: Int): Recipe
}
