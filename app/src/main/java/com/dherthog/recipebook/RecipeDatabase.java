package com.dherthog.recipebook;

import androidx.room.Database;
import androidx.room.RoomDatabase;

/**
 * A representation of a database to keep track of user's favorited Recipes.
 */
@Database(entities = {Recipe.class}, version = 1)
public abstract class RecipeDatabase extends RoomDatabase {

    /**
     * Get's the DAO for a Recipe.
     * @return A RecipeDao instance
     */
    public abstract RecipeDao getRecipeDAO();
}
