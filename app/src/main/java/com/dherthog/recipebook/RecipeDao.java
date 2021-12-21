package com.dherthog.recipebook;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

/**
 * A Dao for the Recipe class.
 */
@Dao
interface RecipeDao {

    /**
     * Inserts the given Recipes into the database.
     * @param recipes The Recipes to insert
     */
    @Insert
    void insert(Recipe recipes);

    /**
     * Deletes the given Recipes from the database.
     * @param recipes The Recipes to delete
     */
    @Delete
    void delete(Recipe recipes);

    /**
     * Returns an ArrayList of favorited Recipes.
     */
    @Query("SELECT * FROM recipe")
    List<Recipe> getAllRecipes();

    /**
     * Returns an ArrayList of Recipes with the given ID.
     */
    @Query("SELECT * FROM recipe WHERE id = :id")
    List<Recipe> getRecipeByID(int id);
}
