package com.dherthog.recipebook;

/**
 * Interface for MainActivity to listen for category begin selected.
 */
public interface CategorySelectedCommunicator {

    /**
     * Notifies the ActivityCommunicator a category has been selected.
     * @param title The title of the selected category
     */
    void categorySelected(String title);
}
