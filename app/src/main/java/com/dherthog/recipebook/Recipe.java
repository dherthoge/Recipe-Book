package com.dherthog.recipebook;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * An Object to store information for a Recipe
 */
@Entity
public class Recipe {

    @PrimaryKey
    private final int id; // The id of the Recipe in TMDB
    @ColumnInfo
    private final String name;
    @ColumnInfo
    private final String imageUrl; // The url for the image of the Recipe in TMDB

    /**
     * @param id The ID of the Recipe in the TMDB
     * @param name The name of the Recipe in the TMDB
     * @param imageUrl The imageUrl of the Recipe in the TMDB
     */
    public Recipe(int id, String name, String imageUrl) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
