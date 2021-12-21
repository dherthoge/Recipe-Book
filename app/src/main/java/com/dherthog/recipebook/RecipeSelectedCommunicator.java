package com.dherthog.recipebook;

/**
 * Interface for MainActivity to listen for category selection.
 */
public interface RecipeSelectedCommunicator {

    /**
     * Notifies the ActivityCommunicator a Recipe has been selected.
     * @param id The ID of the selected Recipe
     */
    void recipeSelected(int id);
}
