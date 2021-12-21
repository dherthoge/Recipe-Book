package com.dherthog.recipebook;

/**
 * Interface for MainActivity to listen for a recipe being favorited.
 */
public interface FavoriteCommunicator {

    /**
     * Notifies the ActivityCommunicator a Recipe has been favorited.
     * @param recipe The favorited Recipe
     */
    void recipeFavorited(Recipe recipe);
}
